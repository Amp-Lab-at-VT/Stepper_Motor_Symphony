package sms;

import org.jfugue.midi.MidiFileManager;
import org.jfugue.pattern.Pattern;
import org.jfugue.pattern.Token;

import javax.sound.midi.InvalidMidiDataException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;


/**
 * Parses MIDI data to create a list of notes contained in the file
 *
 * @author Dylan Green
 * @version 2023.01.13
 */
public class Parser {

    private static final int JFUGUE_BEATS_PER_MEASURE = 4;
    private static final int MINUTES_TO_HUNDREDTHS_OF_SECOND = 6000;

    private ArrayList<PiecewiseFuncEntry> tempoFunction;

    // A counter to store the start time of the current note being processed, in measures since the beginning of the
    // piece including a fraction of the current measure
    private double currentMeasure = 0.0;
    private double currentNoteStartTime = 0.0;

    private boolean preserveVoices = true;


    /**
     * Parses the midi file whose data is stored in the tokens list and returns the data
     * as a list of notes that can be played on stepper motors
     * @return An arraylist of notes with start times and durations in hundredths of a second
     */
    public Pair<ArrayList<Note>, ArrayList<Percussion>> parseMidi(File file) throws InvalidMidiDataException, IOException {
        // Read the file and set up the structures that will accumulate data
        Pattern midiPattern = MidiFileManager.loadPatternFromMidi(file);
        ArrayList<Note> notes = new ArrayList<>();
        ArrayList<Percussion> percussion = new ArrayList<>();

        // TODO: Remove this after debugging
        // Filtering the list makes it easier to read in a debugger but has no effect on parsing
        List<Token> tokens = midiPattern.getTokens().stream()
                .filter(x->x.getType() != Token.TokenType.FUNCTION)
                .collect(Collectors.toList());
        int voiceIndex = 0;

        setupTempoFunction(tokens);

        // Reload the default values for these variables
        currentMeasure = 0.0;
        currentNoteStartTime = 0.0;

        for (Token t : tokens) {
            switch (t.getType()) {
                case NOTE -> noteDispatch(t, voiceIndex, notes, percussion);
                case TRACK_TIME_BOOKMARK -> {
                    currentMeasure = parseTrackTimeBookmark(t);
                    currentNoteStartTime = measureToTime(currentMeasure);
                }
                case VOICE -> {
                    currentMeasure = 0.0;
                    currentNoteStartTime = 0.0;
                    voiceIndex = parseVoice(t);
                }
            }
        }

        return new Pair<>(notes, percussion);
    }

    private void setupTempoFunction(List<Token> tokens) {
        tempoFunction = new ArrayList<>();
        double currentTrackTime = 0.0;

        for (Token t : tokens) {
            switch (t.getType()) {
                case NOTE -> { // Add the duration to the counter - this is the xPos in the piecewise function
                    currentTrackTime += calculateDuration(t.toString());
                }
                case TRACK_TIME_BOOKMARK -> currentTrackTime = parseTrackTimeBookmark(t);
                case TEMPO -> { // Add a new node to the tempo piecewise function
                    int tempo = parseTempo(t);
                    tempoFunction.add(new PiecewiseFuncEntry(currentTrackTime, tempo));
                }
                case VOICE -> currentTrackTime = 0.0;
            }
        }
    }


    /**
     * Takes a note/rest token and parses its data. If the token is a note, then the note is added
     * to the notes list. If the token is a rest, then the method only adds the rest's duration
     * to the note start time counter.
     * @param token The note/rest token to parse
     * @param notes A list of notes that the current token will be added to if it is a note
     */
    private void noteDispatch(Token token, int voiceIndex, ArrayList<Note> notes, ArrayList<Percussion> percussion) {
        String tokenString = token.toString();

        // Percussion is always enclosed in square brackets and is also classified as a note by JFugue
        if (tokenString.charAt(0) == '[') {
            parsePercussion(tokenString, percussion);
        } else {
            parseNote(tokenString, notes, voiceIndex);
        }
    }

    private void parsePercussion(String tokenString, ArrayList<Percussion> percussion) {
        // Extract the percussion command's data, then add it to the list
        Percussion.Type t;
        String typeName = null;
        try { // Get the identifier from inside the square brackets
            typeName = tokenString.substring(1, tokenString.indexOf(']'));
            t = Percussion.Type.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid percussion identifier: " + typeName);
            return;
        }
        int startTimeInHundredths = (int) Math.round(currentNoteStartTime);
        Percussion p = new Percussion(startTimeInHundredths, t);
        percussion.add(p);

        // Add the percussion's "duration" to the start time counter, so we know when the next note will start
        // Percussion duration cannot be heard, but is used behind the scenes for timing purposes
        double duration = calculateDuration(tokenString);
        int tempo = getTempoAtMeasure(currentMeasure);
        currentNoteStartTime += duration * JFUGUE_BEATS_PER_MEASURE * (1.0 / tempo) * MINUTES_TO_HUNDREDTHS_OF_SECOND;
        currentMeasure += duration;
    }

    private void parseNote(String tokenString, ArrayList<Note> notes, int voiceIndex) {
        // Extract the note's data
        org.jfugue.theory.Note note = new org.jfugue.theory.Note(tokenString);
        double duration = calculateDuration(tokenString);
        double frequency = org.jfugue.theory.Note.getFrequencyForNote(tokenString);
        int tempo = getTempoAtMeasure(currentMeasure);

        // Convert the note start time and duration from number of measures to hundredths of a second
        // We do this because the microcontroller checks for new notes 100 times per second
        //int startTimeInHundredths = (int) Math.ceil(currentNoteStartTime * JFUGUE_BEATS_PER_MEASURE * (1.0 / tempo) * MINUTES_TO_HUNDREDTHS_OF_SECOND);
        int startTimeInHundredths = (int) Math.round(currentNoteStartTime);
        int durationInHundredths = (int) Math.floor(duration * JFUGUE_BEATS_PER_MEASURE * (1.0 / tempo) * MINUTES_TO_HUNDREDTHS_OF_SECOND) - 1;

        // If the Note token isn't a rest (i.e. it's an actual note), add it to the notes list
        if (!note.isRest()) {
            // TODO: is there a better way to handle this? - maybe increase the note update rate from 1 hundredth of a second
            if (durationInHundredths == 0) {
                System.out.println("Fixing note with duration 0");
                durationInHundredths = 1;
            }

            Note newNote = new Note(startTimeInHundredths, frequency, durationInHundredths, voiceIndex);
            notes.add(newNote);
        }

        // Add the current note's duration to the start time counter, so we know when the next note will start
        currentNoteStartTime += duration * JFUGUE_BEATS_PER_MEASURE * (1.0 / tempo) * MINUTES_TO_HUNDREDTHS_OF_SECOND;
        currentMeasure += duration;
    }

    /**
     * Calculates the duration in number of measures for a given JFugue token represented as a string
     * JFugue provides a Note.getDuration() method, but it produces incorrect results for durations expressed in
     * scientific notation, as well as percussion tokens.
     * @param str
     * @return The duration of the note token passed in the parameter in number of measures
     * @throws NumberFormatException If the duration within the String parameter is not a valid double
     */
    private double calculateDuration(String str) throws NumberFormatException {
        //str = str.toLowerCase();

        // If the string token doesn't contain a '/', then duration is expressed using letters like w, h, and q
        if (!str.contains("/")) {
            // Token string will look like F5wa100d0, F5w, Rw2, or [ACOUSTIC_SNARE]SA100D80
            // If the token is for percussion, we will remove the percussion identifier by splitting on ']' and taking
            // split[1], then prepending the dummy note name A4. JFugue will throw an exception otherwise.
            String noteData = str;
            if (str.charAt(0) == '[') {
                String durationData = str.split("]")[1];
                noteData = "A4" + durationData;
            }

            org.jfugue.theory.Note n = new org.jfugue.theory.Note(noteData);
            return n.getDuration();
        }

        // At this point, the token string must look like F5/0.24, F5/1.2e-2, F5/0.24a100d0, or F5/1.2e-2a100d0
        // We will first split on '/' so that split1[1] contains duration + attack/decay information
        // Then we will split on 'a' or 'A' so that split2[0] contains only the duration string
        String[] split1 = str.split("/");
        String[] split2 = split1[1].split("[aA]");
        double duration = 0;
        try {
            duration = Double.parseDouble(split2[0]);
        } catch (NumberFormatException e) {
            System.err.println("Could not get length from token " + str);
        }
        return duration;
    }

    private double parseTrackTimeBookmark(Token token) throws NumberFormatException {
        String timeBookmark = token.toString().substring(1);
        return Double.parseDouble(timeBookmark);
    }

    private int parseTempo(Token token) throws NumberFormatException {
        String newTempo = token.toString().substring(1);
        return Integer.parseInt(newTempo);
    }

    private int parseTimeSignature(Token token) throws NumberFormatException {
        String beatsPerMeasure = token.toString().substring(5, 6);
        return Integer.parseInt(beatsPerMeasure);
    }

    private int parseVoice(Token token) {
        String voiceNum = token.toString().substring(1);
        return Integer.parseInt(voiceNum);
    }

    private int getTempoAtMeasure(double measure) {
        int tempo = 120;

        // Reverse iterate through the tempo list until we find an entry whose measure number
        // is less than the measure number passed in
        ListIterator<PiecewiseFuncEntry> iter = tempoFunction.listIterator(tempoFunction.size());
        while (iter.hasPrevious()) {
            PiecewiseFuncEntry entry = iter.previous();
            if (entry.xVal <= measure) {
                tempo = entry.yVal;
                break;
            }
        }

        return tempo;
    }

    /**
     * Converts a measure number to the corresponding time since the start of the song, in
     * hundredths of a second
     * @param measure A measure number to convert, with a beat within the measure being
     * represented as a fraction of a measure.
     * @return The time corresponding to the measure number passed in, in hundredths of
     * a second
     */
    private double measureToTime(double measure) {
        double time = 0;
        PiecewiseFuncEntry lastTempoEntry = tempoFunction.get(tempoFunction.size() - 1);

        // Calculate the time spent with the tempo at the input measure
        ListIterator<PiecewiseFuncEntry> iter = tempoFunction.listIterator(tempoFunction.size());
        while (iter.hasPrevious()) {
            lastTempoEntry = iter.previous();

            if (lastTempoEntry.xVal <= measure) {
                double duration = measure - lastTempoEntry.xVal;
                time += duration * JFUGUE_BEATS_PER_MEASURE * (1.0 / lastTempoEntry.yVal) * MINUTES_TO_HUNDREDTHS_OF_SECOND;
                break;
            }
        }

        // Calculate the time spent in the song before setting the current tempo
        while (iter.hasPrevious()) {
            PiecewiseFuncEntry currentTempoEntry = iter.previous();
            double duration = lastTempoEntry.xVal - currentTempoEntry.xVal;
            time += duration * JFUGUE_BEATS_PER_MEASURE * (1.0 / lastTempoEntry.yVal) * MINUTES_TO_HUNDREDTHS_OF_SECOND;
            lastTempoEntry = currentTempoEntry;
        }

        return time;
    }

    private double letterToDuration(char letter) {
        return switch (letter) {
            case 'w' -> 1.0;
            case 'h' -> 0.5;
            case 'q' -> 0.25;
            case 'i' -> 0.125;
            case 's' -> 0.0625;
            case 't' -> 0.03125;
            case 'x' -> 0.015625;
            case 'o' -> 0.0078125;
            default -> 0;
        };
    }

    public void setPreserveVoices(boolean value) {
        preserveVoices = value;
    }

    public boolean getPreserveVoices() {
        return preserveVoices;
    }

    private record PiecewiseFuncEntry(double xVal, int yVal) {}
}



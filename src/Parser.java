import org.jfugue.midi.MidiFileManager;
import org.jfugue.pattern.Pattern;
import org.jfugue.pattern.Token;
import org.jfugue.theory.Note;

import javax.sound.midi.InvalidMidiDataException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

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

    /**
     * Parses the midi file whose data is stored in the tokens list and returns the data
     * as a list of notes that can be played on stepper motors
     * @return An arraylist of notes with start times and durations in hundredths of a second
     */
    public ArrayList<SimpleNote> parseMidi(File file) throws InvalidMidiDataException, IOException {
        // This contains the midi file's raw data
        Pattern midiPattern = MidiFileManager.loadPatternFromMidi(file);

        ArrayList<SimpleNote> notes = new ArrayList<>();
        List<Token> tokens = midiPattern.getTokens();
        int voiceIndex = 0;

        setupTempoFunction(tokens);

        // Reload the default values for these variables
        currentMeasure = 0.0;
        currentNoteStartTime = 0.0;

        for (Token t : tokens) {
            switch (t.getType()) {
                case NOTE -> parseNote(t, voiceIndex, notes);
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

        return notes;
    }

    private void setupTempoFunction(List<Token> tokens) {
        tempoFunction = new ArrayList<>();
        double currentTrackTime = 0.0;

        for (Token t : tokens) {
            switch (t.getType()) {
                case NOTE -> { // Add the duration to the counter - this is the xPos in the piecewise function
                    String tokenString = t.toString();
                    if (tokenString.charAt(0) != '[') currentTrackTime += calculateDuration(tokenString);
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
    private void parseNote(Token token, int voiceIndex, ArrayList<SimpleNote> notes) {
        String tokenString = token.toString();

        // Percussion, which we want to ignore, is enclosed in square brackets
        if (tokenString.charAt(0) == '[') {
            return;
        }

        // Extract the note's data
        Note note = new Note(tokenString);
        double duration = calculateDuration(tokenString);
        double frequency = Note.getFrequencyForNote(tokenString);
        int tempo = getTempoAtMeasure(currentMeasure);

        // Convert the note start time and duration from number of measures to hundredths of a second
        // We do this because the microcontroller checks for new notes 100 times per second
        //int startTimeInHundredths = (int) Math.ceil(currentNoteStartTime * JFUGUE_BEATS_PER_MEASURE * (1.0 / tempo) * MINUTES_TO_HUNDREDTHS_OF_SECOND);
        int startTimeInHundredths = (int) Math.round(currentNoteStartTime);
        int durationInHundredths = (int) Math.floor(duration * JFUGUE_BEATS_PER_MEASURE * (1.0 / tempo) * MINUTES_TO_HUNDREDTHS_OF_SECOND) - 1;

        // If the Note token isn't a rest (i.e. it's an actual note), add it to the notes list
        if (!note.isRest()) {
            SimpleNote newNote = new SimpleNote(startTimeInHundredths, frequency, durationInHundredths, voiceIndex);
            notes.add(newNote);
        }

        // Add the current note's duration to the start time counter, so we know when the next note will start
        currentNoteStartTime += duration * JFUGUE_BEATS_PER_MEASURE * (1.0 / tempo) * MINUTES_TO_HUNDREDTHS_OF_SECOND;
        currentMeasure += duration;
    }

    /**
     * Calculates the duration in number of measures for a given JFugue token represented as a string
     * JFugue provides a Note.getDuration() method, but it produces incorrect results for durations expressed in
     * scientific notation.
     * @param str
     * @return The duration of the note token passed in the parameter in number of measures
     * @throws NumberFormatException If the duration within the String parameter is not a valid double
     */
    private double calculateDuration(String str) throws NumberFormatException {
        str = str.toLowerCase();

        // If the string token doesn't contain a '/', then duration is expressed using letters like w, h, q and we can let JFugue handle it
        if (!str.contains("/")) {
            return new Note(str).getDuration();
        }

        // Split the string token on the '/' so that splitNote[1] contains the duration data
        String[] splitNote = str.split("/");

        // If the duration data is not expressed in scientific notation, then we can let JFugue handle it
        if (!splitNote[1].contains("e")) {
            return new Note(str).getDuration();
        }

        // If the duration data also contains attack/decay information, split it on 'a' so that element 0 only contains the duration number
        // Otherwise use the duration data as is
        String durationString = (splitNote[1].contains("a")) ? splitNote[1].split("a")[0] : splitNote[1];
        return Double.parseDouble(durationString);
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

    private record PiecewiseFuncEntry(double xVal, int yVal) {}
}



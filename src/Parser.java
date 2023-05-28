import org.jfugue.midi.MidiFileManager;
import org.jfugue.pattern.Pattern;
import org.jfugue.pattern.Token;
import org.jfugue.theory.Note;

import javax.sound.midi.InvalidMidiDataException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses MIDI data to create a list of notes contained in the file
 *
 * @author Dylan Green
 * @version 2023.01.13
 */
public class Parser {

    private static final int JFUGUE_BEATS_PER_MEASURE = 4;
    private static final int MINUTES_TO_HUNDREDTHS_OF_SECOND = 6000;

    // The name of the midi file currently being read
    private String inputFileName = null;

    // This Pattern object contains the midi file's raw data
    private Pattern midiPattern;

    // A counter to store the start time of the current note being processed, in measures since the beginning of the
    // piece including a fraction of the current measure
    private double currentNoteStartTime = 0.0;

    // The music's current tempo, default is 120 beats per minute
    private int tempo = 120;

    public void readFile(String fileName) throws InvalidMidiDataException, IOException {
        midiPattern = MidiFileManager.loadPatternFromMidi(new File(fileName));
        //TODO: remove debug line
        System.out.println(midiPattern.toString());
        inputFileName = fileName;
    }

    /**
     * Parses the midi file whose data is stored in the tokens list and returns the data
     * as a list of notes that can be played on stepper motors
     * @return An arraylist of notes with start times and durations in hundredths of a second
     */
    public ArrayList<SimpleNote> parseMidi() {
        ArrayList<SimpleNote> notes = new ArrayList<>();
        List<Token> tokens = midiPattern.getTokens();

        // Reload the default values for these variables
        currentNoteStartTime = 0.0;
        tempo = 120;

        for (Token t : tokens) {
            switch (t.getType()) {
                case NOTE -> parseNote(t, notes);
                case TRACK_TIME_BOOKMARK -> currentNoteStartTime = parseTrackTimeBookmark(t);
                case TEMPO -> tempo = parseTempo(t);
                case VOICE -> currentNoteStartTime = 0.0;
            }
        }

        return notes;
    }


    /**
     * Takes a note/rest token and parses its data. If the token is a note, then the note is added
     * to the notes arraylist. If the token is a rest, then the method only adds the rest's duration
     * to the note start time counter.
     * @param token The note/rest token to parse
     * @param notes A list of notes that the current token will be added to if it is a note
     */
    private void parseNote(Token token, ArrayList<SimpleNote> notes) {
        String tokenString = token.toString();

        // Percussion, which we want to ignore, is enclosed in square brackets
        if (tokenString.charAt(0) == '[') {
            return;
        }

        // Extract the note's data
        Note note = new Note(tokenString);
        double duration = calculateDuration(tokenString);
        double frequency = Note.getFrequencyForNote(tokenString);

        // Convert the note start time and duration from number of measures to hundredths of a second
        // We do this because the microcontroller checks for new notes 100 times per second
        //int startTimeInHundredths = (int) Math.ceil(currentNoteStartTime * JFUGUE_BEATS_PER_MEASURE * (1.0 / tempo) * MINUTES_TO_HUNDREDTHS_OF_SECOND);
        int startTimeInHundredths = (int) Math.round(currentNoteStartTime);
        int durationInHundredths = (int) Math.floor(duration * JFUGUE_BEATS_PER_MEASURE * (1.0 / tempo) * MINUTES_TO_HUNDREDTHS_OF_SECOND) - 1;

        // If the Note token isn't a rest (i.e. it's an actual note), add it to the notes list
        if (!note.isRest()) {
            SimpleNote newNote = new SimpleNote(startTimeInHundredths, frequency, durationInHundredths);
            notes.add(newNote);
        }

        // Add the current note's duration to the start time counter, so we know when the next note will start
        currentNoteStartTime += duration * JFUGUE_BEATS_PER_MEASURE * (1.0 / tempo) * MINUTES_TO_HUNDREDTHS_OF_SECOND;
    }

    /**
     * Calculates the duration in number of measures for a given JFugue token represented as a string
     * JFugue provides a Note.getDuration() method, but it produces incorrect results for durations expressed in
     * scientific notation.
     * @param str
     * @return The duration of the note token passed in the parameter as a double in number of measures
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
        String bpm =  token.toString().substring(5, 6);
        return Integer.parseInt(bpm);
    }

    public String getInputFileName() {
        return inputFileName;
    }
}

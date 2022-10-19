import org.jfugue.midi.MidiFileManager;
import org.jfugue.pattern.Pattern;
import org.jfugue.pattern.Token;

import javax.sound.midi.InvalidMidiDataException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Parses MIDI data to create a list of notes contained in the file
 *
 * @author Dylan Green
 * @created 2022.07.03
 */
public class Parser {

    private static final boolean DEBUG = true;
    private static final char DATA_SEPARATOR = '/';

    // Used to convert note names to their corresponding frequency
    private Frequencies frequencies;

    // The MIDI file
    private String[] tokens;

    // This keeps track of the start time of the current note being processed
    // Needed because some notes have start times set implicitly through the
    // duration of previous notes and rests
    private double currentTrackTime;

    // These hold the tempo and BPM of the current MIDI file
    private static int tempo;
    private static int beatsPerMeasure;

    // A set of states used by the note parser FSM
    private enum States {PITCH, DURATION, DONE}

    /**
     * Constructs a new MIDI file parser
     *
     * @param filename The name or path of the MIDI file to read from
     */
    public Parser(String filename) {

        // Parse the MIDI data
        try {
            tokens = MidiFileManager.loadPatternFromMidi(new File(filename)).toString().split("\\s+");
        } catch (InvalidMidiDataException e) {
            System.err.println(filename + " could not be read from. The file may be corrupted.");
        } catch (IOException e) {
            System.err.println(filename + " could not be found.");
        }

        // Initialize the frequency converter
        frequencies = new Frequencies();
    }

    public ArrayList<Note> parseMusicData() {

        // This arraylist will store all the notes that get parsed from the music data
        ArrayList<Note> notes = new ArrayList<Note>();

        // Reset the track timer to 0, the start of the track
        currentTrackTime = 0.0;

        for (String token : tokens) {

            switch (getType(token)) {
                case NOTE -> parseNote(token, notes);
                case TRACK_TIME_BOOKMARK -> currentTrackTime = parseTrackTimeBookmark(token);
                case TEMPO -> parseTempo(token);
                case TIME_SIGNATURE -> parseTimeSignature(token);
                case VOICE -> currentTrackTime = 0.0;
            }
        }

        return notes;
    }

    public ArrayList<Note>[] parseMusicTracks() {
        //ArrayList<ArrayList<Note>> tracks = new ArrayList<ArrayList<Note>>(100);
        ArrayList<Note>[] tracks = new ArrayList[100];

        // Reset the track timer to 0, the start of the track
        currentTrackTime = 0.0;
        int currentTrackIndex = 0;

        for (String token : tokens) {

            switch (getType(token)) {
                case NOTE:
                    parseNote(token, tracks[currentTrackIndex]);
                    break;
                case TRACK_TIME_BOOKMARK:
                    currentTrackTime = parseTrackTimeBookmark(token);
                    break;
                case TEMPO:
                    tempo = 180;//parseTempo(token);
                    break;
                case TIME_SIGNATURE:
                    parseTimeSignature(token);
                    break;
                case VOICE:
                    currentTrackTime = 0.0;
                    currentTrackIndex = Integer.parseInt(token.substring(1));
                    //tracks.add(currentTrackIndex, new ArrayList<Note>());
                    tracks[currentTrackIndex] = new ArrayList<Note>();
                    break;
            }
        }

        return tracks;
    }

    private Token.TokenType getType(String token) {
        char firstChar = token.charAt(0);

        // Notes always start with their pitch letter. JFugue considers rests to be
        // of the NOTE type as well, so they are included for consistency.
        if ((firstChar >= 'A' && firstChar <= 'G') || firstChar == 'R') return Token.TokenType.NOTE;

        // Track time bookmarks always start with @
        else if (firstChar == '@') return Token.TokenType.TRACK_TIME_BOOKMARK;

        // Time signatures include "TIME" in the token while tempo tokens are always
        // a T followed by a number
        else if (firstChar == 'T') {
            char secondChar = token.charAt(1);
            if (secondChar == 'I') return Token.TokenType.TIME_SIGNATURE;
            else if (Character.isDigit(secondChar)) return Token.TokenType.TEMPO;
        }

        // Voice tokens always start with a V
        else if (firstChar == 'V') return Token.TokenType.VOICE;

        // If the token isn't one of these types, then it isn't needed by the program
        return Token.TokenType.UNKNOWN_TOKEN;
    }

    /**
     * Reads note data from the MIDI pattern and converts it into a form usable by the program,
     * then adds the note to the main note list. Note that the JFugue library does not differentiate
     * between actual notes and rests since both have the NOTE TokenType.
     *
     * @param token The token to parse, which can either be a rest or an actual note
     * @param notes An arraylist to add the newly parsed note to
     */
    private void parseNote(String token, ArrayList<Note> notes) {

        // Check if the token is a rest
        // If it is, then add the rest's duration to the track's current time
        if (token.charAt(0) == 'R') {
            currentTrackTime += parseRest(token);
        }

        // Otherwise, it's an actual note
        else {
            // The note parser will use an FSM since data length is
            // variable and unpredictable unlike the other token types
            States state = States.PITCH;

            // These StringBuilders will hold the data read from the note token
            StringBuilder pitchData = new StringBuilder();
            StringBuilder durationData = new StringBuilder();

            // Iterate over each character in the token
            for (int n = 0; n < token.length(); n++) {
                char current = token.charAt(n);

                // if the current char is the data separator, just skip over it
                if (current == DATA_SEPARATOR) continue;

                // The note parsing FSM
                switch (state) {
                    case PITCH:

                        //Ignore percussion tokens
                        //if (current)
                        // The pitch data always ends with the octave number
                        if (Character.isDigit(current)) state = States.DURATION;

                        pitchData.append(current);
                        break;
                    case DURATION:
                        // If we reach attack or decay data, we have finished reading duration data
                        if (current == 'a' || current == 'd' || current == 'A' || current == 'D') {
                            state = States.DONE;
                            continue;
                        } else {
                            durationData.append(current);
                        }
                        break;
                    case DONE:
                        // We have finished reading the current token
                        break;
                } // At this point, we now have the pitch and duration data as strings
            }

            double pitch = frequencies.get(pitchData.toString());
            double duration;

            // If the duration data is stored as a number, parse it directly
            if (Character.isDigit(durationData.charAt(0))) {
                duration = Double.parseDouble(durationData.toString());
            }

            // Otherwise, it's stored as a letter
            else {
                duration = NoteLengths.getLength(durationData.charAt(0));

                if (DEBUG) {
                    if (durationData.length() > 2) {
                        System.err.println("Duration data longer than expected");
                        System.err.println("Duration data: " + durationData.toString());
                    }
                    if (durationData.length() > 1 && !Character.isDigit(durationData.charAt(1)) && durationData.charAt(1) != '.') {
                        System.err.println("Expected numeric character at index 1");
                        System.err.println("Duration data: " + durationData.toString());
                    }
                }

                if (durationData.length() > 1) {
                    double multiplier = 1.0;
                    if (durationData.charAt(1) == '.') {
                        multiplier = 1.5;

                    }
                    else if (Character.isDigit(durationData.charAt(1))) {
                        multiplier = Integer.parseInt(durationData.substring(1));
                    }
                    duration *= multiplier;
                }


            }

            // Add the new note to the list and add its duration to the track timer
            Note newNote = new Note(currentTrackTime, pitch, duration);
            //TODO
            newNote.setName(pitchData.toString());
            notes.add(newNote);
            currentTrackTime += duration;
        }
    }

    private double parseRest(String token) {
        double duration = 0;

        // Find the index of the separator character /
        int separatorIndex = token.indexOf('/');

        // If the separator character is present, then the rest duration
        // is stored as a decimal number
        if (separatorIndex != -1) {
            duration = Double.parseDouble(token.substring(separatorIndex + 1));
        }

        // Otherwise, it's stored as a letter and needs to be converted into a number
        else {
            //duration = NoteLengths.getLength(token.charAt(1));

            // If the rest is dotted, multiply its duration by 1.5
            /*if (token.length() >= 3) {
                if (token.charAt(2) == '.') {
                    duration *= 1.5;
                }
                else if (Character.isDigit(token.charAt(2))) {
                    duration *= Integer.parseInt(token.substring(2));
                }
            }*/

            String[] splitToken = splitBeforeLetter(token);
            int i = 1;
            while (splitToken[i] != null) {
                double currentRestDuration = NoteLengths.getLength(splitToken[i].charAt(0));

                if (splitToken[i].length() > 1) {
                    if (Character.isDigit(splitToken[i].charAt(1))) {
                        currentRestDuration *= Integer.parseInt(splitToken[i].substring(1));
                    }
                    else if (splitToken[i].charAt(1) == '.') {
                        currentRestDuration *= 1.5;
                    }
                }

                duration += currentRestDuration;

                i++;
            }

            if (DEBUG) {
                if (token.length() == 3 && token.charAt(2) != '.' && !Character.isDigit(token.charAt(2))) {
                    System.err.println("Unexpected character at index 2");
                    System.err.println("Rest token: " + token);
                }
            }
        }

        return duration;
    }

    private double parseTrackTimeBookmark(String token) {
        return Double.parseDouble(token.substring(1));
    }

    private void parseTempo(String token) {
        tempo = Integer.parseInt(token.substring(1));
    }

    private void parseTimeSignature(String token) {
        beatsPerMeasure = Integer.parseInt(token.substring(5, 6));
    }

    public String[] splitBeforeLetter(String string) {
        String[] returnArray = new String[9]; // 8 possible types of note lengths + the note type
        StringBuilder builder = new StringBuilder();
        int arrayIndex = 0;

        for (int i = 0; i < string.length() - 1; i++) {
            builder.append(string.charAt(i));

            if (Character.isLetter(string.charAt(i + 1))) {
                returnArray[arrayIndex] = builder.toString();
                builder.setLength(0); //Reset the StringBuilder
                arrayIndex++;
            }
        }

        builder.append(string.charAt(string.length() - 1));
        returnArray[arrayIndex] = builder.toString();


        return returnArray;
    }

    public String[] getTokens() {
        return tokens;
    }

    public static int getTempo() {
        return tempo;
    }

    public static int getBeatsPerMeasure() {
        return beatsPerMeasure;
    }
}

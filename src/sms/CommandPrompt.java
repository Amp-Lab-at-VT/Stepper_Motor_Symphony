package sms;

import javax.sound.midi.InvalidMidiDataException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandPrompt {

    // The commands accepted by the program
    private enum CommandTypes{HELP, EXIT, READ, SET, WRITE, PARAMETERS, UNKNOWN}

    // Used for reading user input from the terminal
    private final BufferedReader reader;

    // Used for reading a MIDI file
    private final Parser parser;
    private Pair<ArrayList<Note>, ArrayList<Percussion>> midiData;

    private File inputFile;

    public CommandPrompt() {
        reader = new BufferedReader(new InputStreamReader(System.in));
        parser = new Parser();
        System.out.println("Stepper Motor Symphony by Dylan Green");
        System.out.println("Version 2023.01.13");
    }

    /**
     * Runs the program, presenting a command prompt to the user
     * @throws IOException If the terminal cannot be read from
     */
    public void run() throws IOException {
        var currentCommand = new Command(CommandTypes.UNKNOWN, null);
        do {
            System.out.print(">>> ");
            String input = reader.readLine();

            if (!input.isEmpty()) {
                currentCommand = parseInput(input);
                execute(currentCommand);
            }

        } while (currentCommand.type() != CommandTypes.EXIT);
    }

    /**
     * Turns user input into a command to be processed by the program
     * @param input An input string read from the terminal
     * @return The input, turned into a command able to be processed later
     * by the program
     */
    private Command parseInput(String input) {
        String[] inputAsArray = input.split("\\s+");
        CommandTypes type;

        // Get the command type
        switch (inputAsArray[0]) {
            case "help" -> type = CommandTypes.HELP;
            case "exit" -> type = CommandTypes.EXIT;
            case "read" -> type = CommandTypes.READ;
            case "set" -> type = CommandTypes.SET;
            case "write" -> type = CommandTypes.WRITE;
            case "parameters" -> type = CommandTypes.PARAMETERS;

            default -> type = CommandTypes.UNKNOWN;
        }

        // Extract the args from the input
        String[] args = Arrays.copyOfRange(inputAsArray, 1, inputAsArray.length);

        return new Command(type, args);
    }

    /**
     * Executes a command based on its type
     * @param command The command to execute
     */
    private void execute(Command command) {
        switch(command.type()) {
            case HELP -> help();
            case READ -> read(command);
            case SET -> set(command);
            case WRITE -> write();
            case PARAMETERS -> parameters();
            case UNKNOWN -> unknown();
        }
    }

    // TODO: Add help text
    /**
     * Prints a listing of all possible commands that the user can enter
     */
    private void help() {
        System.out.println("Help text");
    }

    /**
     * Tells the user that an unknown command was entered into the console
     */
    private void unknown() {
        System.err.println("Unknown command");
    }

    /**
     * Reads an input file so that it can be parsed by the program
     * @param command The command entered into the console by the user. The first
     *                argument will contain the name of the input file.
     */
    private void read(Command command) {
        // "read" only takes 1 argument, the filename
        if (command.args.length != 1) {
            System.err.println("Usage: read <filename>");
            return;
        }

        inputFile = new File(command.args[0]);

        // Try to parse the file entered by the user
        try {
            midiData = parser.parseMidi(inputFile);
            System.out.println("Successfully read file " + inputFile);
        } catch (InvalidMidiDataException e) {
            System.err.println("File " + inputFile + " contains invalid MIDI data.");
        } catch (IOException e) {
            System.err.println("File " + inputFile + " could not be found.");
        }
    }

    /**
     * Sets a variable to the value provided by the user. The variable names
     * accepted by the program are "preserveTracks",
     * @param command The command entered by the user. The set command will be
     *                formatted as "set <variable> <value>"
     */
    private void set(Command command) {
        String[] args = command.args();
        if (args.length != 2) {
            System.err.println("Usage: update <variable> <value>");
            return;
        }

        String varName = args[0].toLowerCase();
        String value = args[1].toLowerCase();

        switch (varName) {
            case "preservetracks", "p" -> {
                if (value.equals("true")) {
                    parser.setPreserveVoices(true);
                } else if (value.equals("false")) {
                    parser.setPreserveVoices(false);
                } else {
                    System.err.println("Unrecognized value");
                    System.err.println("Required: true/false");
                }
            }
            default -> System.err.println("Unrecognized variable name: " + varName);
        }
    }

    /**
     * Writes the currently read file's data into a .ino file that can be run by an ESP8266.
     */
    private void write() {
        if (inputFile == null) {
            System.err.println("An input file has not been read yet.");
            return;
        }

        String outputFileName = inputFile.getName();
        outputFileName = outputFileName.substring(0, outputFileName.lastIndexOf('.')) + ".ino";

        // Assign the notes to motors and write the output to a file
        List<Motor> motors = assignNotes();
        try {
            InoWriter writer = new InoWriter(motors, midiData.second(), outputFileName);
            writer.run();
        } catch (IOException e) {
            System.err.println("The Arduino sketch file could not be written to.");
            return;
        }

        System.out.println("Program requires " + motors.size() + " motors");
    }

    /**
     * Prints parameters that can be changed by the user to the console
     */
    private void parameters() {
        System.out.println("Input File Name: " + inputFile);
        System.out.println("preserveVoices: " + parser.getPreserveVoices());
    }

    /**
     * This method entails assigning notes to motors
     * @return List of Motor(s)
     */
    private List<Motor> assignNotes() {
        if (parser.getPreserveVoices()) {
            midiData.first().sort(Note.voiceOrder);
            return NoteAssigner.assign(midiData.first());
        } else {
            midiData.first().sort(Note.chronologicalOrder);
            return NoteAssigner.condensingAssign(midiData.first());
        }
    }

    /**
     * This record stores data for a command to be processed by the program
     * @param type The command type that was parsed
     * @param args The command arguments entered by the user
     */
    private record Command(CommandTypes type, String[] args) {}
}

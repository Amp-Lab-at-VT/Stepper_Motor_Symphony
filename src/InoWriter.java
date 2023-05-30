import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class InoWriter {
    
    private final ArrayList<Motor> motors;
    private FileWriter writer = null;

    private String outputPath = "";

    private static final String TAB = "    ";

    private static final String fileHeader = """
                    //Program written by Stepper Motor Symphony
                    #include "stepper.hpp"

                    #define COMMAND_SIZE 8
                    #define RECORD_SIZE 8
                    
                    void checkForNextNote();
                    void processCommands();
                    
                    """;

    private static final String structs = """
                    struct command {
                        uint32_t motorIndex;
                        uint32_t period;
                    };
                    
                    struct record {
                        uint32_t time;
                        uint32_t numCommands;
                    };
                    
                    """;

    private static final String checkForNextNote = """
                    void checkForNextNote() {
                        uint32_t newMillis = millis();
                        if (newMillis - oldMillis >= 10) {
                            oldMillis = newMillis;
                            counter++;
                            if (counter == currentRecord.time && recordIndex < numRecords) {
                                processCommands();
                                recordIndex++;
                                memcpy_P(&currentRecord, &records[recordIndex], RECORD_SIZE);
                            }
                        }
                    }
                    """;
    private static final String processCommands = """
                    void processCommands() {
                        uint8_t numCommands = currentRecord.numCommands;
                    
                        for (int n = 0; n < numCommands; n++) {
                            memcpy_P(&currentCommand, &commands[commandIndex], COMMAND_SIZE);
                            uint8_t motorIndex = currentCommand.motorIndex;
                            uint16_t period = currentCommand.period;
                            motors[motorIndex].setPeriod(period);
                            commandIndex++;
                        }
                    }
                    """;

    private static final String outputFolder = "arduino/";
    
    public InoWriter(ArrayList<Motor> motorList, String outputFileName) throws IOException {
        motors = motorList;

        // Get the file name without the type extension
        String[] fileNameArray = outputFileName.split("\\.");
        fileNameArray[fileNameArray.length - 1] = "";
        String outputFileNameWithoutType = outputFolder + String.join("", fileNameArray);

        // Create the output directory
        if(!Files.exists(new File("arduino/").toPath()) && !new File("arduino/").mkdir()) {
            throw new IOException("Could not create output directory");
        }

        // Arduino sketches require the .ino file to be in a parent directory with the same name
        // If the directory exists, just overwrite the contents;
        // Otherwise, try to create that directory and throw an IOException if it fails
        if (!Files.exists(new File(outputFileNameWithoutType).toPath()) && !new File(outputFileNameWithoutType).mkdir()) {
            throw new IOException("Could not create Arduino sketch directory");
        }

        // Copy the C++ files to the output directory
        // TODO: Add support for standalone program execution instead of just in an IDE
        File stepperCppSrc = new File("src/cpp/stepper.cpp");
        File stepperCppDest = new File(outputFileNameWithoutType + "/stepper.cpp");
        File stepperHppSrc = new File("src/cpp/stepper.hpp");
        File stepperHppDest = new File(outputFileNameWithoutType + "/stepper.hpp");
        Files.copy(stepperCppSrc.toPath(), stepperCppDest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(stepperHppSrc.toPath(), stepperHppDest.toPath(), StandardCopyOption.REPLACE_EXISTING);

        // Create the file that will hold the output program
        outputPath = outputFileNameWithoutType + "/" + outputFileName;
        writer = new FileWriter(outputPath);
    }
    
    public void run() throws IOException {
        writer.write(fileHeader);
        writer.write(structs);

        //Write the music command data
        StringBuilder[] commands = notesToCommands();
        boolean readFirstCommand = false;
        writer.write("const command commands[] PROGMEM = {");
        for (int n = 0; n < commands.length; n++) {
            if (commands[n] != null) {
                if (readFirstCommand) {
                    writer.write(", ");
                }
                writer.write(commands[n].toString());

                readFirstCommand = true;
            }
        }
        writer.write("};\n");

        //Write the record data
        readFirstCommand = false;
        int numRecords = 0;
        writer.write("const record records[] PROGMEM = {");
        for (int n = 0; n < commands.length; n++) {
            if (commands[n] != null) {
                if (readFirstCommand) {
                    writer.write(", ");
                }

                int numCommands = commands[n].toString().split("},").length;

                if (n == 0) {
                    writer.write("{1, " + numCommands + "}");

                } else {
                    writer.write("{" + n + ", " + numCommands + "}");
                }
                numRecords++;

                readFirstCommand = true;
            }
        }
        writer.write("};\n\n");

        //Global variables
        String variables = "Stepper motors[" + motors.size() + "];\n" +
                "command currentCommand;\n" +
                "record currentRecord;\n" +
                "uint32_t oldMillis = 0;\n" +
                "uint16_t commandIndex = 0;\n" +
                "uint16_t recordIndex = 0;\n" +
                "uint16_t numRecords = " + numRecords + ";\n" +
                "uint32_t counter = 0;\n\n";
        writer.write(variables);

        //Setup function
        writer.write("void setup() {\n");
        for (int controlPin = 0; controlPin < motors.size(); controlPin++) {
            writer.write(TAB + "motors[" + controlPin + "].setPin(D" + controlPin + ");\n");
        }
        writer.write("\n");
        writer.write(TAB + "memcpy_P(&currentRecord, &records[0], RECORD_SIZE);\n");
        writer.write("}\n\n");

        //Loop function
        writer.write("void loop() {\n");
        writer.write(TAB + "checkForNextNote();\n");
        for (int n = 0; n < motors.size(); n++) {
            writer.write(TAB + "motors[" + n + "].run(micros());\n");
        }
        writer.write("}\n\n");

        writer.write(checkForNextNote);
        writer.write(processCommands);

        writer.flush();
        writer.close();
        System.out.println("Successfully wrote to " + outputPath);
    }

    private StringBuilder[] notesToCommands() {

        //Find the end time of the last note
        int songEndTime = 0;
        for (Motor motor : motors) {
            SimpleNote lastSimpleNote = motor.getNotes().get(motor.getNotes().size() - 1);
            int currentEndTime = lastSimpleNote.startTime() + lastSimpleNote.duration();

            if (currentEndTime > songEndTime) {
                songEndTime = currentEndTime;
            }
        }

        StringBuilder[] commandArray = new StringBuilder[songEndTime + 1];

        for (Motor motor : motors) {
            for (SimpleNote currentSimpleNote : motor.getNotes()) {

                // Set up note parameters
                int startTime = currentSimpleNote.startTime();
                int endTime = startTime + currentSimpleNote.duration();
                int pitch = (int) Math.round(currentSimpleNote.pitch());
                int stepInterval;
                if (pitch == 0) {
                    stepInterval = 0;
                }
                else {
                    stepInterval = 1000000 / pitch;
                }

                String startCommand = "{" + motor.getIndex() + ", " + stepInterval + "}";
                String endCommand = "{" + motor.getIndex() + ", 0}";

                //Add the note start command
                if (commandArray[startTime] == null) {
                    commandArray[startTime] = new StringBuilder(startCommand);
                }
                else {
                    commandArray[startTime].append(", ").append(startCommand);
                }

                //Add the note end command
                if (commandArray[endTime] == null) {
                    commandArray[endTime] = new StringBuilder(endCommand);
                }
                else {
                    commandArray[endTime].append(", ").append(endCommand);
                }
            }
        }

        return commandArray;
    }

}

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class InoWriter {
    
    private final ArrayList<Motor> motors;
    private FileWriter writer = null;
    private final String inputFileName;

    private static final String TAB = "    ";
    
    public InoWriter(ArrayList<Motor> motorList, File outputFile, String inputFileName) {
        motors = motorList;
        this.inputFileName = inputFileName;

        try {
            writer = new FileWriter(outputFile);
        }
        catch (IOException e) {
            System.err.println(outputFile.getName() + " could not be opened. Application exiting...");
            e.printStackTrace();
        }


    }
    
    public void run() throws IOException {

        //Write the file header
        writer.write("//Program written by StepperMidi\n");
        writer.write("//Generated from " + inputFileName + "\n\n");
        writer.write("#include <AccelStepper.h>\n\n");

        //Write the structs used in the program
        String structs = "struct command {\n" +
                TAB + "uint8_t motorIndex;\n" +
                TAB + "uint16_t frequency;\n" +
                "};\n\n" +
                "struct record {\n" +
                TAB + "uint32_t time;\n" +
                TAB + "uint8_t numCommands;\n" +
                "};\n\n";
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
                //writer.write(TAB + TAB + "case " + n + ":\n");
                writer.write(commands[n].toString());
                //writer.write(TAB + TAB + TAB + "break;\n");

                readFirstCommand = true;
            }
        }
        writer.write("};\n");

        //Write the command record data
        readFirstCommand = false;
        int numRecords = 0;
        writer.write("const record records[] PROGMEM = {");
        for (int n = 0; n < commands.length; n++) {
            if (commands[n] != null) {
                if (readFirstCommand) {
                    writer.write(", ");
                }

                int numCommands = commands[n].toString().split("},").length;
                writer.write("{" + n + ", " + numCommands + "}");
                numRecords++;

                readFirstCommand = true;
            }
        }
        writer.write("};\n\n");

        //Global variables
        String variables = "AccelStepper motors[" + motors.size() + "];\n" +
                "command currentCommand;\n" +
                "record currentRecord;\n" +
                "uint32_t oldMillis = 0;\n" +
                "uint16_t commandIndex = 0;\n" +
                "uint16_t recordIndex = 0;\n" +
                "uint16_t numRecords = " + numRecords + ";\n" +
                "uint32_t counter = 0;\n\n";
        writer.write(variables);

        //Setup function
        int controlPin = 2;
        writer.write("void setup() {\n");
        for (int n = 0; n < motors.size(); n++) {
            writer.write(TAB + "motors[" + n + "] = AccelStepper(AccelStepper::DRIVER, " + controlPin + ", 12);\n");
            controlPin++;
        }
        writer.write("\n");
        for (int n = 0; n < motors.size(); n++) {
            writer.write(TAB + "motors[" + n + "].setEnablePin(13);\n");
            writer.write(TAB + "motors[" + n + "].setPinsInverted(false, false, true);\n");
            writer.write(TAB + "motors[" + n + "].setMinPulseWidth(20);\n");
            writer.write(TAB + "motors[" + n + "].enableOutputs();\n");
            writer.write(TAB + "motors[" + n + "].setMaxSpeed(8000);\n");
            writer.write(TAB + "motors[" + n + "].setSpeed(0);\n\n");
        }
        writer.write(TAB + "memcpy_P(&currentRecord, &records[0], 5);\n");
        writer.write("}\n\n");

        //Loop function
        writer.write("void loop() {\n");
        writer.write(TAB + "checkForNextNote();\n");
        for (int n = 0; n < motors.size(); n++) {
            writer.write(TAB + "motors[" + n + "].runSpeed();\n");
        }
        writer.write("}\n\n");

        //checkForNextNote function
        String checkForNextNote = "void checkForNextNote() {\n" +
                TAB + "uint32_t newMillis = millis();\n" +
                TAB + "if (newMillis - oldMillis >= 10) {\n" +
                TAB + TAB + "oldMillis = newMillis;\n" +
                TAB + TAB + "counter++;\n\n" +
                TAB + TAB + "if (counter == currentRecord.time) {\n" +
                TAB + TAB + TAB + "if (recordIndex < numRecords) {\n" +
                TAB + TAB + TAB + TAB +"handleCommands();\n" +
                TAB + TAB + TAB + TAB + "recordIndex++;\n" +
                TAB + TAB + TAB + TAB + "memcpy_P(&currentRecord, &records[recordIndex], 5);\n" +
                TAB + TAB + TAB + "}\n" +
                TAB + TAB + "}\n" +
                TAB + "}\n" +
                "}\n\n";
        writer.write(checkForNextNote);

        //handleCommands function
        String handleCommands = "void handleCommands() {\n" +
                TAB + "uint8_t numCommands = currentRecord.numCommands;\n\n" +
                TAB + "for (int n = 0; n < numCommands; n++) {\n" +
                TAB + TAB + "memcpy_P(&currentCommand, &commands[commandIndex], 3);\n" +
                TAB + TAB + "uint8_t motorIndex = currentCommand.motorIndex;\n" +
                TAB + TAB + "uint16_t frequency = currentCommand.frequency;\n" +
                TAB + TAB + "motors[motorIndex].setSpeed(frequency);\n\n" +
                TAB + TAB + "commandIndex++;\n" +
                TAB + "}\n" +
                "}\n";
        writer.write(handleCommands);

        writer.flush();
        writer.close();
    }

    private StringBuilder[] notesToCommands() {

        //Find the end time of the last note
        int songEndTime = 0;
        for (Motor motor : motors) {
            Note lastNote = motor.getNotes().get(motor.getNotes().size() - 1);
            int currentEndTime = lastNote.getStartTime() + lastNote.getDuration();

            if (currentEndTime > songEndTime) {
                songEndTime = currentEndTime;
            }
        }

        StringBuilder[] commandArray = new StringBuilder[songEndTime + 1];

        int count = 0;
        for (Motor motor : motors) {
            for (Note current : motor.getNotes()) {
                int startTime = current.getStartTime();
                int endTime = startTime + current.getDuration();

                //String startCommand = TAB + TAB + TAB + motor.getName() + ".setSpeed(" + current.getPitch() + ");\n";
                int pitch = (int) Math.round(current.getPitch());
                int stepInterval;
                if (pitch == 0) {
                    stepInterval = 0;
                }
                else {
                    stepInterval = 1000000 / pitch;
                }
                String startCommand = "{" + motor.getIndex() + ", " + stepInterval + "}";
                //String endCommand = TAB + TAB + TAB + motor.getName() + ".setSpeed(0);\n";
                String endCommand = "{" + motor.getIndex() + ", 0}";

                if (count > 1600) return commandArray;
                //if (motor.getIndex() > 3) continue;
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
                count++;
            }
        }

        return commandArray;
    }

}

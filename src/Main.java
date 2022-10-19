
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Starts the program
 * 
 * @author Dylan Green
 * @version 2021.07.21
 */
public class Main {
  
    public static void main(String[] args) throws IOException {

        Parser p = new Parser(args[0]);
        ArrayList<Note> notes = p.parseMusicData();
        Collections.sort(notes);
        ArrayList<Motor> motors = NoteAssigner.assign(notes);

        // Form the output file name
        String outputFileName = args[0].split(".")[0];
        InoWriter writer = new InoWriter(motors, new File(outputFileName), "args[0]");
        writer.run();

    }
}
 


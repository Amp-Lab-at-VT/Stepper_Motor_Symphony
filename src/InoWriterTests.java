import org.jfugue.theory.Note;
import org.junit.Test;

import javax.sound.midi.InvalidMidiDataException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InoWriterTests {

    @Test
    public void testFileWrite_midiInWorkingDir() throws IOException, InvalidMidiDataException {
        // Copy a test file into the working directory
        File testFileSrc = new File("testfiles/testfile.mid");
        File testFileDst = new File("testfile.mid");
        Files.copy(testFileSrc.toPath(), testFileDst.toPath(), StandardCopyOption.REPLACE_EXISTING);
        assertTrue(Files.exists(testFileDst.toPath()));

        // Turn the midi into an Arduino sketch
        Parser p = new Parser();
        p.readFile("testfile.mid");
        ArrayList<SimpleNote> notes = p.parseMidi();
        Collections.sort(notes);
        ArrayList<Motor> motors = NoteAssigner.assign(notes);
        InoWriter writer = new InoWriter(motors, "testfile.ino");
        writer.run();

        assertTrue(Files.exists(new File("arduino/").toPath()));
        assertTrue(Files.exists(new File("arduino/testfile/").toPath()));
        assertTrue(Files.exists(new File("arduino/testfile/testfile.ino").toPath()));
        assertTrue(Files.exists(new File("arduino/testfile/stepper.hpp").toPath()));
        assertTrue(Files.exists(new File("arduino/testfile/stepper.cpp").toPath()));

        // Clean up
        assertTrue(Files.deleteIfExists(testFileDst.toPath()));
        assertTrue(Files.deleteIfExists(new File("arduino/testfile/testfile.ino").toPath()));
        assertTrue(Files.deleteIfExists(new File("arduino/testfile/stepper.hpp").toPath()));
        assertTrue(Files.deleteIfExists(new File("arduino/testfile/stepper.cpp").toPath()));
        assertTrue(Files.deleteIfExists(new File("arduino/testfile/").toPath()));
    }

    @Test
    public void testFileWrite_midiNotInWorkingDir() throws IOException, InvalidMidiDataException {
        // Turn the midi into an Arduino sketch
        Parser p = new Parser();
        p.readFile("testfiles/testfile.mid");
        ArrayList<SimpleNote> notes = p.parseMidi();
        Collections.sort(notes);
        ArrayList<Motor> motors = NoteAssigner.assign(notes);
        InoWriter writer = new InoWriter(motors, "testfile.ino");
        writer.run();

        assertTrue(Files.exists(new File("arduino/").toPath()));
        assertTrue(Files.exists(new File("arduino/testfile/").toPath()));
        assertTrue(Files.exists(new File("arduino/testfile/testfile.ino").toPath()));
        assertTrue(Files.exists(new File("arduino/testfile/stepper.hpp").toPath()));
        assertTrue(Files.exists(new File("arduino/testfile/stepper.cpp").toPath()));

        // Clean up
        assertTrue(Files.deleteIfExists(new File("arduino/testfile/testfile.ino").toPath()));
        assertTrue(Files.deleteIfExists(new File("arduino/testfile/stepper.hpp").toPath()));
        assertTrue(Files.deleteIfExists(new File("arduino/testfile/stepper.cpp").toPath()));
        assertTrue(Files.deleteIfExists(new File("arduino/testfile/").toPath()));
    }
}

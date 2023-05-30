import org.junit.Test;

import javax.sound.midi.InvalidMidiDataException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class ParserTests {

    @Test
    public void testNotesPlayedAfterTempoChange_1Part() throws InvalidMidiDataException, IOException {
        Parser p = new Parser();

        p.readFile(new File("testfiles/tempochange_1part.mid"));

        // File has 12 notes, starting at 120bpm, switching to 60bpm after 4 notes
        // and then switching back to 120bpm after another 4 notes
        ArrayList<SimpleNote> notes = p.parseMidi();
        assertEquals(12, notes.size());

        assertEquals(0, notes.get(0).startTime());
        assertEquals(46, notes.get(0).duration());
        assertEquals(50, notes.get(1).startTime());
        assertEquals(46, notes.get(1).duration());
        assertEquals(100, notes.get(2).startTime());
        assertEquals(46, notes.get(2).duration());
        assertEquals(150, notes.get(3).startTime());
        assertEquals(46, notes.get(3).duration());
        assertEquals(203, notes.get(4).startTime());
        assertEquals(93, notes.get(4).duration());
        assertEquals(303, notes.get(5).startTime());
        assertEquals(93, notes.get(5).duration());
        assertEquals(403, notes.get(6).startTime());
        assertEquals(93, notes.get(6).duration());
        assertEquals(503, notes.get(7).startTime());
        assertEquals(93, notes.get(7).duration());
        assertEquals(600, notes.get(8).startTime());
        assertEquals(46, notes.get(8).duration());
        assertEquals(650, notes.get(9).startTime());
        assertEquals(46, notes.get(9).duration());
        assertEquals(700, notes.get(10).startTime());
        assertEquals(46, notes.get(10).duration());
        assertEquals(750, notes.get(11).startTime());
        assertEquals(46, notes.get(11).duration());
    }

    @Test
    public void testNotesPlayedAfterTempoChange_2Parts() throws InvalidMidiDataException, IOException {
        Parser p = new Parser();

        p.readFile(new File("testfiles/tempochange_2parts.mid"));

        // File has 12 notes, starting at 120bpm, switching to 60bpm after 4 notes
        // and then switching back to 120bpm after another 4 notes
        ArrayList<SimpleNote> notes = p.parseMidi();
        assertEquals(24, notes.size());

        // Part 1 - Same as in the single part tempo change test
        assertEquals(0, notes.get(0).startTime());
        assertEquals(48, notes.get(0).duration());
        assertEquals(50, notes.get(1).startTime());
        assertEquals(48, notes.get(1).duration());
        assertEquals(100, notes.get(2).startTime());
        assertEquals(48, notes.get(2).duration());
        assertEquals(150, notes.get(3).startTime());
        assertEquals(48, notes.get(3).duration());
        assertEquals(200, notes.get(4).startTime());
        assertEquals(98, notes.get(4).duration());
        assertEquals(300, notes.get(5).startTime());
        assertEquals(98, notes.get(5).duration());
        assertEquals(400, notes.get(6).startTime());
        assertEquals(98, notes.get(6).duration());
        assertEquals(500, notes.get(7).startTime());
        assertEquals(98, notes.get(7).duration());
        assertEquals(600, notes.get(8).startTime());
        assertEquals(48, notes.get(8).duration());
        assertEquals(650, notes.get(9).startTime());
        assertEquals(48, notes.get(9).duration());
        assertEquals(700, notes.get(10).startTime());
        assertEquals(48, notes.get(10).duration());
        assertEquals(750, notes.get(11).startTime());
        assertEquals(48, notes.get(11).duration());

        // Part 2 - should have the same start time and durations as the first part
        assertEquals(0, notes.get(12).startTime());
        assertEquals(48, notes.get(12).duration());
        assertEquals(50, notes.get(13).startTime());
        assertEquals(48, notes.get(13).duration());
        assertEquals(100, notes.get(14).startTime());
        assertEquals(48, notes.get(14).duration());
        assertEquals(150, notes.get(15).startTime());
        assertEquals(48, notes.get(15).duration());
        assertEquals(200, notes.get(16).startTime());
        assertEquals(98, notes.get(16).duration());
        assertEquals(300, notes.get(17).startTime());
        assertEquals(98, notes.get(17).duration());
        assertEquals(400, notes.get(18).startTime());
        assertEquals(98, notes.get(18).duration());
        assertEquals(500, notes.get(19).startTime());
        assertEquals(98, notes.get(19).duration());
        assertEquals(600, notes.get(20).startTime());
        assertEquals(48, notes.get(20).duration());
        assertEquals(650, notes.get(21).startTime());
        assertEquals(48, notes.get(21).duration());
        assertEquals(700, notes.get(22).startTime());
        assertEquals(48, notes.get(22).duration());
        assertEquals(750, notes.get(23).startTime());
        assertEquals(48, notes.get(23).duration());
    }

    @Test
    public void testTempoChange_2Parts_TempoChangeOnPart2() throws InvalidMidiDataException, IOException {
        Parser p = new Parser();

        p.readFile(new File("testfiles/tempochange_2parts_decimalTempo.mid"));

        // File has 12 notes, starting at 120bpm, switching to 60bpm after 4 notes
        // and then switching back to 120bpm after another 4 notes
        ArrayList<SimpleNote> notes = p.parseMidi();
        assertEquals(24, notes.size());

        // Part 1 - Same as in the single part tempo change test
        assertEquals(0, notes.get(0).startTime());
        assertEquals(48, notes.get(0).duration());
        assertEquals(50, notes.get(1).startTime());
        assertEquals(48, notes.get(1).duration());
        assertEquals(100, notes.get(2).startTime());
        assertEquals(48, notes.get(2).duration());
        assertEquals(150, notes.get(3).startTime());
        assertEquals(48, notes.get(3).duration());
        assertEquals(200, notes.get(4).startTime());
        assertEquals(98, notes.get(4).duration());
        assertEquals(300, notes.get(5).startTime());
        assertEquals(98, notes.get(5).duration());
        assertEquals(400, notes.get(6).startTime());
        assertEquals(98, notes.get(6).duration());
        assertEquals(500, notes.get(7).startTime());
        assertEquals(98, notes.get(7).duration());
        assertEquals(600, notes.get(8).startTime());
        assertEquals(48, notes.get(8).duration());
        assertEquals(650, notes.get(9).startTime());
        assertEquals(48, notes.get(9).duration());
        assertEquals(700, notes.get(10).startTime());
        assertEquals(48, notes.get(10).duration());
        assertEquals(750, notes.get(11).startTime());
        assertEquals(48, notes.get(11).duration());

        // Part 2 - should have the same start time and durations as the first part
        assertEquals(0, notes.get(12).startTime());
        assertEquals(48, notes.get(12).duration());
        assertEquals(50, notes.get(13).startTime());
        assertEquals(48, notes.get(13).duration());
        assertEquals(100, notes.get(14).startTime());
        assertEquals(48, notes.get(14).duration());
        assertEquals(150, notes.get(15).startTime());
        assertEquals(48, notes.get(15).duration());
        assertEquals(200, notes.get(16).startTime());
        assertEquals(98, notes.get(16).duration());
        assertEquals(300, notes.get(17).startTime());
        assertEquals(98, notes.get(17).duration());
        assertEquals(400, notes.get(18).startTime());
        assertEquals(98, notes.get(18).duration());
        assertEquals(500, notes.get(19).startTime());
        assertEquals(98, notes.get(19).duration());
        assertEquals(600, notes.get(20).startTime());
        assertEquals(48, notes.get(20).duration());
        assertEquals(650, notes.get(21).startTime());
        assertEquals(48, notes.get(21).duration());
        assertEquals(700, notes.get(22).startTime());
        assertEquals(48, notes.get(22).duration());
        assertEquals(750, notes.get(23).startTime());
        assertEquals(48, notes.get(23).duration());
    }

    @Test
    public void testMultipleNotesIn1Part() throws InvalidMidiDataException, IOException {
        Parser p = new Parser();

        p.readFile(new File("testfiles/multipleNotesAtOnce_1part.mid"));

        // File has 12 notes, starting at 120bpm, switching to 60bpm after 4 notes
        // and then switching back to 120bpm after another 4 notes
        ArrayList<SimpleNote> notes = p.parseMidi();
        assertEquals(8, notes.size());

        assertEquals(0, notes.get(0).startTime());
        assertEquals(48, notes.get(0).duration());
        assertEquals(0, notes.get(1).startTime());
        assertEquals(48, notes.get(1).duration());
        assertEquals(50, notes.get(2).startTime());
        assertEquals(48, notes.get(2).duration());
        assertEquals(50, notes.get(3).startTime());
        assertEquals(48, notes.get(3).duration());
        assertEquals(100, notes.get(4).startTime());
        assertEquals(48, notes.get(4).duration());
        assertEquals(100, notes.get(5).startTime());
        assertEquals(48, notes.get(5).duration());
        assertEquals(150, notes.get(6).startTime());
        assertEquals(48, notes.get(6).duration());
        assertEquals(150, notes.get(7).startTime());
        assertEquals(48, notes.get(7).duration());
    }

}

package sms;

import org.junit.Test;

import javax.sound.midi.InvalidMidiDataException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static sms.Percussion.Type;

public class ParserTests {

    @Test
    public void testNotesPlayedAfterTempoChange_1Part() throws InvalidMidiDataException, IOException {
        Parser p = new Parser();

        // File has 12 notes, starting at 120bpm, switching to 60bpm after 4 notes
        // and then switching back to 120bpm after another 4 notes
        ArrayList<Note> notes = p.parseMidi(new File("testfiles/tempochange_1part.mid")).first();
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

        // File has 12 notes, starting at 120bpm, switching to 60bpm after 4 notes
        // and then switching back to 120bpm after another 4 notes
        ArrayList<Note> notes = p.parseMidi(new File("testfiles/tempochange_2parts.mid")).first();
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

        // File has 12 notes, starting at 120bpm, switching to 60bpm after 4 notes
        // and then switching back to 120bpm after another 4 notes
        ArrayList<Note> notes = p.parseMidi(new File("testfiles/tempochange_2parts_decimalTempo.mid")).first();
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

        // File has 12 notes, starting at 120bpm, switching to 60bpm after 4 notes
        // and then switching back to 120bpm after another 4 notes
        ArrayList<Note> notes = p.parseMidi(new File("testfiles/multipleNotesAtOnce_1part.mid")).first();
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

    @Test
    public void testSimplePercussion() throws InvalidMidiDataException, IOException {
        Parser p = new Parser();
        var percussion = p.parseMidi(new File("testfiles/simplePercussion.mid")).second();
        assertEquals(4, percussion.size());

        assertEquals(0, percussion.get(0).startTime());
        assertEquals(Type.ACOUSTIC_BASS_DRUM, percussion.get(0).type());
        assertEquals(50, percussion.get(1).startTime());
        assertEquals(Type.ACOUSTIC_BASS_DRUM, percussion.get(1).type());
        assertEquals(100, percussion.get(2).startTime());
        assertEquals(Type.ACOUSTIC_BASS_DRUM, percussion.get(2).type());
        assertEquals(150, percussion.get(3).startTime());
        assertEquals(Type.ACOUSTIC_BASS_DRUM, percussion.get(3).type());
    }

    @Test
    public void testPercussion_3parts() throws InvalidMidiDataException, IOException {
        Parser p = new Parser();
        var percussion = p.parseMidi(new File("testfiles/percussion_3parts.mid")).second();
        assertEquals(10, percussion.size());

        var bass = percussion.stream()
                .filter(command -> command.type() == Type.ACOUSTIC_BASS_DRUM)
                .toList();
        var snare = percussion.stream()
                .filter(command -> command.type() == Type.ACOUSTIC_SNARE)
                .toList();
        var hihat = percussion.stream()
                .filter(command -> command.type() == Type.CLOSED_HI_HAT)
                .toList();

        assertEquals(4, bass.size());
        assertEquals(0, bass.get(0).startTime());
        assertEquals(50, bass.get(1).startTime());
        assertEquals(100, bass.get(2).startTime());
        assertEquals(150, bass.get(3).startTime());

        assertEquals(2, snare.size());
        assertEquals(50, snare.get(0).startTime());
        assertEquals(150, snare.get(1).startTime());

        assertEquals(4, hihat.size());
        assertEquals(25, hihat.get(0).startTime());
        assertEquals(75, hihat.get(1).startTime());
        assertEquals(125, hihat.get(2).startTime());
        assertEquals(175, hihat.get(3).startTime());
    }

    @Test
    public void testSimpleCombinedNotesAndPercussion() throws InvalidMidiDataException, IOException {
        Parser p = new Parser();
        var parsed = p.parseMidi(new File("testfiles/percussionAndNotes.mid"));
        var notes = parsed.first();
        var percussion = parsed.second();
        assertEquals(8, notes.size());
        assertEquals(6, percussion.size());

        assertEquals(0, notes.get(0).startTime());
        assertEquals(25, notes.get(1).startTime());
        assertEquals(50, notes.get(2).startTime());
        assertEquals(75, notes.get(3).startTime());
        assertEquals(100, notes.get(4).startTime());
        assertEquals(125, notes.get(5).startTime());
        assertEquals(150, notes.get(6).startTime());
        assertEquals(175, notes.get(7).startTime());

        for (Note n : notes) {
            assertEquals(23, n.duration());
        }

        var bass = percussion.stream()
                .filter(command -> command.type() == Type.ACOUSTIC_BASS_DRUM)
                .toList();
        var snare = percussion.stream()
                .filter(command -> command.type() == Type.ELECTRIC_SNARE)
                .toList();

        assertEquals(4, bass.size());
        assertEquals(2, snare.size());

        assertEquals(4, bass.size());
        assertEquals(0, bass.get(0).startTime());
        assertEquals(50, bass.get(1).startTime());
        assertEquals(100, bass.get(2).startTime());
        assertEquals(150, bass.get(3).startTime());

        assertEquals(2, snare.size());
        assertEquals(50, snare.get(0).startTime());
        assertEquals(150, snare.get(1).startTime());
    }
}

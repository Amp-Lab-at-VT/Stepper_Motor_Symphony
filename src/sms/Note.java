package sms;

import java.util.Comparator;

/**
 * Stores data of a single note within the MIDI file
 * @param startTime The time this note should start playing at, in hundredths of a second
 *                  since the start of the song
 * @param pitch The frequency of the note, in Hertz
 * @param duration How long the note should play for, in hundredths of a second
 * @param voiceIndex The index of the voice this note comes from in the MIDI file
 */
public record Note(int startTime, double pitch, int duration, int voiceIndex) implements MusicCommand {

    /**
     * Returns a String representation of this note's data
     */
    public String toString() {
        String startTimeSeconds = (startTime / 100) + "." + String.format("%02d", (startTime % 100)) + "s";
        int endTime = startTime + duration;
        String endTimeSeconds = (endTime / 100) + "." + String.format("%02d", (endTime % 100)) + "s";
        return "Note " + pitch + " at " + startTimeSeconds + " until " + endTimeSeconds;
    }

    /**
     * Used to sort notes in chronological order. If two notes start playing at the same time,
     * then they are sorted by their pitch
     */
    public static final Comparator<Note> chronologicalOrder = (n1, n2) -> {
        int timeDifference = n1.startTime - n2.startTime;
        double pitchDifference = n1.pitch - n2.pitch;

        if (timeDifference < 0) return -1;
        if (timeDifference > 0) return 1;
        if (pitchDifference < 0) return -1;
        if (pitchDifference > 0) return 1;
        return 0;
    };

    /**
     * Used to sort notes in chronological order but in a way that preserves the original
     * ordering of the voice indices. This means that all notes for voice 0 will be grouped
     * together and sorted in chronological order, followed by all the notes for voice 1, etc.
     */
    public static final Comparator<Note> voiceOrder = (n1, n2) -> {
        int voiceDifference = n1.voiceIndex - n2.voiceIndex;

        if (voiceDifference < 0) return -1;
        if (voiceDifference > 0) return 1;
        return chronologicalOrder.compare(n1, n2);
    };

}

/**
 * Stores data of a single note within the MIDI file
 *
 * @version 2021.07.21
 */
public record SimpleNote(int startTime, double pitch, int duration) implements Comparable<SimpleNote> {

    /**
     * Returns a String representation of this note's data
     */
    public String toString() {
        String startTimeSeconds = (startTime / 100) + "." + String.format("%02d", (startTime % 100)) + "s";
        int endTime = startTime + duration;
        String endTimeSeconds = (endTime / 100) + "." + String.format("%02d", (endTime % 100)) + "s";
        return "Note " + pitch + " at " + startTimeSeconds + " until " + endTimeSeconds;
    }

    public int compareTo(SimpleNote other) {
        double timeDifference = this.startTime - other.startTime;
        double pitchDifference = this.pitch - other.pitch;

        if (timeDifference < 0) return -1;
        if (timeDifference > 0) return 1;
        if (pitchDifference < 0) return -1;
        if (pitchDifference > 0) return 1;
        return 0;
    }

}

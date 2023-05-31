import java.util.Comparator;

/**
 * Stores data of a single note within the MIDI file
 *
 * @version 2021.07.21
 */
public record SimpleNote(int startTime, double pitch, int duration, int voiceIndex) implements Comparable<SimpleNote> {

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

    public static final Comparator<SimpleNote> chronologicalOrder = new Comparator<SimpleNote>() {
        @Override
        public int compare(SimpleNote n1, SimpleNote n2) {
            double timeDifference = n1.startTime - n2.startTime;
            double pitchDifference = n1.pitch - n2.pitch;

            if (timeDifference < 0) return -1;
            if (timeDifference > 0) return 1;
            if (pitchDifference < 0) return -1;
            if (pitchDifference > 0) return 1;
            return 0;        }
    };

    public static final Comparator<SimpleNote> voiceOrder = new Comparator<SimpleNote>() {
        @Override
        public int compare(SimpleNote n1, SimpleNote n2) {
            int voiceDifference = n1.voiceIndex - n2.voiceIndex;
            double timeDifference = n1.startTime - n2.startTime;
            double pitchDifference = n1.pitch - n2.pitch;

            if (voiceDifference < 0) return -1;
            if (voiceDifference > 0) return 1;
            if (timeDifference < 0) return -1;
            if (timeDifference > 0) return 1;
            if (pitchDifference < 0) return -1;
            if (pitchDifference > 0) return 1;
            return 0;        }
    };

}

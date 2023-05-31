import java.util.ArrayList;
import java.util.List;

/**
 * Assigns notes to stepper motors
 */
public class NoteAssigner {

    public static ArrayList<Motor> assign(List<SimpleNote> notes) {
        ArrayList<Motor> motors = new ArrayList<>();
        if (notes.isEmpty()) return motors;

        // For each voice in the original song, we will extract all the notes that correspond
        // to that voice, then assign that subset of notes to motors such that each voice's notes
        // are played on unique motors

        int firstVoiceNoteIndex = 0;
        int currentVoice = notes.get(0).voiceIndex();
        for (int i = 0; i < notes.size(); i++) {

            // Check if we reached the notes for a new voice
            if (notes.get(i).voiceIndex() != currentVoice) {
                // Get the sublist for the current voice and assign its notes to motors
                var sublist = notes.subList(firstVoiceNoteIndex, i);
                var voiceMotors = condensingAssign(sublist);
                motors.addAll(voiceMotors);

                firstVoiceNoteIndex = i;
                currentVoice = notes.get(i).voiceIndex();
            }
        }

        // Check if there are still notes left to be added
        if (firstVoiceNoteIndex < notes.size() - 1) {
            var sublist = notes.subList(firstVoiceNoteIndex, notes.size());
            var voiceMotors = condensingAssign(sublist);
            motors.addAll(voiceMotors);
        }

        // Set the index for each motor
        for (int i = 0; i < motors.size(); i++) {
            motors.get(i).setIndex(i);
        }

        return motors;
    }

    /**
     * Assigns each note from the list to a motor using a condensing algorithm. Each note is
     * assigned to the first motor that isn't playing a note at that note's start time. This
     * means the song will be played on as few motors as possible, although this may not
     * sound as good sometimes.
     *
     * @param notes The list of notes to be assigned
     * @return A list of motors, each containing their assigned notes
     */
    public static List<Motor> condensingAssign(List<SimpleNote> notes) {
        ArrayList<Motor> motors = new ArrayList<>();

        for (var note : notes) {
            boolean noteAdded = false;
            int motorIndex = 0;

            // Go through each motor and find the first one that isn't playing anything at
            // the current note's start time. If no motor is available, then add a new one.
            while (!noteAdded) {
                if (motorIndex == motors.size()) {
                    motors.add(new Motor(motors.size()));
                }

                if (!motors.get(motorIndex).isInUse(note.startTime())) {
                    motors.get(motorIndex).addNote(note);
                    noteAdded = true;
                }
                else {
                    motorIndex++;
                }
            }
        }

        return motors;
    }
}

import java.util.ArrayList;

/**
 * Assigns notes to stepper motors
 */
public class NoteAssigner {

    /**
     * Assigns each note from the list to a motor using a condensing algorithm. Each note is
     * assigned to the first motor that isn't playing a note at that note's start time. This
     * means the song will be played on as few motors as possible, although this may not
     * sound as good sometimes.
     *
     * @param notes The list of notes to be assigned
     * @return A list of motors, each containing their assigned notes
     */
    public static ArrayList<Motor> assign(ArrayList<SimpleNote> notes) {
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

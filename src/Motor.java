import java.util.ArrayList;

public class Motor {
    
    private int index;
    private final ArrayList<IntPair> inUseTimes;
    private final ArrayList<SimpleNote> notes;
    
    public Motor() {
        inUseTimes = new ArrayList<>();
        notes = new ArrayList<>();
    }

    public Motor(int motorIndex) {
        index = motorIndex;
        inUseTimes = new ArrayList<>();
        notes = new ArrayList<>();
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int newIndex) {
        index = newIndex;
    }

    public ArrayList<SimpleNote> getNotes() {
        return notes;
    }

    /**
     * Adds a new note to be played on this motor.
     * @param newNote The note to be added, which must not conflict with any notes already assigned to this motor.
     */
    public void addNote(SimpleNote newNote) {
        notes.add(newNote);
        inUseTimes.add(new IntPair(newNote.startTime(), newNote.startTime() + newNote.duration()));
    }
    
    public boolean isInUse(int time) {
        // If no notes have been assigned to the motor yet, just return false
        if (inUseTimes.isEmpty()) return false;

        // Get the last note assigned to the motor and check if it conflicts with the time parameter
        IntPair lastNoteTimes = inUseTimes.get(inUseTimes.size() - 1);
        return time >= lastNoteTimes.startTime() && time < lastNoteTimes.endTime();
    }

    /**
     * A pair of ints used to determine whether the stepper motor is in use
     * at a certain time. The ints specify the start and end times of a note
     * the motor is playing, in hundredths of a second
     */
    private record IntPair(int startTime, int endTime) {}
}

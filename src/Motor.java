import java.util.ArrayList;

public class Motor {
    
    private final int index;
    private final ArrayList<IntPair> inUseTimes;
    private final ArrayList<Note> notes;
    
    public Motor(int newIndex) {
        index = newIndex;
        inUseTimes = new ArrayList<IntPair>();
        notes = new ArrayList<Note>();
    }

    public int getIndex() {
        return index;
    }

    public ArrayList<Note> getNotes() {
        return notes;
    }
    
    public boolean addNote(Note newNote) {
        if (isInUse(newNote.getStartTime())) {
            return false;
        }
        else {
            notes.add(newNote);
            inUseTimes.add(new IntPair(newNote.getStartTime(), newNote.getStartTime() + newNote.getDuration()));
            return true;
        }
    }
    
    public boolean isInUse(int time) {
        for (IntPair current : inUseTimes) {
            if (time >= current.getStartTime() && time < current.getEndTime()) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * A pair of ints used to determine whether the stepper motor is in use
     * at a certain time. The ints specify the start and end times of a note
     * the motor is playing, in hundredths of a second
     */
    private class IntPair {
        
        private final int startTime;
        private final int endTime;
        
        public IntPair(int newStartTime, int newEndTime) {
            startTime = newStartTime;
            endTime = newEndTime;
        }
        
        private int getStartTime() {
            return startTime;
        }
        
        private int getEndTime() {
            return endTime;
        }
    }
}

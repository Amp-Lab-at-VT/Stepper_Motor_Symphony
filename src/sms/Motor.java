package sms;

import java.util.ArrayList;
import java.util.Comparator;

public class Motor {
    
    private int index;
    private ArrayList<IntPair> inUseTimes;
    private ArrayList<Note> notes;
    
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

    public ArrayList<Note> getNotes() {
        return notes;
    }

    public ArrayList<IntPair> getUsageTimes() {
        return inUseTimes;
    }

    /**
     * Adds a new note to be played on this motor.
     * @param newNote The note to be added, which must not conflict with any notes already assigned to this motor.
     */
    public void addNote(Note newNote) {
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

    public static final Comparator<Motor> onTimeDescendingOrder = (m1, m2) -> {
        int difference = m1.getOnTime() - m2.getOnTime();

        return Integer.compare(0, difference);
    };

    private int getOnTime() {
        int sum = 0;

        for (var t : inUseTimes) {
            sum += t.endTime - t.startTime;
        }

        return sum;
    }

    public boolean conflictsWith(Motor other) {
        for (var p1 : inUseTimes) {
            for (var p2 : other.inUseTimes) {
                if (p1.conflictsWith(p2)) {
                    return true;
                }
            }
        }

        return false;
    }

    public double getPercentConflict(Motor other) {
        int m1OnTime = 0;
        int m2OnTime = 0;
        for (var p1 : inUseTimes) {
            m1OnTime += p1.endTime - p1.startTime;
        }
        for (var p2 : other.inUseTimes) {
            m2OnTime += p2.endTime - p2.startTime;
        }

        int conflictDuration = 0;
        for (var p1 : inUseTimes) {
            for (var p2 : other.inUseTimes) {
//                if (p1.startTime >= p2.startTime && p1.startTime < p2.endTime) {
//                    conflictDuration += p2.endTime - p1.startTime;
//                } else if (p2.startTime >= p1.startTime && p2.startTime < p1.endTime) {
//                    conflictDuration += p1.endTime - p2.startTime;
//                }
                // Check if the ranges overlap
                if (p1.startTime < p2.endTime && p1.endTime > p2.startTime) {
                    int overlapLow = Math.max(p1.startTime, p2.startTime);
                    int overlapHigh = Math.min(p1.endTime, p2.endTime);
                    conflictDuration += overlapHigh - overlapLow;
                }
            }
        }

        if (m1OnTime > m2OnTime) {
            return ((double) conflictDuration) / m1OnTime;
        } else {
            return ((double) conflictDuration) / m2OnTime;
        }
    }

    public void forceCombine(Motor other) {
        ArrayList<Note> combined = new ArrayList<>();
        ArrayList<IntPair> newUsageTimes = new ArrayList<>();

        // Perform a merge sort on each motor's note lists
        // If the next note to add conflicts with the note that was added last,
        // update the last note's duration such that the notes won't conflict
        int index1 = 0;
        int index2 = 0;
        while (index1 < this.notes.size() || index2 < other.notes.size()) {
            // Get the next data to add to the combined list
            Note nextNote;
            IntPair nextUsageTime;
            if (index1 == this.notes.size()) { // Reached end of list 1
                nextNote = other.notes.get(index2);
                nextUsageTime = other.inUseTimes.get(index2);
                index2++;
            } else if (index2 == other.notes.size()) { // Reached end of list 2
                nextNote = notes.get(index1);
                nextUsageTime = inUseTimes.get(index1);
                index1++;
            } else if (inUseTimes.get(index1).startTime < other.inUseTimes.get(index2).startTime) {
                // Note in list 1 comes before note in list 2
                nextNote = notes.get(index1);
                nextUsageTime = inUseTimes.get(index1);
                index1++;
            } else { // Note in list 2 comes before note in list 1
                nextNote = other.notes.get(index2);
                nextUsageTime = other.inUseTimes.get(index2);
                index2++;
            }

            // Check if the next data to add conflicts with the previous
            if (!combined.isEmpty() && newUsageTimes.get(newUsageTimes.size() - 1).conflictsWith(nextUsageTime)) {
                // Change the previously added data so that it doesn't conflict
                var oldNote = combined.get(combined.size() - 1);
                int newDuration = nextNote.startTime() - oldNote.startTime();
                var newNote = new Note(oldNote.startTime(), oldNote.pitch(), newDuration, oldNote.voiceIndex());
                combined.remove(combined.size() - 1);
                combined.add(newNote);

                var oldUsagePair = newUsageTimes.get(newUsageTimes.size() - 1);
                var newUsageTime = new IntPair(oldUsagePair.startTime, nextUsageTime.endTime);
                newUsageTimes.remove(newUsageTimes.size() - 1);
                newUsageTimes.add(newUsageTime);
            }
            combined.add(nextNote);
            newUsageTimes.add(nextUsageTime);
        }

        // Now we add the last notes from the list that didn't reach the end
        notes = combined;
        inUseTimes = newUsageTimes;
    }

    /**
     * A pair of ints used to determine whether the stepper motor is in use
     * at a certain time. The ints specify the start and end times of a note
     * the motor is playing, in hundredths of a second
     */
    record IntPair(int startTime, int endTime) {
        public boolean conflictsWith(IntPair other) {
            return (this.startTime >= other.startTime && this.startTime < other.endTime)
                    || (other.startTime >= this.startTime && other.startTime < this.endTime);
        }
    }
}

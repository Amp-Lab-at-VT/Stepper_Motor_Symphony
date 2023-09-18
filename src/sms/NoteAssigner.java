package sms;

import sms.Motor;
import sms.Note;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Assigns notes to stepper motors
 */
public class NoteAssigner {

    private static final double ACCEPTABLE_CONFLICT_THRESHOLD = 0.03;

    public static ArrayList<Motor> assign(List<Note> notes) {
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

                // If the condensing assignment added more than one motor, check the percentage of time
                // that notes between the motors are playing at the same time. If the percentage is less
                // than the acceptable threshold, then combine the motors' note lists into one.
                if (voiceMotors.size() > 1) {
                    for (int n = 1; n < voiceMotors.size(); n++) {
                        double conflict = voiceMotors.get(0).getPercentConflict(voiceMotors.get(n));
                        System.out.println("% conflict: " + conflict);

                        // Combine the motors' note lists
                        if (conflict <= ACCEPTABLE_CONFLICT_THRESHOLD) {
                            voiceMotors.get(0).forceCombine(voiceMotors.get(n));
                            voiceMotors.remove(n);
                            n--;
                        }
                    }
                }
                motors.addAll(voiceMotors);

                firstVoiceNoteIndex = i;
                currentVoice = notes.get(i).voiceIndex();
            }
        }

        // Check if there are still notes left to be added
        if (firstVoiceNoteIndex < notes.size() - 1) {
            var sublist = notes.subList(firstVoiceNoteIndex, notes.size());
            var voiceMotors = condensingAssign(sublist);
            if (voiceMotors.size() > 1) {
                for (int n = 1; n < voiceMotors.size(); n++) {
                    double conflict = voiceMotors.get(0).getPercentConflict(voiceMotors.get(n));
                    System.out.println("% conflict: " + conflict);

                    // Combine the motors' note lists
                    if (conflict <= ACCEPTABLE_CONFLICT_THRESHOLD) {
                        voiceMotors.get(0).forceCombine(voiceMotors.get(n));
                        voiceMotors.remove(n);
                        n--;
                    }
                }
            }
            motors.addAll(voiceMotors);
        }

        // Sort the motors by the length of time that they are playing a note
        // Motors that get used more often will appear earlier in the list
        motors.sort(Motor.onTimeDescendingOrder);

        // Set the index for each motor
        for (int i = 0; i < motors.size(); i++) {
            motors.get(i).setIndex(i);
        }

        joinTracks(motors);

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
    public static List<Motor> condensingAssign(List<Note> notes) {
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

    private static void joinTracks(List<Motor> motors) {
        boolean finished = false;
        while (!finished) {
            ListIterator<Motor> i1 = motors.listIterator(motors.size());
            ListIterator<Motor> i2 = motors.listIterator(motors.size());
            finished = true;

            boolean alreadyCombinedList = false;
            while (i1.hasPrevious()) {
                if (alreadyCombinedList) break;

                Motor m1 = i1.previous();

                while (i2.hasPrevious()) {
                    Motor m2 = i2.previous();
                    boolean conflict = m1.conflictsWith(m2);
                    if (!conflict) {
                        finished = false;

                        // Combine motors' note lists into the motor with the lower index
                        Motor higher;
                        Motor lower;
                        if (m1.getIndex() > m2.getIndex()) {
                            higher = m1;
                            lower = m2;
                        } else {
                            higher = m2;
                            lower = m1;
                        }
                        for (var note : higher.getNotes()) {
                            lower.getNotes().add(note);
                            lower.getUsageTimes().add(new Motor.IntPair(note.startTime(), note.startTime() + note.duration()));
                        }
                        lower.getNotes().sort(Note.chronologicalOrder);

                        // Remove the motor with the higher index
                        motors.remove(higher);
                        alreadyCombinedList = true;
                        break;
                    }
                }
            }
        }
    }
}

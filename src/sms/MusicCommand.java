package sms;

import java.util.Comparator;

/**
 * An interface for a music command that will be uploaded to a microcontroller
 * and executed to create a sound
 */
public interface MusicCommand {

    // All music commands must have a time within the song to start at
    int startTime = 0;

    /**
     * Used for sorting music commands in chronological order
     */
    Comparator<MusicCommand> chronologicalOrder
            = Comparator.comparingInt(c -> c.startTime);
}

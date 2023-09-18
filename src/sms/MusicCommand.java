package sms;

import java.util.Comparator;

public interface MusicCommand {
    int startTime = 0;

    Comparator<MusicCommand> chronologicalOrder
            = Comparator.comparingInt(c -> c.startTime);
}

package sms;


import java.util.Comparator;

public record Percussion(int startTime, Type type) implements MusicCommand {
    public enum Type {
        ACOUSTIC_BASS_DRUM, BASS_DRUM, SIDE_STICK, ACOUSTIC_SNARE, HAND_CLAP,
        ELECTRIC_SNARE, LO_FLOOR_TOM, CLOSED_HI_HAT, HIGH_FLOOR_TOM, PEDAL_HI_HAT, LO_TOM, OPEN_HI_HAT, LO_MID_TOM,
        HI_MID_TOM, CRASH_CYMBAL_1, HI_TOM, RIDE_CYMBAL_1, CHINESE_CYMBAL, RIDE_BELL, TAMBOURINE, SPLASH_CYMBAL,
        COWBELL, CRASH_CYMBAL_2, VIBRASLAP, RIDE_CYMBAL_2, HI_BONGO, LO_BONGO, MUTE_HI_CONGA, OPEN_HI_CONGA, LO_CONGA,
        HI_TIMBALE, LO_TIMBALE, HI_AGOGO, LO_AGOGO, CABASA, MARACAS, SHORT_WHISTLE, LONG_WHISTLE, SHORT_GUIRO, LONG_GUIRO,
        CLAVES, HI_WOOD_BLOCK, LO_WOOD_BLOCK, MUTE_CUICA, OPEN_CUICA, MUTE_TRIANGLE, OPEN_TRIANGLE
    }

    public static final Comparator<Percussion> chronologicalOrder
            = Comparator.comparingInt(p -> p.startTime);

    /**
     * Returns a String representation of this percussion command's data
     */
    public String toString() {
        String startTimeSeconds = (startTime / 100) + "." + String.format("%02d", (startTime % 100)) + "s";
        return "Percussion " + type + " at " + startTimeSeconds;
    }
}

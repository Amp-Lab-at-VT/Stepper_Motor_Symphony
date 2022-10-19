import java.util.HashMap;
import java.util.Map;

/**
 * Maps string representations of note pitches to their corresponding
 * frequency, measured in Hz
 */
public class Frequencies {

    private Map<String, Double> frequencies;

    /**
     * Sets up the frequency map
     */
    public Frequencies() {
        frequencies = new HashMap<String, Double>();
        
        frequencies.put("C0", 16.35);
        frequencies.put("C#0", 17.32);
        frequencies.put("Db0", 17.32);
        frequencies.put("D0", 18.35);
        frequencies.put("D#0", 19.45);
        frequencies.put("Eb0", 19.45);
        frequencies.put("E0", 20.60);
        frequencies.put("F0", 21.83);
        frequencies.put("F#0", 23.12);
        frequencies.put("Gb0", 23.12);
        frequencies.put("G0", 24.50);
        frequencies.put("G#0", 25.96);
        frequencies.put("Ab0", 25.96);
        frequencies.put("A0", 27.50);
        frequencies.put("A#0", 29.14);
        frequencies.put("Bb0", 29.14);
        frequencies.put("B0", 30.87);

        frequencies.put("C1", 32.70);
        frequencies.put("C#1", 34.65);
        frequencies.put("Db1", 34.65);
        frequencies.put("D1", 36.71);
        frequencies.put("D#1", 38.89);
        frequencies.put("Eb1", 38.89);
        frequencies.put("E1", 41.20);
        frequencies.put("F1", 43.65);
        frequencies.put("F#1", 46.25);
        frequencies.put("Gb1", 46.25);
        frequencies.put("G1", 49.00);
        frequencies.put("G#1", 51.91);
        frequencies.put("Ab1", 51.91);
        frequencies.put("A1", 55.00);
        frequencies.put("A#1", 58.27);
        frequencies.put("Bb1", 58.27);
        frequencies.put("B1", 61.74);

        frequencies.put("C2", 65.41);
        frequencies.put("C#2", 69.30);
        frequencies.put("Db2", 69.30);
        frequencies.put("D2", 73.42);
        frequencies.put("D#2", 77.78);
        frequencies.put("Eb2", 77.78);
        frequencies.put("E2", 82.41);
        frequencies.put("F2", 87.31);
        frequencies.put("F#2", 92.50);
        frequencies.put("Gb2", 92.50);
        frequencies.put("G2", 98.00);
        frequencies.put("G#2", 103.83);
        frequencies.put("Ab2", 103.83);
        frequencies.put("A2", 110.00);
        frequencies.put("A#2", 116.54);
        frequencies.put("Bb2", 116.54);
        frequencies.put("B2", 123.47);

        frequencies.put("C3", 130.81);
        frequencies.put("C#3", 138.59);
        frequencies.put("Db3", 138.59);
        frequencies.put("D3", 146.83);
        frequencies.put("D#3", 155.56);
        frequencies.put("Eb3", 155.56);
        frequencies.put("E3", 164.81);
        frequencies.put("F3", 174.61);
        frequencies.put("F#3", 185.00);
        frequencies.put("Gb3", 185.00);
        frequencies.put("G3", 196.00);
        frequencies.put("G#3", 207.65);
        frequencies.put("Ab3", 207.65);
        frequencies.put("A3", 220.00);
        frequencies.put("A#3", 233.08);
        frequencies.put("Bb3", 233.08);
        frequencies.put("B3", 246.94);

        frequencies.put("C4", 261.63);
        frequencies.put("C#4", 277.18);
        frequencies.put("Db4", 277.18);
        frequencies.put("D4", 293.66);
        frequencies.put("D#4", 311.13);
        frequencies.put("Eb4", 311.13);
        frequencies.put("E4", 329.63);
        frequencies.put("F4", 349.23);
        frequencies.put("F#4", 369.99);
        frequencies.put("Gb4", 369.99);
        frequencies.put("G4", 392.00);
        frequencies.put("G#4", 415.30);
        frequencies.put("Ab4", 415.30);
        frequencies.put("A4", 440.00);
        frequencies.put("A#4", 466.16);
        frequencies.put("Bb4", 466.16);
        frequencies.put("B4", 493.88);

        frequencies.put("C5", 523.25);
        frequencies.put("C#5", 554.37);
        frequencies.put("Db5", 554.37);
        frequencies.put("D5", 587.33);
        frequencies.put("D#5", 622.25);
        frequencies.put("Eb5", 622.25);
        frequencies.put("E5", 659.25);
        frequencies.put("F5", 698.46);
        frequencies.put("F#5", 739.99);
        frequencies.put("Gb5", 739.99);
        frequencies.put("G5", 783.99);
        frequencies.put("G#5", 830.61);
        frequencies.put("Ab5", 830.61);
        frequencies.put("A5", 880.00);
        frequencies.put("A#5", 932.33);
        frequencies.put("Bb5", 932.33);
        frequencies.put("B5", 987.77);

        frequencies.put("C6", 1046.50);
        frequencies.put("C#6", 1108.73);
        frequencies.put("Db6", 1108.73);
        frequencies.put("D6", 1174.66);
        frequencies.put("D#6", 1244.51);
        frequencies.put("Eb6", 1244.51);
        frequencies.put("E6", 1318.51);
        frequencies.put("F6", 1396.91);
        frequencies.put("F#6", 1479.98);
        frequencies.put("Gb6", 1479.98);
        frequencies.put("G6", 1567.98);
        frequencies.put("G#6", 1661.22);
        frequencies.put("Ab6", 1661.22);
        frequencies.put("A6", 1760.00);
        frequencies.put("A#6", 1864.66);
        frequencies.put("Bb6", 1864.66);
        frequencies.put("B6", 1975.53);

        frequencies.put("C7", 2093.00);
        frequencies.put("C#7", 2217.46);
        frequencies.put("Db7", 2217.46);
        frequencies.put("D7", 2349.32);
        frequencies.put("D#7", 2489.02);
        frequencies.put("Eb7", 2489.02);
        frequencies.put("E7", 2637.02);
        frequencies.put("F7", 2793.83);
        frequencies.put("F#7", 2959.96);
        frequencies.put("Gb7", 2959.96);
        frequencies.put("G7", 3135.96);
        frequencies.put("G#7", 3322.44);
        frequencies.put("Ab7", 3322.44);
        frequencies.put("A7", 3520.00);
        frequencies.put("A#7", 3729.31);
        frequencies.put("Bb7", 3729.31);
        frequencies.put("B7", 3951.07);

        frequencies.put("C8", 4186.01);
        frequencies.put("C#8", 4434.92);
        frequencies.put("Db8", 4434.92);
        frequencies.put("D8", 4698.63);
        frequencies.put("D#8", 4978.03);
        frequencies.put("Eb8", 4978.03);
        frequencies.put("E8", 5274.04);
        frequencies.put("F8", 5587.65);
        frequencies.put("F#8", 5919.91);
        frequencies.put("Gb8", 5919.91);
        frequencies.put("G8", 6271.93);
        frequencies.put("G#8", 6644.88);
        frequencies.put("Ab8", 6644.88);
        frequencies.put("A8", 7040.00);
        frequencies.put("A#8", 7458.62);
        frequencies.put("Bb8", 7458.62);
        frequencies.put("B8", 7902.13);
    }
    
    public double get(String pitch) {
        return frequencies.get(pitch);
    }
}

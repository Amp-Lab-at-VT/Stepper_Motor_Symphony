/**
 * This class contains length data for notes that store their duration as
 * a letter instead of a number
 * @author Dylan Green
 * @version 2022.04.18
 */
public class NoteLengths {

    /**
     * 
     * @param letter The letter representation of a given note's length
     * @return The length that the given letter corresponds to, as a fraction
     * of the amount of time a full measure takes up
     */
    public static double getLength(char letter) {
        return switch (letter) {
            case 'w' -> 1.0;
            case 'h' -> 0.5;
            case 'q' -> 0.25;
            case 'i' -> 0.125;
            case 's' -> 0.0625;
            case 't' -> 0.03125;
            case 'x' -> 0.015625;
            case 'o' -> 0.0078125;
            default -> 0;
        };
    }
}

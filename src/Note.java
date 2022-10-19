/**
 * Stores data of a single note within the MIDI file
 * 
 * @version 2021.07.21
 */
public class Note implements Comparable<Note> {
    
    private int startTime;
    private double pitch;
    private int duration;
    private String name;
    
    /**
     * Creates a new note
     * 
     * @param startTime The start time of this note within the midi file, in number
     * of measures since the start
     * @param pitch The pitch of the note in Hz
     * @param duration The length of the note in number of measures
     */
    public Note (double startTime, double pitch, double duration) {
        
        this.startTime = (int) Math.ceil(startTime * Parser.getBeatsPerMeasure() * (1.0 / Parser.getTempo()) * 60 * 100);
        this.pitch = pitch;
        this.duration = (int) Math.floor(duration * Parser.getBeatsPerMeasure() * (1.0 / Parser.getTempo()) * 60 * 100);
    }
    
    /**
     * Gets the time at which this note starts playing, in hundredths of
     * seconds since the start of the Arduino program.
     * 
     * @return The note's start time
     */
    public int getStartTime() {
        return this.startTime;
    }
    
    /**
     * Gets this note's pitch value.
     * 
     * @return The note's pitch
     */
    public double getPitch() {
        return this.pitch;
    }
    
    /**
     * Gets this note's duration in hundredths of seconds
     * 
     * @return The note's duration
     */
    public int getDuration() {
        return this.duration;
    }
    
    /**
     * Returns a String representation of this note's data
     */
    public String toString() {
        String startTimeSeconds = (startTime / 100) + "." + (startTime % 100) + "s";
        int endTime = startTime + duration;
        String endTimeSeconds = (endTime / 100) + "." + (endTime % 100) + "s";
        return "Note " + pitch + " at " + startTimeSeconds + " until " + endTimeSeconds;
    }
    
    public int compareTo(Note other) {
        double diff = this.startTime - other.startTime;
        
        if (diff < 0) return -1;
        if (diff > 0) return 1;
        return 0;
    }

    //TODO
    public void setName(String newName) {
        name = newName;
    }
    public String getName() {
        return name;
    }

}

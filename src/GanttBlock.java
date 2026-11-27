// One box in the Gantt chart. The label is "idle" when the CPU has nothing to run

public class GanttBlock {
    public String label;
    public int startTime;
    public int endTime;

    public GanttBlock(String label, int startTime, int endTime) {
        this.label = label;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
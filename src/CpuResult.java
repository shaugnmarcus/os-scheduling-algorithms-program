// Everything our CPU algorithms produces. calculateTotals() fills in the turnaround, waiting, averages, utilization and throughput
 
import java.util.ArrayList;
import java.util.List;

public class CpuResult {
    public List<ProcessInfo> processList = new ArrayList<>();
    public List<GanttBlock> ganttBlocks = new ArrayList<>();

    public double averageTurnaround;
    public double averageWaiting;
    public double averageResponse;
    public int totalTime;
    public int busyTime;
    public double cpuUtilization; // in percent
    public double throughput; // processes finished per time unit

    // Called after the algorithm has set the completion and response time of every process
    public void calculateTotals() {
        int totalTurnaround = 0;
        int totalWaiting = 0;
        int totalResponse = 0;

        for (ProcessInfo process : processList) {
            process.turnaroundTime = process.completionTime - process.arrivalTime;
            process.waitingTime = process.turnaroundTime - process.burstTime;

            totalTurnaround += process.turnaroundTime;
            totalWaiting += process.waitingTime;
            totalResponse += process.responseTime;
        }

        int numberOfProcesses = processList.size();
        averageTurnaround = (double) totalTurnaround / numberOfProcesses;
        averageWaiting = (double) totalWaiting / numberOfProcesses;
        averageResponse = (double) totalResponse / numberOfProcesses;

        busyTime = 0;
        for (GanttBlock block : ganttBlocks) {
            if (!block.label.equals("Idle")) {
                busyTime += block.endTime - block.startTime;
            }
        }

        totalTime = 0;
        if (ganttBlocks.size() > 0) {
            totalTime = ganttBlocks.get(ganttBlocks.size() - 1).endTime;
        }

        cpuUtilization = (double) busyTime / totalTime * 100;
        throughput = (double) numberOfProcesses / totalTime;
    }
}
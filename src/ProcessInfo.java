// Holds the data of one process (inputs and computed results)

public class ProcessInfo {
    public int id;               // 1 means P1, 2 means P2, ...
    public int arrivalTime;
    public int burstTime;
    public int remainingTime;    // used by round robin

    public int startTime = -1;   // -1 means it has not started yet
    public int completionTime;
    public int turnaroundTime;
    public int waitingTime;
    public int responseTime;

    public ProcessInfo(int id, int arrivalTime, int burstTime) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
    }
}
// Everything our disk algorithms produces. The first item in "path" is the starting head position

import java.util.ArrayList;
import java.util.List;

public class DiskResult {
    public List<Integer> path = new ArrayList<>();
    public List<Integer> movements = new ArrayList<>();   // movement of each step
    public List<String> notes = new ArrayList<>();

    public int totalMovement = 0;
    public int requestsServed = 0;
    public double averageSeekDistance = 0;

    public DiskResult(int startingHead) {
        path.add(startingHead);
    }

    public int getCurrentPosition() {
        return path.get(path.size() - 1);
    }

    // Moves the head to a new track and records the distance
    // isRequest is false when the head only goes to the disk end (not a real request)
    public void addMove(int newPosition, boolean isRequest) {
        int distance = Math.abs(newPosition - getCurrentPosition());
        path.add(newPosition);
        movements.add(distance);
        totalMovement += distance;

        if (isRequest) {
            requestsServed++;
            notes.add("Request served");
        } else {
            notes.add("Moved to disk end");
        }
    }

    // average seek distance = total head movement / number of requests
    public void calculateAverage() {
        if (requestsServed > 0) {
            averageSeekDistance = (double) totalMovement / requestsServed;
        }
    }
}
package src;
import org.json.simple.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class Process {

    public String name;
    public int arrival_time;
    public int burst_time;
    public int remaining_time;
    public int priority;
    public int completion_time;
    public int waiting_time;
    int lastExecuted;
    public int turnaround_time;
    int startTime = -1;

    public int quantum;
    public List<Integer> quantumTimeHistory = new ArrayList<>();

    public int lastReadyTime;
    public int readyQueueIndex;

    public Process(String name, int burst, int priority, Integer quantum, int arrival) {
        this.name = name;
        this.burst_time = burst;
        this.remaining_time = burst;
        this.priority = priority;
        this.quantum = (quantum == null ? 0 : quantum);
        this.arrival_time = arrival;

        this.quantumTimeHistory = new ArrayList<>();
        this.quantumTimeHistory.add(this.quantum);
    }

    public void recordQuantum() {
        quantumTimeHistory.add(quantum);
    }

    public void resetQuantumHistory() {
        quantumTimeHistory.clear();
        quantumTimeHistory.add(quantum);
    }

    public int[] calculateStagesTimes() {
        int q = this.quantum;
        int fcfsTime = (int) Math.ceil(0.25 * q);
        int priorityTime = (int) Math.ceil(0.25 * q);
        int sjfTime = q - (fcfsTime + priorityTime);
        if (sjfTime < 0) { sjfTime = 0; }
        return new int[]{fcfsTime, priorityTime, sjfTime};
    }

    public void updateQuantumCaseI() {
        quantum += 2;
        recordQuantum();
    }

    public void updateQuantumCaseII(int remainingQ) {
        quantum += (int) Math.ceil((double) remainingQ / 2);
        recordQuantum();
    }

    public void updateQuantumCaseIII(int remainingQ) {
        quantum += remainingQ;
        recordQuantum();
    }


    public void computeTimes() {
        turnaround_time = completion_time - arrival_time;
        waiting_time = turnaround_time - burst_time;
    }

    public Process copy() {
        Process p = new Process(name, burst_time, priority, quantum, arrival_time);
        p.remaining_time = burst_time;
        p.startTime = -1;
        p.completion_time = -1;
        p.waiting_time = 0;
        p.turnaround_time = 0;

        p.quantumTimeHistory = new ArrayList<>(this.quantumTimeHistory);
        return p;
    }

    public static Process createAgProcess(JSONObject obj) {
        String name = (String) obj.get("name");
        int arrival = ((Long) obj.get("arrival")).intValue();
        int burst = ((Long) obj.get("burst")).intValue();
        int priority = ((Long) obj.get("priority")).intValue();
        Integer quantum = obj.containsKey("quantum")
                ? ((Long) obj.get("quantum")).intValue()
                : null;

        return new Process(name, burst, priority, quantum, arrival);
    }

    public void updateQuantumHistory(int newValue) {
        this.quantum = newValue;
        this.quantumTimeHistory.add(newValue);
    }

    public String getQuantumHistoryString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < quantumTimeHistory.size(); i++) {
            sb.append(quantumTimeHistory.get(i));
            if (i < quantumTimeHistory.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
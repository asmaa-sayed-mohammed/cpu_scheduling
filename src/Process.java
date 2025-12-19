package src;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class Process {
//    JSONObject obj = new JSONObject();
//    JSONArray arr = new JSONArray();

    public int time_quantum;
    public int burst_time;
    public int priority;
    public String name;
    public int remaining_time;
    public int arrival_time;
    public int lastReadyTime;
    public int readyQueueIndex;
    public int waiting_time = 0;
    public int turnaround_time = 0;
    public int completion_time = 0;

    public Process(String name, int burst_time, int priority, Integer time_quantum, int arrival_time){
        this.name = name;
        this.burst_time = burst_time;
        this.priority = priority;
        this.time_quantum = (time_quantum == null ? 0 : time_quantum);
        this.arrival_time = arrival_time;
        this.remaining_time = burst_time;
    }

    public List<Integer> computeTimes(){
        List<Integer> times = new ArrayList<>(2);
        turnaround_time = completion_time - arrival_time;
        waiting_time = turnaround_time - burst_time;
        times.add(turnaround_time);
        times.add(waiting_time);
        return times;
    }

    public static Process createAgProcess(JSONObject obj) {
        String name = (String) obj.get("name");
        int arrival = ((Long) obj.get("arrival")).intValue();
        int burst = ((Long) obj.get("burst")).intValue();
        int priority = ((Long) obj.get("priority")).intValue();

        Integer quantum = null;
        if (obj.containsKey("quantum")) {
            quantum = ((Long) obj.get("quantum")).intValue();
        }

        return new Process(name, burst, priority, quantum, arrival);
    }
}

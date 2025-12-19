package src;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
public class RoundRobin {
    public  void run(InputData input){
        List<Process> processes = input.processes;
        int time_quantum = input.rrQuantum;
        int context_time = input.contextSwitch;

        Queue<Process> readyQueue = new LinkedList<>();
        int time = 0;
        int nfinished = 0;
        int n = processes.size();
        double averagewt = 0;
        double averagetat = 0;
        int i = 0;
        Process previous = null;
        processes.sort(Comparator.comparingInt(p -> p.arrival_time));
        while (nfinished < n) {
            while (i < n && processes.get(i).arrival_time <= time) {
                readyQueue.add(processes.get(i));
                i++;
            }
            if (readyQueue.isEmpty()) {
                time++;
                continue;
            }
            Process process = readyQueue.poll();
            if (previous != null && previous != process) {
                time += context_time;
            }
            int execution = Math.min(time_quantum, process.remaining_time);
            process.remaining_time -= execution;
            time += execution;
            while (i < n && processes.get(i).arrival_time <= time) {
                readyQueue.add(processes.get(i));
                i++;
            }
            if (process.remaining_time > 0) {
                readyQueue.add(process);
            } else {
                process.completion_time = time;
                process.turnaround_time = process.completion_time - process.arrival_time;
                process.waiting_time = process.turnaround_time - process.burst_time;
                averagewt += process.waiting_time;
                averagetat += process.turnaround_time;
                nfinished++;
            }
            previous = process;


        }
        averagewt = averagewt / n;
        averagetat = averagetat / n;
    }
}
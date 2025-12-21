package src;
import java.util.*;
public class RoundRobin implements Scheduler{
    public  general_output schedule (InputData input){
        List<Process> processes = input.processes;
        List<ProcessOutput> outputs = new ArrayList<>();
        int time_quantum = input.rrQuantum;
        int context_time = input.contextSwitch;

        Queue<Process> readyQueue = new LinkedList<>();
        List<String> executionOrder = new ArrayList<>();
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
            executionOrder.add(process.name);
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
        for (Process p : processes){
            ProcessOutput out = new ProcessOutput();
            out.name = p.name;
            out.waitingTime = p.waiting_time;
            out.turnaroundTime = p.turnaround_time;

            outputs.add(out);
        }
        averagewt = averagewt / n;
        averagetat = averagetat / n;
        double avgWaitingRounded = Math.round(averagewt * 100.0) / 100.0;
        double avgTurnaroundRounded = Math.round(averagetat * 100.0) / 100.0;
        return new general_output(executionOrder, outputs, avgWaitingRounded, avgTurnaroundRounded);
    }
}
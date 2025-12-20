package src;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class SJF implements Scheduler{

    public general_output schedule (InputData input){
        List<Process> allProcesses = input.processes;
        int contextSwitchingTime = input.contextSwitch;
        // sort all src.process by arrival_time
        allProcesses.sort(Comparator.comparingInt(p -> p.arrival_time));

        PriorityQueue<Process> readyQueue = new PriorityQueue<>(new Comparator<Process>() {
            @Override
            public int compare(Process o1, Process o2) {
                if (o1.remaining_time != o2.remaining_time){
                    return Integer.compare(o1.remaining_time , o2.remaining_time);
                }
                else {
                    return Integer.compare(o1.arrival_time , o2.arrival_time);
                }
            }
        });
        int currentTime = 0;
        int totalProcess = allProcesses.size();
        int totalCompleted = 0;// Counts completed operations

        ArrayList<String> processList = new ArrayList<>(); // To store the order in which operations are executed


        Process currentlyRunningProcess = null;
        int nextProcess = 0;

        //System.out.println("Current Time: " + currentTime + " | Completed: " + totalCompleted);
        while (totalCompleted < totalProcess) {// Continue to all processes end
            // Adding the operations that have arrived to readyQueue
            while (nextProcess < allProcesses.size() && allProcesses.get(nextProcess).arrival_time <= currentTime) {
                // If an operation arrives, then add it to readyQueue and move cursor
                readyQueue.add(allProcesses.get(nextProcess));
                nextProcess++;
            }

            // If I have src.process or more in readyQueue
            if (!readyQueue.isEmpty()) {
                Process readyPro = readyQueue.peek(); // I take the first src.process in readyQueue

                if (currentlyRunningProcess == null) { // If CPU is empty (First execution or completion of a previous src.process)
                    // Add context switching time penalty if currentTime and contextSwitching greater than 0
                    if (currentTime > 0 && contextSwitchingTime > 0) {
                        currentTime += contextSwitchingTime;
                        //  Update readyQueue because has increased
                        while (nextProcess < allProcesses.size() && allProcesses.get(nextProcess).arrival_time <= currentTime) {
                            readyQueue.add(allProcesses.get(nextProcess));
                            nextProcess++;
                        }
                    }

                    currentlyRunningProcess = readyQueue.poll();
                }
                else if (readyPro.remaining_time < currentlyRunningProcess.remaining_time) {
                    // Preemption: The src.process in the readyQueue is shorter than the one currently running.

                    // Stop the currently running src.process and return it to the queue
                    readyQueue.add(currentlyRunningProcess);

                    // Add context switching time penalty
                    currentTime += contextSwitchingTime;

                    // Take readyPro from readyQueue
                    currentlyRunningProcess = readyQueue.poll();
                }
                // else: No preemption, currentlyRunningProcess continues execution (no context switch)
            }

            // Execution and Check if the src.process ended
            if (currentlyRunningProcess != null) {
                // Add src.process name to processList
                if (processList.isEmpty() || !processList.get(processList.size() - 1).equals(currentlyRunningProcess.name)) {
                    processList.add(currentlyRunningProcess.name);
                }

                currentlyRunningProcess.remaining_time--;
                currentTime++;

                // Check if src.process is completed
                if (currentlyRunningProcess.remaining_time == 0) {
                    currentlyRunningProcess.completion_time = currentTime;
                    List<Integer> times = new ArrayList<>(2);
                    times = currentlyRunningProcess.computeTimes();// Compute times
                    totalCompleted++;
                    currentlyRunningProcess = null; // Freeing up the CPU to allow a new src.process to work in the next time.
                }
            } else {
                // There are no ready processes and no processes in operation.
                currentTime++;
            }
        }
        // Calculate Averages
        List<ProcessOutput> processOutputs = new ArrayList<>();
        double turnaroundTime = 0;
        double waitingTime = 0;

        for (Process p : allProcesses) {
            ProcessOutput out = new ProcessOutput();
            out.name = p.name;
            out.waitingTime = p.waiting_time;
            out.turnaroundTime = p.turnaround_time;

            waitingTime += out.waitingTime;
            turnaroundTime += out.turnaroundTime;

            processOutputs.add(out);
        }

        double avgTurnaroundTime = turnaroundTime / allProcesses.size();
        double avgWaitingTime = waitingTime / allProcesses.size();
        double avgWaitingRounded = Math.round(avgWaitingTime * 10.0) / 10.0;
        double avgTurnaroundRounded = Math.round(avgTurnaroundTime * 10.0) / 10.0;
        return new general_output(processList, processOutputs, avgWaitingRounded, avgTurnaroundRounded);
    }
}

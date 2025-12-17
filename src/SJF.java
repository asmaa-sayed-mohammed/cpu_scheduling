import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class SJF {

    public void runScheduling (ArrayList<process> allProcesses , int contextSwitchingTime){
        // sort all process by arrival_time
        allProcesses.sort(Comparator.comparingInt(p -> p.arrival_time));

        PriorityQueue<process> readyQueue = new PriorityQueue<>(new Comparator<process>() {
            @Override
            public int compare(process o1, process o2) {
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


        process currentlyRunningProcess = null;
        int nextProcess = 0;

        //System.out.println("Current Time: " + currentTime + " | Completed: " + totalCompleted);
        while (totalCompleted < totalProcess) {// Continue to all processes end
            // Adding the operations that have arrived to readyQueue
            while (nextProcess < allProcesses.size() && allProcesses.get(nextProcess).arrival_time <= currentTime) {
                // If an operation arrives, then add it to readyQueue and move cursor
                readyQueue.add(allProcesses.get(nextProcess));
                nextProcess++;
            }

            // If I have process or more in readyQueue
            if (!readyQueue.isEmpty()) {
                process readyPro = readyQueue.peek(); // I take the first process in readyQueue

                if (currentlyRunningProcess == null) { // If CPU is empty (First execution or completion of a previous process)
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
                    // Preemption: The process in the readyQueue is shorter than the one currently running.

                    // Stop the currently running process and return it to the queue
                    readyQueue.add(currentlyRunningProcess);

                    // Add context switching time penalty
                    currentTime += contextSwitchingTime;

                    // Take readyPro from readyQueue
                    currentlyRunningProcess = readyQueue.poll();
                }
                // else: No preemption, currentlyRunningProcess continues execution (no context switch)
            }

            // Execution and Check if the process ended
            if (currentlyRunningProcess != null) {
                // Add process name to processList
                if (processList.isEmpty() || !processList.getLast().equals(currentlyRunningProcess.name)) {
                    processList.add(currentlyRunningProcess.name);
                }

                currentlyRunningProcess.remaining_time--;
                currentTime++;

                // Check if process is completed
                if (currentlyRunningProcess.remaining_time == 0) {
                    currentlyRunningProcess.completion_time = currentTime;
                    currentlyRunningProcess.computeTimes(); // Compute times
                    totalCompleted++;
                    currentlyRunningProcess = null; // Freeing up the CPU to allow a new process to work in the next time.
                }
            } else {
                // There are no ready processes and no processes in operation.
                currentTime++;
            }
        }
        // Calculate Averages
        double turnaroundTime = 0;
        double waitingTime = 0;

        for (process p : allProcesses){
            turnaroundTime += p.turnaround_time;
            waitingTime += p.waiting_time;
        }

        double avgTurnaroundTime = turnaroundTime / allProcesses.size();
        double avgWaitingTime = waitingTime / allProcesses.size();

        System.out.println("\n------------------- Gantt Chart Simplified -------------------");
        // Print Simplified Gantt Chart
        for (String string : processList) {
            System.out.print(string + " ");
        }
        System.out.println("\n");
        System.out.println("---------------------------------------------------------------------------------------------------");

        System.out.println("\n-------------------- Print Processes Data --------------------");


        System.out.printf("%-10s | %-15s | %-15s | %-15s | %-15s | %-15s\n",
                "Process", "Arrival Time", "Burst Time", "Completion Time", "Turnaround Time", "Waiting Time");
        System.out.println("---------------------------------------------------------------------------------------------------");

        // Data for each process
        for (process p : allProcesses) {
            System.out.printf("%-10s | %-15d | %-15d | %-15d | %-15d | %-15d\n",
                    p.name, p.arrival_time, p.burst_time, p.completion_time, p.turnaround_time, p.waiting_time);
        }
        System.out.println("---------------------------------------------------------------------------------------------------");


        // Print Average Summary
        System.out.println("\n----------- Average Data  ------------");
        System.out.printf("Average Turnaround Time (ATT): %.2f\n", avgTurnaroundTime); // Print with 2 decimal places
        System.out.printf("Average Waiting Time (AWT): %.2f\n", avgWaitingTime); // Print with 2 decimal places

    }
}

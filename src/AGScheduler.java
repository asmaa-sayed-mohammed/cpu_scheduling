package src;
import src.InputData;
import java.util.*;

// =========================================================
// 3. AGScheduler Main Logic (Corrected Flow with User Input)
// =========================================================
class AGScheduler {
    public List<Process> allProcesses = new ArrayList<>();

    private LinkedList<Process> readyQueue = new LinkedList<>();
    private List<Process> completedProcesses = new ArrayList<>();
    public List<process_AG_out> agOuts = new ArrayList<>();
    private List<GanttSegment> ganttSegments = new ArrayList<>();
    public List<String> executionOrder = new ArrayList<>();
    public double totalTAT = 0;
    public double totalWT = 0;

    // Constructor for use from Main.java

    // Main method for standalone execution (keeping your original)


    private void buildExecutionOrder() {
        executionOrder.clear();
        String lastProcess = null;
        for (GanttSegment seg : ganttSegments) {
            if (!seg.name.equals(lastProcess)) {
                executionOrder.add(seg.name);
                lastProcess = seg.name;
            }
        }
    }

    private void updateQuantum(Process p, int caseNum, int remainingQ) {
        if (caseNum == 1) p.quantum += 2;
        else if (caseNum == 2) p.quantum += (int) Math.ceil((double) remainingQ / 2);
        else if (caseNum == 3) p.quantum += remainingQ;

        p.updateQuantumHistory(p.quantum);
    }

    public AG_output runAG(InputData input){
        allProcesses = new ArrayList<>();
        for (Process p : input.processes) {
            Process copy = p.copy(); // This will copy the initial quantum history
            allProcesses.add(copy);
        }

        readyQueue.clear();
        completedProcesses.clear();
        ganttSegments.clear();
        executionOrder.clear();
        totalTAT = 0;
        totalWT = 0;

        for (Process p : allProcesses) {
            p.remaining_time = p.burst_time;
            p.startTime = -1;
            p.completion_time = -1;
            p.resetQuantumHistory(); // Ensure clean history with initial quantum
        }

        allProcesses.sort(Comparator.comparingInt(p -> p.arrival_time));
        runScheduler();
        buildExecutionOrder();

        agOuts.clear();
        for (Process p : completedProcesses) {
            process_AG_out ag = new process_AG_out();
            int tat = p.completion_time - p.arrival_time;
            int wt = tat - p.burst_time;
            totalTAT += tat;
            totalWT += wt;
            ag.name = p.name;
            ag.waitingTime = wt;
            ag.turnaroundTime = tat;
            ag.quantumHistory = new ArrayList<>(p.quantumTimeHistory); // Copy history
            agOuts.add(ag);
        }

        double averageWaiting = totalWT / completedProcesses.size();
        double averageTurnaround = totalTAT / completedProcesses.size();
        return new AG_output(executionOrder, agOuts, averageWaiting, averageTurnaround);
    }


    private Process selectNextProcess() {
        return readyQueue.poll();
    }

    // Finds the process that should preempt based on Priority, otherwise null.
    private Process findPriorityPreemptor() {
        if (readyQueue.isEmpty()) return null;
        return readyQueue.stream()
                .min(Comparator.comparingInt(p -> p.priority))
                .orElse(null);
    }

    // Finds the process that should preempt based on SJF, otherwise null.
    private Process findSJFPreemptor() {
        if (readyQueue.isEmpty()) return null;
        return readyQueue.stream()
                .min(Comparator.comparingInt(p -> p.remaining_time))
                .orElse(null);
    }

    public void runScheduler() {
        int currentTime = 0;
        Process currentRunning = null;
        int processIndex = 0;
        Process preemptor = null;

        while (completedProcesses.size() < allProcesses.size()) {
            while (processIndex < allProcesses.size() && allProcesses.get(processIndex).arrival_time <= currentTime) {
                if (!readyQueue.contains(allProcesses.get(processIndex))) {
                    readyQueue.add(allProcesses.get(processIndex));
                }
                processIndex++;
            }

            if (currentRunning == null || currentRunning.remaining_time == 0 || preemptor != null) {
                if (currentRunning != null && currentRunning.remaining_time == 0) {
                    currentRunning.completion_time = currentTime;
                    currentRunning.quantum = 0;
                    currentRunning.quantumTimeHistory.add(0);
                    completedProcesses.add(currentRunning);
                }

                if (preemptor != null) {
                    currentRunning = preemptor;
                    preemptor = null;
                    readyQueue.remove(currentRunning);
                } else if (!readyQueue.isEmpty()) {
                    currentRunning = selectNextProcess();
                } else {
                    if (processIndex < allProcesses.size()) {
                        currentTime = allProcesses.get(processIndex).arrival_time;
                        continue;
                    } else break;
                }

                if (currentRunning.startTime == -1) currentRunning.startTime = currentTime;
            }

            int[] stages = currentRunning.calculateStagesTimes();
            int fcfsTime = stages[0], priorityTime = stages[1], sjfTime = stages[2];
            int qBeforeRun = currentRunning.quantum, totalExecuted = 0, segmentStart = currentTime;

            if (fcfsTime > 0 && currentRunning.remaining_time > 0) {
                int runLimit = Math.min(fcfsTime, currentRunning.remaining_time);
                for (int t = 0; t < runLimit; t++) {
                    currentRunning.remaining_time--;
                    currentTime++;
                    totalExecuted++;
                    while (processIndex < allProcesses.size() && allProcesses.get(processIndex).arrival_time <= currentTime)
                        readyQueue.add(allProcesses.get(processIndex++));
                    if (currentRunning.remaining_time == 0) break;
                }
                if (totalExecuted > 0) ganttSegments.add(new GanttSegment(currentRunning.name, "FCFS", segmentStart, currentTime));
                if (currentRunning.remaining_time == 0) continue;
                Process pPreemptor = findPriorityPreemptor();
                if (pPreemptor != null && pPreemptor.priority < currentRunning.priority) {
                    currentRunning.updateQuantumCaseII(qBeforeRun - totalExecuted);
                    updateQuantum(currentRunning, 2, qBeforeRun - totalExecuted);
                    readyQueue.addLast(currentRunning);
                    preemptor = pPreemptor;
                    currentRunning = null;
                    continue;
                }
            }

            if (priorityTime > 0 && currentRunning != null && currentRunning.remaining_time > 0) {
                int runLimit = Math.min(priorityTime, currentRunning.remaining_time);
                for (int t = 0; t < runLimit; t++) {
                    currentRunning.remaining_time--;
                    currentTime++;
                    totalExecuted++;
                    while (processIndex < allProcesses.size() && allProcesses.get(processIndex).arrival_time <= currentTime)
                        readyQueue.add(allProcesses.get(processIndex++));
                    if (currentRunning.remaining_time == 0) break;
                }
                if (totalExecuted > 0) ganttSegments.add(new GanttSegment(currentRunning.name, "Priority", segmentStart, currentTime));
                if (currentRunning.remaining_time == 0) continue;
                Process sPreemptor = findSJFPreemptor();
                if (sPreemptor != null && sPreemptor.remaining_time < currentRunning.remaining_time) {
                    currentRunning.updateQuantumCaseIII(qBeforeRun - totalExecuted);
                    readyQueue.addLast(currentRunning);
                    preemptor = sPreemptor;
                    currentRunning = null;
                    continue;
                }
            }

            if (sjfTime > 0 && currentRunning != null && currentRunning.remaining_time > 0) {
                int runLimit = Math.min(sjfTime, currentRunning.remaining_time);
                for (int t = 0; t < runLimit; t++) {
                    currentRunning.remaining_time--;
                    currentTime++;
                    totalExecuted++;
                    while (processIndex < allProcesses.size() && allProcesses.get(processIndex).arrival_time <= currentTime)
                        readyQueue.add(allProcesses.get(processIndex++));
                    if (currentRunning.remaining_time == 0) break;
                    Process sPreemptor = findSJFPreemptor();
                    if (sPreemptor != null && sPreemptor.remaining_time < currentRunning.remaining_time) {
                        ganttSegments.add(new GanttSegment(currentRunning.name, "SJF", segmentStart, currentTime));
                        currentRunning.updateQuantumCaseIII(qBeforeRun - totalExecuted);
                        readyQueue.addLast(currentRunning);
                        preemptor = sPreemptor;
                        currentRunning = null;
                        break;
                    }
                }
                if (currentRunning != null && preemptor == null)
                    ganttSegments.add(new GanttSegment(currentRunning.name, "SJF", segmentStart, currentTime));
            }

            if (currentRunning != null && currentRunning.remaining_time > 0 && totalExecuted == qBeforeRun) {
                currentRunning.updateQuantumCaseI();
                readyQueue.addLast(currentRunning);
                currentRunning = null;
            } else if (currentRunning != null && currentRunning.remaining_time == 0) continue;
        }
    }


    // **********************************************
    // Print Results
    // **********************************************

    public void printResults() {

        System.out.println("=================================================");
        System.out.println("           AG SCHEDULER SIMULATION RESULTS");
        System.out.println("=================================================");

        System.out.println("\n--- Detailed Execution Timeline (Gantt) ---");

        if (ganttSegments.isEmpty()) {
            System.out.println("No segments generated. Check input data or scheduler logic.");
            return;
        }





        System.out.println("\n--- Metrics Table ---");
        System.out.printf("%-5s | %-5s | %-5s | %-5s | %-5s | %-5s%n",
                "Proc", "AT", "BT", "CT", "TAT", "WT");
        System.out.println("----------------------------------------------");

        completedProcesses.sort(Comparator.comparing(p -> p.name));



        System.out.println("\n----------------------------------------------");
        System.out.printf("Average Waiting Time (AWT): %.2f%n", totalWT / completedProcesses.size());
        System.out.printf("Average Turnaround Time (ATT): %.2f%n", totalTAT / completedProcesses.size());
        System.out.println("=================================================");
    }

    // Add a simple run() method for Main.java compatibility
    public void run() {
        runScheduler();
    }

    // In AGScheduler.java, add these methods:

    public List<GanttSegment> getGanttSegments() {
        return ganttSegments;
    }

    public List<Process> getCompletedProcesses() {
        return completedProcesses;
    }
}

// =========================================================
// 2. Gantt Segment Class
// =========================================================
class GanttSegment {
    public String name;
    public String stage;
    public int startTime;
    public int endTime;

    public GanttSegment(String name, String stage, int startTime, int endTime) {
        this.name = name;
        this.stage = stage;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return String.format("%s(%s) [%d-%d]", name, stage, startTime, endTime);
    }
}
//import java.util.*;
//
//class AGScheduler {
//    private LinkedList<process> readyQueue = new LinkedList<>();
//    private List<process> allProcesses;
//    private List<process> completedProcesses = new ArrayList<>();
//    private List<GanttSegment> ganttSegments = new ArrayList<>();
//
//    public AGScheduler(List<process> processes) {
//        this.allProcesses = new ArrayList<>(processes);
//        allProcesses.sort(Comparator.comparingInt(p -> p.arrival_time));
//    }
//
//    public List<GanttSegment> getGanttSegments() {
//        return ganttSegments;
//    }
//
//    private int[] calculateStagesTimes(process p) {
//        int q = p.time_quantum;
//        int fcfsTime = (int) Math.ceil(0.25 * q);
//        int priorityTime = (int) Math.ceil(0.25 * q);
//        int sjfTime = q - (fcfsTime + priorityTime);
//        return new int[]{fcfsTime, priorityTime, Math.max(0, sjfTime)};
//    }
//
//    private void updateQuantum(process p, int caseNum, int remainingQ) {
//        if (caseNum == 1) p.time_quantum += 2;
//        else if (caseNum == 2) p.time_quantum += (int) Math.ceil((double) remainingQ / 2);
//        else if (caseNum == 3) p.time_quantum += remainingQ;
//
//        p.updateQuantumHistory(p.time_quantum);
//    }
//
//    private process findPriorityPreemptor() {
//        if (readyQueue.isEmpty()) return null;
//        return readyQueue.stream().min(Comparator.comparingInt(p -> p.priority)).orElse(null);
//    }
//
//    private process findSJFPreemptor() {
//        if (readyQueue.isEmpty()) return null;
//        return readyQueue.stream().min(Comparator.comparingInt(p -> p.remaining_time)).orElse(null);
//    }
//
//    public void runScheduler() {
//        int currentTime = 0;
//        process currentRunning = null;
//        int processIndex = 0;
//        process preemptor = null;
//
//        while (completedProcesses.size() < allProcesses.size()) {
//            while (processIndex < allProcesses.size() && allProcesses.get(processIndex).arrival_time <= currentTime) {
//                if (!readyQueue.contains(allProcesses.get(processIndex))) {
//                    readyQueue.add(allProcesses.get(processIndex));
//                }
//                processIndex++;
//            }
//
//            if (currentRunning == null || currentRunning.remaining_time == 0 || preemptor != null) {
//                if (currentRunning != null && currentRunning.remaining_time == 0) {
//                    currentRunning.completion_time = currentTime;
//                    currentRunning.computeTimes();
//                    currentRunning.time_quantum = 0;
//                    currentRunning.quantumHistory.add(0);
//                    completedProcesses.add(currentRunning);
//                }
//
//                if (preemptor != null) {
//                    currentRunning = preemptor;
//                    preemptor = null;
//                    readyQueue.remove(currentRunning);
//                } else if (readyQueue.isEmpty()) {
//                    if (processIndex < allProcesses.size()) {
//                        currentTime = allProcesses.get(processIndex).arrival_time;
//                        continue;
//                    } else break;
//                } else {
//                    currentRunning = readyQueue.poll();
//                }
//            }
//
//            int[] stages = calculateStagesTimes(currentRunning);
//            int qBeforeRun = currentRunning.time_quantum;
//            int totalExecutedInThisTurn = 0;
//            int segmentStart = currentTime;
//
//            // FCFS Stage
//            int runFCFS = Math.min(stages[0], currentRunning.remaining_time);
//            for (int i = 0; i < runFCFS; i++) {
//                currentRunning.remaining_time--;
//                currentTime++;
//                totalExecutedInThisTurn++;
//                updateArrived(currentTime, processIndex);
//                if (currentRunning.remaining_time == 0) break;
//            }
//            ganttSegments.add(new GanttSegment(currentRunning.name, "FCFS", segmentStart, currentTime));
//            if (currentRunning.remaining_time == 0) continue;
//
//            // Priority Check
//            process pPre = findPriorityPreemptor();
//            if (pPre != null && pPre.priority < currentRunning.priority) {
//                updateQuantum(currentRunning, 2, qBeforeRun - totalExecutedInThisTurn);
//                readyQueue.addLast(currentRunning);
//                preemptor = pPre;
//                currentRunning = null;
//                continue;
//            }
//
//            // Priority Stage
//            segmentStart = currentTime;
//            int runPriority = Math.min(stages[1], currentRunning.remaining_time);
//            for (int i = 0; i < runPriority; i++) {
//                currentRunning.remaining_time--;
//                currentTime++;
//                totalExecutedInThisTurn++;
//                updateArrived(currentTime, processIndex);
//                if (currentRunning.remaining_time == 0) break;
//            }
//            ganttSegments.add(new GanttSegment(currentRunning.name, "Priority", segmentStart, currentTime));
//            if (currentRunning.remaining_time == 0) continue;
//
//            // SJF Check
//            process sPre = findSJFPreemptor();
//            if (sPre != null && sPre.remaining_time < currentRunning.remaining_time) {
//                updateQuantum(currentRunning, 3, qBeforeRun - totalExecutedInThisTurn);
//                readyQueue.addLast(currentRunning);
//                preemptor = sPre;
//                currentRunning = null;
//                continue;
//            }
//
//            // SJF Preemptive Stage
//            segmentStart = currentTime;
//            int runSJF = Math.min(stages[2], currentRunning.remaining_time);
//            boolean isPreempted = false;
//            for (int i = 0; i < runSJF; i++) {
//                currentRunning.remaining_time--;
//                currentTime++;
//                totalExecutedInThisTurn++;
//                updateArrived(currentTime, processIndex);
//                if (currentRunning.remaining_time == 0) break;
//
//                process sjfP = findSJFPreemptor();
//                if (sjfP != null && sjfP.remaining_time < currentRunning.remaining_time) {
//                    updateQuantum(currentRunning, 3, qBeforeRun - totalExecutedInThisTurn);
//                    readyQueue.addLast(currentRunning);
//                    preemptor = sjfP;
//                    isPreempted = true;
//                    break;
//                }
//            }
//            ganttSegments.add(new GanttSegment(currentRunning.name, "SJF", segmentStart, currentTime));
//            if (isPreempted) { currentRunning = null; continue; }
//
//            // Quantum End Case
//            if (currentRunning != null && currentRunning.remaining_time > 0 && totalExecutedInThisTurn >= qBeforeRun) {
//                updateQuantum(currentRunning, 1, 0);
//                readyQueue.addLast(currentRunning);
//                currentRunning = null;
//            }
//        }
//    }
//
//    private void updateArrived(int time, int index) {
//        while (index < allProcesses.size() && allProcesses.get(index).arrival_time <= time) {
//            if (!readyQueue.contains(allProcesses.get(index))) {
//                readyQueue.add(allProcesses.get(index));
//            }
//            index++;
//        }
//    }
//
//    public void printResults() {
//        System.out.println("\n--- AG SCHEDULER RESULTS ---");
//        completedProcesses.sort(Comparator.comparing(p -> p.name));
//        System.out.printf("%-5s | %-5s | %-5s | %-5s | %-5s | %-5s | %-5s%n",
//                "Proc", "AT", "BT", "CT", "TAT", "WT", "Quantum History");
//        double totalWT = 0, totalTAT = 0;
//        for (process p : completedProcesses) {
//            String history = p.quantumHistory.toString();
//            System.out.printf("%-5s | %-5d | %-5d | %-5d | %-5d | %-5d| %-5s%n",
//                    p.name, p.arrival_time, p.burst_time, p.completion_time, p.turnaround_time, p.waiting_time , history);
//            totalWT += p.waiting_time;
//            totalTAT += p.turnaround_time;
//        }
//        System.out.printf("\nAverage Waiting Time: %.2f%n", totalWT / completedProcesses.size());
//        System.out.printf("Average Turnaround Time: %.2f%n", totalTAT / completedProcesses.size());
//    }
//}
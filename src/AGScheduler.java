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
            Process copy = p.copy();
            copy.quantumTimeHistory.clear(); // تأكد أنه نظيف
            copy.recordQuantum(); // سجل أول كوانتم
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
            ag.quantumHistory = p.quantumTimeHistory;
            agOuts.add(ag);
        }

        double averageWaiting = totalWT / completedProcesses.size();
        double averageTurnaround = totalTAT / completedProcesses.size();
        return new AG_output(executionOrder, agOuts, averageWaiting, averageTurnaround);
    }

    private Process selectNextProcess() {
        return readyQueue.poll();
    }

    private Process findPriorityPreemptor() {
        if (readyQueue.isEmpty()) return null;
        return readyQueue.stream()
                .min(Comparator.comparingInt(p -> p.priority))
                .orElse(null);
    }

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
                    currentRunning.computeTimes();
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

            int qBeforeRun = currentRunning.quantum;
            int totalExecutedInThisTurn = 0;
            int segmentStart = currentTime;

            // احسب مراحل التنفيذ بناءً على الـ quantum الحالي
            int fcfsTime = (int) Math.ceil(0.25 * currentRunning.quantum);
            int priorityTime = (int) Math.ceil(0.25 * currentRunning.quantum);
            int sjfTime = currentRunning.quantum - (fcfsTime + priorityTime);
            if (sjfTime < 0) sjfTime = 0;

            // FCFS Stage
            int runFCFS = Math.min(fcfsTime, currentRunning.remaining_time);
            for (int i = 0; i < runFCFS; i++) {
                currentRunning.remaining_time--;
                currentTime++;
                totalExecutedInThisTurn++;
                updateArrived(currentTime, processIndex);
                if (currentRunning.remaining_time == 0) break;
            }
            if (runFCFS > 0) {
                ganttSegments.add(new GanttSegment(currentRunning.name, "FCFS", segmentStart, currentTime));
            }
            if (currentRunning.remaining_time == 0) continue;

            // Priority Check بعد مرحلة FCFS
            Process pPre = findPriorityPreemptor();
            if (pPre != null && pPre.priority < currentRunning.priority) {
                currentRunning.quantum += (int) Math.ceil((double) (qBeforeRun - totalExecutedInThisTurn) / 2);
                currentRunning.recordQuantum();
                readyQueue.addLast(currentRunning);
                preemptor = pPre;
                currentRunning = null;
                continue;
            }

            // Priority Stage
            segmentStart = currentTime;
            int runPriority = Math.min(priorityTime, currentRunning.remaining_time);
            for (int i = 0; i < runPriority; i++) {
                currentRunning.remaining_time--;
                currentTime++;
                totalExecutedInThisTurn++;
                updateArrived(currentTime, processIndex);
                if (currentRunning.remaining_time == 0) break;
            }
            if (runPriority > 0) {
                ganttSegments.add(new GanttSegment(currentRunning.name, "Priority", segmentStart, currentTime));
            }
            if (currentRunning.remaining_time == 0) continue;

            // SJF Check بعد مرحلة Priority
            Process sPre = findSJFPreemptor();
            if (sPre != null && sPre.remaining_time < currentRunning.remaining_time) {
                currentRunning.quantum += (qBeforeRun - totalExecutedInThisTurn);
                currentRunning.recordQuantum();
                readyQueue.addLast(currentRunning);
                preemptor = sPre;
                currentRunning = null;
                continue;
            }

            // SJF Preemptive Stage
            segmentStart = currentTime;
            int runSJF = Math.min(sjfTime, currentRunning.remaining_time);
            boolean isPreempted = false;
            for (int i = 0; i < runSJF; i++) {
                currentRunning.remaining_time--;
                currentTime++;
                totalExecutedInThisTurn++;
                updateArrived(currentTime, processIndex);
                if (currentRunning.remaining_time == 0) break;

                // SJF Preemption أثناء التنفيذ
                Process sjfP = findSJFPreemptor();
                if (sjfP != null && sjfP.remaining_time < currentRunning.remaining_time) {
                    currentRunning.quantum += (qBeforeRun - totalExecutedInThisTurn);
                    currentRunning.recordQuantum();
                    readyQueue.addLast(currentRunning);
                    preemptor = sjfP;
                    isPreempted = true;
                    break;
                }
            }
            if (runSJF > 0) {
                ganttSegments.add(new GanttSegment(currentRunning.name, "SJF", segmentStart, currentTime));
            }
            if (isPreempted) {
                currentRunning = null;
                continue;
            }

            // Quantum End Case - العملية أنهت كامل الـ quantum
            if (currentRunning != null && currentRunning.remaining_time > 0 && totalExecutedInThisTurn >= qBeforeRun) {
                currentRunning.quantum += 2;
                currentRunning.recordQuantum();
                readyQueue.addLast(currentRunning);
                currentRunning = null;
            }
        }
    }

    private void updateArrived(int time, int index) {
        while (index < allProcesses.size() && allProcesses.get(index).arrival_time <= time) {
            if (!readyQueue.contains(allProcesses.get(index))) {
                readyQueue.add(allProcesses.get(index));
            }
            index++;
        }
    }

    // تمت إزالة دالة printResults() كما طلبت

    public void run() {
        runScheduler();
    }

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
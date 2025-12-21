package src;
import java.util.*;

public class AGScheduler {
    public static AG_output runAGScheduler(InputData input) {
        // نسخ العمليات
        List<Process> processes = new ArrayList<>();
        for (Process original : input.processes) {
            Process copy = new Process(original.name, original.arrival_time,
                    original.burst_time, original.priority,
                    original.quantum);
            copy.remaining_time = copy.burst_time;
            copy.arrival_time = -1;
            copy.completion_time = -1;
            processes.add(copy);
        }

        LinkedList<Process> readyQueue = new LinkedList<>();
        List<Process> completedProcesses = new ArrayList<>();
        List<String> executionOrder = new ArrayList<>();
        List<String> executionOrderDetailed = new ArrayList<>();

        // ترتيب حسب وقت الوصول
        processes.sort(Comparator.comparingInt(p -> p.arrival_time));

        int currentTime = 0;
        Process currentRunning = null;
        int processIndex = 0;
        Process preemptor = null;

        while (completedProcesses.size() < processes.size()) {
            // إضافة العمليات التي وصلت
            while (processIndex < processes.size() &&
                    processes.get(processIndex).arrival_time <= currentTime) {
                readyQueue.add(processes.get(processIndex));
                processIndex++;
            }

            // اختيار العملية التالية (معالجة Preemption)
            if (currentRunning == null || currentRunning.remaining_time == 0 || preemptor != null) {
                if (currentRunning != null && currentRunning.remaining_time == 0) {
                    currentRunning.completion_time = currentTime;
                    completedProcesses.add(currentRunning);
                    currentRunning.quantum = 0;
                    if (currentRunning.quantum_history == null) {
                        currentRunning.quantum_history = new ArrayList<>();
                    }
                    currentRunning.quantum_history.add(0);
                }

                if (preemptor != null) {
                    currentRunning = preemptor;
                    preemptor = null;
                    readyQueue.remove(currentRunning);
                } else if (readyQueue.isEmpty()) {
                    if (processIndex < processes.size()) {
                        currentTime = processes.get(processIndex).arrival_time;
                        continue;
                    } else {
                        break;
                    }
                } else {
                    currentRunning = readyQueue.poll();
                }

                if (currentRunning.arrival_time == -1) {
                    currentRunning.arrival_time = currentTime;
                }
            }

            // تطبيق منطق AG
            int[] stages = calculateStagesTimes(currentRunning.quantum);
            int fcfsTime = stages[0];
            int priorityTime = stages[1];
            int sjfTime = stages[2];
            int qBeforeRun = currentRunning.quantum;
            int totalExecuted = 0;

            // مرحلة FCFS
            if (fcfsTime > 0 && currentRunning.remaining_time > 0) {
                int runLimit = Math.min(fcfsTime, currentRunning.remaining_time);
                for (int t = 0; t < runLimit; t++) {
                    executionOrder.add(currentRunning.name);
                    executionOrderDetailed.add(currentRunning.name + "(FCFS)");
                    currentRunning.remaining_time--;
                    currentTime++;
                    totalExecuted++;

                    // تحديث ready queue مع الواصلين الجدد
                    while (processIndex < processes.size() &&
                            processes.get(processIndex).arrival_time <= currentTime) {
                        readyQueue.add(processes.get(processIndex));
                        processIndex++;
                    }

                    if (currentRunning.remaining_time == 0) break;
                }

                if (currentRunning.remaining_time == 0) continue;

                // التحقق من Preemption بواسطة Priority (Case II)
                Process priorityPreemptor = findPriorityPreemptor(readyQueue, currentRunning);
                if (priorityPreemptor != null) {
                    updateQuantumCaseII(currentRunning, qBeforeRun - totalExecuted);
                    readyQueue.addLast(currentRunning);
                    preemptor = priorityPreemptor;
                    currentRunning = null;
                    continue;
                }
            }

            // مرحلة Priority
            if (priorityTime > 0 && currentRunning != null && currentRunning.remaining_time > 0) {
                int runLimit = Math.min(priorityTime, currentRunning.remaining_time);
                for (int t = 0; t < runLimit; t++) {
                    executionOrder.add(currentRunning.name);
                    executionOrderDetailed.add(currentRunning.name + "(Priority)");
                    currentRunning.remaining_time--;
                    currentTime++;
                    totalExecuted++;

                    while (processIndex < processes.size() &&
                            processes.get(processIndex).arrival_time <= currentTime) {
                        readyQueue.add(processes.get(processIndex));
                        processIndex++;
                    }

                    if (currentRunning.remaining_time == 0) break;
                }

                if (currentRunning.remaining_time == 0) continue;

                // التحقق من Preemption بواسطة SJF (Case III)
                Process sjfPreemptor = findSJFPreemptor(readyQueue, currentRunning);
                if (sjfPreemptor != null) {
                    updateQuantumCaseIII(currentRunning, qBeforeRun - totalExecuted);
                    readyQueue.addLast(currentRunning);
                    preemptor = sjfPreemptor;
                    currentRunning = null;
                    continue;
                }
            }

            // مرحلة SJF
            if (sjfTime > 0 && currentRunning != null && currentRunning.remaining_time > 0) {
                int runLimit = Math.min(sjfTime, currentRunning.remaining_time);
                for (int t = 0; t < runLimit; t++) {
                    executionOrder.add(currentRunning.name);
                    executionOrderDetailed.add(currentRunning.name + "(SJF)");
                    currentRunning.remaining_time--;
                    currentTime++;
                    totalExecuted++;

                    while (processIndex < processes.size() &&
                            processes.get(processIndex).arrival_time <= currentTime) {
                        readyQueue.add(processes.get(processIndex));
                        processIndex++;
                    }

                    if (currentRunning.remaining_time == 0) break;

                    // Preemptive SJF Check
                    Process sjfPreemptor = findSJFPreemptor(readyQueue, currentRunning);
                    if (sjfPreemptor != null) {
                        updateQuantumCaseIII(currentRunning, qBeforeRun - totalExecuted);
                        readyQueue.addLast(currentRunning);
                        preemptor = sjfPreemptor;
                        currentRunning = null;
                        break;
                    }
                }
            }

            // Case I: Quantum Exhaustion
            if (currentRunning != null && currentRunning.remaining_time > 0 &&
                    totalExecuted == qBeforeRun) {
                updateQuantumCaseI(currentRunning);
                readyQueue.addLast(currentRunning);
                currentRunning = null;
            }
        }

        // حساب النتائج النهائية
        return buildAGOutput(processes, executionOrder);
    }

    // ============== الدوال المساعدة ==============

    private static int[] calculateStagesTimes(int quantum) {
        int fcfsTime = (int) Math.ceil(0.25 * quantum);
        int priorityTime = (int) Math.ceil(0.25 * quantum);
        int sjfTime = quantum - (fcfsTime + priorityTime);
        if (sjfTime < 0) sjfTime = 0;
        return new int[]{fcfsTime, priorityTime, sjfTime};
    }

    private static Process findPriorityPreemptor(LinkedList<Process> readyQueue, Process current) {
        if (readyQueue.isEmpty()) return null;
        return readyQueue.stream()
                .filter(p -> p.priority < current.priority)
                .min(Comparator.comparingInt(p -> p.priority))
                .orElse(null);
    }

    private static Process findSJFPreemptor(LinkedList<Process> readyQueue, Process current) {
        if (readyQueue.isEmpty()) return null;
        return readyQueue.stream()
                .filter(p -> p.remaining_time < current.remaining_time)
                .min(Comparator.comparingInt(p -> p.remaining_time))
                .orElse(null);
    }

    private static void updateQuantumCaseI(Process process) {
        process.quantum += 2;
        if (process.quantum_history == null) {
            process.quantum_history = new ArrayList<>();
        }
        process.quantum_history.add(process.quantum);
    }

    private static void updateQuantumCaseII(Process process, int remainingQ) {
        process.quantum += (int) Math.ceil((double) remainingQ / 2);
        if (process.quantum_history == null) {
            process.quantum_history = new ArrayList<>();
        }
        process.quantum_history.add(process.quantum);
    }

    private static void updateQuantumCaseIII(Process process, int remainingQ) {
        process.quantum += remainingQ;
        if (process.quantum_history == null) {
            process.quantum_history = new ArrayList<>();
        }
        process.quantum_history.add(process.quantum);
    }

    private static AG_output buildAGOutput(List<Process> processes, List<String> executionOrder) {
        List<process_AG_out> processResults = new ArrayList<>();
        double totalWT = 0;
        double totalTAT = 0;

        for (Process p : processes) {
            process_AG_out result = new process_AG_out();
            result.name = p.name;

            // حساب waiting time و turnaround time
            if (p.completion_time != -1) {
                result.turnaroundTime = p.completion_time - p.arrival_time;
                result.waitingTime = result.turnaroundTime - p.burst_time;
            } else {
                result.turnaroundTime = 0;
                result.waitingTime = 0;
            }

            // إعداد quantum history
            if (p.quantum_history != null && !p.quantum_history.isEmpty()) {
                result.quantumHistory = new ArrayList<>();
                for (Integer q : p.quantum_history) {
                    result.quantumHistory.add(q);
                }
                // إضافة 0 إذا انتهت العملية
                if (p.remaining_time == 0 && !result.quantumHistory.contains(0)) {
                    result.quantumHistory.add(0);
                }
            } else {
                result.quantumHistory = Arrays.asList(p.quantum, 0);
            }

            processResults.add(result);
            totalWT += result.waitingTime;
            totalTAT += result.turnaroundTime;
        }

        double avgWT = totalWT / processes.size();
        double avgTAT = totalTAT / processes.size();

        return new AG_output(executionOrder, processResults, avgWT, avgTAT);
    }
}
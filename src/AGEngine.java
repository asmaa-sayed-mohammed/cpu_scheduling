package src;

import java.util.*;

public class AGEngine {

    // ========= MAIN FUNCTION YOU NEED =========
    static String runAG(String input) {

        Scanner sc = new Scanner(input);
        int n = sc.nextInt();

        List<AGProcess> processes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            processes.add(new AGProcess(
                    sc.next(),
                    sc.nextInt(),
                    sc.nextInt(),
                    sc.nextInt(),
                    sc.nextInt()
            ));
        }

        simulateAG(processes);
        return buildJSON(processes);
    }

    // ========= AG SCHEDULING =========
    static void simulateAG(List<AGProcess> processes) {

        processes.sort(Comparator.comparingInt(p -> p.arrival));
        Queue<AGProcess> ready = new LinkedList<>();

        int time = 0, finished = 0, idx = 0;

        while (finished < processes.size()) {

            while (idx < processes.size() && processes.get(idx).arrival <= time) {
                ready.add(processes.get(idx++));
            }

            if (ready.isEmpty()) {
                time++;
                continue;
            }

            AGProcess p = ready.poll();
            int q = p.quantum;

            int q25 = (int) Math.ceil(q * 0.25);
            int q50 = (int) Math.ceil(q * 0.50);

            // FCFS
            int exec = Math.min(q25, p.remaining);
            p.remaining -= exec;
            time += exec;

            if (p.remaining == 0) {
                finishProcess(p, time);
                finished++;
                continue;
            }

            // Priority (non-preemptive)
            exec = Math.min(q50 - q25, p.remaining);
            p.remaining -= exec;
            time += exec;

            if (p.remaining == 0) {
                finishProcess(p, time);
                finished++;
                continue;
            }

            // SJF (preemptive)
            exec = Math.min(q - q50, p.remaining);
            p.remaining -= exec;
            time += exec;

            if (p.remaining == 0) {
                finishProcess(p, time);
                finished++;
            } else {
                p.quantum += q25;          // AG quantum increase rule
                p.quantumHistory.add(p.quantum);
                ready.add(p);
            }
        }
    }

    static void finishProcess(AGProcess p, int time) {
        p.quantum = 0;
        p.quantumHistory.add(0);
        p.completion = time;
    }

    // ========= JSON OUTPUT =========
    static String buildJSON(List<AGProcess> processes) {

        StringBuilder sb = new StringBuilder();

        if (processes.size() > 1) sb.append("[\n");

        for (int i = 0; i < processes.size(); i++) {
            AGProcess p = processes.get(i);

            int tat = p.completion - p.arrival;
            int wt = tat - p.burst;

            sb.append("  {\n");
            sb.append("    \"name\": \"").append(p.name).append("\",\n");
            sb.append("    \"waitingTime\": ").append(wt).append(",\n");
            sb.append("    \"turnaroundTime\": ").append(tat).append(",\n");
            sb.append("    \"quantumHistory\": ").append(p.quantumHistory).append("\n");
            sb.append("  }");

            if (i != processes.size() - 1) sb.append(",");
            sb.append("\n");
        }

        if (processes.size() > 1) sb.append("]");

        return sb.toString();
    }
}

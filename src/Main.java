package src;
import java.util.*;

class AGProcess {
    String name;
    int arrival, burst, remaining, priority;
    int quantum;
    int completion;
    List<Integer> quantumHistory = new ArrayList<>();

    AGProcess(String n, int a, int b, int p, int q) {
        name = n;
        arrival = a;
        burst = b;
        remaining = b;
        priority = p;
        quantum = q;
        quantumHistory.add(q);
    }
}


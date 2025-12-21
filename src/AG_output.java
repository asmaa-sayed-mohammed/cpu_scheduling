package src;

import java.util.List;

public class AG_output {
    public List<String> executionOrder;
    public List<process_AG_out> processResults;
    public double averageWaitingTime;
    public double averageTurnaroundTime;

    public AG_output(List<String> executionOrder, List<process_AG_out> processResults,
                     double averageWT, double averageTAT) {
        this.executionOrder = executionOrder;
        this.processResults = processResults;
        this.averageWaitingTime = averageWT;
        this.averageTurnaroundTime = averageTAT;
    }
}

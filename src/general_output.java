package src;

import java.util.List;

public class general_output {
    List<String> executionOrder;
    List<ProcessOutput> processResults;
    double averageWaitingTime;
    double averageTurnaroundTime;

    general_output(List<String> executionOrder, List<ProcessOutput> processResults, double averageWaitingTime, double averageTurnaroundTime){
        this.executionOrder = executionOrder;
        this.processResults = processResults;
        this.averageTurnaroundTime = averageTurnaroundTime;
        this.averageWaitingTime = averageWaitingTime;
    }
}

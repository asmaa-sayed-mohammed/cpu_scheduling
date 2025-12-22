package src;

import java.util.ArrayList;
import java.util.List;

public class actualOutputs {
    public List<expectedOutput> get_actual_output(List<InputData> inputs){
        List<expectedOutput> actual_output = new ArrayList<>();

        general_output rr_out;
        general_output sjf_out;
        general_output priority_out;
        int i = 1;

        RoundRobin rr = new RoundRobin();
        SJF sjf = new SJF();
        PriorityScheduler priority = new PriorityScheduler();

        System.out.println("RR/SJF/Priority output(ordered: test1, test2,etc...)");
        for (InputData data : inputs){
            System.out.println("Test (" + i + ")");
            rr_out = rr.schedule(data.copy());
            sjf_out = sjf.schedule(data.copy());
            priority_out = priority.schedule(data.copy());
            expectedOutput expected = new expectedOutput(rr_out, sjf_out, priority_out);
            actual_output.add(expected);
            i++;
        }
        return actual_output;
    }

    public List<AG_output> get_actual_AG_output(List<InputData> inputs){
        List<AG_output> actual_outputs = new ArrayList<>();


        System.out.println("AG output(ordered: test1, test2,etc...)");
        int i = 1;
        for (InputData data : inputs){
            System.out.println("Test (" + i + ")");
            AGScheduler ag = new AGScheduler();
             AG_output output = ag.runAG(data.copy());
             actual_outputs.add(output);
             i++;
        }
        return actual_outputs;
    }
}
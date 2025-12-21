package src;

import java.util.ArrayList;
import java.util.List;

public class actualOutputs {
    public List<expectedOutput> get_actual_output(List<InputData> inputs){
        List<expectedOutput> actual_output = new ArrayList<>();

        general_output rr_out;
        general_output sjf_out;
        general_output priority_out;

        RoundRobin rr = new RoundRobin();
        SJF sjf = new SJF();
        PriorityScheduler priority = new PriorityScheduler();

        for (InputData data : inputs){
             rr_out = rr.schedule(data.copy());
             sjf_out = sjf.schedule(data.copy());
             priority_out = priority.schedule(data.copy());
             expectedOutput expected = new expectedOutput(rr_out, sjf_out, priority_out);
             actual_output.add(expected);
        }
        return actual_output;
    }

    public List<AG_output> get_actual_AG_output(List<InputData> inputs){
        List<AG_output> actual_outputs = new ArrayList<>();
        AG_output output;

        AGScheduler ag = new AGScheduler();
        for (InputData data : inputs){
            output = AGScheduler.runAGScheduler(data);
            actual_outputs.add(output);
        }
        return actual_outputs;
    }
}

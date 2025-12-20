package src;
import java.util.*;
public class InputData {
    Integer contextSwitch;
    Integer rrQuantum;
    Integer agingInterval;
    List<Process> processes;

    public InputData copy() {
        InputData copy = new InputData();
        copy.contextSwitch = this.contextSwitch;
        copy.rrQuantum = this.rrQuantum;
        copy.agingInterval = this.agingInterval;

        copy.processes = new ArrayList<>();
        if (this.processes != null) {
            for (Process p : this.processes) {
                copy.processes.add(p.copy());
            }
        }

        return copy;
    }
}

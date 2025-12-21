package src;
import java.util.List;
import java.util.ArrayList;

public class PriorityScheduler implements Scheduler {
    List<Process> waiting_processes = new ArrayList<>();
    List<Process> ready_queue = new ArrayList<>();
    List<Process> processed = new ArrayList<>(); //for output
    List<String> execution_order = new ArrayList<>(); //for output
    int contextSwitch;
    int agingInterval;
    int currentTime = 0;
    Process running_process;

    @Override
    public general_output schedule(InputData input) {
        execution_order = new ArrayList<>();
        ready_queue = new ArrayList<>();
        processed = new ArrayList<>();
        currentTime = 0;
        contextSwitch = input.contextSwitch;
        agingInterval = input.agingInterval;
        waiting_processes = input.processes;
        running_process = null;
        boolean first_iter = true;

        while (processed.size() < waiting_processes.size()) {

            // Add newly arrived processes to ready queue
            AddInReady();
            // Apply aging to ready processes (before picking next)
            CheckForAging();
            // Pick next process based on priority
            Process next = null;
            if (!ready_queue.isEmpty()) {
                next = PickHighestPriority();
            }

            // Handle preemption / context switch
            if (next != null && next != running_process) {
                if(!first_iter)
                    HandleContextSwitch(next);
                running_process = next;
                first_iter = false;

                // only add to execution order if switching or starting
                if(running_process != null){
                    if (!execution_order.isEmpty()) {
                        String last = execution_order.get(execution_order.size() - 1);
                        if (!last.equals(next.name)) {
                            execution_order.add(next.name);
                        }
                    } else {
                        execution_order.add(next.name);
                    }

                }
            }

            // CPU idle advance time and continue
            if (running_process == null) {
                AdvanceTimeUnit();
                continue;
            }

            else{
                //  Execute 1 time unit
                Execute();
            }
            CheckProcessCompletion();

        }
        return getOutput();
    }

    void AddInReady(){
        // add new processes in each possible arrival time from waiting queue to ready queue to be processed
        for(Process p : waiting_processes){
            if((p.arrival_time <= currentTime) &&(p.remaining_time > 0) && !(processed.contains(p)) && !(ready_queue.contains(p))){
                p.readyQueueIndex = ready_queue.size();
                ready_queue.add(p);
                p.lastReadyTime = currentTime;
            }
        }
    }

    void CheckForAging(){
        // apply aging on starving processes
        for(Process p : ready_queue){
            if (p != running_process) {
                int waiting_time = currentTime - p.lastReadyTime;  //waiting = total time in system - burst time

                if(waiting_time >= agingInterval){
                    //waiting_time is increased by agingInterval ex.5, 10, 15,...
                    p.priority = (p.priority == 1)?1 : p.priority - 1;  // decrease num, increase priority, if not already == 1

                    // to not aging the following time units infinitely
                    p.lastReadyTime = currentTime;
                }


            }
        }
    }

    void AdvanceTimeUnit(){
        currentTime ++;
        // handle things change every time unit
        AddInReady();
        Process next = null;
        // Apply aging to ready processes (before picking next)
        CheckForAging();
    }

    Process PickHighestPriority(){
        //sorting ready queue by priority then the arrival time
        // we define the sort function using lambda function which return int
        // based on it sort function determine the order
        // this the calling of defined lambda function at the same time
        ready_queue.sort((p1, p2) ->{
            if(p1.priority != p2.priority)
                return p1.priority - p2.priority;

            if(p1.arrival_time != p2.arrival_time)
                return p1.arrival_time - p2.arrival_time;

            return p1.readyQueueIndex - p2.readyQueueIndex;
        });
        // pick the highest priority process
        return ready_queue.get(0);
    }



    void HandleContextSwitch(Process next_process){
        // handle context switch before executing the next process
        // the time the CPU remain idle in must be added to time counter (virtual clock)
        // the second condition is the key to know the highest priority process is changed and we must switch to process
        if((running_process != next_process)){
            // update last ready time when re-entering ready queue after preemption
            if(running_process != null && running_process.remaining_time > 0 && running_process.lastExecuted == currentTime-1)
                running_process.lastReadyTime = currentTime;

            for(int i = 0; i < contextSwitch; i++){
                AdvanceTimeUnit();
                AddInReady();
            }

            running_process = null;
        }

    }

    void Execute(){
        CheckForAging();
        Process highest = PickHighestPriority();


        if(highest != running_process){
            HandleContextSwitch(highest);
            running_process = highest;
            // only add to execution order if switching or starting
            if(running_process != null){
                if (!execution_order.isEmpty()) {
                    String last = execution_order.get(execution_order.size() - 1);
                    if (!last.equals(highest.name)) {
                        execution_order.add(highest.name);
                    }
                } else {
                    execution_order.add(highest.name);
                }

            }
        }
        else{
            // execute the highest priority process either changed or not
            running_process.remaining_time--;   // update the time by only one time before testing priority (preemptive)
            running_process.lastExecuted = currentTime;
            AdvanceTimeUnit();  // update the clock

        }

    }

    void CheckProcessCompletion(){
        // check for completion of some process
        if(running_process != null && running_process.remaining_time <= 0){
            running_process.remaining_time = 0;
            running_process.completion_time = currentTime;
            running_process.computeTimes();
            processed.add(running_process);
            ready_queue.remove(running_process);
            running_process = null;
        }

    }

    public general_output getOutput(){
        List<ProcessOutput> outputs = new ArrayList<>();
        double totalWaiting = 0;
        double totalTurnaround = 0;
        for(int i = 0; i < processed.size(); i++){
            Process p = processed.get(i);
            ProcessOutput out = new ProcessOutput();
            out.name = p.name;
            out.waitingTime = p.waiting_time;
            out.turnaroundTime = p.turnaround_time;
            totalWaiting += out.waitingTime;
            totalTurnaround += p.turnaround_time;
            outputs.add(out);

        }
        outputs.sort((o1, o2) -> o1.name.compareTo(o2.name));
        double avgWaiting = totalWaiting / outputs.size();
        double avgTurnaround = totalTurnaround / processed.size();
        double avgWaitingRounded = Math.round(avgWaiting * 100.0) / 100.0;
        double avgTurnaroundRounded = Math.round(avgTurnaround * 100.0) / 100.0;
        return new general_output(execution_order,outputs, avgWaitingRounded, avgTurnaroundRounded);
    }
}
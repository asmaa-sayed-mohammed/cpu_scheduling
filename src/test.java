//import java.util.List;
//import java.util.ArrayList;
//
//public class PriorityScheduler implements Scheduler {
//    List<process> waiting_processes = new ArrayList<>();
//    List<process> ready_queue = new ArrayList<>();
//    List<process> processed = new ArrayList<>();
//    List<String> execution_order = new ArrayList<>();
//    int contextSwitch;
//    int agingInterval;
//    int currentTime = 0;
//    process running_process;
//
//    @Override
//    public void schedule() {
//        running_process = null;
//        boolean first_iter = true;
//
//        while (processed.size() < waiting_processes.size()) {
//
//            // Add newly arrived processes to ready queue
//            AddInReady();
//            // Apply aging to ready processes (before picking next)
//            CheckForAging();
//            // Pick next process based on priority
//            process next = null;
//            if (!ready_queue.isEmpty()) {
//                next = PickHighestPriority();
//            }
//
//            // Handle preemption / context switch
//            if (next != null && next != running_process) {
//                if(!first_iter)
//                    HandleContextSwitch(next);
//                running_process = next;
//                first_iter = false;
//
//                // only add to execution order if switching or starting
//                if(running_process != null){
//                    if (!execution_order.isEmpty()) {
//                        String last = execution_order.get(execution_order.size() - 1);
//                        if (!last.equals(next.name)) {
//                            execution_order.add(next.name);
//                        }
//                    } else {
//                        execution_order.add(next.name);
//                    }
//
//                }
//            }
//
//            // CPU idle advance time and continue
//            if (running_process == null) {
//                AdvanceTimeUnit();
//                continue;
//            }
//
//            else{
//                //  Execute 1 time unit
//                Execute();
//            }
//            CheckProcessCompletion();
//
//        }
//    }
//
//    void AddInReady(){
//        // add new processes in each possible arrival time from waiting queue to ready queue to be processed
//        for(process p : waiting_processes){
//            if((p.arrival_time <= currentTime) &&(p.remaining_time > 0) && !(processed.contains(p)) && !(ready_queue.contains(p))){
//                p.readyQueueIndex = ready_queue.size();
//                ready_queue.add(p);
//                p.lastReadyTime = currentTime;
//            }
//        }
//    }
//
//    boolean CheckForAging(){
//        // apply aging on starving processes
//        boolean aged = false;
//        for(process p : ready_queue){
//            if (p != running_process) {
//                int waiting_time = currentTime - p.lastReadyTime;  //waiting = total time in system - burst time
//                System.out.println(
//                        "[AGING CHECK] t=" + currentTime +
//                                " p=" + p.name +
//                                " lastReady=" + p.lastReadyTime +
//                                " waited=" + (currentTime - p.lastReadyTime) +
//                                " prio=" + p.priority +
//                                "index= " + p.readyQueueIndex
//                );
//                if(waiting_time >= agingInterval){
//                    //waiting_time is increased by agingInterval ex.5, 10, 15,...
//                    p.priority = (p.priority == 1)?1 : p.priority - 1;  // decrease num, increase priority, if not already == 1
//                    //p.lastReadyTime = currentTime;
//
//                    // to not aging the following time units infinitely
//                    p.lastReadyTime = currentTime;
//                    aged = true;
//                }
//
//
//            }
//        }
//        return aged;
//    }
//
//    void AdvanceTimeUnit(){
//        currentTime ++;
//        // handle things change every time unit
//        AddInReady();
//        process next = null;
//        if (!ready_queue.isEmpty()) {
//            next = PickHighestPriority();
//        }
//
//        // Apply aging to ready processes (before picking next)
//        CheckForAging();
//    }
//
//    process PickHighestPriority(){
//        //sorting ready queue by priority then the arrival time
//        // we define the sort function using lambda function which return int
//        // based on it sort function determine the order
//        // if equal also in arrival time it treat both the same, any order is ok
//        // this the calling of defined lambda function at the same time
//        ready_queue.sort((p1, p2) ->{
//            if(p1.priority != p2.priority)
//                return p1.priority - p2.priority;
//            System.out.println("prio comparator: "+ready_queue.get(0).name);
//            if(p1.arrival_time != p2.arrival_time)
//                return p1.arrival_time - p2.arrival_time;
//            System.out.println("arrival comparator: "+ready_queue.get(0).name);
//            return p1.readyQueueIndex - p2.readyQueueIndex;
//        });
//        System.out.println("order comparator: "+ready_queue.get(0).name);
//
//        System.out.println();
//
//        // pick the highest priority process
//        System.out.println("final comparator result: "+ready_queue.get(0).name);
//        return ready_queue.get(0);
//    }
//
//
//
//    void HandleContextSwitch(process next_process){
//        // handle context switch before executing the next process
//        // the time the CPU remain idle in must be added to time counter (virtual clock)
//        // the second condition is the key to know the highest priority process is changed and we must switch to process
//
//        if((running_process != next_process)){
//            // update last ready time when re-entering ready queue after preemption
//            if(running_process != null && running_process.remaining_time > 0)
//                running_process.lastReadyTime = currentTime;
//
//            for(int i = 0; i < contextSwitch; i++){
//                System.out.println("t = " + currentTime+ "CS");
//                AdvanceTimeUnit();
//                AddInReady();
//            }
//
//            running_process = null;
//        }
//
//    }
//
//    void Execute(){
//
//
//        boolean aged = CheckForAging();
//        process highest = PickHighestPriority();
//
//        if(highest != running_process){
//            //System.out.println("here");
//            HandleContextSwitch(highest);
//            running_process = highest;
//            // only add to execution order if switching or starting
//            if(running_process != null){
//                if (!execution_order.isEmpty()) {
//                    String last = execution_order.get(execution_order.size() - 1);
//                    if (!last.equals(highest.name)) {
//                        execution_order.add(highest.name);
//                    }
//                } else {
//                    execution_order.add(highest.name);
//                }
//
//            }
//        }
//        else{
//            // debugging
//            System.out.println("t = " + currentTime+ "    name: "+highest.name + " priority: " + highest.priority + ", rem: " + highest.remaining_time + ", lastReady: "+highest.lastReadyTime+" , index: " + highest.readyQueueIndex + "    ");
//            System.out.println("------------------------------------------------------------------------------------------------");
//            // execute the highest priority process either changed or not
//            //running_process = next_process;
//            running_process.remaining_time--;   // update the time by only one time before testing priority (preemptive)
//
//            AdvanceTimeUnit();  // update the clock
//
//        }
//
//    }
//
//    void CheckProcessCompletion(){
//        // check for completion of some process
//        if(running_process != null && running_process.remaining_time <= 0){
//            running_process.remaining_time = 0;
//            running_process.completion_time = currentTime;
//            running_process.computeTimes();
//            processed.add(running_process);
//            ready_queue.remove(running_process);
//            running_process = null;
//        }
//
//    }
//
//    @Override
//    public void print(){
//        double totalWaiting = 0;
//        double totalTurnaround = 0;
//
//        System.out.println("\"Priority\": {");
//        System.out.print("\"executionOrder\": [");
//        for(int i = 0; i < execution_order.size(); i++){
//            System.out.print("\"" + execution_order.get(i) + "\"");
//            if(i < execution_order.size() - 1)
//                System.out.print(", ");
//        }
//        System.out.println("],");
//        System.out.println("\"processResults\": [");
//        for(int i = 0; i < processed.size(); i++){
//            process p = processed.get(i);
//            totalWaiting += p.waiting_time;
//            totalTurnaround += p.turnaround_time;
//
//            System.out.print("{\"name\": \"" + p.name + "\", "
//                    + "\"waitingTime\": " + p.waiting_time + ", "
//                    + "\"turnaroundTime\": " + p.turnaround_time + "}");
//
//            if(i < execution_order.size() - 1)
//                System.out.print(", ");
//            System.out.println();
//        }
//        System.out.println("        ],");
//        double avgWaiting = totalWaiting / processed.size();
//        double avgTurnaround = totalTurnaround / processed.size();
//
//        System.out.printf("\"averageWaitingTime\": %.1f,\n", avgWaiting);
//        System.out.printf("\"averageTurnaroundTime\": %.1f,\n", avgTurnaround);
//        System.out.println("      }");
//    }
//}
package src;

public class process {
    int time_quantum;
    int burst_time;
    int priority;
    String name;
    int remaining_time;
    int arrival_time;
    // all zeros are initial values it will be computed during the program
    int waiting_time = 0;
    int turnaround_time = 0; // you will use it to compute waiting time
    int completion_time = 0; // you will use it to compute turnaround time

    process(String name, int burst_time, int priority, int time_quantum, int arrival_time){
        this.name = name;
        this.burst_time = burst_time;
        this.priority = priority;
        this.time_quantum = time_quantum;
        this.arrival_time = arrival_time;
        this.remaining_time = burst_time; //initial value you will use it to compute the completion time
    }

}

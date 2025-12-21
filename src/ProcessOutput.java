package src;
import java.util.*;


public class ProcessOutput {
    String name;
    double waitingTime;
    double turnaroundTime;
    Integer quantumTime;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProcessOutput other = (ProcessOutput) obj;
        return waitingTime == other.waitingTime &&
                turnaroundTime == other.turnaroundTime &&
                name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, waitingTime, turnaroundTime);
    }
}

package src;
import java.util.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

public class ProcessOutput {
    String name;
    int waitingTime;
    int turnaroundTime;
    Integer quantumTime;

    public static ProcessOutput fromJson(JSONObject obj){
        ProcessOutput output = new ProcessOutput();
        output.name = (String) obj.get("name");
        output.waitingTime = ((Long) obj.get("waitingTime")).intValue();
        output.turnaroundTime = ((Long) obj.get("turnaroundTime")).intValue();
        output.quantumTime = (obj.containsKey("quantumTime")) ? ((Long) obj.get("quantumTime")).intValue()
                : null;
        return output;
    }
}

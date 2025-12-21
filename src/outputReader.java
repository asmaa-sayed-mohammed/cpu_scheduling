package src;
import java.util.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

public class outputReader {
    static AG_output get_AG_output(JSONObject root){
        // جرب الحالة الأولى: إذا كان root يحتوي على expectedOutput
        if (root.containsKey("expectedOutput")) {
            JSONObject output = (JSONObject) root.get("expectedOutput");
            return readAGAlgorithmOutput(output);
        }
        // الحالة الثانية: إذا كان root هو نفسه expectedOutput
        else if (root.containsKey("processResults")) {
            return readAGAlgorithmOutput(root);
        }
        // إذا لم تكن أي من الحالتين
        else {
            throw new RuntimeException("Cannot find AG output structure. " +
                    "Available keys: " + root.keySet());
        }
    }

    static expectedOutput read_output(JSONObject root){

        JSONObject output = (JSONObject) root.get("expectedOutput");

        general_output sjf = output.containsKey("SJF")
                ? readAlgorithmOutput((JSONObject) output.get("SJF"))
                : null;

        general_output rr = output.containsKey("RR")
                ? readAlgorithmOutput((JSONObject) output.get("RR"))
                : null;

        general_output priority = output.containsKey("Priority")
                ? readAlgorithmOutput((JSONObject) output.get("Priority"))
                : null;

        return new expectedOutput(rr, sjf, priority);
    }

    static general_output readAlgorithmOutput(JSONObject obj){

        List<String> executionOrder =
                (List<String>) obj.get("executionOrder");

        JSONArray arr = (JSONArray) obj.get("processResults");
        List<ProcessOutput> results = new ArrayList<>();

        for (Object o : arr){
            JSONObject p = (JSONObject) o;
            ProcessOutput out = new ProcessOutput();
            out.name = (String) p.get("name");
            out.waitingTime = ((Long) p.get("waitingTime")).doubleValue();
            out.turnaroundTime = ((Long) p.get("turnaroundTime")).doubleValue();
            results.add(out);
        }

        double avgWT = ((Number) obj.get("averageWaitingTime")).doubleValue();
        double avgTAT = ((Number) obj.get("averageTurnaroundTime")).doubleValue();

        return new general_output(executionOrder, results, avgWT, avgTAT);
    }

    static AG_output readAGAlgorithmOutput(JSONObject obj){
        if (obj == null) throw new RuntimeException("JSON object for AG output is null");

        // تحويل executionOrder
        JSONArray orderArray = (JSONArray) obj.get("executionOrder");
        List<String> executionOrder = new ArrayList<>();
        if (orderArray != null) {
            for (Object o : orderArray) {
                executionOrder.add((String) o);
            }
        }
        JSONArray arr = (JSONArray) obj.get("processResults");
        if (arr == null) {
            throw new RuntimeException("processResults is missing in AG output JSON");
        }


        List<process_AG_out> results = new ArrayList<>();
        for (Object o : arr) {
            JSONObject p = (JSONObject) o;
            process_AG_out out = new process_AG_out();
            out.name = (String) p.get("name");
            out.waitingTime = ((Number) p.get("waitingTime")).doubleValue();
            out.turnaroundTime = ((Number) p.get("turnaroundTime")).doubleValue();
            out.quantumHistory = ((List<Integer>) p.get("quantumHistory"));
            results.add(out);
        }

        double avgWT = ((Number) obj.get("averageWaitingTime")).doubleValue();
        double avgTAT = ((Number) obj.get("averageTurnaroundTime")).doubleValue();

        return new AG_output(executionOrder, results, avgWT, avgTAT);
    }

}

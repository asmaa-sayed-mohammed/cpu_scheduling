package src;
import java.util.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
class InputReader {

    static InputData read(JSONObject root) {

        JSONObject input = (JSONObject) root.get("input");

        InputData data = new InputData();
        data.contextSwitch = input.containsKey("contextSwitch") ? ((Long) input.get("contextSwitch")).intValue()
                : null;

        data.rrQuantum = input.containsKey("rrQuantum") ? ((Long) input.get("rrQuantum")).intValue()
                : null;

        data.agingInterval = input.containsKey("agingInterval") ? ((Long) input.get("agingInterval")).intValue()
                : null;

        JSONArray arr = (JSONArray) input.get("processes");
        data.processes = new ArrayList<>();

        for (Object o : arr) {
            data.processes.add(Process.createAgProcess((JSONObject) o));
        }

        return data;
    }
}

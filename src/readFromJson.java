package src;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class readFromJson {

    public File[] getFiles() {
        File directory = new File("libs/test_cases/Other_Schedulers");
        return directory.listFiles((d, name) -> name.endsWith(".json"));
    }

    public File[] get_AG_files() {
        File directory = new File("libs/test_cases/AG");
        return directory.listFiles((d, name) -> name.endsWith(".json"));
    }

    public List<InputData> getInput(File[] files) throws IOException, ParseException {
        List<InputData> input_json = new ArrayList<>();
        JSONParser parser = new JSONParser();
        if (files == null) return null;

        for (File f : files) {
            JSONObject root = (JSONObject) parser.parse(new FileReader(f));
            InputData input = InputReader.read(root);
            input_json.add(input);
        }
        return input_json;
    }

    public List<InputData> get_AG_inputs(File[] files) throws IOException, ParseException {
        List<InputData> input_AG_json = new ArrayList<>();
        JSONParser parser = new JSONParser();
        if (files == null) return null;

        for (File f : files) {
            JSONObject root = (JSONObject) parser.parse(new FileReader(f));
            InputData input = InputReader.read(root);
            input_AG_json.add(input.copy());
        }
        return input_AG_json;
    }

    public List<expectedOutput> getOutput(File[] files) throws IOException, ParseException {
        List<expectedOutput> output_json = new ArrayList<>();
        JSONParser parser = new JSONParser();
        if (files == null) return null;

        for (File f : files) {
            JSONObject root = (JSONObject) parser.parse(new FileReader(f));
            expectedOutput out = outputReader.read_output(root);
            output_json.add(out);
        }
        return output_json;
    }

    public List<AG_output> get_AG_output_json(File[] files) throws IOException, ParseException {
        List<AG_output> AG_output_json = new ArrayList<>();
        JSONParser parser = new JSONParser();
        if (files == null || files.length == 0) return AG_output_json;

        for (File f : files) {
            JSONObject root = (JSONObject) parser.parse(
                    new String(java.nio.file.Files.readAllBytes(f.toPath()))
            );

            JSONObject target = findProcessResults(root);
            if (target == null) {
                throw new RuntimeException("processResults not found in file: " + f.getName());
            }

            AG_output ag = outputReader.readAGAlgorithmOutput(target);
            AG_output_json.add(ag);
        }

        return AG_output_json;
    }

    private JSONObject findProcessResults(JSONObject obj) {
        if (obj == null) return null;

        if (obj.containsKey("processResults")) {
            return obj;
        }

        for (Object key : obj.keySet()) {
            Object value = obj.get(key);
            if (value instanceof JSONObject) {
                JSONObject result = findProcessResults((JSONObject) value);
                if (result != null) return result;
            }
        }
        return null;
    }
}

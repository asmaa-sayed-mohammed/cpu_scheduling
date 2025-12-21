package src;

import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException, ParseException {
        readFromJson read = new readFromJson();
//        actualOutputs actual = new actualOutputs();

        File[] otherSchedulerFiles = read.getFiles();
        assert otherSchedulerFiles != null && otherSchedulerFiles.length > 0 : "No Other_Schedulers JSON files found!";

        File[] agFiles = read.get_AG_files();
        assert agFiles != null && agFiles.length > 0 : "No AG JSON files found!";

        List<InputData> inputs = read.getInput(otherSchedulerFiles);
        assert inputs != null && !inputs.isEmpty() : "No input data read from Other_Schedulers files!";

        List<InputData> AG_input = read.get_AG_inputs(agFiles);
        assert AG_input != null && !AG_input.isEmpty() : "No input data read from AG files!";

        List<expectedOutput> expected_outputs = read.getOutput(otherSchedulerFiles);
        assert expected_outputs != null && !expected_outputs.isEmpty() : "No expected output data read from Other_Schedulers files!";

        List<AG_output> expected_AG_outputs = read.get_AG_output_json(agFiles);
        assert expected_AG_outputs != null && !expected_AG_outputs.isEmpty() : "No expected output data read from AG files!";

        actualOutputs actual = new actualOutputs();
        List<expectedOutput> actual_outputs = actual.get_actual_output(inputs);
        List<AG_output> actual_AG_output = actual.get_actual_AG_output(AG_input);
        assert actual_outputs != null && !actual_outputs.isEmpty() : "No actual output generated for Other_Schedulers!";
        assert actual_AG_output != null && !actual_AG_output.isEmpty() : "No actual AG output generated!";

        List<String> failedOtherSchedulerTests = new ArrayList<>();
        List<String> failedAGTests = new ArrayList<>();

        System.out.println("Checking RR, SJF, Priority test cases...");
        System.out.println("Number of test cases: " + otherSchedulerFiles.length);

        for (int i = 0; i < otherSchedulerFiles.length; i++) {
            String fileName = otherSchedulerFiles[i].getName();
            expectedOutput expected = expected_outputs.get(i);
            expectedOutput actualOut = actual_outputs.get(i);

            try {
                assert compareResult(actualOut, expected) :
                        "Test case failed for RR/SJF/Priority: " + fileName;
                System.out.println("Testing: " + fileName + " - PASSED");
            } catch (AssertionError e) {
                System.out.println("Testing: " + fileName + " - FAILED");
                System.err.println("  " + e.getMessage());
                failedOtherSchedulerTests.add(fileName);
            }
        }

        System.out.println("\nChecking AG test cases...");
        System.out.println("Number of AG test cases: " + agFiles.length);

        for (int i = 0; i < agFiles.length; i++) {
            String fileName = agFiles[i].getName();
            AG_output expected = expected_AG_outputs.get(i);
            AG_output actualOut = actual_AG_output.get(i);

            try {
                assert compare_AG_output(actualOut, expected) :
                        "Test case failed for AG: " + fileName;
                System.out.println("Testing: " + fileName + " - PASSED");
            } catch (AssertionError e) {
                System.out.println("Testing: " + fileName + " - FAILED");
                System.err.println("  " + e.getMessage());
                failedAGTests.add(fileName);
            }
        }

        System.out.println("\n=== SUMMARY ===");
        if (failedOtherSchedulerTests.isEmpty()) {
            System.out.println("All RR/SJF/Priority tests passed!");
        } else {
            System.out.println("Failed RR/SJF/Priority tests: " + failedOtherSchedulerTests);
        }

        if (failedAGTests.isEmpty()) {
            System.out.println("All AG tests passed!");
        } else {
            System.out.println("Failed AG tests: " + failedAGTests);
        }

        if (!failedOtherSchedulerTests.isEmpty() || !failedAGTests.isEmpty()) {
            System.exit(1);
        }
    }

    public static boolean compareResult(expectedOutput actual, expectedOutput expected) {
        return compareGeneralOutput(actual.RR, expected.RR, "RR") &&
                compareGeneralOutput(actual.SJF, expected.SJF, "SJF") &&
                compareGeneralOutput(actual.Priority, expected.Priority, "Priority");
    }

    public static boolean compareGeneralOutput(general_output actual, general_output expected, String schedule) {
        assert actual != null && expected != null : "[" + schedule + "] One of the outputs is null!";

        assert actual.executionOrder.equals(expected.executionOrder) :
                "[" + schedule + "] Execution Order mismatch: Actual=" + actual.executionOrder + ", Expected=" + expected.executionOrder;

        assert actual.processResults.equals(expected.processResults) :
                "[" + schedule + "] Process Results mismatch: Actual=" + actual.processResults + ", Expected=" + expected.processResults;

        assert Math.abs(actual.averageWaitingTime - expected.averageWaitingTime) < 1e-6 :
                "[" + schedule + "] Average Waiting Time mismatch: Actual=" + actual.averageWaitingTime + ", Expected=" + expected.averageWaitingTime;

        assert Math.abs(actual.averageTurnaroundTime - expected.averageTurnaroundTime) < 1e-6 :
                "[" + schedule + "] Average Turnaround Time mismatch: Actual=" + actual.averageTurnaroundTime + ", Expected=" + expected.averageTurnaroundTime;

        return true;
    }

    public static boolean compare_AG_output(AG_output actual, AG_output expected) {
        assert actual != null && expected != null : "[AG schedule] One of the outputs is null!";

        assert actual.executionOrder.equals(expected.executionOrder) :
                "[AG schedule] Execution Order mismatch: Actual=" + actual.executionOrder + ", Expected=" + expected.executionOrder;

        assert actual.processResults.equals(expected.processResults) :
                "[AG schedule] Process Results mismatch: Actual=" + actual.processResults + ", Expected=" + expected.processResults;

        assert Math.abs(actual.averageWaitingTime - expected.averageWaitingTime) < 1e-6 :
                "[AG schedule] Average Waiting Time mismatch: Actual=" + actual.averageWaitingTime + ", Expected=" + expected.averageWaitingTime;

        assert Math.abs(actual.averageTurnaroundTime - expected.averageTurnaroundTime) < 1e-6 :
                "[AG schedule] Average Turnaround Time mismatch: Actual=" + actual.averageTurnaroundTime + ", Expected=" + expected.averageTurnaroundTime;

        return true;
    }
}
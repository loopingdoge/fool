package main;


import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

public class TestComplete {

    public static void runTestSuite(String filename) {
        Yaml yaml = new Yaml();

        try {
            InputStream is = new FileInputStream(new File(filename));
            Map<String, ArrayList<String>> tests = (Map<String, ArrayList<String>>) yaml.load(is);

            int passed = 0;

            for (String testID : tests.keySet()) {

                ArrayList<String> test = tests.get(testID);
                String code = String.valueOf(test.get(0));
                String result = String.valueOf(test.get(1));

                System.out.println(FoolRunner.ANSI_BLUE + "Executing: " + testID + FoolRunner.ANSI_RESET);
                CharStream input = CharStreams.fromString(code);
                String output = FoolRunner.test(testID, input, result, false, false);

                if(output.endsWith( "Test PASSED!" )) passed++;

                System.out.println(output + FoolRunner.ANSI_RESET + "\n");
            }
            String color = (passed == tests.keySet().size()) ? FoolRunner.ANSI_GREEN : FoolRunner.ANSI_RED;
            System.out.println(FoolRunner.ANSI_GREEN + "TESTING COMPLETED. TESTS PASSED " + color + passed + FoolRunner.ANSI_GREEN + "/" + tests.keySet().size() + FoolRunner.ANSI_RESET);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        runTestSuite("test.yml");
    }
}

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

    public static void main(String[] args) throws Exception {

        final String fileName = "test.yml";
        Yaml yaml = new Yaml();

        try {
            InputStream is = new FileInputStream(new File(fileName));
            Map<String, ArrayList<String>> tests = (Map<String, ArrayList<String>>) yaml.load(is);

            for (String testID : tests.keySet()) {

                ArrayList<String> test = tests.get(testID);
                String code = String.valueOf(test.get(0));
                String result = String.valueOf(test.get(1));

                System.out.println("Executing: " + testID);
                CharStream input = CharStreams.fromString(code);
                String output = TestRunner.test(testID, input, result, false);
                System.out.println(output);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

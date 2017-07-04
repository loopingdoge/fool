package main;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.apache.commons.cli.*;

public class Main {

    private static void help(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        System.err.println();
        formatter.printHelp("fool", options);
        System.exit(0);
    }

    public static void main(String[] args) throws Exception {
        Option help = Option.builder("h").longOpt("help").desc("print this message").build();
        Option inputFile = Option.builder("i").longOpt("input").hasArg().argName("filepath").desc("REQUIRED input .fool file").build();
        Option debug = Option.builder("d").longOpt("debug").desc("show debug logs").build();
        Option ast = Option.builder("a").longOpt("ast").desc("show the AST").build();
        Option svm = Option.builder("s").longOpt("svm").hasArg().argName("filepath").desc("outputs the generated SVM code to the given file").build();
        Option bytecode = Option.builder("b").longOpt("bytecode").hasArg().argName("filepath").desc("outputs the generated bytecode to the given file").build();
        Option test = Option.builder("t").longOpt("test").desc("the input file is treatead as a .yml test suite file").build();
        Option noColors = Option.builder("c").longOpt("no-colors").desc("disable the output colors").build();

        Options options = new Options();
        options.addOption(help)
                .addOption(inputFile)
                .addOption(debug)
                .addOption(ast)
                .addOption(svm)
                .addOption(bytecode)
                .addOption(test)
                .addOption(noColors);

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("help")) {
                help(options);
            }

            if (!line.hasOption("input")) {
                System.err.println("Error: missing -i <filepath> option");
                help(options);
            }

            String inputFilename = line.getOptionValue("input");
            String svmFilename = inputFilename.substring(0, inputFilename.lastIndexOf(".")) + ".svm";
            String bytecodeFilename = "";

            if (line.hasOption("svm")) {
                svmFilename = line.getOptionValue("svm");
            }

            if (line.hasOption("bytecode")) {
                bytecodeFilename = line.getOptionValue("bytecode");
            }

            if (line.hasOption("test")) {
                TestComplete.runTestSuite(line.getOptionValue("input"), line.hasOption("no-colors"));
            } else {
                try {
                    CharStream input = CharStreams.fromFileName(inputFilename);
                    String result = FoolRunner.run(
                            input,
                            svmFilename,
                            bytecodeFilename,
                            line.hasOption("svm"),
                            line.hasOption("debug"),
                            line.hasOption("ast"),
                            line.hasOption("no-colors")
                    );
                    System.out.println("Result: " + result);
                } catch (Exception e) {
                    System.err.println("Empty input file.");
                }
            }
        } catch (ParseException e) {
            System.err.println("Error: " + e.getMessage());
            help(options);
        }
    }

}

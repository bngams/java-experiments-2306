package fr.aelion.java2306.usecase;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Stream;

public class Main {

    static PrintWriter writer;
    static String outputPath = "";

    public static void main(String[] args) {

        String pathFolder = "src/main/resources";
        Integer numberOfFiles = 3;
        Integer numberOfLines = 10000;
        String outputFilename = "output.txt";

        try {
            outputPath = pathFolder+"/"+outputFilename;
            writer = new PrintWriter(outputPath, "UTF-8");
            createFiles(pathFolder,numberOfFiles, numberOfLines, true);
            // handleFiles(pathFolder);
            handleFilesExecutors(pathFolder);
            writer.close();
        } catch (RuntimeException | IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    private static void handleFiles(String pathFolder) throws IOException, RuntimeException {
        // type inference
        // var walk = Files.walk(Paths.get(pathFolder));
        Files.list(Paths.get(pathFolder))
            .filter(Files::isRegularFile)
            .flatMap(Main::getLines)
            .filter(Main::filterEvenLines)
            .parallel()
            .forEach(Main::writeLine);

    }

    private static void handleFilesExecutors(String pathFolder) {
        ExecutorService es = Executors.newWorkStealingPool();
        try {
            Files.list(Paths.get(pathFolder)).forEach(path -> {
                es.submit(() -> { Main.getLines(path).forEach(Main::writeLine); });
            });
            es.shutdown();
            es.awaitTermination(20, TimeUnit.SECONDS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    private static boolean filterEvenLines(String line) {
        return true;
    }

    private static void writeLine(String line) {
        try {
            System.out.println(Thread.currentThread());
            Files.writeString(Paths.get(outputPath), line, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeLineWithPrinter(String line) {
        writer.println(line);
    }

    private static Stream<String> getLines(Path path) throws RuntimeException {
        try {
            System.out.println(Thread.currentThread().getName() + " - file : " + path.getFileName());
            return Files.lines(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void createFiles(
            String pathFolder,
            Integer numberOfFiles,
            Integer numberOfLines,
            Boolean reset) throws IOException {
        if(reset) {
            for (int i = 0; i < numberOfFiles; i++) {
                createFile(pathFolder, "File" + i, numberOfLines);
            }
        }
    }

    static void createFile(String pathFolder, String fileName, Integer numberOfLines) throws IOException {
        String inputPath = pathFolder + "/" + fileName;
        PrintWriter myWriter = new PrintWriter(inputPath);
        for (int i = 0; i < numberOfLines ; i++) {
            myWriter.println(fileName + "- line #" + i);
        }
        myWriter.close();
        System.out.println("Successfully wrote to the file.");
    }

}

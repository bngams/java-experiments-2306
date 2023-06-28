package fr.aelion.java2306.usecase;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) {

        String pathFolder = "src/main/resources";
        Integer numberOfFiles = 3;
        Integer numberOfLines = 10;


        try {
            createFiles(pathFolder,numberOfFiles, numberOfLines, false);
            handleFiles(pathFolder, numberOfFiles);
        } catch (RuntimeException | IOException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    private static void handleFiles(String pathFolder, Integer numberOfFiles) throws IOException, RuntimeException {
        // type inference
        // var walk = Files.walk(Paths.get(pathFolder));
        Files.walk(Paths.get(pathFolder))
                .parallel()
                .peek(path -> System.out.println(Thread.currentThread() + " - file " + path.getFileName()))
                .filter(Files::isRegularFile)
                .flatMap(path -> {
                    try {
                        return Files.lines(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .parallel()
                .peek(line -> System.out.println(Thread.currentThread() + " - line " + line))
                .forEach(System.out::println);
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

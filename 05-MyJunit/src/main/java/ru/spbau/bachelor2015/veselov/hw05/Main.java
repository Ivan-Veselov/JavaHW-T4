package ru.spbau.bachelor2015.veselov.hw05;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import ru.spbau.bachelor2015.veselov.hw05.exceptions.AfterClassStageFailedException;
import ru.spbau.bachelor2015.veselov.hw05.exceptions.BeforeClassStageFailedException;
import ru.spbau.bachelor2015.veselov.hw05.exceptions.InvalidTestClassException;
import ru.spbau.bachelor2015.veselov.hw05.reports.TestReport;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.FileVisitResult.CONTINUE;

/**
 * Main class which represents a console application.
 */
public class Main {
    /**
     * Entry point of a programme. This method expects one argument - path to a directory with .java files. Each .java
     * file is considered to be a test class and thus programme tries to test all such classes. All tested classes must
     * be in CLASSPATH.
     *
     * @param args one argument expected - path to a root folder with .java files inside.
     * @throws IOException if any IO exception occurs during walk through folder with .java files.
     */
    public static void main(final @NotNull String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("One argument expected");
            return;
        }

        Path pathToFolder = Paths.get(args[0]);
        if (pathToFolder == null) {
            System.out.println("Invalid path");
            return;
        }

        ClassesCollector classesCollector = new ClassesCollector(pathToFolder);
        Files.walkFileTree(pathToFolder, classesCollector);

        for (Class<?> testClass : classesCollector.getTestClasses()) {
            runClassTesting(testClass);
        }
    }

    private static void runClassTesting(final @NotNull Class<?> testClass) {
        try {
            for (TestReport report : new Tester(testClass).test()) {
                System.out.println(report.report());
            }
        } catch (InvalidTestClassException e) {
            System.out.println("Invalid test class: " + testClass.getName());
        } catch (BeforeClassStageFailedException e) {
            System.out.println("Before class stage failed: " + testClass.getName());
        } catch (AfterClassStageFailedException e) {
            System.out.println("After class stage failed: " + testClass.getName());
        }
    }

    private static class ClassesCollector extends SimpleFileVisitor<Path> {
        private static final @NotNull String extension = "java";

        private final @NotNull Path root;

        private final @NotNull List<Class<?>> testClasses;

        public ClassesCollector(final @NotNull Path root) {
            this.root = root;

            testClasses = new ArrayList<>();
        }

        @Override
        public FileVisitResult visitFile(final @NotNull Path path, final @NotNull BasicFileAttributes attrs) {
            if (FilenameUtils.getExtension(path.toString()).equals(extension)) {
                testClasses.add(getClassFromPath(root.relativize(path)));
            }

            return CONTINUE;
        }

        public @NotNull List<Class<?>> getTestClasses() {
            return new ArrayList<>(testClasses);
        }

        private @NotNull Class<?> getClassFromPath(final @NotNull Path path) {
            StringBuilder classNameBuilder = new StringBuilder();
            for (Path name : path.getParent()) {
                classNameBuilder.append(name.toString());
                classNameBuilder.append(".");
            }

            classNameBuilder.append(FilenameUtils.getBaseName(path.getFileName().toString()));

            try {
                return Class.forName(classNameBuilder.toString());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

package org.achacha.gelkis;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.stream.Collectors.toMap;

/**
 * Collect jars
 */
public class JarCollector {
    /**
     * Global logger available when class specific is not needed
     */
    public static final Logger LOGGER = Logger.getLogger(DetectDuplicatesInJarsMain.class.getName());

    /*
    Collection of processed jar files
    */
    private Map<Path, JarBreaker> classes = new HashMap<>();

    /*
    List of jar base names to ignore
     */
    private List<String> ignoreJarBasename = new ArrayList<>();

    /*
     * Collection of jar name to path (there may be more that one location for same jar
     */
    private Map<String, ArrayList<Path>> jarNameToFilenames = new HashMap<>();

    public Map<String,Collection<String>> detectDuplicateClasses() {
        // Iterate over all jars and collect classes while checking for duplicates
        Map<String,ArrayList<String>> classToJar = new HashMap<>();
        classes.forEach((key, value) -> value.getClasses()
                .forEach(c -> {
                    ArrayList<String> jars = classToJar.get(c);
                    if (jars == null) {
                        jars = new ArrayList<>();
                        jars.add(0, value.getJarPath());
                        classToJar.put(c, jars);
                    } else {
                        jars.add(key.toString());
                    }
                }));

        // classToJar should be a map of class to a collection of jars, find all occurring more that once
        return classToJar.entrySet().stream()
                .filter(e->e.getValue().size()>1)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Add a base filename to ignore
     * e.g. "batik-", "wstx-"
     * @param basename String
     */
    public void addIgnoreBasename(String basename) {
        ignoreJarBasename.add(basename);
    }

    private class JarVisitor implements FileVisitor<Path> {
        private final List<String> ignoreBasename;

        JarVisitor(List<String> ignoreBasename) {
            this.ignoreBasename = ignoreBasename;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            // Process only jars that are not ignored
            if (file.toString().toLowerCase().endsWith(".jar")) {
                String filename = file.getFileName().toString();
                if (ignoreBasename.stream().anyMatch(filename::startsWith)) {
                    LOGGER.info("Ignoring JAR basename: "+file);
                }
                else {
                    LOGGER.info("Processing " + file);
                    if (!isDuplicateJar(file))
                        classes.put(file, new JarBreaker((file)));
                }
            }
            else {
                LOGGER.info("Skipping " + file);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            return FileVisitResult.CONTINUE;
        }
    }

    /**
     * Check if jar file at given path already exists
     * Duplicates may exist at different paths
     *
     * @param path for jar
     * @return true if already processed
     */
    private boolean isDuplicateJar(Path path) {
        String filename = path.getFileName().toString();
        ArrayList<Path> jarPaths = jarNameToFilenames.computeIfAbsent(filename, k -> new ArrayList<>());
        jarPaths.add(path);
        return jarPaths.size() > 1;
    }

    private JarVisitor visitor = new JarVisitor(ignoreJarBasename);

    public void gatherAt(Path rootClassPath) {
        try {
            Files.walkFileTree(rootClassPath, visitor);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to walk directory tree looking for jars", e);
        }
    }

    public Map<String, ArrayList<Path>> getJarNameToFilenames() {
        return jarNameToFilenames;
    }

    public Map<Path, JarBreaker> getClasses() {
        return classes;
    }
}

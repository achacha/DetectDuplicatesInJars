package org.achacha.gelkis;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

/**
 * Collect jars
 */
public class JarCollector {
    /**
     * Global logger available when class specific is not needed
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(JarCollector.class);

    /*
    Collection of processed jar files
    */
    private Map<Path, JarShmenge> classes = new HashMap<>();

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

    private class JarVisitor implements FileVisitor<Path> {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (StringUtils.endsWithIgnoreCase(file.toString(), ".jar")) {
                LOGGER.info("Processing {}", file);
                classes.put(file, new JarShmenge((file)));
            }
            else {
                LOGGER.info("Skipping {}", file);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            return FileVisitResult.CONTINUE;
        }
    }

    private JarVisitor visitor = new JarVisitor();

    public void gatherAt(Path rootClassPath) {
        try {
            Files.walkFileTree(rootClassPath, visitor);
        } catch (IOException e) {
            LOGGER.error("Failed to walk directory tree looking for jars", e);
        }
    }

    public Map<Path, JarShmenge> getClasses() {
        return classes;
    }
}

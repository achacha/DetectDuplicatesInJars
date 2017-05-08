package org.achacha.gelkis;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

/**
 * Load all class files
 */
public class ClassCollector {
    private Map<Path, String> classes = new HashMap<>();

    private class ClassVisitor implements FileVisitor<Path> {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            classes.put(file, "1");
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

    private ClassVisitor visitor = new ClassVisitor();

    public void gatherAt(Path rootClassPath) {
        try {
            Files.walkFileTree(rootClassPath, visitor);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<Path, String> getClasses() {
        return classes;
    }
}

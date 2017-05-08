package org.achacha.gelkis;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;

/**
 * Given a jar file, process the contents
 * Yosh Shmengi was not the most elegant and broke many jars
 */
public class JarShmenge {
    private final String jarPath;
    private Set<String> classes = new HashSet<>();

    /**
     * Convert Enumeration into stream
     * Will go away in Java 9 when Enumeration::asIterator is available
     */
    public static <T> Stream<T> enumerationAsStream(Enumeration<T> e) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        new Iterator<T>() {
                            public T next() {
                                return e.nextElement();
                            }
                            public boolean hasNext() {
                                return e.hasMoreElements();
                            }
                        },
                        Spliterator.ORDERED), false);
    }

    public JarShmenge(Path jarFilePath) {
        this.jarPath = jarFilePath.toString();
        JarCollector.LOGGER.info("Processing JAR: {}", jarFilePath.toAbsolutePath());
        // Iterate over all entries and extract class names
        try (JarFile jar = new JarFile(jarFilePath.toFile())) {
            enumerationAsStream(jar.entries())
                    .map(ZipEntry::toString)
                    .filter(e -> e.endsWith(".class"))
                    .forEach(e -> {
                        classes.add(e);
                        JarCollector.LOGGER.debug("  {}", e);
                    });
        }
        catch(IOException e) {
            JarCollector.LOGGER.error("Failed to process jar", e);
        }
    }

    @Override
    public String toString() {
        return "JavaShmenge{" +
                "jarPath=" + jarPath +
                "classes.size=" + classes.size() +
                '}';
    }

    public String getJarPath() {
        return jarPath;
    }

    public Set<String> getClasses() {
        return classes;
    }
}

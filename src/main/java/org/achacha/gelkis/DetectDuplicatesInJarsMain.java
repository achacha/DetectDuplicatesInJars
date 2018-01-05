package org.achacha.gelkis;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Main entry point
 */
public class DetectDuplicatesInJarsMain {

    private static final Logger LOGGER = JarCollector.LOGGER;

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Scan all directories and gather all jars into one big list and look for duplicate classes");
            System.out.println("Usage: [this app] <Directory where to scan for jars> <Another directory to add to the big list of jars> ...");
            return;
        }

        // Scan all directories into one lump collection
        JarCollector jarCollector = new JarCollector();
        jarCollector.addIgnoreBasename("woodstox-");
        jarCollector.addIgnoreBasename("wstx-asl-");
        jarCollector.addIgnoreBasename("batik-");
        jarCollector.addIgnoreBasename("mail-");
        jarCollector.addIgnoreBasename("axis2-");
        jarCollector.addIgnoreBasename("javax.inject-");
        jarCollector.addIgnoreBasename("tomcat-util-");

        for (String pathToTest : args) {
            LOGGER.info("Checking directory: " + pathToTest);
            jarCollector.gatherAt(Paths.get(pathToTest));
            LOGGER.info("jars collected = " + jarCollector.getClasses().size());

            Optional<Integer> count = jarCollector.getClasses().values()
                    .stream()
                    .map(i -> i.getClasses().size())
                    .reduce((x, y) -> x + y);
            if (count.isPresent()) {
                LOGGER.info("classes detected count = " + count.get());
            } else {
                LOGGER.info("No classes detected.");
            }
        }

        // List all jars
        LOGGER.info("\n\nJars detected\n---\n");
        jarCollector.getJarNameToFilenames().values().forEach(jars ->
                LOGGER.info(
                    jars.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining("\n"))
                )
        );

        // Detect duplicates
        LOGGER.info("\n\nDetecting duplicates\n---\n");
        Map<String, Collection<String>> duplicates = jarCollector.detectDuplicateClasses();
        if (duplicates.size() > 0) {
            LOGGER.info("Duplicates detected\n---\n");
            duplicates.forEach((key, value) -> LOGGER.info(
                    key + "\n\t" + value.stream().collect(Collectors.joining("\n\t")))
            );
            System.exit(-1);
        } else
            LOGGER.info("No duplicates detected.");
    }
}

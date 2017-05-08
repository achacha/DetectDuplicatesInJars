package org.achacha.gelkis;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Main entry point
 */
public class DetectDuplicatesInJarsMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(DetectDuplicatesInJarsMain.class);

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: [this app] <Directory where to scan jars>");
            return;
        }

        String pathToTest = args[0];
        LOGGER.info("Checking directory: {}", pathToTest);

        JarCollector jarCollector = new JarCollector();
        jarCollector.gatherAt(Paths.get(pathToTest));
        LOGGER.info("jars collected = {}", jarCollector.getClasses().size());

        Optional<Integer> count = jarCollector.getClasses().values()
                .stream()
                .map(i -> i.getClasses().size())
                .reduce((x,y)->x+y)
        ;
        if (count.isPresent()) {
            LOGGER.info("classes detected count = {}", count.get());
        }
        else {
            LOGGER.info("No classes detected.");
        }

        Map<String,Collection<String>> duplicates = jarCollector.detectDuplicateClasses();
        if (duplicates.size() > 0) {
            LOGGER.info("Duplicates detected\n---\n");
            duplicates.forEach((key, value) -> LOGGER.info(key + "\n\t" + StringUtils.join(value, "\n\t")));
        }
        else
            LOGGER.info("No duplicates detected.");
    }
}

Detect Duplicates In Jars
---
Scans a directory of jars and detects if there are more than 1 jars that
contain a duplicate of a fully qualified class file.


Building
---
./gradlew build


Usage
---
./gradle run -Pmyargs="[directory to scan]"


Why this utility exists?
---
Very often you build your app and maven/ivy/gradle/etc pull in lots of dependent jars
that sometimes pull in jars or different version or different name which conatain same classes
and based on the order of classloader you may not be pulling in the class you through you were.
This was originally written to resolve the horrible mess we in the java world know as "Xerces Hell",
where XML parsers are defined in many jars of various names or versions and enbedded inside
other jars, often creating coding landmines.  This tool will at least let you know if you are accidentally 
pulling in duplicates and possibly exclude them in the package manager.

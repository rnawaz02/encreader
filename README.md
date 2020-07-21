# Encreader
Command Line Encryption Utility based on Java and C extended from edslite. Repository is cloned from [edslite](https://github.com/sovworks/edslite) which is an android application and build for Linux OS. It allows you to decrypt files encrypted using trucrypt from a command line.
# Build Instructions
Clone repository and simply run

`mvn clean package`

Only requirement is that you have Maven (mvn) installed on the file system path. It is a Java source code so also requires JDK 8 or above installed on the target system. Part of application is build using c code. Executable libraries for Linux (*.so) are already included with the source. It makes build process easy and straight forward. It also prsents a limitation that built jar only runs on Linux. For other operating systems you have to build native libraries by yourself. Simple instructions are given below if you want to take this route.

Above command will create executable jar file inside target folder which can be run using following instructions.

# Run Instructions

Only runs on Linux with prebuilt native libraries. Please build your own native libraries if you want to use on other operating systems

Java 8 or above is required to run this application. 

For help run `java -jar target/encreader-0.0.1-SNAPSHOT.jar` and following self explanatory help message will be printed

Missing required options: f, pw  
usage: Encryption Utility  
-f or --file is path to the container file  
-pw or --password is password  
-of or --openfile mean open file and absolute path follows it.  
-od or opendir mean open directory at absolute path which follows it.  

To open an encrypted container and see content of a folder inside it, run command as below.  
`java -jar target/encreader-0.0.1-SNAPSHOT.jar -f /path/to/encrypted/container/file.anyextension -pw password -od /folder/inside/name`  

To see contents of a file inside an encrypted container.  
`java -jar target/encreader-0.0.1-SNAPSHOT.jar -f /path/to/encrypted/container/file.anyextension -pw password -of /folder/inside/file.etc`  

# Build Instructions for C

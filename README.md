# pathignore

## Introduction

Java-version Pathignore plugin implemented by William_Wu for Jenkins. 

## Getting started

### Preparing for Development
- OpenJDK
    - Version: 1.8.0_275_1
    - Download url: https://jdk.java.net/java-se-ri/8-MR3   
    - To install it and verify that OpenJDK is installed, run the following command: `java -help`
- Maven
    - Version: 3.8.1
    - Download url: https://maven.apache.org/download.cgi
    - To install it and add full path of bin folder of Maven in your PATH variable
    - To verify that Maven is installed, run the following command: `mvn -version `
- IntelliJ IDEA (optional)

### Build hpi
- Method1
    - Start a command window and run the follwing command: 
        - build hpi: `mvn verify`
        - run Jenkins server with hpi: `hpi:run`
- Method2
    - Open Pathignore-plugin project by IntelliJ IDEA
    - Click Button named "Build Project" to build hpi
    - Click Button named "Debug" to run Jenkins server with hpi

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)


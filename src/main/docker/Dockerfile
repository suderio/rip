FROM java:8 

# Install maven
RUN apt-get update
RUN apt-get install -y maven

WORKDIR /rip

# Prepare by downloading dependencies
ADD ../../../pom.xml /rip/pom.xml
RUN ["mvn", "dependency:resolve"]
RUN ["mvn", "verify"]

# Adding source, compile and package into a fat jar
ADD ../../../src /rip/src
RUN ["mvn", "package"]

EXPOSE 4567
CMD ["/usr/lib/jvm/java-8-openjdk-amd64/bin/java", "-jar", "target/rip-jar-with-dependencies.jar"]
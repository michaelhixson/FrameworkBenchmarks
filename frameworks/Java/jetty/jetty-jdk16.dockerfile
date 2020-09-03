FROM debian:buster
RUN apt-get update && apt-get install -y curl

RUN curl https://download.java.net/java/early_access/loom/6/openjdk-16-loom+6-105_linux-x64_bin.tar.gz -o /tmp/jdk.tar.gz
RUN echo "1ead1a760dd6b0daf981cb59752b60350b376aa1705080837aec2d828b2e2faa  /tmp/jdk.tar.gz" | sha256sum -c
RUN tar xf /tmp/jdk.tar.gz -C /opt
RUN rm /tmp/jdk.tar.gz
ENV JAVA_HOME "/opt/jdk-16"
ENV PATH "${JAVA_HOME}/bin:${PATH}"

RUN curl http://apache.spinellicreations.com/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz -o /tmp/maven.tar.gz
RUN echo "c35a1803a6e70a126e80b2b3ae33eed961f83ed74d18fcd16909b2d44d7dada3203f1ffe726c17ef8dcca2dcaa9fca676987befeadc9b9f759967a8cb77181c0  /tmp/maven.tar.gz" | sha512sum -c
RUN tar xf /tmp/maven.tar.gz -C /opt
RUN rm /tmp/maven.tar.gz
ENV MAVEN_HOME "/opt/apache-maven-3.6.3"
ENV PATH "${MAVEN_HOME}/bin:${PATH}"

WORKDIR /jetty
COPY pom.xml pom.xml
COPY src src
RUN mvn compile assembly:single -q
CMD ["java", "-XX:+UseNUMA", "-XX:+UseParallelGC", "-jar", "target/jetty-example-0.1-jar-with-dependencies.jar"]

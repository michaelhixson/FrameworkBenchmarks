FROM debian:buster
RUN apt-get update && apt-get install -y curl

RUN curl https://download.java.net/java/early_access/loom/7/openjdk-15-loom+7-141_linux-x64_bin.tar.gz -o /tmp/jdk.tar.gz
RUN echo "e3751b67f1e6971a6b40ef825f6d88805bcccde954dd2fff04cda815c71c1a2b  /tmp/jdk.tar.gz" | sha256sum -c
RUN tar xf /tmp/jdk.tar.gz -C /opt
RUN rm /tmp/jdk.tar.gz
ENV JAVA_HOME "/opt/jdk-15"
ENV PATH "${JAVA_HOME}/bin:${PATH}"

RUN curl http://apache.spinellicreations.com/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz -o /tmp/maven.tar.gz
RUN echo "c35a1803a6e70a126e80b2b3ae33eed961f83ed74d18fcd16909b2d44d7dada3203f1ffe726c17ef8dcca2dcaa9fca676987befeadc9b9f759967a8cb77181c0  /tmp/maven.tar.gz" | sha512sum -c
RUN tar xf /tmp/maven.tar.gz -C /opt
RUN rm /tmp/maven.tar.gz
ENV MAVEN_HOME "/opt/apache-maven-3.6.3"
ENV PATH "${MAVEN_HOME}/bin:${PATH}"

WORKDIR /httpserver
COPY pom.xml pom.xml
COPY src src
RUN mvn compile assembly:single -q
CMD ["java", "-jar", "target/httpserver-1.0-jar-with-dependencies.jar"]

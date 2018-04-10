FROM maven:3.5.3-jdk-8
WORKDIR /act
COPY pom.xml pom.xml
COPY src src
RUN mvn package -q -P eclipselink_pgsql
WORKDIR /act/target/dist
RUN unzip -q *.zip
CMD ["java", "-server", "-Djava.security.egd=file:/dev/./urandom", "-Xms1G", "-Xmx1G", "-Xss320k", "-XX:+UseNUMA", "-XX:+UseParallelGC", "-XX:+AggressiveOpts", "-Dapp.mode=prod", "-Dapp.nodeGroup=", "-Dprofile=eclipselink_pgsql_rythm", "-Dxio.worker_threads.max=256", "-Dpgsql.host=tfb-database", "-cp", "/act/target/dist/classes:/act/target/dist/lib/*", "com.techempower.act.AppEntry"]

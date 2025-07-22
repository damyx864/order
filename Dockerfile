FROM eclipse-temurin:21-ubi9-minimal

WORKDIR /order

COPY target/*.jar order.jar

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar order.jar"]
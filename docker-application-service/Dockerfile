FROM openjdk:8
ADD money-transfer-0.0.1-SNAPSHOT.jar /data/money-transfer-0.0.1-SNAPSHOT.jar
ADD docker-configuration.yml /data/configuration.yml
CMD java -jar /data/money-transfer-0.0.1-SNAPSHOT.jar server /data/configuration.yml
EXPOSE 8080

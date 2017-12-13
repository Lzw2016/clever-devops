FROM java:8u111-jre-alpine

# 依赖 Maven 环境

ADD clever-devops-server/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=9066", "--server.address=0.0.0.0"]
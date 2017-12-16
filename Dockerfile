# 必须使用 JDK (必须配置环境变量 JAVA_HOME)
FROM java:8u111-jdk


# 依赖 Maven 环境

#ADD clever-devops-server/target/*.jar app.jar
#ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=9066", "--server.address=0.0.0.0"]
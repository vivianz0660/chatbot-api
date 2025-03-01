# Basic Image
FROM openjdk:8-jre-slim
# Author
MAINTAINER vivian
# Config
ENV PARAMS=""
# Zone
ENV TZ=PRC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
# Add application
ADD /chatbot-api-interfaces/target/chatbot-api.jar /chatbot-api.jar
# Excute image；docker run -e PARAMS=" --chatbot-api.groupId=Your planetID --chatbot-api.openAiKey=Apply --chatbot-api.cookie=Login cookie Infor" -p 8090:8090 --name chatbot-api -d chatbot-api:1.0
ENTRYPOINT ["sh","-c","java -jar $JAVA_OPTS /chatbot-api.jar $PARAMS"]

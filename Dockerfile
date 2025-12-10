FROM eclipse-temurin:21-jre-alpine
LABEL authors="ru.makcpp"

COPY ./entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
COPY ./build/libs/etm-solutions-bot-1.0-SNAPSHOT.jar /bot.jar

ENTRYPOINT ["/entrypoint.sh"]
# NotiD 백엔드 런타임 이미지
# CD 워크플로우(cd-deploy.yml)가 미리 './gradlew clean build -x test'로
# 만들어 둔 JAR을 그대로 복사해서 실행합니다.

FROM eclipse-temurin:25-jre

# 비루트 사용자로 실행 (보안 권장 사항)
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

WORKDIR /app

# Spring Boot가 만드는 부팅 가능한 fat JAR만 복사합니다.
# build.gradle 의 version 이 0.0.1-SNAPSHOT 이므로 파일명은
# 'notid-0.0.1-SNAPSHOT.jar' 입니다. (plain jar 인 '*-plain.jar' 는 제외)
ARG JAR_FILE=build/libs/notid-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# Spring Boot 기본 포트
EXPOSE 8080

# JVM 옵션은 컨테이너 실행 시 JAVA_OPTS 환경변수로 주입 가능
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

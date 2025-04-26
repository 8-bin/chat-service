# ===================================
# 1단계: Build Stage (Gradle로 JAR 파일 빌드)
# ===================================
FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

COPY --chown=gradle:gradle . .

RUN gradle clean build -x test

# ===================================
# 2단계: Run Stage (실행 환경만 가볍게 구성)
# ===================================
FROM openjdk:17-jdk-slim

WORKDIR /app

# 빌드한 JAR 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 환경변수로 포트 설정 (8080 기본값)
EXPOSE 8080

# JAR 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
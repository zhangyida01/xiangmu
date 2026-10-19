# 构建上下文必须是仓库根目录：docker build -f deploy/Dockerfile .
# 阶段 1：前端构建
FROM node:20-alpine AS web
WORKDIR /web
COPY delivery-web/package.json ./
RUN npm install --registry=https://registry.npmmirror.com --no-audit --no-fund
COPY delivery-web/ ./
RUN npm run build

# 阶段 2：后端构建，把前端产物塞进 jar 的 static 目录
FROM maven:3.9-eclipse-temurin-17 AS server
WORKDIR /build
COPY deploy/maven-settings.xml /root/.m2/settings.xml
COPY delivery-server/pom.xml ./
RUN mvn -B -q dependency:go-offline -DskipTests || true
COPY delivery-server/src ./src
COPY --from=web /web/dist ./src/main/resources/static
RUN mvn -B clean package -DskipTests

# 阶段 3：运行镜像
FROM eclipse-temurin:17-jre-jammy
ENV TZ=Asia/Shanghai LANG=C.UTF-8
# wget 供 HEALTHCHECK 使用（ubuntu jammy 基础镜像默认不带）；tzdata 保证容器时区正确
RUN apt-get update \
    && apt-get install -y --no-install-recommends wget tzdata \
    && ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone \
    && rm -rf /var/lib/apt/lists/* \
    && useradd -u 1000 -m pdm \
    && mkdir -p /app/logs /app/uploads /app/keys
WORKDIR /app
COPY --from=server /build/target/delivery-server.jar /app/app.jar
RUN chown -R pdm:pdm /app
USER pdm
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
  CMD wget -qO- http://127.0.0.1:8080/api/system/health || exit 1
ENTRYPOINT ["java","-XX:MaxRAMPercentage=70","-Djava.security.egd=file:/dev/./urandom","-jar","/app/app.jar"]
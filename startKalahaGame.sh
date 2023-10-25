./gradlew clean bootJar
docker build -t kalaha .
docker-compose up -d
sleep 8
open src/main/resources/static/kalahaGameUi.html
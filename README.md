# distributed-document-editor

```
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.config.location=classpath:/application-1.properties
```
All mongodb in one file:
```
docker compose -f docker-compose-mongo.yaml up -d
```
One mongo per file
```
port=28001 express_port=30001 docker compose -f docker-compose.yaml -p "m1" up -d
port=28002 express_port=30002 docker compose -f docker-compose.yaml -p "m2" up -d
port=28003 express_port=30003 docker compose -f docker-compose.yaml -p "m3" up -d 
port=28004 express_port=30004 docker compose -f docker-compose.yaml -p "m4" up -d
```


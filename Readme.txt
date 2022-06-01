Running Distributed Document Editor with 1 leader and 3 followers

Prerequisites:
1. JDK (minimum version required is 11): https://openjdk.java.net/install/
2. Maven: https://maven.apache.org/install.html
3. Docker https://docs.docker.com/get-docker/

First, we need 4 MongoDB containers. Below 4 commands create 4 different MongoDB containers.
port=28001 express_port=30001 docker compose -f docker-compose.yaml -p "m1" up -d
port=28002 express_port=30002 docker compose -f docker-compose.yaml -p "m2" up -d
port=28003 express_port=30003 docker compose -f docker-compose.yaml -p "m3" up -d
port=28004 express_port=30004 docker compose -f docker-compose.yaml -p "m4" up -d

Verify that these MongoDB containers are up and running using this command: docker ps
You will see an output like below.
You will also see mongo-express containers, which are just user interfaces for managing these MongoDB instances.
CONTAINER ID   IMAGE           COMMAND                  CREATED      STATUS       PORTS                                           NAMES
0cca76e7064e   mongo-express   "tini -- /docker-ent…"   5 days ago   Up 2 hours   0.0.0.0:30004->8081/tcp, :::30004->8081/tcp     mongo-express28004
3fa2c1ff3d0e   mongo           "docker-entrypoint.s…"   5 days ago   Up 2 hours   0.0.0.0:28004->27017/tcp, :::28004->27017/tcp   mongodb28004
c653da52cb56   mongo-express   "tini -- /docker-ent…"   5 days ago   Up 2 hours   0.0.0.0:30003->8081/tcp, :::30003->8081/tcp     mongo-express28003
25e3e2e6328b   mongo           "docker-entrypoint.s…"   5 days ago   Up 2 hours   0.0.0.0:28003->27017/tcp, :::28003->27017/tcp   mongodb28003
8802d70743da   mongo-express   "tini -- /docker-ent…"   5 days ago   Up 2 hours   0.0.0.0:30002->8081/tcp, :::30002->8081/tcp     mongo-express28002
b943227c8697   mongo           "docker-entrypoint.s…"   5 days ago   Up 2 hours   0.0.0.0:28002->27017/tcp, :::28002->27017/tcp   mongodb28002
2799cc9cb2a4   mongo           "docker-entrypoint.s…"   5 days ago   Up 2 hours   0.0.0.0:28001->27017/tcp, :::28001->27017/tcp   mongodb28001
f6a41eaa2b06   mongo-express   "tini -- /docker-ent…"   5 days ago   Up 2 hours   0.0.0.0:30001->8081/tcp, :::30001->8081/tcp     mongo-express28001

Next, we need to run the Load Balancer. Go inside directory LoadBalancer and run this command: mvn spring-boot:run
When it prints these logs, it means the server is up (note that the port is 8080):
2022-06-01 14:31:51.078  INFO 72230 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8080 (http) with context path ''
2022-06-01 14:31:51.085  INFO 72230 --- [           main] com.example.demo.DemoApplication         : Started DemoApplication in 1.003 seconds (JVM running for 1.193)

Next, we need to run the leader node first. Go inside distributed-document-editor directory and run this command:
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.config.location=classpath:/application-leader.properties

You will see logs similar to below (note that the port is 8081)::
2022-06-01 14:45:18.884  INFO 73294 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8081 (http) with context path ''
2022-06-01 14:45:18.889  INFO 73294 --- [   scheduling-1] c.project.documenteditor.PingService     : Node 1 is connected to [], and leader is 1
2022-06-01 14:45:18.891  INFO 73294 --- [           main] c.p.d.DocumentEditorApplication          : Started DocumentEditorApplication in 1.126 seconds (JVM running for 1.302)

After the leader is up, we can run the follower nodes one by one using below commands

mvn spring-boot:run -Dspring-boot.run.arguments=--spring.config.location=classpath:/application-1.properties
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.config.location=classpath:/application-2.properties
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.config.location=classpath:/application-3.properties

To access the application, go to Load balancer URL, i.e., http://localhost:8080
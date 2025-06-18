## Step-by-Step Microservices Setup with Spring Boot, Eureka, and Gateway

### 1. Initial Setup with Spring Initializr

Use [Spring Initializr](https://start.spring.io/) with these settings:

### ✅ Eureka Discovery Server

- Project: Maven
- Language: Java
- Spring Boot: 3.2.4
- Group: `com.osm`
- Artifact: `discoveryserver`
- Java: 17
- Dependencies:
    - Eureka Server

``:

```properties
server.port=8761
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

Main Class:

```java
@EnableEurekaServer
@SpringBootApplication
public class DiscoveryserverApplication {
    public static void main(String[] args) {
        SpringApplication.run(DiscoveryserverApplication.class, args);
    }
}
```

---

### Security Microservice

- Artifact: `securityservice`
- Dependencies:
    - Spring Web
    - Spring Data JPA
    - MySQL Driver
    - Spring Security
    - OAuth2 Resource Server (JWT)
    - Eureka Discovery Client
    - Lombok
    - MapStruct

``:

```properties
server.port=8081
spring.application.name=securityservice
spring.datasource.url=jdbc:mysql://localhost:3306/osm_security
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update

# Eureka client registration
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

**Main Class:**

```java
@SpringBootApplication
public class SecurityserviceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecurityserviceApplication.class, args);
    }
}
```

---

### Gateway Application

- Artifact: `gateway`
- Dependencies:
    - Spring Cloud Gateway
    - Spring Security
    - Eureka Discovery Client
    - Spring Data JPA
    - MySQL Driver
    - Lombok

``:

```properties
server.port=8080
spring.application.name=gateway

spring.datasource.url=jdbc:mysql://localhost:3306/osm_gateway
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update

# Eureka Configuration
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/

# Gateway routes
spring.cloud.gateway.discovery.locator.enabled=true
spring.cloud.gateway.routes[0].id=securityservice
spring.cloud.gateway.routes[0].uri=lb://securityservice
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/security/**
```

**Main Class:**

```java
@SpringBootApplication
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
```

---

## Run your applications in the following exact order:

1. Eureka Discovery Server (`discoveryserver`) at `localhost:8761`
2. Security Microservice (`securityservice`) at port `8081`
3. Gateway (`gateway`) at port `8080`

---

### Verify Everything:

Open Eureka UI:

```
http://localhost:8761
```

You should clearly see:

- `securityservice`
- `gateway`

---

## Recommended Version Compatibility:

| Component    | Version  |
| ------------ | -------- |
| Spring Boot  | 3.2.4    |
| Spring Cloud | 2024.0.0 |
| Java         | 17+      |

---

Following these instructions ensures a stable, fully functional microservice architecture with Spring Boot and Eureka service discovery.


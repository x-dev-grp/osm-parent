# OSM Parent Module (osm-parent)

The central repository for shared code, base entities, and common models used across all microservices.

## 📦 Modules
- **`xdev-base`**: Core infrastructure, generic services, and base entities.
- **`comunicator`**: Shared DTOs and Feign client definitions.
- **`xdev-security`**: Shared security configurations and OAuth2 utilities.

## 🛠 Tech Stack
- **Language:** Java 21
- **Framework:** Spring Boot 3.4.4
- **Dependencies:** Spring Data JPA, Hibernate Envers, ModelMapper.

## 🚀 Installation
To use these modules in microservices, install them to your local Maven repository:
```bash
./mvnw clean install
```

## 🔗 CI/CD & Publishing
This project publishes its JARs to **GitHub Packages**:
- **Target URL:** `https://maven.pkg.github.com/x-dev-grp/osm-parent`
- **Workflow:** `.github/workflows/maven-publish.yml` (if configured)

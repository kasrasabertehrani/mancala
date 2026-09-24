# Mancala Online

![Java](https://img.shields.io/badge/Java-17-blue?logo=java) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.x-brightgreen?logo=spring-boot) ![Docker](https://img.shields.io/badge/Docker-Multi--Stage-2CA5E0?logo=docker&logoColor=white) ![GitHub Actions](https://img.shields.io/badge/CI%2FCD-GitHub_Actions-2088FF?logo=github-actions&logoColor=white) ![GHCR](https://img.shields.io/badge/Registry-GHCR-lightgrey?logo=github) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=kasrasabertehrani_mancala&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=kasrasabertehrani_mancala) [![codecov](https://codecov.io/github/kasrasabertehrani/mancala/graph/badge.svg?token=B28F3F7W4W)](https://codecov.io/github/kasrasabertehrani/mancala)

An online version of the classic board game **Mancala**.

We designed this project to showcase:
- Domain Driven Design
- DevOps practices
- CI/CD pipelines
- Deployment process
- Multi-platform application development

## Tech Stack

| Area | Technologies |
| --- | --- |
| Application | Java 17, Spring Boot 4.x |
| Communication | HTTP, WebSockets |
| Architecture | Domain-Driven Design, Hexagonal Architecture |
| Containerization | Docker with a multi-stage build |
| CI/CD | GitHub Actions |
| Container registry | GitHub Container Registry (GHCR) |
| Quality reporting | SonarCloud, Codecov |

## Where to play

### Docker
1. Clone the repository
3. Navigate to the root directory
5. Build image and run the container:
   ```bash
   git clone https://github.com/yourusername/mancala-game.git
   
   cd mancala-game
   
   docker build -t mancala-local .
   docker run -p 8080:8080 mancala-local
   ```
## How to Play
1. Type in your name.
2. Create room or connect to the existing one by room id.
3. Play!

Checkout [Mancala Rules](https://www.scholastic.com/content/dam/teachers/blogs/alycia-zimmerman/migrated-files/mancala_rules.pdf)

![Animation](https://github.com/user-attachments/assets/998e9468-a0a5-4526-9fc3-1e737256e7eb)



## Architecture & Design Patterns

This project is built on the combined principles of **Domain-Driven Design (DDD)** and **Hexagonal Architecture (Ports and Adapters)**. 

The primary goal of this architecture is to treat the core rules of Mancala as the untouchable "heart" of the application, strictly isolating the business logic from the complexities of the network, database, or user interface.

* **Domain-Driven Design (The Core):** The pure rules of the game are completely encapsulated within the domain. The domain dictates exactly how Mancala is played and validates every move, but it is intentionally "blind" to the outside world. It knows absolutely nothing about WebSockets, HTTP requests, or whether the game is being played on a web browser or a mobile app. 
* **Hexagonal Architecture (The Adapters):** The Spring Boot controllers and WebSocket handlers act as protective boundary layers (Adapters) around the core domain. They translate messy external network traffic into pure commands the domain understands, and they listen for internal domain events to translate back out to the network. 

## Contribution

### Kasra Sabertehrani — Software Design & Game Development

I was primarily responsible for the design and development of the game application, including:

- Structuring the application around domain models, use-case interfaces, and infrastructure adapters.
- Implementing Mancala gameplay: move validation, stone distribution, captures, extra turns, scoring, and game completion.
- Developing room management and the disconnect, reconnect, and timeout logic.
- Implementing the browser-based game interface and its integration with REST endpoints and STOMP/WebSocket updates.

### Igor Chukarin — Testing, Code Review & Delivery Automation

Igor's responsibilities included:

- Writing unit tests with **JUnit 5** and **Mockito**, including mocked dependencies for isolated testing.
- Reviewing the application code and making code-quality improvements.
- Implementing the **GitHub Actions** CI/CD workflows, including automated verification, quality analysis, and coverage reporting.
- Handling container publication to **GHCR** and **SSH-based deployment** to the production server.




# Real-Time Multiplayer Mancala

![Java](https://img.shields.io/badge/Java-17-blue?logo=java) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen?logo=spring-boot) ![Docker](https://img.shields.io/badge/Docker-Multi--Stage-2CA5E0?logo=docker&logoColor=white) ![GitHub Actions](https://img.shields.io/badge/CI%2FCD-GitHub_Actions-2088FF?logo=github-actions&logoColor=white) ![GHCR](https://img.shields.io/badge/Registry-GHCR-lightgrey?logo=github)

A real-time, online multiplayer implementation of the classic board game Mancala. Built with a strict adherence to Domain-Driven Design (DDD) and fully automated via a modern CI/CD pipeline, this project serves as a showcase of robust software engineering processes.

##  Live Demo & How to Play
You can play the game right now on our live production server, or run it locally. 

 Play Live: [http://204.168.162.199:8080](http://204.168.162.199:8080)

### Game Flow
1. **Enter the Lobby:** Type your player name.
2. **Start a Match:** * Click **Create Room** to generate a new game and get a Room ID.
   * *Or*, enter an existing Room ID and click **Join Room** to connect with a friend.
3. **Play:** The game synchronizes in real-time. If your connection drops, the server will pause the game and give you a 30-second window to reconnect without forfeiting!

![Animation](https://github.com/user-attachments/assets/eb1c4f97-71a5-4f98-a144-4dad93e69bd0)


##  Architecture & Design Patterns

This project is built on the combined principles of **Domain-Driven Design (DDD)** and **Hexagonal Architecture (Ports and Adapters)**. 

The primary goal of this architecture is to treat the core rules of Mancala as the untouchable "heart" of the application, strictly isolating the business logic from the complexities of the network, database, or user interface.

* **Domain-Driven Design (The Core):** The pure rules of the game are completely encapsulated within the domain. The domain dictates exactly how Mancala is played and validates every move, but it is intentionally "blind" to the outside world. It knows absolutely nothing about WebSockets, HTTP requests, or whether the game is being played on a web browser or a mobile app. 
* **Hexagonal Architecture (The Adapters):** The Spring Boot controllers and WebSocket handlers act as protective boundary layers (Adapters) around the core domain. They translate messy external network traffic into pure commands the domain understands, and they listen for internal domain events to translate back out to the network. 


##  Continuous Integration & Deployment (CI/CD)
The project utilizes a fully automated, 3-stage CI/CD pipeline built with **GitHub Actions**. It triggers automatically on pushes and pull requests to the `master` branch.

1. **Continuous Integration (CI):** * Provisions an Ubuntu runner with Java 17.
   * Runs automated tests and verification using the Maven Wrapper (`./mvnw clean verify`).
   * Performs a dry-run Docker build to ensure containerization stability.
2. **Publish (Delivery):** * If CI passes, the application is built into a Docker image.
   * The image is tagged and securely pushed to the **GitHub Container Registry (GHCR)**.
3. **Continuous Deployment (CD):** * Connects to the production server via SSH.
   * Pulls the latest immutable image from GHCR.
   * Gracefully stops the old container, spins up the new version on port `8080`, and prunes unused images.

##  Docker Containerization
The application is packaged using an optimized **Multi-Stage Dockerfile**:
* **Stage 1 (Build):** Utilizes `maven:3.9.9-eclipse-temurin-17` to fetch dependencies offline and compile the executable `.jar`. 
* **Stage 2 (Runtime):** Utilizes a lightweight `eclipse-temurin:17-jre` image to run the application, drastically reducing the final image size and attack surface area.

##  Local Development

If you prefer to run the server locally instead of using the live demo, you have can:

### Run with Docker 
1. Clone the repository: `git clone https://github.com/yourusername/mancala-game.git`
2. Navigate to the root directory: `cd mancala-game`
3. Build and run the container:
   ```bash
   docker build -t mancala-local .
   docker run -p 8080:8080 mancala-local

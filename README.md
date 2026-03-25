# Mancala Online

![Java](https://img.shields.io/badge/Java-17-blue?logo=java) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen?logo=spring-boot) ![Docker](https://img.shields.io/badge/Docker-Multi--Stage-2CA5E0?logo=docker&logoColor=white) ![GitHub Actions](https://img.shields.io/badge/CI%2FCD-GitHub_Actions-2088FF?logo=github-actions&logoColor=white) ![GHCR](https://img.shields.io/badge/Registry-GHCR-lightgrey?logo=github)

An online version of the classic board game **Mancala**.

We designed this project to showcase:
- Domain Driven Design
- DevOps practices
- CI/CD pipelines
- Deployment process
- Multi-platform application development

## Where to play
### Server
You can play the game right now (while we still pay for the server)

Available at: [http://204.168.162.199:8080](http://204.168.162.199:8080)
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

![Animation](https://github.com/user-attachments/assets/eb1c4f97-71a5-4f98-a144-4dad93e69bd0)


## Architecture & Design Patterns

This project is built on the combined principles of **Domain-Driven Design (DDD)** and **Hexagonal Architecture (Ports and Adapters)**. 

The primary goal of this architecture is to treat the core rules of Mancala as the untouchable "heart" of the application, strictly isolating the business logic from the complexities of the network, database, or user interface.

* **Domain-Driven Design (The Core):** The pure rules of the game are completely encapsulated within the domain. The domain dictates exactly how Mancala is played and validates every move, but it is intentionally "blind" to the outside world. It knows absolutely nothing about WebSockets, HTTP requests, or whether the game is being played on a web browser or a mobile app. 
* **Hexagonal Architecture (The Adapters):** The Spring Boot controllers and WebSocket handlers act as protective boundary layers (Adapters) around the core domain. They translate messy external network traffic into pure commands the domain understands, and they listen for internal domain events to translate back out to the network. 


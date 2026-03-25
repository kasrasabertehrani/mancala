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


##  Architecture & Design Patterns
This project utilizes **Domain-Driven Design** and **Hexagonal Architecture** principles:
- **Rich Domain Models:** Core entities such as `GameRoom`, `Player`, and `Board` encapsulate all business logic and rule validation independently of the Spring Framework.

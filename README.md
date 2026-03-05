# Messenger Backend

A real-time messaging backend built with **Spring Boot 4**, featuring REST APIs, WebSocket (STOMP) support, JWT authentication, and PostgreSQL persistence.

## Tech Stack

- **Java 17** / **Spring Boot 4.0.3**
- **Spring Security** — JWT-based stateless authentication
- **Spring WebSocket** — STOMP over SockJS for real-time messaging
- **Spring Data JPA** — PostgreSQL persistence
- **Lombok** — Boilerplate reduction
- **Gradle 9** — Build tool

## Prerequisites

- **Java 17+**
- **PostgreSQL** installed and running

## Getting Started

### 1. Set Up PostgreSQL

#### macOS (Homebrew)

```bash
brew install postgresql
brew services start postgresql
```

#### Ubuntu / Debian

```bash
sudo apt update && sudo apt install postgresql
sudo systemctl start postgresql
```

### 2. Create Database and User

```bash
psql postgres -c "CREATE USER messenger_user WITH PASSWORD 'change-me';"
psql postgres -c "CREATE DATABASE messengerdb OWNER messenger_user;"
```

> **Note:** Update `src/main/resources/application.properties` if you use different credentials.

### 3. Build and Run

```bash
# Build (skipping tests)
./gradlew clean build -x test

# Build (with tests — requires PostgreSQL to be running)
./gradlew clean build

# Run the application
./gradlew bootRun
```

The server starts at **http://localhost:8080**.

## API Reference

### Authentication

| Method | Endpoint         | Description           | Auth Required |
|--------|------------------|-----------------------|---------------|
| POST   | `/auth/register` | Register a new user   | No            |
| POST   | `/auth/login`    | Login and get a JWT   | No            |

#### Register

```json
// POST /auth/register
// Request
{ "username": "john", "password": "secret123" }

// Response — 200 OK
{ "username": "john" }
```

#### Login

```json
// POST /auth/login
// Request
{ "username": "john", "password": "secret123" }

// Response — 200 OK
{ "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

### Messages

| Method | Endpoint                          | Description                          | Auth Required |
|--------|-----------------------------------|--------------------------------------|---------------|
| GET    | `/messages/conversation/{withUser}` | Get conversation with a specific user | Yes (JWT)     |

Include the JWT in the `Authorization` header:

```
Authorization: Bearer <token>
```

## WebSocket (Real-Time Messaging)

### Connection

Connect via SockJS to:

```
ws://localhost:8080/ws
```

### STOMP Destinations

| Action         | Send To            | Payload                                              |
|----------------|--------------------|------------------------------------------------------|
| Send a message | `/app/chat.send`   | `{ "sender": "...", "recipient": "...", "content": "..." }` |
| Join chat      | `/app/chat.join`   | `{ "sender": "..." }`                                |

### Subscribe To

| Destination                        | Description                    |
|------------------------------------|--------------------------------|
| `/user/{username}/queue/messages`  | Private messages for the user  |
| `/topic/messages`                  | All messages (broadcast)       |
| `/topic/status`                    | User join notifications        |

### Testing WebSocket APIs

A test page is included at [ws-test.html](ws-test.html). Open it in a browser while the server is running:

```bash
open ws-test.html        # macOS
xdg-open ws-test.html    # Linux
```

**Steps to test:**

1. Click **Connect** — status should show "Connected ✅"
2. Enter your username (e.g., `admin`) and click **Subscribe to All** — this subscribes to private messages, broadcast, and status channels
3. Click **Send Join** — you should see a `🟢 Status` notification in the log
4. Enter a recipient username, type a message, and click **Send** — you should see:
   - `📩 Private` message (if subscribed as the sender or recipient)
   - `📢 Broadcast` message on `/topic/messages`

> **Tip:** Open the page in two browser tabs with different usernames to simulate a conversation between two users.

## Project Structure

```
src/main/java/com/backend/messenger/
├── MessengerApplication.java          # Entry point
├── config/
│   ├── SecurityConfig.java            # Spring Security & JWT filter config
│   └── WebSocketConfig.java           # STOMP/SockJS configuration
├── controller/
│   ├── AuthController.java            # Registration & login endpoints
│   └── MessageRestController.java     # Message history endpoint
├── model/
│   ├── Message.java                   # Message entity
│   ├── Role.java                      # Role entity
│   └── User.java                      # User entity
├── repository/
│   ├── MessageRepository.java
│   ├── RoleRepository.java
│   └── UserRepository.java
├── security/
│   ├── JwtFilter.java                 # JWT request filter
│   └── JwtUtil.java                   # JWT generation & validation
├── service/
│   ├── MessageService.java            # Message business logic
│   └── UserService.java               # User business logic & bootstrap
└── ws/
    └── ChatController.java            # WebSocket STOMP message handlers
```

## Default User

On first startup, the application creates a default user:

- **Username:** `admin`
- **Password:** `admin`

## Configuration

Key properties in `src/main/resources/application.properties`:

| Property                  | Default                                      | Description             |
|---------------------------|----------------------------------------------|-------------------------|
| `spring.datasource.url`  | `jdbc:postgresql://localhost:5432/messengerdb` | Database URL            |
| `spring.datasource.username` | `messenger_user`                          | Database username       |
| `spring.datasource.password` | `change-me`                               | Database password       |
| `jwt.secret`             | `very-secret-key-change-me`                   | JWT signing secret      |
| `jwt.expiration-ms`      | `86400000` (24 hours)                         | JWT token expiry        |

## License

This project is for personal/educational use.
# Messenger Backend — API Documentation

**Base URL:** `http://localhost:8080`

---

## Authentication

All endpoints except `/auth/**` and `/ws/**` require a JWT token in the `Authorization` header:

```
Authorization: Bearer <token>
```

Token expires after **24 hours** (86400000 ms).

---

## REST Endpoints

### 1. Register

| | |
|---|---|
| **URL** | `POST /auth/register` |
| **Auth** | None |
| **Content-Type** | `application/json` |

**Request Body:**

```json
{
  "username": "john",
  "password": "secret123"
}
```

**Response `200 OK`:**

```json
{
  "username": "john"
}
```

---

### 2. Login

| | |
|---|---|
| **URL** | `POST /auth/login` |
| **Auth** | None |
| **Content-Type** | `application/json` |

**Request Body:**

```json
{
  "username": "john",
  "password": "secret123"
}
```

**Response `200 OK`:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

**Response `401 Unauthorized`:**

```json
{
  "error": "Invalid credentials"
}
```

---

### 3. List Conversations (Paginated)

| | |
|---|---|
| **URL** | `GET /messages/conversations?page=0&size=20` |
| **Auth** | Bearer token required |

**Query Params:**

| Param | Type | Default | Description |
|---|---|---|---|
| `page` | int | `0` | Zero-based page index |
| `size` | int | `20` | Number of conversations per page |

**Response `200 OK`:**

```json
{
  "content": [
    {
      "partner": "jane",
      "lastMessageAt": "2026-03-05T14:30:00.000000Z"
    },
    {
      "partner": "bob",
      "lastMessageAt": "2026-03-04T09:15:00.000000Z"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 2,
  "totalPages": 1,
  "last": true,
  "first": true,
  "numberOfElements": 2
}
```

> Results are sorted by the most recent message (newest conversation first).

---

### 4. Get Conversation History

| | |
|---|---|
| **URL** | `GET /messages/conversation/{withUser}` |
| **Auth** | Bearer token required |

**Path Params:**

- `withUser` — username of the other participant

**Response `200 OK`:**

```json
[
  {
    "id": 1,
    "sender": "john",
    "recipient": "jane",
    "content": "Hey!",
    "status": "SEEN",
    "sentAt": "2026-03-05T10:00:00.000000Z",
    "deliveredAt": "2026-03-05T10:00:01.000000Z",
    "seenAt": "2026-03-05T10:00:03.000000Z"
  },
  {
    "id": 2,
    "sender": "jane",
    "recipient": "john",
    "content": "Hi there!",
    "status": "DELIVERED",
    "sentAt": "2026-03-05T10:00:05.000000Z",
    "deliveredAt": "2026-03-05T10:00:06.000000Z",
    "seenAt": null
  }
]
```

---

### 5. Check User Online Status

| | |
|---|---|
| **URL** | `GET /presence/{username}` |
| **Auth** | Bearer token required |

**Path Params:**

- `username` — the user to check

**Response `200 OK`:**

```json
{
  "username": "jane",
  "online": true
}
```

---

### 6. Get All Online Users

| | |
|---|---|
| **URL** | `GET /presence` |
| **Auth** | Bearer token required |

**Response `200 OK`:**

```json
{
  "onlineUsers": ["john", "jane"]
}
```

---

## WebSocket (STOMP over SockJS)

### Connection

| | |
|---|---|
| **Endpoint** | `ws://localhost:8080/ws` (SockJS) |
| **Protocol** | STOMP 1.1 |

**STOMP CONNECT headers:**

```
Authorization: Bearer <token>
```

The server extracts the username from the JWT and sets it as the STOMP user principal.

---

### Destinations to Subscribe

| Destination | Purpose | Payload |
|---|---|---|
| `/user/queue/messages` | Receive private messages (sent to you or by you) | `Message` object (includes `status`, `deliveredAt`, `seenAt`) |
| `/user/queue/status-updates` | Receive delivery/seen receipts for messages you sent | `{ messageId, status, deliveredAt?, seenAt? }` |
| `/user/queue/errors` | Receive error notifications | `{ "error": "..." }` |
| `/topic/status` | Receive join/status broadcasts | `"john joined"` (plain string) |
| `/topic/presence` | Receive online/offline events for all users | `{ "username": "jane", "online": true }` |

---

### Destinations to Send

#### Send a Private Message

| | |
|---|---|
| **Destination** | `/app/chat.send` |

**Payload:**

```json
{
  "recipient": "jane",
  "content": "Hello!"
}
```

> `sender` is **ignored** — the server overwrites it with the authenticated user's username.

**On success:** the message (with `id`, `sender`, `sentAt`) is delivered to:

- `/user/queue/messages` of the **recipient**
- `/user/queue/messages` of the **sender** (echo back)

**On error** (recipient doesn't exist):

Sent to `/user/queue/errors`:

```json
{
  "error": "User 'nonexistent' does not exist"
}
```

#### Mark Message as Delivered

| | |
|---|---|
| **Destination** | `/app/chat.delivered` |

**Payload:**

```json
{
  "messageId": 123
}
```

> Only the **recipient** of the message can mark it delivered. The sender receives a status update on `/user/queue/status-updates`:

```json
{
  "messageId": 123,
  "status": "DELIVERED",
  "deliveredAt": "2026-03-05T10:00:01.000000Z"
}
```

---

#### Mark Message as Seen

| | |
|---|---|
| **Destination** | `/app/chat.seen` |

**Payload:**

```json
{
  "messageId": 123
}
```

> Only the **recipient** of the message can mark it seen. The sender receives a status update on `/user/queue/status-updates`:

```json
{
  "messageId": 123,
  "status": "SEEN",
  "seenAt": "2026-03-05T10:01:00.000000Z"
}
```

---

#### Join / Announce Presence

| | |
|---|---|
| **Destination** | `/app/chat.join` |

**Payload:** none (empty body)

**Effect:** broadcasts `"<username> joined"` to `/topic/status`.

---

### Automatic Behaviors

- **On WebSocket CONNECT:** user is marked **online**, broadcast to `/topic/presence`. All pending `SENT` messages are auto-marked `DELIVERED` and senders are notified via `/user/queue/status-updates`.
- **On WebSocket DISCONNECT:** user is marked **offline**, broadcast to `/topic/presence`.
- **On `/app/chat.send`:** if recipient is online, message is auto-marked `DELIVERED` immediately.

---

## Data Models

### Message

```typescript
interface Message {
  id: number;
  sender: string;
  recipient: string;
  content: string;
  status: "SENT" | "DELIVERED" | "SEEN";
  sentAt: string;        // ISO 8601 Instant
  deliveredAt: string | null;
  seenAt: string | null;
}
```

### ConversationDTO

```typescript
interface ConversationDTO {
  partner: string;       // username of the other participant
  lastMessageAt: string; // ISO 8601 Instant of the latest message in the conversation
}
```

### Auth Request / Response

```typescript
// Login & Register request
interface AuthRequest {
  username: string;
  password: string;
}

// Login response
interface LoginResponse {
  token: string;
}

// Register response
interface RegisterResponse {
  username: string;
}

// Error response
interface ErrorResponse {
  error: string;
}
```

---

## Next.js Integration Quick Reference

### HTTP Calls

```typescript
const res = await fetch("http://localhost:8080/auth/login", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({ username, password }),
});
const { token } = await res.json();
```

### WebSocket (STOMP + SockJS)

```typescript
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

const client = new Client({
  webSocketFactory: () => new SockJS("http://localhost:8080/ws"),
  connectHeaders: { Authorization: `Bearer ${token}` },
  onConnect: () => {
    client.subscribe("/user/queue/messages", (msg) => {
      const message = JSON.parse(msg.body);
    });
    client.subscribe("/user/queue/errors", (msg) => {
      const error = JSON.parse(msg.body);
    });
    client.subscribe("/topic/status", (msg) => {
      console.log(msg.body); // "john joined"
    });
    // Announce join
    client.publish({ destination: "/app/chat.join" });
  },
});
client.activate();

// Send a message
client.publish({
  destination: "/app/chat.send",
  body: JSON.stringify({ recipient: "jane", content: "Hello!" }),
});
```

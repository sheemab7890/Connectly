<div align="center">

<img src="https://img.shields.io/badge/Java-17+-ED8B00?style=flat-square&logo=openjdk&logoColor=white"/>
<img src="https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=flat-square&logo=springboot&logoColor=white"/>
<img src="https://img.shields.io/badge/Spring_Cloud-2023.x-6DB33F?style=flat-square&logo=spring&logoColor=white"/>
<img src="https://img.shields.io/badge/Apache_Kafka-3.x-231F20?style=flat-square&logo=apachekafka&logoColor=white"/>
<img src="https://img.shields.io/badge/Neo4j-5.x-008CC1?style=flat-square&logo=neo4j&logoColor=white"/>
<img src="https://img.shields.io/badge/PostgreSQL-15+-4169E1?style=flat-square&logo=postgresql&logoColor=white"/>
<img src="https://img.shields.io/badge/JWT-HMAC--SHA256-000000?style=flat-square&logo=jsonwebtokens&logoColor=white"/>
<img src="https://img.shields.io/badge/SSE-Real--Time-FF6B35?style=flat-square"/>
<img src="https://img.shields.io/badge/License-MIT-blue?style=flat-square"/>

<br/><br/>

# Connectly — Distributed Social Platform

### A production-grade LinkedIn clone engineered on a microservices architecture

Event-driven · Graph-powered social graph · Real-time SSE notifications · JWT security · Transactional Outbox Pattern

<br/>

[Architecture](#architecture) · [Services](#services) · [Event Flow](#event-flow) · [Security](#security) · [Databases](#databases) · [Running Locally](#running-locally)

</div>

---

## What This Is

Connectly is a **backend microservices platform** that replicates LinkedIn's core functionality — user authentication, social connections, content posting, and real-time notifications. It is built to demonstrate **senior-level backend engineering** across distributed systems design, event-driven architecture, graph databases, and security.

Six services. Three databases. One Kafka bus. Zero shared state.

---

## Architecture

The system uses a **hybrid communication model**: synchronous HTTP via Feign for real-time queries, asynchronous Kafka for decoupled event delivery. An API Gateway handles all inbound traffic, validates JWT tokens, and routes to downstream services discovered via Eureka.

```
┌─────────────────────────────────────────────────────────────────────┐
│                   CLIENT  (Browser / Mobile)                        │
│              Login · Posts · Connections · SSE Stream               │
└──────────────────────────────┬──────────────────────────────────────┘
                               │ HTTP + SSE
                               ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    API GATEWAY  · :9876                             │
│                                                                     │
│  JWT validation (Bearer header + HttpOnly cookie fallback)          │
│  lb://USER-SERVICE       ←  /api/v1/users/**       (public)         │
│  lb://POST-SERVICE       ←  /api/v1/posts/**       (JWT required)   │
│  lb://CONNECTION-SERVICE ←  /api/v1/connections/** (JWT required)   │
│  lb://NOTIFICATION-SVC   ←  /api/v1/notifications/**(JWT required)  │
└──────┬──────────────┬──────────────┬──────────────┬────────────────┘
       │              │              │              │
       ▼              ▼              ▼              ▼
 ┌──────────┐   ┌──────────┐  ┌──────────┐  ┌────────────┐
 │   USER   │   │   POST   │  │  CONN.   │  │   NOTIF.   │
 │  :8080   │   │  :8888   │  │  :9040   │  │   :9999    │
 │          │   │          │  │          │  │            │
 │ Sign Up  │   │ Posts    │  │ Graph    │  │ Kafka sink │
 │ Login    │   │ Likes    │  │ Requests │  │ SSE push   │
 │ JWT gen  │   │ Comments │  │ Neo4j    │  │ Persist    │
 │ Outbox   │   │ Feign ──►├─►/internal │  │ Feign ────►│
 └────┬─────┘   └────┬─────┘  └────┬────┘  └──────┬─────┘
      │              │             │               │
      ▼              ▼             ▼               ▼
 ┌─────────┐   ┌──────────┐  ┌─────────┐    ┌──────────┐
 │Postgres │   │Postgres  │  │  Neo4j  │    │Postgres  │
 │users    │   │posts     │  │ Person  │    │notifica- │
 │outbox   │   │likes     │  │ nodes + │    │tions     │
 │         │   │comments  │  │ edges   │    │          │
 └─────────┘   └──────────┘  └─────────┘    └──────────┘

═══════════════════════ APACHE KAFKA ════════════════════════════════

  [user_created]                    User Svc ──────────► Connection Svc
  [send-connection-request-topic]   Conn Svc ──────────► Notification Svc
  [accept-connection-request-topic] Conn Svc ──────────► Notification Svc
  [post-created-topic]              Post Svc ──────────► Notification Svc
  [post-liked-topic]                Post Svc ──────────► Notification Svc
  [post-comment-topic]              Post Svc ──────────► Notification Svc

════════════════════ EUREKA REGISTRY  :8761 ════════════════════════
```

---

## Tech Stack

| Concern | Technology |
|---|---|
| Language | Java 17+ |
| Framework | Spring Boot 3.x |
| Gateway | Spring Cloud Gateway (reactive, WebFlux) |
| Service Discovery | Spring Cloud Netflix Eureka |
| Inter-service HTTP | Spring Cloud OpenFeign |
| Async Messaging | Apache Kafka |
| Graph Database | Neo4j 5.x + Spring Data Neo4j |
| Relational Database | PostgreSQL 15+ + Spring Data JPA |
| Authentication | JJWT · HMAC-SHA256 · BCrypt |
| Real-time Push | Server-Sent Events (SSE) |
| Build | Maven · Lombok · ModelMapper · Jackson |

---

## Services

### API Gateway — `:9876`

The single entry point for all traffic. Built on Spring Cloud Gateway's reactive WebFlux stack.

**Responsibilities**
- Validates JWTs from `Authorization: Bearer` header or `access_token` HttpOnly cookie
- Routes to downstream services using Eureka load balancing (`lb://SERVICE-NAME`)
- Strips `/api/v1` prefix via `StripPrefix=2` before forwarding
- Mutates request to ensure `Authorization` header is always present downstream
- Passes SSE long-lived connections transparently to the Notification Service

**Route Table**

| Incoming Path | Downstream | Auth Filter |
|---|---|---|
| `/api/v1/users/**` | `USER-SERVICE` | ❌ None — public endpoints |
| `/api/v1/posts/**` | `POST-SERVICE` | ✅ `AuthenticationFilter` |
| `/api/v1/connections/**` | `CONNECTION-SERVICE` | ✅ `AuthenticationFilter` |
| `/api/v1/notifications/**` | `NOTIFICATION-SERVICE` | ✅ `AuthenticationFilter` |

---

### Discovery Service — `:8761`

Netflix Eureka Server. All microservices register at startup. The gateway resolves `lb://SERVICE-NAME` through this registry, enabling transparent client-side load balancing with zero hardcoded service URLs.

> Dashboard available at `http://localhost:8761`

---

### User Service — `:8080`

Owns user identity, authentication, and JWT issuance. Implements the **Transactional Outbox Pattern** — the most critical reliability guarantee in the system.

**Endpoints**

| Method | Path | Description |
|---|---|---|
| `POST` | `/auth/signUp` | Register new user |
| `POST` | `/auth/login` | Authenticate · JWT in body + HttpOnly cookie |

**Signup flow**

```
1. Validate email uniqueness
2. Hash password with BCrypt (salted, adaptive cost)
3. Save User entity to PostgreSQL
4. Serialize UserCreatedEvent → save to outbox_events  ← same DB transaction
5. Return UserDto to client

OutboxPoller  (every 10s):
  → Fetch top 50 PENDING outbox events
  → Publish to Kafka [user_created] with synchronous .get() ACK
  → Mark status = SENT only after confirmed delivery
  → On failure: leave as PENDING → auto-retry next cycle
```

**Why Outbox?** Without it, a network partition between the DB write and the Kafka publish causes a silent inconsistency — the user exists but the rest of the system never learns about them. The Outbox Pattern makes signup and event delivery **atomically guaranteed**.

**JWT properties**
- Algorithm: HMAC-SHA256 · Subject: `userId` · Expiry: 10 minutes
- Delivered in response body (API clients) and `HttpOnly; Path=/; MaxAge=600` cookie (browsers)

---

### Connection Service — `:9040`

Models the **social graph** in Neo4j. Users are `Person` nodes; relationships are first-class graph edges. Consumes `UserCreatedEvent` to build the graph and produces events when connections change.

**Graph model**

```cypher
(:Person)-[:REQUESTED_TO]->(:Person)   // pending request
(:Person)-[:CONNECTED_TO]->(:Person)   // accepted connection

// Accepting a request is one atomic Cypher query:
MATCH (p1)-[r:REQUESTED_TO]->(p2) WHERE ...
DELETE r
CREATE (p1)-[:CONNECTED_TO]->(p2)
```

**Endpoints**

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/core/first-degree` | JWT | Get current user's connections |
| `GET` | `/core/internal/{userId}/first-degree` | None | Internal service-to-service |
| `POST` | `/core/request/{userId}` | JWT | Send connection request |
| `POST` | `/core/accept/{userId}` | JWT | Accept connection request |
| `POST` | `/core/reject/{userId}` | JWT | Reject connection request |

**Why Neo4j?** First/second-degree traversal and mutual connection queries are `O(depth)` in a graph DB. The equivalent SQL multi-join query degrades significantly at scale and becomes complex to maintain.

**Idempotent consumer:** checks `existsByUserId` before creating a Person node — safe for Kafka message redelivery.

---

### Post Service — `:8888`

The content layer. Manages posts, likes, and comments. Calls Connection Service synchronously via Feign and publishes domain events for every user action.

**Endpoints**

| Method | Path | Description |
|---|---|---|
| `POST` | `/core/createPost` | Create post |
| `GET` | `/core/{postId}` | Get post by ID |
| `GET` | `/core/users/{userId}/allPosts` | All posts by user |
| `POST` | `/likes/like/{postId}` | Like a post |
| `DELETE` | `/likes/dislike/{postId}` | Unlike a post |
| `POST` | `/comments/{postId}` | Comment on post |
| `GET` | `/comments/getAllComments/{postId}` | Get all comments |
| `DELETE` | `/comments/{postId}/delete/{commentId}` | Delete comment |

**JWT token forwarding across Feign calls**

A two-part pattern propagates JWTs to downstream services without any manual header passing:

1. `JwtAuthenticationFilter` stores the raw token as `credentials` in `UsernamePasswordAuthenticationToken`
2. `FeignAuthForwardInterceptor` implements `feign.RequestInterceptor`, reads the token from `SecurityContextHolder`, and injects `Authorization: Bearer <token>` into every outgoing Feign request automatically

Token propagation is completely transparent — zero boilerplate in service code.

**Event payloads carry `creatorId`** on all three event types (post, like, comment). The Notification Service never needs a secondary DB lookup to know who to notify.

---

### Notification Service — `:9999`

The real-time delivery layer and terminal sink of the event pipeline. Consumes all domain events, persists them to PostgreSQL, and pushes them instantly to connected browser clients via SSE.

**SSE architecture**

```
Client connects → GET /notifications/stream
  → JWT validated → userId extracted from SecurityContextHolder
  → SseEmitter created with timeout = 0L (infinite)
  → Registered in ConcurrentHashMap<Long, SseEmitter>

Kafka event arrives → SendNotification.sendNotification(userId, message)
  → 1. Persist Notification to PostgreSQL    (durable, survives disconnects)
  → 2. Look up emitter for userId
       → Connected : push named SSE event "notification" immediately
       → Offline   : notification is in DB, retrievable when user returns

Memory safety — emitter lifecycle callbacks:
  onCompletion / onTimeout / onError  →  remove from registry automatically
```

**Topics consumed**

| Topic | Trigger | Who gets notified |
|---|---|---|
| `send-connection-request-topic` | Connection request sent | Request receiver |
| `accept-connection-request-topic` | Connection request accepted | Original sender |
| `post-created-topic` | Post created | All first-degree connections (fan-out via Feign) |
| `post-liked-topic` | Post liked | Post creator |
| `post-comment-topic` | Post commented on | Post creator |

> `DispatcherType.ASYNC` is explicitly permitted in `SecurityConfig` — a non-obvious but mandatory configuration for SSE + Spring Security to work without spurious 403 errors on async dispatches.

---

## Event Flow

```
USER REGISTERS
  UserService ──── [user_created] ─────────────────► ConnectionService
                                                       creates Person node in Neo4j

USER SENDS CONNECTION REQUEST
  ConnectionService creates REQUESTED_TO edge in Neo4j
  ConnectionService ── [send-connection-request-topic] ─► NotificationService
                                                            SSE push to receiver

USER ACCEPTS CONNECTION REQUEST
  ConnectionService: REQUESTED_TO → CONNECTED_TO  (atomic Cypher)
  ConnectionService ── [accept-connection-request-topic] ► NotificationService
                                                            SSE push to sender

USER CREATES A POST
  PostService saves Post to PostgreSQL
  PostService ─────── [post-created-topic] ─────────────► NotificationService
                                                            Feign → ConnectionService (fetch connections)
                                                            SSE push to each connection

USER LIKES A POST
  PostService saves PostLike to PostgreSQL
  PostService ─────── [post-liked-topic] ───────────────► NotificationService
                                                            SSE push to post creator

USER COMMENTS ON A POST
  PostService saves PostComment to PostgreSQL
  PostService ─────── [post-comment-topic] ─────────────► NotificationService
                                                            SSE push to post creator
```

---

## Security

Every service is stateless. No sessions. All identity flows through JWT.

```
STEP 1 — Login
  POST /api/v1/users/auth/login  (no auth filter on this route)
  → UserService.logIn() → BCrypt.checkpw()
  → JWT generated: subject=userId, expiry=10min, signed HMAC-SHA256
  → Returned in response body  (API / mobile clients)
  → Set as HttpOnly cookie     (browser clients — auto-sent on subsequent requests)

STEP 2 — Gateway validation
  Every authenticated request:
  → AuthenticationFilter extracts token:
      1st try: Authorization: Bearer <token>  header
      2nd try: access_token cookie            fallback
  → jwtService.validate(token) — checks signature + expiry
  → Failure → 401, request dropped
  → Success → mutates request, ensures Authorization header present → routes downstream

STEP 3 — Downstream re-validation
  Service receives forwarded request:
  → JwtAuthenticationFilter (OncePerRequestFilter) parses claims
  → Extracts userId from claims.getSubject()
  → Sets UsernamePasswordAuthenticationToken in SecurityContextHolder
      principal   = userId (String)
      credentials = raw token (Post Service only — needed for Feign)
  → SecurityUtils.getCurrentUserId() reads principal anywhere in the service

STEP 4 — Feign token propagation  (Post Service only)
  FeignAuthForwardInterceptor.apply():
  → Reads raw token from SecurityContextHolder.getAuthentication().getCredentials()
  → Injects Authorization: Bearer <token> into every Feign request
  → No manual header management anywhere in service code

STEP 5 — Internal endpoints  (Connection Service)
  /connections/core/internal/** → permitAll() in SecurityConfig
  → Trusted service-to-service calls skip JWT validation
  → Not exposed through the API Gateway
```

**Security properties across all services**
- `SessionCreationPolicy.STATELESS` — no server-side sessions
- CSRF disabled — JWT makes CSRF irrelevant in stateless APIs
- `@EnableMethodSecurity` on all services — `@PreAuthorize` ready for RBAC
- Same HMAC-SHA256 secret shared across all services — validated at both gateway and service level

---

## Databases

### User Service · PostgreSQL

```sql
users
  id          BIGSERIAL PRIMARY KEY
  name        VARCHAR   NOT NULL
  email       VARCHAR   NOT NULL UNIQUE
  password    VARCHAR   NOT NULL          -- BCrypt hash, never plaintext

outbox_events
  id             UUID      PRIMARY KEY
  aggregate_type VARCHAR                  -- "USER"
  aggregate_id   VARCHAR                  -- userId as string
  event_type     VARCHAR                  -- "UserCreatedEvent"
  payload        TEXT                     -- JSON serialized event body
  status         VARCHAR                  -- "PENDING" | "SENT"
  created_at     TIMESTAMP
```

### Post Service · PostgreSQL

```sql
posts
  id         BIGSERIAL PRIMARY KEY
  content    VARCHAR   NOT NULL
  user_id    BIGINT    NOT NULL
  created_at TIMESTAMP

posts_likes
  id         BIGSERIAL PRIMARY KEY
  user_id    BIGINT    NOT NULL
  post_id    BIGINT    NOT NULL
  created_at TIMESTAMP

post_comments
  id         BIGSERIAL PRIMARY KEY
  user_id    BIGINT    NOT NULL
  post_id    BIGINT    NOT NULL
  comment    VARCHAR   NOT NULL
  created_at TIMESTAMP
```

### Connection Service · Neo4j

```cypher
-- Node
(:Person { userId: Long, name: String, email: String })

-- Relationships
(:Person)-[:REQUESTED_TO]->(:Person)
(:Person)-[:CONNECTED_TO]->(:Person)

-- First-degree query
MATCH (a:Person {userId: $id})-[:CONNECTED_TO]-(b:Person) RETURN b
```

### Notification Service · PostgreSQL

```sql
notifications
  id         BIGSERIAL PRIMARY KEY
  user_id    BIGINT    NOT NULL
  message    VARCHAR
  created_at TIMESTAMP
```

---

## Kafka Topics

| Topic | Producer | Consumer | Event Class | Purpose |
|---|---|---|---|---|
| `user_created` | User Service | Connection Service | `UserCreatedEvents` | Sync new users into Neo4j |
| `send-connection-request-topic` | Connection Service | Notification Service | `SendConnectionRequestEvent` | Notify receiver of request |
| `accept-connection-request-topic` | Connection Service | Notification Service | `AcceptConnectionRequestEvent` | Notify sender of acceptance |
| `post-created-topic` | Post Service | Notification Service | `PostCreatedEvent` | Fan-out to all connections |
| `post-liked-topic` | Post Service | Notification Service | `PostLikedEvent` | Notify post creator |
| `post-comment-topic` | Post Service | Notification Service | `PostCommentEvent` | Notify post creator |

All event classes live in a **shared Maven module** (`org.example.events`) imported by all producers and consumers — enforcing a type-safe, versioned event contract across service boundaries.

Consumer groups: `connection-service` · `notification-service`

---

## Design Patterns

| Pattern | Where | Benefit |
|---|---|---|
| Transactional Outbox | User Service | Zero Kafka event loss — DB write and event creation are atomic |
| API Gateway | Gateway | Single entry point · centralized auth · decoupled routing |
| Filter Chain | Gateway + all services | Composable JWT validation stages, no duplication |
| Idempotent Consumer | Connection Service | Safe Kafka redelivery — no duplicate Neo4j nodes |
| Feign Request Interceptor | Post Service | Transparent JWT propagation, zero boilerplate in service code |
| Token Credential Storage | Post Service | Raw JWT in Spring Security `credentials` enables interceptor access |
| Internal Endpoint | Connection Service | Service-to-service calls without JWT overhead |
| Graph Data Model | Connection Service | O(depth) traversal vs O(n²) SQL joins at social graph scale |
| Fan-out on Event | Notification Service | Post creation triggers N notifications to all connections |
| SSE Emitter Registry | Notification Service | Thread-safe `ConcurrentHashMap` with lifecycle callbacks — no memory leaks |
| Dual-write | Notification Service | Notification persisted to DB and pushed via SSE in same call |
| Repository | All services | Spring Data abstraction over all persistence layers |
| DTO | All services | Clean boundary between API contracts and domain models |
| Service Registry | Discovery Service | Dynamic service resolution — zero hardcoded URLs |

---

## Running Locally

### Prerequisites

- Java 17+, Maven 3.8+
- Docker and Docker Compose

### Step 1 — Start infrastructure

```bash
docker-compose up -d
```

```yaml
# docker-compose.yml
version: '3.8'
services:

  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    depends_on: [zookeeper]
    ports: ["9092:9092"]
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

  neo4j:
    image: neo4j:5
    ports: ["7474:7474", "7687:7687"]
    environment:
      NEO4J_AUTH: neo4j/password

  postgres:
    image: postgres:15
    ports: ["5432:5432"]
    environment:
      POSTGRES_PASSWORD: password
    volumes:
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
```

```sql
-- init.sql
CREATE DATABASE "user-service";
CREATE DATABASE "post-service";
CREATE DATABASE "NotificationDB";
```

### Step 2 — Start services (in order)

```bash
# 1. Registry first
cd discovery-service     && mvn spring-boot:run

# 2. Core services (any order)
cd user-service          && mvn spring-boot:run &
cd connection-service    && mvn spring-boot:run &
cd post-service          && mvn spring-boot:run &
cd notification-service  && mvn spring-boot:run &

# 3. Gateway last
cd api-gateway           && mvn spring-boot:run
```

### Step 3 — Verify

Open `http://localhost:8761` — all five services should be registered:

```
✅ USER-SERVICE
✅ POST-SERVICE
✅ CONNECTION-SERVICE
✅ NOTIFICATION-SERVICE
✅ API-GATEWAY
```

---

## Environment Variables

> ⚠️ `jwt.secretKey` **must be identical** across all services. In production, use a secrets manager — never commit secrets to version control.

| Service | Variable | Default | Notes |
|---|---|---|---|
| All | `jwt.secretKey` | `sjdfsdkj...` | Shared HMAC-SHA256 signing secret |
| All | `eureka.client.service-url.defaultZone` | `http://localhost:8761/eureka` | Eureka URL |
| User, Post, Notification | `spring.datasource.url` | See `application.properties` | PostgreSQL JDBC URL per service |
| User, Post, Notification | `spring.datasource.username` | `postgres` | |
| User, Post, Notification | `spring.datasource.password` | `password` | |
| Connection | `spring.neo4j.uri` | `bolt://localhost:7687` | Neo4j Bolt protocol |
| Connection | `spring.neo4j.database` | `neo4j` | |
| Notification | `spring.kafka.bootstrap-servers` | `localhost:9092` | Kafka broker address |

---

## Scalability

Every service is **stateless by design** — JWT carries identity, no session affinity required. Services scale horizontally behind the Eureka registry with the gateway automatically load-balancing across instances.

**One known limitation:** The Notification Service stores SSE emitters in an in-memory `ConcurrentHashMap`. In a multi-instance deployment, a Kafka event consumed by Instance B cannot reach a client connected to Instance A.

**The fix:** Redis Pub/Sub. Each instance subscribes to a per-user Redis channel. Any instance that receives a Kafka event publishes to Redis; the instance holding the SSE connection delivers it. This is the only change needed to make the system fully horizontally scalable.

| Component | Scaling path |
|---|---|
| API Gateway | Stateless · scale behind any load balancer |
| User / Post Services | Stateless · Eureka round-robin across instances |
| Connection Service | Stateless · Neo4j read replicas for query scale |
| Kafka | Increase partitions to parallelize consumer throughput |
| Notification Service | Redis Pub/Sub required for multi-instance SSE delivery |
| PostgreSQL | Read replicas for read-heavy workloads |
| Neo4j | Causal Clustering for high availability |


---

<div align="center">

Built by **[Sheemab](https://github.com/sheemab7890)**

If this helped you, consider leaving a ⭐

</div>

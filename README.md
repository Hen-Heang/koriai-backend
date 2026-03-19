# KoriAI Backend

REST API backend for KoriAI — an AI-powered Korean language learning platform. Built with Spring Boot, MyBatis, PostgreSQL, and OpenAI.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Framework | Spring Boot 4.0.3 |
| Language | Java 17 |
| Database | PostgreSQL |
| ORM | MyBatis + Flyway migrations |
| Auth | JWT (stateless) |
| AI | OpenAI API (chat, TTS, corrections, vocab) |
| Deployment | Railway (Docker) |

---

## Getting Started

### Prerequisites
- Java 17+
- PostgreSQL running locally
- OpenAI API key

### Environment Variables

Create a `.env` file in the project root:

```env
OPENAI_API_KEY=sk-proj-...
DB_URL=jdbc:postgresql://localhost:5432/aitestdb
DB_USERNAME=postgres
DB_PASSWORD=123
JWT_SECRET=your-long-random-secret
```

### Run Locally

```bash
export $(cat .env | xargs) && ./mvnw spring-boot:run
```

Server starts on `http://localhost:8080`

### Run Tests

```bash
export $(cat .env | xargs) && ./mvnw test
```

---

## API Reference

**Base URL:** `http://localhost:8080/api`

> All endpoints require `Authorization: Bearer <token>` except `/auth/**`, `/health`, and `/scenarios`.

---

### Authentication `/api/auth`

| Method | Path | Body | Description |
|--------|------|------|-------------|
| POST | `/register` | `{email, password, displayName, koreanLevel}` | Create a new account |
| POST | `/login` | `{email, password}` | Returns JWT token |

**`koreanLevel` values:** `beginner`, `intermediate`, `advanced`

---

### Chat `/api/chat`

| Method | Path | Body / Params | Description |
|--------|------|--------------|-------------|
| POST | `/conversations` | `{title, conversationType, scenarioId?}` | Create a new conversation |
| GET | `/conversations` | `?limit=20&offset=0` | List user's conversations |
| GET | `/conversations/{id}/messages` | `?limit=100&offset=0` | Get messages in a conversation |
| POST | `/send` | `{conversationId, message}` | Send message — waits for full reply |
| POST | `/stream` | `{conversationId, message}` | Send message — **streaming SSE response** |

#### Streaming Response Format (`/stream`)

Returns `text/event-stream`. Events arrive as:

```
event: start
data: {"userMessageId": 1}

event: token
data: {"token": "안녕"}

event: token
data: {"token": "하세요!"}

event: done
data: {"assistantMessageId": 2}

event: error
data: {"message": "Streaming failed"}
```

---

### Corrections `/api/corrections`

| Method | Path | Body / Params | Description |
|--------|------|--------------|-------------|
| POST | `/check` | `{text}` | Check Korean grammar/spelling, returns corrections |
| GET | `/history` | `?limit=30` | User's past correction history |

**Response includes:** `correctedText`, `explanation`, `grammarPoints[]`, `changes[]` (with original/corrected/reason per change)

---

### Diary `/api/diary`

| Method | Path | Body / Params | Description |
|--------|------|--------------|-------------|
| POST | `/` | `{entryDate, originalText}` | Create or update diary entry with AI feedback |
| GET | `/` | `?month=2026-03` | Get all entries for a month |

**Response includes:** `correctedText`, `feedback`, `mood`, `grammarPoints[]`, `changes[]`

---

### Vocabulary `/api/vocab`

| Method | Path | Body / Params | Description |
|--------|------|--------------|-------------|
| GET | `/` | — | All saved vocab words |
| POST | `/save` | `{term, meaning, example?, category?}` | Save a vocab word manually |
| GET | `/review/due` | — | Cards due for spaced-repetition review |
| POST | `/{id}/review` | `?correct=true` | Mark a card as reviewed |
| POST | `/generate` | `?category=food&count=10` | AI-generate vocab cards (max 20) |

---

### Dashboard `/api/dashboard`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/progress` | Streak, weekly minutes, words saved, accuracy, daily activity chart |
| GET | `/streak` | `{streakDays, activityToday}` |

---

### Scenarios `/api/scenarios`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | List all 8 learning scenarios |
| GET | `/{id}` | Get one scenario by ID |

**Available scenarios:** Restaurant, Convenience Store, Subway, Job Interview, Doctor Visit, University, Business Negotiation, Discussing Current Events

---

### Text-to-Speech `/api/tts`

| Method | Path | Body | Description |
|--------|------|------|-------------|
| POST | `/` | `{text, voice}` | Returns `audio/mpeg` — Korean TTS via OpenAI |

**Text limit:** 500 characters

---

### Health `/api/health`

| Method | Path | Description |
|--------|------|-------------|
| GET | `/` | `{status: "UP", name, timestamp}` — public, no auth required |

---

## Data Models

### User
```
id, email, passwordHash, displayName, koreanLevel, preferredModel, createdAt, updatedAt
```

### Conversation
```
id, userId, scenarioId, title, conversationType, modelUsed, messageCount, createdAt, updatedAt
```

### Message
```
id, conversationId, role (USER | ASSISTANT), content, tokensUsed, createdAt
```

### SentenceCorrection
```
id, userId, originalText, correctedText, explanation, grammarPoints (JSON), changes (JSON), modelUsed, createdAt
```

### DiaryEntry
```
id, userId, entryDate, originalText, correctedText, feedback, mood, wordCount, grammarPoints (JSON), changes (JSON), createdAt
```

### VocabCard
```
id, userId, category, term, meaning, example, exampleTranslation, mastery (0–5), nextReviewDate, tags, createdAt
```

### ApiUsageLog
```
id, userId, model, feature, promptTokens, completionTokens, estimatedCostUsd, responseTimeMs, createdAt
```

---

## Project Structure

```
src/
├── main/
│   ├── java/com/heang/koriaibackend/
│   │   ├── ai/                    # OpenAI service + streaming
│   │   ├── common/                # API response wrapper, exceptions, utils
│   │   ├── config/                # Security, CORS, OpenAI, Jackson config
│   │   ├── domain/
│   │   │   ├── auth/              # Register + login
│   │   │   ├── chat/              # Chat conversations + SSE streaming
│   │   │   ├── conversations/     # Conversation CRUD
│   │   │   ├── correction/        # Grammar correction
│   │   │   ├── dashboard/         # Progress + streak
│   │   │   ├── diary/             # Diary entries + AI feedback
│   │   │   ├── health/            # Health check
│   │   │   ├── messages/          # Message CRUD
│   │   │   ├── scenarios/         # Hardcoded learning scenarios
│   │   │   ├── tts/               # Text-to-speech
│   │   │   ├── usage/             # API usage logging
│   │   │   ├── users/             # User profile management
│   │   │   └── vocab/             # Vocabulary + spaced repetition
│   │   └── security/              # JWT filter + utilities
│   └── resources/
│       ├── application.yaml       # App config
│       ├── db/migration/          # Flyway SQL migrations
│       └── mapper/                # MyBatis XML mappers
└── test/
```

---

## API Response Format

All endpoints return a consistent wrapper:

```json
{
  "data": { ... },
  "status": {
    "code": 0,
    "message": "Success"
  }
}
```

Error example:
```json
{
  "data": { "message": "Invalid username or password" },
  "status": {
    "code": 1001,
    "message": "Invalid username or password"
  }
}
```

---

## Deployment

Deployed on **Railway** via Docker. See `railway.toml` and `Dockerfile`.

Health check path: `/api/health`

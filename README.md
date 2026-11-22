# SkillSwap

A peer-to-peer **campus skill-barter platform**. Students teach a skill they know and
earn **time credits**, then spend those credits to learn from other students. No money involved.

Built as an academic **Java OOP project**: Java is the backend, with a plain HTML/CSS/JavaScript frontend.

## Features

- Browse skills with search and category filters
- Book a session using credits (the cost is calculated in Java)
- **Teach a skill:** host a 1-on-1 session or a group workshop and earn credits
- Switch between students to try both sides (teacher and learner)
- Wallet with earned / spent totals and booking history
- Booking rules: no double booking, 1-on-1 sessions are taken once booked, group workshops have limited spots
- Student matching by skills offered and wanted

## Tech stack

| Part | Technology |
|---|---|
| Backend | Java 21, OOP |
| REST API | Javalin 6 |
| Database | SQLite through JDBC |
| Frontend | HTML, CSS, JavaScript (calls the REST API) |
| Build | Maven (with Maven Wrapper) |
| Tests | JUnit 5 |

## How to run

1. Install JDK 21 and Maven.
2. In the project folder run:

```
mvn compile exec:exec
```

3. Open http://localhost:7070 in your browser.

The database file `skillswap.db` is created automatically with sample students and sessions.
To reset the data, stop the server, delete `skillswap.db`, and start again.

To run the tests:

```
mvn test
```

## Project structure

```
src/main/java/com/skillswap/
  model/       User, Skill, Session (abstract), OneOnOneSession, GroupWorkshop
  interfaces/  Matchable, Transactable
  exception/   InsufficientCreditsException, MatchNotFoundException
  dao/         Database, UserDao, SessionDao, BookingDao  (SQL lives only here)
  service/     WalletService, SessionService, MatchingService  (business rules)
  api/         Routes  (REST endpoints)
  Main.java    Starts the database and the web server
frontend/      index.html, css/style.css, js/app.js
src/test/      JUnit tests
```

Layers talk in one direction: **Frontend -> Routes -> Services -> DAOs -> SQLite**.

## OOP concepts used

| Concept | Where |
|---|---|
| **Abstraction** | `Session` is an abstract class with abstract `calculateCost()` and `getSessionType()` |
| **Inheritance** | `OneOnOneSession` and `GroupWorkshop` extend `Session` |
| **Polymorphism** | `calculateCost()` runs the right rule for each session type (1-on-1: 1 credit per 30 min, group: 1 credit per 60 min) |
| **Interfaces** | `User` implements `Matchable` (profile matching) and `Transactable` (credit wallet) |
| **Encapsulation** | Private fields with getters/setters and validation (for example in `User.debit()` and `User.setName()`) |
| **Custom exceptions** | `InsufficientCreditsException` (not enough credits), `MatchNotFoundException` (no teacher found) |

## REST API

| Method | URL | Purpose |
|---|---|---|
| GET | `/api/skills?userId=1` | All sessions with cost, spots left and "booked by me" |
| GET | `/api/users` | All students |
| GET | `/api/wallet/{userId}` | A student's balance |
| GET | `/api/history/{userId}` | Sessions the student learned and taught |
| GET | `/api/cost?type=ONE_ON_ONE&duration=60` | Credit cost of a session type |
| GET | `/api/match?userId=1&skill=Guitar` | Find a teacher for a skill |
| POST | `/api/sessions` | Book a session `{userId, skillId}` |
| POST | `/api/teach` | Host a new session |

Errors are returned as JSON: `{ "error": "message" }`.

## Demo script

1. Open the site as **Yasha** (5 credits) and book a session. Credits go down.
2. Keep booking until credits run out and the "Not enough credits" error shows.
3. Switch to **Asha Nair**, click **Teach a skill**, and publish a 1-on-1 session.
4. Switch back to Yasha and book it. Check Asha's wallet: her credits went up.
5. Click the credits pill to see the history.

## Team workflow

One branch per feature (`feature/<name>`), pull requests, never push directly to `main`.
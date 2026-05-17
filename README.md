# 📈 Trading System

> A trading system built with Spring Boot — featuring order management, portfolio tracking, and sector overlap analysis with robust concurrency control.

[![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.6-brightgreen?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql)](https://www.mysql.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)](LICENSE)

---

## ✨ Features

### Core Functionality
- **Order Management** — Place, fill, and cancel BUY/SELL orders
- **Portfolio Tracking** — View holdings with sector breakdown
- **Sector Overlap Analysis** — Compare portfolio against benchmark baskets with risk flags (`HIGH` / `MEDIUM` / `LOW`)
- **Direct Portfolio Additions** — Bypass the order system for admin adjustments

### Business Rules
- Maximum **3 PENDING orders** per trader at any time
- SELL orders require **sufficient existing holdings**
- Only `PENDING` orders can be filled or cancelled

### Technical Highlights
- **Concurrency Control** — Pessimistic locking (`SELECT FOR UPDATE`) prevents race conditions
- **Performance** — 2,436 req/s throughput · 253ms P95 latency (+62% after tuning)
- **Pure Java Overlap Calculator** — Zero DB calls inside business logic
- **Unit Tests** — 31 tests with Mockito covering edge cases and concurrency

---

## 🛠 Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Spring Boot | 3.2.6 | Application framework |
| Java | 17 | Runtime |
| MySQL | 8.0 | Database |
| Hibernate | 6.4.x | ORM |
| HikariCP | 5.1.x | Connection pooling |
| Swagger / OpenAPI | 2.3.0 | API documentation |
| Logback | 1.4.x | Logging with rotation |
| JUnit 5 / Mockito | Latest | Unit testing |

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- MySQL 8.0+
- Maven 3.8+

### Installation

**1. Clone the repository**
```bash
git clone https://github.com/idevkshitij/trading-system.git
cd trading-system
```

**2. Create the MySQL database**
```sql
CREATE DATABASE trading_system;
```

**3. Update database credentials** in `src/main/resources/application.yml`
```yaml
spring:
  datasource:
    username: root
    password: your_password_here
```

**4. Build and run**
```bash
mvn clean package
mvn spring-boot:run
```

**5. Open Swagger UI**
```
http://localhost:8080/swagger-ui.html
```

---

## 📡 API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/orders` | Place a new order |
| `POST` | `/orders/{orderId}/fill` | Execute a PENDING order |
| `POST` | `/orders/{orderId}/cancel` | Cancel a PENDING order |
| `GET` | `/orders/trader/{traderId}` | Get all orders for a trader |
| `GET` | `/portfolio/{traderId}` | Get portfolio holdings |
| `POST` | `/portfolio/add?traderId={traderId}` | Directly add to portfolio |
| `GET` | `/overlap/{traderId}` | Sector overlap analysis |

### API Examples

**Place an Order**
```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"traderId":"T001","stock":"AAPL","sector":"TECH","quantity":50,"side":"BUY"}'
```
```json
{
  "orderId": 1,
  "status": "PENDING",
  "message": "Order placed successfully"
}
```

**Fill an Order**
```bash
curl -X POST http://localhost:8080/orders/1/fill
```

**Get Portfolio**
```bash
curl http://localhost:8080/portfolio/T001
```
```json
{
  "traderId": "T001",
  "positions": {"AAPL": 50, "MSFT": 30},
  "sectorBreakdown": {"TECH": 80}
}
```

**Sector Overlap Analysis**
```bash
curl http://localhost:8080/overlap/T001
```
```json
{
  "overlaps": [
    {"basket": "TECH_HEAVY",    "overlap": "75.00%"},
    {"basket": "FINANCE_HEAVY", "overlap": "0.00%"},
    {"basket": "BALANCED",      "overlap": "50.00%"}
  ],
  "dominantBasket": "TECH_HEAVY",
  "riskFlag": "HIGH"
}
```

---

## 📊 Available Stocks

The system ships with 12 pre-loaded stocks across 4 sectors:

| Symbol | Name | Sector |
|---|---|---|
| AAPL | Apple Inc. | TECH |
| MSFT | Microsoft Corp | TECH |
| GOOGL | Alphabet Inc. | TECH |
| TSLA | Tesla Inc. | TECH |
| NVDA | NVIDIA Corp | TECH |
| JPM | JPMorgan Chase | FINANCE |
| GS | Goldman Sachs | FINANCE |
| BAC | Bank of America | FINANCE |
| MS | Morgan Stanley | FINANCE |
| WFC | Wells Fargo | FINANCE |
| XOM | Exxon Mobil | ENERGY |
| JNJ | Johnson & Johnson | HEALTHCARE |

---

## ⚡ Performance Results

Load test with **1,000 concurrent users** over **60 seconds**:

| Metric | Before Tuning | After Tuning | Improvement |
|---|---|---|---|
| Throughput (req/s) | 1,503 | 2,436 | **+62%** |
| Avg Latency | 241ms | 116ms | **-52%** |
| P95 Latency | 500ms | 253ms | **-49%** |
| Total Requests | 90,248 | 146,250 | **+62%** |
| Success Rate | 100% | 100% | — |

### Optimizations Applied
- HikariCP connection pool increased from **20 → 100 connections**
- Composite indexes on `orders(trader_id, status)` and `orders(trader_id, created_at)`
- JPA batching with `batch_size=50`, `order_inserts=true`, `order_updates=true`
- Native upsert query replacing find-then-save pattern

---

## 🔒 Concurrency Control

### Pessimistic Locking Strategy

`SELECT FOR UPDATE` prevents race conditions at the database level:

```sql
-- Prevent >3 pending orders
SELECT * FROM orders WHERE trader_id = ? AND status = 'PENDING' FOR UPDATE;

-- Prevent overselling
SELECT * FROM portfolio WHERE trader_id = ? AND stock_symbol = ? FOR UPDATE;
```

### Transaction Isolation

`@Transactional(isolation = Isolation.REPEATABLE_READ)` prevents phantom reads and ensures consistent count queries during order placement.

---

## 🗄 Database Schema

### Key Tables and Indexes

**`orders` table**
- Composite index: `idx_orders_trader_status (trader_id, status)` — for pending count queries
- Composite index: `idx_orders_trader_created (trader_id, created_at)` — for history queries

**`portfolio` table**
- Composite index: `idx_portfolio_trader_stock (trader_id, stock_symbol)` — for lookups
- Unique constraint: `uk_trader_stock (trader_id, stock_symbol)`

**`stocks` table**
- Pre-loaded with 12 benchmark stocks
- Index: `idx_sector (sector)`

---

## 📁 Project Structure

```
src/main/java/com/kshitij/trading/
├── TradingApplication.java
├── config/
│   └── OpenAPIConfig.java
├── controller/
│   ├── OrderController.java
│   ├── PortfolioController.java
│   └── OverlapController.java
├── domain/
│   ├── entity/         (Trader, Stock, Order, Portfolio)
│   ├── enums/          (OrderStatus, Side)
│   └── repository/     (TraderRepository, StockRepository, OrderRepository, PortfolioRepository)
├── dto/                (OrderRequest, OrderResponse, PortfolioResponse, OverlapResponse, DirectAddRequest, ErrorResponse)
├── exception/          (BusinessException, InsufficientHoldingsException, OrderNotFoundException, PendingOrderLimitException, GlobalExceptionHandler)
└── service/            (OrderService, PortfolioService, SectorOverlapService)

src/main/resources/
├── application.yml
├── data.sql
└── logback-spring.xml

src/test/java/com/kshitij/trading/service/
├── SectorOverlapServiceTest.java
├── OrderServiceTest.java
└── PortfolioServiceTest.java
```

---

## 🎯 Design Decisions

| Decision | Rationale |
|---|---|
| Pessimistic Locking | High contention on trader data; prevents race conditions without complex retry logic |
| Pure Java Overlap Calculation | No DB calls, no framework dependencies inside business logic |
| Separate Fill/Cancel Endpoints | Clear state machine; matches real trading systems |
| JPA Batching | 98% reduction in database round trips |
| Custom Exception Handler | Proper HTTP status codes (400 / 404 / 409 / 500) |
| Composite Indexes | Optimizes the most frequent query (pending order count) |
| HikariCP Connection Pool | Industry standard; tuned to 100 connections |
| DTOs for API Layer | Decouples internal entities from API contracts |

---

## 🔭 Monitoring

### Actuator Endpoints
| Endpoint | Purpose |
|---|---|
| `/actuator/health` | Application health status |
| `/actuator/metrics` | JVM and system metrics |
| `/actuator/threaddump` | Thread dumps for debugging |
| `/actuator/logfile` | View logs via HTTP |
| `/actuator/loggers` | Dynamic log level changes |

### Logging Configuration

**Log Files**
- `logs/trading-system.log` — All application logs (INFO, WARN, ERROR)
- `logs/trading-system-error.log` — ERROR level only

**Rotation Policy** — Max 100MB per file · 30-day retention · 10GB total cap

```bash
# View logs in real-time
tail -f logs/trading-system.log
```

### VisualVM Monitoring via JMX

```bash
java -Dcom.sun.management.jmxremote.port=9010 \
     -Dcom.sun.management.jmxremote.authenticate=false \
     -Dcom.sun.management.jmxremote.ssl=false \
     -jar target/trading-system-1.0.0.jar
```

Connect VisualVM to `localhost:9010` to monitor threads and memory.

---

## 🧪 Testing

### Unit Tests

```bash
mvn test
```

31 tests across three test classes:

- `SectorOverlapServiceTest` — Risk flag calculation and overlap percentages
- `OrderServiceTest` — Business rules, state machine, edge cases
- `PortfolioServiceTest` — Holdings management, direct add, locking

### Load Testing with k6

Install k6 from [k6.io](https://k6.io), then:

```bash
k6 run load-test.js
```

---

## 🔮 Future Improvements

- Read replicas for portfolio and overlap queries
- Redis caching for sector benchmark baskets
- Database partitioning for orders table by date
- Async order processing with virtual threads (Java 21+)
- Distributed tracing with Micrometer + Zipkin
- Kubernetes deployment with horizontal scaling
- Rate limiting for API protection

---

## 🛟 Troubleshooting

**MySQL Connection Error**
```bash
# Check if MySQL is running
mysql -u root -p -e "SELECT 1"
# Verify credentials in application.yml
```

**Port 8080 Already in Use**
```bash
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

**Tables Not Created**
```sql
DROP DATABASE trading_system;
CREATE DATABASE trading_system;
-- Restart the application
```

---

## 👤 Author

**Kshitij Shrivastava**
- GitHub: [@idevkshitij](https://github.com/idevkshitij)
- Email: [mail.kshitij09@gmail.com](mailto:mail.kshitij09@gmail.com)

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).

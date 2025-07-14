# Clean Architecture Order Management (Java + Spring Boot)

## 📝 Scenario
You're tasked with building and designing the core backend for a medical logistics order system. Your focus is to demonstrate clean code, test-driven development, DDD principles, and deployment awareness.

## 🎯 Objective
Build a backend service in Java using Spring Boot, TDD, and DDD. Propose how it could be deployed in a production cloud environment.

## 🛠 Functional Requirements

### Order Lifecycle Implementation

#### 1. Place Order
- Creates an order with **PENDING** status
- 🔸 Order must include at least one item

#### 2. Approve Order
- Transitions **PENDING** → **APPROVED**
- ❌ Not valid if the order is already approved or canceled

#### 3. Cancel Order
- Transitions **PENDING** → **CANCELLED**
- ❌ Not valid if the order is already approved or canceled

## ✅ Deliverables

### 1. Code
- **Web API** and backend business logic
- **In-memory repository** (no persistence needed)

### 2. Tests
- Use **TDD** to validate all domain rules
- Include **unit tests**

### 3. System Design
- Provide a **cloud-native deployment diagram** (draw.io, Markdown, or text)
- Include:
  - **Core services** (Order API, Database if added, etc.)
  - **CI/CD approach** (e.g. GitHub Actions, GitLab CI)
  - **Runtime environment** (e.g. GCP Cloud Run, AWS ECS, or Kubernetes)
  - **Monitoring/logging** (e.g. Stackdriver, Prometheus, ELK)
  - **Optional API Gateway, Auth** (can be stubbed or excluded)

# Order System - Domain Driven Design Implementation

A Spring Boot application implementing an order management system using Domain Driven Design (DDD) patterns and principles, according to these 
[requirements](REQUIREMENTS.md).
## Architecture Overview

This project follows Domain Driven Design (DDD) architecture with a clean separation of concerns across multiple layers:
```

src/main/java/com/bluedrop/order/
├── domain/                # Core business logic and domain model
├── application/           # Application services and use cases
├── infrastructure/        # Technical implementations and adapters
└── OrderApplication.java  # Main Spring Boot application
```
## Project Structure Benefits

This DDD implementation provides:

- **Separation of Concerns** - Clear boundaries between layers
- **Business Logic Isolation** - Domain logic independent of technical concerns
- **Testability** - Easy to unit test business rules
- **Maintainability** - Changes in one layer don't affect others
- **Scalability** - Easy to extend with new features
- **Domain Expertise** - Code reflects business language and concepts

This project demonstrates a clean implementation of Domain Driven Design principles in a Spring Boot application, providing a solid foundation for complex business domain modeling.


## Technical Stack
- **Java 21** - Programming language
- **Spring Boot** - Application framework
- **Lombok** - Code generation
- **JUnit 5** - Testing framework

## Key Features

### Business Capabilities
- ✅ Create new orders
- ✅ Retrieve order details
- ✅ Approve orders
- ✅ Cancel orders
- ✅ Calculate order totals
- ✅ Domain event publishing

### Technical Capabilities
- Clean architecture with DDD principles
- Business rule enforcement
- Domain event sourcing
- Input validation
- State transition management
- Unit testing setup

## Testing

The project includes comprehensive testing setup:
- **Unit Tests** - Domain logic validation
- **API Tests** - Application API context testing

Run tests with:
```bash
./mvnw test
```

## 🏃 Running the Application

1. **Prerequisites**
   - Java 21
   - Maven 3.6+

2. **Build and Run**
```shell script
./mvnw clean install
   ./mvnw spring-boot:run
```

3. **Access**
    - Application will start on `http://localhost:8080`

## Domain Details

More domain details can be found on the [domain details](DOMAIN_RULES.md) page.
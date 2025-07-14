## 📋 Domain Rules

### Order Management Rules
1. **Order Creation**
    - Must have valid OrderId and CustomerId
    - Must contain at least one item
    - Initially set to PENDING status

2. **State Transitions**
    - PENDING → APPROVED ✅
    - PENDING → CANCELLED ✅
    - APPROVED → CANCELLED ❌
    - CANCELLED → APPROVED ❌

3. **Business Invariants**
    - Orders cannot be empty
    - Price calculations must handle currency consistency
    - Domain events are published for all state changes

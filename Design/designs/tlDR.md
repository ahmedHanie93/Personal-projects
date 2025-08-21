# 🚀 ECS Fargate Production Service Review & Roadmap

## Current State: Root Cause of Previous Issues & Immediate Fix

### 1. Datadog Alerts: "Service Instances Below Threshold"

**Investigation Findings:**

- **Autoscaling Policy:** Correctly configured with `min_capacity = 3`, `max_capacity = 10`, using target tracking on
  CPU/Memory
- **Capacity Provider Strategy:** Included `FARGATE_SPOT` with `base = 1`, forcing ECS to place at least one task on
  Spot capacity
- **Core Issue:** Fargate Spot instances are interruptible and can become unavailable with ~2-minute warning
- **Impact:** While autoscaling aimed for 3 tasks, ECS could often only run 2 (or fewer) due to Spot constraints

**Root Cause Conclusion:**
The autoscaling policy was sound, but the Spot instance configuration with `base = 1` created an unreliable foundation,
preventing the service from reliably meeting its minimum task count.

### 2. Capacity Provider Strategy: Resolution

**The Immediate Fix:**
The capacity provider strategy has been simplified to use only On-Demand capacity.

- **Pull Request:** The change to remove Spot instances has been implemented and merged

**Current Configuration:**

```hcl
capacity_provider_strategy = [
  {
    capacity_provider = "FARGATE"
    weight            = 1
    base              = 1  # Currently kept for explicit intent
  }
]
```

---

## Key Concepts Glossary

| Term                              | Definition                                                                                                                         |
|-----------------------------------|------------------------------------------------------------------------------------------------------------------------------------|
| **`app_replicas`** (Our Variable) | The human-readable setting that declares the **minimum number of tasks** we want running (e.g., `3`). This is our source of truth. |
| **`min_capacity`** (Autoscaling)  | Must be set to `var.app_replicas`. This defines the lower bound for the autoscaler.                                                |
| **`desired_count`** (ECS)         | The live number of tasks ECS is trying to run. **Application Auto Scaling will manage this value after creation.**                 |

---

## Future Work: Enhancements for Reliability and Operational Excellence

### Task 1: Implement Desired Count Bootstrap & Handoff Pattern

**Problem:** Potential race condition where service creation may result in 0 tasks until autoscaler makes first
adjustment.

**Solution:**

- **Bootstrap:** Terraform must set the initial `desired_count` to `var.app_replicas` to ensure the service starts with
  a healthy number of tasks immediately.
- **Handoff:** After creation, tell Terraform to ignore all future changes to `desired_count`, as Application Auto
  Scaling will now manage it. This prevents Terraform from fighting with the autoscaler.

**Implementation:**

```hcl
# variables.tf
variable "app_replicas" {
  description = "The initial and minimum number of tasks to run"
  type        = number
  default     = 3
}

# ecs_service.tf
resource "aws_ecs_service" "app" {
  # ... other configuration ...
  desired_count = var.app_replicas  # Bootstrap the count
  
  lifecycle {
    ignore_changes = [desired_count]  # Hand off to autoscaling
  }
}

# autoscaling.tf
resource "aws_appautoscaling_target" "ecs" {
  # ... other configuration ...
  min_capacity = var.app_replicas  # Must match initial desired_count
  max_capacity = 10
}
```

### Task 2: Simplify Capacity Provider Strategy

**Problem:** The `base = 1` parameter is redundant when using a single capacity provider.

**Solution:** Remove the `base` parameter as `weight = 1` is sufficient.

**Proposed Change:**

```hcl
capacity_provider_strategy = [
  {
    capacity_provider = "FARGATE"
    weight            = 1
    # base parameter removed
  }
]
```

### Task 3: Formalize Multi-AZ Spread Placement

**Problem:** Reliance on implicit AZ spread behavior rather than explicit configuration.

**Solution:** Declare explicit placement strategy for AZ spread.

**Implementation:**

```hcl
ordered_placement_strategy {
  type  = "spread"
  field = "attribute:ecs.availability-zone"
}
```

### Task 4: Harden Deployment Configuration

**Problem:** Default deployment settings may not optimize for zero-downtime deployments or automatic rollback.

**Solution:** Implement deployment circuit breaker and configure healthy percentages.

**Implementation:**

```hcl
deployment_minimum_healthy_percent = 100  # Prevents too many tasks from being killed at once
deployment_maximum_percent         = 200  # Allows extra capacity for rolling deployments
health_check_grace_period_seconds  = 60   # Give tasks time to start before health checks begin

deployment_circuit_breaker {
  enable   = true  # Enable the circuit breaker
  rollback = true  # Automatically rollback on deployment failure
}
```

---

## Target Architecture: Consolidated Golden Pattern

The completed future work will result in this robust configuration:

```hcl
# variables.tf
variable "app_replicas" {
  description = "The initial and minimum number of tasks to run"
  type        = number
  default     = 3
}

# ecs_service.tf
resource "aws_ecs_service" "app" {
  name            = var.name
  cluster         = aws_ecs_cluster.this.id
  task_definition = aws_ecs_task_definition.app.arn

  # Capacity Providers (After Task 2)
  capacity_provider_strategy {
    capacity_provider = "FARGATE"
    weight            = 1
  }

  # Network
  network_configuration {
    subnets          = [aws_subnet.private_a.id, aws_subnet.private_b.id]
    security_groups  = [aws_security_group.app.id]
    assign_public_ip = false
  }

  # Desired Count & Autoscaling (After Task 1)
  desired_count = var.app_replicas
  lifecycle {
    ignore_changes = [desired_count]
  }

  # Placement (After Task 3)
  ordered_placement_strategy {
    type  = "spread"
    field = "attribute:ecs.availability-zone"
  }

  # Deployment Safety (After Task 4)
  deployment_minimum_healthy_percent = 100
  deployment_maximum_percent         = 200
  health_check_grace_period_seconds  = 60
  
  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }
}

# autoscaling.tf
resource "aws_appautoscaling_target" "ecs" {
  service_namespace  = "ecs"
  resource_id        = "service/${aws_ecs_cluster.this.name}/${aws_ecs_service.app.name}"
  scalable_dimension = "ecs:service:DesiredCount"
  min_capacity       = var.app_replicas
  max_capacity       = 10
}
```

### Sources

- [AWS Docs: CapacityProviderStrategyItem](https://docs.aws.amazon.com/AmazonECS/latest/APIReference/API_CapacityProviderStrategyItem.html)
- [AWS Docs: Service Definition Parameters](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/service_definition_parameters.html)
- [AWS Docs: Task Placement](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/task-placement.html)
- [AWS Docs: Deployment Circuit Breaker](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/deployment-circuit-breaker.html)
- [AWS Docs: Fargate Capacity Providers](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/fargate-capacity-providers.html)
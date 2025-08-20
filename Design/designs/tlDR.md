Golden Pattern: ECS Fargate Production Service
1. Root Cause of Previous Issues

We received Datadog alerts:

“Number of Instances for X-Service in Production is below the expected threshold.”

What we found:

Our ECS service had the correct autoscaling policy (min=3, max=10 tasks, CPU/Memory target tracking).

However, the capacity provider strategy included Spot with base = 1.

This forced ECS to always place at least one task on Spot capacity.

Since Spot is unreliable (can be reclaimed or unavailable), that task sometimes failed to start.

As a result: the autoscaling policy still wanted 3 tasks, but ECS could only place 2 (or fewer), leading to the alert.

👉 Root Cause: The autoscaling policy was fine, but the Spot base=1 guaranteed unreliable task placement.

2. Capacity Provider Strategy
   Why Spot Caused Issues

Spot is cost-efficient but not production-safe.

By setting base = 1 on Spot, ECS was forced to run one task there, even if Fargate On-Demand had capacity.

Best Practice for Production

Use only Fargate On-Demand.

base is not technically needed if you only have one provider. But keeping base = 1 improves readability, making it explicit that at least one task will always run on Fargate.


Diagram (conceptual):
ECS Service (Desired: 3 tasks)
┌───────────────────────────┐
│     Task 1 → Spot ❌       │  (fails if Spot unavailable)
│     Task 2 → Fargate ✅    │
│     Task 3 → Fargate ✅    │
└───────────────────────────┘
↓
Datadog Alert: "Running < threshold"


        ECS Service (Desired: 3–10 tasks)
        ┌───────────────────────────┐
        │ Task 1 → Fargate (AZ-A) ✅ │
        │ Task 2 → Fargate (AZ-B) ✅ │
        │ Task 3 → Fargate (AZ-A) ✅ │
        │   ... scale up to 10 ...  │
        └───────────────────────────┘
                    ↓
         Highly Available & Resilient

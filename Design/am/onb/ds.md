A great coverage report answers one question in under 30 seconds: **“Where are we exposed?”** Everything else is noise. Here’s exactly what a report should look like—pixel for pixel—to be clear, concise, and effortlessly understood.

---

## 1. The Executive Summary (top‑fold, at a glance)

Open the report and immediately see a compact table like this:

| Metric | Current | Threshold | Status |
|--------|---------|-----------|--------|
| Lines | 84.2% | 80% | 🟢 |
| Branches | 73.1% | 70% | 🟢 |
| Functions | 82.5% | 80% | 🟢 |
| **Diff (this change)** | 96.3% | 90% | 🟢 |
| Uncovered critical paths | 2 files | 0 | 🔴 |

- No pie charts, no sparklines, no “covered vs uncovered” bars that eat pixels.
- Each number is colour‑coded (green ≥ threshold, yellow within 2%, red below).
- **Only these five rows.** Shipping a feature? The diff line dominates.

Below that, a single sentence:  
*“2 critical files lack any test coverage: `CartCheckout.tsx` (branches) and `usePayment.ts` (all paths).”*  
Now every reader, from intern to VP, knows the status.

---

## 2. The “Map of the Code” – File‑Tree View

A collapsible tree of your `src/` folder. Each folder and file shows its line coverage % and a tiny inline bar (e.g. `█████ ░ 79%`). Clicking a file expands an **annotated source view**:

```tsx
// src/components/CartCheckout.tsx
 1:  ✅ const { items, total } = useCart()
 2:  ✅ if (!items.length) {
 3:  ❌   return <EmptyState />   // uncovered
 4:    }
 5:  ✅ return <CheckoutForm total={total} items={items} />
```

Colours are deliberately minimalist:
- Green (covered) – very faint background, because it’s the norm.
- Red (uncovered) – bright background on the line number and partial column. The eye snaps to it.
- Yellow (partial branch) – the line is executed but one branch isn’t. A small “⚠️ 1/2 branches” badge next to the line number.

**Mouseover the red line?** A tooltip: *“Hit 0 times. Relevant test layer(s): none.”* If E2E covers it but unit doesn’t, we still show covered, because *behaviour is verified*.

---

## 3. Layered Coverage (The “How” View)

Since you use three test types, the report must answer: *“Who’s guarding which code?”* Don’t merge and forget. Show a single number **decorated with tiny layer icons**:

![Layer icons idea: a blue "V" for Vitest, orange "P" for Playwright, green "E" for E2E]

```
CheckoutForm.tsx    87%   [ V  P  E ]   ← all three layers hit different parts
usePayment.ts      45%   [    P    ]   ← only Playwright integration tests cover it; unit and E2E miss important branches
```

A filter toggle at the top lets you instantly colour the heat‑map by layer:
- “Show me code only E2E covers” → guides test pyramid corrections.
- “Show me dead code (no layer covers)” → first priority to test or delete.

---

## 4. Uncovered List (Actionable, Not a Dump)

A classic mistake: dumping 500 uncovered lines. Instead, group them by **impact**:

**🟠 High Risk (untested branching logic in payment/auth)**
- `src/features/checkout/paymentGateway.ts` — `handle3DSecure()` (0% branch)

**🟡 Medium Risk (UI states not verified)**
- `src/components/OrderList.tsx` — empty state, error state (0%)

**🔵 Low Risk (configuration / rarely‑changing constants)**
- `src/constants/branding.ts` (excluded via config, not shown by default)

Every item in the high‑risk list is a clickable link straight to the red line. No hunting.

---

## 5. Diff Coverage (The Gatekeeper)

This section appears **only in PR/MR comments** or on the CI summary page. It’s a tiny table:

| File | Δ Lines | Covered | Missing |
|------|---------|---------|---------|
| `src/hooks/useCart.ts` | +12 | 12 (100%) | – |
| `src/utils/taxCalc.ts` | +8 | 5 (62.5%) | lines 22,24,25 |

Below the table, the missing lines are rendered inline, so a reviewer clicks once and sees the red gap. 90% threshold is enforced; if it fails, the PR is blocked with a clear message: *“Diff coverage 62.5% < 90%. See details.”*

---

## 6. The One‑Trend Sparkline

Instead of a graph, a single row:

`Coverage last 7 days: 84.2% (−0.3% from 84.5%)  Trend: ↘`

No more. If it falls >1%, an additional warning: *“Review recent commits: coverage dropped sharply in `auth/`.”* This tiny contextual clue turns a number into a conversation.

---

## 7. Clear Labels & Zero Jargon

- **Never** use “statements” — say “Lines”.
- **Never** use “functions” — say “Functions / Hooks”.
- On the merged report, a small info icon explains exactly what data sources were combined: *“Vitest unit + Playwright component + Playwright E2E (prod build instrumented).”*

All numbers are percentages with **one decimal place at most** (73.1%, not 73.12%). Human brains compare 73% and 84% instantly; decimals beyond .5 add cognitive load.

---

## 8. Report Format & Delivery

- **HTML dashboard** (Istanbul/nyc’s default, lightly styled with your brand) hosted as a CI artifact. Password‑protected, no login. One click, opens instantly.
- **PR comment** (automated) contains only the diff summary. No HTML.
- **Badge** in the README: `coverage 84%` (just the merged line %). Clicking it goes to the full report.

**What you should *never* see:**
- Raw JSON dumps.
- Mammoth tables with every file un‑filtered.
- Pie charts that waste space for the same four numbers.
- A separate report per test layer presented in isolation without a merge—missing the point.

---

In practice, the Istanbul HTML reporter already gives you a solid foundation. Customise its layout with a thin CSS overlay, and wrap it in a small CI‑generated page that adds the executive summary, diff section, and layer‑toggle. For most teams I’ve coached, this takes a day to set up, but saves tens of hours every sprint because **the report becomes a decision document, not a homework assignment**.
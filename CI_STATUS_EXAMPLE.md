# CI Build Monitor - Example with Failures

Last updated: 2025-11-14T09:28:24.102Z

## Summary

- **Total Workflows:** 7
- **✅ Successful:** 3
- **❌ Failed:** 2
- **🚫 Cancelled:** 0
- **⏱️ Timed Out:** 1

## ❌ Failed Builds - Detailed Information

### Gleam p0 CI

| Field | Value |
|-------|-------|
| **Status** | ❌ Failed |
| **Branch** | `main` |
| **Commit** | [`66ea0d0`](https://github.com/kwr14/langs/commit/66ea0d0) |
| **Message** | Improve Gleam CI workflow with formatted test output |
| **Author** | kwr14 |
| **Last Run** | 2025-11-13T22:43:48.000Z |
| **Duration** | 0m 18s |
| **Run URL** | [View Run](https://github.com/kwr14/langs/actions/runs/19348148849) |
| **Actions** | [📝 Create Issue & Assign to Author](https://github.com/kwr14/langs/issues/new?title=CI%20Failure%3A%20Gleam%20p0%20CI%20on%20main&body=%23%23%20CI%20Build%20Failure%0A%0A**Workflow%3A**%20Gleam%20p0%20CI%0A**Branch%3A**%20main%0A**Commit%3A**%2066ea0d0%20-%20Improve%20Gleam%20CI%20workflow%20with%20formatted%20test%20output%0A**Author%3A**%20%40kwr14%0A**Run%3A**%20https%3A%2F%2Fgithub.com%2Fkwr14%2Flangs%2Factions%2Fruns%2F19348148849%0A%0A%23%23%23%20Failed%20Jobs%0A-%20%5BCheck%20formatting%5D(https%3A%2F%2Fgithub.com%2Fkwr14%2Flangs%2Factions%2Fruns%2F19348148849%2Fjob%2F55352119494)%20-%20failure%0A%0A%23%23%23%20Action%20Required%0APlease%20investigate%20and%20fix%20the%20failing%20build.&assignees=kwr14) |

**Failed Jobs:**
- [Check formatting](https://github.com/kwr14/langs/actions/runs/19348148849/job/55352119494) - failure
- [Build project](https://github.com/kwr14/langs/actions/runs/19348148849/job/55352119495) - failure

---

### Python p0 CI

| Field | Value |
|-------|-------|
| **Status** | ⏱️ Timed Out |
| **Branch** | `feature/new-tests` |
| **Commit** | [`abc1234`](https://github.com/kwr14/langs/commit/abc1234) |
| **Message** | Add comprehensive test suite |
| **Author** | developer123 |
| **Last Run** | 2025-11-14T08:15:30.000Z |
| **Duration** | 60m 0s |
| **Run URL** | [View Run](https://github.com/kwr14/langs/actions/runs/19360000000) |
| **Actions** | [📝 Create Issue & Assign to Author](https://github.com/kwr14/langs/issues/new?title=CI%20Failure%3A%20Python%20p0%20CI%20on%20feature%2Fnew-tests&body=%23%23%20CI%20Build%20Failure%0A%0A**Workflow%3A**%20Python%20p0%20CI%0A**Branch%3A**%20feature%2Fnew-tests%0A**Commit%3A**%20abc1234%20-%20Add%20comprehensive%20test%20suite%0A**Author%3A**%20%40developer123%0A**Run%3A**%20https%3A%2F%2Fgithub.com%2Fkwr14%2Flangs%2Factions%2Fruns%2F19360000000%0A%0A%23%23%23%20Failed%20Jobs%0A-%20%5BRun%20tests%5D(https%3A%2F%2Fgithub.com%2Fkwr14%2Flangs%2Factions%2Fruns%2F19360000000%2Fjob%2F55352119500)%20-%20timed_out%0A%0A%23%23%23%20Action%20Required%0APlease%20investigate%20and%20fix%20the%20failing%20build.&assignees=developer123) |

**Failed Jobs:**
- [Run tests](https://github.com/kwr14/langs/actions/runs/19360000000/job/55352119500) - timed_out

---

## All Workflows

| Workflow | Status | Conclusion | Event | Last Run | SHA | Branch | Duration | URL |
|---|---|---|---|---|---|---|---|---|
| CI Build Monitor | completed | ✅ success | schedule | 2025-11-14T09:28:19.000Z | 464673b | main | 0m 5s | [link](https://github.com/kwr14/langs/actions/runs/19360229164) |
| Gleam p0 CI | completed | ❌ failure | push | 2025-11-13T22:43:48.000Z | 66ea0d0 | main | 0m 18s | [link](https://github.com/kwr14/langs/actions/runs/19348148849) |
| Monorepo Common CI | completed | ✅ success | push | 2025-11-14T09:16:19.000Z | b24b998 | main | 0m 19s | [link](https://github.com/kwr14/langs/actions/runs/19359923710) |
| Python p0 CI | completed | ⏱️ timed_out | push | 2025-11-14T08:15:30.000Z | abc1234 | feature/new-tests | 60m 0s | [link](https://github.com/kwr14/langs/actions/runs/19360000000) |
| Scala cassandra-best-practise CI | n/a | ❓ n/a | n/a | n/a | n/a | n/a | n/a | [link](https://github.com/kwr14/langs/blob/main/.github/workflows/scala-cassandra-best-practise.yml) |
| Scala durabletask CI | n/a | ❓ n/a | n/a | n/a | n/a | n/a | n/a | [link](https://github.com/kwr14/langs/blob/main/.github/workflows/scala-durabletask.yml) |
| Scala effects CI | n/a | ❓ n/a | n/a | n/a | n/a | n/a | n/a | [link](https://github.com/kwr14/langs/blob/main/.github/workflows/scala-effects.yml) |


# Automata - Workflow Management API

A durable workflow execution engine built with Scala, Cats Effect, http4s, and Cassandra.

## 📚 Documentation

- **[Architecture Guide](./ARCHITECTURE.md)** - Detailed system architecture, components, and design patterns
- **[Use Cases & Examples](./USE_CASES.md)** - Real-world use cases with complete workflow definitions
- **[Enhancement Guide](./ENHANCEMENTS.md)** - Roadmap for production-ready features and scalability

## Overview

This project demonstrates best practices for building a workflow orchestration system with:

- **Durable Execution**: Workflows persist state and can survive restarts
- **Task Dependencies**: Define task execution order with dependency graphs
- **Retry Logic**: Automatic retry with configurable max attempts
- **REST API**: http4s-based API for workflow management
- **Functional Programming**: Cats Effect for pure functional effects
- **High-Performance JSON**: jsoniter-scala for fast serialization
- **Interactive Documentation**: Swagger UI for API exploration

## Quick Architecture Overview

```
┌─────────────────┐
│   HTTP Client   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  ServerComponent│  (http4s REST API)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ WorkflowEngine  │  (Orchestration)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│PersistenceLayer │  (In-Memory / Cassandra)
└─────────────────┘
```

For detailed architecture diagrams and component descriptions, see [ARCHITECTURE.md](./ARCHITECTURE.md).

## API Documentation

The API is documented using OpenAPI 3.1 specification. See [`openapi.yaml`](./openapi.yaml) for the complete specification.

### Interactive API Documentation (Swagger UI)

The server includes an integrated Swagger UI for interactive API exploration:

- **Swagger UI**: http://localhost:8080/api-docs
- **OpenAPI Spec**: http://localhost:8080/api-docs/openapi.yaml

With Swagger UI, you can:
- 📖 Browse all API endpoints and schemas
- 🧪 Test API calls directly from your browser
- 📝 See request/response examples
- 🔍 Explore data models interactively

### Quick Start

1. **Start the server:**
   ```bash
   sbt "project core" run
   ```
   Server will start on `http://localhost:8080`

   You'll see:
   ```
   🚀 Server started on http://localhost:8080
   📖 API Documentation: http://localhost:8080/api-docs
   📝 OpenAPI Spec: http://localhost:8080/api-docs/openapi.yaml
   ```

2. **Open Swagger UI:**

   Navigate to http://localhost:8080/api-docs in your browser to explore the API interactively.

3. **Create a workflow** (using example):
   ```bash
   curl -X POST http://localhost:8080/workflows \
     -H "Content-Type: application/json" \
     -d @examples/etl-pipeline.json
   ```

4. **Get workflow status:**
   ```bash
   curl http://localhost:8080/workflows/{workflow-id}
   ```

5. **Get workflow result:**
   ```bash
   curl http://localhost:8080/workflows/{workflow-id}/result
   ```

### Example Workflows

Pre-built workflow examples are available in the `examples/` directory:

- **[etl-pipeline.json](./examples/etl-pipeline.json)** - Customer data ETL pipeline (5 tasks)
- **[video-processing.json](./examples/video-processing.json)** - Video transcoding pipeline (8 tasks, parallel execution)
- **[ml-training.json](./examples/ml-training.json)** - Machine learning model training (10 tasks)

See [USE_CASES.md](./USE_CASES.md) for detailed explanations and more examples.

### API Endpoints

#### Workflow Management

| Method | Endpoint                      | Description                          |
|--------|-------------------------------|--------------------------------------|
| POST   | `/workflows`                  | Create and start a new workflow      |
| GET    | `/workflows`                  | List all workflows                   |
| GET    | `/workflows/{id}`             | Get workflow status and tasks        |
| GET    | `/workflows/{id}/result`      | Get workflow execution result        |

#### API Documentation

| Method | Endpoint                      | Description                          |
|--------|-------------------------------|--------------------------------------|
| GET    | `/api-docs`                   | Interactive Swagger UI documentation |
| GET    | `/api-docs/openapi.yaml`      | OpenAPI 3.1 specification (YAML)     |

For detailed request/response schemas, examples, and error codes, see the [OpenAPI specification](./openapi.yaml) or browse the [interactive documentation](http://localhost:8080/api-docs).

### Testing Scripts

Two helper scripts are provided for testing:

```bash
# Comprehensive workflow test (creates workflow, checks status, gets results)
./test-workflow.sh

# Basic API endpoint test
./test-api.sh
```

## Data Models

### Workflow Definition
Defines the structure of a workflow before execution:
- **name**: Human-readable workflow name
- **taskDefinitions**: List of tasks to execute

### Task Definition
Defines a task template:
- **name**: Task name
- **taskType**: Type identifier for task executor
- **parameterTypes**: Map of parameter types
- **maxRetries**: Maximum retry attempts (default: 3)
- **dependencies**: Names of tasks that must complete first

### Workflow
Runtime representation of an executing workflow:
- **id**: Unique UUID
- **name**: Workflow name
- **status**: Pending | Running | Completed | Failed
- **tasks**: List of task instances
- **variables**: Workflow-level variables
- **createdAt/updatedAt**: Timestamps

### Task
Runtime representation of a task:
- **id**: Unique UUID
- **name**: Task name
- **status**: Pending | Running | Completed | Failed
- **taskType**: Type identifier
- **parameters**: Task-specific parameters
- **retries**: Current retry count
- **maxRetries**: Maximum retries allowed
- **dependencies**: UUIDs of dependent tasks

### Workflow Result
Final execution result:
- **workflowId**: Workflow UUID
- **taskResults**: Map of task IDs to results
- **error**: Optional error message

### Task Result
Individual task execution result:
- **taskId**: Task UUID
- **output**: Task output (any JSON value)
- **error**: Optional error message

## Tech Stack

- **Scala**: 2.13 / 3.5
- **Cats Effect**: 3.5+ (Functional effects)
- **http4s**: 0.23 (HTTP server/client)
- **Ember**: HTTP server implementation
- **jsoniter-scala**: 2.30 (High-performance JSON)
- **Cassandra**: 4.17 (Persistence - planned)

## Development

### Build

```bash
sbt compile
```

### Run

```bash
sbt "project core" run
```

### Test

```bash
sbt test
```

## Production Enhancements

This is a development/demonstration version. For production use, see [ENHANCEMENTS.md](./ENHANCEMENTS.md) for:

- ✅ Cassandra persistence (durability)
- ✅ Distributed task execution (scalability)
- ✅ Advanced retry strategies (resilience)
- ✅ Monitoring & observability (metrics, logging, tracing)
- ✅ Advanced features (conditional branching, scheduling, versioning)

## OpenAPI Validation

Validate the OpenAPI specification:

```bash
# Using Python
python3 -c "import yaml; yaml.safe_load(open('openapi.yaml')); print('✅ Valid')"

# Using npx (requires Node.js)
npx @apidevtools/swagger-cli validate openapi.yaml
```

## Contributing

Contributions are welcome! Please see the documentation guides:

- [ARCHITECTURE.md](./ARCHITECTURE.md) - Understand the system design
- [USE_CASES.md](./USE_CASES.md) - Learn from examples
- [ENHANCEMENTS.md](./ENHANCEMENTS.md) - See the roadmap

## License

Apache 2.0


# Workflow Engine - Use Cases & Examples

## 📋 Real-World Use Cases

### 1. Data Processing Pipeline

**Scenario**: ETL (Extract, Transform, Load) workflow for processing customer data.

**Workflow Definition**:
```json
{
  "name": "Customer Data ETL Pipeline",
  "taskDefinitions": [
    {
      "name": "Extract from Database",
      "taskType": "database_extract",
      "parameterTypes": {
        "source": "postgresql",
        "table": "customers",
        "query": "SELECT * FROM customers WHERE updated_at > ?"
      },
      "maxRetries": 3,
      "dependencies": []
    },
    {
      "name": "Validate Data Quality",
      "taskType": "data_validation",
      "parameterTypes": {
        "rules": "email_format,phone_format,required_fields"
      },
      "maxRetries": 2,
      "dependencies": ["Extract from Database"]
    },
    {
      "name": "Transform to Standard Format",
      "taskType": "data_transform",
      "parameterTypes": {
        "format": "json",
        "schema": "customer_v2"
      },
      "maxRetries": 2,
      "dependencies": ["Validate Data Quality"]
    },
    {
      "name": "Load to Data Warehouse",
      "taskType": "database_load",
      "parameterTypes": {
        "destination": "snowflake",
        "table": "dim_customers"
      },
      "maxRetries": 5,
      "dependencies": ["Transform to Standard Format"]
    },
    {
      "name": "Update Metadata",
      "taskType": "metadata_update",
      "parameterTypes": {
        "catalog": "data_catalog",
        "lineage": "true"
      },
      "maxRetries": 2,
      "dependencies": ["Load to Data Warehouse"]
    }
  ]
}
```

**Benefits**:
- Automatic retry on transient failures
- Clear dependency chain ensures data consistency
- Durable execution survives system restarts
- Easy to monitor and debug

---

### 2. Video Processing Workflow

**Scenario**: Process uploaded videos (transcode, generate thumbnails, extract metadata).

**Workflow Definition**:
```json
{
  "name": "Video Processing Pipeline",
  "taskDefinitions": [
    {
      "name": "Download Video",
      "taskType": "s3_download",
      "parameterTypes": {
        "bucket": "uploads",
        "key": "video_id"
      },
      "maxRetries": 3,
      "dependencies": []
    },
    {
      "name": "Extract Metadata",
      "taskType": "ffprobe",
      "parameterTypes": {
        "format": "json"
      },
      "maxRetries": 2,
      "dependencies": ["Download Video"]
    },
    {
      "name": "Transcode to 1080p",
      "taskType": "ffmpeg_transcode",
      "parameterTypes": {
        "resolution": "1920x1080",
        "codec": "h264"
      },
      "maxRetries": 2,
      "dependencies": ["Download Video"]
    },
    {
      "name": "Transcode to 720p",
      "taskType": "ffmpeg_transcode",
      "parameterTypes": {
        "resolution": "1280x720",
        "codec": "h264"
      },
      "maxRetries": 2,
      "dependencies": ["Download Video"]
    },
    {
      "name": "Generate Thumbnail",
      "taskType": "thumbnail_extract",
      "parameterTypes": {
        "timestamp": "00:00:05",
        "format": "jpg"
      },
      "maxRetries": 2,
      "dependencies": ["Download Video"]
    },
    {
      "name": "Upload Processed Files",
      "taskType": "s3_upload",
      "parameterTypes": {
        "bucket": "processed-videos"
      },
      "maxRetries": 5,
      "dependencies": [
        "Transcode to 1080p",
        "Transcode to 720p",
        "Generate Thumbnail",
        "Extract Metadata"
      ]
    }
  ]
}
```

**Benefits**:
- Parallel processing (multiple transcodes run simultaneously)
- Fault tolerance (retry failed transcodes)
- Resource optimization (schedule based on dependencies)

---

### 3. E-Commerce Order Fulfillment

**Scenario**: Process customer orders from payment to shipping.

**Workflow Definition**:
```json
{
  "name": "Order Fulfillment Workflow",
  "taskDefinitions": [
    {
      "name": "Validate Payment",
      "taskType": "payment_validation",
      "parameterTypes": {
        "gateway": "stripe"
      },
      "maxRetries": 3,
      "dependencies": []
    },
    {
      "name": "Check Inventory",
      "taskType": "inventory_check",
      "parameterTypes": {
        "warehouse": "primary"
      },
      "maxRetries": 2,
      "dependencies": ["Validate Payment"]
    },
    {
      "name": "Reserve Items",
      "taskType": "inventory_reserve",
      "parameterTypes": {
        "duration": "24h"
      },
      "maxRetries": 3,
      "dependencies": ["Check Inventory"]
    },
    {
      "name": "Generate Packing Slip",
      "taskType": "document_generation",
      "parameterTypes": {
        "template": "packing_slip_v2"
      },
      "maxRetries": 2,
      "dependencies": ["Reserve Items"]
    },
    {
      "name": "Create Shipping Label",
      "taskType": "shipping_label",
      "parameterTypes": {
        "carrier": "fedex",
        "service": "ground"
      },
      "maxRetries": 3,
      "dependencies": ["Reserve Items"]
    },
    {
      "name": "Send to Warehouse",
      "taskType": "warehouse_notification",
      "parameterTypes": {
        "priority": "normal"
      },
      "maxRetries": 5,
      "dependencies": ["Generate Packing Slip", "Create Shipping Label"]
    },
    {
      "name": "Send Confirmation Email",
      "taskType": "email_notification",
      "parameterTypes": {
        "template": "order_confirmation"
      },
      "maxRetries": 3,
      "dependencies": ["Send to Warehouse"]
    }
  ]
}
```

---

### 4. Machine Learning Model Training Pipeline

**Scenario**: Train and deploy ML models with data preparation and validation.

**Workflow Definition**:
```json
{
  "name": "ML Model Training Pipeline",
  "taskDefinitions": [
    {
      "name": "Fetch Training Data",
      "taskType": "data_fetch",
      "parameterTypes": {
        "source": "feature_store",
        "dataset": "customer_churn_v3"
      },
      "maxRetries": 3,
      "dependencies": []
    },
    {
      "name": "Data Preprocessing",
      "taskType": "data_preprocessing",
      "parameterTypes": {
        "normalization": "standard_scaler",
        "encoding": "one_hot"
      },
      "maxRetries": 2,
      "dependencies": ["Fetch Training Data"]
    },
    {
      "name": "Train Model",
      "taskType": "model_training",
      "parameterTypes": {
        "algorithm": "xgboost",
        "hyperparameters": "config_v1"
      },
      "maxRetries": 1,
      "dependencies": ["Data Preprocessing"]
    },
    {
      "name": "Evaluate Model",
      "taskType": "model_evaluation",
      "parameterTypes": {
        "metrics": "accuracy,precision,recall,f1"
      },
      "maxRetries": 2,
      "dependencies": ["Train Model"]
    },
    {
      "name": "Register Model",
      "taskType": "model_registry",
      "parameterTypes": {
        "registry": "mlflow",
        "stage": "staging"
      },
      "maxRetries": 3,
      "dependencies": ["Evaluate Model"]
    }
  ]
}
```

---

## 🚀 How to Use

### Basic Usage

1. **Start the server**:
```bash
cd scala/cassandra-best-practise
sbt "project core" run
```

2. **Create a workflow** (using curl):
```bash
curl -X POST http://localhost:8080/workflows \
  -H "Content-Type: application/json" \
  -d @examples/etl-pipeline.json
```

3. **Check workflow status**:
```bash
curl http://localhost:8080/workflows/{workflow-id}
```

4. **Get results**:
```bash
curl http://localhost:8080/workflows/{workflow-id}/result
```

### Using Swagger UI

1. Open http://localhost:8080/api-docs
2. Click "Try it out" on any endpoint
3. Fill in the request body
4. Click "Execute"
5. View the response

### Using Test Scripts

```bash
# Comprehensive workflow test
./test-workflow.sh

# Basic API test
./test-api.sh
```


#!/bin/bash

# Test ETL Pipeline Example
# This script tests the ETL workflow example from examples/etl-pipeline.json

set -e

BASE_URL="http://localhost:8080"

echo "🧪 Testing ETL Pipeline Workflow Example"
echo "=========================================="
echo ""

# Step 1: Create the ETL workflow
echo "📋 Step 1: Creating ETL Pipeline Workflow..."
WORKFLOW_RESPONSE=$(curl -s -X POST "$BASE_URL/workflows" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Customer Data ETL Pipeline",
    "taskDefinitions": [
      {
        "name": "Extract Customer Data",
        "taskType": "extract",
        "parameterTypes": {
          "source": "database",
          "table": "customers"
        },
        "maxRetries": 3,
        "dependencies": []
      },
      {
        "name": "Validate Data Quality",
        "taskType": "validate",
        "parameterTypes": {
          "rules": "not_null,email_format,phone_format"
        },
        "maxRetries": 2,
        "dependencies": ["Extract Customer Data"]
      },
      {
        "name": "Transform Customer Records",
        "taskType": "transform",
        "parameterTypes": {
          "operations": "normalize,enrich,deduplicate"
        },
        "maxRetries": 2,
        "dependencies": ["Validate Data Quality"]
      },
      {
        "name": "Load to Data Warehouse",
        "taskType": "load",
        "parameterTypes": {
          "destination": "warehouse",
          "table": "dim_customers"
        },
        "maxRetries": 3,
        "dependencies": ["Transform Customer Records"]
      },
      {
        "name": "Update Metadata",
        "taskType": "metadata",
        "parameterTypes": {
          "action": "update_lineage"
        },
        "maxRetries": 1,
        "dependencies": ["Load to Data Warehouse"]
      }
    ]
  }')

echo "Response: $WORKFLOW_RESPONSE"
echo ""

# Extract workflow ID
WORKFLOW_ID=$(echo "$WORKFLOW_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)

if [ -z "$WORKFLOW_ID" ]; then
  echo "❌ Failed to create workflow or extract ID"
  echo "Response was: $WORKFLOW_RESPONSE"
  exit 1
fi

echo "✅ Workflow created with ID: $WORKFLOW_ID"
echo ""

# Step 2: Get workflow status immediately
echo "📊 Step 2: Getting initial workflow status..."
curl -s "$BASE_URL/workflows/$WORKFLOW_ID" | jq '.'
echo ""

# Step 3: Wait for workflow to complete (poll until completed)
echo "⏳ Step 3: Waiting for workflow to complete..."
MAX_ATTEMPTS=10
ATTEMPT=0
STATUS="Running"

while [ "$STATUS" != "Completed" ] && [ "$STATUS" != "Failed" ] && [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
  sleep 1
  ATTEMPT=$((ATTEMPT + 1))
  WORKFLOW_STATUS=$(curl -s "$BASE_URL/workflows/$WORKFLOW_ID")
  STATUS=$(echo "$WORKFLOW_STATUS" | jq -r '.status.type' 2>/dev/null)
  echo "  Attempt $ATTEMPT: Status = $STATUS"
done

if [ "$STATUS" = "Completed" ]; then
  echo "✅ Workflow completed after $ATTEMPT seconds"
elif [ "$STATUS" = "Failed" ]; then
  echo "❌ Workflow failed"
else
  echo "⚠️  Workflow did not complete within $MAX_ATTEMPTS seconds (Status: $STATUS)"
fi
echo ""

# Step 4: Get final workflow status
echo "📊 Step 4: Getting final workflow status..."
FINAL_STATUS=$(curl -s "$BASE_URL/workflows/$WORKFLOW_ID")
echo "$FINAL_STATUS" | jq '.'
echo ""

# Step 5: Get workflow result
echo "📦 Step 5: Getting workflow result..."
RESULT_RESPONSE=$(curl -s "$BASE_URL/workflows/$WORKFLOW_ID/result")
echo "Raw response: $RESULT_RESPONSE"
if echo "$RESULT_RESPONSE" | jq . > /dev/null 2>&1; then
  echo "$RESULT_RESPONSE" | jq '.'
else
  echo "⚠️  Response is not valid JSON or result not found yet"
fi
echo ""

# Step 6: List all workflows
echo "📋 Step 6: Listing all workflows..."
curl -s "$BASE_URL/workflows" | jq '.[] | {id, name, status}'
echo ""

# Check if workflow completed
STATUS=$(echo "$FINAL_STATUS" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
if [ "$STATUS" = "Completed" ]; then
  echo "✅ ETL Pipeline workflow completed successfully!"
else
  echo "⚠️  Workflow status: $STATUS"
fi

echo ""
echo "=========================================="
echo "✅ ETL Pipeline Test Complete!"


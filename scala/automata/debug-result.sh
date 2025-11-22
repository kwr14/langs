#!/bin/bash

# Debug script to check workflow result endpoint

BASE_URL="http://localhost:8080"

echo "🔍 Debugging Workflow Result Endpoint"
echo "======================================"
echo ""

# Get the first workflow
echo "📋 Getting first workflow..."
WORKFLOW_ID=$(curl -s "$BASE_URL/workflows" | jq -r '.[0].id' 2>/dev/null)

if [ -z "$WORKFLOW_ID" ] || [ "$WORKFLOW_ID" = "null" ]; then
  echo "❌ No workflows found. Creating a simple test workflow first..."
  
  # Create a simple workflow
  WORKFLOW_RESPONSE=$(curl -s -X POST "$BASE_URL/workflows" \
    -H "Content-Type: application/json" \
    -d '{
      "name": "Simple Test",
      "taskDefinitions": [
        {
          "name": "Task 1",
          "taskType": "test",
          "parameterTypes": {"param": "value"},
          "maxRetries": 1,
          "dependencies": []
        }
      ]
    }')
  
  WORKFLOW_ID=$(echo "$WORKFLOW_RESPONSE" | jq -r '.id' 2>/dev/null)
  echo "Created workflow: $WORKFLOW_ID"
  echo ""
  
  # Wait for it to complete
  echo "⏳ Waiting 3 seconds for workflow to complete..."
  sleep 3
  echo ""
fi

echo "Using Workflow ID: $WORKFLOW_ID"
echo ""

# Check workflow status
echo "📊 Workflow Status:"
curl -s "$BASE_URL/workflows/$WORKFLOW_ID" | jq '.'
echo ""

# Check result endpoint with detailed output
echo "📦 Workflow Result Endpoint:"
echo "URL: $BASE_URL/workflows/$WORKFLOW_ID/result"
echo ""
echo "Raw Response:"
RESULT=$(curl -s -w "\nHTTP_CODE:%{http_code}" "$BASE_URL/workflows/$WORKFLOW_ID/result")
echo "$RESULT"
echo ""

# Try to parse as JSON
echo "Parsed JSON (if valid):"
echo "$RESULT" | sed 's/HTTP_CODE:.*//' | jq '.' 2>/dev/null || echo "⚠️  Not valid JSON"
echo ""

echo "======================================"


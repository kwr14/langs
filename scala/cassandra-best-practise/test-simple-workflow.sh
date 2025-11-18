#!/bin/bash

# Simple workflow test to debug result endpoint

BASE_URL="http://localhost:8080"

echo "🧪 Simple Workflow Test"
echo "======================="
echo ""

# Create a simple 1-task workflow
echo "📋 Creating simple workflow..."
WORKFLOW_RESPONSE=$(curl -s -X POST "$BASE_URL/workflows" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Simple Single Task",
    "taskDefinitions": [
      {
        "name": "Only Task",
        "taskType": "simple",
        "parameterTypes": {"key": "value"},
        "maxRetries": 1,
        "dependencies": []
      }
    ]
  }')

echo "$WORKFLOW_RESPONSE" | jq '.'
WORKFLOW_ID=$(echo "$WORKFLOW_RESPONSE" | jq -r '.id')
echo ""
echo "Workflow ID: $WORKFLOW_ID"
echo ""

# Poll for completion
echo "⏳ Waiting for completion..."
for i in {1..10}; do
  sleep 1
  STATUS=$(curl -s "$BASE_URL/workflows/$WORKFLOW_ID" | jq -r '.status.type')
  echo "  $i: Status = $STATUS"
  
  if [ "$STATUS" = "Completed" ]; then
    echo "✅ Workflow completed!"
    break
  fi
done
echo ""

# Get final workflow state
echo "📊 Final Workflow State:"
curl -s "$BASE_URL/workflows/$WORKFLOW_ID" | jq '.'
echo ""

# Check task results directly
echo "🔍 Checking task results..."
TASK_ID=$(curl -s "$BASE_URL/workflows/$WORKFLOW_ID" | jq -r '.tasks[0].id')
echo "Task ID: $TASK_ID"
echo ""

# Try to get workflow result
echo "📦 Workflow Result:"
RESULT=$(curl -s -w "\nHTTP:%{http_code}" "$BASE_URL/workflows/$WORKFLOW_ID/result")
HTTP_CODE=$(echo "$RESULT" | grep "HTTP:" | cut -d: -f2)
BODY=$(echo "$RESULT" | sed '/HTTP:/d')

echo "HTTP Code: $HTTP_CODE"
echo "Body: $BODY"

if [ "$HTTP_CODE" = "200" ]; then
  echo ""
  echo "Parsed:"
  echo "$BODY" | jq '.'
else
  echo "❌ Result endpoint returned $HTTP_CODE"
fi

echo ""
echo "======================="


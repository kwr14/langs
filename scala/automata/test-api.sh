#!/bin/bash

# Test script for Workflow Management API

BASE_URL="http://localhost:8080"

echo "🧪 Testing Workflow Management API"
echo "=================================="
echo ""

# Test 1: Check if OpenAPI spec is accessible
echo "📝 Test 1: Fetching OpenAPI Specification..."
curl -s -o /dev/null -w "HTTP Status: %{http_code}\n" "$BASE_URL/api-docs/openapi.yaml"
echo ""

# Test 2: Check if Swagger UI is accessible
echo "📖 Test 2: Checking Swagger UI..."
curl -s -o /dev/null -w "HTTP Status: %{http_code}\n" "$BASE_URL/api-docs"
echo ""

# Test 3: Create a workflow
echo "🚀 Test 3: Creating a workflow..."
WORKFLOW_JSON='{
  "name": "Test Workflow",
  "taskDefinitions": [
    {
      "name": "Task 1",
      "taskType": "example",
      "parameterTypes": {
        "param1": "string"
      },
      "maxRetries": 3,
      "dependencies": []
    }
  ]
}'

RESPONSE=$(curl -s -X POST "$BASE_URL/workflows" \
  -H "Content-Type: application/json" \
  -d "$WORKFLOW_JSON" \
  -w "\nHTTP_STATUS:%{http_code}")

HTTP_STATUS=$(echo "$RESPONSE" | grep "HTTP_STATUS" | cut -d: -f2)
BODY=$(echo "$RESPONSE" | sed '/HTTP_STATUS/d')

echo "HTTP Status: $HTTP_STATUS"
echo "Response Body:"
echo "$BODY" | jq '.' 2>/dev/null || echo "$BODY"
echo ""

# Extract workflow ID if successful
if [ "$HTTP_STATUS" = "200" ] || [ "$HTTP_STATUS" = "201" ]; then
  WORKFLOW_ID=$(echo "$BODY" | jq -r '.id' 2>/dev/null)
  
  if [ -n "$WORKFLOW_ID" ] && [ "$WORKFLOW_ID" != "null" ]; then
    echo "✅ Workflow created with ID: $WORKFLOW_ID"
    echo ""
    
    # Test 4: Get workflow by ID
    echo "🔍 Test 4: Fetching workflow by ID..."
    WORKFLOW_RESPONSE=$(curl -s "$BASE_URL/workflows/$WORKFLOW_ID")
    echo "$WORKFLOW_RESPONSE" | jq '.' 2>/dev/null || echo "Response: $WORKFLOW_RESPONSE"

    # Show workflow status
    WORKFLOW_STATUS=$(echo "$WORKFLOW_RESPONSE" | jq -r '.status' 2>/dev/null)
    if [ -n "$WORKFLOW_STATUS" ] && [ "$WORKFLOW_STATUS" != "null" ]; then
      echo "Workflow Status: $WORKFLOW_STATUS"
    fi
    echo ""

    # Wait a bit for tasks to execute
    echo "⏳ Waiting 2 seconds for tasks to execute..."
    sleep 2
    echo ""

    # Test 5: Get workflow result
    echo "📊 Test 5: Fetching workflow result..."
    RESULT_RESPONSE=$(curl -s "$BASE_URL/workflows/$WORKFLOW_ID/result")

    # Check if it's a 404 (result not ready yet)
    if echo "$RESULT_RESPONSE" | grep -q "not found"; then
      echo "⚠️  Workflow result not ready yet (tasks may still be executing)"
      echo "Response: $RESULT_RESPONSE"
    else
      echo "$RESULT_RESPONSE" | jq '.' 2>/dev/null || echo "Response: $RESULT_RESPONSE"
    fi
    echo ""
  else
    echo "⚠️  Could not extract workflow ID from response"
  fi
else
  echo "❌ Failed to create workflow"
fi

echo "=================================="
echo "✅ API Testing Complete!"


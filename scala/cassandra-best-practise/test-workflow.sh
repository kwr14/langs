#!/bin/bash

# Helper script to test workflow endpoints with a real workflow ID

BASE_URL="http://localhost:8080"

echo "🧪 Workflow API Testing Helper"
echo "==============================="
echo ""

# Step 0: List existing workflows
echo "📋 Step 0: Listing existing workflows..."
echo "GET $BASE_URL/workflows"
echo ""

EXISTING_WORKFLOWS=$(curl -s "$BASE_URL/workflows")
echo "$EXISTING_WORKFLOWS" | jq '.' 2>/dev/null || echo "$EXISTING_WORKFLOWS"

WORKFLOW_COUNT=$(echo "$EXISTING_WORKFLOWS" | jq 'length' 2>/dev/null)
if [ -n "$WORKFLOW_COUNT" ] && [ "$WORKFLOW_COUNT" != "null" ]; then
  echo ""
  echo "📊 Found $WORKFLOW_COUNT existing workflow(s)"
fi
echo ""

# Step 1: Create a workflow
echo "🚀 Step 1: Creating a new workflow..."
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

CREATE_RESPONSE=$(curl -s -X POST "$BASE_URL/workflows" \
  -H "Content-Type: application/json" \
  -d "$WORKFLOW_JSON")

echo "Response:"
echo "$CREATE_RESPONSE" | jq '.' 2>/dev/null || echo "$CREATE_RESPONSE"
echo ""

# Extract workflow ID
WORKFLOW_ID=$(echo "$CREATE_RESPONSE" | jq -r '.id' 2>/dev/null)

if [ -z "$WORKFLOW_ID" ] || [ "$WORKFLOW_ID" = "null" ]; then
  echo "❌ Failed to create workflow or extract ID"
  echo "Response was: $CREATE_RESPONSE"
  exit 1
fi

echo "✅ Workflow created with ID: $WORKFLOW_ID"
echo ""

# Step 2: Get workflow by ID
echo "🔍 Step 2: Fetching workflow by ID..."
echo "GET $BASE_URL/workflows/$WORKFLOW_ID"
echo ""

WORKFLOW_RESPONSE=$(curl -s "$BASE_URL/workflows/$WORKFLOW_ID")
echo "$WORKFLOW_RESPONSE" | jq '.' 2>/dev/null || echo "$WORKFLOW_RESPONSE"

# Extract and display status
WORKFLOW_STATUS=$(echo "$WORKFLOW_RESPONSE" | jq -r '.status' 2>/dev/null)
if [ -n "$WORKFLOW_STATUS" ] && [ "$WORKFLOW_STATUS" != "null" ]; then
  echo ""
  echo "📊 Workflow Status: $WORKFLOW_STATUS"
fi
echo ""

# Step 3: Wait for tasks to execute
echo "⏳ Step 3: Waiting 3 seconds for tasks to execute..."
sleep 3
echo ""

# Step 4: Get workflow result
echo "📊 Step 4: Fetching workflow result..."
echo "GET $BASE_URL/workflows/$WORKFLOW_ID/result"
echo ""

RESULT_RESPONSE=$(curl -s "$BASE_URL/workflows/$WORKFLOW_ID/result")

# Check if it's a 404 (result not ready yet)
if echo "$RESULT_RESPONSE" | grep -q "not found"; then
  echo "⚠️  Workflow result not ready yet"
  echo "Response: $RESULT_RESPONSE"
  echo ""
  echo "💡 Tip: Tasks may still be executing. Try checking again:"
  echo "   curl -s $BASE_URL/workflows/$WORKFLOW_ID/result | jq '.'"
else
  echo "$RESULT_RESPONSE" | jq '.' 2>/dev/null || echo "$RESULT_RESPONSE"
  
  # Check if result has task results
  TASK_COUNT=$(echo "$RESULT_RESPONSE" | jq '.taskResults | length' 2>/dev/null)
  if [ -n "$TASK_COUNT" ] && [ "$TASK_COUNT" != "null" ]; then
    echo ""
    echo "✅ Workflow result contains $TASK_COUNT task result(s)"
  fi
fi
echo ""

# Step 5: Check workflow status again
echo "🔄 Step 5: Checking final workflow status..."
FINAL_WORKFLOW=$(curl -s "$BASE_URL/workflows/$WORKFLOW_ID")
FINAL_STATUS=$(echo "$FINAL_WORKFLOW" | jq -r '.status' 2>/dev/null)

if [ -n "$FINAL_STATUS" ] && [ "$FINAL_STATUS" != "null" ]; then
  echo "Final Workflow Status: $FINAL_STATUS"
  
  # Show task statuses
  echo ""
  echo "Task Statuses:"
  echo "$FINAL_WORKFLOW" | jq -r '.tasks[] | "  - \(.name): \(.status)"' 2>/dev/null || echo "  Could not parse task statuses"
fi
echo ""

echo "==============================="
echo "✅ Testing Complete!"
echo ""
echo "📝 Workflow ID for manual testing: $WORKFLOW_ID"
echo ""
echo "💡 Manual test commands:"
echo "   curl -s $BASE_URL/workflows/$WORKFLOW_ID | jq '.'"
echo "   curl -s $BASE_URL/workflows/$WORKFLOW_ID/result | jq '.'"


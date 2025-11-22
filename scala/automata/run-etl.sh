#!/bin/bash

# Script to run the CSV ETL pipeline

echo "🚀 Running CSV ETL Pipeline"
echo "============================"
echo ""

# Default paths
INPUT_CSV="${1:-data/input.csv}"
OUTPUT_CSV="${2:-data/output.csv}"
DB_PATH="${3:-data/people.db}"

echo "📁 Input CSV:  $INPUT_CSV"
echo "📁 Output CSV: $OUTPUT_CSV"
echo "📁 Database:   $DB_PATH"
echo ""

# Check if input file exists
if [ ! -f "$INPUT_CSV" ]; then
  echo "❌ Error: Input file '$INPUT_CSV' not found!"
  echo ""
  echo "Usage: ./run-etl.sh [input.csv] [output.csv] [database.db]"
  exit 1
fi

# Run the ETL workflow
echo "⏳ Starting ETL workflow..."
echo ""

sbt "project core" "runMain uk.sky.etl.ETLApp $INPUT_CSV $OUTPUT_CSV $DB_PATH"

EXIT_CODE=$?

echo ""
if [ $EXIT_CODE -eq 0 ]; then
  echo "✅ ETL workflow completed successfully!"
  echo ""
  echo "📊 Results:"
  echo "  - Output CSV: $OUTPUT_CSV"
  echo "  - Database:   $DB_PATH"
  echo ""
  
  # Show sample output
  if [ -f "$OUTPUT_CSV" ]; then
    echo "📄 Sample output (first 5 rows):"
    head -n 6 "$OUTPUT_CSV"
    echo ""
  fi
  
  # Show database stats
  if [ -f "$DB_PATH" ]; then
    echo "🗄️  Database stats:"
    sqlite3 "$DB_PATH" "SELECT COUNT(*) as total_records FROM people;"
    echo ""
  fi
else
  echo "❌ ETL workflow failed with exit code $EXIT_CODE"
  exit $EXIT_CODE
fi


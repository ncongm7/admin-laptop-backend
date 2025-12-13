#!/bin/bash
# Script to remove Groq API key from git history
FILE="src/main/resources/application.properties"
if [ -f "$FILE" ]; then
    sed -i 's/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//g' "$FILE"
    sed -i 's/${GROQ_API_KEY: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}/${GROQ_API_KEY:}/g' "$FILE"
fi



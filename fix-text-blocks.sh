#!/bin/bash

# Script to fix Java text blocks (""") to Java 8 compatible string concatenation
# for all test files in jreverse-analyzer

find /workspace/JReverse/jreverse-analyzer/src/test -name "*.java" -type f | while read file; do
    echo "Processing: $file"

    # Create a temporary file for sed operations
    temp_file=$(mktemp)

    # Use sed to convert text blocks to string concatenation
    # This is a simplified conversion - may need manual adjustment for complex cases
    sed -E '
        # Convert opening text block
        s/= """/= "/g
        s/String ([a-zA-Z_][a-zA-Z0-9_]*) = """/String \1 = "/g

        # Convert closing text block
        s/^[ \t]*""";$/" ;/g

        # Convert lines in between (add quotes and concatenation)
        /= "$/,/^[ \t]*""";$/{
            /= "$/!{
                /^[ \t]*""";$/!{
                    s/^[ \t]*/"/g
                    s/$/\\n" +/g
                }
            }
        }
    ' "$file" > "$temp_file"

    # Replace original file with processed version
    mv "$temp_file" "$file"
done

echo "Text block conversion completed."
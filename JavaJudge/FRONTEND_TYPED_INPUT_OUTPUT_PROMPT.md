# Frontend Update Prompt - Typed Input/Output & Helper Functions

## Overview
Update the React frontend to support:
1. **Typed Input/Output**: Problems can have different types (int, String, long, double, float)
2. **Dynamic Method Signatures**: Method signature changes based on problem's input/output types
3. **Helper Functions**: Users can write helper methods (static or non-static) in their solutions

## API Changes

### Problem Endpoint Response
The `/api/problems/{problemId}` endpoint now returns:
```json
{
  "id": "example2",
  "title": "Double the Number",
  "description": "Double the input number",
  "difficulty": "MEDIUM",
  "inputType": "int",
  "outputType": "int"
}
```

**For String-based problems:**
```json
{
  "id": "example1",
  "title": "Sum Numbers",
  "description": "Sum the numbers",
  "difficulty": "EASY",
  "inputType": "String",
  "outputType": "String"
}
```

## Code Editor Template Updates

### 1. Dynamic Method Signature

**Before (always String):**
```java
public static String solve(String input) {
    // Write your code here
}
```

**After (dynamic based on problem types):**
```java
// For int input/output:
public static int solve(int input) {
    // Write your code here
}

// For String input/output:
public static String solve(String input) {
    // Write your code here
}

// For long input/output:
public static long solve(long input) {
    // Write your code here
}
```

### 2. Implementation Steps

#### Step 1: Fetch Problem Metadata
When user opens a problem, fetch the problem details including `inputType` and `outputType`:

```javascript
// In your problem detail component
useEffect(() => {
  const fetchProblem = async () => {
    const response = await fetch(`http://localhost:8080/api/problems/${problemId}`);
    const problem = await response.json();
    
    setProblem(problem);
    setInputType(problem.inputType || 'String');
    setOutputType(problem.outputType || 'String');
    
    // Generate method signature template
    const methodSignature = generateMethodSignature(problem.inputType, problem.outputType);
    setCodeTemplate(generateCodeTemplate(methodSignature));
  };
  
  fetchProblem();
}, [problemId]);
```

#### Step 2: Generate Method Signature
Create a helper function to generate the correct method signature:

```javascript
const generateMethodSignature = (inputType, outputType) => {
  const javaInputType = convertToJavaType(inputType);
  const javaOutputType = convertToJavaType(outputType);
  
  return `public static ${javaOutputType} solve(${javaInputType} input)`;
};

const convertToJavaType = (type) => {
  if (!type) return 'String';
  
  const normalized = type.trim();
  const lowerNormalized = normalized.toLowerCase();
  
  // Check for array types first (e.g., "int[]", "String[]")
  if (normalized.endsWith('[]')) {
    const baseType = normalized.substring(0, normalized.length - 2).trim().toLowerCase();
    let javaBaseType;
    switch (baseType) {
      case 'int':
      case 'integer':
        javaBaseType = 'int';
        break;
      case 'long':
        javaBaseType = 'long';
        break;
      case 'double':
        javaBaseType = 'double';
        break;
      case 'float':
        javaBaseType = 'float';
        break;
      case 'string':
        javaBaseType = 'String';
        break;
      default:
        javaBaseType = 'String';
    }
    return javaBaseType + '[]';
  }
  
  // Handle primitive and object types
  switch (lowerNormalized) {
    case 'int':
    case 'integer':
      return 'int';
    case 'long':
      return 'long';
    case 'double':
      return 'double';
    case 'float':
      return 'float';
    case 'string':
      return 'String';
    default:
      return 'String';
  }
};
```

#### Step 3: Generate Code Template
Create the full template with read-only method signature:

```javascript
const generateCodeTemplate = (methodSignature) => {
  return `${methodSignature} {
    // Write your code here
    
}`;
};
```

#### Step 4: Update Code Editor Component

**Option A: Monaco Editor with Read-Only Ranges**

```javascript
import Editor from '@monaco-editor/react';

const CodeEditor = ({ problem, code, onChange }) => {
  const editorRef = useRef(null);
  const methodSignature = generateMethodSignature(problem.inputType, problem.outputType);
  
  const handleEditorDidMount = (editor, monaco) => {
    editorRef.current = editor;
    
    // Set read-only ranges for method signature and closing brace
    const model = editor.getModel();
    const totalLines = model.getLineCount();
    
    // Method signature line (first line) - read-only
    editor.deltaDecorations([], [
      {
        range: new monaco.Range(1, 1, 1, methodSignature.length + 10),
        options: {
          isWholeLine: true,
          className: 'readonly-line',
          hoverMessage: { value: 'Method signature is read-only' },
          glyphMarginClassName: 'readonly-glyph'
        }
      },
      {
        range: new monaco.Range(totalLines, 1, totalLines, 2),
        options: {
          isWholeLine: true,
          className: 'readonly-line',
          hoverMessage: { value: 'Closing brace is read-only' }
        }
      }
    ]);
    
    // Prevent editing read-only lines
    editor.onDidChangeModelContent((e) => {
      const changes = e.changes;
      for (const change of changes) {
        if (change.range.startLineNumber === 1 || 
            change.range.startLineNumber === totalLines) {
          // Revert changes to read-only lines
          editor.executeEdits('prevent-edit', [{
            range: change.range,
            text: model.getValueInRange(change.range)
          }]);
        }
      }
    });
  };
  
  return (
    <Editor
      height="400px"
      language="java"
      value={code}
      onChange={onChange}
      onMount={handleEditorDidMount}
      theme="vs-dark"
      options={{
        minimap: { enabled: false },
        fontSize: 14,
        lineNumbers: 'on',
        readOnly: false,
        // Allow editing except read-only ranges
      }}
    />
  );
};
```

**Option B: Split View (Simpler Alternative)**

```javascript
const CodeEditor = ({ problem, code, onChange }) => {
  const methodSignature = generateMethodSignature(problem.inputType, problem.outputType);
  
  return (
    <div className="code-editor-container">
      {/* Read-only method signature */}
      <div className="readonly-signature">
        <code>{methodSignature} {'{'}</code>
      </div>
      
      {/* Editable code area */}
      <Editor
        height="350px"
        language="java"
        value={code}
        onChange={onChange}
        theme="vs-dark"
        options={{
          minimap: { enabled: false },
          fontSize: 14,
          lineNumbers: 'on',
        }}
      />
      
      {/* Read-only closing brace */}
      <div className="readonly-signature">
        <code>{'}'}</code>
      </div>
    </div>
  );
};
```

#### Step 5: Extract Code for Submission

When submitting, extract only the editable content (between braces):

```javascript
const handleSubmit = async () => {
  // Extract code between braces
  const codeToSubmit = extractCodeBetweenBraces(code);
  
  const response = await fetch('http://localhost:8080/api/submit', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      problemId: problem.id,
      code: codeToSubmit
    })
  });
  
  const result = await result.json();
  // Handle result...
};

const extractCodeBetweenBraces = (fullCode) => {
  // Remove method signature line
  const lines = fullCode.split('\n');
  const methodSignatureLine = lines[0];
  
  // Find opening brace
  const braceIndex = methodSignatureLine.indexOf('{');
  if (braceIndex === -1) return fullCode;
  
  // Find closing brace (last line)
  const lastLine = lines[lines.length - 1].trim();
  
  // Extract everything between first { and last }
  let codeBody = '';
  for (let i = 1; i < lines.length - 1; i++) {
    codeBody += lines[i] + '\n';
  }
  
  // Also get content after { on first line and before } on last line
  const firstLineAfterBrace = methodSignatureLine.substring(braceIndex + 1).trim();
  const lastLineBeforeBrace = lastLine.substring(0, lastLine.length - 1).trim();
  
  if (firstLineAfterBrace) {
    codeBody = firstLineAfterBrace + '\n' + codeBody;
  }
  if (lastLineBeforeBrace) {
    codeBody = codeBody + lastLineBeforeBrace;
  }
  
  return codeBody.trim();
};
```

## CSS Styling

Add styles for read-only lines:

```css
.readonly-line {
  background-color: #2d2d2d;
  color: #888;
  font-style: italic;
}

.readonly-signature {
  background-color: #1e1e1e;
  padding: 8px 12px;
  border: 1px solid #3e3e3e;
  color: #888;
  font-family: 'Courier New', monospace;
  font-size: 14px;
}

.code-editor-container {
  border: 1px solid #3e3e3e;
  border-radius: 4px;
  overflow: hidden;
}
```

## User Experience Enhancements

### 1. Type Information Display
Show the input/output types in the problem description:

```jsx
<div className="problem-info">
  <h2>{problem.title}</h2>
  <p>{problem.description}</p>
  <div className="type-info">
    <span>Input Type: <code>{problem.inputType}</code></span>
    <span>Output Type: <code>{problem.outputType}</code></span>
  </div>
</div>
```

### 2. Helper Function Examples
Show examples of helper functions in the problem description or help section:

```jsx
<div className="helper-examples">
  <h3>You can use helper functions:</h3>
  <pre>{`// Static helper
private static int helper(int x) {
    return x * 2;
}

// Non-static helper (automatically handled)
private int helper(int x) {
    return x * 2;
}

public static ${problem.outputType} solve(${problem.inputType} input) {
    return helper(input);
}`}</pre>
</div>
```

### 3. Default Code Template
When user opens a problem, initialize with template:

```javascript
const initializeCode = (problem) => {
  const methodSignature = generateMethodSignature(problem.inputType, problem.outputType);
  return `${methodSignature} {
    // Write your solution here
    
}`;
};
```

## Summary of Changes

1. **Fetch `inputType` and `outputType`** from problem API
2. **Generate dynamic method signature** based on types
3. **Update code editor template** to show correct types
4. **Extract only editable code** when submitting (between braces)
5. **Display type information** to users
6. **Support helper functions** (users can write full class with helper methods)

## Testing

Test with different problem types:
- `example1`: String input, String output
- `example2`: int input, int output
- Future problems: long, double, float types

Make sure:
- Method signature shows correct types
- Code submission extracts only method body
- Helper functions work correctly
- Read-only lines cannot be edited


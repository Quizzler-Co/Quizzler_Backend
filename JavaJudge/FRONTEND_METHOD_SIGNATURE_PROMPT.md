# Frontend Update Prompt - Multiple Real Method Signatures

## Overview
The backend now supports **multiple real method signatures** per problem. Each problem can have:
- Custom method name (not just "solve")
- Multiple parameters with different types
- Any return type

Update the React frontend to support this new flexible method signature system.

## API Changes

### Problem Endpoint Response
The `/api/problems/{problemId}` endpoint now returns:
```json
{
  "id": "example1",
  "title": "Double the Number",
  "description": "Double the input number",
  "difficulty": "MEDIUM",
  "inputType": "int",
  "outputType": "int",
  "methodName": "solve",
  "parameterTypes": "int",
  "returnType": "int"
}
```

**For problems with multiple parameters:**
```json
{
  "id": "example3",
  "title": "Add Two Numbers",
  "methodName": "add",
  "parameterTypes": "int,int",
  "returnType": "int"
}
```

**For problems with different types:**
```json
{
  "id": "example4",
  "title": "Process String",
  "methodName": "process",
  "parameterTypes": "String,int",
  "returnType": "String"
}
```

## Key Changes Required

### 1. Parse Method Signature from API

```javascript
// In your problem detail component
import { useState, useEffect, useMemo } from 'react';

const [problem, setProblem] = useState(null);
const [code, setCode] = useState("");

useEffect(() => {
  const fetchProblem = async () => {
    const response = await fetch(`http://localhost:8080/api/problems/${problemId}`);
    const problemData = await response.json();
    
    setProblem(problemData);
    
    // Generate initial template from problem data
    const methodName = problemData.methodName || "solve";
    const parameterTypes = problemData.parameterTypes 
      ? problemData.parameterTypes.split(",").map(t => t.trim())
      : (problemData.inputType ? [problemData.inputType] : ["String"]);
    const returnType = problemData.returnType || problemData.outputType || "String";
    
    const initialTemplate = generateCodeTemplate(methodName, parameterTypes, returnType);
    setCode(initialTemplate);
  };
  
  fetchProblem();
}, [problemId]);

// Reset function that regenerates template from current problem
const handleReset = () => {
  if (!problem) return;
  
  const methodName = problem.methodName || "solve";
  const parameterTypes = problem.parameterTypes 
    ? problem.parameterTypes.split(",").map(t => t.trim())
    : (problem.inputType ? [problem.inputType] : ["String"]);
  const returnType = problem.returnType || problem.outputType || "String";
  
  const resetTemplate = generateCodeTemplate(methodName, parameterTypes, returnType);
  setCode(resetTemplate);
};
```

### 2. Generate Dynamic Method Signature

```javascript
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

const generateMethodSignature = (methodName, parameterTypes, returnType) => {
  const javaReturnType = convertToJavaType(returnType);
  const javaParamTypes = parameterTypes.map(convertToJavaType);
  
  let signature = `public static ${javaReturnType} ${methodName}(`;
  
  // Add parameters with names
  javaParamTypes.forEach((type, index) => {
    if (index > 0) signature += ', ';
    signature += `${type} param${index + 1}`;
  });
  
  signature += ')';
  return signature;
};
```

### 3. Generate Code Template

```javascript
const generateCodeTemplate = (methodName, parameterTypes, returnType) => {
  const methodSignature = generateMethodSignature(methodName, parameterTypes, returnType);
  
  // Generate parameter names for user reference
  const paramNames = parameterTypes.map((_, index) => `param${index + 1}`).join(', ');
  
  return `${methodSignature} {
    // Write your solution here
    // Parameters: ${paramNames}
    
}`;
};
```

### 4. Update Code Editor Component

**Option A: Monaco Editor with Read-Only Method Signature**

```javascript
import Editor from '@monaco-editor/react';
import { useRef, useMemo, useCallback } from 'react';

const CodeEditor = ({ problem, code, onChange, onReset }) => {
  const editorRef = useRef(null);
  
  // Always extract types from problem object (not from state)
  // This ensures we always use the actual problem types, not stale state
  const methodName = problem?.methodName || "solve";
  const parameterTypes = problem?.parameterTypes 
    ? problem.parameterTypes.split(",").map(t => t.trim())
    : (problem?.inputType ? [problem.inputType] : ["String"]);
  const returnType = problem?.returnType || problem?.outputType || "String";
  
  // Generate template using useMemo to ensure it updates when problem changes
  const methodSignature = useMemo(() => 
    generateMethodSignature(methodName, parameterTypes, returnType),
    [methodName, parameterTypes.join(','), returnType]
  );
  
  const fullTemplate = useMemo(() => 
    generateCodeTemplate(methodName, parameterTypes, returnType),
    [methodName, parameterTypes.join(','), returnType]
  );
  
  // Reset handler - use the template generated from current problem
  const handleResetClick = () => {
    if (onReset) {
      onReset(fullTemplate);
    } else {
      onChange(fullTemplate);
    }
  };
  
  const handleEditorDidMount = (editor, monaco) => {
    editorRef.current = editor;
    
    const model = editor.getModel();
    const totalLines = model.getLineCount();
    
    // Method signature line (first line) - read-only
    editor.deltaDecorations([], [
      {
        range: new monaco.Range(1, 1, 1, methodSignature.length + 10),
        options: {
          isWholeLine: true,
          className: 'readonly-line',
          hoverMessage: { value: 'Method signature is read-only' }
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
  };
  
  return (
    <>
      <button onClick={handleResetClick} style={{ marginBottom: '10px' }}>
        Reset Template
      </button>
      <Editor
        height="400px"
        language="java"
        value={code || fullTemplate}
        onChange={onChange}
        onMount={handleEditorDidMount}
        theme="vs-dark"
        options={{
          minimap: { enabled: false },
          fontSize: 14,
          lineNumbers: 'on',
        }}
      />
    </>
  );
};
```

**Option B: Split View (Simpler)**

```javascript
const CodeEditor = ({ problem, code, onChange }) => {
  const methodName = problem.methodName || "solve";
  const parameterTypes = problem.parameterTypes 
    ? problem.parameterTypes.split(",").map(t => t.trim())
    : [problem.inputType || "String"];
  const returnType = problem.returnType || problem.outputType || "String";
  
  const methodSignature = generateMethodSignature(methodName, parameterTypes, returnType);
  const paramNames = parameterTypes.map((_, index) => `param${index + 1}`).join(', ');
  
  return (
    <div className="code-editor-container">
      {/* Read-only method signature */}
      <div className="readonly-signature">
        <code>{methodSignature} {'{'}</code>
        <div className="param-hint">Parameters: {paramNames}</div>
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

### 5. Extract Code for Submission

```javascript
const extractCodeBetweenBraces = (fullCode) => {
  const lines = fullCode.split('\n');
  
  // Find first line with opening brace
  let startLine = -1;
  let endLine = -1;
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    if (line.includes('{') && startLine === -1) {
      startLine = i;
      // Get content after {
      const braceIndex = line.indexOf('{');
      const afterBrace = line.substring(braceIndex + 1).trim();
      if (afterBrace) {
        // Content on same line after brace
        return extractMethodBody(lines, i, braceIndex + 1);
      }
    }
    if (line.trim() === '}' && startLine !== -1 && endLine === -1) {
      endLine = i;
      break;
    }
  }
  
  // Extract everything between first { and last }
  if (startLine !== -1 && endLine !== -1) {
    let codeBody = '';
    for (let i = startLine + 1; i < endLine; i++) {
      codeBody += lines[i] + '\n';
    }
    return codeBody.trim();
  }
  
  return fullCode;
};

const extractMethodBody = (lines, startLine, startIndex) => {
  // Find closing brace
  let braceCount = 1;
  let endLine = startLine;
  
  for (let i = startLine; i < lines.length; i++) {
    const line = lines[i];
    for (let j = (i === startLine ? startIndex : 0); j < line.length; j++) {
      if (line[j] === '{') braceCount++;
      if (line[j] === '}') {
        braceCount--;
        if (braceCount === 0) {
          endLine = i;
          let codeBody = '';
          if (i === startLine) {
            // Same line
            codeBody = line.substring(startIndex, j).trim();
          } else {
            // Multiple lines
            codeBody = line.substring(0, j).trim();
          }
          return codeBody;
        }
      }
    }
  }
  
  return '';
};

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
  
  const result = await response.json();
  // Handle result...
};
```

### 6. Display Method Information

```jsx
<div className="problem-info">
  <h2>{problem.title}</h2>
  <p>{problem.description}</p>
  
  <div className="method-signature-info">
    <h3>Method Signature:</h3>
    <code className="method-sig">
      {generateMethodSignature(
        problem.methodName || "solve",
        problem.parameterTypes 
          ? problem.parameterTypes.split(",").map(t => t.trim())
          : [problem.inputType || "String"],
        problem.returnType || problem.outputType || "String"
      )}
    </code>
    
    <div className="param-details">
      <strong>Parameters:</strong>
      <ul>
        {parameterTypes.map((type, index) => (
          <li key={index}>
            <code>param{index + 1}</code>: {convertToJavaType(type)}
          </li>
        ))}
      </ul>
      <strong>Returns:</strong> <code>{convertToJavaType(returnType)}</code>
    </div>
  </div>
</div>
```

### 7. Handle Multiple Parameters in Examples

```jsx
<div className="helper-examples">
  <h3>Example Usage:</h3>
  <pre>{`// For method: ${methodSignature}

// Example with single parameter:
public static ${convertToJavaType(returnType)} ${methodName}(${convertToJavaType(parameterTypes[0])} param1) {
    return param1 * 2;  // Example
}

// Example with multiple parameters:
public static ${convertToJavaType(returnType)} ${methodName}(${parameterTypes.map((t, i) => `${convertToJavaType(t)} param${i + 1}`).join(', ')}) {
    // Use param1, param2, etc.
    return param1 + param2;  // Example
}`}</pre>
</div>
```

## CSS Styling

```css
.readonly-signature {
  background-color: #1e1e1e;
  padding: 12px 16px;
  border: 1px solid #3e3e3e;
  color: #888;
  font-family: 'Courier New', monospace;
  font-size: 14px;
}

.readonly-signature code {
  color: #888;
  font-style: italic;
}

.param-hint {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.method-sig {
  background-color: #2d2d2d;
  padding: 8px 12px;
  border-radius: 4px;
  display: block;
  margin: 8px 0;
  color: #d4d4d4;
}

.method-signature-info {
  background-color: #252526;
  padding: 16px;
  border-radius: 4px;
  margin: 16px 0;
}

.param-details {
  margin-top: 12px;
}

.param-details ul {
  margin: 8px 0;
  padding-left: 20px;
}

.param-details code {
  background-color: #2d2d2d;
  padding: 2px 6px;
  border-radius: 3px;
  color: #ce9178;
}
```

## Summary

**Key Points:**
1. **Parse new fields**: `methodName`, `parameterTypes` (comma-separated), `returnType`
2. **Generate dynamic signature**: Based on method name, parameter types, and return type
3. **Support multiple parameters**: Split `parameterTypes` by comma and generate `param1`, `param2`, etc.
4. **Display method info**: Show signature, parameter details, and return type to users
5. **Extract code correctly**: Only send method body (between braces) to API
6. **Backward compatibility**: Fallback to `inputType`/`outputType` if new fields not present

**Example Problem Configurations:**

- **Single int parameter**: `methodName: "solve"`, `parameterTypes: "int"`, `returnType: "int"`
- **Multiple parameters**: `methodName: "add"`, `parameterTypes: "int,int"`, `returnType: "int"`
- **Mixed types**: `methodName: "process"`, `parameterTypes: "String,int"`, `returnType: "String"`
- **Custom method name**: `methodName: "calculate"`, `parameterTypes: "double,double"`, `returnType: "double"`

The backend automatically handles all method signatures, parameter parsing, and type conversion!


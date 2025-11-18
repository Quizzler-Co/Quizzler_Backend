# Frontend Update: Display Method Signature with Parameter Names

## Problem
The frontend is currently displaying the raw `description` field from the API, which contains the full statement.txt content. The method signature with parameter names (`param1`, `param2`, etc.) is not being displayed correctly on the UI.

## Solution
The backend now provides a `methodSignature` field in the API response that contains a properly formatted method signature with parameter names. The frontend should use this field instead of trying to parse the method signature from the description.

## API Response Format

When calling `/api/problems/{problemId}` or `/api/problems`, the response now includes:

```json
{
  "id": "sum-two-numbers",
  "title": "Sum Two Numbers",
  "description": "Sum Two Numbers\n\nGiven two integers, return their sum.\n\nExample:\nInput: 5, 10\nOutput: 15\n\nInput: -3, 7\nOutput: 4\n\nYour method signature:\npublic static int sum(int param1, int param2)\n",
  "difficulty": "EASY",
  "methodName": "sum",
  "parameterTypes": "int,int",
  "returnType": "int",
  "inputType": "int",
  "outputType": "int",
  "methodSignature": "public static int sum(int param1, int param2)"
}
```

## Frontend Changes Required

### 1. Use `methodSignature` Field for Display

Instead of parsing the method signature from the `description` field, use the `methodSignature` field directly:

```typescript
// ❌ DON'T DO THIS (parsing from description)
const methodSig = extractMethodSignatureFromDescription(problem.description);

// ✅ DO THIS (use the methodSignature field)
const methodSig = problem.methodSignature;
```

### 2. Display the Method Signature

```tsx
// Example React component
function ProblemDisplay({ problem }: { problem: ProblemDTO }) {
  return (
    <div>
      <h2>{problem.title}</h2>
      
      {/* Display the formatted method signature */}
      <div className="method-signature-display">
        <h3>Method Signature:</h3>
        <code className="method-sig">
          {problem.methodSignature}
        </code>
      </div>
      
      {/* Display the problem description (without the method signature part) */}
      <div className="problem-description">
        {problem.description.split('\n').map((line, idx) => (
          <p key={idx}>{line}</p>
        ))}
      </div>
    </div>
  );
}
```

### 3. Optional: Clean Up Description Display

You can optionally remove the method signature line from the description when displaying it, since it's now shown separately:

```typescript
function cleanDescription(description: string): string {
  // Remove lines that contain "Your method signature:" or "Method Signature:"
  const lines = description.split('\n');
  const cleaned = lines.filter(line => 
    !line.toLowerCase().includes('method signature') &&
    !line.trim().startsWith('public static') &&
    !line.trim().startsWith('public ')
  );
  return cleaned.join('\n');
}
```

### 4. TypeScript Interface Update

Make sure your `ProblemDTO` interface includes the `methodSignature` field:

```typescript
interface ProblemDTO {
  id: string;
  title: string;
  description: string;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  methodName: string;
  parameterTypes: string;  // e.g., "int,int"
  returnType: string;
  inputType: string;
  outputType: string;
  methodSignature: string;  // ✅ NEW FIELD - e.g., "public static int sum(int param1, int param2)"
}
```

## Example: Sum Two Numbers Problem

For the "sum-two-numbers" problem, the API returns:

```json
{
  "methodSignature": "public static int sum(int param1, int param2)"
}
```

Display this directly to the user - no parsing needed!

## Benefits

1. **No Parsing Required**: The backend provides the formatted signature ready to display
2. **Consistent Format**: All method signatures follow the same format: `public static [returnType] [methodName]([type1] param1, [type2] param2, ...)`
3. **Parameter Names Always Present**: Parameter names (`param1`, `param2`, etc.) are always included
4. **Type Safety**: The signature is generated server-side, ensuring correctness

## Testing

1. Call `/api/problems/sum-two-numbers`
2. Verify the response includes `methodSignature: "public static int sum(int param1, int param2)"`
3. Display this field directly in your UI
4. Verify parameter names (`param1`, `param2`) are visible to users


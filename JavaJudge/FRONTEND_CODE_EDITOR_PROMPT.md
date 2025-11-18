# Frontend Code Editor Update Prompt

## Important Change - Code Editor Template

The backend now extracts ONLY the code written between the method braces. Update the frontend code editor to show a fixed template where users can only edit the method body.

## Code Editor Template

**Show this EXACT template in the editor (method signature is READ-ONLY, only body is editable):**

```java
public static String solve(String input) {
    // Write your code here
    
}
```

## Requirements

### 1. Read-Only Method Signature
- Display `public static String solve(String input) {` as READ-ONLY text
- Users CANNOT edit or delete this line
- Show it in a different color/style (grayed out or with background)

### 2. Editable Code Area
- Only the area between `{` and `}` is editable
- Users write their code here with comments
- Example editable area:
```java
    // Parse the input
    int num = Integer.parseInt(input.trim());
    return String.valueOf(num * 2);
```

### 3. Read-Only Closing Brace
- Display `}` as READ-ONLY text
- Users CANNOT edit or delete this line

### 4. Visual Design
- Use a code editor component that supports read-only regions (Monaco Editor supports this)
- Or use a custom solution with:
  - Read-only lines shown in gray/disabled style
  - Editable lines in normal editor style
  - Clear visual separation

### 5. Implementation Options

**Option A: Monaco Editor with Read-Only Ranges**
```javascript
// Set read-only ranges for method signature and closing brace
editor.deltaDecorations([], [
  {
    range: new monaco.Range(1, 1, 1, 50), // Method signature line
    options: { 
      isWholeLine: true, 
      className: 'readonly-line',
      hoverMessage: { value: 'This line is read-only' }
    }
  },
  {
    range: new monaco.Range(lastLine, 1, lastLine, 2), // Closing brace
    options: { 
      isWholeLine: true, 
      className: 'readonly-line' 
    }
  }
]);
```

**Option B: Split View**
- Show method signature above editor (read-only text)
- Code editor in middle (editable)
- Show closing brace below editor (read-only text)
- Submit button combines all three parts

**Option C: Template with Placeholder**
- Pre-fill editor with template
- Use editor's readonly option for specific lines
- Or use a wrapper that prevents editing certain lines

### 6. Default Template Content

When user opens a problem, show:
```java
public static String solve(String input) {
    // Write your solution here
    
}
```

The cursor should be positioned at the comment line, ready for user to type.

### 7. Code Submission

When submitting:
- Extract ONLY the editable content (between braces)
- Remove the read-only method signature line
- Remove the read-only closing brace line
- Send just the method body to the API

**Example:**
If editor shows:
```java
public static String solve(String input) {  // READ-ONLY
    int num = Integer.parseInt(input.trim()); // EDITABLE
    return String.valueOf(num * 2);           // EDITABLE
}                                             // READ-ONLY
```

Send to API:
```json
{
  "problemId": "example2",
  "code": "int num = Integer.parseInt(input.trim());\nreturn String.valueOf(num * 2);"
}
```

### 8. User Experience

- **Clear visual indication** of what's editable vs read-only
- **Tooltip/hint** explaining: "Only edit code between the braces"
- **Auto-indentation** for code inside the method
- **Syntax highlighting** for the editable area
- **Line numbers** (optional, but helpful)

### 9. Help Text

Add a help section:
```
"Write your solution in the editable area between the braces.
You can add comments and write your logic here.
The method signature is provided automatically."
```

## Summary

**Key Points:**
1. Method signature `public static String solve(String input) {` is READ-ONLY
2. Only code between braces is EDITABLE
3. Closing brace `}` is READ-ONLY
4. Extract and send only the editable content to API
5. Clear visual distinction between read-only and editable areas

**Implementation:**
- Use Monaco Editor with read-only ranges, OR
- Split view with read-only text + editable editor, OR
- Custom solution that prevents editing specific lines

Make it clear to users that they only need to write the method body, and the signature is handled automatically!


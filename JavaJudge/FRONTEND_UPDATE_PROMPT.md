# Frontend Update Prompt - Code Submission Format

## Important Change

The backend now accepts **full method signatures with comments** instead of just method body. Update the frontend to reflect this change.

## What Changed

### Before (Old Behavior)
Users had to submit ONLY the method body:
```java
int num = Integer.parseInt(input.trim());
return String.valueOf(num * 2);
```

### Now (New Behavior)
Users can submit in **3 formats**:

1. **Full method with comments (RECOMMENDED)**:
```java
public static String solve(String input) {
    // Parse the input number
    int num = Integer.parseInt(input.trim());
    // Return double
    return String.valueOf(num * 2);
}
```

2. **Just method body (still works for backward compatibility)**:
```java
int num = Integer.parseInt(input.trim());
return String.valueOf(num * 2);
```

3. **Full class (also works)**:
```java
public class UserSolution {
    public static String solve(String input) {
        return input;
    }
}
```

## Required Frontend Updates

### 1. Code Editor Template/Placeholder

**Update the placeholder text in the code editor:**

**Old placeholder:**
```
"Write your solution here. Only submit the method body."
```

**New placeholder:**
```
"Write your solution here. You can submit:
- Full method: public static String solve(String input) { ... }
- Method body only: return input;
- Full class: public class UserSolution { ... }"
```

### 2. Code Editor Default Template

**Provide a default template when user opens a problem:**

```java
public static String solve(String input) {
    // Write your solution here
    
    return "";
}
```

This makes it natural for users to write code with comments and proper structure.

### 3. Update Instructions/Help Text

**Add a help section or tooltip explaining the submission format:**

```
You can write your solution in any of these formats:

1. Full method (recommended):
   public static String solve(String input) {
       // Your code here
       return result;
   }

2. Method body only:
   return input;

3. Full class:
   public class UserSolution {
       public static String solve(String input) {
           return input;
       }
   }
```

### 4. Code Editor Features

- **Syntax highlighting** should work for full method signatures
- **Auto-indentation** should handle the method structure properly
- **Line numbers** should be visible
- **Comments support** - users can now add comments!

### 5. Example Code Display

**Show example submissions in the problem detail page:**

```java
// Example for "Double the Number" problem:
public static String solve(String input) {
    // Parse the input as integer
    int num = Integer.parseInt(input.trim());
    
    // Calculate double
    int result = num * 2;
    
    // Return as string
    return String.valueOf(result);
}
```

## UI/UX Improvements

1. **Better code editor experience:**
   - Pre-fill with method template
   - Allow users to write naturally with comments
   - Show that full methods are supported

2. **Update help text:**
   - Remove confusing "method body only" restriction
   - Explain that full methods with comments are welcome

3. **Code examples:**
   - Show examples with comments
   - Demonstrate proper Java coding style

## No API Changes

The API endpoint remains the same:
- `POST /api/submit` with `{problemId, code}`
- Just send whatever code format the user writes
- Backend handles all formats automatically

## Summary

**Key change:** Users can now write full methods with comments, making it much more natural and less confusing. Update the frontend to:
1. Provide method template by default
2. Update placeholders/help text
3. Show examples with full methods
4. Remove restrictions about "method body only"

Make the code editor feel natural for writing Java code with proper structure and comments!


# Frontend Integration Prompt: Java Judge Service for Technical Rounds

## Overview
The Java Judge service has been integrated into the Quizzler backend through the API Gateway and Service Registry (Eureka). You need to extend your existing quiz system to support **coding questions** alongside MCQs for technical rounds. This integrates with your existing architecture: React 19, React Router 7, service layer pattern, Tailwind CSS 4, Framer Motion, and Lucide React icons.

## Integration Strategy
Extend your existing quiz system to support mixed question types:
- **MCQ Questions** (existing) - Multiple choice with options
- **Coding Questions** (new) - Code editor with Java execution

## API Gateway Base URL
All judge service endpoints are accessible through the API Gateway at:
- **Base URL**: `http://localhost:8086/api/v1/judge`
- **Authentication**: All endpoints require JWT Bearer token (same as your existing API calls)
- **Token Storage**: Use your existing token storage pattern (localStorage for "Remember Me", sessionStorage otherwise)

## Available API Endpoints

### 1. Get All Problems
**GET** `/api/v1/judge/problems`

**Query Parameters** (optional):
- `difficulty` - Filter by difficulty (EASY, MEDIUM, HARD)

**Response**: Array of ProblemDTO objects
```json
[
  {
    "id": "problem1",
    "title": "Two Sum",
    "description": "Find two numbers that add up to target",
    "difficulty": "EASY",
    "inputType": "String",
    "outputType": "String",
    "methodName": "solve",
    "parameterTypes": "String",
    "returnType": "String"
  }
]
```

### 2. Get Problem by ID
**GET** `/api/v1/judge/problems/{problemId}`

**Response**: Single ProblemDTO object (same structure as above)

### 3. Submit Code Solution
**POST** `/api/v1/judge/submit`

**Request Body**:
```json
{
  "problemId": "problem1",
  "code": "public String solve(String input) {\n    return input;\n}"
}
```

**Response**: SubmissionResponse object
```json
{
  "verdict": "ACCEPTED",
  "message": "All test cases passed",
  "expectedOutput": "expected output",
  "actualOutput": "actual output"
}
```

**Possible Verdict Values**:
- `ACCEPTED` - Solution is correct
- `WRONG_ANSWER` - Solution produces incorrect output
- `COMPILATION_ERROR` - Code has compilation errors
- `RUNTIME_ERROR` - Code throws runtime exception
- `ERROR` - General error occurred

## TypeScript/JavaScript Types/Interfaces

Extend your existing Question model to support coding questions. Add these types to your existing models/types:

```typescript
// Extend your existing Question interface/model
interface Question {
  // ... existing MCQ fields
  id: string;
  questionText: string;
  options?: string[]; // For MCQ questions
  correctAnswer?: string | number; // For MCQ questions
  type: 'MCQ' | 'CODING'; // Add question type
  points: number;
  timeLimit?: number;
  explanation?: string;
  
  // New fields for coding questions
  problemId?: string; // Reference to judge service problem
  methodName?: string;
  parameterTypes?: string;
  returnType?: string;
  inputType?: string;
  outputType?: string;
  difficulty?: 'EASY' | 'MEDIUM' | 'HARD';
}

// Judge Service Types (add to your types/models folder)
interface ProblemDTO {
  id: string;
  title: string;
  description: string;
  difficulty: 'EASY' | 'MEDIUM' | 'HARD';
  inputType: string;
  outputType: string;
  methodName: string;
  parameterTypes: string;
  returnType: string;
}

interface SubmissionRequest {
  problemId: string;
  code: string;
}

interface SubmissionResponse {
  verdict: 'ACCEPTED' | 'WRONG_ANSWER' | 'COMPILATION_ERROR' | 'RUNTIME_ERROR' | 'ERROR';
  message: string;
  expectedOutput?: string;
  actualOutput?: string;
}

// Extend your existing quiz/participation types
interface QuizAnswer {
  questionId: string;
  questionType: 'MCQ' | 'CODING';
  selectedAnswer?: string | number; // For MCQ
  code?: string; // For coding questions
  submissionResult?: SubmissionResponse; // For coding questions
}
```

## Features to Implement

### 1. Extend Quiz Creation (Admin)
**File**: Your existing quiz creation component/form

**Changes needed**:
- Add question type selector: "MCQ" or "Coding Question"
- When "Coding Question" is selected:
  - Show problem selector (fetch from `/api/v1/judge/problems`)
  - Or allow creating new problem inline
  - Display method signature preview
  - Hide MCQ-specific fields (options, correctAnswer)
- Save coding question with `type: 'CODING'` and `problemId` reference

**UI Pattern**: Use your existing form components, Tailwind styling, and Framer Motion animations

### 2. Extend Quiz Taking Experience
**File**: Your existing quiz play interface component

**Changes needed**:
- Detect question type: `question.type === 'CODING'`
- **For MCQ questions**: Show existing MCQ UI (options, radio buttons)
- **For Coding questions**: Show code editor instead of options

**Code Editor Component** (`components/CodeEditor.tsx`):
```typescript
import { Editor } from '@monaco-editor/react'; // or CodeMirror
import { motion } from 'framer-motion';
import { Play, Loader2 } from 'lucide-react';

interface CodeEditorProps {
  question: Question;
  code: string;
  onChange: (code: string) => void;
  onSubmit: (code: string) => Promise<void>;
  isSubmitting: boolean;
  submissionResult?: SubmissionResponse;
}

export const CodeEditor: React.FC<CodeEditorProps> = ({
  question,
  code,
  onChange,
  onSubmit,
  isSubmitting,
  submissionResult,
}) => {
  // Generate method signature template
  const getMethodTemplate = () => {
    if (!question.methodName) return '';
    return `public ${question.returnType} ${question.methodName}(${question.parameterTypes} input) {\n    // Your code here\n    return null;\n}`;
  };

  const handleSubmit = async () => {
    if (!question.problemId) return;
    await onSubmit(code);
  };

  return (
    <div className="flex flex-col gap-4">
      {/* Problem Description */}
      <div className="bg-white rounded-lg p-4 shadow-sm">
        <h3 className="font-semibold mb-2">{question.questionText}</h3>
        <div className="text-sm text-gray-600">
          <p><strong>Method:</strong> {question.methodName}</p>
          <p><strong>Parameters:</strong> {question.parameterTypes}</p>
          <p><strong>Return Type:</strong> {question.returnType}</p>
        </div>
      </div>

      {/* Code Editor */}
      <div className="border rounded-lg overflow-hidden">
        <Editor
          height="400px"
          defaultLanguage="java"
          value={code || getMethodTemplate()}
          onChange={(value) => onChange(value || '')}
          theme="vs-light"
          options={{
            minimap: { enabled: false },
            fontSize: 14,
            lineNumbers: 'on',
            scrollBeyondLastLine: false,
          }}
        />
      </div>

      {/* Submit Button */}
      <motion.button
        whileHover={{ scale: 1.02 }}
        whileTap={{ scale: 0.98 }}
        onClick={handleSubmit}
        disabled={isSubmitting}
        className="flex items-center justify-center gap-2 bg-blue-600 text-white px-6 py-3 rounded-lg font-medium disabled:opacity-50"
      >
        {isSubmitting ? (
          <>
            <Loader2 className="w-5 h-5 animate-spin" />
            Judging...
          </>
        ) : (
          <>
            <Play className="w-5 h-5" />
            Run Code
          </>
        )}
      </motion.button>

      {/* Submission Result */}
      {submissionResult && (
        <SubmissionResult result={submissionResult} />
      )}
    </div>
  );
};
```

**Submission Result Component** (`components/SubmissionResult.tsx`):
```typescript
import { motion } from 'framer-motion';
import { CheckCircle2, XCircle, AlertCircle, Code2 } from 'lucide-react';

const getVerdictConfig = (verdict: string) => {
  switch (verdict) {
    case 'ACCEPTED':
      return { icon: CheckCircle2, color: 'text-green-600 bg-green-50', badge: 'bg-green-100 text-green-800' };
    case 'WRONG_ANSWER':
      return { icon: XCircle, color: 'text-orange-600 bg-orange-50', badge: 'bg-orange-100 text-orange-800' };
    case 'COMPILATION_ERROR':
    case 'RUNTIME_ERROR':
    case 'ERROR':
      return { icon: AlertCircle, color: 'text-red-600 bg-red-50', badge: 'bg-red-100 text-red-800' };
    default:
      return { icon: Code2, color: 'text-gray-600 bg-gray-50', badge: 'bg-gray-100 text-gray-800' };
  }
};

export const SubmissionResult: React.FC<{ result: SubmissionResponse }> = ({ result }) => {
  const config = getVerdictConfig(result.verdict);
  const Icon = config.icon;

  return (
    <motion.div
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      className={`rounded-lg p-4 ${config.color}`}
    >
      <div className="flex items-center gap-3 mb-3">
        <Icon className="w-5 h-5" />
        <span className={`px-3 py-1 rounded-full text-sm font-medium ${config.badge}`}>
          {result.verdict}
        </span>
      </div>
      <p className="text-sm mb-3">{result.message}</p>
      
      {/* Show output comparison for wrong answers */}
      {result.verdict === 'WRONG_ANSWER' && result.expectedOutput && result.actualOutput && (
        <div className="grid grid-cols-2 gap-4 mt-4">
          <div>
            <p className="text-xs font-semibold mb-1">Expected Output:</p>
            <pre className="bg-white p-2 rounded text-xs overflow-auto">{result.expectedOutput}</pre>
          </div>
          <div>
            <p className="text-xs font-semibold mb-1">Your Output:</p>
            <pre className="bg-white p-2 rounded text-xs overflow-auto">{result.actualOutput}</pre>
          </div>
        </div>
      )}
      
      {/* Show error details */}
      {(result.verdict === 'COMPILATION_ERROR' || result.verdict === 'RUNTIME_ERROR') && result.message && (
        <div className="mt-4">
          <p className="text-xs font-semibold mb-1">Error Details:</p>
          <pre className="bg-white p-2 rounded text-xs overflow-auto text-red-600">{result.message}</pre>
        </div>
      )}
    </motion.div>
  );
};
```

### 3. Update Quiz Play Component
**File**: Your existing quiz play interface

**Integration**:
```typescript
// In your quiz play component
const [answers, setAnswers] = useState<Map<string, QuizAnswer>>(new Map());
const [submittingCode, setSubmittingCode] = useState<string | null>(null);

const handleCodeSubmit = async (questionId: string, code: string) => {
  const question = currentQuestion; // Your current question
  if (!question.problemId) return;

  setSubmittingCode(questionId);
  try {
    const result = await JudgeService.submitCode(question.problemId, code);
    
    // Update answer with code and result
    setAnswers(prev => new Map(prev).set(questionId, {
      questionId,
      questionType: 'CODING',
      code,
      submissionResult: result,
    }));

    // Show toast notification (use your existing toast system)
    if (result.verdict === 'ACCEPTED') {
      toast.success('Code accepted! All test cases passed.');
    } else {
      toast.error(`Submission failed: ${result.verdict}`);
    }
  } catch (error) {
    toast.error('Failed to submit code. Please try again.');
  } finally {
    setSubmittingCode(null);
  }
};

// In your render
{currentQuestion.type === 'CODING' ? (
  <CodeEditor
    question={currentQuestion}
    code={answers.get(currentQuestion.id)?.code || ''}
    onChange={(code) => {
      setAnswers(prev => new Map(prev).set(currentQuestion.id, {
        questionId: currentQuestion.id,
        questionType: 'CODING',
        code,
      }));
    }}
    onSubmit={(code) => handleCodeSubmit(currentQuestion.id, code)}
    isSubmitting={submittingCode === currentQuestion.id}
    submissionResult={answers.get(currentQuestion.id)?.submissionResult}
  />
) : (
  // Your existing MCQ rendering
  <MCQQuestion question={currentQuestion} ... />
)}
```

### 4. Update Results Screen
**File**: Your existing results screen component

**Changes**:
- Show coding question results with verdict badges
- Display code submitted for coding questions
- Show submission results (ACCEPTED/WRONG_ANSWER/etc.)
- Use your existing color scheme and Framer Motion animations

### 5. Code Editor Features
- Use **Monaco Editor** (`@monaco-editor/react`) or **CodeMirror**
- Syntax highlighting for Java
- Line numbers
- Auto-indentation
- Save code to localStorage as draft (optional enhancement)

## API Service Implementation

Create a new service file following your existing service layer pattern (e.g., `JudgeService.ts` or `JudgeAPIService.ts`). Match the pattern used in your `QuizService` or `QuizAPIService`:

```typescript
// services/JudgeService.ts
// Follow your existing service pattern with error handling

const API_BASE_URL = 'http://localhost:8086/api/v1/judge';

// Use your existing token retrieval pattern
const getAuthToken = (): string | null => {
  // Match your existing pattern - check localStorage first, then sessionStorage
  const rememberMe = localStorage.getItem('rememberMe') === 'true';
  if (rememberMe) {
    return localStorage.getItem('token');
  }
  return sessionStorage.getItem('token');
};

const getAuthHeaders = () => {
  const token = getAuthToken();
  return {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  };
};

// Handle errors following your existing pattern
const handleResponse = async <T>(response: Response): Promise<T> => {
  if (!response.ok) {
    if (response.status === 401) {
      // Handle unauthorized - redirect to login (match your existing pattern)
      throw new Error('Unauthorized');
    }
    if (response.status === 403) {
      throw new Error('Forbidden');
    }
    if (response.status === 404) {
      throw new Error('Not found');
    }
    const error = await response.json().catch(() => ({ message: 'An error occurred' }));
    throw new Error(error.message || 'Failed to process request');
  }
  return response.json();
};

export const JudgeService = {
  // Get all problems (for admin/problem management)
  async getAllProblems(difficulty?: string): Promise<ProblemDTO[]> {
    const url = difficulty 
      ? `${API_BASE_URL}/problems?difficulty=${difficulty}`
      : `${API_BASE_URL}/problems`;
    
    const response = await fetch(url, {
      method: 'GET',
      headers: getAuthHeaders(),
    });
    
    return handleResponse<ProblemDTO[]>(response);
  },

  // Get problem by ID
  async getProblem(problemId: string): Promise<ProblemDTO> {
    const response = await fetch(`${API_BASE_URL}/problems/${problemId}`, {
      method: 'GET',
      headers: getAuthHeaders(),
    });
    
    return handleResponse<ProblemDTO>(response);
  },

  // Submit code solution
  async submitCode(problemId: string, code: string): Promise<SubmissionResponse> {
    const response = await fetch(`${API_BASE_URL}/submit`, {
      method: 'POST',
      headers: getAuthHeaders(),
      body: JSON.stringify({
        problemId,
        code,
      }),
    });
    
    return handleResponse<SubmissionResponse>(response);
  },
};
```

## UI/UX Recommendations (Using Your Existing Patterns)

1. **Code Editor in Quiz**:
   - Match your existing quiz play interface styling
   - Use your existing progress bar, timer, and navigation
   - Integrate seamlessly with MCQ questions
   - Use your existing button styles and Framer Motion animations

2. **Result Display**:
   - Use your existing toast notification system for quick feedback
   - Show detailed results inline (below code editor)
   - Use your existing color scheme (green for success, red for errors)
   - Match your existing card/panel styling with Tailwind CSS

3. **Error Handling**:
   - Use your existing error handling pattern
   - Show user-friendly messages via toast notifications
   - Handle 401/403/404 errors (redirect to login if needed)
   - Display error states with your existing error components

4. **Loading States**:
   - Use your existing loading spinner component
   - Disable submit button while processing (match your existing pattern)
   - Show "Judging..." message with Lucide React icons
   - Use Framer Motion for smooth loading animations

5. **Question Navigator**:
   - Update your existing question navigator to show:
     - ✅ Answered (MCQ or coding with ACCEPTED)
     - ⚠️ Partially answered (coding with wrong answer)
     - ❌ Unanswered
   - Use your existing visual indicators pattern

## Example Usage Flow (Integrated with Your Quiz System)

### Admin Flow (Creating Quiz with Coding Questions):
1. Admin goes to quiz creation page (existing)
2. Admin adds questions, selects "Coding Question" type
3. Admin selects problem from judge service or creates new
4. Admin saves quiz with mixed question types (MCQ + Coding)

### User Flow (Taking Quiz with Coding Questions):
1. User starts quiz (existing flow)
2. User navigates through questions (existing navigation)
3. **For MCQ questions**: User sees existing MCQ interface
4. **For Coding questions**: 
   - User sees problem description and code editor
   - User writes code in editor
   - User clicks "Run Code" button
   - Code is submitted to `/api/v1/judge/submit`
   - Result displayed inline (ACCEPTED/WRONG_ANSWER/etc.)
   - User can modify code and resubmit
   - Question marked as answered when ACCEPTED
5. User submits quiz (existing flow)
6. Results screen shows:
   - MCQ answers (existing)
   - Coding question results with verdicts
   - Overall score calculation

## Additional Notes

- The judge service runs Java code in Docker containers for security
- Method signatures are dynamic based on the problem
- All submissions are authenticated via JWT tokens (use your existing auth pattern)
- The API Gateway handles load balancing and circuit breaking
- **Integration Points**:
  - Extend your existing `Question` model to include coding question fields
  - Extend your quiz creation form to support coding questions
  - Extend your quiz play interface to render code editor for coding questions
  - Update your results screen to show coding question results
  - Use your existing service layer pattern for API calls
  - Use your existing error handling and toast notification patterns

## Dependencies to Install

```bash
npm install @monaco-editor/react
# OR
npm install @uiw/react-codemirror @codemirror/lang-java
```

## Implementation Checklist

- [ ] Create `JudgeService.ts` following your service layer pattern
- [ ] Add TypeScript interfaces for ProblemDTO, SubmissionRequest, SubmissionResponse
- [ ] Extend Question model/interface to support coding questions
- [ ] Create `CodeEditor.tsx` component
- [ ] Create `SubmissionResult.tsx` component
- [ ] Update quiz creation form to support coding questions
- [ ] Update quiz play interface to render code editor for coding questions
- [ ] Update question navigator to show coding question status
- [ ] Update results screen to display coding question results
- [ ] Add error handling for judge service API calls
- [ ] Test with mixed quiz (MCQ + Coding questions)
- [ ] Test all verdict types (ACCEPTED, WRONG_ANSWER, COMPILATION_ERROR, etc.)

## Testing

Test with these scenarios:
1. Valid solution that passes all test cases
2. Solution with compilation errors
3. Solution with runtime errors
4. Solution with wrong output
5. Network error handling
6. Authentication error handling

---

## Quick Start Implementation Order

1. **Service Layer** (30 min)
   - Create `JudgeService.ts` matching your existing service pattern
   - Add types/interfaces

2. **Components** (2-3 hours)
   - Create `CodeEditor.tsx` component
   - Create `SubmissionResult.tsx` component
   - Use Monaco Editor or CodeMirror

3. **Integration** (2-3 hours)
   - Extend quiz creation form
   - Update quiz play interface
   - Update results screen

4. **Testing** (1 hour)
   - Test all question types
   - Test all verdict scenarios
   - Test error handling

**Total Estimated Time**: 6-7 hours

---

**Ready to implement?** Follow your existing patterns and architecture. The judge service is already integrated in the backend - you just need to connect the frontend!


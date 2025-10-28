# Manual JAR Creation Guide for User Auth Service

## Current Situation
- The Spring Boot JAR file `target/user-auth-0.0.1-SNAPSHOT.jar` doesn't exist
- Maven builds keep getting interrupted in the terminal session
- You have basic JAR files but they're missing Spring Boot dependencies

## Manual Solution

### Step 1: Open a New Command Prompt
1. Open a new Command Prompt or PowerShell window
2. Navigate to the user-auth directory:
   ```cmd
   cd D:\Quizzler\Quizzler_Backend\user-auth
   ```

### Step 2: Run Maven Build
Run this command in the new terminal:
```cmd
apache-maven-3.9.6\bin\mvn.cmd clean package -DskipTests
```

### Step 3: Verify JAR Creation
After the build completes, check if the JAR was created:
```cmd
dir target\*.jar
```

You should see: `user-auth-0.0.1-SNAPSHOT.jar`

### Step 4: Run the Spring Boot JAR
```cmd
java -jar target\user-auth-0.0.1-SNAPSHOT.jar
```

## Alternative: Use the Batch File
I've created `build-user-auth.bat` in the root directory. You can run it from anywhere:
```cmd
D:\Quizzler\Quizzler_Backend\build-user-auth.bat
```

## What This Will Create
- **File**: `target/user-auth-0.0.1-SNAPSHOT.jar`
- **Type**: Spring Boot executable JAR (fat JAR)
- **Size**: ~50-100MB (includes all dependencies)
- **Runnable**: `java -jar target/user-auth-0.0.1-SNAPSHOT.jar`

## Troubleshooting
If you get errors:
1. **Java not found**: Make sure Java 21 is in your PATH
2. **Maven not found**: Use the full path `apache-maven-3.9.6\bin\mvn.cmd`
3. **Build fails**: Check your internet connection (Maven downloads dependencies)

## Current Files Available
- `user-auth.jar` (53KB) - Basic JAR, missing dependencies
- `user-auth-executable.jar` (100KB) - Has manifest, missing dependencies
- `run-user-auth.bat` - Alternative run script

The Spring Boot JAR is the proper solution for production use.

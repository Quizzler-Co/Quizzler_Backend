# User Auth Service - JAR Creation Guide

## Problem
The JAR file created was missing the main manifest attribute and Spring Boot dependencies.

## Solutions Provided

### 1. Executable JAR with Manifest (Created)
- **File**: `user-auth-executable.jar`
- **Size**: 100KB
- **Issue**: Missing Spring Boot dependencies (NoClassDefFoundError)

### 2. Run Script (Created)
- **File**: `run-user-auth.bat`
- **Purpose**: Runs the application using classpath instead of JAR

## Recommended Solution

To create a proper Spring Boot executable JAR, you need to:

1. **Complete the Maven build** (this was interrupted):
   ```bash
   mvn clean package -DskipTests
   ```

2. **The Spring Boot Maven plugin will create**:
   - `target/user-auth-0.0.1-SNAPSHOT.jar` (executable JAR with all dependencies)

3. **Run the Spring Boot JAR**:
   ```bash
   java -jar target/user-auth-0.0.1-SNAPSHOT.jar
   ```

## Alternative: Manual Dependency Download

If Maven keeps getting interrupted, you can:

1. **Download dependencies manually**:
   ```bash
   mvn dependency:copy-dependencies
   ```

2. **Run with classpath**:
   ```bash
   java -cp "target/classes;target/dependency/*" com.userauth.UserAuthApplication
   ```

## Current Status
- ✅ Basic JAR created (`user-auth.jar`)
- ✅ Executable JAR with manifest created (`user-auth-executable.jar`)
- ✅ Run script created (`run-user-auth.bat`)
- ❌ Spring Boot fat JAR (requires complete Maven build)

The Spring Boot fat JAR is the proper solution for production deployment.

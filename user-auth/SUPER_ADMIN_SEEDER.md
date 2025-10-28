# Super Admin Seeder

## Overview
The super admin seeder automatically creates a default admin user when the application starts.

## Features
- ✅ Automatically creates super admin on application startup
- ✅ Checks for existing user to prevent duplicates
- ✅ Sets both ROLE_USER and ROLE_ADMIN roles
- ✅ Password is securely hashed using BCrypt
- ✅ Prints credentials to console for first-time setup

## Default Credentials

When you first start the application, these credentials will be printed to the console:

```
Email: admin@quizzler.com
Username: admin
Password: Admin@123
```

## How It Works

The `DataInitializer` class runs on application startup and:

1. Creates ROLE_USER and ROLE_ADMIN roles if they don't exist
2. Checks if super admin already exists
3. If not, creates a new super admin user with:
   - Name: "Super Admin"
   - Email: "admin@quizzler.com"
   - Username: "admin"
   - Password: "Admin@123" (hashed)
   - Roles: ROLE_USER + ROLE_ADMIN

## Location

The seeder code is in:
```
Quizzler_Backend/user-auth/src/main/java/com/userauth/config/DataInitializer.java
```

## First Login

After starting the application:
1. Look for the console output with super admin credentials
2. Go to your frontend login page
3. Login with:
   - Email: `admin@quizzler.com`
   - Password: `Admin@123`

## Security Notes

⚠️ **IMPORTANT:**
- Change the default password immediately after first login
- In production, change the password in the code or disable auto-creation
- The default password is for development/testing only

## Customizing the Super Admin

To change the super admin credentials, edit `DataInitializer.java`:

```java
String superAdminEmail = "your-email@example.com";
String superAdminUsername = "your-username";
String superAdminPassword = "YourSecurePassword123!";
```

## Production Deployment

For production, consider:
1. Using environment variables for credentials
2. Disabling auto-creation and manually creating admin
3. Using a more secure password generation strategy

## Manual Creation Alternative

If you prefer to create admin manually:

### Using MySQL:
```sql
-- First, get role IDs
SELECT * FROM roles;

-- Then insert admin user (use password hash from existing admin)
INSERT INTO users (name, username, email, password, created_at, updated_at)
VALUES ('Admin User', 'admin', 'admin@quizzler.com', '$2a$10$your-hashed-password', NOW(), NOW());

-- Link roles to user
INSERT INTO user_role (user_id, role_id) VALUES (LAST_INSERT_ID(), 1); -- ROLE_USER
INSERT INTO user_role (user_id, role_id) VALUES (LAST_INSERT_ID(), 2); -- ROLE_ADMIN
```

### Using Registration API:
```bash
POST http://localhost:8086/api/v1/auth/register
{
  "name": "Super Admin",
  "username": "admin",
  "email": "admin@quizzler.com",
  "password": "SecurePassword123!",
  "role": "ROLE_ADMIN"
}
```

## Troubleshooting

### "Super admin user already exists"
This is normal if you restart the application. The admin already exists in the database.

### Can't login
1. Check console output for the correct password
2. Verify password: `Admin@123`
3. Ensure user-auth service is running on port 8098

### Want to reset the admin
Delete the user from database and restart:
```sql
DELETE FROM user_role WHERE user_id IN (SELECT id FROM users WHERE email = 'admin@quizzler.com');
DELETE FROM users WHERE email = 'admin@quizzler.com';
```

## Related Files

- `DataInitializer.java` - Seed logic
- `SecurityConfig.java` - Password encoder configuration
- `User.java` - User entity
- `Role.java` - Role entity


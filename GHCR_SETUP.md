# GitHub Container Registry (GHCR) Setup

This guide explains how to build and push Docker images to GitHub Container Registry (GHCR).

## Prerequisites

1. **GitHub Account** with access to the repository
2. **Personal Access Token (PAT)** with `write:packages` permission
   - Create at: https://github.com/settings/tokens
   - Select scope: `write:packages`
3. **Docker** installed and running

## Image Naming Convention

Images will be pushed to: `ghcr.io/<username>/quizzler-backend/<service-name>:<tag>`

Example: `ghcr.io/myusername/quizzler-backend/api-gateway:latest`

## Method 1: Automated via GitHub Actions (Recommended)

### Setup

1. **Push your code to GitHub** - The workflow will automatically trigger on push to main/master
2. **Workflow file**: `.github/workflows/build-and-push.yml`

### How it Works

- **On push to main/master**: Builds and pushes all images with tags:
  - `latest` (for default branch)
  - `main-<sha>` or `master-<sha>` (commit SHA)
  - Branch name

- **On tag push (v*)**: Creates version tags:
  - `v1.0.0`
  - `1.0`
  - `1`

- **On pull request**: Builds images but doesn't push (for testing)

- **Manual trigger**: Use "Run workflow" button in GitHub Actions tab

### Viewing Images

After workflow completes:
1. Go to your GitHub repository
2. Click on "Packages" (right sidebar)
3. Or visit: `https://github.com/<username>?tab=packages`

## Method 2: Manual Push (Local)

### Using Scripts

**Linux/Mac:**
```bash
chmod +x push-to-ghcr.sh
./push-to-ghcr.sh <github-username> [tag]
```

**Windows:**
```cmd
push-to-ghcr.bat <github-username> [tag]
```

### Manual Commands

1. **Login to GHCR:**
   ```bash
   echo $GITHUB_TOKEN | docker login ghcr.io -u <username> --password-stdin
   ```

2. **Build and push each service:**
   ```bash
   # Service Registry
   docker build -t ghcr.io/<username>/quizzler-backend/service-registry:latest ./service-registry
   docker push ghcr.io/<username>/quizzler-backend/service-registry:latest

   # API Gateway
   docker build -t ghcr.io/<username>/quizzler-backend/api-gateway:latest ./api-gateway
   docker push ghcr.io/<username>/quizzler-backend/api-gateway:latest

   # User Auth
   docker build -t ghcr.io/<username>/quizzler-backend/user-auth:latest ./user-auth
   docker push ghcr.io/<username>/quizzler-backend/user-auth:latest

   # Quiz Service
   docker build -t ghcr.io/<username>/quizzler-backend/quiz-service:latest ./Quiz
   docker push ghcr.io/<username>/quizzler-backend/quiz-service:latest

   # Question Service
   docker build -t ghcr.io/<username>/quizzler-backend/question-service:latest ./question-service
   docker push ghcr.io/<username>/quizzler-backend/question-service:latest

   # Participation Service
   docker build -t ghcr.io/<username>/quizzler-backend/participation-service:latest ./participation-service
   docker push ghcr.io/<username>/quizzler-backend/participation-service:latest

   # Leaderboard
   docker build -t ghcr.io/<username>/quizzler-backend/leaderboard:latest ./LeaderBoard
   docker push ghcr.io/<username>/quizzler-backend/leaderboard:latest

   # Java Judge
   docker build -t ghcr.io/<username>/quizzler-backend/java-judge:latest ./JavaJudge
   docker push ghcr.io/<username>/quizzler-backend/java-judge:latest

   # Bug Report Service
   docker build -t ghcr.io/<username>/quizzler-backend/bugreport-service:latest ./BugReport-service
   docker push ghcr.io/<username>/quizzler-backend/bugreport-service:latest
   ```

## Using Images from GHCR

### Update docker-compose.yml

Replace `build` sections with `image` references:

```yaml
services:
  service-registry:
    image: ghcr.io/<username>/quizzler-backend/service-registry:latest
    # Remove build section
    container_name: service-registry
    # ... rest of config

  api-gateway:
    image: ghcr.io/<username>/quizzler-backend/api-gateway:latest
    # ... rest of config
```

### Pull and Run

```bash
# Login to GHCR (if private)
echo $GITHUB_TOKEN | docker login ghcr.io -u <username> --password-stdin

# Pull images
docker-compose pull

# Run services
docker-compose up -d
```

## Making Images Public

By default, GHCR packages are private. To make them public:

1. Go to your GitHub repository
2. Click "Packages" in the right sidebar
3. Click on a package
4. Click "Package settings"
5. Scroll down to "Danger Zone"
6. Click "Change visibility" → "Make public"

Or use GitHub CLI:
```bash
gh api user/packages/container/<package-name> -X PATCH -f visibility=public
```

## Troubleshooting

### Authentication Issues

**Error: "unauthorized: authentication required"**
- Ensure your PAT has `write:packages` permission
- Check that you're logged in: `docker login ghcr.io`

### Permission Denied

**Error: "denied: permission_denied"**
- Verify the package name matches your username
- Check package visibility settings
- Ensure PAT has correct scopes

### Build Failures

**Error: "failed to solve"**
- Check Dockerfile paths are correct
- Verify all dependencies are available
- Check build context includes necessary files

### Rate Limiting

GitHub has rate limits for package operations:
- Authenticated: 5,000 requests/hour
- Unauthenticated: 60 requests/hour

If you hit limits, wait or use authenticated requests.

## Best Practices

1. **Use tags**: Don't rely only on `latest`, use version tags
2. **Multi-arch builds**: Consider building for multiple architectures
3. **Security scanning**: Enable Dependabot for vulnerability scanning
4. **Clean up old images**: Regularly delete unused image versions
5. **Use semantic versioning**: Tag releases with `v1.0.0` format

## Image Sizes

Expected image sizes (approximate):
- Service Registry: ~200MB
- API Gateway: ~250MB
- User Auth: ~250MB
- Quiz Service: ~200MB
- Question Service: ~200MB
- Participation Service: ~200MB
- Leaderboard: ~200MB
- Java Judge: ~300MB (includes JDK)
- Bug Report Service: ~200MB

## CI/CD Integration

The GitHub Actions workflow automatically:
- Builds on every push
- Pushes on merge to main
- Creates version tags
- Uses build cache for faster builds
- Supports pull request testing

## Additional Resources

- [GHCR Documentation](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry)
- [Docker Buildx](https://docs.docker.com/buildx/working-with-buildx/)
- [GitHub Actions](https://docs.github.com/en/actions)





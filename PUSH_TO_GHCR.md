# How to Push Docker Images to GitHub Container Registry (GHCR)

## Quick Start - Choose Your Method

### Method 1: Automated (GitHub Actions) - Recommended ✅

**This is the easiest way!** Just push your code to GitHub and it will automatically build and push images.

#### Steps:

1. **Push your code to GitHub:**
   ```bash
   git add .
   git commit -m "Add Docker setup and GHCR workflow"
   git push origin main
   ```

2. **The workflow will automatically:**
   - Build all 9 service images
   - Push them to `ghcr.io/<your-username>/quizzler-backend/<service-name>`
   - Tag them as `latest` on the main branch

3. **View your images:**
   - Go to: `https://github.com/<your-username>?tab=packages`
   - Or check the Actions tab in your repository

#### Workflow Triggers:
- ✅ Push to **any branch** → Builds and pushes with branch name tag
- ✅ Push to `main` or `master` → Also tags as `latest`
- ✅ Create a tag (e.g., `v1.0.0`) → Creates version tags
- ✅ Pull Request → Builds but doesn't push (for testing)
- ✅ Manual trigger → Use "Run workflow" button

#### Image Tagging:
- **Main/Master branch**: `latest` + `main-<sha>` or `master-<sha>`
- **Other branches**: `<branch-name>` + `<branch-name>-<sha>`
- **Tags**: `v1.0.0`, `1.0`, `1` (for semantic version tags)

---

### Method 2: Manual Push (Local) - For Testing

If you want to push manually from your local machine:

#### Prerequisites:
1. **Create a GitHub Personal Access Token (PAT):**
   - Go to: https://github.com/settings/tokens
   - Click "Generate new token (classic)"
   - Select scope: `write:packages`
   - Copy the token (you'll need it)

#### Using Scripts:

**Windows:**
```cmd
push-to-ghcr.bat <your-github-username> latest
```

**Linux/Mac:**
```bash
chmod +x push-to-ghcr.sh
./push-to-ghcr.sh <your-github-username> latest
```

The script will:
1. Ask for your GitHub PAT
2. Login to GHCR
3. Build all 9 services
4. Push them to GHCR

#### Manual Commands (if you prefer):

```bash
# 1. Login to GHCR
echo YOUR_GITHUB_TOKEN | docker login ghcr.io -u YOUR_USERNAME --password-stdin

# 2. Build and push each service (replace YOUR_USERNAME)
docker build -t ghcr.io/YOUR_USERNAME/quizzler-backend/service-registry:latest ./service-registry
docker push ghcr.io/YOUR_USERNAME/quizzler-backend/service-registry:latest

docker build -t ghcr.io/YOUR_USERNAME/quizzler-backend/api-gateway:latest ./api-gateway
docker push ghcr.io/YOUR_USERNAME/quizzler-backend/api-gateway:latest

docker build -t ghcr.io/YOUR_USERNAME/quizzler-backend/user-auth:latest ./user-auth
docker push ghcr.io/YOUR_USERNAME/quizzler-backend/user-auth:latest

docker build -t ghcr.io/YOUR_USERNAME/quizzler-backend/quiz-service:latest ./Quiz
docker push ghcr.io/YOUR_USERNAME/quizzler-backend/quiz-service:latest

docker build -t ghcr.io/YOUR_USERNAME/quizzler-backend/question-service:latest ./question-service
docker push ghcr.io/YOUR_USERNAME/quizzler-backend/question-service:latest

docker build -t ghcr.io/YOUR_USERNAME/quizzler-backend/participation-service:latest ./participation-service
docker push ghcr.io/YOUR_USERNAME/quizzler-backend/participation-service:latest

docker build -t ghcr.io/YOUR_USERNAME/quizzler-backend/leaderboard:latest ./LeaderBoard
docker push ghcr.io/YOUR_USERNAME/quizzler-backend/leaderboard:latest

docker build -t ghcr.io/YOUR_USERNAME/quizzler-backend/java-judge:latest ./JavaJudge
docker push ghcr.io/YOUR_USERNAME/quizzler-backend/java-judge:latest

docker build -t ghcr.io/YOUR_USERNAME/quizzler-backend/bugreport-service:latest ./BugReport-service
docker push ghcr.io/YOUR_USERNAME/quizzler-backend/bugreport-service:latest
```

---

## Image Naming Convention

All images follow this pattern:
```
ghcr.io/<username>/quizzler-backend/<service-name>:<tag>
```

**Examples:**
- `ghcr.io/johndoe/quizzler-backend/api-gateway:latest`
- `ghcr.io/johndoe/quizzler-backend/user-auth:v1.0.0`
- `ghcr.io/johndoe/quizzler-backend/quiz-service:main-abc123`

---

## Making Images Public (Optional)

By default, GHCR packages are **private**. To make them public:

1. Go to: `https://github.com/<your-username>?tab=packages`
2. Click on a package
3. Click "Package settings"
4. Scroll to "Danger Zone"
5. Click "Change visibility" → "Make public"

Or use GitHub CLI:
```bash
gh api user/packages/container/<package-name> -X PATCH -f visibility=public
```

---

## Using Images from GHCR

After pushing, you can use the images in `docker-compose.yml`:

```yaml
services:
  service-registry:
    image: ghcr.io/YOUR_USERNAME/quizzler-backend/service-registry:latest
    # Remove the 'build' section
    container_name: service-registry
    # ... rest of config
```

Then pull and run:
```bash
docker-compose pull
docker-compose up -d
```

---

## Troubleshooting

### "unauthorized: authentication required"
- ✅ Make sure your PAT has `write:packages` permission
- ✅ Check you're logged in: `docker login ghcr.io`

### "denied: permission_denied"
- ✅ Verify package name matches your username
- ✅ Check package visibility settings

### GitHub Actions not running
- ✅ Check `.github/workflows/build-and-push.yml` exists
- ✅ Verify workflow file syntax is correct
- ✅ Check Actions tab for error messages

---

## Quick Reference

| Method | When to Use | Effort |
|--------|-------------|--------|
| GitHub Actions | Production, CI/CD | ⭐ Easy (automatic) |
| Manual Script | Testing, one-time push | ⭐⭐ Medium |
| Manual Commands | Custom control | ⭐⭐⭐ Advanced |

---

## Next Steps

1. **Choose your method** (recommend GitHub Actions)
2. **Push to GitHub** or run manual script
3. **Verify images** at `https://github.com/<username>?tab=packages`
4. **Update docker-compose.yml** to use GHCR images (optional)

For more details, see [GHCR_SETUP.md](GHCR_SETUP.md)


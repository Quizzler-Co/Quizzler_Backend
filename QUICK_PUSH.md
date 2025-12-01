# Quick Push to GHCR - Manual Commands

## Step 1: Login to GHCR

```bash
# Replace YOUR_USERNAME and YOUR_TOKEN
echo YOUR_GITHUB_TOKEN | docker login ghcr.io -u YOUR_USERNAME --password-stdin
```

## Step 2: Build and Push All Services

Replace `YOUR_USERNAME` with your GitHub username:

```bash
# Service Registry
docker build -t ghcr.io/YOUR_USERNAME/quizzler-backend/service-registry:latest ./service-registry
docker push ghcr.io/YOUR_USERNAME/quizzler-backend/service-registry:latest

# API Gateway
docker build -t ghcr.io/YOUR_USERNAME/quizzler-backend/api-gateway:latest ./api-gateway
docker push ghcr.io/YOUR_USERNAME/quizzler-backend/api-gateway:latest

# User Auth
docker build -t ghcr.io/YOUR_USERNAME/quizzler-backend/user-auth:latest ./user-auth
docker push ghcr.io/YOUR_USERNAME/quizzler-backend/user-auth:latest

# Quiz Service
docker build -t ghcr.io/YOUR_USERNAME/quizzler-backend/quiz-service:latest ./Quiz
docker push ghcr.io/YOUR_USERNAME/quizzler-backend/quiz-service:latest

# Question Service
docker build -t ghcr.io/YOUR_USERNAME/quizzler-backend/question-service:latest ./question-service
docker push ghcr.io/YOUR_USERNAME/quizzler-backend/question-service:latest

# Participation Service
docker build -t ghcr.io/YOUR_USERNAME/quizzler-backend/participation-service:latest ./participation-service
docker push ghcr.io/YOUR_USERNAME/quizzler-backend/participation-service:latest

# Leaderboard
docker build -t ghcr.io/YOUR_USERNAME/quizzler-backend/leaderboard:latest ./LeaderBoard
docker push ghcr.io/YOUR_USERNAME/quizzler-backend/leaderboard:latest

# Java Judge
docker build -t ghcr.io/YOUR_USERNAME/quizzler-backend/java-judge:latest ./JavaJudge
docker push ghcr.io/YOUR_USERNAME/quizzler-backend/java-judge:latest

# Bug Report Service
docker build -t ghcr.io/YOUR_USERNAME/quizzler-backend/bugreport-service:latest ./BugReport-service
docker push ghcr.io/YOUR_USERNAME/quizzler-backend/bugreport-service:latest
```

## Step 3: Verify

Visit: `https://github.com/YOUR_USERNAME?tab=packages`

You should see all 9 packages listed!


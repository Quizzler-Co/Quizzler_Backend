# Pushing to GHCR from Any Branch

## Current Behavior

When you push to **any branch**, the GitHub Actions workflow will:

1. ✅ **Build all 9 service images**
2. ✅ **Push them to GHCR**
3. ✅ **Tag them with the branch name**

## Image Tagging by Branch

### Main/Master Branch
When you push to `main` or `master`:
```
ghcr.io/<username>/quizzler-backend/service-registry:latest
ghcr.io/<username>/quizzler-backend/service-registry:main-abc123
```

### Feature Branches
When you push to a feature branch (e.g., `feature/docker-setup`):
```
ghcr.io/<username>/quizzler-backend/service-registry:feature-docker-setup
ghcr.io/<username>/quizzler-backend/service-registry:feature-docker-setup-abc123
```

### Development Branch
When you push to `develop`:
```
ghcr.io/<username>/quizzler-backend/service-registry:develop
ghcr.io/<username>/quizzler-backend/service-registry:develop-abc123
```

## Example Workflow

### 1. Create and Push a Feature Branch

```bash
# Create a new branch
git checkout -b feature/add-new-endpoint

# Make your changes
# ... edit files ...

# Commit and push
git add .
git commit -m "Add new endpoint"
git push origin feature/add-new-endpoint
```

### 2. GitHub Actions Will Automatically:
- ✅ Detect the push to `feature/add-new-endpoint`
- ✅ Build all Docker images
- ✅ Push to GHCR with tag: `feature-add-new-endpoint`
- ✅ Also tag with: `feature-add-new-endpoint-<commit-sha>`

### 3. View Your Images

Go to: `https://github.com/<username>?tab=packages`

You'll see images tagged with your branch name!

## Using Branch-Specific Images

### In docker-compose.yml

You can use branch-specific images:

```yaml
services:
  api-gateway:
    image: ghcr.io/<username>/quizzler-backend/api-gateway:feature-add-new-endpoint
    # ... rest of config
```

### Pull Specific Branch Images

```bash
# Pull images from a specific branch
docker pull ghcr.io/<username>/quizzler-backend/api-gateway:feature-add-new-endpoint

# Or use docker-compose
docker-compose pull
```

## Tag Strategy Summary

| Event | Tags Created | Example |
|-------|-------------|---------|
| Push to `main` | `latest`, `main-<sha>` | `latest`, `main-abc123` |
| Push to `develop` | `develop`, `develop-<sha>` | `develop`, `develop-abc123` |
| Push to `feature/xyz` | `feature-xyz`, `feature-xyz-<sha>` | `feature-xyz`, `feature-xyz-abc123` |
| Tag `v1.0.0` | `v1.0.0`, `1.0`, `1` | `v1.0.0`, `1.0`, `1` |

## Best Practices

### 1. Use Semantic Versioning for Releases
```bash
# Create a release tag
git tag v1.0.0
git push origin v1.0.0
```
This creates: `v1.0.0`, `1.0`, and `1` tags

### 2. Use Branch Names for Development
- Feature branches → `feature-<name>`
- Bug fixes → `fix-<name>`
- Hotfixes → `hotfix-<name>`

### 3. Keep Main Branch Clean
- Only merge tested code to `main`
- `main` always has `latest` tag
- Use feature branches for development

## Workflow Status

You can check workflow status:
1. Go to your repository
2. Click "Actions" tab
3. See all workflow runs
4. Click on a run to see details

## Troubleshooting

### Workflow Not Running?
- ✅ Check if `.github/workflows/build-and-push.yml` exists
- ✅ Verify you pushed to the branch
- ✅ Check Actions tab for errors

### Images Not Appearing?
- ✅ Wait a few minutes (builds take time)
- ✅ Check workflow logs for errors
- ✅ Verify you have `write:packages` permission

### Wrong Tags?
- ✅ Branch names are sanitized (special chars become `-`)
- ✅ Tags are lowercase
- ✅ SHA is shortened to 7 characters

## Quick Reference

```bash
# Push to feature branch → Creates branch-name tag
git push origin feature/my-feature

# Push to main → Creates latest tag
git push origin main

# Create version tag → Creates v1.0.0, 1.0, 1 tags
git tag v1.0.0
git push origin v1.0.0
```





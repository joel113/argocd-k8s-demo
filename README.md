# Argo CD demo: Java Quarkus app with test/int/prod overlays

This repository is a minimal demo showing how to deploy a Java Quarkus app with Argo CD using Kustomize overlays for test, int, and prod stages.

What is included:

- Minimal Quarkus app (port 8080) that returns JSON {message, env}
- Dockerfile for building the container
- k8s/base + overlays (kustomize) for test/int/prod
- Argo CD Application manifests in `argocd/apps/` (edit `repoURL` before applying)
- GitHub Actions workflow `.github/workflows/ci.yml` to build & push container image and optionally update overlay tags

Quick start

1) Build the native JAR (Quarkus build)

   mvn -DskipTests package

2) Build & push the image (update registry and repo owner)

   docker build -t ghcr.io/your-org/argocd-demo:latest .
   docker push ghcr.io/your-org/argocd-demo:latest

3) Make sure the overlays point to your pushed images (k8s/overlays/*/kustomization.yaml). The included GitHub Action will push to ghcr.io/${{ github.repository_owner }}/argocd-demo:latest and (optionally) update overlays on push to `main`.

CI (GitHub Actions)

The workflow in `.github/workflows/ci.yml` does the following:

- checks-out the code, builds the Quarkus artifact with Maven
- logs into GitHub Container Registry and builds/pushes the image as `ghcr.io/<owner>/argocd-demo:latest`
- optionally updates kustomize overlays with the new tag and pushes the change back to `main` (controlled inside the workflow)

Set up secrets (if pushing to a different registry):

- For GitHub Container Registry the workflow uses `${{ secrets.GITHUB_TOKEN }}` automatically. If you push to Docker Hub or another registry, add appropriate secrets and update the `docker/login-action` step.

Argo CD Image Updater (optional)

To automate image tag updates in Git (and let Argo CD sync them), you can use Argo CD Image Updater. The overlays contain example comment blocks with `image-updater.argoproj.io` keys that show how you might annotate the Deployment/overlays for detection. A simple `argocd-image-updater` config looks like this (install Image Updater in the cluster and configure it):

Example snippet for `argocd-image-updater` (cluster-side config)

  # Not a complete manifest — show how to reference the repo and registry
  repositories:
    - name: github-images
      url: https://ghcr.io
      usernameSecret:
        name: reg-secret
        key: username
      passwordSecret:
        name: reg-secret
        key: password

And annotate your Kubernetes manifests (or overlays) to indicate which images to update. We added example annotation comments in each overlay's patch showing the keys `image-updater.argoproj.io/image-list` and update-strategy.

Argo CD setup and apply

1) Install Argo CD if needed:

   kubectl create namespace argocd
   kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

2) Create the environment namespaces:

   kubectl create namespace test
   kubectl create namespace int
   kubectl create namespace prod

3) Edit `argocd/apps/*-application.yaml` and set `spec.source.repoURL` to your repository URL, then apply the Application manifests:

   kubectl apply -f argocd/apps/test-application.yaml
   kubectl apply -f argocd/apps/int-application.yaml
   kubectl apply -f argocd/apps/prod-application.yaml

Notes & next steps

- Add resource requests/limits and liveness/readiness probes in `k8s/base/deployment.yaml` for production readiness.
- Consider adding a step in CI that builds smaller immutable tags (e.g. commit SHA) and pushes those; then update overlays with that tag to have traceable deployments.
- If you want, I can add a complete `argocd-image-updater` config and a small GitHub Action step that creates a tag per build and updates the corresponding overlay automatically.

What I implemented for you (done)

- CI now builds and pushes both `latest` and an immutable `${{ github.sha }}` tag. The workflow will update overlays on `main` to use the immutable SHA tag so deployments are traceable.
- `k8s/base/deployment.yaml` now includes `livenessProbe`, `readinessProbe`, `imagePullPolicy`, and `resources.requests/limits`.
- Example Argo CD Image Updater notes and secret usage are added to `argocd/image-updater/README.md`.

How the CI updates overlays (summary)

1. On push to `main` the workflow builds images tagged with `latest` and `${GITHUB_SHA}` and pushes both to GHCR.
2. The workflow edits `k8s/overlays/*/kustomization.yaml` replacing the `image: ...` lines in the patch blocks with `image: <registry>/argocd-demo:<SHA>` and commits the change back to `main`.
3. Argo CD will see the repo change and (with automated sync) deploy the new immutable image.

Argo CD Image Updater

If you prefer Image Updater instead of committing tag changes from CI, install `argocd-image-updater` in your cluster and configure it to talk to your registry. See `argocd/image-updater/README.md` for an example of how to create registry secrets and repository entries.


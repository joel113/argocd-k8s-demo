# Argo CD Image Updater: examples

This folder contains example snippets to help configure Argo CD Image Updater.

## Registry credentials

Create a Kubernetes secret with your registry credentials (example for GHCR):

```bash
kubectl create secret generic ghcr-secret -n argocd \
  --from-literal=username=<GHCR_USER> \
  --from-literal=password=<GHCR_TOKEN>
```

## Example repositories configuration (cluster-side)

This is an example snippet showing how you can register a registry for Image Updater (this is not a full manifest; adapt to your installation):

```yaml
repositories:
  - name: ghcr
    url: https://ghcr.io
    usernameSecret:
      name: ghcr-secret
      key: username
    passwordSecret:
      name: ghcr-secret
      key: password
```

## Annotate Applications / manifests

Annotate the Deployment or the Argo CD Application to tell Image Updater which images to track. We added example comments earlier in the repository showing keys like `image-updater.argoproj.io/image-list` and `image-updater.argoproj.io/<image>.update-strategy`.

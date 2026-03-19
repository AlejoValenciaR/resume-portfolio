# Hello Spring on LocalStack EKS

This repository is now prepared for a beginner-friendly Jenkins flow that:

1. runs Spring Boot tests
2. builds a Docker image
3. pushes the image to LocalStack ECR
4. deploys the app to the LocalStack EKS cluster

The app stays simple:

- local development uses `/apps` as the base route
- Kubernetes sets `APP_BASE_PATH=/apps`, so the public URLs become:
  - `https://nauthappstest.tech/apps/api/hello`
  - `https://nauthappstest.tech/apps/api/users`

## Current App State

- Spring Boot `3.5.11`
- Java `21`
- HTTP port `8080`
- Endpoints:
  - `GET /apps/api/hello`
  - `GET /apps/api/users`
  - `GET /apps/api/users/{id}`
- Docker image built from the root `Dockerfile`

## Feasibility

Deploying this app to LocalStack EKS is realistic as a development and demo workflow, not as production-grade Kubernetes.

Why this is feasible:

- your Terraform repo already creates ECR and EKS resources
- LocalStack documents EKS support with optional ingress/load balancer startup
- Jenkins can build the image, push to ECR, and apply Kubernetes manifests

Why this is still limited:

- LocalStack EKS can behave differently from real AWS EKS
- node group and ingress behavior depend on the LocalStack edition/version and VM memory
- browser access from the public internet depends on Azure VM reverse-proxy wiring outside this repo

If the EKS path remains unstable in your LocalStack instance, the closest reliable fallback is to run the Spring Boot container directly on the Azure VM behind Nginx instead of pretending the EKS flow is production-ready.

## What Was Added Here

- `server.servlet.context-path=${APP_BASE_PATH:/apps}` so the app always uses `/apps` by default
- `Jenkinsfile` for `git push -> Jenkins -> test -> build -> push -> deploy`
- `scripts/deploy-to-localstack-eks.sh` for the CLI deployment workflow
- `k8s/*.template.yaml` Kubernetes manifests for namespace, deployment, service, and ingress

## Jenkins Agent Requirements

Your Jenkins agent needs:

- Docker
- AWS CLI v2
- `kubectl`
- Bash
- Java 21

It also needs these Jenkins credentials:

- `LS_AWS_ACCESS_KEY_ID`
- `LS_AWS_SECRET_ACCESS_KEY`
- `LS_ENDPOINT_URL`

## Expected Jenkins Flow

After a GitHub push:

1. Jenkins checks out this repo.
2. Jenkins runs `./mvnw -B test`.
3. Jenkins calls `scripts/deploy-to-localstack-eks.sh`.
4. The script:
   - verifies LocalStack credentials with `sts get-caller-identity`
   - ensures the ECR repository exists
   - reads the `repositoryUri` from LocalStack
   - builds and pushes the Docker image
   - updates kubeconfig for the LocalStack EKS cluster
   - creates or updates the image pull secret
   - applies the Kubernetes manifests
   - waits for the deployment rollout

## Required Infra Changes Outside This Repo

These are required if you want the app reachable in a public browser at `https://nauthappstest.tech/apps/...`.

### 1) Azure VM LocalStack config

In the `azure-docker-vm-terraform` repo, the LocalStack container should be started with:

- `EKS_START_K3D_LB_INGRESS=1`
- `EKS_LOADBALANCER_PORT=8081`
- `LOCALSTACK_HOST=localstack.nauthappstest.tech`

Strong recommendation for remote Docker pushes:

- `ECR_ENDPOINT_STRATEGY=off`

Why:

- `EKS_START_K3D_LB_INGRESS=1` gives the cluster an ingress/load balancer port that Nginx can proxy to
- `LOCALSTACK_HOST` prevents Jenkins and kubeconfig from receiving localhost-style endpoints
- `ECR_ENDPOINT_STRATEGY=off` is the simplest remote-friendly ECR URI strategy when Jenkins is not running on the same host as LocalStack

### 2) Azure VM Nginx routing

In the same Azure VM repo, `nauthappstest.tech` should proxy `/apps/` to the LocalStack EKS ingress port:

- `http://127.0.0.1:8081`

That is what makes the pod reachable from browsers after TLS terminates in Nginx.

### 3) Terraform EKS repo

In `localstack-terraform-jenkins`:

- keep `enable_eks = true`
- ensure the cluster and node group already exist before running the app pipeline

## Local Manual Run

Local development now uses `/apps` as the base path by default:

```bash
./mvnw spring-boot:run
```

Then use:

- `http://localhost:8080/apps/api/hello`
- `http://localhost:8080/apps/api/users`

## Notes

The deployment script fails early when LocalStack returns localhost-only ECR or Kubernetes endpoints to a remote Jenkins agent. That is intentional, because otherwise the pipeline would appear correct while being unreachable from outside the VM.

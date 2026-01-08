# Local Kubernetes + Terraform Guide (Base Payments)

This guide shows how to build the Base Payments service into a local Docker image and deploy it with Terraform to a local Kubernetes cluster. The Terraform configuration enforces **three replicas** and exposes the service through a **LoadBalancer** service.

## About load balancers on local Kubernetes
Kubernetes defines a `LoadBalancer` Service type, but **the actual load balancer is provided by your cluster environment**. Local clusters typically need an add-on or helper:

- **Docker Desktop / Rancher Desktop**: includes a built-in load balancer implementation, so `LoadBalancer` services usually work out of the box.
- **Minikube**: use `minikube tunnel` to provision a local load balancer IP.
- **Kind**: install a local load balancer such as **MetalLB**, or use `kubectl port-forward` as a fallback.

## Prerequisites (macOS)
Install the required tools (Terraform, kubectl, Docker Desktop) and verify the cluster is reachable.

```bash
brew install terraform kubectl
brew install --cask docker
```

Start Docker Desktop, enable Kubernetes, and confirm access:

```bash
kubectl cluster-info
kubectl get nodes
```

## 1) Build the local Docker image
From the repository root:

```bash
./gradlew bootBuildImage --imageName base-payments:local
```

If you are using **kind**, load the image into the cluster:

```bash
kind load docker-image base-payments:local
```

## 2) (Optional) Start dependencies
The application expects Postgres/Kafka. If you want to run the full stack locally, start the provided Docker Compose stack:

```bash
docker compose up -d
```

## 3) Deploy with Terraform
Terraform configuration lives in `terraform/local-k8s`.

```bash
cd terraform/local-k8s
terraform init
terraform apply
```

Terraform creates:

- a `base-payments` namespace
- a Deployment with **3 replicas**
- a `LoadBalancer` Service (`base-payments-lb`)

## 4) Verify the deployment

```bash
kubectl get pods -n base-payments
kubectl get svc -n base-payments
```

If your local Kubernetes cluster needs help exposing LoadBalancer IPs:

- **Minikube**: run `minikube tunnel` in another terminal.
- **Kind**: install MetalLB, or run a temporary port forward:

```bash
kubectl port-forward -n base-payments svc/base-payments-lb 8080:80
```

## 5) Test the service
Once the service has an external IP (or after port-forwarding):

```bash
curl http://localhost:8080/actuator/health
```

## 6) Tear down

```bash
terraform destroy
```

## Customization
You can override defaults with Terraform variables:

```bash
terraform apply \
  -var="image=base-payments:local" \
  -var="replicas=3" \
  -var="container_port=8080" \
  -var="service_port=80"
```

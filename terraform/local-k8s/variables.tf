variable "kubeconfig_path" {
  description = "Path to the kubeconfig file to use for the Kubernetes provider."
  type        = string
  default     = "~/.kube/config"
}

variable "namespace" {
  description = "Namespace for the Base Payments deployment."
  type        = string
  default     = "base-payments"
}

variable "app_name" {
  description = "Name for the application resources."
  type        = string
  default     = "base-payments"
}

variable "image" {
  description = "Container image to run. Use a local Docker image name for local clusters."
  type        = string
  default     = "base-payments:local"
}

variable "replicas" {
  description = "Number of application replicas to run."
  type        = number
  default     = 3
}

variable "container_port" {
  description = "Container port exposed by the Spring Boot application."
  type        = number
  default     = 8080
}

variable "service_port" {
  description = "Service port for external access."
  type        = number
  default     = 80
}

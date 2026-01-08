output "service_name" {
  description = "Name of the Kubernetes Service created for the app."
  value       = kubernetes_service.app.metadata[0].name
}

output "namespace" {
  description = "Namespace where the resources were deployed."
  value       = kubernetes_namespace.app.metadata[0].name
}

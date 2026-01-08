terraform {
  required_version = ">= 1.5.0"

  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.30"
    }
  }
}

provider "kubernetes" {
  config_path = var.kubeconfig_path
}

locals {
  app_labels = {
    app = var.app_name
  }
}

resource "kubernetes_namespace" "app" {
  metadata {
    name = var.namespace
  }
}

resource "kubernetes_deployment" "app" {
  metadata {
    name      = var.app_name
    namespace = kubernetes_namespace.app.metadata[0].name
    labels    = local.app_labels
  }

  spec {
    replicas = var.replicas

    selector {
      match_labels = local.app_labels
    }

    template {
      metadata {
        labels = local.app_labels
      }

      spec {
        container {
          name              = var.app_name
          image             = var.image
          image_pull_policy = "IfNotPresent"

          port {
            container_port = var.container_port
          }

          env {
            name  = "SERVER_PORT"
            value = tostring(var.container_port)
          }

          readiness_probe {
            http_get {
              path = "/actuator/health/readiness"
              port = var.container_port
            }

            initial_delay_seconds = 20
            period_seconds        = 10
          }

          liveness_probe {
            http_get {
              path = "/actuator/health/liveness"
              port = var.container_port
            }

            initial_delay_seconds = 30
            period_seconds        = 20
          }

          resources {
            requests = {
              cpu    = "250m"
              memory = "512Mi"
            }

            limits = {
              cpu    = "500m"
              memory = "1Gi"
            }
          }
        }
      }
    }
  }
}

resource "kubernetes_service" "app" {
  metadata {
    name      = "${var.app_name}-lb"
    namespace = kubernetes_namespace.app.metadata[0].name
  }

  spec {
    selector = local.app_labels

    port {
      port        = var.service_port
      target_port = var.container_port
    }

    type = "LoadBalancer"
  }
}

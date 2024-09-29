# Base Payments Service
## Daniel Machado Vasconcelos

### Overview
This project aims to demonstrate how Spring Modulith can be used to create a modular, maintainable architecture by ensuring small, self-contained modules without unnecessary package dependencies. The goal is to improve cohesion and reduce coupling, while leveraging event-driven design patterns and architectural best practices.

### Basic requirements:
- **Spring Modulith**: Helps maintain modularity by abstracting architectural patterns, such as the **Outbox Pattern**, to handle event-driven communication efficiently without introducing tight coupling between modules.
- **Event Sourcing**: Ensures all state changes are captured as immutable events, allowing for a more resilient, traceable, and auditable system.
- **Apache Kafka**: Acts as the core message broker, enabling reliable and scalable communication between different modules.
- **Spring Security**: Secures the service by enforcing authentication and authorization for all actions performed within the system.
- **Spring MVC**: Exposes RESTful endpoints to manage and interact with orders and other core domain functionalities.
- **Spring Data JPA**: Simplifies data persistence and retrieval, working seamlessly with the event sourcing approach.
- **Docker Compose**: Provides an easy-to-deploy environment with all necessary services, including Kafka and PostgreSQL, making the project ready for local development and testing.

### Docker Compose Support
This project contains a Docker Compose file named `compose.yaml`. In this file, the following services have been defined:

* **PostgreSQL**: [`postgres:latest`](https://hub.docker.com/_/postgres)
* **Kafka**: [`bitnami/kafka:latest`](https://hub.docker.com/r/bitnami/kafka/)
* **AKHQ** (Kafka GUI): [`tchiotludo/akhq:latest`](https://hub.docker.com/r/tchiotludo/akhq)

> Note: Please review the tags of the used images and set them to the same versions as those running in production to ensure consistency.

# Getting Started
## How to Build?

Clone this repository into a new project folder (e.g., `base-payments`).

```bash
git clone https://github.com/DanielMachadoVasconcelos/base-payments.git
cd base-payments
```

Start the external resources by running the Docker Compose file. 

> Note: This step is optional, since the spring boot applications will start the necessary resources automatically

```bash
docker-compose up -d
```

## Running the Application

To run the application, you can use the following command:

```bash
./gradlew bootRun
```

This command will start the application on port `8080` by default.

## Testing the Application

You can test the application by sending HTTP requests to the exposed endpoints. The application provides the following endpoints:

* `POST /api/orders`: Creates a new order
* `GET /api/orders/{id}`: Retrieves an order by its ID
* `GET /api/orders`: Retrieves all orders
* `PUT /api/orders/{id}/cancel`: Cancels an order by its ID
* `PUT /api/orders/{id}/complete`: Completes an order by its ID
* `POST /api/orders/{id}/items`:  Adds an item to an order by its ID
* `DELETE /api/orders/{id}/items/{itemId}`: Removes an item from an order by its ID and item ID
* `GET /api/orders/{id}/events`: Retrieves all events for an order by its ID
* `GET /api/orders/{id}/events/{eventId}`: Retrieves a specific event for an order by its ID and event ID

## Curl comands

#### To create a order
```bash
curl --location --request POST 'localhost:5000/v1/orders' \
--header 'Content-Type: application/json' \
--data-raw '{
    "currency": "USD",
    "amount": 3500
}'
```

### Postgres
> Note:  Access the local url (localhost:5050) in your favorite browser to verify the Postgres Database Admin UI.

**Use the following credentials:**

| username      | password |
|---------------|--------|
| admin@admin.com | admin | 

Or access the database using the following command:
```bash
//PGPASSWORD=password psql -U user -h localhost orders
```

### Kafka

> Note: Access the local url (localhost:8080) in your favorite browser to verify the Kafka GUI.

**Look for the following topics:**

- `orders-events.v1.topic`



### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle Documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/3.3.2/gradle-plugin/reference/html/)
* [Create an OCI Image](https://docs.spring.io/spring-boot/docs/3.3.2/gradle-plugin/reference/html/#build-image)
* [Docker Compose Support](https://docs.spring.io/spring-boot/docs/3.3.2/reference/htmlsingle/index.html#features.docker-compose)
* [Spring Modulith](https://docs.spring.io/spring-modulith/reference/)
* [Spring for Apache Kafka](https://docs.spring.io/spring-boot/docs/3.3.2/reference/htmlsingle/index.html#messaging.kafka)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/docs/3.3.2/reference/htmlsingle/index.html#using.devtools)
* [Spring Configuration Processor](https://docs.spring.io/spring-boot/docs/3.3.2/reference/htmlsingle/index.html#appendix.configuration-metadata.annotation-processor)
* [Spring Web](https://docs.spring.io/spring-boot/docs/3.3.2/reference/htmlsingle/index.html#web)
* [Spring Data JPA](https://docs.spring.io/spring-boot/docs/3.3.2/reference/htmlsingle/index.html#data.sql.jpa)

### Guides
The following guides illustrate how to use some features concretely:

* [Messaging with Kafka](https://spring.io/guides/gs/messaging-kafka/)
* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)

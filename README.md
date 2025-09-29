# Recipe Manager

A **Spring Boot REST API** to manage recipes. Users can **add, update, delete, fetch, and filter recipes** based on vegetarian status, servings, ingredients, or instructions.

---

## **Table of Contents**

1. [Architecture & Design](#architecture--design)
2. [Entity Relationship](#entity-relationship)
3. [Running the Application](#running-the-application)
4. [Swagger / OpenAPI](#swagger--openapi)
5. [Sample Postman Requests](#sample-postman-requests)
6. [Database](#database)
7. [Testing](#testing)

---

## **Architecture & Design**

- **Spring Boot 3.x** REST API
- **Spring Data JPA** for database persistence
- **H2** for testing / MySQL for production
- **Lombok** for reducing boilerplate
- **Global Exception Handling** for user-friendly messages
- **DTO layer** for data transfer

**Layers:**  


- **Controller**: Handles HTTP requests and responses
- **Service**: Contains business logic
- **Repository**: JPA-based data access
- **Database**: Stores recipes and ingredients

---

## **Entity Relationship**

**Entities:**

- **Recipe**
    - `id`: Long
    - `name`: String
    - `vegetarian`: boolean
    - `servings`: int
    - `instructions`: TEXT
    - `createdAt`, `updatedAt`: LocalDateTime
    - `ingredients`: Set<Ingredient> (ManyToMany)

- **Ingredient**
    - `id`: Long
    - `name`: String
    - `recipes`: Set<Recipe> (ManyToMany)

**Relationship Diagram (ER):**

```text
+-----------+        +-----------------+         +------------+
|  Recipe   |<------>| recipe_ingredient|<------->| Ingredient |
+-----------+        +-----------------+         +------------+
| id        |                                     | id        |
| name      |                                     | name      |
| vegetarian|                                     +------------+
| servings  |
| instructions|
| createdAt |
| updatedAt |
+-----------+

```

## Running the Application

### Prerequisites
- Java 17+  
- Gradle 8+  
- MySQL (optional, can use H2 in-memory DB for testing)  

### Steps

**Clone the repo:**
```bash
git clone https://github.com/your-username/recipe-manager.git
cd recipe-manager

Build the project:
/gradlew build

Run the application:
./gradlew bootRun

Server will start on: http://localhost:8080

Using H2 DB console:

URL: http://localhost:8080/h2-console

JDBC URL: jdbc:h2:mem:recipesdb

User: sa

Password: (leave blank)

```

**Testing:**
```bash
Unit tests with JUnit 5 and Mockito
Integration tests with H2 in-memory database

./gradlew clean test

```

**Swagger / OpenAPI:**
```bash

http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs

```
**Postman Requests:**
```bash
Create Recipe
POST http://localhost:8080/api/recipes
Content-Type: application/json

{
  "name": "Pasta Primavera",
  "vegetarian": true,
  "servings": 4,
  "instructions": "Cook pasta and mix with vegetables",
  "ingredients": ["Pasta", "Tomato", "Broccoli"]
}

GET http://localhost:8080/api/recipes

GET http://localhost:8080/api/recipes/1

Update Recipe:
PUT /api/recipes/1
Content-Type: application/json

{
  "name": "Pasta Primavera Deluxe",
  "vegetarian": true,
  "servings": 5,
  "instructions": "Cook pasta and mix with fresh vegetables",
  "ingredients": ["Pasta", "Tomato", "Broccoli", "Bell Pepper"]
}

DELETE http://localhost:8080/api/recipes/1

GET http://localhost:8080/api/recipes/vegetarian?vegetarian=true

GET http://localhost:8080/api/recipes/servings?servings=4

GET http://localhost:8080/api/recipes/ingredients?ingredients=Pasta,Tomato

GET http://localhost:8080/api/recipes/exclude-ingredients?ingredients=Salmon&text=oven

GET http://localhost:8080/api/recipes/search-instructions?text=pasta

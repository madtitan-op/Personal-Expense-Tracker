# Expense Tracker

A simple Java Spring Boot application to manage and track your expenses. This project demonstrates CRUD operations, authentication, and category management for expenses.

## Features
- User registration and authentication (JWT-based)
- Add, update, delete, and view expenses
- Categorize expenses
- Secure RESTful APIs
- Swagger UI for API documentation and testing
- **Modern Web UI** (HTML, CSS, JavaScript, Chart.js)

## Technologies Used
- Java 17+
- Spring Boot
- Spring Security (JWT)
- Spring Data JPA
- H2 Database (or configure your own)
- Swagger (Springfox)
- **HTML, CSS, JavaScript** (for frontend)
- **Chart.js** (for analytics and charts)

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven

### Running the Application
1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd Expense-Tracker
   ```
2. Build and run the application:
   ```bash
   ./mvnw spring-boot:run
   ```
   or on Windows:
   ```bash
   mvnw.cmd spring-boot:run
   ```
3. The application will start on `http://localhost:8080` by default.

### Accessing the Web UI
- Open your browser and go to: [http://localhost:8080/](http://localhost:8080/)
- The UI is built with static files in `src/main/resources/static/`:
  - `index.html` (main page)
  - `styles.css` (styling)
  - `script.js` (frontend logic)

### API Documentation with Swagger
- After starting the application, access Swagger UI at:
  - [http://localhost:8080/swagger-ui/](http://localhost:8080/swagger-ui/)
- Use Swagger UI to explore and test the available REST APIs interactively.

## Project Structure
- `src/main/java/com/expense/tracker/` - Main source code
  - `controller/` - REST controllers
  - `service/` - Business logic
  - `repository/` - Data access layer
  - `model/` - Entity classes
  - `config/` - Security and JWT configuration
- `src/main/resources/` - Application properties and static resources
  - `static/` - **Frontend UI files** (`index.html`, `styles.css`, `script.js`)

## Configuration
- Edit `src/main/resources/application.properties` to change database or server settings as needed.

## License
This project is for educational purposes.

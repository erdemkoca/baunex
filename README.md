# Baunex Business Management

A business management application for managing workers and projects, built with Quarkus, Kotlin, and Qute templating engine.

## Features

- **Project Management**: Create, view, edit, and delete projects
- **Worker Management**: Manage your workforce with detailed information
- **Dashboard**: Get an overview of your business with key statistics
- **Responsive UI**: Modern, mobile-friendly user interface

## Screenshots

![Dashboard](https://via.placeholder.com/800x450.png?text=Dashboard+Screenshot)
![Projects](https://via.placeholder.com/800x450.png?text=Projects+Screenshot)
![Workers](https://via.placeholder.com/800x450.png?text=Workers+Screenshot)

## Prerequisites

- JDK 11+ installed
- Maven 3.8.1+
- (Optional) Docker for containerized deployment

## Running the Application

### Development Mode

```bash
./mvnw compile quarkus:dev
```

This command starts the application in development mode with hot reload enabled.

### Production Mode

```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

### Docker

```bash
./mvnw package
docker build -f src/main/docker/Dockerfile.jvm -t baunex/business-management .
docker run -i --rm -p 8080:8080 baunex/business-management
```

## Accessing the Application

Once the application is running, you can access it at:

```
http://localhost:8080
```

## Database

The application uses PostgreSQL as the database. Before running the application, make sure you have PostgreSQL installed and running with the following configuration:

```properties
# PostgreSQL configuration
quarkus.datasource.db-kind=postgresql
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/baunex
quarkus.datasource.username=postgres
quarkus.datasource.password=postgres
```

You'll need to create a database named 'baunex' in your PostgreSQL server:

```sql
CREATE DATABASE baunex;
```

Sample data is automatically loaded on startup when the database is empty.

## Project Structure

- `src/main/kotlin/ch/baunex/project`: Project-related components
- `src/main/kotlin/ch/baunex/worker`: Worker-related components
- `src/main/kotlin/ch/baunex/web`: Web controllers for the frontend
- `src/main/resources/templates`: Qute templates for the UI
- `src/main/resources/META-INF/resources`: Static resources (CSS, JS)

## Technologies Used

- **Backend**: Quarkus, Kotlin, RESTEasy, Hibernate ORM with Panache
- **Frontend**: Qute templating, Bootstrap 5, JavaScript
- **Database**: PostgreSQL

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/my-feature`
3. Commit your changes: `git commit -am 'Add my feature'`
4. Push to the branch: `git push origin feature/my-feature`
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact

For questions or support, please contact [your-email@example.com](mailto:your-email@example.com).

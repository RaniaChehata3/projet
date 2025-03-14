# Healthcare Management System

A comprehensive JavaFX application for managing healthcare operations, including patient records, medical history, medications, lab results, and more.

## Features

- **Authentication System**: Secure login with role-based access control (Admin, Doctor, Patient, Laboratory)
- **Doctor Dashboard**: Manage patient records, medical history, medications, and lab results
- **Patient Management**: Add, edit, view, and delete patient information
- **Medical Records**: Track patient visits, diagnoses, treatments, and follow-ups
- **Medications**: Manage prescribed medications with dosage, frequency, and instructions
- **Lab Results**: Record and view laboratory test results
- **Notes**: Add clinical notes to patient records

## Technology Stack

- **Java 22**: Core programming language
- **JavaFX**: UI framework
- **FXML**: UI layout definition
- **CSS**: Styling
- **H2 Database**: In-memory database for development/testing
- **MySQL**: Database for production
- **Maven**: Dependency management and build tool

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 22 or higher
- Maven 3.6 or higher
- MySQL (optional, for production)

### Installation

1. Clone the repository:
   ```
   git clone https://github.com/yourusername/healthcare-management-system.git
   ```

2. Navigate to the project directory:
   ```
   cd healthcare-management-system
   ```

3. Build the project:
   ```
   mvn clean install
   ```

4. Run the application:
   ```
   mvn javafx:run
   ```

### Database Configuration

By default, the application uses an H2 in-memory database for development and testing. To use MySQL in production:

1. Create a MySQL database named `healthapp`
2. Update the `database.properties` file in `src/main/resources`:
   ```
   # Comment out H2 configuration
   #db.url=jdbc:h2:mem:healthapp;DB_CLOSE_DELAY=-1
   #db.username=sa
   #db.password=

   # Uncomment MySQL configuration
   db.url=jdbc:mysql://localhost:3306/healthapp?useSSL=false&serverTimezone=UTC
   db.username=your_username
   db.password=your_password
   ```

## Demo Accounts

The application comes with pre-configured demo accounts:

| Username | Password | Role       |
|----------|----------|------------|
| admin    | password | Admin      |
| doctor   | password | Doctor     |
| patient  | password | Patient    |
| lab      | password | Laboratory |

## Project Structure

- `src/main/java/com/example/demo`: Java source files
  - `auth`: Authentication-related classes
  - `controller`: JavaFX controllers
  - `database`: Database access objects
  - `model`: Data models
  - `utils`: Utility classes
- `src/main/resources/com/example/demo`: Resources
  - `css`: CSS stylesheets
  - `view`: FXML layout files
  - `images`: Image resources
  - `database`: SQL scripts

## Screenshots

(Add screenshots here)

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- JavaFX community for excellent documentation and examples
- H2 Database for providing a lightweight in-memory database solution
- ControlsFX for enhanced JavaFX controls

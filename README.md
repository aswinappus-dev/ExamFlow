# ExamFlow
## Project Structure

```
ExamFlow/                        # Root project directory
│
├── README.md                    # Project overview and documentation
├── pom.xml                      # Maven configuration file for dependencies and build
├── src/                         # Source code directory
│   ├── main/                    # Main application code
│   │   ├── java/                # Java source files
│   │   │   └── com/
│   │   │       └── examflow/
│   │   │           ├── App.java         # Main entry point of the application
│   │   │           ├── seating/         # Seating plan logic
│   │   │           │   └── SeatingPlanner.java   # Handles seat assignment algorithms
│   │   │           ├── db/              # Database interaction code
│   │   │           │   └── DatabaseManager.java  # Manages DB connections and queries
│   │   │           └── model/           # Data models
│   │   │               ├── Student.java         # Represents a student
│   │   │               ├── Room.java            # Represents an exam room
│   │   │               └── SeatingPlan.java     # Represents a seating plan
│   │   └── resources/             # Application resources (e.g., config files)
│   │       └── application.properties   # Configuration for DB and app settings
│   └── test/                     # Test code
│       └── java/                 # Java test files
│           └── com/
│               └── examflow/
│                   └── AppTest.java     # Unit tests for the application
│
└── .gitignore                    # Specifies files/folders to ignore in git
```

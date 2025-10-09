ExamFlow: Automated Exam Hall Seating Arrangement
ExamFlow is a robust web application built with Java and Spring Boot to automate the process of randomizing and assigning seating arrangements for students in an examination hall. It provides dedicated views for students, invigilators, and a powerful admin panel for seamless management.

✨ Features
👨‍🎓 Student View: Allows students to securely check their assigned exam hall and seat number close to the exam time.

🧑‍🏫 Invigilator View: Provides a detailed, printable seating layout for a specific hall, showing which student is assigned to each bench.

👑 Admin Panel: A comprehensive dashboard for administrators to manage student data, hall details, and exam schedules. Admins can add, update, and delete records as needed.

🤖 Automated Randomization: A time-based scheduler that automatically triggers the seat allocation logic 2 hours before a confirmed exam.

⚙️ Intelligent Rules Engine:

Randomization only occurs if an admin confirms the exam slot.

Admin confirmation is locked 3.5 hours before the exam.

Seating data is automatically cleared 4 hours after an exam concludes.

🧱 Block Allocation: Implements a block-based randomization strategy, grouping students from the same branch into contiguous blocks within an exam hall.

🛠️ Tech Stack
Backend: Java 17, Spring Boot 3

Database: MySQL 8

Data Access: Spring Data JPA (Hibernate)

Frontend Templating: Thymeleaf

Build Tool: Apache Maven

Scheduling: Spring Boot Scheduler

🚀 Getting Started
Follow these instructions to get a copy of the project up and running on your local machine for development and testing purposes.

Prerequisites
Java Development Kit (JDK) 17 or higher

Apache Maven 3.8 or higher

MySQL Server 8 or higher

Installation
Clone the repository:

git clone [https://github.com/aswinaappus-dev/ExamFlow-Seating-Arrangement.git](https://github.com/YOUR_USERNAME/ExamFlow-Seating-Arrangement.git)
cd ExamFlow-Seating-Arrangement

Setup the Database:

Open your MySQL client (e.g., MySQL Workbench).

Create a new database named examflow.

Run the table creation scripts to set up the student, examhall, exam_schedule, and stud_rand tables.

Configure the Application:

Open the src/main/resources/application.properties file.

Update the spring.datasource.username and spring.datasource.password properties to match your local MySQL credentials.

Build and Run the Application:

Open a terminal in the project root directory.

Run the following Maven command to start the application:

mvn spring-boot:run

The application will be accessible at http://localhost:8080.

📈 Usage
Homepage / Student Login: http://localhost:8080/

Invigilator Dashboard: http://localhost:8080/invigilator

Admin Panel: http://localhost:8080/admin

📂 Project Structure
The project follows a standard layered architecture to ensure separation of concerns and maintainability.

src/main/java/com/examflow/
├── model/         # JPA Entities (mapping to database tables)
├── repository/    # Spring Data JPA repositories
├── service/       # Business logic (randomization, admin functions)
├── controller/    # Handles all incoming web requests
├── config/        # Scheduler and other configurations
└── ExamFlowApplication.java # Main entry point

📄 License
This project is licensed under the MIT License - see the LICENSE file for details.

<<<<<<< HEAD
This project was developed by Aswin.
=======
This project was developed by Aswin Appus.
>>>>>>> 1386588417478fe929c089b33fc34e79a2bc2376

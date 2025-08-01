# Online Examination System 

## Overview

This system is a **network-based, JavaFX-powered Online Examination Platform**, designed for local area networks (LAN). It enables teachers to craft and administer examinations and allows students to securely take them from their machines. Developed as part of the *Programming with Frameworks* Group project. Then Further Improved upon during *Software Validation and Testing* where unit tests were implemented.

---

## Features

### 🧑‍🏫 Administrator (Teacher)
- ✍️ Create exams
- 📑 Add multiple-choice questions
- 📚 Maintain a persistent question bank
- 📊 View exam results per student
- 🔐 Restricted to local server control

###  Student
- 🔐 Secure login with credentials
- 🎓 View assigned exams
- ⏳ Take exams with real-time countdown and **auto-submit**
- 📤 Submit answers over local sockets
- 🪞 Optional result feedback

### Anti-Cheating Measures
- 🕵️ Tab switch detection
- 💻 Local connection enforcement only (via socket communication)

---

## ⚙️ Technologies Used

- **Java 17**
- **JavaFX** (FXML-based UI)
- **MySQL** (via XAMPP for remote DB hosting)
- **Gradle** (build automation)
- **SQLite** (optional fallback for offline mode)

---

## Deployment Guide

### Host Setup (Teacher’s Laptop)
1. Install **XAMPP** and start MySQL.
2. Create database `exam_system` and import provided schema.
3. Enable remote MySQL connections via XAMPP settings.
4. Run the **Server** component from this repo.
5. Share your Wi-Fi hotspot with your group.

### 🌐 Client Setup (Students)
1. Clone this repository.
2. Open project in **IntelliJ IDEA** or **NetBeans**.
3. Update the `DatabaseManager.java` and `DatabaseClient.java` with the host's IP:
   ```java
   private static final String URL = "jdbc:mysql://192.168.x.x:3306/exam_system?user=exam_user&password=yourpassword";
   private static final String SERVER_IP = "192.168.x.x";

## Known Limitations
1. Currently works best on LAN; WAN access not configured

2. No GUI-based user management (students must be manually added)

3. Basic encryption; no full SSL/TLS support

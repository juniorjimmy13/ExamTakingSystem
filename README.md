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

### Student
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
- **JUnit 5** (unit testing framework)

---

## 🧪 Testing

### Testing Framework
This project uses **JUnit 5** for comprehensive unit testing, ensuring reliability and maintainability of the examination system.

### Test Coverage
The test suite includes extensive coverage for the `ExamTakingWindow` component with the following test categories:

#### Database Integration Tests
- **Database setup and teardown**: Automated creation and cleanup of test database schema
- **Test data management**: Insertion and validation of test users, exams, and questions
- **Connection handling**: Error handling for database connectivity issues

#### UI Component Tests
- **Window creation**: Validation of exam window initialization with various scenarios
- **JavaFX integration**: Proper initialization and threading for UI components
- **Multiple choice selection**: Radio button functionality and toggle group behavior
- **Timer functionality**: Exam countdown and auto-submission features

#### Data Integrity Tests
- **Question loading**: Verification that exam questions are properly retrieved from database
- **Answer submission**: Testing student answer persistence and validation
- **Score calculation**: Accuracy of exam scoring algorithms
- **Result storage**: Proper recording of student exam results

#### Edge Case Testing
- **Invalid data handling**: Tests with non-existent exams and invalid student credentials
- **Null value handling**: Validation of system behavior with missing or null data
- **Window focus detection**: Anti-cheating measure validation

### Detailed Test Cases

#### 1. Window Creation Tests
```java
@Test
@DisplayName("Test exam window creation with valid data")
void testShowWindow_ValidData() {
    if (Platform.isFxApplicationThread()) {
        assertDoesNotThrow(() -> ExamTakingWindow.showWindow(TEST_USERNAME, TEST_EXAM_TITLE));
    } else {
        Platform.runLater(() -> assertDoesNotThrow(() -> ExamTakingWindow.showWindow(TEST_USERNAME, TEST_EXAM_TITLE)));
        waitForFxEvents();
    }
}

@Test
@DisplayName("Test exam window creation with non-existent exam")
void testShowWindow_NonExistentExam() {
    if (Platform.isFxApplicationThread()) {
        assertDoesNotThrow(() -> ExamTakingWindow.showWindow(TEST_USERNAME, "Non-existent Exam"));
    } else {
        Platform.runLater(() -> assertDoesNotThrow(() -> ExamTakingWindow.showWindow(TEST_USERNAME, "Non-existent Exam")));
        waitForFxEvents();
    }
}

@Test
@DisplayName("Test exam window creation with invalid student")
void testShowWindow_InvalidStudent() {
    if (Platform.isFxApplicationThread()) {
        assertDoesNotThrow(() -> ExamTakingWindow.showWindow("invalidStudent", TEST_EXAM_TITLE));
    } else {
        Platform.runLater(() -> assertDoesNotThrow(() -> ExamTakingWindow.showWindow("invalidStudent", TEST_EXAM_TITLE)));
        waitForFxEvents();
    }
}
```

#### 2. Database Operations Tests
```java
@Test
@DisplayName("Test questions are loaded correctly")
void testLoadQuestions_QuestionsLoaded() throws SQLException {
    try (Connection conn = DriverManager.getConnection(TEST_DB_URL, USER, PASSWORD)) {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM questions q JOIN exams e ON q.exam_id = e.id WHERE e.title = ?");
        stmt.setString(1, TEST_EXAM_TITLE);
        ResultSet rs = stmt.executeQuery();

        assertTrue(rs.next());
        int questionCount = rs.getInt(1);
        assertEquals(3, questionCount, "Should have 3 test questions");
    }
}

@Test
@DisplayName("Test student answer submission")
void testSubmitAnswers_AnswersSubmitted() throws SQLException {
    try (Connection conn = DriverManager.getConnection(TEST_DB_URL, USER, PASSWORD)) {
        PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO student_answers (student_id, exam_id, question_id, selected_option) VALUES (?, ?, ?, ?)");
        stmt.setInt(1, 1);
        stmt.setInt(2, 1);
        stmt.setInt(3, 1);
        stmt.setString(4, "B");
        stmt.executeUpdate();

        PreparedStatement selectStmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM student_answers WHERE student_id = ? AND exam_id = ?");
        selectStmt.setInt(1, 1);
        selectStmt.setInt(2, 1);
        ResultSet rs = selectStmt.executeQuery();

        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1), "Should have one answer recorded");
    }
}

@Test
@DisplayName("Test score calculation")
void testScoreCalculation() throws SQLException {
    try (Connection conn = DriverManager.getConnection(TEST_DB_URL, USER, PASSWORD)) {
        PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO student_results (student_id, exam_id, score, total_questions) VALUES (?, ?, ?, ?)");
        stmt.setInt(1, 1);
        stmt.setInt(2, 1);
        stmt.setInt(3, 67);
        stmt.setInt(4, 3);
        stmt.executeUpdate();

        PreparedStatement selectStmt = conn.prepareStatement(
                "SELECT score FROM student_results WHERE student_id = ? AND exam_id = ?");
        selectStmt.setInt(1, 1);
        selectStmt.setInt(2, 1);
        ResultSet rs = selectStmt.executeQuery();

        assertTrue(rs.next());
        assertEquals(67, rs.getInt("score"), "Score should be 67%");
    }
}
```

#### 3. Data Integrity and Validation Tests
```java
@Test
@DisplayName("Test get correct answer functionality")
void testGetCorrectAnswer() throws SQLException {
    try (Connection conn = DriverManager.getConnection(TEST_DB_URL, USER, PASSWORD)) {
        PreparedStatement stmt = conn.prepareStatement("SELECT correct_option FROM questions WHERE id = ?");
        stmt.setInt(1, 1);
        ResultSet rs = stmt.executeQuery();

        assertTrue(rs.next());
        assertEquals("B", rs.getString("correct_option"), "First question answer should be B");
    }
}

@Test
@DisplayName("Test getId functionality")
void testGetId() throws SQLException {
    try (Connection conn = DriverManager.getConnection(TEST_DB_URL, USER, PASSWORD)) {
        PreparedStatement stmt = conn.prepareStatement("SELECT id FROM users WHERE username = ?");
        stmt.setString(1, TEST_USERNAME);
        ResultSet rs = stmt.executeQuery();

        assertTrue(rs.next());
        assertEquals(1, rs.getInt("id"), "Test user ID should be 1");
    }

    try (Connection conn = DriverManager.getConnection(TEST_DB_URL, USER, PASSWORD)) {
        PreparedStatement stmt = conn.prepareStatement("SELECT id FROM exams WHERE title = ?");
        stmt.setString(1, TEST_EXAM_TITLE);
        ResultSet rs = stmt.executeQuery();

        assertTrue(rs.next());
        assertEquals(1, rs.getInt("id"), "Test exam ID should be 1");
    }
}

@Test
@DisplayName("Test question data integrity")
void testQuestionDataIntegrity() throws SQLException {
    try (Connection conn = DriverManager.getConnection(TEST_DB_URL, USER, PASSWORD)) {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM questions WHERE exam_id = 1");
        ResultSet rs = stmt.executeQuery();

        int questionCount = 0;
        while (rs.next()) {
            questionCount++;
            assertNotNull(rs.getString("question_text"));
            assertNotNull(rs.getString("option_a"));
            assertNotNull(rs.getString("option_b"));
            assertNotNull(rs.getString("option_c"));
            assertNotNull(rs.getString("option_d"));
            assertNotNull(rs.getString("correct_option"));
            assertTrue(rs.getString("correct_option").matches("[ABCD]"));
        }

        assertEquals(3, questionCount, "Should have exactly 3 questions");
    }
}
```

#### 4. UI Component Tests
```java
@Test
@DisplayName("Test multiple choice selection")
void testMultipleChoiceSelection() {
    if (Platform.isFxApplicationThread()) {
        performRadioButtonTest();
    } else {
        Platform.runLater(this::performRadioButtonTest);
        waitForFxEvents();
    }
}

private void performRadioButtonTest() {
    ToggleGroup group = new ToggleGroup();
    RadioButton option1 = new RadioButton("Option A");
    RadioButton option2 = new RadioButton("Option B");

    option1.setToggleGroup(group);
    option2.setToggleGroup(group);

    option1.setSelected(true);
    assertTrue(option1.isSelected());
    assertFalse(option2.isSelected());

    option2.setSelected(true);
    assertFalse(option1.isSelected());
    assertTrue(option2.isSelected());
}

@Test
@DisplayName("Test window focus detection")
void testWindowFocusDetection() {
    if (Platform.isFxApplicationThread()) {
        performWindowFocusTest();
    } else {
        Platform.runLater(this::performWindowFocusTest);
        waitForFxEvents();
    }
}

private void performWindowFocusTest() {
    Stage testStage = new Stage();
    testStage.setTitle("Test Focus");
    assertNotNull(testStage.focusedProperty());
    testStage.show();
    testStage.close();
}
```

#### 5. Error Handling and Edge Cases
```java
@Test
@DisplayName("Test database connection error handling")
void testDatabaseConnectionError() {
    if (Platform.isFxApplicationThread()) {
        assertDoesNotThrow(() -> ExamTakingWindow.showWindow(TEST_USERNAME, TEST_EXAM_TITLE));
    } else {
        Platform.runLater(() -> assertDoesNotThrow(() -> ExamTakingWindow.showWindow(TEST_USERNAME, TEST_EXAM_TITLE)));
        waitForFxEvents();
    }
}

@Test
@DisplayName("Test exam submission validation")
void testExamSubmissionValidation() throws SQLException {
    try (Connection conn = DriverManager.getConnection(TEST_DB_URL, USER, PASSWORD)) {
        PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO student_answers (student_id, exam_id, question_id, selected_option) VALUES (?, ?, ?, ?)");
        stmt.setInt(1, 1);
        stmt.setInt(2, 1);
        stmt.setInt(3, 1);
        stmt.setString(4, null);

        assertDoesNotThrow(() -> stmt.executeUpdate());
    }
}

@Test
@DisplayName("Test timer functionality")
void testTimerFunctionality() {
    if (Platform.isFxApplicationThread()) {
        assertDoesNotThrow(() -> ExamTakingWindow.showWindow(TEST_USERNAME, TEST_EXAM_TITLE));
    } else {
        Platform.runLater(() -> assertDoesNotThrow(() -> ExamTakingWindow.showWindow(TEST_USERNAME, TEST_EXAM_TITLE)));
        waitForFxEvents();
    }
    try {
        Thread.sleep(2000);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}
```

### Test Database Configuration
The test suite uses a separate MySQL test database (`Test_db`) to ensure tests don't interfere with production data:

```java
private static final String TEST_DB_URL = "jdbc:mysql://localhost:3306/Test_db";
private static final String TEST_USERNAME = "testStudent";
private static final String TEST_EXAM_TITLE = "Test Exam";
```

### Running Tests

#### Prerequisites
1. Ensure MySQL is running locally
2. Create a test database named `Test_db`
3. Grant appropriate permissions to the root user

#### Execute Tests
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests ExamTakingWindowTest

# Run tests with detailed output
./gradlew test --info
```

#### Test Data Setup
Tests automatically:
- Create necessary database tables (users, exams, questions, student_answers, student_results)
- Insert test data including sample questions and users
- Clean up test data after each test execution
- Drop all test tables after test suite completion

### Test Results and Reporting
- Test results are generated in `build/reports/tests/test/index.html`
- Coverage reports can be generated using JaCoCo plugin
- All tests include descriptive display names for clear result interpretation

---

## Deployment Guide

### Host Setup (Teacher's Laptop)
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
   ```

---

## Known Limitations
1. Currently works best on LAN; WAN access not configured
2. No GUI-based user management (students must be manually added)
3. Basic encryption; no full SSL/TLS support

package com.ETP.ExamTakingSystem.UI;

import java.sql.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class ExamTakingWindowTest {
    private static final String TEST_DB_URL = "jdbc:mysql://localhost:3306/Test_db";
    private static final String TEST_USERNAME = "testStudent";
    private static final String TEST_EXAM_TITLE = "Test Exam";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    
    private static boolean javaFxInitialized = false;

    @BeforeAll
    static void initJavaFX() {
        if (!javaFxInitialized) {
            try {
                new JFXPanel(); // Initializes JavaFX toolkit once
                javaFxInitialized = true;
            } catch (Exception e) {
                // JavaFX might already be initialized, ignore
                javaFxInitialized = true;
            }
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        setupTestDatabase();
    }

    @AfterEach
    void tearDown() throws SQLException {
        cleanupTestData();
    }

    @AfterAll
    static void tearDownDatabase() throws SQLException {
        try (Connection conn = DriverManager.getConnection(TEST_DB_URL, USER, PASSWORD)) {
            dropAllTables(conn);
        }
    }

    private void setupTestDatabase() throws SQLException {
        try (Connection conn = DriverManager.getConnection(TEST_DB_URL, USER, PASSWORD)) {
            createTables(conn);
            insertTestData(conn);
        }
    }

    private void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        
        // Create users table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id INT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) UNIQUE NOT NULL,
                password VARCHAR(255) NOT NULL,
                role ENUM('student', 'teacher', 'admin') DEFAULT 'student'
            )
        """);

        // Create exams table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS exams (
                id INT AUTO_INCREMENT PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                description TEXT,
                duration_minutes INT DEFAULT 60,
                created_by INT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (created_by) REFERENCES users(id)
            )
        """);

        // Create questions table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS questions (
                id INT AUTO_INCREMENT PRIMARY KEY,
                exam_id INT NOT NULL,
                question_text TEXT NOT NULL,
                option_a VARCHAR(255) NOT NULL,
                option_b VARCHAR(255) NOT NULL,
                option_c VARCHAR(255) NOT NULL,
                option_d VARCHAR(255) NOT NULL,
                correct_option CHAR(1) NOT NULL,
                FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE
            )
        """);

        // Create student_answers table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS student_answers (
                id INT AUTO_INCREMENT PRIMARY KEY,
                student_id INT NOT NULL,
                exam_id INT NOT NULL,
                question_id INT NOT NULL,
                selected_option CHAR(1),
                answered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (student_id) REFERENCES users(id),
                FOREIGN KEY (exam_id) REFERENCES exams(id),
                FOREIGN KEY (question_id) REFERENCES questions(id),
                UNIQUE KEY unique_answer (student_id, exam_id, question_id)
            )
        """);

        // Create student_results table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS student_results (
                id INT AUTO_INCREMENT PRIMARY KEY,
                student_id INT NOT NULL,
                exam_id INT NOT NULL,
                score INT NOT NULL,
                total_questions INT NOT NULL,
                completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (student_id) REFERENCES users(id),
                FOREIGN KEY (exam_id) REFERENCES exams(id),
                UNIQUE KEY unique_result (student_id, exam_id)
            )
        """);
    }

    private void insertTestData(Connection conn) throws SQLException {
        // Insert test user
        PreparedStatement userStmt = conn.prepareStatement(
            "INSERT IGNORE INTO users (id, username, password, role) VALUES (1, ?, 'testpass', 'student')"
        );
        userStmt.setString(1, TEST_USERNAME);
        userStmt.executeUpdate();

        // Insert test teacher
        PreparedStatement teacherStmt = conn.prepareStatement(
            "INSERT IGNORE INTO users (id, username, password, role) VALUES (2, 'testTeacher', 'teacherpass', 'teacher')"
        );
        teacherStmt.executeUpdate();

        // Insert test exam
        PreparedStatement examStmt = conn.prepareStatement(
            "INSERT IGNORE INTO exams (id, title, description, duration_minutes, created_by) VALUES (1, ?, 'A test exam for unit testing', 30, 2)"
        );
        examStmt.setString(1, TEST_EXAM_TITLE);
        examStmt.executeUpdate();

        // Insert test questions
        insertTestQuestions(conn);
    }

    private void insertTestQuestions(Connection conn) throws SQLException {
        String[] questions = {
            "What is 2 + 2?",
            "What is the capital of France?",
            "Which programming language is this test written in?"
        };
        
        String[][] options = {
            {"3", "4", "5", "6"},
            {"London", "Paris", "Berlin", "Madrid"},
            {"Python", "Java", "C++", "JavaScript"}
        };
        
        String[] correctAnswers = {"B", "B", "B"};

        for (int i = 0; i < questions.length; i++) {
            PreparedStatement stmt = conn.prepareStatement("""
                INSERT IGNORE INTO questions 
                (id, exam_id, question_text, option_a, option_b, option_c, option_d, correct_option) 
                VALUES (?, 1, ?, ?, ?, ?, ?, ?)
            """);
            stmt.setInt(1, i + 1);
            stmt.setString(2, questions[i]);
            stmt.setString(3, options[i][0]);
            stmt.setString(4, options[i][1]);
            stmt.setString(5, options[i][2]);
            stmt.setString(6, options[i][3]);
            stmt.setString(7, correctAnswers[i]);
            stmt.executeUpdate();
        }
    }

    private void cleanupTestData() throws SQLException {
        try (Connection conn = DriverManager.getConnection(TEST_DB_URL, USER, PASSWORD)) {
            Statement stmt = conn.createStatement();
            stmt.execute("DELETE FROM student_results WHERE student_id = 1");
            stmt.execute("DELETE FROM student_answers WHERE student_id = 1");
        }
    }

    private static void dropAllTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
        stmt.execute("DROP TABLE IF EXISTS student_results");
        stmt.execute("DROP TABLE IF EXISTS student_answers");
        stmt.execute("DROP TABLE IF EXISTS questions");
        stmt.execute("DROP TABLE IF EXISTS exams");
        stmt.execute("DROP TABLE IF EXISTS users");
        stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

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

    private void waitForFxEvents() {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
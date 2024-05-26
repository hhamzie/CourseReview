package edu.virginia.sde.reviews;

import org.junit.jupiter.api.*;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

public class DatabaseDriverTests {
    private static final String TEST_DB_FILENAME = "test_database.db";
    private DatabaseDriver dbDriver;

    @BeforeEach
    public void setUp() throws SQLException {
        dbDriver = new DatabaseDriver(TEST_DB_FILENAME);
        try {
            dbDriver.connect();
        } catch (SQLException e) {
            fail("Setup failed: " + e.getMessage());
        }
    }

    @AfterEach
    public void tearDown() throws SQLException {
        try {
            dbDriver.disconnect();
            new File(TEST_DB_FILENAME).delete();
        } catch (SQLException e) {
            fail("Teardown failed: " + e.getMessage());
        }
    }

    @Test
    public void testInsertAndRetrieveUser() throws SQLException {
        dbDriver.insertUser("testUser", "password123");
        ResultSet rs = dbDriver.getUser("testUser");
        assertTrue(rs.next(), "User should exist in the database");
        assertEquals("testUser", rs.getString("Username"), "Username should match");
    }

    @Test
    public void testInsertAndUpdateReview() throws SQLException {

        dbDriver.insertUser("reviewer", "password");
        dbDriver.insertCourse("CS", 3140, "Software Development Essentials");

        int reviewId = dbDriver.insertReview(1, 1, 5, "2023-03-15 12:00:00", "Great course!");
        System.out.println(reviewId);
        dbDriver.updateReview(reviewId, 4, "Updated comment");

        ResultSet rs = dbDriver.getAllReviews();

        boolean found = false;
        while (rs.next()) {
            if (rs.getInt("ReviewID") == reviewId) {
                assertEquals(4, rs.getInt("Rating"), "Rating should be updated");
                assertEquals("Updated comment", rs.getString("Comment"), "Comment should be updated");
                found = true;
                break;
            }
        }
        assertTrue(found, "Review with the correct ID should exist in the database");
    }

    @Test
    public void testDeleteUser() throws SQLException {
        dbDriver.insertUser("deleteUser", "password");
        dbDriver.deleteUser("deleteUser");

        ResultSet rs = dbDriver.getUser("deleteUser");
        assertFalse(rs.next(), "User should be deleted from the database");
    }

    @Test
    public void testDeleteCourse() throws SQLException {
        dbDriver.insertCourse("MATH", 1010, "Calculus I");
        ResultSet rs1 = dbDriver.getAllCourses();
        assertTrue(rs1.next(), "Course should exist before deletion");

        int courseId = rs1.getInt("CourseID");
        dbDriver.deleteCourse(courseId);

        ResultSet rs2 = dbDriver.getAllCourses();
        assertFalse(rs2.next(), "Course should be deleted from the database");
    }

    @Test
    public void testUpdateUser() throws SQLException {
        String username = "updateUser";
        dbDriver.insertUser(username, "oldPassword");
        dbDriver.updateUser(username, "newPassword");

        ResultSet rs = dbDriver.getUser(username);
        assertTrue(rs.next(), "User should exist");
        assertEquals("newPassword", rs.getString("Password"), "Password should be updated");
    }

    @Test
    public void testDeleteReview() throws SQLException {
        // Delete Review
        dbDriver.insertUser("reviewer", "password");
        dbDriver.insertCourse("CS", 3140, "Software Development Essentials");
        dbDriver.insertReview(1, 1, 5, "2023-03-15 12:00:00", "Great course!");

        ResultSet rs1 = dbDriver.getAllReviews();
        assertTrue(rs1.next(), "Review should exist before deletion");

        int reviewId = rs1.getInt("ReviewID");
        dbDriver.deleteReview(reviewId);

        ResultSet rs2 = dbDriver.getAllReviews();
        assertFalse(rs2.next(), "Review should be deleted from the database");
    }


}

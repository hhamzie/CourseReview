package edu.virginia.sde.reviews;

import java.sql.*;


/*
Sources:

- https://www.sqltutorial.org/sql-cheat-sheet/ for database logic
- https://www.youtube.com/watch?v=-EGxNA7qfCM youtube video for help with initalization
- Chatgpt for help with coding logic for initalize as well as insert review

 */
public class DatabaseDriver {
    private final String sqliteFilename;
    private Connection connection;

    public DatabaseDriver(String sqliteFilename) {
        this.sqliteFilename = sqliteFilename;
    }

    public void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            throw new IllegalStateException("The connection is already opened");
        }
        connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteFilename);
        connection.createStatement().execute("PRAGMA foreign_keys = ON");
        connection.setAutoCommit(true);
        initializeTables();
    }

    private void initializeTables() {
        try (Statement stmt = connection.createStatement()) {
            // User Table
            String sqlUser = "CREATE TABLE IF NOT EXISTS User (" +
                    "UserID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "Username TEXT UNIQUE NOT NULL," +
                    "Password TEXT NOT NULL);";
            stmt.execute(sqlUser);

            // Course Table
            String sqlCourse = "CREATE TABLE IF NOT EXISTS Course (" +
                    "CourseID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "Subject TEXT NOT NULL," +
                    "Number INTEGER NOT NULL," +
                    "Title TEXT NOT NULL);";
            stmt.execute(sqlCourse);

            // Review Table
            String sqlReview = "CREATE TABLE IF NOT EXISTS Review (" +
                    "ReviewID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "UserID INTEGER NOT NULL," +
                    "CourseID INTEGER NOT NULL," +
                    "Rating INTEGER NOT NULL," +
                    "Timestamp TEXT NOT NULL," +
                    "Comment TEXT," +
                    "FOREIGN KEY(UserID) REFERENCES User(UserID)," +
                    "FOREIGN KEY(CourseID) REFERENCES Course(CourseID) ON DELETE CASCADE);";

            stmt.execute(sqlReview);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }



    public boolean insertUser(String username, String password) {
        String sql = "INSERT INTO User(Username, Password) VALUES(?,?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public boolean insertCourse(String subject, int number, String title) {
        String sql = "INSERT INTO Course(Subject, Number, Title) VALUES(?,?,?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, subject);
            pstmt.setInt(2, number);
            pstmt.setString(3, title);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public int insertReview(int userID, int courseID, int rating, String timestamp, String comment) {
        String sql = "INSERT INTO Review(UserID, CourseID, Rating, Timestamp, Comment) VALUES(?,?,?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, userID);
            pstmt.setInt(2, courseID);
            pstmt.setInt(3, rating);
            pstmt.setString(4, timestamp);
            pstmt.setString(5, comment);
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Returns the generated review ID
                } else {
                    throw new SQLException("Creating review failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return -1; // Indicate failure
        }
    }


    public ResultSet getAllUsers() throws SQLException {
        String sql = "SELECT * FROM User";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    public ResultSet getUser(String username) throws SQLException {
        String sql = "SELECT * FROM User WHERE Username = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, username);
        return pstmt.executeQuery();
    }



    public ResultSet getAllCourses() throws SQLException {
        String sql = "SELECT * FROM Course";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }
    public ResultSet getCourse(String subject, String number) throws SQLException {
        String sql = "SELECT * FROM Course WHERE Subject = ? AND Number = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, subject);
        pstmt.setInt(2, Integer.parseInt(number));
        return pstmt.executeQuery();
    }

    public ResultSet getCourseById(int courseId) throws SQLException {
        String sql = "SELECT * FROM Course WHERE CourseID = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, courseId);
        return pstmt.executeQuery();
    }


    public ResultSet getAllReviews() throws SQLException {
        String sql = "SELECT * FROM Review";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }
    public ResultSet getAllReviewsForSpecificCourse(int courseId) throws SQLException {
        String sql = "SELECT * FROM Review WHERE CourseID = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, courseId);
        return pstmt.executeQuery();
    }

    public ResultSet getReview(String rating, String comment, String timestamp ) throws SQLException {
        String sql = "SELECT * FROM Review WHERE Rating = ? AND Comment = ? AND Timestamp = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, Integer.parseInt(rating));
        pstmt.setString(2, comment);
        pstmt.setString(3, timestamp);
        return pstmt.executeQuery();
    }
    public ResultSet getReviewByUserIDAndCourseID(int userID, int courseID) throws SQLException {
        String sql = "SELECT * FROM Review WHERE UserID = ? AND CourseID = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, userID);
        pstmt.setInt(2, courseID);
        return pstmt.executeQuery();
    }



    public boolean checkIfUserReviewedCourse(String username, int courseID) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Review JOIN User ON Review.UserID = User.UserID WHERE User.Username = ? AND Review.CourseID = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setInt(2, courseID);

            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0;
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return false;
    }


    public boolean updateCourse(int courseId, String newSubject, int newNumber, String newTitle) throws SQLException {
        String sql = "UPDATE Course SET Subject = ?, Number = ?, Title = ? WHERE CourseID = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, newSubject);
        pstmt.setInt(2, newNumber);
        pstmt.setString(3, newTitle);
        pstmt.setInt(4, courseId);
        int rowsAffected = pstmt.executeUpdate();
        return rowsAffected > 0;
    }

    public boolean updateReview(int reviewId, int newRating, String newComment) throws SQLException {
        String sql = "UPDATE Review SET Rating = ?, Comment = ? WHERE ReviewID = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, newRating);
        pstmt.setString(2, newComment);
        pstmt.setInt(3, reviewId);
        int rowsAffected = pstmt.executeUpdate();
        return rowsAffected > 0;
    }

    public boolean updateUser(String username, String newPassword) throws SQLException {
        String sql = "UPDATE User SET Password = ? WHERE Username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public boolean deleteCourse(int courseId) throws SQLException {
        String sql = "DELETE FROM Course WHERE CourseID = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, courseId);
        int rowsAffected = pstmt.executeUpdate();
        return rowsAffected > 0;
    }

    public boolean deleteReview(int reviewId) throws SQLException {
        String sql = "DELETE FROM Review WHERE ReviewID = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, reviewId);
        int rowsAffected = pstmt.executeUpdate();
        return rowsAffected > 0;
    }

    public boolean deleteUser(String username) throws SQLException {
        String sql = "DELETE FROM User WHERE Username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public ResultSet getReviewsForCourse(int courseId) throws SQLException {
        String sql = "SELECT * FROM Review WHERE CourseID = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, courseId);
        return pstmt.executeQuery();
    }


    public void executeSql(String sql) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void closeConnection() throws SQLException {
        if (connection != null) {
            if (!connection.getAutoCommit()) {
                connection.rollback();
            }
            connection.close();
        }
    }

    public void disconnect() throws SQLException {
        connection.close();
    }

    public ResultSet getUserReviews(String username) throws SQLException {
        String sql = "SELECT * FROM Review WHERE UserID = (SELECT UserID FROM User WHERE Username = ?)";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setString(1, username);
        return pstmt.executeQuery();
    }



    public ResultSet getCourseByReview(int reviewId) throws SQLException {
        String sql = "SELECT Course.* FROM Course JOIN Review ON Course.CourseID = Review.CourseID WHERE Review.ReviewID = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, reviewId);
        return pstmt.executeQuery();
    }



}

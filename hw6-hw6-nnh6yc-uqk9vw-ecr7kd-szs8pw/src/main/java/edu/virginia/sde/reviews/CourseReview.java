package edu.virginia.sde.reviews;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseReview extends Review {
    private double avgRating;
    private Review[] reviews;

    public CourseReview(Course course, int rating, User user, String timestamp, String comment, double avgRating, Review[] reviews) {
        super(course, rating, user, timestamp, comment);
        this.avgRating = avgRating;
        this.reviews = reviews;
    }

    public double getAvgRating() {
        return avgRating;
    }

    public Review[] getReviews() {
        return reviews;
    }

    private Course getCourseByCourseId(int courseId) {
        try {
            DatabaseDriver databaseDriver = new DatabaseDriver("sql.db");
            databaseDriver.connect();

            ResultSet resultSet = databaseDriver.getCourseById(courseId);

            if (resultSet.next()) {
                String subject = resultSet.getString("Subject");
                int number = resultSet.getInt("Number");
                String title = resultSet.getString("Title");

                return new Course(subject, number, title);
            }

            resultSet.close();
            databaseDriver.disconnect(); // Make sure to disconnect when done
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


}

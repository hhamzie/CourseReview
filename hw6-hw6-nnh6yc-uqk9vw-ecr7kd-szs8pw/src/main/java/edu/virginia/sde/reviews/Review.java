package edu.virginia.sde.reviews;

import java.sql.Timestamp;

public class Review {
    private Course course;
    private User user;
    private String timestamp;
    private String comment;

    private String courseSubject;
    private String courseNumber;
    private String courseTitle;
    private int rating;


    public Review(Course course, int rating, User user, String timestamp, String comment) {
        this.course = course;
        this.rating = rating;
        this.user = user;
        this.timestamp = timestamp;
        this.comment = comment;
    }
    public Review(int rating, String comment, String timestamp) {
        this.rating = rating;
        this.timestamp = timestamp;
        this.comment = comment;
    }
    public Review(Course course, int rating) {
        this.course = course;
        courseSubject = course.getSubject();
        courseNumber = Integer.toString(course.getNumber());
        courseTitle = course.getTitle();
        //rating = (int) course.getRating();
        this.rating = rating;
    }

    // Getters
    public Course getCourse() { return course; }
    public int getRating() { return rating; }
    public User getUser() { return user; }
    public String getTimestamp() { return timestamp; }
    public String getComment() { return comment; }

    @Override
    public String toString() {
        return rating + " " + timestamp + " " + comment;
    }
}

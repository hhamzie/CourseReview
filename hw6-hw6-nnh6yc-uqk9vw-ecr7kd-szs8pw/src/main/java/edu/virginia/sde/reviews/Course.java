package edu.virginia.sde.reviews;

import java.util.List;

public class Course {
    //size of 4, 4, and 50 chars
    private String subject;
    private int number;
    private String title;

    private double rating;

    public Course(String subject, int number, String title){
        this.subject = subject;
        this.number = number;
        this.title = title;
    }
    public String getSubject() {
        return subject;
    }

    public void setRating(double rating){
        this.rating = rating;

    }
    public double getRating(){
        return rating;

    }

    public int getNumber(){return number;}

    public String getTitle(){return title;}

    @Override
    public String toString() {
        return subject + " " + number+ " " + title;
    }
}

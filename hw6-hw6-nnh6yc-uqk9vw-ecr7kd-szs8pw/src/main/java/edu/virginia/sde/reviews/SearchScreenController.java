package edu.virginia.sde.reviews;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import static edu.virginia.sde.reviews.LoginScreenController.username;


/*
Sources:

        - https://www.youtube.com/watch?v=vego72w5kPU
        - https://stackoverflow.com/questions/18941093/how-to-fill-up-a-tableview-with-database-data
        - https://www.youtube.com/watch?v=tw_NXq08NUE
         All above is just tableview help

        - https://www.youtube.com/watch?v=F7-gdu5ru3o
        - https://itecnote.com/tecnote/javafx-tableview-detect-a-doubleclick-on-a-cell/
          Double click help

         -chrome-extension://efaidnbmnnnibpcajpcglclefindmkaj/https://docs.oracle.com/javafx/2/ui_controls/jfxpub-ui_controls.pdf
          javafx cheatsheet

          https://www.w3schools.com/java/java_try_catch.asp for try catch help

          -chatgpt for alot of the tableview coding logic as well as code lines to do with cell factory value and imprting variables from other classes

        */
public class SearchScreenController {
    @FXML
    private Label messageLabel;
    @FXML
    private TextField addSubject;
    @FXML
    private TextField addName;
    @FXML
    private TextField addNumber;
    @FXML
    private TextField subjectSearch;
    @FXML
    private TextField nameSearch;
    @FXML
    private TextField numberSearch;
    @FXML
    private TableView<Course> tableView;
    @FXML
    private TableColumn<Course, String> subjectColumn;
    @FXML
    private TableColumn<Course, Integer> numberColumn;
    @FXML
    private TableColumn<Course, String> nameColumn;
    @FXML
    private TableColumn<Course, Double> ratingColumn;
    public static int courseid;

    public static double ratingAVG;

    private String userLogin = username;

    private ObservableList<Course> courseData = FXCollections.observableArrayList();

    DatabaseDriver driver = new DatabaseDriver("sql.db");


    @FXML
    private void initialize() throws SQLException {
        driver.connect();
        ResultSet dbCourse = driver.getAllCourses();

        try {
            if (!dbCourse.isBeforeFirst()) {
                return;
            } else {
                while (dbCourse.next()) {
                    Course newCourse = new Course(dbCourse.getString("Subject"),
                            Integer.parseInt(dbCourse.getString("Number")),
                            dbCourse.getString("Title"));

                    double averageRating = getAverageReviewRating(Integer.parseInt(dbCourse.getString("CourseID")));
                    System.out.println(averageRating);
                    newCourse.setRating(averageRating);

                    courseData.add(newCourse);
                }
            }
        } finally {
            driver.disconnect();
        }



        subjectColumn.setCellValueFactory(new PropertyValueFactory<Course, String>("subject"));
        numberColumn.setCellValueFactory(new PropertyValueFactory<Course, Integer>("number"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<Course, String>("title"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<Course, Double>("rating"));


        tableView.setItems(courseData);

        tableView.setRowFactory(tv -> {
            TableRow<Course> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && (!row.isEmpty())) {
                    Course rowData = row.getItem();
                    try {
                        handleRowDoubleClick(rowData);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            return row;
        });

    }


    public void handleRowDoubleClick(Course course) throws SQLException {
        driver.connect();

        var dbCourse = driver.getCourse(course.getSubject(),Integer.toString(course.getNumber()));

        courseid = Integer.parseInt(dbCourse.getString("CourseID"));

        ratingAVG = course.getRating();

        driver.disconnect();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("coursereview-screen.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleSearch(ActionEvent actionEvent) {
        String subjectInput = subjectSearch.getText().trim().toUpperCase();
        String nameInput = nameSearch.getText().trim().toLowerCase();
        String numberInput = numberSearch.getText().trim();

        ObservableList<Course> filteredCourses = FXCollections.observableArrayList();

        for (Course course : courseData) {
            boolean subjectMatches = subjectInput.isEmpty() || course.getSubject().equalsIgnoreCase(subjectInput);
            boolean numberMatches = numberInput.isEmpty() || String.valueOf(course.getNumber()).equals(numberInput);
            boolean nameMatches = nameInput.isEmpty() || course.getTitle().toLowerCase().contains(nameInput);

            if (subjectMatches && numberMatches && nameMatches) {
                filteredCourses.add(course);
            }
        }

        tableView.setItems(filteredCourses);

    }

    public void handleAddCourse(ActionEvent actionEvent) throws SQLException {
        String courseSubject = addSubject.getText();
        String courseName = addName.getText();
        String courseNumber = addNumber.getText();

        if (courseSubject.length() <= 4 && courseName.length() <= 50 && courseNumber.length() <= 4) {
            try {
                driver.connect();
                ResultSet dbCourse = driver.getCourse(courseSubject, courseNumber);
                if (dbCourse.next()) {
                    driver.disconnect();
                    messageLabel.setText("Course Exists");
                    throw new IllegalStateException("Course Exists");
                }

                boolean success = driver.insertCourse(courseSubject, Integer.parseInt(courseNumber), courseName);
                if (success) {
                    System.out.println("Course added successfully.");
                    Course newCourse = new Course(courseSubject, Integer.parseInt(courseNumber), courseName);
                    courseData.add(newCourse);

                }
                else {
                    System.out.println("Failed to add course.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                driver.disconnect();
            }
        }
    }



    public void handleMyReviews(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("myreviews-screen.fxml"));
            Parent signupView = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(signupView, 650, 400);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public double getAverageReviewRating(int courseID) throws SQLException {
        var reviews = driver.getReviewsForCourse(courseID);
        double totalRating = 0.0;
        double reviewCount = 0.0;

        while (reviews.next()) {
            int rating = reviews.getInt("Rating");
            totalRating += rating;
            reviewCount++;
        }

        if (reviewCount == 0) {
            return 0.0;
        }

        return totalRating / reviewCount;
    }


    public void handleLogout(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-screen.fxml"));
            Parent signupView = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(signupView, 650, 400);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

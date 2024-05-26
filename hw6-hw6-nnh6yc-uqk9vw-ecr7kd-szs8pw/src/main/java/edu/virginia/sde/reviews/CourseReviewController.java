package edu.virginia.sde.reviews;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;

import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import static edu.virginia.sde.reviews.LoginScreenController.username;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import static edu.virginia.sde.reviews.SearchScreenController.courseid;
import static edu.virginia.sde.reviews.SearchScreenController.ratingAVG;
import static java.lang.Integer.parseInt;


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
          - https://www.w3schools.com/java/java_try_catch.asp for try catch help

          -chatgpt for alot of the tableview coding logic as well as code lines to do with cell factory value and imprting variables from other classes


stuff used here is also what i used for every controller
        */
public class CourseReviewController {

    @FXML
    private Label courseTitleField;

    @FXML
    private TextArea CommentTextArea;
    @FXML
    private ChoiceBox ratingChoiceBox;


    @FXML
    private TableColumn<Review, Integer> ratingColumn;
    @FXML
    private TableColumn<Review, String> commentColumn;
    @FXML
    private TableColumn<Review, String> addTimeStamp;
    @FXML
    private TableView<Review> reviewsTableView;

    @FXML
    private Label avgRating;
    private String userLogin = username;
    private Course course;
    private String courseNum;
    private String courseSubject;

    private ObservableList<Review> reviewData = FXCollections.observableArrayList();
    DatabaseDriver driver = new DatabaseDriver("sql.db");

    public static Review userReview;

    @FXML
    private void initialize() throws SQLException {
        setCourseData(ratingAVG);

        driver.connect();

        ResultSet dbReview = driver.getAllReviewsForSpecificCourse(courseid);

        try {
            if (!dbReview.isBeforeFirst()) {
                return;
            } else {
                while (dbReview.next()) {
                    Review newReview= new Review(Integer.parseInt(dbReview.getString("Rating")), dbReview.getString("Comment"), dbReview.getString("Timestamp"));
                    reviewData.add(newReview);
                }
            }
        } finally {

            driver.disconnect();
        }

        ratingColumn.setCellValueFactory(new PropertyValueFactory<Review, Integer>("rating"));
        commentColumn.setCellValueFactory(new PropertyValueFactory<Review, String>("comment"));
        addTimeStamp.setCellValueFactory(new PropertyValueFactory<Review, String>("timestamp"));

        reviewsTableView.setItems(reviewData);
    }


    public void handleSubmitButton(ActionEvent actionEvent) throws SQLException {
        Integer ratingValue = (Integer) ratingChoiceBox.getValue();
        String reviewComment = CommentTextArea.getText();
        Timestamp timeStamp = new Timestamp(System.currentTimeMillis());

        try {
            driver.connect();

            var dbUser = driver.getUser(username);
            int userID = Integer.parseInt(dbUser.getString("UserID"));

            var dbReview = driver.getReviewByUserIDAndCourseID(userID, courseid);

            if (dbReview.next()) {
                driver.disconnect();
                System.out.println("Review Exists");
            }
            driver.insertReview(userID, courseid, ratingValue, timeStamp.toString(), reviewComment);

            boolean hasReviewed = driver.checkIfUserReviewedCourse(username, courseid);

            if (hasReviewed) {
                System.out.println("Review added successfully.");
                Review newReview = new Review(ratingValue, reviewComment, timeStamp.toString());
                reviewData.add(newReview);
                userReview = newReview;
                reviewsTableView.setItems(reviewData);

            }
            else {
                System.out.println("Failed to add review.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            driver.disconnect();
        }
    }

    public void handleEditButton(ActionEvent actionEvent) throws SQLException {
        Integer ratingValue = (Integer) ratingChoiceBox.getValue();
        String reviewComment = CommentTextArea.getText();

        try {
            driver.connect();

            var dbUser = driver.getUser(username);
            int userID = Integer.parseInt(dbUser.getString("UserID"));
            var dbReview = driver.getReviewByUserIDAndCourseID(userID, courseid);

            if (!dbReview.next()) {
                driver.disconnect();
                System.out.println("Review Does Not Exist");
            }

            int reviewID = Integer.parseInt(dbReview.getString("ReviewID"));

            boolean hasUpdated = driver.updateReview(reviewID, ratingValue, reviewComment);
            var newReview = new Review(ratingValue, reviewComment, userReview.getTimestamp());


            if (hasUpdated) {
                System.out.println("Review updated successfully.");
                reviewData.remove(userReview);
                reviewData.add(newReview);
                userReview = newReview;
                reviewsTableView.setItems(reviewData);

            }
            else {
                System.out.println("Failed to update review.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            driver.disconnect();
        }

    }

    public void handleDeleteButton(ActionEvent actionEvent) throws SQLException {
        try {
            driver.connect();

            var dbUser = driver.getUser(username);
            int userID = Integer.parseInt(dbUser.getString("UserID"));
            var dbReview = driver.getReviewByUserIDAndCourseID(userID, courseid);

            if (!dbReview.next()) {
                driver.disconnect();
                System.out.println("Review Does Not Exist");
            }
            driver.deleteReview(Integer.parseInt(dbReview.getString("ReviewID")));

            boolean hasReviewed = driver.checkIfUserReviewedCourse(username, courseid);

            if (!hasReviewed) {
                System.out.println("Review removed successfully.");
                reviewData.remove(userReview);
                reviewsTableView.setItems(reviewData);

            }
            else {
                System.out.println("Failed to remove review.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            driver.disconnect();
        }

    }

    public void setCourseData(double rating) throws SQLException {
        driver.connect();

        var dbCourse = driver.getCourseById(courseid);
        courseTitleField.setText(dbCourse.getString("Title"));


        String text= Double.toString(rating);
        avgRating.setText(text);

        driver.disconnect();

    }

    public void handleBackButton(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("coursesearch-screen.fxml"));
            Parent signupView = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(signupView, 650, 400);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleMyReviewsAction(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("path/to/coursereview-screen.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}

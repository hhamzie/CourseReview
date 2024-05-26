package edu.virginia.sde.reviews;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.scene.control.cell.PropertyValueFactory;
import static edu.virginia.sde.reviews.LoginScreenController.username;
import static edu.virginia.sde.reviews.SearchScreenController.ratingAVG;
import static edu.virginia.sde.reviews.SearchScreenController.courseid;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.collections.ObservableList;


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
          -  - https://www.w3schools.com/java/java_try_catch.asp for try catch help

          -chatgpt for alot of the tableview coding logic as well as code lines to do with cell factory value and imprting variables from other classes

        */
public class MyReviewsScreenController {
    @FXML
    private Label messageLabel;
    @FXML
    private ListView<Review> reviewsListView;

    @FXML
    private TableColumn<Review, String> subjectColumn;
    @FXML
    private TableColumn<Review, Integer> numberColumn;
    @FXML
    private TableColumn<Review, String> titleColumn;
    @FXML
    private TableColumn<Review, Integer> ratingColumn;

    @FXML
    private TableView<Review> reviewsTableView;

    private DatabaseDriver driver = new DatabaseDriver("sql.db");

    private ObservableList<Review> reviewData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        try {
            driver.connect();
            ResultSet dbReview = driver.getUserReviews(username);

            if (!dbReview.isBeforeFirst()) {
                System.out.println("No reviews found for user: " + username);
                return;
            }

            while (dbReview.next()) {
                int courseID = dbReview.getInt("CourseID");
                ResultSet dbCourse = driver.getCourseById(courseID);

                if (dbCourse.next()) {
                    Course newCourse = new Course(
                            dbCourse.getString("Subject"),
                            dbCourse.getInt("Number"),
                            dbCourse.getString("Title")
                    );

                    String empty = "";
                    Review newReview = new Review(
                            newCourse,
                            dbReview.getInt("Rating")
                    );
                    System.out.println(newReview.getRating());

                    reviewData.add(newReview);
                }
            }
            subjectColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCourse().getSubject()));
            numberColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getCourse().getNumber()).asObject());
            titleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCourse().getTitle()));
            ratingColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getRating()).asObject());

            reviewsTableView.setItems(reviewData);

            reviewsTableView.setRowFactory(tv -> {
                TableRow<Review> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && (!row.isEmpty())) {
                        Review rowData = row.getItem();
                        try {
                            handleRowDoubleClick(rowData);
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }

                    }
                });
                return row;
            });

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                driver.disconnect();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    public void handleRowDoubleClick(Review myReview) throws SQLException {
        driver.connect();
        Course course = myReview.getCourse();
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


    public void handleBackAction(ActionEvent actionEvent) {
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
}

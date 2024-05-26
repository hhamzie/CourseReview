package edu.virginia.sde.reviews;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;


public class LoginScreenController {
    @FXML
    private Label messageLabel;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;

    public static String username;

    DatabaseDriver driver = new DatabaseDriver("sql.db");

    public void handleLoginButton(ActionEvent actionEvent) throws SQLException {
        this.username = usernameField.getText();
        String password = passwordField.getText();

        try {
            driver.connect();
            ResultSet dbUser = driver.getUser(username);

            if (dbUser.next()) {
                String dbPassword = dbUser.getString("Password");
                if (password.equals(dbPassword)) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("coursesearch-screen.fxml"));
                        Parent signupView = loader.load();
                        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                        Scene scene = new Scene(signupView, 650, 400);
                        stage.setScene(scene);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Wrong Password");
                }
            } else {
                System.out.println("User not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            driver.disconnect();
        }
    }


    public void handleSignupButton(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("newuser-screen.fxml"));
            Parent signupView = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(signupView, 650, 400);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handleCloseButton(ActionEvent actionEvent) {
        Platform.exit();
    }

}

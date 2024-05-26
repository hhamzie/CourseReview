package edu.virginia.sde.reviews;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NewUserScreenController {
    @FXML
    private Label messageLabel;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    DatabaseDriver driver = new DatabaseDriver("sql.db");

    public void handleBackButton(ActionEvent actionEvent) {
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

    public void handleContinueButton(ActionEvent actionEvent) throws SQLException {
        String username = usernameField.getText();
        String password = passwordField.getText();
        if(password.length() < 8){
            messageLabel.setText("Invalid Password Length");
            throw new IllegalStateException("Invalid Length");
        }
        driver.connect();
        ResultSet dbUser = driver.getUser(username);

        if (dbUser.next()) {
            driver.disconnect();
            messageLabel.setText("User Exists");
            throw new IllegalStateException("User Exists");
        }

        boolean ifInserted = driver.insertUser(username,password);

        if (ifInserted){
            System.out.println("added user");
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-screen.fxml"));
            Parent signupView = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(signupView, 650, 400);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
        driver.disconnect();

    }

}

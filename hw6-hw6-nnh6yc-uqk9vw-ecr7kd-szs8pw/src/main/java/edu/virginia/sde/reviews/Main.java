package edu.virginia.sde.reviews;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login-screen.fxml"));
        Scene scene = new Scene(fxmlLoader.load(),650,400);
        stage.setTitle("Course Review System");
        stage.setScene(scene);
        stage.show();
    }



}

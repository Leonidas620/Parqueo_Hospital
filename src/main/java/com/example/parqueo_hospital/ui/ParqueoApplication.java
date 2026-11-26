package com.example.parqueo_hospital.ui;

import com.example.parqueo_hospital.dao.Database;
import com.example.parqueo_hospital.model.ParkingException;
import com.example.parqueo_hospital.service.ParkingService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class ParqueoApplication extends Application {
    @Override
    public void start(Stage stage) {
        Navigator.init(stage);
        stage.setTitle("Parqueo Hospital Provincial");
        try {
            Session.init(new ParkingService());
        } catch (ParkingException e) {
            e.printStackTrace();
            Dialogs.warn(e.getMessage());
            Platform.exit();
            return;
        }
        Navigator.showLogin();
        stage.show();
    }

    public static void main(String[] args) {
        try {
            Database.verifyConnection();
            launch(args);
        } catch (ParkingException e) {
            System.err.println(e.getMessage());
            if (e.getCause() != null) e.getCause().printStackTrace();
        }
    }
}

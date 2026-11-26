package com.example.parqueo_hospital.ui;

import com.example.parqueo_hospital.model.ParkingException;
import com.example.parqueo_hospital.ui.controller.DialogController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

/** Alertas y formularios modales con el estilo de la aplicación. */
public final class Dialogs {
    private Dialogs() { }

    public static void info(String message) {
        alert(Alert.AlertType.INFORMATION, "Información", message).showAndWait();
    }

    public static void warn(String message) {
        alert(Alert.AlertType.WARNING, "Atención", message).showAndWait();
    }

    /** Pregunta con dos botones nombrados por la acción; devuelve true si se elige el primero. */
    public static boolean confirm(String title, String message, String yesText, String noText) {
        ButtonType yes = new ButtonType(yesText, ButtonBar.ButtonData.OK_DONE);
        ButtonType no = new ButtonType(noText, ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alert = alert(Alert.AlertType.CONFIRMATION, title, message);
        alert.getButtonTypes().setAll(yes, no);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == yes;
    }

    /**
     * Ejecuta una acción y convierte sus errores en un mensaje claro para el usuario.
     * Los errores inesperados se registran en consola y no se muestran en crudo.
     */
    public static void guard(Runnable action) {
        try {
            action.run();
        } catch (ParkingException | IllegalArgumentException e) {
            warn(e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
            warn("Ocurrió un error inesperado. Intente de nuevo.");
        }
    }

    public static <T extends DialogController> T showForm(String title, String fxml, Runnable onSaved) {
        return showForm(title, fxml, controller -> { }, onSaved);
    }

    /** Abre un formulario modal definido en FXML y espera a que se cierre. */
    public static <T extends DialogController> T showForm(String title, String fxml,
                                                          Consumer<T> init, Runnable onSaved) {
        FXMLLoader loader = Navigator.loader(fxml);
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar el formulario " + fxml, e);
        }
        T controller = loader.getController();
        Stage dialog = new Stage();
        dialog.initOwner(Navigator.stage());
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle(title);
        dialog.setResizable(false);
        Scene scene = new Scene(root);
        Navigator.applyStyles(scene);
        dialog.setScene(scene);
        controller.attach(dialog, onSaved);
        init.accept(controller);
        dialog.showAndWait();
        return controller;
    }

    private static Alert alert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.initOwner(Navigator.stage());
        alert.setTitle("Parqueo Hospital Provincial");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().setAll(Navigator.stylesheet().toExternalForm());
        return alert;
    }
}

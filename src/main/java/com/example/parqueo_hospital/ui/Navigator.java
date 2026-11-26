package com.example.parqueo_hospital.ui;

import com.example.parqueo_hospital.ui.controller.ShellController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/** Carga las vistas FXML y cambia entre el login y la ventana principal. */
public final class Navigator {
    private static final String BASE = "/com/example/parqueo_hospital/ui/";
    private static Stage stage;
    private static ShellController shell;

    private Navigator() { }

    public static void init(Stage primaryStage) {
        stage = primaryStage;
    }

    public static Stage stage() {
        return stage;
    }

    public static URL stylesheet() {
        return Navigator.class.getResource(BASE + "css/app.css");
    }

    public static void applyStyles(Scene scene) {
        scene.getStylesheets().setAll(stylesheet().toExternalForm());
    }

    public static FXMLLoader loader(String fxml) {
        URL url = Navigator.class.getResource(BASE + "view/" + fxml);
        if (url == null) throw new IllegalStateException("No se encontró la vista " + fxml);
        return new FXMLLoader(url);
    }

    public static void showLogin() {
        shell = null;
        Parent root = load("Login.fxml");
        replaceScene(new Scene(root, 560, 430), 520, 400);
    }

    /** Abre la ventana principal y muestra la pantalla de inicio del rol actual. */
    public static void showShell() {
        Parent root = load("Shell.fxml");
        replaceScene(new Scene(root, 1060, 680), 900, 560);
        show(Screen.home());
    }

    /** Carga una pantalla en el centro de la ventana principal y devuelve su controlador. */
    public static <T> T show(Screen screen) {
        FXMLLoader loader = loader(screen.fxml());
        Parent content = loadFrom(loader, screen.fxml());
        shell.setContent(content, screen);
        return loader.getController();
    }

    /** Cierra la sesión sin preguntar: las confirmaciones las hace quien llama. */
    public static void logout() {
        Session.clear();
        showLogin();
    }

    public static void registerShell(ShellController controller) {
        shell = controller;
    }

    private static Parent load(String fxml) {
        FXMLLoader loader = loader(fxml);
        return loadFrom(loader, fxml);
    }

    private static Parent loadFrom(FXMLLoader loader, String fxml) {
        try {
            return loader.load();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar la pantalla " + fxml, e);
        }
    }

    private static void replaceScene(Scene scene, double minWidth, double minHeight) {
        applyStyles(scene);
        stage.setScene(scene);
        stage.setMinWidth(minWidth);
        stage.setMinHeight(minHeight);
        stage.centerOnScreen();
    }
}

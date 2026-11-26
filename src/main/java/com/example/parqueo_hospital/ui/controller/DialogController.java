package com.example.parqueo_hospital.ui.controller;

import com.example.parqueo_hospital.ui.Dialogs;
import javafx.fxml.FXML;
import javafx.stage.Stage;

/** Base de los formularios modales: cerrar, cancelar y avisar del resultado. */
public abstract class DialogController {
    private Stage stage;
    private Runnable onSaved = () -> { };

    public void attach(Stage stage, Runnable onSaved) {
        this.stage = stage;
        this.onSaved = onSaved == null ? () -> { } : onSaved;
    }

    @FXML
    public void cancel() {
        stage.close();
    }

    /** Cierra el formulario, refresca la pantalla de origen y después muestra el mensaje de éxito. */
    protected void succeed(String message) {
        stage.close();
        onSaved.run();
        if (message != null) Dialogs.info(message);
    }
}

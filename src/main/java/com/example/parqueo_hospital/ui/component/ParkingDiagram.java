package com.example.parqueo_hospital.ui.component;

import com.example.parqueo_hospital.model.ParkingServiceRecord;
import com.example.parqueo_hospital.structures.LinkedVehicleList;
import com.example.parqueo_hospital.structures.VehicleNode;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * Dibuja la lista enlazada de vehículos presentes leyéndola directamente: no guarda copia.
 * La búsqueda por chapa resalta los nodos que coinciden sin quitar los demás, para que las
 * flechas siempre muestren los enlaces reales de la lista.
 */
public class ParkingDiagram extends Region {
    private static final double NODE_WIDTH = 116;
    private static final double NODE_HEIGHT = 48;
    private static final double COLUMN_STEP = 205;
    private static final double ROW_STEP = 115;

    private final Canvas canvas = new Canvas();
    private Supplier<LinkedVehicleList> source = LinkedVehicleList::new;
    private Runnable onRedraw = () -> { };
    private String filter = "";
    private boolean showOver24;

    public ParkingDiagram() {
        getChildren().add(canvas);
        getStyleClass().add("diagram-canvas");
        setPrefHeight(320);
        widthProperty().addListener((o, old, value) -> redraw());
        heightProperty().addListener((o, old, value) -> redraw());
    }

    public void setSource(Supplier<LinkedVehicleList> source) {
        this.source = source;
    }

    /** Se ejecuta después de cada redibujado (se usa para actualizar las barras de ocupación). */
    public void setOnRedraw(Runnable onRedraw) {
        this.onRedraw = onRedraw;
    }

    public void setFilter(String text) {
        this.filter = text == null ? "" : text.trim();
        redraw();
    }

    public boolean isShowOver24() {
        return showOver24;
    }

    public void setShowOver24(boolean showOver24) {
        this.showOver24 = showOver24;
        redraw();
    }

    @Override
    protected void layoutChildren() {
        canvas.setWidth(getWidth());
        canvas.setHeight(getHeight());
        redraw();
    }

    public void redraw() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();
        if (width <= 0 || height <= 0) return;

        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, width, height);

        LinkedVehicleList list = showOver24 ? source.get().over24Hours(LocalDateTime.now()) : source.get();
        String needle = filter.toLowerCase(Locale.ROOT);
        int perRow = Math.max(1, (int) Math.floor((width - 80) / COLUMN_STEP));
        double totalWidth = perRow * COLUMN_STEP - (COLUMN_STEP - NODE_WIDTH);
        double startX = Math.max(50, (width - totalWidth) / 2);

        int index = 0;
        double previousX = 0;
        double previousY = 0;
        int previousRow = -1;
        double lastX = 0;
        double lastY = 0;

        for (VehicleNode node = list.head(); node != null; node = node.next()) {
            ParkingServiceRecord service = node.service();
            boolean match = !needle.isEmpty()
                    && service.vehicle().plate().toLowerCase(Locale.ROOT).contains(needle);

            int row = index / perRow;
            int column = index % perRow;
            if (row % 2 == 1) column = perRow - 1 - column;
            double x = startX + column * COLUMN_STEP;
            double y = 55 + row * ROW_STEP;

            g.setStroke(Color.BLACK);
            g.setLineWidth(1.4);

            if (index > 0) {
                if (row == previousRow) {
                    boolean forward = x > previousX;
                    double fromX = forward ? previousX + NODE_WIDTH : previousX;
                    double toX = forward ? x : x + NODE_WIDTH;
                    g.strokeLine(fromX, previousY + 24, toX, y + 24);
                    g.strokeLine(toX + (forward ? -8 : 8), y + 18, toX, y + 24);
                    g.strokeLine(toX + (forward ? -8 : 8), y + 30, toX, y + 24);
                } else {
                    g.strokeLine(previousX + 58, previousY + NODE_HEIGHT, x + 58, y);
                    g.strokeLine(x + 52, y + 8, x + 58, y);
                    g.strokeLine(x + 64, y + 8, x + 58, y);
                }
            }

            g.setFill(match ? Color.web("#ffe98a") : Color.WHITE);
            g.fillRect(x, y, NODE_WIDTH, NODE_HEIGHT);
            if (match) g.setLineWidth(2.6);
            g.strokeRect(x, y, NODE_WIDTH, NODE_HEIGHT);
            g.setLineWidth(1.4);
            g.strokeLine(x + 85, y, x + 85, y + NODE_HEIGHT);

            g.setFill(Color.BLACK);
            g.fillOval(x + 97, y + 17, 14, 14);
            g.setTextAlign(TextAlignment.CENTER);
            g.fillText(service.vehicle().plate(), x + 42, y + 29);
            g.setTextAlign(TextAlignment.LEFT);

            if (index == 0) g.fillText("Head", x + 35, y + 64);

            previousX = x;
            previousY = y;
            previousRow = row;
            lastX = x;
            lastY = y;
            index++;
        }

        if (index == 0) {
            g.setFill(Color.GRAY);
            g.setTextAlign(TextAlignment.CENTER);
            g.fillText(showOver24 ? "No hay vehículos con más de 24 horas" : "No hay vehículos presentes",
                    width / 2, Math.max(60, height / 2));
            g.setTextAlign(TextAlignment.LEFT);
        } else {
            g.setFill(Color.BLACK);
            g.fillText("Tail", lastX + 35, lastY + 64);
            double tailX = lastX + NODE_WIDTH;
            g.strokeLine(tailX, lastY + 24, tailX + 28, lastY + 24);
            g.strokeLine(tailX + 28, lastY + 14, tailX + 28, lastY + 34);
            g.strokeLine(tailX + 34, lastY + 18, tailX + 34, lastY + 30);
        }

        double needed = Math.max(320, ((index + perRow - 1) / perRow) * ROW_STEP + 110);
        if (Math.abs(getMinHeight() - needed) > 0.5) setMinHeight(needed);
        onRedraw.run();
    }
}

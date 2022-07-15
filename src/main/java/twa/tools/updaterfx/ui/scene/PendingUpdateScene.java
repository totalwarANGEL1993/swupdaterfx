package twa.tools.updaterfx.ui.scene;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import twa.tools.updaterfx.controller.app.InterfaceController;

public class PendingUpdateScene extends Pane {
    private static PendingUpdateScene self;
    private final Button startUpdate;
    private final Button ignoreUpdate;

    public static PendingUpdateScene getInstance() {
        return New(null, null);
    }

    public static PendingUpdateScene New(Double w, Double h) {
        if (null == self && null != w && null != h) {
            self = new PendingUpdateScene(w, h);
        }
        return self;
    }

    private PendingUpdateScene(Double w, Double h) {
        setWidth(w);
        setHeight(h);

        startUpdate = new Button("Aktualisieren");
        startUpdate.setId("PendingUpdateScene_Update");
        startUpdate.setFont(Font.font(18));
        startUpdate.setPrefSize(250, 40);
        startUpdate.setLayoutX((w/2) - (startUpdate.getPrefWidth()/2));
        startUpdate.setLayoutY((h/2) - (startUpdate.getPrefHeight()+20));
        startUpdate.setOnMouseClicked(mouseEvent -> InterfaceController.getInstance().onButtonClicked(startUpdate));
        getChildren().add(startUpdate);

        Label startLabel = new Label("Für die Anwendung steht ein Update zum Download bereit.");
        startLabel.setPrefSize(325, 30);
        startLabel.setLayoutX((w/2) - (startLabel.getPrefWidth()/2));
        startLabel.setLayoutY((h/2) - (startUpdate.getPrefHeight()+65));
        getChildren().add(startLabel);

        ignoreUpdate = new Button("Ignorieren");
        ignoreUpdate.setId("PendingUpdateScene_Ignore");
        ignoreUpdate.setPrefSize(150, 20);
        ignoreUpdate.setLayoutX((w/2) - (ignoreUpdate.getPrefWidth()/2));
        ignoreUpdate.setLayoutY(h - 200);
        ignoreUpdate.setOnMouseClicked(mouseEvent -> InterfaceController.getInstance().onButtonClicked(ignoreUpdate));
        getChildren().add(ignoreUpdate);
    }
}

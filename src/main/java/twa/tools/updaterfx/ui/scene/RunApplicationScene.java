package twa.tools.updaterfx.ui.scene;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import lombok.Getter;
import twa.tools.updaterfx.controller.app.InterfaceController;

public class RunApplicationScene extends Pane {
    private static RunApplicationScene self;
    private final Button startApplication;
    private final Button displayChangelog;

    public static RunApplicationScene getInstance() {
        return New(null, null);
    }

    public static RunApplicationScene New(Double w, Double h) {
        if (null == self && null != w && null != h) {
            self = new RunApplicationScene(w, h);
        }
        return self;
    }

    private RunApplicationScene(Double w, Double h) {
        setWidth(w);
        setHeight(h);

        startApplication = new Button("Ausführen");
        startApplication.setId("RunApplicationScene_RunApp");
        startApplication.setFont(Font.font(18));
        startApplication.setPrefSize(250, 40);
        startApplication.setLayoutX((w/2) - (startApplication.getPrefWidth()/2));
        startApplication.setLayoutY((h/2) - (startApplication.getPrefHeight()+20));
        startApplication.setOnMouseClicked(mouseEvent -> InterfaceController.getInstance().onButtonClicked(startApplication));
        getChildren().add(startApplication);

        Label startLabel = new Label("Die Anwendung ist aktuell und kann gestartet werden.");
        startLabel.setPrefSize(300, 30);
        startLabel.setLayoutX((w/2) - (startLabel.getPrefWidth()/2));
        startLabel.setLayoutY((h/2) - (startApplication.getPrefHeight()+65));
        getChildren().add(startLabel);

        displayChangelog = new Button("Changelog");
        displayChangelog.setId("RunApplicationScene_Changelog");
        displayChangelog.setPrefSize(150, 20);
        displayChangelog.setLayoutX((w/2) - (displayChangelog.getPrefWidth()/2));
        displayChangelog.setLayoutY(h - 200);
        displayChangelog.setOnMouseClicked(mouseEvent -> InterfaceController.getInstance().onButtonClicked(displayChangelog));
        getChildren().add(displayChangelog);
    }
}

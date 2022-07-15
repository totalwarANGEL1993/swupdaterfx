package twa.tools.updaterfx.ui.scene;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Pane;

public class OngoingProcessScene extends Pane {
    private static OngoingProcessScene self;

    public static OngoingProcessScene getInstance() {
        return New(null, null);
    }

    public static OngoingProcessScene New(Double w, Double h) {
        if (null == self && null != w && null != h) {
            self = new OngoingProcessScene(w, h);
        }
        return self;
    }

    private OngoingProcessScene(Double w, Double h) {
        setWidth(w);
        setHeight(h);

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(100, 100);
        progressIndicator.setLayoutX((w / 2) - (progressIndicator.getPrefWidth()/2));
        progressIndicator.setLayoutY((h / 2) - progressIndicator.getPrefHeight());
        getChildren().add(progressIndicator);

        Label label = new Label("Bitte warten...");
        label.setPrefSize(100, 30);
        label.setLayoutX((w / 2) - (label.getPrefWidth() / 2) + 15);
        label.setLayoutY(progressIndicator.getLayoutY() +100);
        getChildren().add(label);
    }
}

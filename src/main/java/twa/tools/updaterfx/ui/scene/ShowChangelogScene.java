package twa.tools.updaterfx.ui.scene;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import lombok.Getter;
import twa.tools.updaterfx.controller.app.InterfaceController;

public class ShowChangelogScene extends Pane {
    private static ShowChangelogScene self;
    private final TextArea textArea;
    private final ScrollPane viewport;
    private final TextFlow textFlowUpdate;
    private final TextFlow textFlowLog;
    @Getter
    private final Button backButton;
    @Getter
    private final Button continueButton;

    public static ShowChangelogScene getInstance() {
        return New(null, null);
    }

    public static ShowChangelogScene New(Double w, Double h) {
        if (null == self && null != w && null != h) {
            self = new ShowChangelogScene(w, h);
        }
        return self;
    }

    private ShowChangelogScene(Double w, Double h) {
        setWidth(w);
        setHeight(h);

        textFlowUpdate = new TextFlow();
        Text text1 = new Text("Ein Update steht zur Installation bereit. Folgend siehst du die letzten Änderungen.");
        text1.setStyle("-fx-font-weight: regular");
        Text text2 = new Text("\nHinweis: ");
        text2.setStyle("-fx-font-weight: bold");
        Text text3 = new Text("Du kannst das Update auch überspringen.");
        text3.setStyle("-fx-font-weight: regular");
        textFlowUpdate.getChildren().addAll(text1, text2, text3);
        textFlowUpdate.setPrefSize(w -30, 50);
        textFlowUpdate.setLayoutX(5);
        textFlowUpdate.setLayoutY(5);
        getChildren().add(textFlowUpdate);

        textFlowLog = new TextFlow();
        Text text4 = new Text("Dies ist die Versionshistorie des lokalen Stand.");
        text1.setStyle("-fx-font-weight: regular");
        textFlowLog.getChildren().addAll(text4);
        textFlowLog.setPrefSize(w -30, 50);
        textFlowLog.setLayoutX(5);
        textFlowLog.setLayoutY(5);
        getChildren().add(textFlowLog);

        viewport = new ScrollPane();
        viewport.setPrefSize(w -30, h -140);
        viewport.setLayoutX(5);
        viewport.setLayoutY(50);
        getChildren().add(viewport);

        textArea = new TextArea();
        textArea.setPrefSize(viewport.getPrefWidth() -2, viewport.getPrefHeight() -2);
        textArea.setEditable(false);
        textArea.setFont(Font.font("Monospaced"));
        viewport.setContent(textArea);

        backButton = new Button("Zurück");
        backButton.setId("ShowChangelogScene_Back");
        backButton.setLayoutX(5);
        backButton.setLayoutY(h -80);
        backButton.setOnMouseClicked(mouseEvent -> InterfaceController.getInstance().onButtonClicked(backButton));
        getChildren().add(backButton);

        continueButton = new Button("Aktualisieren");
        continueButton.setId("ShowChangelogScene_Continue");
        continueButton.setLayoutX(w -110);
        continueButton.setLayoutY(h -80);
        continueButton.setOnMouseClicked(mouseEvent -> InterfaceController.getInstance().onButtonClicked(continueButton));
        getChildren().add(continueButton);
    }

    public void setReadonlyMode(boolean readonly) {
        continueButton.setVisible(!readonly);
        textFlowLog.setVisible(readonly);
        textFlowUpdate.setVisible(!readonly);
    }

    public void setText(String text) {
        textArea.setText(text);
        viewport.setVvalue(0);
    }
}

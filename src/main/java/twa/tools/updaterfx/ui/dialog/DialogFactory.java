package twa.tools.updaterfx.ui.dialog;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.StageStyle;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.Optional;

public class DialogFactory {
    public static void infoDialog(String title, String text) {
        infoDialog(title, text, null);
    }

    public static void infoDialog(String title, String text, IAction action) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(text);
        alert.initStyle(StageStyle.UTILITY);
        alert.setContentText(null);

        final Optional<ButtonType> result = alert.showAndWait();
        // Any result triggers action
        if (null != action) {
            action.perform();
        }
    }

    public static void requestDialog(String title, String text, IAction onConfirm) {
        requestDialog(title, text, onConfirm, null);
    }

    public static void requestDialog(String title, String text, IAction onConfirm, IAction onCancel) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(text);
        alert.initStyle(StageStyle.UTILITY);
        alert.setContentText(null);

        final Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            if (null != onConfirm) onConfirm.perform();
        }
        else {
            if (null != onCancel) onCancel.perform();
        }
    }

    public static void errorDialog(String title, String text) {
        errorDialog(title, text, null);
    }

    public static void errorDialog(String title, String text, IAction action) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(text);
        alert.initStyle(StageStyle.UTILITY);
        alert.setContentText(null);

        final Optional<ButtonType> result = alert.showAndWait();
        // Any result triggers action
        if (null != action) {
            action.perform();
        }
    }

    public static void exceptionDialog(Exception e) {
        exceptionDialog(e, false);
    }

    public static void exceptionDialog(Exception e, boolean exit) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Fehler");
        alert.setHeaderText("Beim Ausführen einer Aktion ist ein Fehler aufgetreten.");
        alert.initStyle(StageStyle.UTILITY);
        alert.setContentText(e.getMessage());

        String fullText = ExceptionUtils.getFullStackTrace(e);

        Label label = new Label("Details zum Fehler:");

        TextArea textArea = new TextArea(fullText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);
        final Optional<ButtonType> result = alert.showAndWait();
        if (exit) System.exit(1);
    }

    /**
     *
     */
    public static interface IAction {
        void perform();
    }
}

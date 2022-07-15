package twa.tools.updaterfx.controller.app;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import twa.tools.updaterfx.ui.dialog.DialogFactory;
import twa.tools.updaterfx.ui.scene.EmptyScene;
import twa.tools.updaterfx.ui.scene.OngoingProcessScene;
import twa.tools.updaterfx.ui.scene.PendingUpdateScene;
import twa.tools.updaterfx.ui.scene.RunApplicationScene;
import twa.tools.updaterfx.ui.scene.ShowChangelogScene;

import java.util.Stack;

/**
 * Controller for setup and maintaining the stage.
 */
@Component
public class InterfaceController implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(InterfaceController.class);
    private static InterfaceController self;

    @Value("${app.version}")
    private String appVersion;
    @Value("${app.name}")
    private String appName;

    @Value("${target.app.name}")
    private String targetAppName;

    @Getter
    @Setter
    private Stage stage;

    @Getter
    @Setter
    private Stack<Scene> sceneHistory = new Stack<>();

    private Scene ongoingProcessScene;
    private Scene displayChangelogScene;
    private Scene startApplicationScene;
    private Scene pendingUpdateScene;
    private Scene emptyScene;

    @Override
    public void afterPropertiesSet() throws Exception {
        self = this;
    }

    public static InterfaceController getInstance() {
        return self;
    }

    /**
     * Prepares the stage and displays it
     * @param stage Primary stage
     */
    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle(appName);
        stage.getIcons().add(new Image("config/logo_100.png"));
        stage.setWidth(600);
        stage.setHeight(450);
        stage.setResizable(false);
        stage.show();

        OngoingProcessScene.New(stage.getWidth(), stage.getHeight());
        ShowChangelogScene.New(stage.getWidth(), stage.getHeight());
        RunApplicationScene.New(stage.getWidth(), stage.getHeight());
        PendingUpdateScene.New(stage.getWidth(), stage.getHeight());
        EmptyScene.New(stage.getWidth(), stage.getHeight());
        showOngoingProcessScene();

        LogicController.getInstance().start();
    }

    /**
     * Shows an empty scene used when something crashes.
     */
    public void showEmptyScene() {
        EmptyScene content = EmptyScene.getInstance();
        if (null == emptyScene)
            emptyScene = new Scene(content);
        stage.setScene(emptyScene);
    }

    /**
     * Shows a scene with a busy indicator.
     */
    public void showOngoingProcessScene() {
        OngoingProcessScene content = OngoingProcessScene.getInstance();
        if (null == ongoingProcessScene)
            ongoingProcessScene = new Scene(content);
        stage.setScene(ongoingProcessScene);
        // After each process the history becomes invalid
        sceneHistory.clear();
    }

    /**
     * Shows the scene for downloading or ignoring updates.
     */
    public void showPendingUpdateScene() {
        PendingUpdateScene content = PendingUpdateScene.getInstance();
        if (null == pendingUpdateScene)
            pendingUpdateScene = new Scene(content);
        stage.setScene(pendingUpdateScene);
        sceneHistory.push(pendingUpdateScene);
    }

    /**
     * Shows the scene that is displaying the changelog.
     * @param text     Text to display
     * @param readonly Hide update button
     */
    public void showDisplayChangelogScene(String text, boolean readonly) {
        ShowChangelogScene content = ShowChangelogScene.getInstance();
        content.setReadonlyMode(readonly);
        content.setText(text);
        if (null == displayChangelogScene)
            displayChangelogScene = new Scene(content);
        stage.setScene(displayChangelogScene);
        sceneHistory.push(displayChangelogScene);
    }

    /**
     * Shows the scene for starting the application.
     */
    public void showStartApplicationScene() {
        RunApplicationScene content = RunApplicationScene.getInstance();
        if (null == startApplicationScene)
            startApplicationScene = new Scene(content);
        stage.setScene(startApplicationScene);
        sceneHistory.push(startApplicationScene);
    }

    /**
     * Event handler for the buttons.
     * @param button Button reference
     */
    public void onButtonClicked(Button button) {
        final String id = button.getId();

        // Run App Scene
        if ("RunApplicationScene_RunApp".equals(id)) {
            showOngoingProcessScene();
            LogicController.getInstance().startApplication();
        }
        if ("RunApplicationScene_Changelog".equals(id)) {
            showDisplayChangelogScene(LogicController.getInstance().getChangelog(), true);
        }

        // Update Scene
        if ("PendingUpdateScene_Update".equals(id)) {
            showDisplayChangelogScene(LogicController.getInstance().getChangelog(), false);
        }
        if ("PendingUpdateScene_Ignore".equals(id)) {
            stage.hide();
            DialogFactory.requestDialog(
                "Updates ignorieren",
                "Sollen die ausstehenden Aktualisierungen wirklich ignoriert und die Anwendung gestartet werden?",
                () -> LogicController.getInstance().startApplication(),
                this::showPendingUpdateScene
            );
            stage.show();
        }

        // Changelog Scene
        if ("ShowChangelogScene_Continue".equals(id)) {
            showOngoingProcessScene();
            LogicController.getInstance().startUpdate();
        }
        if ("ShowChangelogScene_Back".equals(id)) {
            returnToPreviousScene();
        }
    }

    private void returnToPreviousScene() {
        sceneHistory.pop();
        Scene last = sceneHistory.peek();
        if (null != last) {
            stage.setScene(last);
        }
    }
}

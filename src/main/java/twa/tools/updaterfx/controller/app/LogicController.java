package twa.tools.updaterfx.controller.app;

import javafx.application.Platform;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import twa.tools.updaterfx.controller.git.GitController;
import twa.tools.updaterfx.controller.git.GitException;
import twa.tools.updaterfx.ui.dialog.DialogFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controller for executing the processes.
 */
@Component
public class LogicController implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(LogicController.class);
    private static LogicController self;

    private String changelog;

    @Value("${target.repository}")
    private String targetRepositoryUrl;
    @Value("${target.branch}")
    private String targetBranch;
    @Value("${target.directory}")
    private String targetDirectory;
    @Value("${target.app.jar}")
    private String targetJarName;
    @Value("${target.app.run}")
    private String targetJarMode;
    @Value("${target.app.dir}")
    private String targetDirName;

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private GitController gitController;

    @Setter
    @Getter
    private Exception lastException;

    public LogicController() {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        self = this;
    }

    public static LogicController getInstance() {
        return self;
    }

    public String getChangelog() {
        return changelog;
    }

    public void start() {
        Thread taskThread = new Thread(() -> {
            checkoutRepository();
            fetchChanges();
        });
        taskThread.start();
    }

    /**
     * Calls the git agent to fetch updates from the repository.
     */
    public void fetchChanges() {
            try {
                changelog = getLocalChangelog();
                boolean hasChanges = !gitController.isCurrentVersion();
                if (hasChanges) {
                    changelog = getRemoteChangelog();
                }
                Platform.runLater(() -> {
                    if (hasChanges) {
                        InterfaceController.getInstance().showPendingUpdateScene();
                    }
                    else {
                        InterfaceController.getInstance().showStartApplicationScene();
                    }
                });
            }
            catch (Exception e) {
                LOG.error("Fehler beim Suchen nach Updates.", e);
                Platform.runLater(() -> {
                    InterfaceController.getInstance().showEmptyScene();
                    DialogFactory.exceptionDialog(e);
                });
            }
    }

    /**
     * Executes the application update.
     */
    public void startUpdate() {
        Thread taskThread = new Thread(() -> {
            try {
                boolean hasChanges = !gitController.isCurrentVersion();
                if (hasChanges) {
                    gitController.rebaseRepository();
                }
                Platform.runLater(() -> {
                    InterfaceController.getInstance().showStartApplicationScene();
                });
            }
            catch (Exception e) {
                LOG.error("Fehler beim Ausführen des Updates.", e);
                Platform.runLater(() -> {
                    InterfaceController.getInstance().showEmptyScene();
                    DialogFactory.exceptionDialog(e);
                });
            }
        });
        taskThread.start();
    }

    /**
     * Starts the application maintained by the updater.
     */
    public void startApplication() {
        Thread taskThread = new Thread(() -> {
            try {
                runTargetApplication();
                ((ConfigurableApplicationContext) applicationContext).close();
                System.exit(0);
            }
            catch (Exception e) {
                LOG.error("Fehler beim Start der Anwendung.", e);
                Platform.runLater(() -> {
                    InterfaceController.getInstance().showEmptyScene();
                    DialogFactory.exceptionDialog(e);
                });
            }
        });
        taskThread.start();
    }

    /**
     * Performs the checkout of the repository.
     *
     * When it is not found it will try to clone it first. The local copy is always reset to head.
     */
    public void checkoutRepository() {
        try {
            // Checkout repository if not existing
            final Path gitDir = Paths.get(System.getProperty("user.dir"), targetDirectory, ".git");
            if (!gitDir.toFile().exists()) {
                restoreRepository();
            }
            // Reset local repository
            final Status status = getRepositoryStatus();
            if (status.hasUncommittedChanges()) {
                gitController.resetRepository();
            }
        }
        catch (Exception e) {
            LOG.error("Fehler beim Laden der Daten.", e);
            Platform.runLater(() -> {
                InterfaceController.getInstance().showEmptyScene();
                DialogFactory.exceptionDialog(e);
            });
        }
    }

    private void restoreRepository() throws GitException {
        LOG.info("Delete and reclone repository...");
        gitController.purgeRepository();
        gitController.cloneRepository();
    }

    private Status getRepositoryStatus() throws GitException {
        try {
            LOG.info("Obtain repository status...");
            return gitController.getRepository().status().call();
        }
        catch (IOException | GitAPIException e) {
            throw new GitException("Unable to read status!", e);
        }
    }

    private String getRemoteChangelog() throws GitException {
        LOG.info("Reading changelog...");
        final ByteArrayOutputStream data = (ByteArrayOutputStream) gitController.getFile("changelog.txt", false);
        String content = new String(data.toByteArray(), StandardCharsets.UTF_8);
        if (StringUtils.isBlank(content)) {
            LOG.warn("Changelog not found!");
        }
        else {
            LOG.info("Changelog loaded!");
        }
        return content;
    }

    private String getLocalChangelog() throws GitException {
        LOG.info("Reading changelog...");
        final ByteArrayOutputStream data = (ByteArrayOutputStream) gitController.getFile("changelog.txt", true);
        String content = new String(data.toByteArray(), StandardCharsets.UTF_8);
        if (StringUtils.isBlank(content)) {
            LOG.warn("Changelog not found!");
        }
        else {
            LOG.info("Changelog loaded!");
        }
        return content;
    }

    private void runTargetApplication() throws IOException {
        String pwd = System.getProperty("user.dir");
        if (targetJarMode.equals("normal")) {
            String execution = "" +
                "jre/bin/java -Dspring.profiles.active=pro -Dfile.encoding=UTF8 -jar " +
                targetDirectory +
                File.separator +
                targetDirName +
                File.separator +
                targetJarName;
            execution = execution.replaceAll("\\\\", "/");
            LOG.info("Executing runtime: " +execution);
            Runtime.getRuntime().exec(execution);
        }
        else {
            String path = "" +
                pwd +
                File.separator +
                targetDirectory +
                File.separator +
                targetDirName;
            String execution = "" +
                "cmd /c /b start & cd " +
                "\"" + path + "\"" +
                " & " +
                "\"" + pwd + "/jre/bin/java" + "\" " +
                "-Dspring.profiles.active=pro -Dfile.encoding=UTF8 -jar " +
                targetJarName;
            LOG.info("Executing runtime: " +execution);
            Runtime.getRuntime().exec(execution);
        }
    }
}

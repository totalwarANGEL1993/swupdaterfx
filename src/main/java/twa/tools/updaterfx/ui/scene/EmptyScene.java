package twa.tools.updaterfx.ui.scene;

import javafx.scene.layout.Pane;

public class EmptyScene extends Pane {
    private static EmptyScene self;

    public static EmptyScene getInstance() {
        return New(null, null);
    }

    public static EmptyScene New(Double w, Double h) {
        if (null == self && null != w && null != h) {
            self = new EmptyScene(w, h);
        }
        return self;
    }

    private EmptyScene(Double w, Double h) {
        setWidth(w);
        setHeight(h);
    }
}

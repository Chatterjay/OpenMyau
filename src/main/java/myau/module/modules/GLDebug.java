package myau.module.modules;

import myau.module.Module;
import myau.property.properties.BooleanProperty;

public class GLDebug extends Module {
    public final BooleanProperty enabled = new BooleanProperty("enabled", false);

    public GLDebug() {
        super("GLDebug", false, true);
    }

    @Override
    public void onEnabled() {
        setEnabled(false);
    }
}

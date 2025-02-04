module de.eisfeldj.augendiagnosefx {
    requires de.eisfeldj.xmpcore;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.xml;
    requires java.logging;
    requires java.prefs;
	requires commons.lang3;

    opens de.eisfeldj.augendiagnosefx.controller;

    exports de.eisfeldj.augendiagnosefx;
}
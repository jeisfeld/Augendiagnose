module AugendiagnoseFX {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.xml;
    requires java.logging;
    requires xmpcore;
    requires java.prefs;

    opens de.eisfeldj.augendiagnosefx.controller;

    exports de.eisfeldj.augendiagnosefx;
}
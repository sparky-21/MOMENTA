package com.momenta.application;

/**
 * Plain (non-Application) entry point, used only so the app can be run
 * directly from IntelliJ's green Run button instead of {@code mvn
 * javafx:run} every time.
 *
 * Why this class has to exist separately from {@link Main}:
 * when the JVM launches a class that itself {@code extends
 * javafx.application.Application}, it first checks whether the JavaFX
 * runtime is on the *module path*. The javafx-maven-plugin sets that up for
 * {@code mvn javafx:run}, but IntelliJ's own "Run" button just puts the
 * Maven dependencies on the plain classpath — so launching {@code Main}
 * directly fails with "JavaFX runtime components are missing", even though
 * javafx-controls/javafx-fxml are sitting right there in the classpath.
 *
 * That check only triggers when the class passed to {@code java} is itself
 * an Application subclass. Launcher is not one — it is an ordinary class
 * with an ordinary main() — so the JVM starts it with no JavaFX-specific
 * check at all, and by the time it calls into Main.main(), JavaFX classes
 * are already loadable from the classpath like any other library.
 */
public final class Launcher {

    private Launcher() {
    }

    public static void main(String[] args) {
        Main.main(args);
    }
}

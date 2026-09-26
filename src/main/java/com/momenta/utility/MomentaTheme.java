package com.momenta.utility;

import com.momenta.service.SettingsService;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.Chart;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;

/**
 * MOMENTA visual system.
 *
 * A dark, futuristic, purple-first visual language shared by every screen.
 * This class intentionally does not contain application/data logic.
 */
public final class MomentaTheme {

    // ---------- MOMENTA PALETTE ----------
    public static final Color VOID = Color.web("#07040D");
    public static final Color NIGHT = Color.web("#0B0714");
    public static final Color SURFACE = Color.web("#120B1F");
    public static final Color SURFACE_2 = Color.web("#1A102B");
    public static final Color SURFACE_3 = Color.web("#24153A");

    public static final Color ROYAL_PURPLE = Color.web("#8B5CF6");
    public static final Color ELECTRIC_VIOLET = Color.web("#A855F7");
    public static final Color HOT_ORCHID = Color.web("#D946EF");
    public static final Color NEON_LILAC = Color.web("#C4B5FD");
    public static final Color DEEP_PLUM = Color.web("#6D28D9");
    public static final Color COSMIC_BLUE = Color.web("#6366F1");
    public static final Color PINK = Color.web("#F472B6");
    public static final Color SOFT_GREEN = Color.web("#6EE7B7");
    public static final Color PEACH = Color.web("#FDBA74");
    public static final Color SKY_BLUE = Color.web("#67E8F9");
    public static final Color PERIWINKLE = Color.web("#A5B4FC");

    public static final Color TEXT = Color.web("#FAF7FF");
    public static final Color TEXT_2 = Color.web("#DDD3ED");
    public static final Color MUTED = Color.web("#9588A8");
    public static final Color BORDER = Color.web("#35214F");
    public static final Color BORDER_SOFT = Color.web("#251638");

    // Kept for compatibility with existing project code.
    public static final Color SEA_GREEN = Color.web("#2DD4BF");
    public static final Color MINT = Color.web("#A7F3D0");
    public static final Color SKY_BACKGROUND = Color.web("#F4FAFC");
    public static final Color LAVENDER = Color.web("#B8A7FF");
    public static final Color CHARCOAL = Color.web("#263238");
    public static final Color WHITE = Color.WHITE;

    private static final String FONT = "Segoe UI";
    private static final String FONT_BOLD = "Segoe UI Semibold";

    private MomentaTheme() {}

    public static void apply(Parent root, String viewName) {
        boolean dark = true; // MOMENTA's redesigned identity is intentionally dark-first.
        Color accent = accentFor(viewName);

        if (root instanceof Region region) {
            region.setBackground(background(NIGHT, 0));
            region.setPadding(Insets.EMPTY);
        }

        styleTree(root, accent, dark);

        if ("Dashboard".equals(viewName)) {
            styleDashboard(root);
        } else if ("Home".equals(viewName)) {
            styleHome(root);
        } else if ("Login".equals(viewName) || "Register".equals(viewName)) {
            styleAuth(root, viewName);
        } else {
            styleWorkspace(root, accent, viewName);
        }
    }

    public static Color accentColor() {
        return accentFromName(SettingsService.getAccent());
    }

    public static Color accentLightColor() {
        return accentColor().deriveColor(0, 0.35, 1.18, 1);
    }

    public static Color textColor() {
        return TEXT;
    }

    public static Color mutedColor() {
        return MUTED;
    }

    public static boolean isDark() {
        return true;
    }

    public static Color accentFor(String viewName) {
        Color personalized = accentColor();

        // Respect the user's selected accent, while keeping the default
        // Royal Purple accent mapped to distinct page accents.
        if (!"Royal Purple".equals(SettingsService.getAccent())) {
            return personalized;
        }

        return switch (viewName) {
            case "Tasks", "Calendar" -> ELECTRIC_VIOLET;
            case "Goals" -> ROYAL_PURPLE;
            case "Projects" -> NEON_LILAC;
            case "Habits" -> SOFT_GREEN;
            case "Finance" -> PEACH;
            case "Focus" -> PINK;
            case "Analytics" -> PERIWINKLE;
            case "Home", "Login", "Register", "Settings" -> ROYAL_PURPLE;
            default -> ROYAL_PURPLE;
        };
    }

    private static void styleTree(Node node, Color accent, boolean dark) {
        if (node instanceof Label label) {
            label.setTextFill(TEXT);
            label.setFont(fontFor(label));
        }

        if (node instanceof Button button) {
            styleButton(button, accent);
        } else if (node instanceof TextInputControl input) {
            styleInput(input, accent);
        } else if (node instanceof ComboBox<?> combo) {
            styleCombo(combo, accent);
        } else if (node instanceof DatePicker datePicker) {
            styleCombo(datePicker, accent);
        } else if (node instanceof Slider slider) {
            slider.setStyle(
                    "-fx-control-inner-background: " + hex(SURFACE_3) + ";" +
                            "-fx-accent: " + hex(accent) + ";"
            );
        } else if (node instanceof CheckBox checkBox) {
            checkBox.setTextFill(TEXT_2);
            checkBox.setFont(Font.font(FONT, 13));
        }

        if (node instanceof ProgressBar progressBar) {
            progressBar.setPrefHeight(10);
            progressBar.setStyle(
                    "-fx-accent: " + hex(accent) + ";" +
                            "-fx-control-inner-background: " + hex(SURFACE_3) + ";"
            );
        }

        if (node instanceof TableView<?> table) {
            styleTable(table, accent);
        } else if (node instanceof ListView<?> list) {
            styleList(list, accent);
        } else if (node instanceof ScrollPane scroll) {
            scroll.setFitToWidth(true);
            scroll.setPannable(true);
            scroll.setBackground(background(NIGHT, 0));
            scroll.setBorder(Border.EMPTY);
        } else if (node instanceof Chart chart) {
            chart.setBackground(background(SURFACE, 18));
            chart.setBorder(border(accent, 1));
            chart.setPadding(new Insets(10));
        }

        if (node instanceof VBox box) {
            box.setFillWidth(true);
        }

        if (node instanceof HBox box) {
            box.setFillHeight(true);
        }

        if (node instanceof Separator separator) {
            separator.setStyle("-fx-background-color: " + hex(BORDER_SOFT) + ";");
        }

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                styleTree(child, accent, dark);
            }
        }
    }

    // ---------- DASHBOARD ----------

    private static void styleDashboard(Node root) {
        Node sidebarNode = findById(root, "sidebar");

        if (sidebarNode instanceof VBox sidebar) {
            sidebar.setBackground(new Background(new BackgroundFill(
                    sidebarGradient(), CornerRadii.EMPTY, Insets.EMPTY)));

            sidebar.setBorder(new Border(new BorderStroke(
                    BORDER,
                    BorderStrokeStyle.SOLID,
                    CornerRadii.EMPTY,
                    new BorderWidths(0, 1, 0, 0)
            )));

            sidebar.setPadding(new Insets(26, 16, 26, 16));
            sidebar.setSpacing(7);

            for (Node child : sidebar.getChildren()) {
                if (child instanceof Button button) {
                    styleNavButton(button);
                }
            }
        }

        styleCard(findById(root, "pulseCard"), ELECTRIC_VIOLET);
        styleCard(findById(root, "taskCard"), COSMIC_BLUE);
        styleCard(findById(root, "goalCard"), SOFT_GREEN);
        styleCard(findById(root, "projectCard"), HOT_ORCHID);
        styleCard(findById(root, "nowCard"), ROYAL_PURPLE);
        styleCard(findById(root, "insightCard"), PERIWINKLE);
        styleCard(findById(root, "gamificationCard"), HOT_ORCHID);

        Node greeting = findById(root, "greetingLabel");
        if (greeting instanceof Label label) {
            label.setFont(Font.font(FONT_BOLD, 28));
            label.setTextFill(TEXT);
        }

        Node count = findById(root, "notificationCountLabel");
        if (count instanceof Label label) {
            label.setTextFill(NEON_LILAC);
            label.setFont(Font.font(FONT_BOLD, 12));
        }

        // Strong metric labels.
        for (String id : new String[]{
                "pulseLabel", "taskCountLabel", "goalCountLabel",
                "projectCountLabel", "levelLabel", "xpLabel"
        }) {
            Node n = findById(root, id);
            if (n instanceof Label label) {
                label.setTextFill(NEON_LILAC);
                label.setFont(Font.font(FONT_BOLD, 25));
            }
        }
    }

    private static void styleNavButton(Button button) {
        button.setFont(Font.font(FONT_BOLD, 12.5));
        button.setTextFill(TEXT_2);
        button.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPadding(new Insets(11, 14, 11, 14));
        button.setBackground(background(Color.TRANSPARENT, 12));
        button.setBorder(Border.EMPTY);

        button.setOnMouseEntered(e -> {
            button.setTextFill(TEXT);
            button.setTranslateX(3);
            button.setBackground(background(Color.web("#24143A"), 12));
        });

        button.setOnMouseExited(e -> {
            button.setTextFill(TEXT_2);
            button.setTranslateX(0);
            button.setBackground(background(Color.TRANSPARENT, 12));
        });
    }

    // ---------- HOME ----------

    private static void styleHome(Node root) {
        if (root instanceof Region region) {
            region.setBackground(new Background(new BackgroundFill(
                    heroGradient(), CornerRadii.EMPTY, Insets.EMPTY)));
        }

        Node nav = findById(root, "homeNav");
        if (nav instanceof Region region) {
            region.setBackground(background(Color.web("#090510EE"), 0));
            region.setBorder(new Border(new BorderStroke(
                    BORDER,
                    BorderStrokeStyle.SOLID,
                    CornerRadii.EMPTY,
                    new BorderWidths(0, 0, 1, 0)
            )));
        }

        Node content = findById(root, "heroContent");
        if (content instanceof Region region) {
            region.setMaxWidth(1100);
        }

        styleHomeButton(findById(root, "navLogin"), false);
        styleHomeButton(findById(root, "navStart"), true);
        styleHomeButton(findById(root, "heroStart"), true);
        styleHomeButton(findById(root, "heroLogin"), false);

        Node brand = findById(root, "brandLabel");
        if (brand instanceof Label label) {
            label.setTextFill(NEON_LILAC);
            label.setFont(Font.font(FONT_BOLD, 20));
        }

        Node brandSub = findById(root, "brandSub");
        if (brandSub instanceof Label label) {
            label.setTextFill(MUTED);
            label.setFont(Font.font(FONT, 10));
        }

        Node title = findById(root, "heroTitle");
        if (title instanceof Label label) {
            label.setTextFill(NEON_LILAC);
            label.setFont(Font.font(FONT_BOLD, 56));
            label.setEffect(glow(ROYAL_PURPLE, 18));
        }

        Node subtitle = findById(root, "heroSubtitle");
        if (subtitle instanceof Label label) {
            label.setTextFill(TEXT_2);
            label.setFont(Font.font(FONT, 16));
        }

        String[] ids = {
                "featureTasks", "featureGoals", "featureFocus",
                "featureAnalytics", "featureLife"
        };

        Color[] colors = {
                ELECTRIC_VIOLET, ROYAL_PURPLE, PINK, PERIWINKLE, HOT_ORCHID
        };

        for (int i = 0; i < ids.length; i++) {
            Node node = findById(root, ids[i]);

            if (node instanceof VBox card) {
                Color c = colors[i];

                card.setBackground(background(Color.web("#120B1FEF"), 18));
                card.setBorder(border(c, 1.4));
                card.setPadding(new Insets(18));
                card.setEffect(glow(c, 7));

                card.setOnMouseEntered(e -> {
                    card.setTranslateY(-6);
                    card.setBackground(background(Color.web("#1D1030F5"), 18));
                    card.setEffect(glow(c, 15));
                });

                card.setOnMouseExited(e -> {
                    card.setTranslateY(0);
                    card.setBackground(background(Color.web("#120B1FEF"), 18));
                    card.setEffect(glow(c, 7));
                });
            }
        }

        Node privacy = findById(root, "privacyPill");
        if (privacy instanceof Label label) {
            label.setTextFill(NEON_LILAC);
            label.setFont(Font.font(FONT_BOLD, 11));
        }

        Node footer = findById(root, "footerLabel");
        if (footer instanceof Label label) {
            label.setTextFill(MUTED);
        }
    }

    private static void styleHomeButton(Node node, boolean primary) {
        if (!(node instanceof Button button)) return;

        button.setFont(Font.font(FONT_BOLD, 13));
        button.setPadding(new Insets(11, 20, 11, 20));
        button.setTextFill(TEXT);
        button.setBackground(background(primary ? ROYAL_PURPLE : SURFACE, 13));
        button.setBorder(border(primary ? ELECTRIC_VIOLET : BORDER, primary ? 1.4 : 1));
        button.setEffect(primary ? glow(ROYAL_PURPLE, 8) : null);

        button.setOnMouseEntered(e -> {
            button.setTranslateY(-2);
            button.setBackground(background(primary ? HOT_ORCHID : SURFACE_3, 13));
            button.setEffect(glow(primary ? HOT_ORCHID : ROYAL_PURPLE, 13));
        });

        button.setOnMouseExited(e -> {
            button.setTranslateY(0);
            button.setBackground(background(primary ? ROYAL_PURPLE : SURFACE, 13));
            button.setEffect(primary ? glow(ROYAL_PURPLE, 8) : null);
        });
    }

    // ---------- AUTH ----------

    private static void styleAuth(Node root, String viewName) {
        if (root instanceof Region region) {
            region.setBackground(new Background(new BackgroundFill(
                    heroGradient(), CornerRadii.EMPTY, Insets.EMPTY)));
        }

        String cardId = "Login".equals(viewName) ? "loginCard" : "registerCard";
        Node cardNode = findById(root, cardId);

        if (cardNode instanceof VBox card) {
            card.setBackground(background(Color.web("#11091DEE"), 26));
            card.setBorder(border(ROYAL_PURPLE, 1.5));
            card.setPadding(new Insets(38, 44, 38, 44));
            card.setEffect(glow(ROYAL_PURPLE, 18));
        }
    }

    // ---------- ALL OTHER WORKSPACES ----------

    private static void styleWorkspace(Node root, Color accent, String viewName) {
        if (root instanceof Region region) {
            region.setBackground(background(NIGHT, 0));
        }

        // Make the page header feel like a real application toolbar.
        if (root instanceof BorderPane pane) {
            Node top = pane.getTop();

            if (top instanceof Region region) {
                region.setBackground(background(Color.web("#0D0818F5"), 0));
                region.setBorder(new Border(new BorderStroke(
                        BORDER,
                        BorderStrokeStyle.SOLID,
                        CornerRadii.EMPTY,
                        new BorderWidths(0, 0, 1, 0)
                )));
            }
        }

        // Give common workspace tables a large, premium surface.
        for (String id : new String[]{
                "taskTable", "goalTable", "projectTable", "habitTable",
                "expenseTable", "incomeTable", "historyTable"
        }) {
            Node table = findById(root, id);
            if (table instanceof TableView<?> tv) {
                tv.setEffect(glow(accent, 5));
            }
        }

        // Large page title labels.
        if (root instanceof Parent parent) {
            styleLargeTitles(parent);
        }

        // View-specific visual emphasis.
        switch (viewName) {
            case "Focus" -> styleFocus(root, accent);
            case "Analytics" -> styleAnalytics(root, accent);
            case "Finance" -> styleFinance(root, accent);
            case "Habits" -> styleHabits(root, accent);
            case "Calendar" -> styleCalendar(root, accent);
            case "Settings" -> styleSettings(root, accent);
            default -> {}
        }
    }

    private static void styleLargeTitles(Parent parent) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            if (node instanceof Label label) {
                double size = label.getFont().getSize();
                if (size >= 22) {
                    label.setFont(Font.font(FONT_BOLD, Math.min(size + 4, 32)));
                    label.setTextFill(NEON_LILAC);
                    label.setEffect(glow(ROYAL_PURPLE, 5));
                }
            }
            if (node instanceof Parent child) {
                styleLargeTitles(child);
            }
        }
    }

    private static void styleFocus(Node root, Color accent) {
        Node timer = findById(root, "timerLabel");
        if (timer instanceof Label label) {
            label.setFont(Font.font(FONT_BOLD, 58));
            label.setTextFill(NEON_LILAC);
            label.setEffect(glow(accent, 22));
        }

        Node status = findById(root, "statusLabel");
        if (status instanceof Label label) {
            label.setTextFill(TEXT_2);
        }
    }

    private static void styleAnalytics(Node root, Color accent) {
        for (String id : new String[]{
                "pulseLabel", "productivityLabel", "habitConsistencyLabel",
                "taskCompletionLabel", "goalCompletionLabel", "focusScoreLabel",
                "financeLabel"
        }) {
            Node n = findById(root, id);
            if (n instanceof Label label) {
                label.setFont(Font.font(FONT_BOLD, 27));
                label.setTextFill(NEON_LILAC);
            }
        }

        for (String id : new String[]{
                "pulseProgress", "productivityProgress", "habitProgress"
        }) {
            Node n = findById(root, id);
            if (n instanceof ProgressBar bar) {
                bar.setPrefHeight(11);
                bar.setStyle("-fx-accent: " + hex(accent) + ";");
            }
        }
    }

    private static void styleFinance(Node root, Color accent) {
        Color[] colors = {SOFT_GREEN, PEACH, NEON_LILAC, SKY_BLUE};

        String[] ids = {"incomeLabel", "expenseLabel", "balanceLabel", "savingsLabel"};

        for (int i = 0; i < ids.length; i++) {
            Node n = findById(root, ids[i]);
            if (n instanceof Label label) {
                label.setFont(Font.font(FONT_BOLD, 20));
                label.setTextFill(colors[i]);
            }
        }
    }

    private static void styleHabits(Node root, Color accent) {
        Node weekly = findById(root, "weeklyProgressBar");
        if (weekly instanceof ProgressBar bar) {
            bar.setPrefHeight(12);
            bar.setStyle("-fx-accent: " + hex(SOFT_GREEN) + ";");
        }

        Node status = findById(root, "selectedStatusLabel");
        if (status instanceof Label label) {
            label.setTextFill(TEXT_2);
            label.setWrapText(true);
        }
    }

    private static void styleCalendar(Node root, Color accent) {
        Node month = findById(root, "monthLabel");
        if (month instanceof Label label) {
            label.setTextFill(NEON_LILAC);
            label.setFont(Font.font(FONT_BOLD, 21));
        }

        Node selected = findById(root, "selectedDateLabel");
        if (selected instanceof Label label) {
            label.setTextFill(accent);
            label.setFont(Font.font(FONT_BOLD, 14));
        }
    }

    private static void styleSettings(Node root, Color accent) {
        for (String id : new String[]{
                "accountLabel", "fontScaleLabel"
        }) {
            Node n = findById(root, id);
            if (n instanceof Label label) {
                label.setTextFill(NEON_LILAC);
            }
        }
    }

    // ---------- DIALOGS & ALERTS ----------

    /**
     * Themes a Dialog/Alert's DialogPane so pop-ups (task/goal/project/habit
     * forms, the command palette, and every AlertUtil message) match the
     * dark MOMENTA surface instead of falling back to JavaFX's default
     * white "Modena" chrome. Call this once, right after the dialog's
     * content and button types are set.
     *
     * DialogPane builds its header/content/button-bar regions lazily, so
     * the actual re-skin happens on sceneProperty() (immediately if the
     * scene already exists, otherwise the moment showAndWait() creates it).
     */
    public static void styleDialog(DialogPane pane) {
        if (pane == null) return;

        Color accent = accentColor();

        pane.setBackground(background(SURFACE, 0));
        pane.setBorder(border(BORDER, 1));
        pane.setStyle("-fx-background-color: " + hex(SURFACE) + ";");

        styleTree(pane, accent, true);

        Runnable chrome = () -> applyDialogChrome(pane, accent);

        if (pane.getScene() != null) {
            chrome.run();
        }
        pane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) chrome.run();
        });
    }

    private static void applyDialogChrome(DialogPane pane, Color accent) {
        pane.applyCss();
        pane.layout();

        Node header = pane.lookup(".header-panel");
        if (header != null) {
            header.setStyle(
                    "-fx-background-color: " + hex(SURFACE_2) + ";" +
                            "-fx-border-color: " + hex(BORDER) + ";" +
                            "-fx-border-width: 0 0 1 0;"
            );
        }

        for (Node n : pane.lookupAll(".header-panel .label")) {
            if (n instanceof Label label) {
                label.setTextFill(NEON_LILAC);
                label.setFont(Font.font(FONT_BOLD, 16));
            }
        }

        Node message = pane.lookup(".content.label");
        if (message instanceof Label label) {
            label.setTextFill(TEXT_2);
            label.setFont(Font.font(FONT, 13));
            label.setWrapText(true);
        }

        Node buttonBar = pane.lookup(".button-bar");
        if (buttonBar != null) {
            buttonBar.setStyle("-fx-background-color: " + hex(SURFACE) + ";");
        }

        for (ButtonType type : pane.getButtonTypes()) {
            Node buttonNode = pane.lookupButton(type);
            if (buttonNode instanceof Button button) {
                styleButton(button, accent);
            }
        }
    }

    // ---------- COMPONENTS ----------

    private static void styleCard(Node node, Color accent) {
        if (!(node instanceof Region card)) return;

        card.setBackground(background(SURFACE, 18));
        card.setBorder(border(accent, 1.4));
        card.setPadding(new Insets(17));
        card.setEffect(glow(accent, 7));

        card.setOnMouseEntered(e -> {
            card.setTranslateY(-3);
            card.setBackground(background(SURFACE_2, 18));
            card.setEffect(glow(accent, 14));
        });

        card.setOnMouseExited(e -> {
            card.setTranslateY(0);
            card.setBackground(background(SURFACE, 18));
            card.setEffect(glow(accent, 7));
        });
    }

    private static void styleButton(Button button, Color accent) {
        button.setFont(Font.font(FONT_BOLD, 12.5));
        button.setTextFill(TEXT);
        button.setPadding(new Insets(9, 15, 9, 15));
        button.setBackground(background(SURFACE, 11));
        button.setBorder(border(accent, 1));
        button.setCursor(javafx.scene.Cursor.HAND);

        button.setOnMouseEntered(e -> {
            button.setTranslateY(-1);
            button.setTextFill(Color.WHITE);
            button.setBackground(background(accent.deriveColor(0, 0.22, 0.92, 1), 11));
            button.setBorder(border(accent, 1.4));
            button.setEffect(glow(accent, 10));
        });

        button.setOnMouseExited(e -> {
            button.setTranslateY(0);
            button.setTextFill(TEXT);
            button.setBackground(background(SURFACE, 11));
            button.setBorder(border(accent, 1));
            button.setEffect(null);
        });

        button.setOnMousePressed(e -> button.setTranslateY(1));
        button.setOnMouseReleased(e -> button.setTranslateY(0));
    }

    private static void styleInput(TextInputControl input, Color accent) {
        input.setFont(Font.font(FONT, 13));
        input.setBackground(background(SURFACE, 11));
        input.setBorder(border(BORDER, 1));
        input.setPadding(new Insets(10, 12, 10, 12));

        input.setStyle(
                "-fx-text-fill: " + hex(TEXT) + ";" +
                        "-fx-prompt-text-fill: " + hex(MUTED) + ";" +
                        "-fx-highlight-fill: " + hex(accent) + ";" +
                        "-fx-highlight-text-fill: white;"
        );

        input.focusedProperty().addListener((obs, oldVal, focused) -> {
            input.setBorder(border(focused ? accent : BORDER, focused ? 1.5 : 1));
            if (focused) input.setEffect(glow(accent, 7));
            else input.setEffect(null);
        });
    }

    private static void styleCombo(ComboBoxBase<?> combo, Color accent) {
        combo.setBackground(background(SURFACE, 10));
        combo.setBorder(border(BORDER, 1));
        combo.setStyle(
                "-fx-background-color: " + hex(SURFACE) + ";" +
                        "-fx-text-fill: " + hex(TEXT) + ";" +
                        "-fx-accent: " + hex(accent) + ";" +
                        "-fx-font-family: '" + FONT + "';" +
                        "-fx-font-size: 13px;"
        );
    }

    private static void styleTable(TableView<?> table, Color accent) {
        table.setMaxWidth(Double.MAX_VALUE);
        table.setMaxHeight(Double.MAX_VALUE);
        table.setBackground(background(SURFACE, 16));
        table.setBorder(border(BORDER, 1));
        table.setPlaceholder(makePlaceholder());
        table.setStyle(
                "-fx-background-color: " + hex(SURFACE) + ";" +
                        "-fx-control-inner-background: " + hex(SURFACE) + ";" +
                        "-fx-table-cell-border-color: " + hex(BORDER_SOFT) + ";" +
                        "-fx-selection-bar: " + hex(DEEP_PLUM) + ";" +
                        "-fx-selection-bar-non-focused: " + hex(DEEP_PLUM) + ";"
        );
    }

    private static Label makePlaceholder() {
        Label label = new Label("✦  Nothing here yet");
        label.setTextFill(MUTED);
        label.setFont(Font.font(FONT_BOLD, 14));
        return label;
    }

    private static void styleList(ListView<?> list, Color accent) {
        list.setMaxWidth(Double.MAX_VALUE);
        list.setBackground(background(SURFACE, 14));
        list.setBorder(border(BORDER, 1));
        list.setStyle(
                "-fx-background-color: " + hex(SURFACE) + ";" +
                        "-fx-control-inner-background: " + hex(SURFACE) + ";" +
                        "-fx-selection-bar: " + hex(DEEP_PLUM) + ";"
        );
    }

    // ---------- VISUAL HELPERS ----------

    private static Font fontFor(Label label) {
        double size = label.getFont() == null ? 13 : label.getFont().getSize();
        boolean bold = size >= 16;
        return Font.font(bold ? FONT_BOLD : FONT, scaled(size));
    }

    private static double scaled(double size) {
        return size * SettingsService.getFontScale();
    }

    private static Color accentFromName(String name) {
        return switch (name == null ? "Royal Purple" : name) {
            case "Royal Purple" -> ROYAL_PURPLE;
            case "Electric Violet" -> ELECTRIC_VIOLET;
            case "Plum" -> DEEP_PLUM;
            case "Sky Blue" -> SKY_BLUE;
            case "Lavender" -> NEON_LILAC;
            case "Soft Green" -> SOFT_GREEN;
            case "Peach" -> PEACH;
            case "Pink" -> PINK;
            case "Periwinkle" -> PERIWINKLE;
            default -> ROYAL_PURPLE;
        };
    }

    private static Background background(Color color, double radius) {
        return new Background(new BackgroundFill(
                color,
                new CornerRadii(radius),
                Insets.EMPTY
        ));
    }

    private static Border border(Color color, double width) {
        return new Border(new BorderStroke(
                color,
                BorderStrokeStyle.SOLID,
                new CornerRadii(12),
                new BorderWidths(width)
        ));
    }

    private static DropShadow glow(Color color, double radius) {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                0.38
        ));
        shadow.setRadius(radius);
        shadow.setSpread(0.10);
        return shadow;
    }

    private static String hex(Color color) {
        return String.format(
                "#%02X%02X%02X",
                (int) Math.round(color.getRed() * 255),
                (int) Math.round(color.getGreen() * 255),
                (int) Math.round(color.getBlue() * 255)
        );
    }

    private static LinearGradient sidebarGradient() {
        return new LinearGradient(
                0, 0, 1, 1,
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0B0613")),
                new Stop(0.45, Color.web("#160A27")),
                new Stop(1, Color.web("#0A0611"))
        );
    }

    private static LinearGradient heroGradient() {
        return new LinearGradient(
                0, 0, 1, 1,
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#08040F")),
                new Stop(0.35, Color.web("#17082A")),
                new Stop(0.68, Color.web("#24103A")),
                new Stop(1, Color.web("#09050F"))
        );
    }

    private static Node findById(Node root, String id) {
        if (id.equals(root.getId())) return root;

        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                Node found = findById(child, id);
                if (found != null) return found;
            }
        }

        return null;
    }
}
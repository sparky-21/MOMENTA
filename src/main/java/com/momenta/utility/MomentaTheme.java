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
import javafx.application.Platform;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import java.util.List;

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

    // Module accents — deliberately softer than the core purple so the UI
    // stays colorful without becoming a neon/gaming interface.
    public static final Color TASKS_ACCENT = Color.web("#5DADE2");
    public static final Color GOALS_ACCENT = Color.web("#9B8AFB");
    public static final Color PROJECTS_ACCENT = Color.web("#A5B4FC");
    public static final Color HABITS_ACCENT = Color.web("#6BCB9A");
    public static final Color FINANCE_ACCENT = Color.web("#F4A261");
    public static final Color FOCUS_ACCENT = Color.web("#E78AC3");
    public static final Color ANALYTICS_ACCENT = Color.web("#8E9FE6");
    public static final Color CALENDAR_ACCENT = Color.web("#67E8F9");
    public static final Color SETTINGS_ACCENT = Color.web("#8B5CF6");

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
            case "Tasks" -> TASKS_ACCENT;
            case "Goals" -> GOALS_ACCENT;
            case "Projects" -> PROJECTS_ACCENT;
            case "Habits" -> HABITS_ACCENT;
            case "Finance" -> FINANCE_ACCENT;
            case "Focus" -> FOCUS_ACCENT;
            case "Analytics" -> ANALYTICS_ACCENT;
            case "Calendar" -> CALENDAR_ACCENT;
            case "Settings", "Home", "Login", "Register" -> SETTINGS_ACCENT;
            default -> SETTINGS_ACCENT;
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

            Node sidebarBrand = findById(root, "sidebarBrand");
            if (sidebarBrand instanceof Label label) {
                label.setTextFill(NEON_LILAC);
                label.setFont(Font.font(FONT_BOLD, 25));
                label.setEffect(glow(ROYAL_PURPLE, 8));
            }
            Node sidebarSub = findById(root, "sidebarSub");
            if (sidebarSub instanceof Label label) {
                label.setTextFill(MUTED);
                label.setFont(Font.font(FONT_BOLD, 9.5));
            }

            for (Node child : sidebar.getChildren()) {
                if (child instanceof Button button) {
                    styleNavButton(button, moduleAccentFor(button.getText()));
                }
            }
        }

        styleCard(findById(root, "pulseCard"), ELECTRIC_VIOLET);
        styleCard(findById(root, "taskCard"), TASKS_ACCENT);
        styleCard(findById(root, "goalCard"), GOALS_ACCENT);
        styleCard(findById(root, "projectCard"), PROJECTS_ACCENT);
        styleCard(findById(root, "nowCard"), ROYAL_PURPLE);
        styleHeroCard(findById(root, "nowCard"), ROYAL_PURPLE);
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

        Node dashboardScroll = findByType(root, ScrollPane.class);
        if (dashboardScroll instanceof ScrollPane scroll) {
            scroll.setBackground(background(NIGHT, 0));
            scroll.setStyle("-fx-background-color: " + hex(NIGHT) + "; -fx-background: " + hex(NIGHT) + ";");
            if (scroll.getContent() instanceof VBox content) {
                content.setBackground(background(NIGHT, 0));
                content.setSpacing(20);
            }
        }

        styleDashboardList(findById(root, "notificationList"));
        styleDashboardSurface(findById(root, "notificationsCard"), BORDER_SOFT);
        styleDashboardHero(findById(root, "dashboardHero"));
        styleDashboardSurface(findById(root, "gamificationCard"), HOT_ORCHID);
        styleDashboardSurface(findById(root, "insightCard"), PERIWINKLE);
        styleHeroMetric(findById(root, "pulseCard"), ELECTRIC_VIOLET);
        styleHeroMetric(findById(root, "taskCard"), TASKS_ACCENT);
        styleHeroMetric(findById(root, "goalCard"), GOALS_ACCENT);
        styleHeroMetric(findById(root, "projectCard"), PROJECTS_ACCENT);

        // Strong metric labels.
        Node status = findById(root, "dashboardStatus");
        if (status instanceof Label label) {
            label.setTextFill(SOFT_GREEN);
            label.setFont(Font.font(FONT_BOLD, 9.5));
        }

        Node subtitle = findById(root, "dashboardSubtitle");
        if (subtitle instanceof Label label) {
            label.setTextFill(TEXT_2);
            label.setFont(Font.font(FONT, 12.5));
            label.setWrapText(true);
        }

        Node kicker = findById(root, "dashboardKicker");
        if (kicker instanceof Label label) {
            label.setTextFill(NEON_LILAC);
            label.setFont(Font.font(FONT_BOLD, 10.5));
            label.setEffect(glow(ROYAL_PURPLE, 6));
        }

        Node nowKicker = findById(root, "nowKicker");
        if (nowKicker instanceof Label label) {
            label.setTextFill(ROYAL_PURPLE);
            label.setFont(Font.font(FONT_BOLD, 10));
        }

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

    private static void styleDashboardHero(Node node) {
        if (!(node instanceof Region region)) return;
        region.setBackground(new Background(new BackgroundFill(
                heroCardGradient(ROYAL_PURPLE), new CornerRadii(20), Insets.EMPTY)));
        region.setBorder(border(ROYAL_PURPLE.deriveColor(0, 0.75, 1, 0.52), 1));
        region.setPadding(new Insets(18, 20, 18, 20));
        region.setEffect(softShadow(ROYAL_PURPLE));
    }

    private static void styleDashboardSurface(Node node, Color accent) {
        if (!(node instanceof Region region)) return;
        region.setBackground(background(SURFACE, 18));
        region.setBorder(border(accent.deriveColor(0, 0.72, 1, 0.42), 1));
        region.setEffect(softShadow(accent));
    }

    private static void styleNavButton(Button button, Color itemAccent) {
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
            button.setBorder(new Border(new BorderStroke(
                    itemAccent.deriveColor(0, 0.75, 1, 0.75),
                    BorderStrokeStyle.SOLID, new CornerRadii(12),
                    new BorderWidths(0, 0, 0, 3))));
            button.setEffect(glow(itemAccent, 5));
        });

        button.setOnMouseExited(e -> {
            button.setTextFill(TEXT_2);
            button.setTranslateX(0);
            button.setBackground(background(Color.TRANSPARENT, 12));
            button.setBorder(Border.EMPTY);
            button.setEffect(null);
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

        Node scrollNode = findByType(root, ScrollPane.class);
        if (scrollNode instanceof ScrollPane scroll) {
            scroll.setBackground(background(Color.TRANSPARENT, 0));
            scroll.setBorder(Border.EMPTY);
            scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

            // The ScrollPane control's own background/style, set above, does
            // NOT reach the ".viewport" (and internal ".scroll-pane") nodes
            // its Skin creates — those carry their own default (light)
            // background from the platform stylesheet as separate painted
            // layers stacked in front of whatever the ScrollPane itself is
            // set to. polishWorkspace() already patches this exact thing for
            // every workspace screen (Tasks, Dashboard, ...); Login/Register
            // are the only other views built on a ScrollPane and need the
            // same patch, or their card can appear to be floating over a
            // plain white panel that never gets themed.
            Platform.runLater(() -> {
                scroll.applyCss();
                for (Node node : scroll.lookupAll(".viewport")) {
                    node.setStyle("-fx-background-color: transparent;");
                }
                for (Node node : scroll.lookupAll(".scroll-pane")) {
                    if (node instanceof Control control) {
                        control.setStyle(
                                "-fx-background-color: transparent;" +
                                "-fx-background: transparent;" +
                                "-fx-border-color: transparent;"
                        );
                    }
                }
            });
        }

        if (cardNode instanceof VBox card) {
            card.setBackground(new Background(new BackgroundFill(
                    heroCardGradient(SETTINGS_ACCENT),
                    new CornerRadii(26), Insets.EMPTY)));
            card.setBorder(border(SETTINGS_ACCENT.deriveColor(0, 0.85, 1, 0.9), 1.2));
            card.setPadding(new Insets(40, 46, 40, 46));
            card.setSpacing(15);
            card.setEffect(glow(SETTINGS_ACCENT, 16));
            card.setMaxWidth("Register".equals(viewName) ? 480 : 430);

            Node brand = findById(root, "registerBrand");
            if (brand == null) brand = findById(root, "loginBrand");
            if (brand instanceof Label label) {
                label.setTextFill(NEON_LILAC);
                label.setFont(Font.font(FONT_BOLD, 34));
                label.setEffect(glow(ROYAL_PURPLE, 10));
            }
            Node kicker = findById(root, "registerKicker");
            if (kicker instanceof Label label) {
                label.setTextFill(SETTINGS_ACCENT);
                label.setFont(Font.font(FONT_BOLD, 11));
            }
            Node subtitle = findById(root, "registerSubtitle");
            if (subtitle == null) subtitle = findById(root, "loginSubtitle");
            if (subtitle instanceof Label label) {
                label.setTextFill(TEXT_2);
                label.setFont(Font.font(FONT, 13));
            }

            for (Node child : card.getChildren()) {
                if (child instanceof Label label && label.getText() != null) {
                    if (label.getText().toLowerCase().contains("momenta") ||
                        label.getText().toLowerCase().contains("create your")) {
                        label.setTextFill(NEON_LILAC);
                        label.setFont(Font.font(FONT_BOLD, 27));
                    }
                }
            }

            // Authentication buttons need an explicit JavaFX inline style so the
            // platform's default Button skin cannot turn them into light/white
            // controls. Keep the primary/secondary hierarchy inside the dark theme.
            int buttonIndex = 0;
            for (Node child : card.getChildren()) {
                if (child instanceof Button button) {
                    boolean primary = buttonIndex++ == 0;
                    styleAuthButton(button, primary);
                }
            }
        }
    }

    private static void styleAuthButton(Button button, boolean primary) {
        Color base = primary ? ROYAL_PURPLE : SURFACE;
        Color hover = primary ? ELECTRIC_VIOLET : SURFACE_3;
        Color edge = primary ? ELECTRIC_VIOLET : BORDER;

        button.setFont(Font.font(FONT_BOLD, 13));
        button.setTextFill(TEXT);
        button.setPadding(new Insets(11, 20, 11, 20));
        button.setCursor(javafx.scene.Cursor.HAND);
        button.setStyle(
                "-fx-text-fill: " + hex(TEXT) + ";" +
                "-fx-font-family: 'Segoe UI Semibold';" +
                "-fx-font-size: 13px;" +
                "-fx-font-weight: 600;" +
                "-fx-background-color: " + hex(base) + ";" +
                "-fx-background-radius: 13;" +
                "-fx-border-color: " + hex(edge) + ";" +
                "-fx-border-width: 1.2;" +
                "-fx-border-radius: 13;" +
                "-fx-padding: 11 20 11 20;"
        );
        button.setEffect(primary ? glow(ROYAL_PURPLE, 7) : null);

        button.setOnMouseEntered(e -> {
            button.setTranslateY(-1);
            button.setTextFill(TEXT);
            button.setStyle(
                    "-fx-text-fill: " + hex(TEXT) + ";" +
                    "-fx-font-family: 'Segoe UI Semibold';" +
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: 600;" +
                    "-fx-background-color: " + hex(hover) + ";" +
                    "-fx-background-radius: 13;" +
                    "-fx-border-color: " + hex(primary ? HOT_ORCHID : ROYAL_PURPLE) + ";" +
                    "-fx-border-width: 1.4;" +
                    "-fx-border-radius: 13;" +
                    "-fx-padding: 11 20 11 20;"
            );
            button.setEffect(glow(primary ? HOT_ORCHID : ROYAL_PURPLE, 10));
        });

        button.setOnMouseExited(e -> {
            button.setTranslateY(0);
            button.setTextFill(TEXT);
            button.setStyle(
                    "-fx-text-fill: " + hex(TEXT) + ";" +
                    "-fx-font-family: 'Segoe UI Semibold';" +
                    "-fx-font-size: 13px;" +
                    "-fx-font-weight: 600;" +
                    "-fx-background-color: " + hex(base) + ";" +
                    "-fx-background-radius: 13;" +
                    "-fx-border-color: " + hex(edge) + ";" +
                    "-fx-border-width: 1.2;" +
                    "-fx-border-radius: 13;" +
                    "-fx-padding: 11 20 11 20;"
            );
            button.setEffect(primary ? glow(ROYAL_PURPLE, 7) : null);
        });

        button.setOnMousePressed(e -> button.setTranslateY(1));
        button.setOnMouseReleased(e -> button.setTranslateY(0));
    }

    // ---------- ALL OTHER WORKSPACES ----------

    private static void styleWorkspace(Node root, Color accent, String viewName) {
        if (root instanceof Region region) {
            region.setBackground(background(NIGHT, 0));
        }

        // Make the page header feel like a real application toolbar.
        if (root instanceof BorderPane pane) {
            Node top = pane.getTop();
            Node center = pane.getCenter();

            if (top instanceof Region region) {
                region.setBackground(new Background(new BackgroundFill(
                        headerGradient(accent), CornerRadii.EMPTY, Insets.EMPTY)));
                region.setBorder(new Border(new BorderStroke(
                        accent.deriveColor(0, 1, 1, 0.28),
                        BorderStrokeStyle.SOLID,
                        CornerRadii.EMPTY,
                        new BorderWidths(0, 0, 1, 0)
                )));
                region.setPadding(new Insets(14, 24, 12, 24));
            }

            if (center instanceof Region region) {
                region.setBackground(background(NIGHT, 0));
            }
        }

        // JavaFX's default viewport/table/chart chrome can remain white even
        // after the parent is themed. Polish those internal skins after CSS
        // has been created, without introducing an external stylesheet.
        Platform.runLater(() -> polishWorkspace(root, accent));

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

        // Premium surface groups and module-specific workspace framing.
        styleWorkspaceSurfaceGroups(root, accent, viewName);

        // View-specific visual emphasis.
        switch (viewName) {
            case "Tasks" -> styleTaskWorkspace(root, accent);
            case "Goals" -> styleGoalWorkspace(root, accent);
            case "Projects" -> styleProjectWorkspace(root, accent);
            case "Focus" -> styleFocus(root, accent);
            case "Analytics" -> styleAnalytics(root, accent);
            case "Finance" -> styleFinance(root, accent);
            case "Habits" -> styleHabits(root, accent);
            case "Calendar" -> styleCalendar(root, accent);
            case "Settings" -> styleSettings(root, accent);
            default -> {}
        }
    }

    private static void polishWorkspace(Node root, Color accent) {
        if (!(root instanceof Parent parent)) return;

        parent.applyCss();

        for (Node node : parent.lookupAll(".scroll-pane")) {
            if (node instanceof Control control) {
                control.setStyle(
                        "-fx-background-color: " + hex(NIGHT) + ";" +
                        "-fx-background: " + hex(NIGHT) + ";" +
                        "-fx-border-color: transparent;"
                );
            }
        }

        for (Node node : parent.lookupAll(".viewport")) {
            node.setStyle("-fx-background-color: " + hex(NIGHT) + ";");
        }

        for (Node node : parent.lookupAll(".table-view")) {
            node.setStyle(
                    "-fx-background-color: " + hex(SURFACE) + ";" +
                    "-fx-control-inner-background: " + hex(SURFACE) + ";" +
                    "-fx-table-cell-border-color: " + hex(BORDER_SOFT) + ";" +
                    "-fx-selection-bar: " + hex(accent.deriveColor(0, 0.75, 0.82, 1)) + ";" +
                    "-fx-selection-bar-non-focused: " + hex(accent.deriveColor(0, 0.75, 0.70, 1)) + ";" +
                    "-fx-font-family: 'Segoe UI';" +
                    "-fx-font-size: 13px;"
            );
        }

        for (Node node : parent.lookupAll(".column-header-background")) {
            node.setStyle("-fx-background-color: " + hex(SURFACE_2) + ";");
        }

        for (Node node : parent.lookupAll(".column-header")) {
            node.setStyle(
                    "-fx-background-color: " + hex(SURFACE_2) + ";" +
                    "-fx-border-color: " + hex(BORDER_SOFT) + ";" +
                    "-fx-border-width: 0 0 1 0;"
            );
        }

        for (Node node : parent.lookupAll(".filler")) {
            node.setStyle("-fx-background-color: " + hex(SURFACE_2) + ";");
        }

        for (Node node : parent.lookupAll(".table-row-cell")) {
            node.setStyle(
                    "-fx-background-color: " + hex(SURFACE) + ";" +
                    "-fx-border-color: " + hex(BORDER_SOFT) + ";" +
                    "-fx-border-width: 0 0 1 0;"
            );
        }

        for (Node node : parent.lookupAll(".table-cell")) {
            if (node instanceof Labeled labeled) {
                labeled.setTextFill(TEXT_2);
            }
        }

        stylePremiumCharts(parent, accent);

        for (Node node : parent.lookupAll(".spinner")) {
            node.setStyle(
                    "-fx-background-color: " + hex(SURFACE) + ";" +
                    "-fx-border-color: " + hex(BORDER) + ";" +
                    "-fx-border-radius: 10;" +
                    "-fx-background-radius: 10;"
            );
        }
    }

    private static void stylePremiumCharts(Parent parent, Color accent) {
        for (Node node : parent.lookupAll(".chart")) {
            node.setStyle(
                    "-fx-background-color: " + hex(SURFACE) + ";" +
                    "-fx-padding: 14;" +
                    "-fx-border-color: " + hex(BORDER) + ";" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 16;" +
                    "-fx-background-radius: 16;"
            );
        }
        for (Node node : parent.lookupAll(".chart-plot-background")) {
            node.setStyle("-fx-background-color: " + hex(SURFACE) + ";");
        }
        for (Node node : parent.lookupAll(".chart-vertical-grid-lines")) {
            node.setStyle("-fx-stroke: " + hex(BORDER_SOFT) + "; -fx-stroke-dash-array: 2 5;");
        }
        for (Node node : parent.lookupAll(".chart-horizontal-grid-lines")) {
            node.setStyle("-fx-stroke: " + hex(BORDER_SOFT) + "; -fx-stroke-dash-array: 2 5;");
        }
        for (Node node : parent.lookupAll(".axis")) {
            node.setStyle("-fx-tick-label-fill: " + hex(MUTED) + "; -fx-tick-mark-stroke: " + hex(BORDER) + "; -fx-minor-tick-visible: false;");
        }
        for (Node node : parent.lookupAll(".axis-label")) {
            if (node instanceof Label label) {
                label.setTextFill(MUTED);
                label.setFont(Font.font(FONT, 10.5));
            }
        }
        for (Node node : parent.lookupAll(".chart-title")) {
            if (node instanceof Label label) {
                label.setTextFill(TEXT);
                label.setFont(Font.font(FONT_BOLD, 15));
            }
        }
        for (Node node : parent.lookupAll(".chart-legend")) {
            node.setStyle("-fx-background-color: " + hex(SURFACE_2) + "; -fx-background-radius: 10; -fx-padding: 7;");
        }
        for (Node node : parent.lookupAll(".chart-legend-item")) {
            if (node instanceof Labeled labeled) labeled.setTextFill(TEXT_2);
        }
        // PieChart labels are rendered as Text nodes, not JavaFX Label controls.
        // Therefore setting Label.textFill alone does not affect the visible
        // Food / Transport text on a dark chart background.
        for (Node node : parent.lookupAll(".chart-pie-label")) {
            if (node instanceof Text text) {
                text.setFill(TEXT_2);
                text.setFont(Font.font(FONT_BOLD, 11.5));
            } else if (node instanceof Labeled labeled) {
                labeled.setTextFill(TEXT_2);
            }
        }

        // Make the connector lines visible as well.
        for (Node node : parent.lookupAll(".chart-pie-label-line")) {
            node.setStyle("-fx-stroke: " + hex(TEXT_2) + "; -fx-stroke-width: 1.1;");
        }
        for (Node node : parent.lookupAll(".chart-content")) {
            node.setEffect(softShadow(accent));
        }
    }

    private static void styleWorkspaceSurfaceGroups(Node root, Color accent, String viewName) {
        if (!(root instanceof BorderPane pane)) return;

        Node center = pane.getCenter();
        if (center instanceof ScrollPane scroll && scroll.getContent() instanceof Parent content) {
            styleSurfaceGroups(content, accent);
        } else if (center instanceof Parent parent) {
            styleSurfaceGroups(parent, accent);
        }

        styleNamedSurface(findById(root, "workspaceHeader"), accent, 14, false);
        styleNamedSurface(findById(root, "workspaceBody"), accent, 16, false);
        styleNamedSurface(findById(root, "habitDetailsCard"), SOFT_GREEN, 16, true);
        styleNamedSurface(findById(root, "financeIncomeCard"), SOFT_GREEN, 16, true);
        styleNamedSurface(findById(root, "financeExpenseCard"), PEACH, 16, true);
        styleNamedSurface(findById(root, "financeBalanceCard"), SKY_BLUE, 16, true);
        styleNamedSurface(findById(root, "financeSavingsCard"), NEON_LILAC, 16, true);
        styleNamedSurface(findById(root, "analyticsPulseCard"), ELECTRIC_VIOLET, 16, true);
        styleNamedSurface(findById(root, "analyticsProductivityCard"), SKY_BLUE, 16, true);
        styleNamedSurface(findById(root, "analyticsHabitCard"), SOFT_GREEN, 16, true);
        styleNamedSurface(findById(root, "settingsAppearanceCard"), accent, 16, true);
        styleNamedSurface(findById(root, "settingsStartupCard"), accent, 16, true);

        // Settings is intentionally more structured: each preference block
        // becomes a calm elevated panel instead of a flat white/default area.
        if ("Settings".equals(viewName)) {
            for (Node node : allNodes(root)) {
                if (node instanceof Label label && label.getText() != null &&
                        ("Appearance".equals(label.getText()) || "Startup".equals(label.getText()))) {
                    Node parent = label.getParent();
                    if (parent instanceof VBox section) {
                        section.setBackground(background(SURFACE, 16));
                        section.setBorder(border(BORDER, 1));
                        section.setPadding(new Insets(18));
                        section.setEffect(softShadow(accent));
                    }
                }
            }
        }

        // Focus gets a dedicated timer/control stage.
        if ("Focus".equals(viewName)) {
            for (Node node : allNodes(root)) {
                if (node instanceof Label label && "FOCUS".equals(label.getText())) {
                    Node parent = label.getParent();
                    if (parent instanceof VBox stage) {
                        stage.setBackground(new Background(new BackgroundFill(
                                heroCardGradient(accent), new CornerRadii(22), Insets.EMPTY)));
                        stage.setBorder(border(accent, 1.2));
                        stage.setPadding(new Insets(24));
                        stage.setEffect(glow(accent, 14));
                    }
                }
            }
        }

        // Workspace entry animation keeps every module consistent with the
        // existing dashboard motion language.

// পরে (ঠিক):
        Platform.runLater(() -> {
            if (center != null) {
                AnimationUtil.fadeIn(center, 220);
            }
        });
    }

    private static void styleSurfaceGroups(Node node, Color accent) {
        if (!(node instanceof Parent parent)) return;

        for (Node child : parent.getChildrenUnmodifiable()) {
            if (child instanceof Region region) {
                boolean containsTable = hasDirectType(region, TableView.class);
                boolean containsChart = hasDirectType(region, Chart.class);

                if (containsTable || containsChart) {
                    region.setBackground(background(SURFACE, 16));
                    region.setBorder(border(BORDER, 1));
                    region.setPadding(new Insets(14));
                    region.setEffect(softShadow(accent));
                }
            }
            styleSurfaceGroups(child, accent);
        }
    }

    private static boolean hasDirectType(Parent parent, Class<?> type) {
        for (Node child : parent.getChildrenUnmodifiable()) {
            if (type.isInstance(child)) return true;
        }
        return false;
    }

    private static List<Node> allNodes(Node root) {
        List<Node> nodes = new java.util.ArrayList<>();
        collectNodes(root, nodes);
        return nodes;
    }

    private static void collectNodes(Node node, List<Node> nodes) {
        nodes.add(node);
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                collectNodes(child, nodes);
            }
        }
    }

    private static void styleNamedSurface(Node node, Color accent, double radius, boolean padded) {
        if (!(node instanceof Region region)) return;
        region.setBackground(new Background(new BackgroundFill(
                cardGradient(accent), new CornerRadii(radius), Insets.EMPTY)));
        region.setBorder(border(accent.deriveColor(0, 0.78, 1, 0.55), 1));
        if (padded) region.setPadding(new Insets(14));
        region.setEffect(softShadow(accent));
    }

    private static void styleTaskWorkspace(Node root, Color accent) {
        styleWorkspaceToolbar(root, TASKS_ACCENT);
    }

    private static void styleGoalWorkspace(Node root, Color accent) {
        styleWorkspaceToolbar(root, GOALS_ACCENT);
    }

    private static void styleProjectWorkspace(Node root, Color accent) {
        styleWorkspaceToolbar(root, PROJECTS_ACCENT);
    }

    private static void styleWorkspaceToolbar(Node root, Color accent) {
        if (!(root instanceof BorderPane pane)) return;

        Node kickerNode = findById(root, "moduleKicker");
        if (kickerNode instanceof Label kicker) {
            kicker.setTextFill(accent);
            kicker.setFont(Font.font(FONT_BOLD, 10.5));
            kicker.setOpacity(0.95);
        }

        Node titleNode = findById(root, "pageTitle");
        if (titleNode instanceof Label title) {
            title.setTextFill(TEXT);
            title.setFont(Font.font(FONT_BOLD, 29));
            title.setEffect(glow(accent, 7));
        }

        Node subtitleNode = findById(root, "pageSubtitle");
        if (subtitleNode instanceof Label subtitle) {
            subtitle.setTextFill(TEXT_2);
            subtitle.setFont(Font.font(FONT, 12.5));
        }

        Node actionNode = findById(root, "actionBar");
        if (actionNode instanceof Region actions) {
            actions.setPadding(new Insets(4, 0, 2, 0));
        }

        Node tableNode = findById(root, "workspaceTable");
        if (tableNode instanceof TableView<?> table) {
            table.setEffect(softShadow(accent));
            table.setBorder(border(accent.deriveColor(0, 0.72, 1, 0.62), 1.1));
            table.setBackground(new Background(new BackgroundFill(
                    cardGradient(accent), new CornerRadii(16), Insets.EMPTY)));
        }

        if (pane.getTop() instanceof Region header) {
            header.setPadding(new Insets(18, 24, 16, 24));
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
        accent = FOCUS_ACCENT;
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
        Color[] colors = {SOFT_GREEN, FINANCE_ACCENT, NEON_LILAC, SKY_BLUE};

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

        card.setBackground(new Background(new BackgroundFill(
                cardGradient(accent), new CornerRadii(18), Insets.EMPTY)));
        card.setBorder(border(accent.deriveColor(0, 0.78, 1, 0.72), 1.15));
        card.setPadding(new Insets(18));
        card.setEffect(softShadow(accent));

        card.setOnMouseEntered(e -> {
            card.setTranslateY(-3);
            card.setBackground(background(SURFACE_2, 18));
            card.setEffect(glow(accent, 12));
        });

        card.setOnMouseExited(e -> {
            card.setTranslateY(0);
            card.setBackground(background(SURFACE, 18));
            card.setEffect(softShadow(accent));
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
                        "-fx-highlight-text-fill: white;" +
                        "-fx-control-inner-background: " + hex(SURFACE) + ";" +
                        "-fx-background-color: " + hex(SURFACE) + ";" +
                        "-fx-background-insets: 0;" +
                        "-fx-background-radius: 11;" +
                        "-fx-border-color: " + hex(BORDER) + ";" +
                        "-fx-border-radius: 11;" +
                        "-fx-border-width: 1;"
        );

        if (input instanceof TextArea area) {
            area.setWrapText(true);
            area.setPrefRowCount(Math.max(area.getPrefRowCount(), 4));
            Platform.runLater(() -> {
                area.applyCss();
                for (Node n : area.lookupAll(".content")) {
                    n.setStyle("-fx-background-color: " + hex(SURFACE) + ";");
                }
                for (Node n : area.lookupAll(".scroll-pane")) {
                    n.setStyle("-fx-background-color: " + hex(SURFACE) + ";");
                }
            });
        }

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
        table.setPlaceholder(makePlaceholder(table.getId()));
        table.setStyle(
                "-fx-background-color: " + hex(SURFACE) + ";" +
                        "-fx-control-inner-background: " + hex(SURFACE) + ";" +
                        "-fx-table-cell-border-color: " + hex(BORDER_SOFT) + ";" +
                        "-fx-selection-bar: " + hex(DEEP_PLUM) + ";" +
                        "-fx-selection-bar-non-focused: " + hex(DEEP_PLUM) + ";" +
                        "-fx-text-background-color: " + hex(TEXT) + ";"
        );

        // JavaFX TableView headers/cells are skin-created nodes, so styling the
        // TableView alone is not enough to prevent the default dark text.
        Platform.runLater(() -> {
            table.applyCss();
            for (Node headerBg : table.lookupAll(".column-header-background")) {
                headerBg.setStyle(
                        "-fx-background-color: " + hex(SURFACE_2) + ";" +
                        "-fx-border-color: " + hex(accent.deriveColor(0, 0.55, 1, 0.45)) + ";" +
                        "-fx-border-width: 0 0 1 0;"
                );
            }
            for (Node header : table.lookupAll(".column-header")) {
                header.setStyle(
                        "-fx-background-color: " + hex(SURFACE_2) + ";" +
                        "-fx-border-color: transparent;"
                );
            }
            for (Node labelNode : table.lookupAll(".column-header .label")) {
                if (labelNode instanceof Label label) {
                    label.setTextFill(TEXT);
                    label.setFont(Font.font(FONT_BOLD, 12.5));
                }
            }
            for (Node cell : table.lookupAll(".table-cell")) {
                cell.setStyle(
                        "-fx-text-fill: " + hex(TEXT_2) + ";" +
                        "-fx-background-color: transparent;"
                );
            }
        });
    }

    private static Label makePlaceholder(String id) {
        String text = switch (id == null ? "" : id) {
            case "taskTable" -> "✦  No tasks yet  •  add your first next action";
            case "goalTable" -> "✦  No goals yet  •  define what matters next";
            case "projectTable" -> "✦  No projects yet  •  turn a goal into progress";
            case "habitTable" -> "✦  No habits yet  •  build a small daily rhythm";
            case "expenseTable" -> "✦  No expenses recorded yet";
            case "incomeTable" -> "✦  No income recorded yet";
            default -> "✦  Nothing here yet";
        };
        Label label = new Label(text);
        label.setTextFill(MUTED);
        label.setFont(Font.font(FONT_BOLD, 13));
        label.setWrapText(true);
        label.setAlignment(javafx.geometry.Pos.CENTER);
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

    private static void styleHeroCard(Node node, Color accent) {
        if (!(node instanceof Region card)) return;
        card.setBackground(new Background(new BackgroundFill(
                heroCardGradient(accent), new CornerRadii(20), Insets.EMPTY)));
        card.setBorder(border(accent.deriveColor(0, 0.85, 1.05, 0.9), 1.4));
        card.setEffect(glow(accent, 13));
    }

    private static DropShadow softShadow(Color accent) {
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.color(
                accent.getRed(), accent.getGreen(), accent.getBlue(), 0.16));
        shadow.setRadius(16);
        shadow.setOffsetY(5);
        return shadow;
    }

    private static LinearGradient cardGradient(Color accent) {
        return new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#171027")),
                new Stop(0.58, SURFACE),
                new Stop(1, Color.color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0.075))
        );
    }

    private static LinearGradient heroCardGradient(Color accent) {
        return new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#21113A")),
                new Stop(0.48, Color.web("#171026")),
                new Stop(1, Color.color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0.20))
        );
    }

    private static LinearGradient headerGradient(Color accent) {
        return new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0D0818")),
                new Stop(0.68, Color.web("#120B1F")),
                new Stop(1, Color.color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0.10))
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
            case "Sea Green" -> SEA_GREEN;
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

    @SuppressWarnings("unchecked")
    private static void styleDashboardList(Node node) {
        if (!(node instanceof ListView<?> rawList)) return;
        ListView<String> list = (ListView<String>) rawList;
        list.setBackground(background(SURFACE, 16));
        list.setBorder(border(BORDER_SOFT, 1));
        list.setPrefHeight(105);
        list.setMinHeight(90);
        list.setMaxHeight(125);
        list.setStyle(
                "-fx-background-color: " + hex(SURFACE) + ";" +
                "-fx-control-inner-background: " + hex(SURFACE) + ";" +
                "-fx-selection-bar: " + hex(DEEP_PLUM) + ";" +
                "-fx-selection-bar-non-focused: " + hex(SURFACE_3) + ";" +
                "-fx-focus-color: transparent;" +
                "-fx-faint-focus-color: transparent;"
        );

        // Notification rows use ListCell skins; explicitly style their text
        // so JavaFX's default blue/black text cannot override the dark theme.
        list.setCellFactory(view -> new ListCell<String>() {
            {
                setFont(Font.font(FONT, 13));
                setTextFill(TEXT_2);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
                setTextFill(empty ? MUTED : TEXT_2);
                setBackground(empty
                        ? Background.EMPTY
                        : background(SURFACE, 0));
                setPadding(new Insets(10, 12, 10, 12));
            }
        });
    }

    private static void styleHeroMetric(Node node, Color accent) {
        if (!(node instanceof Region card)) return;
        card.setMinHeight(92);
        card.setPadding(new Insets(16));
        card.setEffect(softShadow(accent));
    }

    private static Color moduleAccentFor(String label) {
        if (label == null) return SETTINGS_ACCENT;
        String name = label.replaceAll("[^A-Za-z]", "").toLowerCase();
        return switch (name) {
            case "tasks" -> TASKS_ACCENT;
            case "goals" -> GOALS_ACCENT;
            case "projects" -> PROJECTS_ACCENT;
            case "calendar" -> CALENDAR_ACCENT;
            case "habits" -> HABITS_ACCENT;
            case "finance" -> FINANCE_ACCENT;
            case "focus" -> FOCUS_ACCENT;
            case "analytics" -> ANALYTICS_ACCENT;
            case "settings" -> SETTINGS_ACCENT;
            default -> ROYAL_PURPLE;
        };
    }

    private static Node findByType(Node root, Class<?> type) {
        if (type.isInstance(root)) return root;
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                Node found = findByType(child, type);
                if (found != null) return found;
            }
        }
        return null;
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
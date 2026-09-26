package com.momenta.utility;

import com.momenta.service.SettingsService;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.Chart;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Central JavaFX-only visual identity for MOMENTA.
 * Phase 23 adds persistent user personalization without introducing CSS.
 */
public final class MomentaTheme {

    public static final Color SEA_GREEN = Color.web("#287F78");
    public static final Color MINT = Color.web("#B8F2E6");
    public static final Color SKY_BACKGROUND = Color.web("#F4FAFC");
    public static final Color SKY_BLUE = Color.web("#5DADE2");
    public static final Color LAVENDER = Color.web("#9B8AFB");
    public static final Color SOFT_GREEN = Color.web("#6BCB9A");
    public static final Color PEACH = Color.web("#F4A261");
    public static final Color PINK = Color.web("#E78AC3");
    public static final Color PERIWINKLE = Color.web("#8E9FE6");
    public static final Color CHARCOAL = Color.web("#263238");
    public static final Color MUTED = Color.web("#78909C");
    public static final Color WHITE = Color.WHITE;

    private static final Color DARK_BACKGROUND = Color.web("#182126");
    private static final Color DARK_SURFACE = Color.web("#222D33");
    private static final Color DARK_TEXT = Color.web("#E8F1F3");
    private static final Color DARK_MUTED = Color.web("#AABBC1");

    private MomentaTheme() {}

    /** Applies the current user's palette, typography and spacing to a complete view. */
    public static void apply(Parent root, String viewName) {
        Color accent = accentFor(viewName);
        boolean dark = isDark();
        Color backgroundColor = dark ? DARK_BACKGROUND : SKY_BACKGROUND;

        if (root instanceof Region region) {
            region.setBackground(background(backgroundColor, 0));
            region.setPadding(new Insets(SettingsService.isCompactMode() ? 0 : 0));
        }

        styleTree(root, accent, dark);

        if ("Dashboard".equals(viewName)) {
            styleDashboard(root, dark);
        }
    }

    public static Color accentColor() {
        return accentFromName(SettingsService.getAccent());
    }

    public static Color accentLightColor() {
        return accentColor().deriveColor(0, 0.35, 1.15, 1);
    }

    public static Color textColor() {
        return isDark() ? DARK_TEXT : CHARCOAL;
    }

    public static Color mutedColor() {
        return isDark() ? DARK_MUTED : MUTED;
    }

    public static boolean isDark() {
        return "Dark".equalsIgnoreCase(SettingsService.getTheme());
    }

    public static Color accentFor(String viewName) {
        // User-selected accent is the primary personalization setting.
        Color personalized = accentColor();
        if (!"Sea Green".equals(SettingsService.getAccent())) {
            return personalized;
        }
        return switch (viewName) {
            case "Tasks", "Calendar" -> SKY_BLUE;
            case "Goals" -> LAVENDER;
            case "Projects" -> MINT;
            case "Habits" -> SOFT_GREEN;
            case "Finance" -> PEACH;
            case "Focus" -> PINK;
            case "Analytics" -> PERIWINKLE;
            case "Login", "Register", "Settings" -> SEA_GREEN;
            default -> SEA_GREEN;
        };
    }

    private static void styleTree(Node node, Color accent, boolean dark) {
        if (node instanceof Label label) {
            label.setTextFill(dark ? DARK_TEXT : CHARCOAL);
            if (label.getFont().getSize() >= 22) {
                label.setTextFill(accent);
            }
            scaleFont(label);
        }

        if (node instanceof Button button) {
            styleButton(button, accent, dark);
        } else if (node instanceof TextInputControl input) {
            styleInput(input, accent, dark);
        } else if (node instanceof ComboBox<?> combo) {
            combo.setStyle("");
            combo.setBackground(background(dark ? DARK_SURFACE : WHITE, 8));
            combo.setBorder(border(accent, 1));
            scaleFont(combo);
        } else if (node instanceof Control control) {
            scaleFont(control);
        }

        if (node instanceof TableView<?> table) {
            styleTable(table, accent, dark);
        } else if (node instanceof ListView<?> list) {
            styleList(list, accent, dark);
        } else if (node instanceof ScrollPane scroll) {
            scroll.setFitToWidth(true);
            scroll.setPannable(true);
            scroll.setBackground(background(dark ? DARK_BACKGROUND : WHITE, 0));
            scroll.setBorder(border(accent, 1));
        } else if (node instanceof Chart chart) {
            chart.setBackground(background(dark ? DARK_SURFACE : WHITE, 8));
            chart.setBorder(border(accent, 1));
        }

        if (node instanceof VBox box) {
            box.setFillWidth(true);
            if (SettingsService.isCompactMode() && box.getSpacing() > 10) {
                box.setSpacing(Math.max(6, box.getSpacing() - 5));
            }
        }

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                styleTree(child, accent, dark);
            }
        }
    }

    private static void styleDashboard(Node root, boolean dark) {
        Node sidebarNode = findById(root, "sidebar");
        if (sidebarNode instanceof VBox sidebar) {
            sidebar.setBackground(background(SEA_GREEN, 0));
            sidebar.setPadding(new Insets(22, 14, 22, 14));
            sidebar.setSpacing(SettingsService.isCompactMode() ? 4 : 8);

            for (Node child : sidebar.getChildren()) {
                if (child instanceof Button button) {
                    button.setBackground(background(Color.TRANSPARENT, 10));
                    button.setBorder(Border.EMPTY);
                    button.setTextFill(WHITE);
                    button.setPadding(new Insets(10, 12, 10, 12));
                    button.setOnMouseEntered(e -> button.setBackground(
                            background(MINT, 10)));
                    button.setOnMouseExited(e -> button.setBackground(
                            background(Color.TRANSPARENT, 10)));
                }
            }
        }

        styleCard(findById(root, "pulseCard"), LAVENDER, dark);
        styleCard(findById(root, "taskCard"), SKY_BLUE, dark);
        styleCard(findById(root, "goalCard"), SOFT_GREEN, dark);
        styleCard(findById(root, "projectCard"), MINT, dark);
        styleCard(findById(root, "nowCard"), SEA_GREEN, dark);
        styleCard(findById(root, "insightCard"), PERIWINKLE, dark);
        styleCard(findById(root, "gamificationCard"), LAVENDER, dark);
    }

    private static void styleCard(Node node, Color accent, boolean dark) {
        if (!(node instanceof Region card)) return;
        card.setBackground(background(dark ? DARK_SURFACE : WHITE, 10));
        card.setBorder(border(accent, 2));
        card.setPadding(new Insets(SettingsService.isCompactMode() ? 10 : 14));
    }

    private static void styleButton(Button button, Color accent, boolean dark) {
        button.setTextFill(dark ? DARK_TEXT : CHARCOAL);
        button.setFont(Font.font(button.getFont().getFamily(), scaled(button.getFont().getSize())));
        button.setPadding(new Insets(SettingsService.isCompactMode() ? 6 : 8,
                SettingsService.isCompactMode() ? 10 : 14,
                SettingsService.isCompactMode() ? 6 : 8,
                SettingsService.isCompactMode() ? 10 : 14));
        button.setBackground(background(dark ? DARK_SURFACE : WHITE, 8));
        button.setBorder(border(accent, 1));
        button.setOnMouseEntered(e -> button.setBackground(
                background(accent.deriveColor(0, 0.28, 1, 1), 8)));
        button.setOnMouseExited(e -> button.setBackground(background(dark ? DARK_SURFACE : WHITE, 8)));
    }

    private static void styleInput(TextInputControl input, Color accent, boolean dark) {
        input.setBackground(background(dark ? DARK_SURFACE : WHITE, 8));
        input.setBorder(border(accent, 1));
        input.setPadding(new Insets(9, 10, 9, 10));

        String textColor = toHex(dark ? DARK_TEXT : CHARCOAL);
        String promptColor = toHex(mutedColor());

        input.setStyle(
                "-fx-text-fill: " + textColor + ";" +
                        "-fx-prompt-text-fill: " + promptColor + ";"
        );
    }

    private static void styleTable(TableView<?> table, Color accent, boolean dark) {
        table.setMaxWidth(Double.MAX_VALUE);
        table.setMaxHeight(Double.MAX_VALUE);
        table.setBackground(background(dark ? DARK_SURFACE : WHITE, 8));
        table.setBorder(border(accent, 1));
        table.setPlaceholder(new Label("No data available."));
    }

    private static void styleList(ListView<?> list, Color accent, boolean dark) {
        list.setMaxWidth(Double.MAX_VALUE);
        list.setBackground(background(dark ? DARK_SURFACE : WHITE, 8));
        list.setBorder(border(accent, 1));
    }

    private static void scaleFont(Control control) {
        double size = 13 * SettingsService.getFontScale();

        String oldStyle = control.getStyle();

        if (oldStyle == null) {
            oldStyle = "";
        }

        control.setStyle(
                oldStyle +
                        (oldStyle.endsWith(";") || oldStyle.isEmpty() ? "" : ";") +
                        "-fx-font-size: " + size + "px;"
        );
    }
    private static double scaled(double size) {
        return size * SettingsService.getFontScale();
    }

    private static Color accentFromName(String name) {
        return switch (name == null ? "Sea Green" : name) {
            case "Sky Blue" -> SKY_BLUE;
            case "Lavender" -> LAVENDER;
            case "Soft Green" -> SOFT_GREEN;
            case "Peach" -> PEACH;
            case "Pink" -> PINK;
            case "Periwinkle" -> PERIWINKLE;
            default -> SEA_GREEN;
        };
    }

    private static Background background(Color color, double radius) {
        return new Background(new BackgroundFill(color, new CornerRadii(radius), Insets.EMPTY));
    }

    private static Border border(Color color, double width) {
        return new Border(new BorderStroke(
                color,
                BorderStrokeStyle.SOLID,
                new CornerRadii(10),
                new BorderWidths(width)));
    }

    private static String toHex(Color color) {
        return String.format(
                "#%02X%02X%02X",
                (int) Math.round(color.getRed() * 255),
                (int) Math.round(color.getGreen() * 255),
                (int) Math.round(color.getBlue() * 255)
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

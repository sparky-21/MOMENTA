package com.momenta.utility;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.Chart;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Central JavaFX-only visual identity for MOMENTA.
 *
 * Phase 19 deliberately avoids CSS. Every visual rule is applied through
 * JavaFX properties such as Background, Border, Paint, Font and Insets.
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

    private MomentaTheme() {
    }

    /** Applies the MOMENTA palette to a complete FXML view. */
    public static void apply(Parent root, String viewName) {
        Color accent = accentFor(viewName);

        if (root instanceof Region region) {
            region.setBackground(background(SKY_BACKGROUND));
            region.setPadding(new Insets(0));
        }

        styleTree(root, accent);

        if ("Dashboard".equals(viewName)) {
            styleDashboard(root);
        }
    }

    public static Color accentFor(String viewName) {
        return switch (viewName) {
            case "Tasks" -> SKY_BLUE;
            case "Goals" -> LAVENDER;
            case "Projects" -> MINT;
            case "Calendar" -> SKY_BLUE;
            case "Habits" -> SOFT_GREEN;
            case "Finance" -> PEACH;
            case "Focus" -> PINK;
            case "Analytics" -> PERIWINKLE;
            case "Login", "Register" -> SEA_GREEN;
            default -> SEA_GREEN;
        };
    }

    private static void styleTree(Node node, Color accent) {
        if (node instanceof Label label) {
            label.setTextFill(CHARCOAL);
            if (label.getFont().getSize() >= 22) {
                label.setTextFill(accent);
            }
        }

        if (node instanceof Button button) {
            styleButton(button, accent);
        } else if (node instanceof TextInputControl input) {
            styleInput(input, accent);
        } else if (node instanceof TableView<?> table) {
            styleTable(table, accent);
        } else if (node instanceof ListView<?> list) {
            styleList(list, accent);
        } else if (node instanceof ScrollPane scroll) {
            scroll.setFitToWidth(true);
            scroll.setPannable(true);
            scroll.setBackground(background(WHITE));
            scroll.setBorder(border(accent, 1));
        } else if (node instanceof Chart chart) {
            chart.setBackground(background(WHITE));
            chart.setBorder(border(accent, 1));
        }

        if (node instanceof VBox box) {
            box.setFillWidth(true);
        }

        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                styleTree(child, accent);
            }
        }
    }

    private static void styleDashboard(Node root) {
        Node sidebarNode = findById(root, "sidebar");
        if (sidebarNode instanceof VBox sidebar) {
            sidebar.setBackground(background(SEA_GREEN));
            sidebar.setPadding(new Insets(22, 14, 22, 14));
            sidebar.setSpacing(8);

            for (Node child : sidebar.getChildren()) {
                if (child instanceof Button button) {
                    button.setBackground(new Background(new BackgroundFill(
                            Color.TRANSPARENT, new CornerRadii(10), Insets.EMPTY)));
                    button.setBorder(Border.EMPTY);
                    button.setTextFill(WHITE);
                    button.setPadding(new Insets(10, 12, 10, 12));
                    button.setOnMouseEntered(e -> button.setBackground(
                            new Background(new BackgroundFill(MINT, new CornerRadii(10), Insets.EMPTY))));
                    button.setOnMouseExited(e -> button.setBackground(
                            new Background(new BackgroundFill(Color.TRANSPARENT, new CornerRadii(10), Insets.EMPTY))));
                }
            }
        }

        styleCard(findById(root, "pulseCard"), LAVENDER);
        styleCard(findById(root, "taskCard"), SKY_BLUE);
        styleCard(findById(root, "goalCard"), SOFT_GREEN);
        styleCard(findById(root, "projectCard"), MINT);
        styleCard(findById(root, "nowCard"), SEA_GREEN);
        styleCard(findById(root, "insightCard"), PERIWINKLE);
    }

    private static void styleCard(Node node, Color accent) {
        if (!(node instanceof Region card)) return;
        card.setBackground(background(WHITE));
        card.setBorder(border(accent, 2));
        card.setPadding(new Insets(14));
    }

    private static void styleButton(Button button, Color accent) {
        button.setTextFill(CHARCOAL);
        button.setFont(Font.font(button.getFont().getFamily(), 13));
        button.setPadding(new Insets(8, 14, 8, 14));
        button.setBackground(background(WHITE));
        button.setBorder(border(accent, 1));
        button.setOnMouseEntered(e -> button.setBackground(
                new Background(new BackgroundFill(accent.deriveColor(0, 0.28, 1, 1),
                        new CornerRadii(8), Insets.EMPTY))));
        button.setOnMouseExited(e -> button.setBackground(background(WHITE)));
    }

    private static void styleInput(TextInputControl input, Color accent) {
        input.setBackground(background(WHITE));
        input.setBorder(border(accent, 1));
        input.setPadding(new Insets(9, 10, 9, 10));
    }

    private static void styleTable(TableView<?> table, Color accent) {
        table.setMaxWidth(Double.MAX_VALUE);
        table.setMaxHeight(Double.MAX_VALUE);
        table.setBackground(background(WHITE));
        table.setBorder(border(accent, 1));
        table.setPlaceholder(new Label("No data available."));
    }

    private static void styleList(ListView<?> list, Color accent) {
        list.setMaxWidth(Double.MAX_VALUE);
        list.setBackground(background(WHITE));
        list.setBorder(border(accent, 1));
    }

    private static Background background(Color color) {
        return new Background(new BackgroundFill(color, new CornerRadii(10), Insets.EMPTY));
    }

    private static Border border(Color color, double width) {
        return new Border(new BorderStroke(
                color,
                BorderStrokeStyle.SOLID,
                new CornerRadii(10),
                new BorderWidths(width)
        ));
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

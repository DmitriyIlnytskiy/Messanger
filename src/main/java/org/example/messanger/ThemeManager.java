package org.example.messanger;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ThemeManager {

    public enum Theme {
        LIGHT, DARK
    }

    private static Theme currentTheme = Theme.LIGHT;

    public static void applyTheme(Scene scene, Theme theme) {
        currentTheme = theme;

        Color background = (theme == Theme.DARK) ? Color.web("#1e1e1e") : Color.web("#ffffff");
        Color foreground = (theme == Theme.DARK) ? Color.web("#f5f5f5") : Color.web("#000000");

        setNodeTheme(scene.getRoot(), background, foreground);
    }

    private static void setNodeTheme(Parent node, Color background, Color foreground) {
        if (node instanceof Region region) {
            region.setBackground(new Background(new BackgroundFill(background, null, null)));
        }

        // Loop through all child nodes and apply theme
        node.lookupAll("*").forEach(child -> {
            if (child instanceof Region region) {
                // Apply background to Regions (e.g., StackPane, HBox, VBox)
                region.setBackground(new Background(new BackgroundFill(background, null, null)));
            }

            if (child instanceof Labeled labeled) {
                // Apply text color for Labeled (e.g., Label, Button)
                labeled.setTextFill(foreground);
            } else if (child instanceof TextInputControl input) {
                // Apply text color and background for input fields (e.g., TextField, TextArea)
                input.setStyle("-fx-text-fill: " + toRgbString(foreground) +
                        "; -fx-control-inner-background: " + toRgbString(background));
            } else if (child instanceof Button button) {
                // Apply background color to buttons with a solid black border
                button.setStyle("-fx-background-color: " + toRgbString(background) + "; " +
                        "-fx-text-fill: " + toRgbString(foreground) + "; " +
                        "-fx-border-color: black; -fx-border-width: 2px; " +
                        "-fx-border-radius: 5; -fx-background-radius: 5;");
            }
        });
    }

    private static String toRgbString(Color color) {
        return String.format("rgb(%d,%d,%d)",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    public static Theme getCurrentTheme() {
        return currentTheme;
    }
}

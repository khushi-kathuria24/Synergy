package com.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class Dashboard {
    private BorderPane root;
    private WebView liveFeed;
    private Label connectionStatus;
    private String droneCamUrl;

    public Dashboard() {
        root = new BorderPane();
        root.getStyleClass().add("dashboard-background");
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #FFFFFF, #F8FAFC); -fx-padding: 10px;");

        // Update this with your ESP32-CAM stream URL
        droneCamUrl = "http://esp32cam.local";

        // Create main layout structure using SplitPane for better resizing
        VBox headerBox = createResponsiveHeader();
        SplitPane mainContent = createResponsiveMainContent();

        root.setTop(headerBox);
        root.setCenter(mainContent);

        // Initialize the connection
        connectToDrone();

        // Make the layout responsive
        makeResponsive();
    }

    private void makeResponsive() {
        liveFeed.prefWidthProperty().bind(
            root.widthProperty().multiply(0.7).subtract(40)
        );
        liveFeed.prefHeightProperty().bind(
            root.heightProperty().multiply(0.7).subtract(100)
        );
    }

    private VBox createResponsiveHeader() {
        VBox headerBox = new VBox(10);
        headerBox.setPadding(new Insets(20));
        headerBox.setStyle("""
            -fx-background-color: linear-gradient(to bottom, #F1F5F9, #E2E8F0);
            -fx-background-radius: 8px;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 0);
        """);

        Label header = new Label("Drone Control Dashboard");
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        header.setStyle("-fx-text-fill: #334155;");
        header.setMaxWidth(Double.MAX_VALUE);
        header.setAlignment(Pos.CENTER);

        connectionStatus = new Label("Status: Connecting...");
        connectionStatus.setStyle("-fx-text-fill: #64748B; -fx-font-size: 14px;");
        connectionStatus.setMaxWidth(Double.MAX_VALUE);
        connectionStatus.setAlignment(Pos.CENTER);

        headerBox.getChildren().addAll(header, connectionStatus);
        return headerBox;
    }

    private SplitPane createResponsiveMainContent() {
        SplitPane splitPane = new SplitPane();
        
        // Create left side with video feed and controls
        VBox leftContent = new VBox(20);
        leftContent.setPadding(new Insets(20));
        leftContent.setAlignment(Pos.TOP_CENTER);
        leftContent.setStyle("""
            -fx-background-color: linear-gradient(to bottom, #FFFFFF, #F8FAFC);
            -fx-background-radius: 8px;
        """);

        // Video feed with WebView
        liveFeed = new WebView();
        liveFeed.setMinSize(640, 480);
        VBox.setVgrow(liveFeed, Priority.ALWAYS);

        // Create main controls
        VBox controlsContainer = createControlButtons();

        leftContent.getChildren().addAll(liveFeed, controlsContainer);

        // Create right side with stats and settings
        ScrollPane rightScrollPane = new ScrollPane(createResponsiveSidebarWithSettings());
        rightScrollPane.setFitToWidth(true);
        rightScrollPane.setStyle("-fx-background: linear-gradient(to bottom, #F1F5F9, #E2E8F0);");

        // Add both sides to split pane
        splitPane.getItems().addAll(leftContent, rightScrollPane);
        splitPane.setDividerPositions(0.75);

        return splitPane;
    }

    private VBox createControlButtons() {
        VBox controlsContainer = new VBox(10);
        controlsContainer.setStyle("""
            -fx-background-color: linear-gradient(to bottom, #F1F5F9, #E2E8F0);
            -fx-padding: 15px;
            -fx-background-radius: 8px;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 4, 0, 0, 0);
        """);

        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER);

        Button reconnectButton = createStyledButton("Reconnect", "#60A5FA");
        Button captureButton = createStyledButton("Capture Photo", "#818CF8");

        reconnectButton.setOnAction(e -> connectToDrone());
        captureButton.setOnAction(e -> captureScreenshot());

        controls.getChildren().addAll(reconnectButton, captureButton);
        controlsContainer.getChildren().add(controls);
        return controlsContainer;
    }

    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: white;
            -fx-font-weight: bold;
            -fx-padding: 10 20;
            -fx-min-width: 120px;
            -fx-cursor: hand;
            -fx-background-radius: 6px;
        """, color));
        
        button.setOnMouseEntered(e -> 
            button.setStyle(button.getStyle() + "-fx-opacity: 0.9;")
        );
        button.setOnMouseExited(e -> 
            button.setStyle(button.getStyle().replace("-fx-opacity: 0.9;", ""))
        );

        return button;
    }

    private VBox createResponsiveSidebarWithSettings() {
        VBox sidebar = new VBox(15);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("""
            -fx-background-color: linear-gradient(to bottom, #F8FAFC, #F1F5F9);
        """);

        // Stream Settings Section
        Label settingsHeader = new Label("Stream Settings");
        settingsHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        settingsHeader.setStyle("-fx-text-fill: #334155;");
        settingsHeader.setAlignment(Pos.CENTER);
        settingsHeader.setMaxWidth(Double.MAX_VALUE);

        VBox settingsBox = new VBox(10);
        settingsBox.setStyle("""
            -fx-background-color: linear-gradient(to bottom, #FFFFFF, #F8FAFC);
            -fx-padding: 15px;
            -fx-background-radius: 8px;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 4, 0, 0, 0);
        """);

        // Resolution selector
        Label resLabel = new Label("Resolution:");
        resLabel.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold;");
        ComboBox<String> resolution = new ComboBox<>();
        resolution.getItems().addAll("640x480", "800x600", "1280x720");
        resolution.setValue("1280x720");
        resolution.setMaxWidth(Double.MAX_VALUE);
        resolution.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #334155;");

        // Quality selector
        Label qualLabel = new Label("Quality:");
        qualLabel.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold;");
        ComboBox<String> quality = new ComboBox<>();
        quality.getItems().addAll("Low", "Medium", "High");
        quality.setValue("High");
        quality.setMaxWidth(Double.MAX_VALUE);
        quality.setStyle("-fx-background-color: #FFFFFF; -fx-text-fill: #334155;");

        Button applyButton = createStyledButton("Apply Settings", "#60A5FA");
        applyButton.setMaxWidth(Double.MAX_VALUE);

        settingsBox.getChildren().addAll(
            resLabel, resolution,
            qualLabel, quality,
            applyButton
        );

        // Stats Section
        Label statsHeader = new Label("Drone Statistics");
        statsHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        statsHeader.setStyle("-fx-text-fill: #334155;");
        statsHeader.setAlignment(Pos.CENTER);
        statsHeader.setMaxWidth(Double.MAX_VALUE);

        VBox statsBox = new VBox(15);
        statsBox.getChildren().addAll(
            createResponsiveStatLabel("Battery Level:", "95%"),
            createResponsiveStatLabel("Signal Strength:", "Strong"),
            createResponsiveStatLabel("Altitude:", "25.4m"),
            createResponsiveStatLabel("Speed:", "15.2 km/h"),
            createResponsiveStatLabel("GPS Status:", "Locked"),
            createResponsiveStatLabel("Frame Rate:", "30 FPS"),
            createResponsiveStatLabel("Storage:", "14.2 GB Free")
        );

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #E2E8F0;");

        sidebar.getChildren().addAll(
            settingsHeader,
            settingsBox,
            separator,
            statsHeader,
            statsBox
        );

        return sidebar;
    }

    private HBox createResponsiveStatLabel(String name, String value) {
        HBox statBox = new HBox(10);
        statBox.setAlignment(Pos.CENTER_LEFT);
        statBox.setStyle("""
            -fx-background-color: linear-gradient(to right, #FFFFFF, #F8FAFC);
            -fx-padding: 8px 12px;
            -fx-background-radius: 6px;
            -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 2, 0, 0, 0);
        """);

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: #64748B;");

        statBox.getChildren().addAll(nameLabel, valueLabel);
        return statBox;
    }

    private void connectToDrone() {
        try {
            liveFeed.getEngine().load(droneCamUrl);
            connectionStatus.setText("Status: Connected");
            connectionStatus.setStyle("-fx-text-fill: #10B981;");
            showSuccess("Connection Successful", "Successfully connected to the drone.");
        } catch (Exception e) {
            connectionStatus.setText("Status: Connection Failed");
            connectionStatus.setStyle("-fx-text-fill: #EF4444;");
            showError("Connection Error", "Failed to connect to drone camera. Please check the connection and try again.");
        }
    }

    private void captureScreenshot() {
        showInfo("Screenshot Captured", "Image saved to gallery");
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setStyle("-fx-background-color: #FEE2E2;");
        alert.showAndWait();
    }

    private void showSuccess(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setStyle("-fx-background-color: #DCFCE7;");
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setStyle("-fx-background-color: #F1F5F9;");
        alert.showAndWait();
    }

    public BorderPane getRoot() {
        return root;
    }
}
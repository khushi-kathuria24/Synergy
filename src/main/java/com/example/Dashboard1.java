// package com.example;

// import javafx.geometry.Insets;
// import javafx.geometry.Pos;
// import javafx.scene.control.*;
// import javafx.scene.layout.*;
// import javafx.scene.web.WebView;
// import javafx.scene.text.Font;
// import javafx.scene.text.FontWeight;

// public class Dashboard1 {
//     private BorderPane root;
//     private WebView liveFeed;
//     private Label connectionStatus;
//     private Button reconnectButton;
//     private String droneCamUrl;

//     public Dashboard1() {
//         root = new BorderPane();
//         root.getStyleClass().add("dashboard-background");
        
//         // Make the root layout responsive
//         root.setStyle("-fx-padding: 10px;");
        
//         // Update this with your actual ESP32-CAM stream URL
//         droneCamUrl = "http://esp32cam.local";

//         // Create responsive header
//         VBox headerBox = createResponsiveHeader();
//         root.setTop(headerBox);

//         // Create responsive main content
//         VBox mainContent = createResponsiveMainContent();
//         root.setCenter(mainContent);

//         // Create responsive sidebar
//         VBox sidebar = createResponsiveSidebar();
//         root.setRight(sidebar);

//         // Initialize the connection
//         connectToDrone();
//     }

//     private VBox createResponsiveHeader() {
//         VBox headerBox = new VBox(10);
//         headerBox.setPadding(new Insets(20));
//         headerBox.setStyle("-fx-background-color: #2C3E50;");

//         Label header = new Label("Drone Live Feed");
//         header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
//         header.setStyle("-fx-text-fill: white;");
//         header.setMaxWidth(Double.MAX_VALUE);
//         header.setAlignment(Pos.CENTER);

//         connectionStatus = new Label("Status: Connecting...");
//         connectionStatus.setStyle("-fx-text-fill: #ECF0F1;");
//         connectionStatus.setMaxWidth(Double.MAX_VALUE);
//         connectionStatus.setAlignment(Pos.CENTER);

//         headerBox.getChildren().addAll(header, connectionStatus);
//         return headerBox;
//     }

//     private VBox createResponsiveMainContent() {
//         // Use AnchorPane for more flexible layout
//         AnchorPane mainContent = new AnchorPane();
        
//         // Live Feed Container
//         VBox feedContainer = new VBox(20);
//         feedContainer.getStyleClass().add("feed-container");
//         feedContainer.setPadding(new Insets(10));
//         feedContainer.setAlignment(Pos.CENTER);

//         // Make WebView responsive
//         liveFeed = new WebView();
//         liveFeed.setMinSize(300, 200); // Minimum size
//         liveFeed.setPrefSize(800, 600);
//         HBox.setHgrow(liveFeed, Priority.ALWAYS);
//         VBox.setVgrow(liveFeed, Priority.ALWAYS);
        
//         // Responsive Controls
//         HBox controls = new HBox(20);
//         controls.setAlignment(Pos.CENTER);
//         controls.setPadding(new Insets(10));
        
//         String buttonStyle = "-fx-font-size: 14px; -fx-padding: 10px 20px; -fx-min-width: 120px;";
        
//         reconnectButton = new Button("Reconnect");
//         reconnectButton.getStyleClass().add("action-button");
//         reconnectButton.setStyle(buttonStyle);
//         reconnectButton.setOnAction(e -> connectToDrone());
//         HBox.setHgrow(reconnectButton, Priority.ALWAYS);
        
//         Button captureButton = new Button("Capture");
//         captureButton.getStyleClass().add("action-button");
//         captureButton.setStyle(buttonStyle);
//         captureButton.setOnAction(e -> captureScreenshot());
//         HBox.setHgrow(captureButton, Priority.ALWAYS);

//         controls.getChildren().addAll(reconnectButton, captureButton);
        
//         // Wrap controls in a container
//         VBox controlsWrapper = new VBox(controls);
//         controlsWrapper.setPadding(new Insets(10));
//         controlsWrapper.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 5px;");
        
//         feedContainer.getChildren().addAll(liveFeed, controlsWrapper);
        
//         // Set anchors to make layout responsive
//         AnchorPane.setTopAnchor(feedContainer, 0.0);
//         AnchorPane.setBottomAnchor(feedContainer, 0.0);
//         AnchorPane.setLeftAnchor(feedContainer, 0.0);
//         AnchorPane.setRightAnchor(feedContainer, 0.0);
        
//         mainContent.getChildren().add(feedContainer);
        
//         // Convert to VBox to maintain existing method signature
//         VBox responsiveContent = new VBox(mainContent);
//         responsiveContent.setFillWidth(true);
        
//         return responsiveContent;
//     }

//     private VBox createResponsiveSidebar() {
//         // Make sidebar collapsible and responsive
//         VBox sidebar = new VBox(15);
//         sidebar.setPadding(new Insets(20));
//         sidebar.setMinWidth(200);
//         sidebar.setMaxWidth(300);
//         HBox.setHgrow(sidebar, Priority.SOMETIMES);
//         sidebar.getStyleClass().add("sidebar");

//         Label statsHeader = new Label("Drone Statistics");
//         statsHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
//         statsHeader.setMaxWidth(Double.MAX_VALUE);
//         statsHeader.setAlignment(Pos.CENTER);

//         VBox statsBox = new VBox(10);
//         statsBox.getChildren().addAll(
//             createResponsiveStatLabel("Battery Level:", "95%"),
//             createResponsiveStatLabel("Signal Strength:", "Strong"),
//             createResponsiveStatLabel("Resolution:", "1280x720"),
//             createResponsiveStatLabel("Frame Rate:", "30 FPS")
//         );

//         TitledPane settingsPane = new TitledPane("Stream Settings", createResponsiveSettingsContent());
//         settingsPane.setExpanded(false);
//         settingsPane.setMaxWidth(Double.MAX_VALUE);

//         sidebar.getChildren().addAll(statsHeader, statsBox, settingsPane);
//         return sidebar;
//     }

//     private HBox createResponsiveStatLabel(String name, String value) {
//         HBox statBox = new HBox(10);
//         statBox.setAlignment(Pos.CENTER_LEFT);
        
//         Label nameLabel = new Label(name);
//         nameLabel.setStyle("-fx-font-weight: bold;");
        
//         Label valueLabel = new Label(value);
//         valueLabel.getStyleClass().add("stat-value");
        
//         statBox.getChildren().addAll(nameLabel, valueLabel);
//         HBox.setHgrow(nameLabel, Priority.ALWAYS);
//         HBox.setHgrow(valueLabel, Priority.ALWAYS);
        
//         return statBox;
//     }

//     private VBox createResponsiveSettingsContent() {
//         VBox settings = new VBox(10);
//         settings.setPadding(new Insets(10));

//         ComboBox<String> resolution = new ComboBox<>();
//         resolution.getItems().addAll("640x480", "800x600", "1280x720");
//         resolution.setValue("1280x720");
//         resolution.setMaxWidth(Double.MAX_VALUE);

//         ComboBox<String> quality = new ComboBox<>();
//         quality.getItems().addAll("Low", "Medium", "High");
//         quality.setValue("High");
//         quality.setMaxWidth(Double.MAX_VALUE);

//         Button applyButton = new Button("Apply Settings");
//         applyButton.getStyleClass().add("settings-button");
//         applyButton.setMaxWidth(Double.MAX_VALUE);

//         settings.getChildren().addAll(
//             new Label("Resolution:"),
//             resolution,
//             new Label("Quality:"),
//             quality,
//             applyButton
//         );

//         return settings;
//     }

//     private void connectToDrone() {
//         // WebView wb =new WebView();
//         try {
//             // Attempt to load the stream URL
//             liveFeed.getEngine().load(droneCamUrl);
//             // wb.getEngine().load(droneCamUrl);
//             connectionStatus.setText("Status: Connected");
//             connectionStatus.setStyle("-fx-text-fill: #2ECC71;");
//         } catch (Exception e) {
//             connectionStatus.setText("Status: Connection Failed");
//             connectionStatus.setStyle("-fx-text-fill: #E74C3C;");
//             showError("Connection Error", "Failed to connect to drone camera. Please check the connection and try again.");
//         }
//     }

//     private void captureScreenshot() {
//         // Placeholder for screenshot functionality
//         showInfo("Screenshot Captured", "Image saved to gallery");
//     }

//     private void showError(String title, String content) {
//         Alert alert = new Alert(Alert.AlertType.ERROR);
//         alert.setTitle(title);
//         alert.setHeaderText(null);
//         alert.setContentText(content);
//         alert.showAndWait();
//     }

//     private void showInfo(String title, String content) {
//         Alert alert = new Alert(Alert.AlertType.INFORMATION);
//         alert.setTitle(title);
//         alert.setHeaderText(null);
//         alert.setContentText(content);
//         alert.showAndWait();
//     }

//     public BorderPane getRoot() {
//         return root;
//     }
// }
package com.example;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Dashboard {
    private BorderPane root;
    private ImageView liveFeedImageView;
    private Label connectionStatus;
    private String droneCamUrl;
    private volatile boolean running;

    public Dashboard() {
        root = new BorderPane();
        root.getStyleClass().add("dashboard-background");
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #FFFFFF, #F8FAFC); -fx-padding: 10px;");

        // Update this with your ESP32-CAM stream URL
        droneCamUrl = "http://192.168.234.193:81/stream"; // Replace with your ESP32-CAM IP

        // Create main layout structure using SplitPane for better resizing
        VBox headerBox = createResponsiveHeader();
        SplitPane mainContent = createResponsiveMainContent();

        root.setTop(headerBox);
        root.setCenter(mainContent);

        // Start the video stream
        running = true;
        startMJPEGStream();

        // Make the layout responsive
        makeResponsive();
    }

    private void makeResponsive() {
        liveFeedImageView.fitWidthProperty().bind(
            root.widthProperty().multiply(0.7).subtract(40)
        );
        liveFeedImageView.fitHeightProperty().bind(
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
    
    private HBox createControlButtons() {
        HBox controlBox = new HBox(10);
        controlBox.setAlignment(Pos.CENTER);
        controlBox.setPadding(new Insets(10));

        // Reconnect Button
        Button reconnectButton = createStyledButton("Reconnect", "#10B981");
        reconnectButton.setOnAction(e -> reconnectToStream());

        // Screenshot Button
        Button screenshotButton = createStyledButton("Screenshot", "#3B82F6");
        screenshotButton.setOnAction(e -> captureScreenshot());

        controlBox.getChildren().addAll(reconnectButton, screenshotButton);
        return controlBox;
    }

    private void reconnectToStream() {
        // Update connection status
        connectionStatus.setText("Status: Attempting to Reconnect...");
        connectionStatus.setStyle("-fx-text-fill: #F59E0B;");

        // Stop current stream
        stopStream();

        // Start a new stream
        running = true;
        startMJPEGStream();
    }

    private void captureScreenshot() {
        try {
            // Get the current image from the live feed
            Image currentFrame = liveFeedImageView.getImage();
            
            if (currentFrame != null) {
                // Generate unique filename with timestamp
                String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String filename = "drone_screenshot_" + timestamp + ".png";
                
                // Use user's home directory for screenshots
                File screenshotDir = new File(System.getProperty("user.home"), "Drone Screenshots");
                
                // Create directory if it doesn't exist
                if (!screenshotDir.exists()) {
                    screenshotDir.mkdirs();
                }
                
                File outputFile = new File(screenshotDir, filename);
    
                // Convert JavaFX Image to BufferedImage
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(currentFrame, null);
    
                // Save the image
                ImageIO.write(bufferedImage, "png", outputFile);
    
                // Show confirmation dialog
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Screenshot Saved");
                alert.setHeaderText(null);
                alert.setContentText("Screenshot saved to: " + outputFile.getAbsolutePath());
                alert.showAndWait();
            } else {
                // Show error if no image is available
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Screenshot Failed");
                alert.setHeaderText(null);
                alert.setContentText("No image available to capture.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            e.printStackTrace();
            
            // Show error dialog
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Screenshot Error");
            alert.setHeaderText(null);
            alert.setContentText("Failed to save screenshot: " + e.getMessage());
            alert.showAndWait();
        }
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

        // Video feed with ImageView
        liveFeedImageView = new ImageView();
        liveFeedImageView.setPreserveRatio(true);
        liveFeedImageView.setFitWidth(640);
        liveFeedImageView.setFitHeight(480);
        VBox.setVgrow(liveFeedImageView, Priority.ALWAYS);

        // Add control buttons below the video feed
        HBox controlButtons = createControlButtons();

        leftContent.getChildren().addAll(liveFeedImageView, controlButtons);

        // Create right side with stats and settings
        ScrollPane rightScrollPane = new ScrollPane(createResponsiveSidebarWithSettings());
        rightScrollPane.setFitToWidth(true);
        rightScrollPane.setStyle("-fx-background: linear-gradient(to bottom, #F1F5F9, #E2E8F0);");

        // Add both sides to split pane
        splitPane.getItems().addAll(leftContent, rightScrollPane);
        splitPane.setDividerPositions(0.75);

        return splitPane;
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

    private void startMJPEGStream() {
        Thread videoThread = new Thread(() -> {
            try {
                URL url = new URL(droneCamUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "JavaFX-MJPEG-Viewer");
                connection.setRequestProperty("Accept", "multipart/x-mixed-replace");
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                while (running) {
                    BufferedImage frame = readMJPEGFrame(inputStream);
                    if (frame != null) {
                        Platform.runLater(() -> liveFeedImageView.setImage(SwingFXUtils.toFXImage(frame, null)));
                    }
                }
                connection.disconnect();
            } catch (Exception e) {
                Platform.runLater(() -> {
                    connectionStatus.setText("Status: Connection Failed");
                    connectionStatus.setStyle("-fx-text-fill: #E74C3C;");
                });
                e.printStackTrace();
            }
        });
        videoThread.setDaemon(true);
        videoThread.start();
    }

    private BufferedImage readMJPEGFrame(InputStream inputStream) throws Exception {
        String boundary = "--"; // Default boundary prefix
        while (true) {
            int ch;
            StringBuilder header = new StringBuilder();
            while ((ch = inputStream.read()) != -1 && header.length() < 10000) {
                header.append((char) ch);
                if (header.toString().contains(boundary)) {
                    break;
                }
            }
            if (header.toString().contains(boundary)) {
                String contentLengthHeader = "Content-Length: ";
                int contentLength = -1;
                while ((ch = inputStream.read()) != -1) {
                    header.append((char) ch);
                    if (header.toString().endsWith("\r\n\r\n")) {
                        String[] lines = header.toString().split("\r\n");
                        for (String line : lines) {
                            if (line.startsWith(contentLengthHeader)) {
                                contentLength = Integer.parseInt(line.substring(contentLengthHeader.length()));
                                break;
                            }
                        }
                        break;
                    }
                }
                if (contentLength > 0) {
                    byte[] imageData = new byte[contentLength];
                    int bytesRead = 0;
                    while (bytesRead < contentLength) {
                        bytesRead += inputStream.read(imageData, bytesRead, contentLength - bytesRead);
                    }
                    return ImageIO.read(new java.io.ByteArrayInputStream(imageData));
                }
            }
        }
    }

    public BorderPane getRoot() {
        return root;
    }

    public void stopStream() {
        running = false;
    }
}

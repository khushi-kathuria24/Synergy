package com.example;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class App extends Application {
    private static Stage primaryStage;
    private static String currentUsername;
    
    // Define minimum window dimensions
    private static final double MIN_WIDTH = 500;
    private static final double MIN_HEIGHT = 750;
    
    // Define default window dimensions based on screen size
    private static Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
    private static final double DEFAULT_WIDTH = screenBounds.getWidth() * 0.8;
    private static final double DEFAULT_HEIGHT = screenBounds.getHeight() * 0.8;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("Aerodronics");
        
        // Set minimum dimensions
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
        
        // Set initial window size to 80% of screen size
        primaryStage.setWidth(DEFAULT_WIDTH);
        primaryStage.setHeight(DEFAULT_HEIGHT);
        
        // Enable window resizing
        primaryStage.setResizable(true);
        
        // Center the window on screen
        primaryStage.centerOnScreen();
        
        // Start with the login page
        showLoginPage();

        primaryStage.show();
    }

    public static void showLoginPage() {
        Login loginPage = new Login();
        Scene scene = createResponsiveScene(loginPage.getRoot(), MIN_WIDTH, MIN_HEIGHT);
        applyStyles(scene);
        primaryStage.setScene(scene);
    }

    public static void showSignupPage() {
        Signup signupPage = new Signup();
        Scene scene = createResponsiveScene(signupPage.getRoot(), MIN_WIDTH, MIN_HEIGHT);
        applyStyles(scene);
        primaryStage.setScene(scene);
    }

    public static void showDashboard() {
        Dashboard dashboard = new Dashboard();
        // Dashboard can be wider than login/signup pages
        Scene scene = createResponsiveScene(dashboard.getRoot(), MIN_WIDTH * 1.5, MIN_HEIGHT);
        applyStyles(scene);
        primaryStage.setScene(scene);
        
        // Adjust window size for dashboard if needed
        if (primaryStage.getWidth() < MIN_WIDTH * 1.5) {
            primaryStage.setWidth(MIN_WIDTH * 1.5);
            primaryStage.centerOnScreen();
        }
    }

    public static void showDashboard(String username) {
        currentUsername = username;
        showDashboard();
    }

    private static Scene createResponsiveScene(javafx.scene.Parent root, double minWidth, double minHeight) {
        // Create scene with current window dimensions
        Scene scene = new Scene(
            root,
            Math.max(primaryStage.getWidth(), minWidth),
            Math.max(primaryStage.getHeight(), minHeight)
        );
        
        // Add window resize listener
        scene.widthProperty().addListener((obs, oldVal, newVal) -> {
            updateResponsiveStyles(scene, newVal.doubleValue());
        });
        
        return scene;
    }

    private static void updateResponsiveStyles(Scene scene, double width) {
        // Remove all size-related style classes
        scene.getRoot().getStyleClass().removeAll("small", "medium", "large");
        
        // Add appropriate style class based on window width
        if (width < 800) {
            scene.getRoot().getStyleClass().add("small");
        } else if (width < 1200) {
            scene.getRoot().getStyleClass().add("medium");
        } else {
            scene.getRoot().getStyleClass().add("large");
        }
    }

    private static void applyStyles(Scene scene) {
        try {
            String cssPath = App.class.getResource("/resouces/styles.css").toExternalForm();
            scene.getStylesheets().clear();  // Clear existing stylesheets
            scene.getStylesheets().add(cssPath);
            
            // Set initial responsive styles
            updateResponsiveStyles(scene, scene.getWidth());
        } catch (Exception e) {
            System.err.println("Warning: Could not load stylesheet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String getCurrentUsername() {
        return currentUsername;
    }
    
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
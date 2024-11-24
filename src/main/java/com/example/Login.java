package com.example;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class Login {
    private static final Logger LOGGER = Logger.getLogger(Login.class.getName());
    private VBox root;
    private TextField usernameField;
    private PasswordField passwordField;
    private TextField visiblePasswordField;
    private CheckBox rememberMeCheckbox;
    private final Preferences prefs = Preferences.userNodeForPackage(Login.class);

    public Login() {
        root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.getStyleClass().add("signup-background");

        setupUI();
        loadRememberedCredentials();
        testDatabaseConnection();
    }

    private void setupUI() {
        // Logo
        setupLogo();

        // Title
        Label titleLabel = new Label("Login to Aerodronics");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleLabel.getStyleClass().add("title-label");

        // Form Container
        VBox formContainer = new VBox(15);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.getStyleClass().add("form-container");
        formContainer.setMaxWidth(400);

        // Username Field Container
        VBox usernameContainer = createFieldContainer("Username", "Enter Username", false);
        usernameField = (TextField) usernameContainer.getChildren().get(1);

        // Password Field Container with Visibility Toggle
        VBox passwordContainer = createPasswordFieldContainer("Password", "Enter Password");
        passwordField = (PasswordField) passwordContainer.lookup(".password-field");
        visiblePasswordField = (TextField) passwordContainer.lookup(".visible-password-field");

        // Remember Me Checkbox
        rememberMeCheckbox = new CheckBox("Remember Me");
        rememberMeCheckbox.getStyleClass().add("remember-me-checkbox");

        // Forgot Password Hyperlink-style Button
        Button forgotPasswordButton = new Button("Forgot Password?");
        forgotPasswordButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: transparent;" +
            "-fx-text-fill: #2196F3;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 0;"
        );
        forgotPasswordButton.setOnMouseEntered(e -> 
            forgotPasswordButton.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-border-color: transparent;" +
                "-fx-text-fill: #1976D2;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 0;"
            )
        );
        forgotPasswordButton.setOnMouseExited(e -> 
            forgotPasswordButton.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-border-color: transparent;" +
                "-fx-text-fill: #2196F3;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 0;"
            )
        );
        forgotPasswordButton.setOnAction(e -> handleForgotPassword());

        // Login Button
        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("primary-button");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setOnAction(e -> handleLogin(
            usernameField.getText(),
            passwordField.getText()
        ));

        // Container for Remember Me and Forgot Password
        HBox optionsBox = new HBox(20);
        optionsBox.setAlignment(Pos.CENTER);
        optionsBox.getChildren().addAll(rememberMeCheckbox, forgotPasswordButton);

        // Signup Link
        HBox signupBox = createSignupBox();

        // Add all elements to form container
        formContainer.getChildren().addAll(
            usernameContainer,
            passwordContainer,
            optionsBox,
            loginButton,
            signupBox
        );

        // Add all components to root
        root.getChildren().addAll(titleLabel, formContainer);
    }

    private void setupLogo() {
        try {
            ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/resouces/logo.png")));
            logo.setFitWidth(200);
            logo.setPreserveRatio(true);
            root.getChildren().add(logo);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Logo image not found", e);
        }
    }

    private HBox createSignupBox() {
        HBox signupBox = new HBox(10);
        signupBox.setAlignment(Pos.CENTER);
        Label signupLabel = new Label("Don't have an account?");
        Button signupButton = new Button("Sign Up");
        signupButton.getStyleClass().add("secondary-button");
        signupButton.setOnAction(e -> App.showSignupPage());
        signupBox.getChildren().addAll(signupLabel, signupButton);
        return signupBox;
    }

    private VBox createFieldContainer(String labelText, String promptText, boolean isPassword) {
        VBox container = new VBox(5);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("field-container");

        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");

        Control field;
        if (isPassword) {
            field = new PasswordField();
            ((PasswordField) field).setPromptText(promptText);
        } else {
            field = new TextField();
            ((TextField) field).setPromptText(promptText);
        }
        field.getStyleClass().add("input-field");

        container.getChildren().addAll(label, field);
        return container;
    }

    private VBox createPasswordFieldContainer(String labelText, String promptText) {
        VBox container = new VBox(5);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("password-field-container");

        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");

        // Horizontal box for password field and visibility toggle
        HBox passwordInputBox = new HBox(5);
        passwordInputBox.setAlignment(Pos.CENTER_LEFT);
        passwordInputBox.getStyleClass().add("password-input-box");

        PasswordField passwordField = new PasswordField();
        TextField visiblePasswordField = new TextField();
        
        passwordField.setPromptText(promptText);
        visiblePasswordField.setPromptText(promptText);
        
        passwordField.getStyleClass().addAll("input-field", "password-field");
        visiblePasswordField.getStyleClass().addAll("input-field", "visible-password-field");
        
        visiblePasswordField.setVisible(false);
        visiblePasswordField.setManaged(false);

        // Bind text properties
        passwordField.textProperty().bindBidirectional(visiblePasswordField.textProperty());

        // Password Visibility Toggle Button
        Button visibilityToggle = new Button("👁");
        visibilityToggle.getStyleClass().add("password-visibility-toggle");
        
        visibilityToggle.setOnMousePressed(e -> {
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
        });
        
        visibilityToggle.setOnMouseReleased(e -> {
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
        });

        passwordInputBox.getChildren().addAll(
            passwordField, 
            visiblePasswordField, 
            visibilityToggle
        );

        container.getChildren().addAll(label, passwordInputBox);
        return container;
    }

    private void loadRememberedCredentials() {
        if (prefs.getBoolean("rememberMe", false)) {
            String savedUsername = prefs.get("username", "");
            String savedPassword = prefs.get("password", "");
            usernameField.setText(savedUsername);
            passwordField.setText(savedPassword);
            rememberMeCheckbox.setSelected(true);
        }
    }

    private void saveCredentials(String username, String password) {
        if (rememberMeCheckbox.isSelected()) {
            prefs.putBoolean("rememberMe", true);
            prefs.put("username", username);
            prefs.put("password", password);
        } else {
            clearSavedCredentials();
        }
    }

    private void clearSavedCredentials() {
        prefs.remove("rememberMe");
        prefs.remove("username");
        prefs.remove("password");
    }

    private void handleForgotPassword() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Forgot Password");
        dialog.setHeaderText("Reset Password");
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email address");
        
        content.getChildren().addAll(
            new Label("Please enter your email address to reset your password:"),
            emailField
        );
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String email = emailField.getText();
                if (isValidEmail(email)) {
                    showInfo("Password Reset", 
                        "If an account exists for " + email + ", " +
                        "you will receive password reset instructions.");
                } else {
                    showError("Invalid Email", "Please enter a valid email address.");
                }
            }
        });
    }

    private void handleLogin(String username, String password) {
        if (validateLogin(username, password)) {
            try {
                if (DatabaseConnection.authenticateUser(username, password)) {
                    if (rememberMeCheckbox.isSelected()) {
                        saveCredentials(username, password);
                    } else {
                        clearSavedCredentials();
                    }
                    App.showDashboard();
                } else {
                    showError("Login Error", "Invalid username or password");
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Login error", e);
                showError("Login Error", "An error occurred during login. Please try again.");
            }
        }
    }

    private boolean validateLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            showError("Validation Error", "Username and password are required.");
            return false;
        }
        return true;
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private void testDatabaseConnection() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                LOGGER.info("Database connection successful");
            } else {
                LOGGER.warning("Database connection failed");
                showError("Connection Error", "Unable to connect to the database");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection error", e);
            showError("Connection Error", "Database connection failed: " + e.getMessage());
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public VBox getRoot() {
        return root;
    }
}
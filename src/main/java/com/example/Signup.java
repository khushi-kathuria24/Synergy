package com.example;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.regex.Pattern;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Signup {
    private VBox root;

    public Signup() {
        root = new VBox(10);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(36));
        root.getStyleClass().add("signup-background");

        // Logo
        try {
            ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/resouces/logo.png")));
            logo.setFitWidth(200);
            logo.setPreserveRatio(true);
            root.getChildren().add(logo);
        } catch (Exception e) {
            System.out.println("Logo image not found");
        }

        // Title
        Label titleLabel = new Label("Create Account");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        titleLabel.getStyleClass().add("title-label");

        // Form Container
        VBox formContainer = new VBox(7);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.getStyleClass().add("form-container");
        formContainer.setMaxWidth(400);

        // Full Name Field Container
        VBox fullNameContainer = createFieldContainer("Full Name", "Enter Full Name", false);
        TextField fullNameField = (TextField) fullNameContainer.getChildren().get(1);

        // Email Field Container
        VBox emailContainer = createFieldContainer("Email", "Enter Email", false);
        TextField emailField = (TextField) emailContainer.getChildren().get(1);

        // Username Field Container
        VBox usernameContainer = createFieldContainer("Username", "Choose Username", false);
        TextField usernameField = (TextField) usernameContainer.getChildren().get(1);

        // Password Field Container with Visibility Toggle
        VBox passwordContainer = createPasswordFieldContainer("Password", "Create Password");
        PasswordField passwordField = (PasswordField) passwordContainer.lookup(".password-field");

        // Confirm Password Field Container with Visibility Toggle
        VBox confirmPasswordContainer = createPasswordFieldContainer("Confirm Password", "Confirm Password");
        PasswordField confirmPasswordField = (PasswordField) confirmPasswordContainer.lookup(".password-field");

        // Signup Button
        Button signupButton = new Button("Create Account");
        signupButton.getStyleClass().add("primary-button");
        signupButton.setMaxWidth(Double.MAX_VALUE);
        signupButton.setOnAction(e -> handleSignup(
            fullNameField.getText(),
            emailField.getText(),
            usernameField.getText(),
            passwordField.getText(),
            confirmPasswordField.getText()
        ));

        // Login Link
        HBox loginBox = new HBox(5);
        loginBox.setAlignment(Pos.CENTER);
        Label loginLabel = new Label("Already have an account?");
        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("secondary-button");
        loginButton.setOnAction(e -> App.showLoginPage());
        loginBox.getChildren().addAll(loginLabel, loginButton);

        // Add all elements to form container
        formContainer.getChildren().addAll(
            fullNameContainer,
            emailContainer,
            usernameContainer,
            passwordContainer,
            confirmPasswordContainer,
            signupButton,
            loginBox
        );

        // Add all components to root
        root.getChildren().addAll(titleLabel, formContainer);
    }

    private VBox createFieldContainer(String labelText, String promptText, boolean isPassword) {
        VBox container = new VBox(5);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getStyleClass().add("password-field-container");

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

        // Improved Visibility Toggle Button
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

    private void handleSignup(String fullName, String email, String username, String password, String confirmPassword) {
        if (validateSignup(fullName, email, username, password, confirmPassword)) {
            // Attempt to register user in the database
            if (registerUserInDatabase(fullName, email, username, password)) {
                showSuccess("Account created successfully!");
                App.showLoginPage();
            } else {
                showError("Registration Error", "Failed to create account. Username might already exist.");
            }
        }
    }

    private boolean registerUserInDatabase(String fullName, String email, String username, String password) {
        String insertQuery = "INSERT INTO users (full_name, email, username, password) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
            
            // Hash the password before storing
            String hashedPassword = hashPassword(password);
            
            pstmt.setString(1, fullName);
            pstmt.setString(2, email);
            pstmt.setString(3, username);
            pstmt.setString(4, hashedPassword);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            // Log the error and show a generic error message
            System.err.println("Database registration error: " + e.getMessage());
            return false;
        }
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Hashing Error: " + e.getMessage());
            return null;
        }
    }

    private boolean validateSignup(String fullName, String email, String username, String password, String confirmPassword) {
        if (fullName.isEmpty() || email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Error", "All fields are required.");
            return false;
        }

        if (!isValidEmail(email)) {
            showError("Error", "Please enter a valid email address.");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showError("Error", "Passwords do not match.");
            return false;
        }

        if (fullName.length() < 2) {
            showError("Error", "Full name must be at least 2 characters.");
            return false;
        }

        if (username.length() < 3) {
            showError("Error", "Username must be at least 3 characters.");
            return false;
        }

        if (password.length() < 6) {
            showError("Error", "Password must be at least 6 characters.");
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        if (email == null) return false;
        // Simple email validation regex
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public VBox getRoot() {
        return root;
    }
}
package com.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ESP32CamMJPEGViewer extends Application {

    private static final String STREAM_URL = "http://192.168.234.193:81/stream"; 
    private volatile boolean running = true;

    @Override
    public void start(Stage primaryStage) {
        ImageView imageView = new ImageView();
        StackPane root = new StackPane(imageView);
        Scene scene = new Scene(root, 640, 480);

        primaryStage.setTitle("ESP32-Cam Video Stream");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> {
            running = false; // Stop video thread on close
            Platform.exit();
        });
        primaryStage.show();

        startMJPEGStream(imageView);
    }

    private void startMJPEGStream(ImageView imageView) {
        Thread videoThread = new Thread(() -> {
            try {
                URL url = new URL(STREAM_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("User-Agent", "JavaFX-MJPEG-Viewer");
                connection.setRequestProperty("Accept", "multipart/x-mixed-replace");
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                while (running) {
                    BufferedImage frame = readMJPEGFrame(inputStream);
                    if (frame != null) {
                        Platform.runLater(() -> imageView.setImage(SwingFXUtils.toFXImage(frame, null)));
                    }
                }
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        videoThread.setDaemon(true);
        videoThread.start();
    }

    private BufferedImage readMJPEGFrame(InputStream inputStream) throws Exception {
        // MJPEG frames are separated by boundary markers. Extract and decode the image.
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
                // Read content-length header
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

    public static void main(String[] args) {
        launch(args);
    }
}

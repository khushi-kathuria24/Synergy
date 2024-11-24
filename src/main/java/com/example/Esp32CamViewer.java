package com.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class Esp32CamViewer extends Application {

    private static final String STREAM_URL = "http://<ESP32-CAM-IP>/stream"; // Replace <ESP32-CAM-IP> with your ESP32-CAM IP address
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

        startVideoStream(imageView);
    }

    private void startVideoStream(ImageView imageView) {
        Thread videoThread = new Thread(() -> {
            System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME); // Load OpenCV native library

            VideoCapture capture = new VideoCapture(STREAM_URL);
            if (!capture.isOpened()) {
                System.err.println("Unable to open video stream.");
                return;
            }

            Mat frame = new Mat();
            WritableImage writableImage = new WritableImage(640, 480);

            while (running) {
                if (capture.read(frame)) {
                    if (!frame.empty()) {
                        BufferedImage bufferedImage = matToBufferedImage(frame);
                        if (bufferedImage != null) {
                            Platform.runLater(() -> {
                                SwingFXUtils.toFXImage(bufferedImage, writableImage);
                                imageView.setImage(writableImage);
                            });
                        }
                    }
                }
            }
            capture.release();
        });
        videoThread.setDaemon(true);
        videoThread.start();
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        int width = mat.width();
        int height = mat.height();
        int channels = mat.channels();
        BufferedImage image;

        if (channels > 1) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        } else {
            image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        }

        byte[] data = new byte[width * height * channels];
        mat.get(0, 0, data);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(data, 0, targetPixels, 0, data.length);
        return image;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

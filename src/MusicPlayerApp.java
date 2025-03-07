import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class MusicPlayerApp extends Application {

    // Add these CSS styles at the start of your MusicPlayerApp class
    private static final String WINDOW_STYLE = """
    -fx-background-color: #01050a;
    -fx-border-color: #000408;
    -fx-border-width: 5;
    -fx-border-radius: 49;
    -fx-background-radius: 49;
    """;

    private static final String CONTROL_BUTTON_STYLE = """
    -fx-background-color: rgba(255, 255, 255, 0.1);
    -fx-background-radius: 50;
    -fx-min-width: 72;
    -fx-min-height: 72;
    -fx-max-width: 72;
    -fx-max-height: 72;
    -fx-text-fill: white;
    """;

    private static final String PLAY_BUTTON_STYLE = """
    -fx-background-color: rgba(255, 255, 255, 0.1);
    -fx-background-radius: 50;
    -fx-min-width: 100;
    -fx-min-height: 100;
    -fx-max-width: 100;
    -fx-max-height: 100;
    -fx-text-fill: white;
    """;

    private static final String WINDOW_CONTROL_STYLE = """
    -fx-background-color: transparent;
    -fx-border-color: #9B9FB3;
    -fx-border-width: 4;
    -fx-border-radius: 20;
    -fx-background-radius: 20;
    -fx-min-width: 44;
    -fx-min-height: 44;
    -fx-max-width: 44;
    -fx-max-height: 44;
    -fx-text-fill: #9B9FB3;
    -fx-opacity: 1.0;
    """;

    // Track whether the song was playing when the user started dragging the progress bar.
    private boolean sliderWasPlaying = false;

    // Sliders for progress
    private Slider progressSlider;
    private Label currentTimeLabel;
    private Label totalTimeLabel;

    // At the top of your MusicPlayerApp class, add a constant for maximum display length:
    private static final int MAX_TRACK_NAME_LENGTH = 80;

    // QUEUE FEATURE: Two queues for track management
    private List<File> defaultQueue = new ArrayList<>();
    private List<File> customQueue = new ArrayList<>(); // Unimplemented custom queue for future use
    private int currentTrackIndex = -1;

    // Variables for window dragging
    private double xOffset = 0;
    private double yOffset = 0;

    // Main panes for the two screens
    private StackPane contentPane;
    private BorderPane playerPane;
    private BorderPane trackListPane;

    // UI elements on the player screen
    private ImageView albumImageView;
    private MediaPlayer mediaPlayer;

    private BorderPane root;

    // Make the play button a class-level variable so we can update its text from anywhere
    private Button btnPlayStop;

    // Folder from which to load tracks (relative to current directory)
    private File musicDir = new File("music"); // make sure this folder exists with audio files
    private File artDir = new File("music\\artwork"); // make sure this folder exists for extracted artwork

    // UI element on the track list screen
    private ListView<File> trackListView;

    // Make the top bar a class-level variable so it can be updated later
    private HBox topBar;

    // Add this at the top of your MusicPlayerApp class (with your other class-level variables)
    private Label playIcon;


    @Override
    public void start(Stage primaryStage) {
        // Create a style that ensures text is visible and properly sized
        String windowButtonTextStyle = WINDOW_CONTROL_STYLE +
                "-fx-font-size: 28px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: -3 0 0 0; " +
                "-fx-content-display: center; " +
                "-fx-alignment: top-center;";

        primaryStage.initStyle(StageStyle.TRANSPARENT);

        // Create a top bar with navigation and window controls
        topBar = new HBox();
        topBar.setStyle("-fx-background-color: transparent; -fx-padding: 15;");

        // Left side: Navigation buttons
        HBox navButtons = new HBox(10);
        navButtons.setAlignment(Pos.CENTER_LEFT);

        Button btnNowPlaying = new Button("♪");
        Button btnLibrary = new Button("≡");
        btnNowPlaying.setStyle(windowButtonTextStyle + "-fx-padding: -3 0 0 0;");
        btnLibrary.setStyle(windowButtonTextStyle + "-fx-padding: -3 0 0 0;");

        btnNowPlaying.setOnAction(e -> switchToPlayer());
        btnLibrary.setOnAction(e -> switchToTrackList());

        navButtons.getChildren().addAll(btnNowPlaying, btnLibrary);

        // Right side: Window controls
        HBox windowControls = new HBox(10);
        windowControls.setAlignment(Pos.CENTER_RIGHT);

        // Use HBox.setHgrow to create space between left and right controls
        HBox.setHgrow(windowControls, Priority.ALWAYS);

        // Add mouse listeners for window dragging
        topBar.setOnMousePressed((MouseEvent e) -> {
            xOffset = e.getSceneX();
            yOffset = e.getSceneY();
        });
        topBar.setOnMouseDragged((MouseEvent e) -> {
            primaryStage.setX(e.getScreenX() - xOffset);
            primaryStage.setY(e.getScreenY() - yOffset);
        });

        // Styled window controls
        Button btnMinimize = new Button("–");
        Button btnExit = new Button("×");

        btnExit.setStyle(windowButtonTextStyle);

        // For the minimize button, adjust vertical alignment differently
        btnMinimize.setStyle(windowButtonTextStyle + "-fx-font-size: 36px; " + "-fx-padding: -5 0 0 0;" + "-fx-font-family: 'Courier New';");

        btnMinimize.setOnMouseEntered(e -> btnMinimize.setStyle(windowButtonTextStyle + "-fx-font-size: 36px; " + "-fx-padding: -5 0 0 0;" + "-fx-background-color: rgba(178, 191, 230, 0.20); "+ "-fx-font-family: 'Courier New';"));
        btnMinimize.setOnMouseExited(e -> btnMinimize.setStyle(windowButtonTextStyle + "-fx-font-size: 36px; " + "-fx-padding: -5 0 0 0;"+ "-fx-font-family: 'Courier New';"));

        btnExit.setOnMouseEntered(e -> btnExit.setStyle(windowButtonTextStyle + "-fx-background-color: rgba(178, 191, 230, 0.20); "));
        btnExit.setOnMouseExited(e -> btnExit.setStyle(windowButtonTextStyle));

        btnExit.setOnAction(e -> primaryStage.close());
        btnMinimize.setOnAction(e -> primaryStage.setIconified(true));
        windowControls.getChildren().addAll(btnMinimize, btnExit);
        topBar.getChildren().addAll(navButtons, windowControls);

        // Main content setup
        contentPane = new StackPane();
        playerPane = createPlayerScreen();
        trackListPane = createTrackListScreen();
        contentPane.getChildren().addAll(playerPane, trackListPane);

        // Initially show player screen
        playerPane.setVisible(true);
        trackListPane.setVisible(false);

        // Main layout
        root = new BorderPane();
        root.setStyle(WINDOW_STYLE);
        root.setTop(topBar);
        root.setCenter(contentPane);

        Scene scene = new Scene(root, 533, 820);
        scene.setFill(null); // Required for transparent window
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private BorderPane createPlayerScreen() {
        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-background-color: transparent;");

        // Album image view with rounded corners
        albumImageView = new ImageView();
        albumImageView.setFitWidth(480);
        albumImageView.setFitHeight(480);
        albumImageView.setPreserveRatio(true);
        Rectangle clip = new Rectangle(480, 480);
        clip.setArcWidth(100);
        clip.setArcHeight(100);
        albumImageView.setClip(clip);

        // <<-- NEW: Create progress slider with time labels and hide them initially -->>
        progressSlider = new Slider();
        progressSlider.setMin(0);
        progressSlider.setMax(100);
        progressSlider.setValue(0);
        progressSlider.setPrefWidth(300); // progress bar is half the width
        progressSlider.setVisible(false); // hide until a song is played

        // Allow user to seek: pause on press and update time on release.
        progressSlider.setOnMousePressed(e -> {
            if (mediaPlayer != null) {
                if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    sliderWasPlaying = true;
                    mediaPlayer.pause();
                } else {
                    sliderWasPlaying = false;
                }
            }
        });
        progressSlider.setOnMouseReleased(e -> {
            if (mediaPlayer != null) {
                Duration total = mediaPlayer.getTotalDuration();
                double seekTime = progressSlider.getValue() / 100.0 * total.toSeconds();
                mediaPlayer.seek(Duration.seconds(seekTime));
                // Resume playing only if it was playing before the drag.
                if (sliderWasPlaying) {
                    mediaPlayer.play();
                    playIcon.setText("⏸");
                }
            }
        });

        // Create time labels and hide them initially
        currentTimeLabel = new Label("00:00");
        totalTimeLabel = new Label("00:00");
        currentTimeLabel.setVisible(false);
        totalTimeLabel.setVisible(false);

        // Container for time labels and slider
        HBox progressContainer = new HBox(10);
        progressContainer.setAlignment(Pos.CENTER);
        progressContainer.getChildren().addAll(currentTimeLabel, progressSlider, totalTimeLabel);

        // Combine album image and progress container in a VBox
        VBox centerBox = new VBox(10);
        centerBox.setAlignment(Pos.CENTER);
        centerBox.getChildren().addAll(albumImageView, progressContainer);
        centerBox.setPadding(new Insets(20));
        pane.setCenter(centerBox);

        // Bottom: Playback controls
        HBox controls = new HBox(20);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(34, 0, 20, 0));

        Button btnSkipBack = new Button();
        btnSkipBack.setStyle(CONTROL_BUTTON_STYLE);
        // ... (hover effects for btnSkipBack)
        Label skipBackIcon = new Label("⏮");
        skipBackIcon.setStyle("-fx-font-size: 39px; -fx-smooth: true; -fx-text-fill: white; -fx-padding: 0 0 2 1;");
        btnSkipBack.setGraphic(new Group(skipBackIcon));

        Button btnSkipForward = new Button();
        btnSkipForward.setStyle(CONTROL_BUTTON_STYLE);
        // ... (hover effects for btnSkipForward)
        Label skipForwardIcon = new Label("⏭");
        skipForwardIcon.setStyle("-fx-font-size: 39px; -fx-smooth: true; -fx-text-fill: white; -fx-padding: 0 1 2 0;");
        btnSkipForward.setGraphic(new Group(skipForwardIcon));

        // <<-- MODIFIED: Skip button actions to change track in the queue -->>
        btnSkipBack.setOnAction(e -> {
            if (!defaultQueue.isEmpty()) {
                currentTrackIndex = (currentTrackIndex - 1 + defaultQueue.size()) % defaultQueue.size();
                File previousTrack = defaultQueue.get(currentTrackIndex);
                playTrack(previousTrack);
            }
        });

        btnSkipForward.setOnAction(e -> {
            if (!defaultQueue.isEmpty()) {
                currentTrackIndex = (currentTrackIndex + 1) % defaultQueue.size();
                File nextTrack = defaultQueue.get(currentTrackIndex);
                playTrack(nextTrack);
            }
        });

        // Use the class-level play button here
        // --- Modified play button code in createPlayerScreen() ---
        btnPlayStop = new Button();
        btnPlayStop.setStyle(PLAY_BUTTON_STYLE);

        // Add hover effect for btnPlayStop:
        btnPlayStop.setOnMouseEntered(e -> btnPlayStop.setStyle( "-fx-background-color: rgba(255, 255, 255, 0.16);"
                + "-fx-background-radius: 50;" + "-fx-min-width: 100;" + "-fx-min-height: 100;" + "-fx-max-width: 100;" +
                "-fx-max-height: 100;" + "-fx-text-fill: white;" )); btnPlayStop.setOnMouseExited(e ->
                btnPlayStop.setStyle(PLAY_BUTTON_STYLE));

        // Create a separate label for the play icon with an enlarged font (4× larger)
        playIcon = new Label("▶");
        playIcon.setStyle("-fx-font-size: 100px; -fx-text-fill: white; -fx-smooth: true; -fx-padding: 2 0 0 2;");
        // Wrap the label in a Group to prevent clipping issues
        btnPlayStop.setGraphic(new Group(playIcon));

        // Setup button actions
        btnPlayStop.setOnAction(e -> {
            if (mediaPlayer != null) {
                if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    mediaPlayer.pause();
                    playIcon.setStyle("-fx-font-size: 100px; -fx-text-fill: white; -fx-smooth: true; -fx-padding: 2 0 0 2;");
                    playIcon.setText("▶");
                } else {
                    mediaPlayer.play();
                    playIcon.setStyle("-fx-font-size: 100px; -fx-text-fill: white; -fx-padding: 0 0 13 0; -fx-smooth: true;");
                    playIcon.setText("⏸");
                }
            }
        });

        controls.getChildren().addAll(btnSkipBack, btnPlayStop, btnSkipForward);
        pane.setBottom(controls);

        // <<-- ADD THIS CODE TO CLIP THE PLAYER PANE -->>
        pane.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            double width = newBounds.getWidth();
            double height = newBounds.getHeight();
            double radius = 49 * 2; // Rounded bottom corners

            // Primary clip: Rounded rectangle
            Rectangle roundedRect = new Rectangle(width, height);
            roundedRect.setArcWidth(radius);
            roundedRect.setArcHeight(radius);

            // Secondary clip: A full-width rectangle covering the top portion
            Rectangle topRect = new Rectangle(width, height - radius / 2);
            topRect.setTranslateY(0); // Align with top

            // Use an intersection to keep only the bottom rounded part
            pane.setClip(new Group(roundedRect, topRect));
        });
        // <<-- END ADDITION -->>

        return pane;
    }

    // Method to update background with blurred album art
    private void updateBackground(Image albumImage) {
        if (albumImage != null) {
            ImageView bgImage = new ImageView(albumImage);
            bgImage.setFitWidth(playerPane.getWidth() + 40);
            bgImage.setFitHeight(playerPane.getHeight() + 40);
            bgImage.setPreserveRatio(false);
            bgImage.setSmooth(true);
            bgImage.setCache(true);

            // Create blur effect with conditional color adjustment
            GaussianBlur blur = new GaussianBlur(30);
            ColorAdjust darker = new ColorAdjust();

            double avgBrightness = getImageAverageBrightness(albumImage);
            if (avgBrightness > 0.8) {  // very bright image
                darker.setBrightness(-0.6);
            } else {
                darker.setBrightness(-0.7);
                darker.setSaturation(0.3);
            }
            blur.setInput(darker);
            bgImage.setEffect(blur);

            SnapshotParameters params = new SnapshotParameters();
            WritableImage snapshot = bgImage.snapshot(params, null);

            BackgroundImage background = new BackgroundImage(
                    snapshot,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    BackgroundSize.DEFAULT
            );
            playerPane.setBackground(new Background(background));
        } else {
            playerPane.setStyle("-fx-background-color: #01050a;");
        }
    }


    /**
     * Creates the track list screen that displays audio files from the specified folder.
     * Double-clicking a track will load and play it.
     */
    private BorderPane createTrackListScreen() {
        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-background-color: #0f0f0f;");

        // Center: ListView of tracks
        trackListView = new ListView<>();
        // Keep the ListView editable so we can commit changes but disable auto-edit in each cell.
        trackListView.setEditable(true);
        trackListView.setStyle("-fx-control-inner-background: transparent; -fx-background-color: transparent;");

        // Use a custom cell factory that shows a rename button to trigger editing.
        trackListView.setCellFactory(lv -> new ListCell<File>() {
            private HBox hbox;
            private Label nameLabel;
            private Button editButton;
            private TextField textField;

            {
                // Disable the default editing trigger on the cell.
                setEditable(false);

                // Build the cell layout: a label on the left, a spacer, and an edit button on the right.
                hbox = new HBox();
                nameLabel = new Label();
                nameLabel.setTextFill(Color.WHITE);
                editButton = new Button("✎");
                editButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 20px; -fx-padding: -6 0 -6 0");
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                hbox.getChildren().addAll(nameLabel, spacer, editButton);
                hbox.setAlignment(Pos.CENTER_LEFT);
                hbox.setSpacing(10);

                // When the user clicks the edit button, explicitly start editing.
                editButton.setOnAction(e -> {
                    startEdit();
                    e.consume();
                });
            }

            @Override
            protected void updateItem(File item, boolean empty) {
                super.updateItem(item, empty);
                int index = getIndex() < 0 ? 0 : getIndex();
                String baseColor = (index % 2 == 0) ? "#0f0f0f" : "#1a1a1a";

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setTooltip(null); // Remove tooltip when empty
                    setStyle("-fx-background-color: " + baseColor + "; -fx-font-size: 16px;");
                } else {
                    // Attach a tooltip showing the full file name
                    setTooltip(new Tooltip(item.getName()));

                    if (isEditing()) {
                        if (textField == null) {
                            textField = new TextField(item.getName());
                            textField.setStyle("-fx-font-size: 18px;");
                            textField.setOnAction(e -> processEdit());
                            textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                                if (!isNowFocused) {
                                    processEdit();
                                }
                            });
                        }
                        textField.setText(item.getName());
                        setText(null);
                        setGraphic(textField);
                    } else {
                        nameLabel.setText(getTruncatedFileName(item.getName()));
                        setText(null);
                        setGraphic(hbox);
                    }

                    // Update hover style
                    setOnMouseEntered(e -> {
                        if (!isSelected()) {
                            String hoverColor = (baseColor.equals("#0f0f0f") ? "#202020" : "#323232");
                            setStyle("-fx-background-color: " + hoverColor + "; -fx-font-size: 16px;");
                        }
                    });
                    setOnMouseExited(e -> {
                        if (!isSelected()) {
                            setStyle("-fx-background-color: " + baseColor + "; -fx-font-size: 16px;");
                        }
                    });
                }
            }

            @Override
            public void startEdit() {
                super.startEdit();
                if (getItem() == null) {
                    return;
                }
                if (textField == null) {
                    textField = new TextField();
                    textField.setStyle("-fx-font-size: 18px;");
                    textField.setOnAction(e -> processEdit());
                    textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                        if (!isNowFocused) {
                            processEdit();
                        }
                    });
                }
                // Remove ".mp3" from the display text when editing (if present)
                String fullName = getItem().getName();
                String displayName = fullName;
                if (fullName.toLowerCase().endsWith(".mp3")) {
                    displayName = fullName.substring(0, fullName.length() - 4);
                }
                textField.setText(displayName);
                setGraphic(textField);
                textField.selectAll();
                textField.requestFocus();
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setGraphic(hbox);
                updateItem(getItem(), false);
            }

            @Override
            public void updateSelected(boolean selected) {
                super.updateSelected(selected);
                int index = getIndex() < 0 ? 0 : getIndex();
                String baseColor = (index % 2 == 0) ? "#0f0f0f" : "#1a1a1a";
                if (selected) {
                    setStyle("-fx-background-color: #4a4a4a; -fx-font-size: 16px;");
                } else {
                    setStyle("-fx-background-color: " + baseColor + "; -fx-font-size: 16px;");
                }
            }

            // Helper method to remove the .mp3 extension if present.
            private String beautifyFileName(String fileName) {
                if (fileName.toLowerCase().endsWith(".mp3")) {
                    return fileName.substring(0, fileName.length() - 4);
                }
                return fileName;
            }

            // Helper method to truncate the beautified file name.
            private String getTruncatedFileName(String fileName) {
                String beautified = beautifyFileName(fileName);
                if (beautified.length() <= MAX_TRACK_NAME_LENGTH) {
                    return beautified;
                } else {
                    return beautified.substring(0, MAX_TRACK_NAME_LENGTH) + "...";
                }
            }

            private void processEdit() {
                if (textField == null || getItem() == null) {
                    cancelEdit();
                    return;
                }
                String newName = textField.getText().trim();
                if (newName.isEmpty()) {
                    cancelEdit();
                    return;
                }
                newName = sanitizeFileName(newName);
                File oldFile = getItem();
                String originalName = oldFile.getName();
                String extension = "";
                int dotIndex = originalName.lastIndexOf('.');
                if (dotIndex > 0) {
                    extension = originalName.substring(dotIndex); // e.g. ".mp3"
                }
                // Ensure the new name has the same extension as the original.
                if (!newName.toLowerCase().endsWith(extension.toLowerCase())) {
                    newName = newName + extension;
                }
                // Avoid renaming if the new name is identical to the old name.
                if (newName.equals(oldFile.getName())) {
                    cancelEdit();
                    return;
                }
                File newFile = new File(oldFile.getParent(), newName);
                if (newFile.exists()) {
                    int counter = 1;
                    String nameWithoutExt = newName;
                    String ext = "";
                    int dotIdx = newName.lastIndexOf('.');
                    if (dotIdx > 0) {
                        nameWithoutExt = newName.substring(0, dotIdx);
                        ext = newName.substring(dotIdx);
                    }
                    while (newFile.exists()) {
                        newFile = new File(oldFile.getParent(), nameWithoutExt + "_" + counter + ext);
                        counter++;
                    }
                }
                boolean success = oldFile.renameTo(newFile);
                if (success) {
                    // Also rename the artwork file if it exists.
                    String oldFileName = oldFile.getName();
                    int dotIdx = oldFileName.lastIndexOf('.');
                    String oldBaseName = dotIdx > 0 ? oldFileName.substring(0, dotIdx) : oldFileName;
                    File oldArtworkFile = new File(artDir, oldBaseName + ".png");
                    if (oldArtworkFile.exists()) {
                        String newFileName = newFile.getName();
                        int newDotIdx = newFileName.lastIndexOf('.');
                        String newBaseName = newDotIdx > 0 ? newFileName.substring(0, newDotIdx) : newFileName;
                        File newArtworkFile = new File(artDir, newBaseName + ".png");
                        boolean artworkRenamed = oldArtworkFile.renameTo(newArtworkFile);
                        if (!artworkRenamed) {
                            System.out.println("Failed to rename artwork file: " + oldArtworkFile.getAbsolutePath());
                        }
                    }
                    getListView().getItems().set(getIndex(), newFile);
                    commitEdit(newFile);
                } else {
                    cancelEdit();
                }
            }
        });

        // Populate the ListView with track files from the music directory.
        if (musicDir.exists() && musicDir.isDirectory()) {
            File[] files = musicDir.listFiles((dir, name) -> {
                String lower = name.toLowerCase();
                return lower.endsWith(".mp3") || lower.endsWith(".wav") || lower.endsWith(".m4a") || lower.endsWith(".flac");
            });
            if (files != null) {
                for (File file : files) {
                    trackListView.getItems().add(file);
                }
            }
        }

        // When the user double-clicks a cell (but not on the edit button), play the track.
        trackListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                File selectedFile = trackListView.getSelectionModel().getSelectedItem();
                if (selectedFile != null) {
                    playTrack(selectedFile);
                }
            }
        });
        pane.setCenter(trackListView);

        // Clip the pane for rounded corners.
        pane.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            double width = newBounds.getWidth();
            double height = newBounds.getHeight();
            double radius = 49 * 2; // Rounded bottom corners

            // Primary clip: Rounded rectangle.
            Rectangle roundedRect = new Rectangle(width, height);
            roundedRect.setArcWidth(radius);
            roundedRect.setArcHeight(radius);

            // Secondary clip: A full-width rectangle covering the top portion.
            Rectangle topRect = new Rectangle(width, height - radius / 2);
            topRect.setTranslateY(0); // Align with top

            // Combine the clips.
            pane.setClip(new Group(roundedRect, topRect));
        });

        return pane;
    }

    /**
     * Switches the visible screen to the player screen.
     */
    private void switchToPlayer() {
        playerPane.setVisible(true);
        trackListPane.setVisible(false);
    }

    /**
     * Switches the visible screen to the track list screen.
     */
    private void switchToTrackList() {
        trackListPane.setVisible(true);
        playerPane.setVisible(false);
    }

    /**
     * Loads and plays the selected track. When the media is ready, the code extracts the embedded album cover
     * (if available) and updates the album image view and player background.
     */
    private void playTrack(File file) {
        // QUEUE FEATURE: Always update the default queue and current track index
        defaultQueue = new ArrayList<>(trackListView.getItems());
        for (int i = 0; i < defaultQueue.size(); i++) {
            if (defaultQueue.get(i).getAbsolutePath().equals(file.getAbsolutePath())) {
                currentTrackIndex = i;
                break;
            }
        }

        // Check if the file is a FLAC file; if so, convert it to MP3 using ffmpeg
        if (file.getName().toLowerCase().endsWith(".flac")) {
            File originalFile = file;
            File mp3File = new File(file.getParent(), file.getName().substring(0, file.getName().lastIndexOf(".")) + ".mp3");
            if (!mp3File.exists()) {
                System.out.println("Converting FLAC to MP3: " + file.getAbsolutePath());
                try {
                    ProcessBuilder pb = new ProcessBuilder(
                            "ffmpeg",
                            "-i", file.getAbsolutePath(),
                            "-ab", "320k",
                            "-map_metadata", "0",
                            "-id3v2_version", "3",
                            mp3File.getAbsolutePath()
                    );
                    pb.redirectErrorStream(true);
                    Process process = pb.start();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                    int exitCode = process.waitFor();
                    if (exitCode != 0) {
                        System.out.println("FFmpeg conversion failed with exit code " + exitCode);
                    } else {
                        System.out.println("Conversion successful: " + mp3File.getAbsolutePath());
                        if (originalFile.delete()) {
                            System.out.println("Deleted original FLAC file: " + originalFile.getAbsolutePath());
                        } else {
                            System.out.println("Failed to delete original FLAC file: " + originalFile.getAbsolutePath());
                        }
                        file = mp3File;
                        // Update the track list item with the new MP3 file
                        int index = trackListView.getItems().indexOf(originalFile);
                        if (index != -1) {
                            trackListView.getItems().set(index, file);
                        }
                    }
                } catch (IOException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            } else {
                file = mp3File;
            }
        }
        // Check if the file is an M4A file; if so, convert it to MP3 using ffmpeg
        else if (file.getName().toLowerCase().endsWith(".m4a")) {
            File originalFile = file;
            File mp3File = new File(file.getParent(), file.getName().substring(0, file.getName().lastIndexOf(".")) + ".mp3");
            if (!mp3File.exists()) {
                System.out.println("Converting M4A to MP3: " + file.getAbsolutePath());
                try {
                    ProcessBuilder pb = new ProcessBuilder(
                            "ffmpeg",
                            "-i", file.getAbsolutePath(),
                            "-c:v", "copy",
                            "-c:a", "libmp3lame",
                            "-q:a", "4",
                            mp3File.getAbsolutePath()
                    );
                    pb.redirectErrorStream(true);
                    Process process = pb.start();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                    int exitCode = process.waitFor();
                    if (exitCode != 0) {
                        System.out.println("FFmpeg conversion failed with exit code " + exitCode);
                    } else {
                        System.out.println("Conversion successful: " + mp3File.getAbsolutePath());
                        if (originalFile.delete()) {
                            System.out.println("Deleted original M4A file: " + originalFile.getAbsolutePath());
                        } else {
                            System.out.println("Failed to delete original M4A file: " + originalFile.getAbsolutePath());
                        }
                        file = mp3File;
                        // Update the track list item with the new MP3 file
                        int index = trackListView.getItems().indexOf(originalFile);
                        if (index != -1) {
                            trackListView.getItems().set(index, file);
                        }
                    }
                } catch (IOException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            } else {
                file = mp3File;
            }
        }

        System.out.println("Attempting to play track: " + file.getAbsolutePath());
        // Extract artwork when playing a track
        String artworkPath = AudioArtworkExtractor.extractArtwork(file.getAbsolutePath());

        // Stop any currently playing media
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

        try {
            File fileToPlay = copyToTemp(file);
            Media media = new Media(fileToPlay.toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            // Load artwork if available
            if (artworkPath != null) {
                Image albumImage = new Image("file:" + artworkPath);
                albumImageView.setImage(albumImage);
                updateBackground(albumImage);

                try {
                    Thread.sleep(100);

                    String topColorHex = AverageColorFinder.findDominantColorTop(artworkPath);
                    Color convColor = Color.web(topColorHex);
                    ColorAdjust darker = new ColorAdjust();
                    darker.setBrightness(-0.7);
                    darker.setSaturation(0.8);
                    Color adjustedColor = convColor.deriveColor(0, 1, 1 + darker.getBrightness(), 1);
                    String convHex = String.format("#%02X%02X%02X",
                            (int) (adjustedColor.getRed() * 255),
                            (int) (adjustedColor.getGreen() * 255),
                            (int) (adjustedColor.getBlue() * 255));
                    topBar.setStyle("-fx-background-color: " + convHex + "; -fx-padding: 15; -fx-background-radius: 49 49 0 0;");

                    // Compute a slightly darker color for the border
                    Color borderColor = adjustedColor.darker();
                    root.setBorder(new Border(new BorderStroke(
                            borderColor,
                            BorderStrokeStyle.SOLID,
                            new CornerRadii(49),
                            new BorderWidths(5)
                    )));

                    root.setBackground(new Background(new BackgroundFill(
                            borderColor,
                            new CornerRadii(49),
                            Insets.EMPTY
                    )));

                    Color inverseColor = invertColor(convColor);
                    String inverseHex = toHexString(inverseColor);
                    currentTimeLabel.setStyle("-fx-text-fill: " + inverseHex + "; -fx-font-size: 16px; -fx-font-weight: bold;");
                    totalTimeLabel.setStyle("-fx-text-fill: " + inverseHex + "; -fx-font-size: 16px; -fx-font-weight: bold;");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else {
                albumImageView.setImage(null);
                playerPane.setStyle("-fx-background-color: #01050a;");
                topBar.setStyle("-fx-background-color: transparent; -fx-padding: 15;");
            }

            // QUEUE FEATURE: Set callback to play the next track in the default queue when current track ends.
            mediaPlayer.setOnEndOfMedia(() -> {
                if (!defaultQueue.isEmpty()) {
                    currentTrackIndex = (currentTrackIndex + 1) % defaultQueue.size();
                    File nextTrack = defaultQueue.get(currentTrackIndex);
                    playTrack(nextTrack);
                }
            });

            mediaPlayer.setOnReady(() -> {
                System.out.println("Media is ready. Starting playback.");
                // Show the progress slider and time labels once the media is ready.
                progressSlider.setVisible(true);
                currentTimeLabel.setVisible(true);
                totalTimeLabel.setVisible(true);

                // Set the time labels to white with increased size and bold text.
                currentTimeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-font-family: 'Consolas';");
                totalTimeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-font-family: 'Consolas';");

                // Initialize slider and update it as the track plays.
                progressSlider.setValue(0);
                Duration total = mediaPlayer.getTotalDuration();
                totalTimeLabel.setText(formatTime(total));

                mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                    Duration tot = mediaPlayer.getTotalDuration();
                    if (tot != null && tot.toSeconds() > 0) {
                        progressSlider.setValue(newTime.toSeconds() / tot.toSeconds() * 100);
                        currentTimeLabel.setText(formatTime(newTime));
                    }
                });
                mediaPlayer.play();
                switchToPlayer();
                playIcon.setStyle("-fx-font-size: 100px; -fx-text-fill: white; -fx-padding: 0 0 13 0; -fx-smooth: true;");
                playIcon.setText("⏸");
            });

            mediaPlayer.setOnError(() -> {
                System.out.println("Error occurred: " + mediaPlayer.getError().getMessage());
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Add this helper method to sanitize filenames (inside your MusicPlayerApp class)
    private String sanitizeFileName(String input) {
        // Replace any character not allowed in filenames with an underscore.
        return input.replaceAll("[\\\\/:*?\"<>|]", "_");
    }

    private File copyToTemp(File original) throws IOException {
        File temp = File.createTempFile("tempMedia", original.getName());
        Files.copy(original.toPath(), temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
        temp.deleteOnExit();
        return temp;
    }

    private double getImageAverageBrightness(Image image) {
        PixelReader pixelReader = image.getPixelReader();
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        double totalBrightness = 0;
        int count = 0;
        for (int x = 0; x < width; x += 10) {  // sample every 10 pixels for performance
            for (int y = 0; y < height; y += 10) {
                Color color = pixelReader.getColor(x, y);
                totalBrightness += color.getBrightness();
                count++;
            }
        }
        return totalBrightness / count;
    }

    // <<-- NEW: Helper method to format Duration as mm:ss -->>
    private String formatTime(Duration duration) {
        int seconds = (int) Math.floor(duration.toSeconds());
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }

    private Color invertColor(Color color) {
        return new Color(1 - color.getRed(), 1 - color.getGreen(), 1 - color.getBlue(), 1.0);
    }

    // <<-- NEW: Helper method to convert a Color to a hex string -->>
    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }

    public static void main(String[] args) {
        launch(args);
    }
}

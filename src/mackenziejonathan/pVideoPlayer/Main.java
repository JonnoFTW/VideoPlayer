package mackenziejonathan.pVideoPlayer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Main extends Application {

    private List<File> mMovieFiles = new ArrayList<>(24);
    private Iterator<File> mIterator;

    /**
     * Get the files and put them in the movieFiles array
     * Also setup the iterator
     */
    private boolean setupFiles() throws URISyntaxException {
        String jarDir = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        if (jarDir.endsWith(".jar")) {
            jarDir = jarDir.substring(0, jarDir.lastIndexOf('/'));
        }
        File currentDir = new File(jarDir);
        System.out.println("Loading from " + currentDir.getAbsolutePath());
        try {
            for (File f : currentDir.listFiles()) {
                if (f.isFile() && f.getAbsolutePath().toLowerCase().matches(".*\\.(mp4|m4a|m4v|flv|fxm)")) {
                    mMovieFiles.add(f);
                    System.out.println("Adding: " + f.getAbsolutePath());
                }
            }
        }   catch(NullPointerException|SecurityException npe) {
            System.err.println("Error listing videos");
            mMovieFiles.clear();
        }
        if (mMovieFiles.isEmpty() ) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("pVideoPlayer Error");
            alert.setHeaderText("Fatal Error!");
            alert.setContentText("You need to place VideoPlayer.jar file in a folder\n that contains .mp4 or flv files");
            alert.showAndWait();
            return false;
        }
        Collections.shuffle(mMovieFiles);
//        mAllMoviesIterator = new PermutationIterator<>(mMovieFiles);
        return true;
    }

    private MediaView createMediaView() {
        MediaView mediaView = new MediaView();
        initMediaPlayer(mediaView);
        return mediaView;
    }

    private void initMediaPlayer(final MediaView mediaView) {
        if (mIterator == null) {
            System.out.println("Playing next shuffle");
            Collections.shuffle(mMovieFiles);
            mIterator = mMovieFiles.iterator();
        }
        if (mIterator.hasNext()) {
            File currentMovie = mIterator.next();
            System.out.println("Playing next video: " + currentMovie.getAbsolutePath());
            MediaPlayer mediaPlayer = new MediaPlayer((new Media(currentMovie.toURI().toString())));
            final DoubleProperty width = mediaView.fitWidthProperty();
            final DoubleProperty height = mediaView.fitHeightProperty();

            width.bind(Bindings.selectDouble(mediaView.sceneProperty(), "width"));
            height.bind(Bindings.selectDouble(mediaView.sceneProperty(), "height"));
            mediaView.setPreserveRatio(true);
            mediaPlayer.setAutoPlay(true);
            mediaPlayer.setOnEndOfMedia(() -> {
                mediaPlayer.dispose();
                System.gc();
                initMediaPlayer(mediaView);
            });

            mediaView.setMediaPlayer(mediaPlayer);
        } else {
            mIterator = null;
            initMediaPlayer(mediaView);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // load all the file in the current directory
        if (!setupFiles()) {
            Platform.exit();
            return;
        }


        MediaView mv = createMediaView();
        StackPane root = new StackPane();
        root.getChildren().add(mv);
        final Scene scene = new Scene(root, 960, 540);

        primaryStage.setTitle("pVideoPlayer - The Permutations Video Player");
        primaryStage.setScene(scene);
        scene.setFill(Color.BLACK);

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE) {
                primaryStage.setFullScreen(true);
            }
        });

        primaryStage.setFullScreen(true);
        primaryStage.show();

    }


    public static void main(String[] args) {

        launch(args);
    }
}

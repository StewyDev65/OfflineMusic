import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.images.Artwork;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AudioArtworkExtractor {

    public static String extractArtwork(String mp3FilePath) {
        if (!mp3FilePath.toLowerCase().endsWith(".mp3")) {
            System.out.println("Skipping non-MP3 file: " + mp3FilePath);
            return null;
        }

        try {
            File mp3File = new File(mp3FilePath);
            String baseName = mp3File.getName().substring(0, mp3File.getName().lastIndexOf('.'));

            // Create artwork directory if it doesn't exist
            Path artworkDir = Paths.get(mp3File.getParent(), "artwork");
            if (!Files.exists(artworkDir)) {
                Files.createDirectory(artworkDir);
            }

            // Output path for the artwork
            Path artworkPath = artworkDir.resolve(baseName + ".png");

            // Skip if artwork already exists
            if (Files.exists(artworkPath)) {
                System.out.println("Artwork already exists for: " + baseName);
                return artworkPath.toString();
            }

            AudioFile f = AudioFileIO.read(mp3File);
            Tag tag = f.getTag();

            if (tag != null) {
                Artwork artwork = tag.getFirstArtwork();
                if (artwork != null) {
                    byte[] imageData = artwork.getBinaryData();
                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageData));
                    if (img != null) {
                        ImageIO.write(img, "png", artworkPath.toFile());
                        System.out.println("Extracted artwork for: " + baseName);
                        return artworkPath.toString();
                    }
                }
            }

            System.out.println("No artwork found for: " + baseName);
            return null;

        } catch (Exception e) {
            System.err.println("Error extracting artwork from: " + mp3FilePath);
            e.printStackTrace();
            return null;
        }
    }

    public static void extractAllArtwork(String musicDirPath) {
        try {
            Files.walk(Paths.get(musicDirPath))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".mp3"))
                    .forEach(path -> extractArtwork(path.toString()));
        } catch (IOException e) {
            System.err.println("Error walking through music directory");
            e.printStackTrace();
        }
    }
}
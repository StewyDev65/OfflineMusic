import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class AverageColorFinder {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java AverageColorFinder <image_path>");
            return;

        }


        String imagePath = args[0];
        try {
            // Get both the average and most common colors
            String averageColorHex = findAverageColor(imagePath);
            String dominantColorHex = findDominantColor(imagePath);
            String combinedColorHex = combineColors(averageColorHex, dominantColorHex);
            String topColorHex = findDominantColorTop(imagePath);

            System.out.println("Average color: " + averageColorHex);
            System.out.println("Most common color: " + dominantColorHex);
            System.out.println("Combined color (average of both): " + combinedColorHex);
            System.out.println("Top color: " + topColorHex);
        } catch (IOException e) {
            System.err.println("Error processing image: " + e.getMessage());
        }
    }

    /**
     * Calculates the average color of all pixels in an image.
     *
     * @param imagePath Path to the image file
     * @return Hex representation of the average color
     * @throws IOException If there is an error reading the image
     */
    public static String findAverageColor(String imagePath) throws IOException {
        // Load the image
        BufferedImage image = ImageIO.read(new File(imagePath));

        long totalRed = 0;
        long totalGreen = 0;
        long totalBlue = 0;
        int pixelCount = 0;

        int width = image.getWidth();
        int height = image.getHeight();

        // Sum up all RGB values
        for (int y = 0; y < height; y+=5) {
            for (int x = 0; x < width; x+=5) {
                int rgb = image.getRGB(x, y);

                // Skip fully transparent pixels if the image has an alpha channel
                if ((rgb >>> 24) == 0) {
                    continue;
                }

                // Extract RGB components
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                // Add to totals
                totalRed += red;
                totalGreen += green;
                totalBlue += blue;
                pixelCount++;
            }
        }

        // Calculate averages
        int avgRed = (int)(totalRed / pixelCount);
        int avgGreen = (int)(totalGreen / pixelCount);
        int avgBlue = (int)(totalBlue / pixelCount);

        // Combine into RGB value
        int averageRGB = (avgRed << 16) | (avgGreen << 8) | avgBlue;

        // Convert to hex
        return String.format("#%06X", averageRGB);
    }

    /**
     * Finds the most common color in an image.
     *
     * @param imagePath Path to the image file
     * @return Hex representation of the dominant color
     * @throws IOException If there is an error reading the image
     */
    public static String findDominantColor(String imagePath) throws IOException {
        // Load the image
        BufferedImage image = ImageIO.read(new File(imagePath));

        // Count color occurrences
        Map<Integer, Integer> colorCounts = new HashMap<>();

        int width = image.getWidth();
        int height = image.getHeight();

        // Sample every pixel to find color frequencies
        // For very large images, you might want to sample every nth pixel instead
        for (int y = 0; y < height; y+=5) {
            for (int x = 0; x < width; x+=5) {
                int rgb = image.getRGB(x, y);

                // Skip fully transparent pixels if the image has an alpha channel
                if ((rgb >>> 24) == 0) {
                    continue;
                }

                // Remove alpha channel for counting
                int rgbNoAlpha = rgb & 0x00FFFFFF;

                // Count this color
                colorCounts.put(rgbNoAlpha, colorCounts.getOrDefault(rgbNoAlpha, 0) + 1);
            }
        }

        // Find the most frequent color
        int dominantColor = 0;
        int maxCount = 0;

        for (Map.Entry<Integer, Integer> entry : colorCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                dominantColor = entry.getKey();
            }
        }

        // Convert to hex
        return String.format("#%06X", dominantColor);
    }

    /**
     * Combines two colors by averaging their RGB values.
     *
     * @param color1Hex First color in hex format (e.g., "#FF5733")
     * @param color2Hex Second color in hex format (e.g., "#33FF57")
     * @return Hex representation of the combined color
     */
    public static String combineColors(String color1Hex, String color2Hex) {
        // Parse the hex colors to integers
        int color1 = Integer.parseInt(color1Hex.substring(1), 16);
        int color2 = Integer.parseInt(color2Hex.substring(1), 16);

        // Extract RGB components for first color
        int red1 = (color1 >> 16) & 0xFF;
        int green1 = (color1 >> 8) & 0xFF;
        int blue1 = color1 & 0xFF;

        // Extract RGB components for second color
        int red2 = (color2 >> 16) & 0xFF;
        int green2 = (color2 >> 8) & 0xFF;
        int blue2 = color2 & 0xFF;

        // Average the components
        int avgRed = (red1 + red2) / 2;
        int avgGreen = (green1 + green2) / 2;
        int avgBlue = (blue1 + blue2) / 2;

        // Combine into RGB value
        int combinedRGB = (avgRed << 16) | (avgGreen << 8) | avgBlue;

        // Convert to hex
        return String.format("#%06X", combinedRGB);
    }

    /**
     * Calculates the average color of top 5 rows in an image.
     *
     * @param imagePath Path to the image file
     * @return Hex representation of the average color
     * @throws IOException If there is an error reading the image
     */
    public static String findDominantColorTop(String imagePath) throws IOException {
        // Load the image
        BufferedImage image = ImageIO.read(new File(imagePath));

        // Count color occurrences
        Map<Integer, Integer> colorCounts = new HashMap<>();

        int width = image.getWidth();
        int height = image.getHeight();

        // Sample every pixel to find color frequencies
        // For very large images, you might want to sample every nth pixel instead
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);

                // Skip fully transparent pixels if the image has an alpha channel
                if ((rgb >>> 24) == 0) {
                    continue;
                }

                // Remove alpha channel for counting
                int rgbNoAlpha = rgb & 0x00FFFFFF;

                // Count this color
                colorCounts.put(rgbNoAlpha, colorCounts.getOrDefault(rgbNoAlpha, 0) + 1);
            }
        }

        // Find the most frequent color
        int dominantColor = 0;
        int maxCount = 0;

        for (Map.Entry<Integer, Integer> entry : colorCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                dominantColor = entry.getKey();
            }
        }

        // Convert to hex
        return String.format("#%06X", dominantColor);
    }
}
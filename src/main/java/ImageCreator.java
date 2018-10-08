import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

class ImageCreator {

    void createBlackBlankImage(String outputImagePath, int imageWidth, int imageHeight) throws IOException {
        // creates output image
        BufferedImage outputImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);

        // scales the input image to the output image
        Graphics2D g2d = outputImage.createGraphics();

        g2d.drawImage(null, 0, 0, imageWidth, imageHeight, null);
        g2d.setBackground(Color.BLACK);
        g2d.dispose();

        // extracts extension of output file
        String formatName = outputImagePath.substring(outputImagePath.lastIndexOf(".") + 1);

        // writes to output file
        ImageIO.write(outputImage, formatName, new File(outputImagePath));
    }
}

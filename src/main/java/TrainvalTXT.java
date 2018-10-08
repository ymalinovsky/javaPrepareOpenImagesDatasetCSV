import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class TrainvalTXT {

    private Main main;

    TrainvalTXT(Main main) {
        this.main = main;
    }

    void prepareTrainvalTXT(List<Map<String, String>> trainvalTXTData) throws IOException {
        ImageCreator imageCreator = new ImageCreator();

        String trainvalTXTFilePath = main.PATH_TO_CSV_FILES_FOLDER + "annotations/trainval.txt";

        PrintWriter writer = new PrintWriter(trainvalTXTFilePath, "UTF-8");

        List<String> filenames = new ArrayList<String>();

        for (Map<String, String> trainvalTXT : trainvalTXTData) {
            String filename = trainvalTXT.get("filename");
            String baseFilename = FilenameUtils.getBaseName(filename);

            if (!filenames.contains(baseFilename)) {
                writer.println(baseFilename + " 1");
                filenames.add(baseFilename);

                String blackBlankImageFilePath = main.PATH_TO_CSV_FILES_FOLDER + "annotations/trimaps/" + baseFilename + ".png";

                String resizedImageFilePath = main.getFilePathToResizeImagesFolder(filename);
                BufferedImage bufferedImage = ImageIO.read(new File(resizedImageFilePath));
                Integer imageWidth = bufferedImage.getWidth();
                Integer imageHeight = bufferedImage.getHeight();

                imageCreator.createBlackBlankImage(blackBlankImageFilePath, imageWidth, imageHeight);
            }

        }

        writer.close();
    }
}

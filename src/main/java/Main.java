import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.imageio.*;

import org.apache.commons.csv.*;
import org.apache.commons.io.*;
import org.json.*;

public class Main {

    final String HUMAN_HAND_ID = "/m/0k65p";
    final String HUMAN_HAND_LABEL = "Human hand";
    private final String PATH_TO_ORIGIN_IMAGES_FOLDER = "/Volumes/Macintosh HD/Users/ymalinovsky/Documents/Finger/test6/data_origin/";
    private final String PATH_TO_RESIZE_IMAGES_FOLDER = "/Volumes/Macintosh HD/Users/ymalinovsky/Documents/Finger/test6/data/";
    private final String PATH_TO_CSV_FILES_FOLDER = "/Volumes/Macintosh HD/Users/ymalinovsky/Documents/Finger/test6/Open_Images_Datase/train/";

    public void main(String[] args) throws IOException {
        List<String> imageListWithHand = this.getImageListWithHand();

//        getTuricreateCSV(imageListWithHand);
        getTensorflowCSV(imageListWithHand);

//        resizeImages();

        System.out.println("ATATA!!!");
    }

    private static void getTensorflowCSV(List<String> imageListWithHand) throws IOException {

    }


    private void getTuricreateCSV(List<String> imageListWithHand) throws IOException {
        TuricreateCSV turicreateCSV = new TuricreateCSV(this);

        Map<String, Map<String, String>> turicreateData = turicreateCSV.getTuricreateImagesData(imageListWithHand);
        turicreateCSV.addAnnotationsToTuricreateData(turicreateData);
        turicreateCSV. saveTuricreateDataToCsvFile(turicreateData);
    }

    private List<String> getImageListWithHand() throws IOException {
        List<String> imageListWithHand = new ArrayList<String>();

        Reader in = new FileReader(this.getPathToCsvFilesFolder("imageLabels.csv"));
        Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
        for (CSVRecord record : records) {
            String imageID = record.get("ImageID");
            String source = record.get("Source");
            String labelName = record.get("LabelName");
            String confidence = record.get("Confidence");

            if (source.equals("verification") && confidence.equals("1") && labelName.equals(HUMAN_HAND_ID)) {
                imageListWithHand.add(imageID);
            }

        }

        return imageListWithHand;
    }

    private void resizeImages() {
        ImageResizer imageResizer = new ImageResizer();

        File[] files = new File(PATH_TO_ORIGIN_IMAGES_FOLDER).listFiles();

        for (File file : files) {
            if (file.isFile()) {
                String inputImagePath = this.getFilePathToImagesFolder(file.getName());
                String outputImagePath = this.getFilePathToResizeImagesFolder(file.getName());

                File resizeFile = new File(outputImagePath);
                if (!resizeFile.exists() && !resizeFile.isDirectory()) {
                    try {
                        BufferedImage bufferedImage = ImageIO.read(new File(file.getPath()));

                        int imageWidth = bufferedImage.getWidth();
                        int imageHeight = bufferedImage.getHeight();

                        int scaledWidth;
                        int scaledHeight;
                        if (imageWidth < imageHeight) {
                            scaledWidth = 416;
                            scaledHeight = (int) (imageHeight * (416.0 / imageWidth));
                        } else {
                            scaledHeight = 416;
                            scaledWidth = (int) (imageWidth * (416.0 / imageHeight));
                        }

                        imageResizer.resizeToSize(inputImagePath, outputImagePath, scaledWidth, scaledHeight);
                    } catch (Exception e) {
                        System.out.println(e.getLocalizedMessage());
                    }
                }
            }
        }
    }

    String getFilePathToImagesFolder(String filename) {
        return String.format("%s%s", PATH_TO_ORIGIN_IMAGES_FOLDER, filename);
    }

    String getFilePathToResizeImagesFolder(String filename) {
        return String.format("%s%s", PATH_TO_RESIZE_IMAGES_FOLDER, filename);
    }

    String getPathToCsvFilesFolder(String filename) {
        return String.format("%s%s", PATH_TO_CSV_FILES_FOLDER, filename);
    }

}

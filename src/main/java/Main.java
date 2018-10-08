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
    final String HUMAN_HAND_LABEL = "hand";
    private final String PATH_TO_ORIGIN_IMAGES_FOLDER = "/Volumes/Macintosh HD/Users/ymalinovsky/Documents/Finger/test6/data_origin/";
    private final String PATH_TO_RESIZE_IMAGES_FOLDER = "/Volumes/Macintosh HD/Users/ymalinovsky/Documents/Finger/test6/data/";
    final String PATH_TO_CSV_FILES_FOLDER = "/Volumes/Macintosh HD/Users/ymalinovsky/Documents/Finger/test6/Open_Images_Datase/train/";

    public static void main(String[] args) throws IOException {
        Main main = new Main();
        List<String> imageListWithHand = main.getImageListWithHand();

//        main.getTuricreateCSV(imageListWithHand);
//        main.getTensorflowCSV(imageListWithHand);
//        main.getLabelImgXMLs(imageListWithHand);
        main.getTrainvalTXT(imageListWithHand);
        
//        main.resizeImages();
    }

    private void getTrainvalTXT(List<String> imageListWithHand) throws IOException {
        TrainvalTXT trainvalTXT = new TrainvalTXT(this);

        List<Map<String, String>> existingHandImages = getExistingHandImages(imageListWithHand);
        List<Map<String, String>> existingHandImagesData = getExistingHandImagesData(existingHandImages);

        trainvalTXT.prepareTrainvalTXT(existingHandImagesData);

        System.out.println("DONE!");
    }

    private void getLabelImgXMLs(List<String> imageListWithHand) throws IOException {
        LabelImgXMLs labelImgXMLs = new LabelImgXMLs(this);

        List<Map<String, String>> existingHandImages = getExistingHandImages(imageListWithHand);
        List<Map<String, String>> existingHandImagesData = getExistingHandImagesData(existingHandImages);

        labelImgXMLs.prepareLabelImgXMLs(existingHandImagesData);

        System.out.println("DONE!");
    }

    private void getTensorflowCSV(List<String> imageListWithHand) throws IOException {
        TensorflowCSV tensorflowCSV = new TensorflowCSV(this);

        List<Map<String, String>> existingHandImages = getExistingHandImages(imageListWithHand);
        List<Map<String, String>> existingHandImagesData = getExistingHandImagesData(existingHandImages);

        tensorflowCSV.saveTensorflowDataToCsvFile(existingHandImagesData);

        System.out.println("DONE!");
    }

    private void getTuricreateCSV(List<String> imageListWithHand) throws IOException {
        TuricreateCSV turicreateCSV = new TuricreateCSV(this);

        Map<String, Map<String, String>> turicreateData = turicreateCSV.getTuricreateImagesData(imageListWithHand);
        turicreateCSV.addAnnotationsToTuricreateData(turicreateData);
        turicreateCSV.saveTuricreateDataToCsvFile(turicreateData);

        System.out.println("DONE!");
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

    List<Map<String, String>> getExistingHandImages(List<String> imageListWithHand) throws IOException {
        List<Map<String, String>> imagesData = new ArrayList<Map<String, String>>();

        Reader in = new FileReader(getPathToCsvFilesFolder("imageIDs.csv"));
        Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);

        for (CSVRecord record : records) {
            String imageID = record.get("ImageID");

            if (imageListWithHand.contains(imageID)) {
                String originalURL = record.get("OriginalURL");

                String filename = getFilePathAndSaveFileToFolderImages(originalURL);

                if (filename != null) {
                    Map<String, String> imageData = new HashMap<String, String>();
                    imageData.put("filename", String.format("%s", filename));
                    imageData.put("ImageID", imageID);

                    imagesData.add(imageData);

                    System.out.println("" + imagesData.size() + ": " + filename);
                }
            }

            // tmp test logic
//            if (imagesData.size() == 1) {
//                break;
//            }
        }


        return imagesData;
    }

    List<Map<String, String>> getExistingHandImagesData(List<Map<String, String>> imagesData) throws IOException {
        List<Map<String, String>> existingHandImagesData = new ArrayList<Map<String, String>>();

        Map<String, List<Map<String, String>>> boxesImageData = getBoxesImageData();

        for (Map<String, String> imageData : imagesData) {
            String imageID = imageData.get("ImageID");

            List<Map<String, String>> boxesData = boxesImageData.get(imageID);
            if (boxesData != null) {
                for (Map<String, String> boxData : boxesData) {

                    Double xMin = Double.parseDouble(boxData.get("XMin"));
                    Double xMax = Double.parseDouble(boxData.get("XMax"));
                    Double yMin = Double.parseDouble(boxData.get("YMin"));
                    Double yMax = Double.parseDouble(boxData.get("YMax"));

                    String filename = imageData.get("filename");
                    String filePath = getFilePathToResizeImagesFolder(filename);

                    BufferedImage bufferedImage = ImageIO.read(new File(filePath));
                    Integer imageWidth = bufferedImage.getWidth();
                    Integer imageHeight = bufferedImage.getHeight();

                    Integer xmin = (int)(xMin * imageWidth);
                    Integer xmax = (int)(xMax * imageWidth);
                    Integer ymin = (int)(yMin * imageHeight);
                    Integer ymax = (int)(yMax * imageHeight);

                    Map<String, String> existingImageData = new HashMap<String, String>();
                    existingImageData.put("filename", filename);
                    existingImageData.put("width", imageWidth.toString());
                    existingImageData.put("height", imageHeight.toString());
                    existingImageData.put("class", HUMAN_HAND_LABEL);
                    existingImageData.put("xmin", xmin.toString());
                    existingImageData.put("xmax", xmax.toString());
                    existingImageData.put("ymin", ymin.toString());
                    existingImageData.put("ymax", ymax.toString());

                    existingHandImagesData.add(existingImageData);
                }
            }
        }

        return existingHandImagesData;
    }

    String getFilePathAndSaveFileToFolderImages(String imageStringURL) throws IOException {
        String filename = FilenameUtils.getName(imageStringURL);
        String filePath = getFilePathToImagesFolder(filename);

        File file = new File(filePath);
        if (file.exists() && !file.isDirectory()) {
            return filename;
        }

        InputStream in = new URL(imageStringURL).openStream();
        try {
            byte[] fileByte = IOUtils.toByteArray(in);

            if (fileByte.length < 5000) {
                return null;
            } else {
                FileOutputStream out = new FileOutputStream(filePath);
                out.write(fileByte);
                out.close();
            }

            return filename;
        } finally {
            in.close();
        }
    }

    Map<String, List<Map<String, String>>> getBoxesImageData() throws IOException {
        Map<String, List<Map<String, String>>> boxesImageData = new HashMap<String, List<Map<String, String>>>();

        Reader in = new FileReader(getPathToCsvFilesFolder("boxes.csv"));
        Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
        for (CSVRecord record : records) {
            String labelName = record.get("LabelName");
            String isOccluded = record.get("IsOccluded");

            if (labelName.equals(HUMAN_HAND_ID) && isOccluded.equals("0")) {
                String imageID = record.get("ImageID");

                List<Map<String, String>> boxesData = boxesImageData.get(imageID);
                if (boxesData == null) {
                    boxesData = new ArrayList<Map<String, String>>();
                }

                Map<String, String> boxImageData = new HashMap<String, String>();
                boxImageData.put("XMin", record.get("XMin"));
                boxImageData.put("XMax", record.get("XMax"));
                boxImageData.put("YMin", record.get("YMin"));
                boxImageData.put("YMax", record.get("YMax"));

                boxesData.add(boxImageData);

                boxesImageData.put(imageID, boxesData);
            }
        }

        return boxesImageData;
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

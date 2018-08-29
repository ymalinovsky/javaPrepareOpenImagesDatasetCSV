import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.imageio.*;

import org.apache.commons.csv.*;
import org.apache.commons.io.*;
import org.json.*;

public class Main {

    private final static String HUMAN_HAND_ID = "/m/0k65p";
    private final static String HUMAN_HAND_LABEL = "Human hand";
    private final static String PATH_TO_IMAGES_FOLDER = "/Users/artem/Downloads/Open_Images_Datase/train/data/";

    public static void main(String[] args) throws IOException {
        ArrayList<String> imageListWithHand = getImageListWithHand();
        Map<String, Map<String, String>> turicreateData = getTuricreateImagesData(imageListWithHand);
//        addAnnotationsToTuricreateData(turicreateData);

        System.out.println("ATATA!!!");
    }

    private static ArrayList<String> getImageListWithHand() throws IOException {
        ArrayList<String> imageListWithHand = new ArrayList<String>();

        Reader in = new FileReader("/Users/artem/Downloads/Open_Images_Datase/train/imageLabels.csv");
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

    private static Map<String, Map<String, String>> getTuricreateImagesData(ArrayList<String> imageListWithHand) throws IOException {
        Map<String, Map<String, String>> turicreateImagesData = new HashMap<String, Map<String, String>>();

        Reader in = new FileReader("/Users/artem/Downloads/Open_Images_Datase/train/imageIDs.csv");
        Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
        for (CSVRecord record : records) {
            String imageID = record.get("ImageID");
            String originalURL = record.get("OriginalURL");

            for (String openImageWithHandID : imageListWithHand) {
                if (openImageWithHandID.equals(imageID)) {
                    String filename = getFilePathAndSaveFileToFolderImages(originalURL);

                    if (filename != null) {
                        Map<String, String> turicreateImageData = new HashMap<String, String>();
                        turicreateImageData.put("path", String.format("%s%s", "data/", filename));

                        turicreateImagesData.put(openImageWithHandID, turicreateImageData);
                    }
                }
            }
        }


        return turicreateImagesData;
    }

    private static String getFilePathAndSaveFileToFolderImages(String imageStringURL) throws IOException {
        InputStream in = new URL(imageStringURL).openStream();
        try {
            String filename = FilenameUtils.getName(imageStringURL);
            String filePath = String.format("%s%s", PATH_TO_IMAGES_FOLDER, filename);

            File file = new File(filePath);
            if (!file.exists() && !file.isDirectory()) {
                byte[] fileByte = IOUtils.toByteArray(in);

                if (fileByte.length < 5000) {
                    return null;
                } else {
                    FileOutputStream out = new FileOutputStream(filePath);
                    out.write(fileByte);
                    out.close();
                }
            }

            return filename;
        } finally {
            in.close();
        }
    }

    private static void addAnnotationsToTuricreateData(Map<String, Map<String, String>> turicreateImagesData) throws IOException {
        for (Map.Entry<String, Map<String, String>> entry : turicreateImagesData.entrySet()) {
            String imageID = entry.getKey();
            Map<String, String> turicreateData = entry.getValue();

            Reader in = new FileReader("/Users/artem/Downloads/Open_Images_Datase/train/boxes.csv");
            Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
            for (CSVRecord record : records) {
                String labelName = record.get("LabelName");

                if (imageID.equals(record.get("ImageID")) && labelName.equals(HUMAN_HAND_ID)) {
                    Double xMin = Double.parseDouble(record.get("XMin"));
                    Double xMax = Double.parseDouble(record.get("XMax"));
                    Double yMin = Double.parseDouble(record.get("YMin"));
                    Double yMax = Double.parseDouble(record.get("YMax"));

                    String filePath = turicreateData.get("path");

                    BufferedImage bufferedImage = ImageIO.read(new File(filePath));
                    Integer imageWidth = bufferedImage.getWidth();
                    Integer imageHeight = bufferedImage.getHeight();

                    Integer height = (int) ((yMax - yMin) * imageHeight);
                    Integer width = (int) ((xMax - xMin) * imageWidth);
                    Integer x = (int) (((xMax - xMin) / 2) * imageWidth);
                    Integer y = (int) (((yMax - yMin) / 2) * imageHeight);

                    JSONObject coordinatesJSON = new JSONObject();
                    coordinatesJSON.put("height", height);
                    coordinatesJSON.put("width", width);
                    coordinatesJSON.put("x", x);
                    coordinatesJSON.put("y", y);

                    JSONObject labelJSON = new JSONObject();
                    labelJSON.put("label", HUMAN_HAND_LABEL);

                    JSONArray annotationJSON = new JSONArray();
                    annotationJSON.put(coordinatesJSON);
                    annotationJSON.put(labelJSON);

                    String annotations = turicreateData.get("annotations");
                    JSONArray annotationsJSON = new JSONArray();
                    if (annotations != null) {
                        annotationsJSON = new JSONArray(annotations);
                    }

                    annotationsJSON.put(annotationJSON);

                    turicreateData.put("annotations", annotationsJSON.toString());
                }
            }
        }
    }
}
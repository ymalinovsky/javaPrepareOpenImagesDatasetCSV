import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.commons.csv.*;
import org.apache.commons.io.*;

public class Main {

    private final static String HUMAN_HAND_ID = "/m/0k65p";
    private final static String PATH_TO_IMAGES_FOLDER = "/Users/artem/Downloads/Open_Images_Datase/train/data/";

    public static void main(String[] args) throws IOException {
        ArrayList<String> imageListWithHand = getImageListWithHand();
        Map<String, Map> turicreateImagesData = getTuricreateImagesData(imageListWithHand);

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

    private static Map<String, Map> getTuricreateImagesData(ArrayList<String> imageListWithHand) throws IOException {
        Map<String, Map> turicreateImagesData = new HashMap<String, Map>();


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
}

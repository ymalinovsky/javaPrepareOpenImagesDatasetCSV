import java.io.*;
import java.net.*;
import java.util.*;

import org.apache.commons.csv.*;
import org.apache.commons.io.*;

public class Main {

    private final static String HUMAN_HAND_ID = "/m/0k65p";
    private final static String PATH_TO_IMAGES_FOLDER = "/Users/artem/Downloads/Open_Images_Datase/train/";

    public static void main(String[] args) throws IOException {
        ArrayList<String> imagesListWithHand = getImagesListWithHand();

        System.out.println("ATATA!!!");
    }

    private static ArrayList<String> getImagesListWithHand() throws IOException {
        ArrayList<String> imagesListWithHand = new ArrayList<String>();

        Reader in = new FileReader("/Users/artem/Downloads/Open_Images_Datase/train/imageLabels.csv");
        Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
        for (CSVRecord record : records) {
            String imageID = record.get("ImageID");
            String source = record.get("Source");
            String labelName = record.get("LabelName");
            String confidence = record.get("Confidence");

            if (source.equals("verification") && confidence.equals("1") && labelName.equals(HUMAN_HAND_ID)) {
                imagesListWithHand.add(imageID);
            }

        }

        return imagesListWithHand;
    }

    private static void saveFileToFolderImages(String imageStringURL) throws IOException {

        InputStream in = new URL(imageStringURL).openStream();
        try {
            String filename = FilenameUtils.getName(imageStringURL);

            FileOutputStream out = new FileOutputStream(String.format("%s%s", PATH_TO_IMAGES_FOLDER, filename));
            out.write(IOUtils.toByteArray(in));
            out.close();
        } finally {
            in.close();
        }
    }
}

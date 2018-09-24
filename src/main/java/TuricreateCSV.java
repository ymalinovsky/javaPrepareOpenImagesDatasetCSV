import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.imageio.*;

import org.apache.commons.csv.*;
import org.apache.commons.io.*;
import org.json.*;

class TuricreateCSV {

    private Main main;

    TuricreateCSV(Main main) {
        this.main = main;
    }

    Map<String, Map<String, String>> getTuricreateImagesData(List<String> imageListWithHand) throws IOException {
        Map<String, Map<String, String>> turicreateImagesData = new HashMap<String, Map<String, String>>();

        Reader in = new FileReader(main.getPathToCsvFilesFolder("imageIDs.csv"));
        Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);

        for (CSVRecord record : records) {
            String imageID = record.get("ImageID");

            if (imageListWithHand.contains(imageID)) {
                String originalURL = record.get("OriginalURL");

                String filename = getFilePathAndSaveFileToFolderImages(originalURL);

                if (filename != null) {
                    Map<String, String> turicreateImageData = new HashMap<String, String>();
                    turicreateImageData.put("path", String.format("%s%s", "data/", filename));

                    turicreateImagesData.put(imageID, turicreateImageData);

                    System.out.println("" + turicreateImagesData.size() + ": " + filename);
                }
            }

            // tmp test logic
//            if (turicreateImagesData.size() == 3500) {
//                break;
//            }
        }


        return turicreateImagesData;
    }

    private String getFilePathAndSaveFileToFolderImages(String imageStringURL) throws IOException {
        InputStream in = new URL(imageStringURL).openStream();
        try {
            String filename = FilenameUtils.getName(imageStringURL);
            String filePath = main.getFilePathToImagesFolder(filename);

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

    void addAnnotationsToTuricreateData(Map<String, Map<String, String>> turicreateImagesData) throws IOException {
        Map<String, List<Map<String, String>>> boxesImageData = getBoxesImageData();

        for (Map.Entry<String, Map<String, String>> entry : turicreateImagesData.entrySet()) {
            String imageID = entry.getKey();

            List<Map<String, String>> boxesData = boxesImageData.get(imageID);
            if (boxesData != null) {
                for (Map<String, String> boxData : boxesData) {
                    Map<String, String> turicreateData = entry.getValue();

                    Double xMin = Double.parseDouble(boxData.get("XMin"));
                    Double xMax = Double.parseDouble(boxData.get("XMax"));
                    Double yMin = Double.parseDouble(boxData.get("YMin"));
                    Double yMax = Double.parseDouble(boxData.get("YMax"));

                    String filename = FilenameUtils.getName(turicreateData.get("path"));
                    String filePath = main.getFilePathToResizeImagesFolder(filename);

                    BufferedImage bufferedImage = ImageIO.read(new File(filePath));
                    Integer imageWidth = bufferedImage.getWidth();
                    Integer imageHeight = bufferedImage.getHeight();

                    Integer height = (int) ((yMax - yMin) * imageHeight);
                    Integer width = (int) ((xMax - xMin) * imageWidth);
                    Integer x = (int) ((((xMax - xMin) / 2) + xMin) * imageWidth);
                    Integer y = (int) ((((yMax - yMin) / 2) + yMin) * imageHeight);

                    JSONObject coordinatesJSON = new JSONObject();
                    coordinatesJSON.put("height", height);
                    coordinatesJSON.put("width", width);
                    coordinatesJSON.put("x", x);
                    coordinatesJSON.put("y", y);

                    JSONObject annotationJSON = new JSONObject();
                    annotationJSON.put("coordinates", coordinatesJSON);
                    annotationJSON.put("label", main.HUMAN_HAND_LABEL);

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

    private Map<String, List<Map<String, String>>> getBoxesImageData() throws IOException {
        Map<String, List<Map<String, String>>> boxesImageData = new HashMap<String, List<Map<String, String>>>();

        Reader in = new FileReader(main.getPathToCsvFilesFolder("boxes.csv"));
        Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);
        for (CSVRecord record : records) {
            String labelName = record.get("LabelName");
            String isOccluded = record.get("IsOccluded");

            if (labelName.equals(main.HUMAN_HAND_ID) && isOccluded.equals("0")) {
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

    void saveTuricreateDataToCsvFile(Map<String, Map<String, String>> turicreateData) throws IOException {

        BufferedWriter writer = new BufferedWriter(new FileWriter(main.getPathToCsvFilesFolder("annotations.csv")));

        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                .withHeader("path", "annotations"));

        for (Map.Entry<String, Map<String, String>> entry : turicreateData.entrySet()) {
            Map<String, String> turicreateImageData = entry.getValue();

            String path = turicreateImageData.get("path");
            String annotations = turicreateImageData.get("annotations");

            if (path != null && annotations != null) {
                csvPrinter.printRecord(path, annotations);
            }
        }

        csvPrinter.flush();
    }

}

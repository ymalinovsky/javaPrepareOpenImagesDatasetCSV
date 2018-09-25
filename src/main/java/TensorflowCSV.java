import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.imageio.*;

import org.apache.commons.csv.*;
import org.apache.commons.io.*;
import org.json.*;

class TensorflowCSV {

    private Main main;

    TensorflowCSV(Main main) {
        this.main = main;
    }

    List<Map<String, String>> getTensorflowImagesData(List<String> imageListWithHand) throws IOException {
        List<Map<String, String>> tensorflowImagesData = new ArrayList<Map<String, String>>();

        Reader in = new FileReader(main.getPathToCsvFilesFolder("imageIDs.csv"));
        Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in);

        for (CSVRecord record : records) {
            String imageID = record.get("ImageID");

            if (imageListWithHand.contains(imageID)) {
                String originalURL = record.get("OriginalURL");

                String filename = main.getFilePathAndSaveFileToFolderImages(originalURL);

                if (filename != null) {
                    Map<String, String> tensorflowImageData = new HashMap<String, String>();
                    tensorflowImageData.put("filename", String.format("%s", filename));
                    tensorflowImageData.put("ImageID", imageID);

                    tensorflowImagesData.add(tensorflowImageData);

                    System.out.println("" + tensorflowImagesData.size() + ": " + filename);
                }
            }

            // tmp test logic
//            if (tensorflowImagesData.size() == 1000) {
//                break;
//            }
        }


        return tensorflowImagesData;
    }

    List<Map<String, String>> getTensorflowData(List<Map<String, String>> tensorflowImagesData) throws IOException {
        List<Map<String, String>> tensorflowData = new ArrayList<Map<String, String>>();

        Map<String, List<Map<String, String>>> boxesImageData = main.getBoxesImageData();

        for (Map<String, String> tensorflowImageData : tensorflowImagesData) {
            String imageID = tensorflowImageData.get("ImageID");

            List<Map<String, String>> boxesData = boxesImageData.get(imageID);
            if (boxesData != null) {
                for (Map<String, String> boxData : boxesData) {

                    Double xMin = Double.parseDouble(boxData.get("XMin"));
                    Double xMax = Double.parseDouble(boxData.get("XMax"));
                    Double yMin = Double.parseDouble(boxData.get("YMin"));
                    Double yMax = Double.parseDouble(boxData.get("YMax"));

                    String filename = tensorflowImageData.get("filename");
                    String filePath = main.getFilePathToResizeImagesFolder(filename);

                    BufferedImage bufferedImage = ImageIO.read(new File(filePath));
                    Integer imageWidth = bufferedImage.getWidth();
                    Integer imageHeight = bufferedImage.getHeight();

                    Integer xmin = (int)(xMin * imageWidth);
                    Integer xmax = (int)(xMax * imageWidth);
                    Integer ymin = (int)(yMin * imageHeight);
                    Integer ymax = (int)(yMax * imageHeight);


                    Map<String, String> imageData = new HashMap<String, String>();
                    imageData.put("filename", filename);
                    imageData.put("width", imageWidth.toString());
                    imageData.put("height", imageHeight.toString());
                    imageData.put("class", main.HUMAN_HAND_LABEL);
                    imageData.put("xmin", xmin.toString());
                    imageData.put("xmax", xmax.toString());
                    imageData.put("ymin", ymin.toString());
                    imageData.put("ymax", ymax.toString());

                    tensorflowData.add(imageData);
                }
            }
        }

        return tensorflowData;
    }

    void saveTensorflowDataToCsvFile(List<Map<String, String>> tensorflowData) throws IOException {

        BufferedWriter writer = new BufferedWriter(new FileWriter(main.getPathToCsvFilesFolder("tensorflow_annotations.csv")));

        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("filename", "width", "height", "class", "xmin", "xmax", "ymin", "ymax"));

        for (Map<String, String>tensorflowImageData : tensorflowData) {
            String filename = tensorflowImageData.get("filename");
            String width = tensorflowImageData.get("width");
            String height = tensorflowImageData.get("height");
            String classDescription = tensorflowImageData.get("class");
            String xmin = tensorflowImageData.get("xmin");
            String xmax = tensorflowImageData.get("xmax");
            String ymin = tensorflowImageData.get("ymin");
            String ymax = tensorflowImageData.get("ymax");

            if (filename != null && width != null && height != null && classDescription != null && xmin != null && xmax != null && ymin != null && ymax != null) {
                csvPrinter.printRecord(filename, width, height, classDescription, xmin, xmax, ymin, ymax);
            }
        }

        csvPrinter.flush();
    }

}

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

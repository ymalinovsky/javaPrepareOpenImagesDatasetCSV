import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TrainvalTXT {

    private Main main;

    public TrainvalTXT(Main main) {
        this.main = main;
    }

    void prepareTrainvalTXT(List<Map<String, String>> trainvalTXTData) throws IOException {
        String trainvalTXTFilePath = main.PATH_TO_CSV_FILES_FOLDER + "annotations/trainval.txt";

        PrintWriter writer = new PrintWriter(trainvalTXTFilePath, "UTF-8");

        List<String> filenames = new ArrayList<String>();

        for (Map<String, String> trainvalTXT : trainvalTXTData) {
            String filename = FilenameUtils.getBaseName(trainvalTXT.get("filename"));

            if (!filenames.contains(filename)) {
                writer.println(filename + " 1 1 1");
                filenames.add(filename);
            }

        }

        writer.close();
    }
}

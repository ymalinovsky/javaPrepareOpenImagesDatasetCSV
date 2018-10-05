import java.util.List;
import java.util.Map;

public class TrainvalTXT {

    private Main main;

    public TrainvalTXT(Main main) {
        this.main = main;
    }

    void prepareTrainvalTXT(List<Map<String, String>> trainvalTXTData) {
        for (Map<String, String> trainvalTXT : trainvalTXTData) {
            String filename = trainvalTXT.get("filename");


        }
    }
}

import data.DataProcessor;
import models.LstmModel;
import models.Word2Vec;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class Train {
    public static void main(String[] args) {
        String resourcePath = Paths.get("src", "main", "resources").toString();

        String trueRecordsPath = "";
        String fakeRecordsPath = "";
        trueRecordsPath = Paths.get(resourcePath, "True.csv").toString();
        fakeRecordsPath = Paths.get(resourcePath, "Fake.csv").toString();

        List<List<String>> trueRecords = DataProcessor.getRecordsFromCSV(trueRecordsPath);
        List<List<String>> fakeRecords = DataProcessor.getRecordsFromCSV(fakeRecordsPath);
        int minLengthOfRecords = Math.min(trueRecords.size(), fakeRecords.size());

        String dataPath = Paths.get(resourcePath, "data").toString();
        DataProcessor.processRecords(trueRecords, minLengthOfRecords, dataPath, true);
        DataProcessor.processRecords(fakeRecords, minLengthOfRecords, dataPath, false);

        try {
            Word2Vec.downloadWord2VecModel();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LstmModel model = new LstmModel(
               dataPath,
                (Paths.get(dataPath, "categories.txt")).toString(),
                Word2Vec.getWordVectorsPath(),
                300,
               0);

        model.train(64, 1, Paths.get(resourcePath, "Model.net").toString());
    }
}

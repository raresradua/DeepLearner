package data;

import au.com.bytecode.opencsv.CSVReader;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 *  These functions should be used to process the .csv in labeled .txt files
 *  The model trains on these .txt files
 *
 * @author Vlad Cociorva & Rares Radu
 */
public class DataProcessor {
    /**
     * @param path of the .csv file from where to extract lines
     * @return list of every column from every row in the csv
     */
    public static List<List<String>> getRecordsFromCSV(String path){
        List<List<String>> records = new ArrayList<List<String>>();
        try (CSVReader csvReader = new CSVReader(new FileReader(path));) {
            String[] values = null;
            while ((values = csvReader.readNext()) != null) {
                records.add(Arrays.asList(values));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return records;
    }

    /**
     * @param records list of every column from every row in the csv
     * @param size max number of rows to take for each csv
     * @param pathToSave path where to save the processed dataset
     * @param isTrue label of the dataset row
     */
    public static void processRecords(List<List<String>> records, int size, String pathToSave, boolean isTrue) {
        try {
            FileWriter writer;
            FileWriter writer_test;
            Files.createDirectories(Paths.get(pathToSave, "train"));
            Files.createDirectories(Paths.get(pathToSave, "test"));
            if (isTrue) {
                writer = new FileWriter(Paths.get(pathToSave, "train/1.txt").toString(), false);
                writer_test = new FileWriter(Paths.get(pathToSave, "test/1.txt").toString(), false);
            } else {
                writer = new FileWriter(Paths.get(pathToSave, "train/0.txt").toString(), false);
                writer_test = new FileWriter(Paths.get(pathToSave,"test/0.txt").toString(), false);
            }
            int i = 0;
            for (List<String> r : records) {
                if (i >= size){
                    break;
                }
                if (i == 0) {
                    i++;
                    continue;
                }
                if (i <= size * 0.8) {
                    String tmp = (r.get(0) + r.get(1)).toLowerCase();
                    tmp = tmp.replaceAll("[^a-zA-Z0-9 ]", " ");
                    writer.write(tmp);
                    writer.write("\n");
                } else {
                    String tmp = (r.get(0) + r.get(1)).toLowerCase();
                    tmp = tmp.replaceAll("[^a-zA-Z0-9 ]", " ");
                    writer_test.write(tmp);
                    writer_test.write("\n");
                }
                i++;
            }
            writer.close();
            writer_test.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

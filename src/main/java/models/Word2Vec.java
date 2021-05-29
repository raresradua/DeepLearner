package models;

import org.apache.commons.io.FilenameUtils;
import org.nd4j.common.resources.Downloader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

public class Word2Vec {

    private static String wordVectorsPath;

    public static void downloadWord2VecModel() throws IOException {
        String userHomePath = System.getProperty("user.home");

        String defaultWordVectorsPath = FilenameUtils.concat(userHomePath, "dl4j/word2vec300");
        String word2vecMD5 = "1c892c4707a8a1a508b01a01735c0339";

        Word2Vec.setWordVectorsPath(
                new File(defaultWordVectorsPath, "GoogleNews-vectors-negative300.bin.gz").getAbsolutePath()
        );

        if (new File(wordVectorsPath).exists()) {
            System.out.println("\n\tGoogleNews-vectors-negative300.bin.gz file found at path: " + defaultWordVectorsPath);
            System.out.println("\tChecking md5 of existing file..");

            if (Downloader.checkMD5OfFile(word2vecMD5, new File(wordVectorsPath))) {
                System.out.println("\tExisting file hash matches.");
                return;
            } else {
                System.out.println("\tExisting file hash doesn't match. Retrying download...");
            }
        }

        System.out.println("\tWARNING: GoogleNews-vectors-negative300.bin.gz is a 1.5GB file.");
        System.out.println("\tPress \"ENTER\" to start a download of GoogleNews-vectors-negative300.bin.gz to " + defaultWordVectorsPath);

        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();

        System.out.println("Starting model download (1.5GB!)...");
        Downloader.download("Word2Vec", new URL(
                "https://dl4jdata.blob.core.windows.net/resources/wordvectors/GoogleNews-vectors-negative300.bin.gz"),
                new File(wordVectorsPath), word2vecMD5, 5);
        System.out.println("Successfully downloaded word2vec model to " + wordVectorsPath);
    }

    public static String getWordVectorsPath() {
        return wordVectorsPath;
    }

    public static void setWordVectorsPath(String path) {
        wordVectorsPath = path;
    }

}

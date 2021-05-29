package models;

import data.Iterator;
import data.TextProcessor;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.LSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.InvocationType;
import org.deeplearning4j.optimize.listeners.EvaluativeListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Model used to classify fake news, following LSTM architecture
 * @author Vlad Cociorva & Rares Radu
 */
public class LstmModel {
    private MultiLayerNetwork network;
    private String dataPath = "";
    private TextProcessor textProcessor;
    private List<String> categories;

    /**
     * @param dataPath root path of the data folders (train and test)
     * @param categoriesPath path of the categories file (0, false and 1, true)
     * @param wordVectorsPath path of the Word2Vec model
     * @param vectorSize number of dimensions of the Word2Vec model
     * @param seed seed to be passed to the model for randomness
     */
    public LstmModel(String dataPath, String categoriesPath, String wordVectorsPath, int vectorSize, int seed) {
        Nd4j.getMemoryManager().setAutoGcWindow(10000);

        // verificare daca path-urile sunt valide
        this.dataPath = dataPath;
        this.textProcessor = new TextProcessor(wordVectorsPath);

        MultiLayerConfiguration configuration = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .updater(new Adam(5e-3))
                .l2(1e-5)
                .weightInit(WeightInit.XAVIER)
                .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
                .gradientNormalizationThreshold(1.0)
                .list()
                .layer(new LSTM.Builder().nIn(vectorSize).nOut(256)
                        .activation(Activation.TANH).build())
                .layer(new RnnOutputLayer.Builder().activation(Activation.SOFTMAX)
                        .lossFunction(LossFunctions.LossFunction.MCXENT).nIn(256).nOut(2).build())
                .build();

        network = new MultiLayerNetwork(configuration);
        network.init();

        loadCategories(categoriesPath);
    }

    public LstmModel(String wordVectorsPath) {
        Nd4j.getMemoryManager().setAutoGcWindow(10000);
        this.textProcessor = new TextProcessor(wordVectorsPath);
    }

    public TextProcessor getTextProcessor() {
        return textProcessor;
    }

    /**
     * @param batchSize batchSize to use for training the model
     * @param epochsNo number of epochs to use for training the model
     * @param savePath path where to save the trained model
     */
    public void train(int batchSize, int epochsNo, String savePath){
        Iterator trainIterator = new Iterator.Builder()
                .dataDirectory(this.dataPath)
                .textProcessor(textProcessor)
                .batchSize(batchSize)
                .truncateLength(256)
                .train(true)
                .build();

        Iterator testIterator = new Iterator.Builder()
                .dataDirectory(this.dataPath)
                .textProcessor(textProcessor)
                .batchSize(batchSize)
                .truncateLength(256)
                .train(false)
                .build();

        System.out.println("Training started...");
        network.setListeners(new ScoreIterationListener(1),
                new EvaluativeListener(testIterator, 1, InvocationType.EPOCH_END));

        network.fit(trainIterator, epochsNo);
        System.out.println("Training ended. Evaluation: ");

        Evaluation eval = network.evaluate(testIterator);
        System.out.println(eval.stats());

        try {
            network.save(new File(savePath), true);
            System.out.println("Successfully saved trained model to: " + savePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param text text to classify
     * @return true or fake
     */
    public String predict(String text) {
        DataSet processedText = textProcessor.vectorizeText(text, 256);
        INDArray features = processedText.getFeatures();
        INDArray prediction = network.output(features, false);

        long[] predictionShape = prediction.shape();
        double max = 0;
        int pos = 0;
        for (int i = 0; i < predictionShape[1]; i++){
            if (max < (double) prediction.slice(0).getRow(i).sumNumber()) {
                max = (double) prediction.slice(0).getRow(i).sumNumber();
                pos = i;
            }
        }

        return categories.get(pos).split(",")[1];
    }

    /**
     * @param modelPath path to load the model from
     */
    public void loadModel(String modelPath) {
        try {
            this.network = MultiLayerNetwork.load(new File(modelPath), true);
            System.out.println("Successfully loaded model from: " + modelPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param categoriesPath path to load the categories from
     */
    public void loadCategories(String categoriesPath){
        File categoriesFile = new File(categoriesPath);
        try (BufferedReader brCategories = new BufferedReader(new FileReader(categoriesFile))){
            String temp;
            categories = new ArrayList<>();
            while ((temp = brCategories.readLine()) != null) {
                categories.add(temp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

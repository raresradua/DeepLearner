package data;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.nd4j.linalg.indexing.NDArrayIndex.all;
import static org.nd4j.linalg.indexing.NDArrayIndex.point;

public class Iterator implements DataSetIterator {
    private final int batchSize;
    private final int vectorSize;
    private final int truncateLength;
    private int maxLength;
    private final String dataDirectory;
    private final List<Pair<String, List<String>>> categoryData = new ArrayList<>();
    private int cursor = 0;
    private int totalNews = 0;
    private final TextProcessor textProcessor;
    private int newsPosition = 0;
    private final List<String> labels;
    private int currCategory = 0;

    /**
     * @param dataDirectory  the directory of the news headlines data set
     * @param textProcessor  TextProcessor object
     * @param batchSize      Size of each minibatch for training
     * @param truncateLength If headline length exceed this size, it will be truncated to this size.
     * @param train          If true: return the training data. If false: return the testing data.
     *                       <p>
     *                       - initialize various class variables
     *                       - calls populateData function to load news data in categoryData vector
     *                       - also populates labels (i.e. category related inforamtion) in labels class variable
     */

    private Iterator(String dataDirectory,
                         int batchSize,
                         int truncateLength,
                         boolean train,
                         TextProcessor textProcessor) {
        this.textProcessor = textProcessor;
        this.dataDirectory = dataDirectory;
        this.batchSize = batchSize;
        this.vectorSize = textProcessor.word2Vec.getWordVector(
                textProcessor.word2Vec.vocab().wordAtIndex(0)).length;
        this.truncateLength = truncateLength;
        this.populateData(train);
        this.labels = new ArrayList<>();
        for (Pair<String, List<String>> categoryDatum : this.categoryData) {
            this.labels.add(categoryDatum.getKey().split(",")[1]);
        }
    }

    public static Builder Builder() {
        return new Builder();
    }

    @Override
    public DataSet next(int num) {
        if (cursor >= this.totalNews) throw new NoSuchElementException();
        return nextDataSet(num);
    }

    private DataSet nextDataSet(int num) {
        // Loads news into news list from categoryData List along with category of each news
        List<String> news = new ArrayList<>(num);
        int[] category = new int[num];
        for (int i = 0; i < num && cursor < this.totalNews; i++) {
            if (currCategory < categoryData.size()) {
                news.add(this.categoryData.get(currCategory).getValue().get(newsPosition));
                category[i] = Integer.parseInt(this.categoryData.get(currCategory).getKey().split(",")[0]);
                currCategory++;
                cursor++;
            } else {
                currCategory = 0;
                newsPosition++;
                i--;
            }
        }

        return textProcessor.vectorizeText(news, category, truncateLength);
    }

    /**
     * Used post training to load a review from a file to a features INDArray that can be passed to the network output method
     *
     * @param file      File to load the review from
     * @param maxLength Maximum length (if review is longer than this: truncate to maxLength). Use Integer.MAX_VALUE to not nruncate
     * @return Features array
     * @throws IOException If file cannot be read
     */
    public INDArray loadFeaturesFromFile(File file, int maxLength) throws IOException {
        String news = FileUtils.readFileToString(file, (Charset)null);
        return loadFeaturesFromString(news, maxLength);
    }

    /**
     * Used post training to convert a String to a features INDArray that can be passed to the network output method
     *
     * @param reviewContents Contents of the review to vectorize
     * @param maxLength      Maximum length (if review is longer than this: truncate to maxLength). Use Integer.MAX_VALUE to not nruncate
     * @return Features array for the given input String
     */
    public INDArray loadFeaturesFromString(String reviewContents, int maxLength) {
        List<String> tokens = textProcessor.tokenizerFactory.create(reviewContents).getTokens();
        List<String> tokensFiltered = new ArrayList<>();
        for (String t : tokens) {
            if (textProcessor.word2Vec.hasWord(t)) tokensFiltered.add(t);
        }
        int outputLength = Math.max(maxLength, tokensFiltered.size());

        INDArray features = Nd4j.create(1, vectorSize, outputLength);

        for (int j = 0; j < tokens.size() && j < maxLength; j++) {
            String token = tokens.get(j);
            INDArray vector = textProcessor.word2Vec.getWordVectorMatrix(token);
            features.put(new INDArrayIndex[]{point(0),
                    all(),
                    point(j)}, vector);
        }

        return features;
    }

    /*
    This function loads news headlines from files stored in resources into categoryData List.
    */
    private void populateData(boolean train) {
        File categories = new File(this.dataDirectory + File.separator + "categories.txt");

        try (BufferedReader brCategories = new BufferedReader(new FileReader(categories))) {
            String temp;
            while ((temp = brCategories.readLine()) != null) {
                String curFileName = train ?
                        this.dataDirectory + File.separator + "train" + File.separator + temp.split(",")[0] + ".txt" :
                        this.dataDirectory + File.separator + "test" + File.separator + temp.split(",")[0] + ".txt";
                File currFile = new File(curFileName);
                BufferedReader currBR = new BufferedReader((new FileReader(currFile)));
                String tempCurrLine;
                List<String> tempList = new ArrayList<>();
                while ((tempCurrLine = currBR.readLine()) != null) {
                    tempList.add(tempCurrLine);
                    this.totalNews++;
                }
                currBR.close();
                Pair<String, List<String>> tempPair = Pair.of(temp, tempList);
                this.categoryData.add(tempPair);
            }
        } catch (Exception e) {
            System.out.println("Exception in reading file :" + e.getMessage());
        }
    }

    @Override
    public int inputColumns() {
        return vectorSize;
    }

    @Override
    public int totalOutcomes() {
        return this.categoryData.size();
    }

    @Override
    public void reset() {
        cursor = 0;
        newsPosition = 0;
        currCategory = 0;
    }

    public boolean resetSupported() {
        return true;
    }

    @Override
    public boolean asyncSupported() {
        return true;
    }

    @Override
    public int batch() {
        return batchSize;
    }

    @Override
    public void setPreProcessor(DataSetPreProcessor preProcessor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getLabels() {
        return this.labels;
    }

    @Override
    public boolean hasNext() {
        return cursor < this.totalNews;
    }

    @Override
    public DataSet next() {
        return next(batchSize);
    }

    @Override
    public void remove() {

    }

    @Override
    public DataSetPreProcessor getPreProcessor() {
        throw new UnsupportedOperationException("Not implemented");
    }

    public int getMaxLength() {
        return this.maxLength;
    }

    public static class Builder {
        private String dataDirectory;
        private int batchSize;
        private int truncateLength;
        private boolean train;
        private TextProcessor textProcessor;

        public Builder() {
        }

        public Iterator.Builder dataDirectory(String dataDirectory) {
            this.dataDirectory = dataDirectory;
            return this;
        }

        public Iterator.Builder textProcessor(TextProcessor textProcessor) {
            this.textProcessor = textProcessor;
            return this;
        }

        public Iterator.Builder batchSize(int batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public Iterator.Builder truncateLength(int truncateLength) {
            this.truncateLength = truncateLength;
            return this;
        }

        public Iterator.Builder train(boolean train) {
            this.train = train;
            return this;
        }

        public Iterator build() {
            return new Iterator(dataDirectory,
                    batchSize,
                    truncateLength,
                    train,
                    textProcessor);
        }
    }
}


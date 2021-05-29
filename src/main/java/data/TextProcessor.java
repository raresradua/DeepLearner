package data;

import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TextProcessor {
    TokenizerFactory tokenizerFactory;
    Word2Vec word2Vec;

    public TextProcessor(String word2vecPath) {
        tokenizerFactory = new DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(new CommonPreprocessor());
        word2Vec = WordVectorSerializer.readWord2VecModel(new File(word2vecPath));
    }
    public DataSet vectorizeText(List<String> texts, int[] category, int truncateLength) {
        List<List<String>> allTokens= new ArrayList<>(texts.size());
        int maxLength = 0;

        // Tokenize news and filter out unknown words
        for (String s : texts) {
            List<String> tokens = tokenizerFactory.create(s).getTokens();
            List<String> tokensFiltered = new ArrayList<>();
            for (String t: tokens){
                if (word2Vec.hasWord(t))
                    tokensFiltered.add(t);
            }
            allTokens.add(tokensFiltered);
            maxLength = Math.max(maxLength, tokensFiltered.size());
        }

        // If length of the news exceeds 'truncateLength': only take that number of words
        if (maxLength > truncateLength)
            maxLength = truncateLength;

        // Create data
        INDArray features = Nd4j.create(texts.size(), word2Vec.lookupTable().layerSize(), maxLength);
        INDArray labels = Nd4j.create(texts.size(), 2, maxLength);    //labels: True, False

        // Pad to maxLength
        // featuresMask contains 1 if data is data exists or 0 is just padding
        INDArray featuresMask = Nd4j.zeros(texts.size(), maxLength);
        INDArray labelsMask = Nd4j.zeros(texts.size(), maxLength);

        int[] temp = new int[2];
        for (int i = 0; i < texts.size(); i++) {
            List<String> tokens = allTokens.get(i);
            temp[0] = i;
            // Get word vectors for each word
            for (int j = 0; j < tokens.size() && j < maxLength; j++) {
                String token = tokens.get(j);
                INDArray vector = word2Vec.getWordVectorMatrix(token);
                features.put(new INDArrayIndex[]{NDArrayIndex.point(i),
                                NDArrayIndex.all(),
                                NDArrayIndex.point(j)},
                        vector);

                temp[1] = j;
                featuresMask.putScalar(temp, 1.0);
            }
            int idx = category[i];
            int lastIdx = Math.min(tokens.size(), maxLength);
            labels.putScalar(new int[]{i, idx, lastIdx - 1}, 1.0);
            labelsMask.putScalar(new int[]{i, lastIdx - 1}, 1.0);
        }

        return new DataSet(features, labels, featuresMask, labelsMask);
    }

    public DataSet vectorizeText(String text, int truncateLength){
        List<String> news = new ArrayList<>(1);
        int[] category = new int[1];
        news.add(text);

        return vectorizeText(news, category, truncateLength);
    }
}

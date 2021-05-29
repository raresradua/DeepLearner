import gui.MainFrame;
import models.LstmModel;
import models.Word2Vec;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Main class. Initializes the GUI and loads the trained model, then predicts news given by the user.
 * @author Vlad Cociorva & Rares Radu
 */
public class App {
    public static void main(String[] args) {
        MainFrame mainFrame = new MainFrame();
        mainFrame.setVisible(true);

        mainFrame.getScrollPane().setVisible(false);
        mainFrame.getClassificationLabel().setVisible(false);
        mainFrame.getInfoLabel().setVisible(false);
        mainFrame.getButton().setVisible(false);

        try {
            mainFrame.getTextAreaLabel().setText("Downloading Word2Vec vectorizer...");
            Word2Vec.downloadWord2VecModel();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mainFrame.getTextAreaLabel().setText("Model is loading...");
        LstmModel model = new LstmModel(Word2Vec.getWordVectorsPath());
        model.loadModel("/Users/skyehigh/fun/fac/Java/DeepLearner/src/main/resources/Model.net");
        model.loadCategories("/Users/skyehigh/fun/fac/Java/DeepLearner/src/main/resources/data/categories.txt");

        mainFrame.getButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!mainFrame.getTextArea().getText().isEmpty()){
                    String prediction = model.predict(mainFrame.getTextArea().getText());
                    mainFrame.getInfoLabel().setVisible(true);
                    mainFrame.getClassificationLabel().setVisible(true);
                    mainFrame.getClassificationLabel().setText(prediction);
                    mainFrame.getInfoLabel().setText("Prediction");
                }
            }
        });

        mainFrame.getTextAreaLabel().setText("Insert News Here");
        mainFrame.getScrollPane().setVisible(true);
        mainFrame.getClassificationLabel().setVisible(true);
        mainFrame.getInfoLabel().setVisible(true);
        mainFrame.getButton().setVisible(true);
    }

}

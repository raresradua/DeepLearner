package gui;

import javax.swing.*;

/**
 * Basic GUI of the application
 * @author Vlad Cociorva & Rares Radu
 */
public class MainFrame extends JFrame {
    JLabel textAreaLabel, classificationLabel, infoLabel;
    JScrollPane scrollPane;
    JTextArea textArea;
    JButton button;

    public MainFrame(){
        super("Fake News Classifier");
        init();
    }

    public JLabel getTextAreaLabel() {
        return textAreaLabel;
    }

    public JLabel getClassificationLabel() {
        return classificationLabel;
    }

    public JLabel getInfoLabel() {
        return infoLabel;
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    public JTextArea getTextArea() {
        return textArea;
    }

    public JButton getButton() {
        return button;
    }

    private void init() {
        this.setResizable(false);
        this.setSize(800, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        textAreaLabel = new JLabel();

        infoLabel = new JLabel();
        classificationLabel = new JLabel();

        textArea = new JTextArea();
        textArea.setRows(5);
        textArea.setColumns(5);

        scrollPane = new JScrollPane();
        scrollPane.setViewportView(textArea);

        button = new JButton();
        button.setText("Check");

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                                .addGroup(layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(textAreaLabel)
                                        .addComponent(button))
                        .addGap(0, 0, Short.MAX_VALUE))
                .addGroup(layout.createSequentialGroup()
                        .addComponent(infoLabel)
                        .addGap(18, 18, 18)
                        .addComponent(classificationLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(textAreaLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(scrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(button)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(classificationLabel)
                                        .addComponent(infoLabel))
                                .addContainerGap(40, Short.MAX_VALUE))
        );

        pack();
    }
}

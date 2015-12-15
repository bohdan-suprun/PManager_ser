package edu.nure.gui.util;

import edu.nure.gui.components.EditableJLabel;
import edu.nure.listener.ValueChangedListener;

import javax.swing.*;
import java.awt.event.*;

/**
 * Created by bod on 05.10.15.
 */
public class SaveButtonObserver {

    public static void setListeners(JComponent[] components, final JButton saveButton){

        for(JComponent component: components){
            if(component instanceof JComboBox) {
                ((JComboBox) component).addItemListener(getComboBoxListener(saveButton));
                continue;
            }

            if(component instanceof JTextField) {
                ((JTextField) component).addKeyListener(getEditListener(saveButton));
                continue;
            }

            if(component instanceof JTextArea) {
                ((JTextArea) component).addKeyListener(getEditListener(saveButton));
                continue;
            }


            if(component instanceof EditableJLabel) {
                ((EditableJLabel) component).clear();
                ((EditableJLabel) component).addValueChangedListener(getEditableJlListener(saveButton));
            }

            if(component instanceof JCheckBox) {
                ((JCheckBox) component).addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        saveButton.setEnabled(true);
                    }
                });
            }


        }

    }

    private static KeyListener getEditListener(final JButton button){
        return new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {
                button.setEnabled(true);
            }
        };
    }

    private static ValueChangedListener getEditableJlListener(final JButton button){
        return new ValueChangedListener() {
            @Override
            public void valueChanged(String value, JComponent source) {
                button.setEnabled(true);
            }
        };
    }

    private static ItemListener getComboBoxListener(final JButton button){
        return new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                    button.setEnabled(true);
                }
            }
        };
    }

}

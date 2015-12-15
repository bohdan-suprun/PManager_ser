package edu.nure.gui.components;

import edu.nure.containers.suggestion.AbstractSuggestionContainer;
import edu.nure.listener.CurrentIndexChanged;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by bod on 03.10.15.
 */
public class SuggestionComboBox extends JComboBox<AbstractSuggestionContainer>{

    private CopyOnWriteArrayList<CurrentIndexChanged> listener = new CopyOnWriteArrayList<CurrentIndexChanged>();
    private List<AbstractSuggestionContainer> list = new ArrayList<AbstractSuggestionContainer>();
    private boolean shouldHide;
    private SuggestionPerformer performer;
    private AbstractSuggestionContainer currentItem;

    public SuggestionComboBox(SuggestionPerformer p) {
        super();
        getEditor().getEditorComponent().addKeyListener(getListener());
        setEditable(true);
        performer = p;
        addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent itemEvent) {
                AbstractSuggestionContainer s = getContainer(itemEvent.getItem().toString());
                if (s != null) {
                    ((JTextField) getEditor().getEditorComponent())
                            .setText(s.displayString());
                }
                currentItem = s;
                fireListener();
            }
        });

        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                if ("comboBoxChanged".equals(evt.getActionCommand()) || "comboBoxEdited".equals(evt.getActionCommand())) {
                    if(getSelectedItem() != null) {
                        AbstractSuggestionContainer s = getContainer(getSelectedItem().toString());
                        if(s != null) {
                            ((JTextField) getEditor().getEditorComponent())
                                    .setText(s.displayString());
                        }
                    }
                }
            }
        });


    }

    public void addItemChanged(CurrentIndexChanged c){
        listener.add(c);
    }

    private void fireListener(){
        for(CurrentIndexChanged c: listener)
            c.currentIndexChanged(currentItem);
    }

    private AbstractSuggestionContainer getContainer(String text){
        for (int i = 0; i < getModel().getSize(); i++) {
            if(getModel().getElementAt(i).toString().equals(text)) {
                return getModel().getElementAt(i);
            }
        }
        return null;
    }

    public void addElement(AbstractSuggestionContainer elem){
        list.add(elem);
        Collections.sort(list, new Comparator<AbstractSuggestionContainer>() {
            @Override
            public int compare(AbstractSuggestionContainer abstractContainer, AbstractSuggestionContainer t1) {
                return abstractContainer.getString().toLowerCase().compareTo(t1.getString().toLowerCase());
            }
        });
    }

    private KeyAdapter getListener() {
        return new KeyAdapter() {
            @Override
            public void keyTyped(final KeyEvent e) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            CountDownLatch latch = new CountDownLatch(1);
                            String text = ((JTextField) e.getComponent()).getText();
                            if (text.length() < 3) {
                                hidePopup();
                            } else {
                                list.clear();
                                performer.ajax(SuggestionComboBox.this, text, latch);
                                latch.await(3, TimeUnit.SECONDS);
                                synchronized (this) {
                                    setSuggestionModel(getSuggestedModel(text), text);
                                }
                                if (!shouldHide)
                                    showPopup();
                            }
                        }catch (InterruptedException ex){
                            System.err.println(ex.getMessage());
                        }
                    }
                });
            }

            @Override
            public void keyPressed(KeyEvent e) {
                JTextField textField = (JTextField) e.getComponent();
                String text = textField.getText();
                shouldHide = false;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_RIGHT:
                        for (AbstractSuggestionContainer value : list) {
                            if (value.getString().toLowerCase().contains(text.trim().toLowerCase())) {
                                textField.setText(value.getString());
                                currentItem = value;
                                return;
                            }
                        }
                        break;
                    case KeyEvent.VK_ENTER:
                        performer.search(text);
                        shouldHide = true;
                        break;
                    case KeyEvent.VK_ESCAPE:
                        shouldHide = true;
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void setSuggestionModel(ComboBoxModel<AbstractSuggestionContainer> mdl, String str) {
        setModel(mdl);
        setSelectedIndex(-1);
        ((JTextField) getEditor().getEditorComponent()).setText(str);
    }

    private ComboBoxModel<AbstractSuggestionContainer> getSuggestedModel(String text) {
        DefaultComboBoxModel<AbstractSuggestionContainer> m = new DefaultComboBoxModel<AbstractSuggestionContainer>();
        for (AbstractSuggestionContainer value : list) {
            if (value.getString().toLowerCase().contains(text.toLowerCase().trim())) {
                ;
                m.addElement(value);
            }
        }
        return m;
    }

    public AbstractSuggestionContainer getCurrentItem(){
        return currentItem;
    }

    public SuggestionPerformer getPerformer() {
        return performer;
    }

    public int indexOf(AbstractSuggestionContainer container){
        int i = -1;
        for(int j = 0; j<getModel().getSize();j++)
            if(getModel().getElementAt(i) == container){
                i = j;
                break;
            }
        if(i == -1) {
            i = list.indexOf(container);
            if(i > -1) {
                ((DefaultComboBoxModel) getModel()).addElement(container);
                i = ((DefaultComboBoxModel) getModel()).getIndexOf(container);
            }
        }
        return i;
    }

    public void clear(){
        list.clear();
        ((DefaultComboBoxModel)getModel()).removeAllElements();
    }

    public void resetCurrentEntity(){
        currentItem = null;
        this.setSelectedIndex(-1);
        ((JTextField)this.getEditor().getEditorComponent()).setText("");
    }
}



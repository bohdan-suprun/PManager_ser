package edu.nure.gui.viewers.implementation;

import edu.nure.bl.Urgency;
import edu.nure.bl.constraints.ValidationException;
import edu.nure.containers.list.UrgencyContainer;
import edu.nure.db.entity.basic.Transmittable;
import edu.nure.gui.components.EditableJLabel;
import edu.nure.gui.util.SaveButtonObserver;
import edu.nure.gui.viewers.AbstractViewer;
import edu.nure.net.HttpManager;
import edu.nure.net.request.Priority;

import javax.swing.*;
import java.awt.*;

/**
 * Created by bod on 15.10.15.
 */
public class UrgencyViewer extends AbstractViewer {

    private static UrgencyViewer self;
    private EditableJLabel urg, factor;
    private String oldName;

    private UrgencyViewer() {
        super("URGENCY");
        setContent(createPanel());
    }

    public static synchronized UrgencyViewer getWindow(){
        return (self == null)? self = new UrgencyViewer(): self;
    }


    protected JPanel createPanel(){
        JPanel main = new JPanel(new GridLayout(0,2));
        main.add(new JLabel("Срочность, минут:"));
        main.add(urg = new EditableJLabel("1"));

        main.add(new JLabel("Наценка, %:"));
        main.add(factor = new EditableJLabel("0"));

        return main;
    }

    @Override
    protected void load() {
        HttpManager.getManager().sendGet("?action="+getAct,new Priority(Priority.MAX),
                getDefaultAdapter());
    }

    @Override
    protected void update() {
        HttpManager.getManager().sendGet("?action="+updAct+"&oldTerm="+oldName+"&"+curEntity.getEntity().toQuery(),new Priority(Priority.MAX),
                getDefaultAdapter());
    }

    @Override
    public void addItem(Transmittable t) {
        model.addElement(new UrgencyContainer(t));
        itemList.repaint();
    }

    @Override
    protected void observeSaveButton() {
        SaveButtonObserver.setListeners(new JComponent[]{urg,factor}, saveButton);
    }

    @Override
    protected Transmittable createEntity() throws ValidationException {
        try {
            return new Urgency(
                    Integer.valueOf(urg.getText()),
                    Float.valueOf(factor.getText())
            );
        } catch (NumberFormatException ex){
            throw new ValidationException("Все поля должны быть числами");
        }
    }

    @Override
    protected void entityChanged() {
        Urgency f = (Urgency)curEntity.getEntity();
        oldName = String.valueOf(f.getTerm());
        urg.setText(String.valueOf(f.getTerm()));
        factor.setText(String.valueOf(f.getFactor()));
    }

    @Override
    protected void setDefault() {
        oldName = null;
        urg.setText("1");
        factor.setText("0");
    }
}

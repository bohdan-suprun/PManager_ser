package edu.nure.gui.viewers.implementation;

import edu.nure.bl.Format;
import edu.nure.bl.constraints.ValidationException;
import edu.nure.containers.list.FormatContainer;
import edu.nure.db.entity.basic.Transmittable;
import edu.nure.gui.components.EditableJLabel;
import edu.nure.gui.util.SaveButtonObserver;
import edu.nure.gui.viewers.AbstractViewer;
import edu.nure.net.HttpManager;
import edu.nure.net.request.Priority;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

/**
 * Created by bod on 15.10.15.
 */
public class FormatViewer extends AbstractViewer {

    private static FormatViewer self;
    private EditableJLabel name, width, height, price;
    private String oldName;

    private FormatViewer() {
        super("format");
        setContent(createPanel());
    }

    public static synchronized FormatViewer getWindow(){
        return (self == null)? self = new FormatViewer(): self;
    }

    protected JPanel createPanel(){
        JPanel main = new JPanel(new GridLayout(0,2));
        main.add(new JLabel("Название формата:"));
        main.add(name = new EditableJLabel("<undefined>"));

        main.add(new JLabel("Ширина, мм:"));
        main.add(width = new EditableJLabel("0"));

        main.add(new JLabel("Высота, мм:"));
        main.add(height = new EditableJLabel("0"));

        main.add(new JLabel("Цена за единицу, грн:"));
        main.add(price = new EditableJLabel("0"));

        return main;
    }

    @Override
    protected void load() {
        HttpManager.getManager().sendGet("?action="+getAct,new Priority(Priority.MAX),
                getDefaultAdapter());
    }

    @Override
    protected void update() {
        HttpManager.getManager().sendGet("?action=" + updAct + "&oldName=" + oldName + "&" + curEntity.getEntity().toQuery(),
                new Priority(Priority.MAX), getDefaultAdapter());
    }

    @Override
    public void addItem(Transmittable t) {
        model.addElement(new FormatContainer(t));
        itemList.repaint();
    }

    @Override
    protected void observeSaveButton() {
        SaveButtonObserver.setListeners(new JComponent[]{name, width, height, price}, saveButton);
    }

    @Override
    protected Transmittable createEntity() throws ValidationException {
        String nme = name.getText();
        if(nme.equals("<undefined>")) throw new ValidationException("Нужно указать название для формата");
        return new Format(
                nme,
                Integer.valueOf(width.getText()),
                Integer.valueOf(height.getText()),
                Float.valueOf(price.getText().replace(",", "."))
        );
    }

    @Override
    protected void entityChanged() {
        Format f = (Format)curEntity.getEntity();
        oldName = f.getName();
        name.setText(f.getName());
        width.setText(String.valueOf(f.getWidth()));
        height.setText(String.valueOf(f.getHeight()));
        price.setText(new DecimalFormat("#.##").format(f.getPrice()));
    }

    @Override
    protected void setDefault() {
        oldName = null;
        name.setText("<undefined>");
        width.setText("0");
        height.setText("0");
        price.setText("0.0");
    }
}

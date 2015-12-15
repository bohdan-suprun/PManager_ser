package edu.nure.gui.components;

import edu.nure.bl.Image;
import edu.nure.gui.components.model.ImageModel;
import edu.nure.gui.image.ImageView;
import edu.nure.listener.ImageDeleted;
import edu.nure.listener.ModelChanged;

import javax.swing.*;
import java.awt.event.MouseAdapter;

/**
 * Created by bod on 05.10.15.
 */
public class AlbumPanel extends JPanel {
    private ImageModel model;
    private MouseAdapter previewClicked;
    
    public AlbumPanel(final ImageModel m, MouseAdapter click){
        previewClicked = click;
        setLayout(new WrapLayout());
        setModel(m);

        ImageView.getWindow().addImageDeletedListener(new ImageDeleted() {
            @Override
            public void imageDeleted(Image im) {
                model.remove(im);
            }
        });
    }

    private void repaintModel(){
        removeAll();
        synchronized (model) {
            for (int i = 0; i < model.size(); i++) {
                add(new PreviewImageLabel(model.get(i), previewClicked));
            }
        }
    }

    @Override
    public void setVisible(boolean v){
        super.setVisible(v);
    }

    public ImageModel getModel(){
        return model;
    }

    public void setModel(ImageModel m) {
        this.model = m;
        this.model.addListener(new ModelChanged() {
            @Override
            public void modelChanged() {
                repaintModel();
            }
        });
        repaintModel();
    }

    public JScrollPane getScrolled(){
        JScrollPane pane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pane.setViewportView(this);
        return pane;
    }

}

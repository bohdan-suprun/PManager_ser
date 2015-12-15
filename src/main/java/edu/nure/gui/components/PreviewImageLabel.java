package edu.nure.gui.components;

import edu.nure.bl.Image;
import edu.nure.listener.ResponseAdapter;
import edu.nure.listener.ResponseListener;
import edu.nure.net.HttpManager;
import edu.nure.net.results.DBResult;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;

/**
 * Created by bod on 05.10.15.
 */
public class PreviewImageLabel extends JLabel{
    private Image image;

    public PreviewImageLabel(final Image image, MouseAdapter click) {
        this.image = image;
        addMouseListener(click);
        loadImage();
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
        loadImage();
    }

    private void loadImage(){
        if(image != null && image.getImage() == null) {
            HttpManager.getManager().loadPreview(image.getId(), image.getAlbum(), getListener());
        } else {
            if (image != null)
                showImage();
            if (image == null)
                setIcon(new ImageIcon("resources/unknown.jpg"));
        }
    }

    private ResponseListener getListener(){
        return new ResponseAdapter() {
            @Override
            public void doError(DBResult result) {
                setVisible(false);
            }

            @Override
            public void doBinaryImage(byte[] im) {
                image.setImage(im);
                showImage();
            }
        };
    }

    private void showImage(){
        setIcon(new ImageIcon(image.getImage()));
        setVisible(true);
    }

}

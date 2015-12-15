package edu.nure.gui.components;

import edu.nure.bl.Album;
import edu.nure.bl.Image;
import edu.nure.bl.User;
import edu.nure.gui.components.model.AlbumModel;
import edu.nure.gui.components.model.ImageModel;
import edu.nure.gui.util.MessagesManager;
import edu.nure.gui.util.PHash;
import edu.nure.listener.ModelChanged;
import edu.nure.listener.ResponseAdapter;
import edu.nure.net.results.DBResult;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.util.Date;

/**
 * Created by bod on 07.10.15.
 */
public class AlbumTab extends JPanel{
    private User theUser;
    private JTabbedPane imagePane;
    private JButton addImage;
    private JButton addAlbum;
    private JButton removeAlbum;
    private AlbumModel model;

    private MouseAdapter previewClicked;

    public AlbumTab(final User theUser, MouseAdapter click) {
        this.theUser = theUser;
        imagePane = new JTabbedPane();
        previewClicked = click;
        imagePane.setTabPlacement(JTabbedPane.LEFT);
        imagePane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        addImage = new JButton("+");
        addImage.setContentAreaFilled(true);
        setLayout(new BorderLayout());
        model = new AlbumModel(this.theUser);
        model.addListener(new ModelChanged() {
            @Override
            public void modelChanged() {
                repaintModel();
            }
        });
        addImage.addMouseListener(selectImage());
        imagePane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                JTabbedPane pane = (JTabbedPane) changeEvent.getSource();
                int index = pane.getSelectedIndex();
                if (index > -1) {
                    model.get(index).load();
                }
            }
        });

        add(imagePane, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(addImage);
        buttonPanel.add(addAlbum = new JButton("Создать альбом"));
        buttonPanel.add(removeAlbum = new JButton("Удалить альбом"));
        add(buttonPanel, BorderLayout.EAST);
        addAlbum.addMouseListener(addAlbum());
        this.setVisible(true);
        removeAlbum.addMouseListener(removeAlbum());
    }

    private void repaintModel(){
        imagePane.removeAll();
        for (int i = 0; i < model.size(); i++) {
            imagePane.addTab(model.get(i).getAlbumName().getName(), new AlbumPanel(model.get(i), previewClicked).getScrolled());
        }
        enableButtons();
    }

    private void enableButtons(){

        addImage.setVisible(imagePane.getTabCount() > 0);
        removeAlbum.setVisible(imagePane.getTabCount() > 0);
        addAlbum.setVisible(AlbumTab.this.theUser != null);
    }

    @Override
    public void setVisible(boolean s){
        imagePane.setVisible(s);
        super.setVisible(s);
    }

    private MouseAdapter addAlbum(){
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String name = MessagesManager.inputBox("Название альбома:", "Новый альбом");
                if(name != null && !name.isEmpty()) {
                   Album album = new Album(name.trim(), Album.ID_NOT_SET, theUser.getId());
                   model.insert(album);
                }

            }
        };
    }

    private MouseAdapter selectImage(){
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    JFileChooser fd = new JFileChooser();
                    fd.setAcceptAllFileFilterUsed(false);
                    if(fd.showOpenDialog(AlbumTab.this) != JFileChooser.APPROVE_OPTION || fd.getSelectedFile() == null) return;
                    FileInputStream in = new FileInputStream(fd.getSelectedFile());
                    byte[] buffer = new byte[in.available()];
                    in.read(buffer);
                    Image image = new Image(
                            PHash.getSelf().hash(buffer),
                            Image.ID_NOT_SET,
                            buffer,
                            getCurrentModel().getAlbumName().getId(),
                            new Date()
                    );
                    getCurrentModel().insert(image);
                } catch (Exception ex) {
                    System.err.println(ex.getMessage());
                }


            }
        };
    }

    private ImageModel getCurrentModel(){
        int index = imagePane.getSelectedIndex();
        return model.getElement(index);
    }

    private MouseAdapter removeAlbum(){
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Component c = imagePane.getSelectedComponent();
                Album a = getCurrentModel().getAlbumName();
                model.remove(a);
            }
        };
    }

    private ResponseAdapter onDeleteAlbum(final Component old){
        return new ResponseAdapter(){
            @Override
            public void doDelete(DBResult result) {
                imagePane.remove(old);
                removeAlbum.setVisible(imagePane.getTabCount() > 0);
                addImage.setVisible(imagePane.getTabCount() > 0);
            }

            @Override
            public void doError(DBResult res){
                MessagesManager.errorBox(res.getText(), "Ошибка");

            }
        };

    }

    public void setUser(User u){
        theUser = u;
        model.setOwner(theUser);
        enableButtons();
    }

    public AlbumModel getModel() {
        return model;
    }

    public void setModel(AlbumModel model){
        this.model = model;
        this.model.addListener(new ModelChanged() {
            @Override
            public void modelChanged() {
                repaintModel();
            }
        });
        repaintModel();

    }
}

package edu.nure.gui.image;

import edu.nure.bl.Album;
import edu.nure.bl.Image;
import edu.nure.bl.User;
import edu.nure.containers.list.UserContainer;
import edu.nure.gui.util.MessagesManager;
import edu.nure.listener.ImageDeleted;
import edu.nure.listener.ResponseAdapter;
import edu.nure.listener.SelectAdapter;
import edu.nure.net.HttpManager;
import edu.nure.net.request.Priority;
import edu.nure.net.results.DBResult;
import edu.nure.net.results.DBSelectResult;
import edu.nure.net.results.ResultItem;
import edu.nure.util.Action;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Image view window.
 */

public class ImageView extends JDialog {
    private static ImageView self;
    private final ArrayList<ImageDeleted> listeners;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JButton prevButton;
    private JButton nextButton;
    private JPanel imageArea;
    private JLabel image;
    private JButton deleteButton;
    private JButton saveButton;
    private JLabel albumName;
    private JLabel owner;
    private JLabel creationDate;
    private boolean readOnly;
    private Image imageObject;
    private Album album;

    private ImageView() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        listeners = new ArrayList<ImageDeleted>();
        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        saveButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(imageObject == null || imageObject.getImage() == null)
                    MessagesManager.errorBox("Невозможно сохранить изображение","Ошибка");
                JFileChooser fileChooser = new JFileChooser();
                if (fileChooser.showSaveDialog(ImageView.this) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    try {
                        FileOutputStream out = new FileOutputStream(file);
                        out.write(imageObject.getImage());
                        out.close();
                        MessagesManager.infoBox("Сохранено", "Изображение сохранено");
                    } catch (IOException ex){
                        MessagesManager.errorBox("Неудалось сохранить файл","Ошибка");
                    }
                }
            }
        });

        deleteButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(imageObject != null) {
                    int i = MessagesManager.questionBox("Вы действительно хотите удалить изображение?","Удалить?",new String[]
                            {"Да", "Нет"});
                    if(i == 0)
                        deleteImage();
                }
            }
        });

        nextButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                navigate("next");
            }
        });

        prevButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                navigate("prev");
            }
        });


    }

    public static synchronized ImageView getWindow() {
        return (self == null) ? self = new ImageView() : self;
    }

    public void addImageDeletedListener(ImageDeleted li){
        synchronized (listeners){
            listeners.add(li);
        }
    }

    private void fireListener(Image im){
        synchronized (listeners){
            for(ImageDeleted li: listeners)
                li.imageDeleted(im);
        }
    }

    private void onCancel() {
        imageObject = null;
        album = null;
        readOnly = false;
// add your code here if necessary
        dispose();
    }

    private void createUIComponents() {
       imageArea = new ImagePanel();
    }

    private void loadImage(){
        HttpManager.getManager().getImage("id=" + imageObject.getId() + "&full=", new ResponseAdapter() {
            @Override
            public void doError(DBResult result) {
                MessagesManager.errorBox("Невозможно загрузить изображение", "Error");
                image.setIcon(new ImageIcon());

            }

            @Override
            public void doBinaryImage(byte[] im) {
                ImageIcon icon = new ImageIcon(im);
                ((ImagePanel)imageArea).setImage(icon);
                imageArea.repaint();
            }
        });

        loadDescription();
    }

    public ImageView setReadOnly(){
        readOnly = true;
        return this;
    }

    private void deleteImage(){
        HttpManager.getManager().sendGet(HttpManager.HOST,"/image/?action="+Action.DELETE_IMAGE+"&id="+imageObject.getId(),
                new Priority(Priority.MAX),
                new ResponseAdapter() {
                    @Override
                    public void doError(DBResult result) {
                        MessagesManager.errorBox("Неудалось удалить изображение: " + result.getText() +
                                "CODE(" + result.getStatus() + ")", "Ошибка");
                    }

                    @Override
                    public void doDelete(DBResult result) {
                        MessagesManager.infoBox("Изображение было удалено", "Удалено");
                        ((ImagePanel) imageArea).setImage(new ImageIcon());
                        fireListener(imageObject);
                        imageObject = null;
                    }
                });

    }

    private void loadDescription(){
        creationDate.setText(imageObject.getCreatedIn());
        HttpManager.getManager().sendGet("?action="+Action.GET_ALBUM+"&id=-1&albumId="+imageObject.getAlbum(),
                new Priority(Priority.MAX), new SelectAdapter(){
                    @Override
                    public void onAlbum(DBSelectResult res) {
                        ResultItem[] items = res.getResult();
                        if(items.length > 0) {
                            album = ((Album) items[0].getEntity());
                            albumName.setText(album.getName());
                            HttpManager.getManager().sendGet("?action="+Action.GET_USER+"&id="+album.getUserId(),
                                    new Priority(Priority.MAX), new SelectAdapter(){
                                        @Override
                                        public void onUser(DBSelectResult res) {
                                            ResultItem[] items = res.getResult();
                                            if(items.length > 0)
                                                owner.setText(new UserContainer(((User) items[0].getEntity())).toString());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void doError(DBResult result) {
                        album = null;
                    }
                });

    }

    private void navigate(String direction){
        if(album != null)
            HttpManager.getManager().getImage("albumId="+album.getId()+"&"+direction+"="+imageObject.getId(),
                    new SelectAdapter(){
                        @Override
                        public void onImage(DBSelectResult res) {
                            ResultItem[] items = res.getResult();
                            if(items.length > 0)
                                setImage((Image) items[0].getEntity());
                        }
                    });

    }

    public ImageView setImage(Image im){
        imageObject = im;
        loadImage();
        return this;
    }

    @Override
    public void setVisible(boolean v){
        if(v){
            setSize(Toolkit.getDefaultToolkit().getScreenSize());
            deleteButton.setVisible(!readOnly);
            prevButton.setVisible(false);
            nextButton.setVisible(false);
        }
        super.setVisible(v);
    }
}

class ImagePanel extends JPanel {
    private ImageIcon image;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            if(image.getIconWidth() < getWidth() && image.getIconHeight() < getHeight())
                g.drawImage(image.getImage(), 0, 0, image.getIconWidth(), image.getIconHeight(), this);
            else{
                int height = this.getHeight();
                int width = image.getIconWidth();
                float factor = this.getWidth()/(float)height;
                width = (int)(height * factor);
                g.drawImage(image.getImage(), 0, 0, width, height, this);
            }
        }
    }

    public void setImage(ImageIcon im){
        image = im;
    }
}

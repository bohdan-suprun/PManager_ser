package edu.nure.gui.mainwindow;

import edu.nure.bl.User;
import edu.nure.bl.constraints.Validator;
import edu.nure.containers.list.AbstractContainer;
import edu.nure.containers.list.OrderContainer;
import edu.nure.containers.list.UserContainer;
import edu.nure.containers.suggestion.AbstractSuggestionContainer;
import edu.nure.containers.suggestion.UserNameSuggestionContainer;
import edu.nure.containers.suggestion.UserPhoneSuggestionContainer;
import edu.nure.gui.components.PreviewImageLabel;
import edu.nure.gui.components.SuggestionComboBox;
import edu.nure.gui.components.SuggestionPerformer;
import edu.nure.gui.components.WrapLayout;
import edu.nure.gui.image.ImageView;
import edu.nure.gui.user.UserDescription;
import edu.nure.gui.util.MessagesManager;
import edu.nure.gui.util.PHash;
import edu.nure.gui.viewers.implementation.FormatViewer;
import edu.nure.gui.viewers.implementation.ResponsibleOrderViewer;
import edu.nure.gui.viewers.implementation.UrgencyViewer;
import edu.nure.listener.SelectAdapter;
import edu.nure.net.HttpManager;
import edu.nure.net.request.Priority;
import edu.nure.net.results.DBResult;
import edu.nure.net.results.DBSelectResult;
import edu.nure.net.results.ResultItem;
import edu.nure.util.Action;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.util.concurrent.CountDownLatch;

/**
 * Created by bod on 08.09.15.
 */
public class MainForm extends JFrame {
    private static MainForm self;
    private JPanel panel1;
    private JTabbedPane propertiesTab;
    private JLabel userName;
    private JScrollPane scrollPane;
    private JList<AbstractContainer> searchResult;
    private DefaultListModel<AbstractContainer> searchResultModel;
    private JComboBox<AbstractSuggestionContainer> searchComboBox;
    private JButton searchButton;
    private JList listOrder;
    private JSplitPane splitter;
    private JButton newUserButton;
    private JButton manageFormat;
    private JButton manageUrgency;
    private JButton byImage;
    private JScrollPane scrolledImageArea;
    private JPanel searchByImage;
    private User worker;
    private DefaultListModel<OrderContainer> model;
    private OrderRequester requester;

    public MainForm() {
        super("PManager");
        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        searchButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SuggestionComboBox cb = ((SuggestionComboBox) searchComboBox);
                synchronized (searchComboBox) {
                    cb.getPerformer().search(((JTextField) cb.getEditor().getEditorComponent()).getText());
                    searchResult.setVisible(true);
                    scrolledImageArea.setVisible(false);
                }
            }
        });

        byImage.setIcon(new ImageIcon("resources/byIm.png"));
        byImage.setContentAreaFilled(false);
        byImage.setToolTipText("Поиск по изображению");
        byImage.addMouseListener(searchByImage());
        newUserButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                UserDescription.getWindow().setVisible(true);
            }
        });

        manageFormat.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                FormatViewer.getWindow().setVisible(true);
            }
        });

        manageUrgency.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                UrgencyViewer.getWindow().setVisible(true);
            }
        });
        pack();
    }

    public static synchronized MainForm getMainForm() {
        return (self == null) ? self = new MainForm() : self;
    }

    public JList getListOrder(){
        return listOrder;
    }

    private void createUIComponents() {
        splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, propertiesTab, listOrder);
        splitter.setOneTouchExpandable(true);
        splitter.setResizeWeight(0.7);
        model = new DefaultListModel<OrderContainer>();
        listOrder = new JList<OrderContainer>(model);
        listOrder.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new ResponsibleOrderViewer(worker).setVisible(true);
            }
        });
        searchComboBox = new SuggestionComboBox(performer());
        searchResult = new JList<AbstractContainer>(searchResultModel = new DefaultListModel<AbstractContainer>());
        searchResult.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                Point last = searchResult.indexToLocation(searchResult.getLastVisibleIndex());
                int i = searchResult.getSelectedIndex();
                if (last != null && p.getY() <= last.getY() + 35 && i != -1) {
                    UserDescription.getWindow().setUser((User) searchResultModel.get(i).getEntity()).setVisible(true);
                    searchResult.clearSelection();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                searchComboBox.requestFocus();
            }
        });
        searchByImage = new JPanel(new WrapLayout());
        searchResult.setVisible(true);
        scrolledImageArea = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrolledImageArea.setViewportView(new JPanel());
    }

    private SuggestionPerformer performer() {
        return new SuggestionPerformer() {
            @Override
            public void ajax(final SuggestionComboBox box, String text, final CountDownLatch latch) {
                text = text.trim();
                String uri = "?action=" + Action.GET_USER + "&";
                boolean byPhone = false;
                if (text.matches("[\\+\\(\\)\\s\\-0-9]{2,}")) {
                    uri += "phone=" + text + "&ajax=";
                    byPhone = true;
                }
                if (text.matches(Validator.NAME_VALIDATOR)) {
                    uri += "name=" + text + "&ajax=";
                }
                final boolean flag = byPhone;
                HttpManager.getManager().sendGet(uri, new Priority(Priority.MAX), new SelectAdapter() {
                    @Override
                    public void onUser(DBSelectResult res) {

                        ResultItem[] items = res.getResult();
                        for (ResultItem item : items) {
                            if (flag) {
                                box.addElement(new UserPhoneSuggestionContainer(item.getEntity()));
                            } else {
                                box.addElement(new UserNameSuggestionContainer(item.getEntity()));
                            }
                        }
                        latch.countDown();
                    }

                    @Override
                    public void doError(DBResult result) {
                        latch.countDown();
                        System.err.println(result.getText() + " " + result.getStatus() + result.actionRepresentation());
                    }
                });
            }

            @Override
            public void search(String text) {
                text = text.trim();
                if (text.length() < 3) {
                    searchResultModel.clear();
                    return;
                }
                searchResult.setVisible(false);
                searchResult.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                String uri = "?action=" + Action.GET_USER + "&";
                if (text.matches("[\\+\\(\\)\\s\\-0-9]{3,}")) {
                    uri += "phone=" + text;
                } else if (text.matches(Validator.NAME_VALIDATOR)) {
                    uri += "name=" + text;
                } else return;

                HttpManager.getManager().sendGet(uri, new Priority(Priority.MAX), new SelectAdapter() {
                    @Override
                    public void onUser(DBSelectResult res) {
                        ResultItem[] items = res.getResult();
                        if (items.length == 0) {
                            MessagesManager.errorBox("Ничего ненайдено", "Ошибка");
                            searchResultModel.clear();
                        } else {
                            searchResultModel.clear();
                            for (ResultItem item : items) {
                                User user = (User) item.getEntity();
                                searchResultModel.addElement(new UserContainer(user));
                            }
                        }
                        searchResult.setVisible(true);
                        scrolledImageArea.setVisible(false);
                        searchResult.setCursor(Cursor.getDefaultCursor());
                    }

                    @Override
                    public void doError(DBResult result) {
                        MessagesManager.errorBox("Ничего ненайдено", "Ошибка");
                        searchResultModel.clear();
                    }
                });

            }
        };
    }

    @Override
    public void setVisible(boolean b) {
        if (worker != null) {
            if  (requester == null) {
                requester = new OrderRequester(worker, model);
            }
            userName.setText(worker.getName());
        }
        super.setVisible(b);
    }

    public DefaultListModel<OrderContainer> getOrderModel() {
        return model;
    }

    public User getWorker() {
        return worker;
    }

    public void setWorker(User worker) {
        this.worker = worker;
    }

    private MouseAdapter searchByImage(){
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    JFileChooser fd = new JFileChooser();
                    if(fd.showOpenDialog(MainForm.this) != JFileChooser.APPROVE_OPTION || fd.getSelectedFile() == null) return;
                    FileInputStream in = new FileInputStream(fd.getSelectedFile());
                    byte[] buffer = new byte[in.available()];
                    in.read(buffer);
                    String hash = PHash.getSelf().hash(buffer);
                    HttpManager.getManager().getImage("hash=" + hash + "&limit=7", lookLikesReceived());
                } catch (Exception ex) {
                    MessagesManager.errorBox("Ошибка загрузки изображения", "Error");
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                byImage.setContentAreaFilled(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                byImage.setContentAreaFilled(false);
            }
        };
    }

    private SelectAdapter lookLikesReceived(){
        return new SelectAdapter(){
            @Override
            public void onImage(DBSelectResult res) {
                ResultItem[] items = res.getResult();
                searchByImage.removeAll();
                scrolledImageArea.setVisible(true);
                searchResult.setVisible(false);
                if(items.length == 0){
                    MessagesManager.errorBox("Ничего ненайдено", "ERROR");
                    return;
                }

                for(ResultItem item: items){
                    searchByImage.add(new PreviewImageLabel((edu.nure.bl.Image) item.getEntity(),
                            previewImageClicked()));
                }
                scrolledImageArea.setViewportView(searchByImage);
                panel1.repaint();
            }

            @Override
            public void doError(DBResult result) {
                MessagesManager.errorBox("Ничего ненайдено", "ERROR");
                scrolledImageArea.setVisible(true);
                searchResult.setVisible(false);
            }
        };
    }

    private MouseAdapter previewImageClicked(){
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                PreviewImageLabel l = (PreviewImageLabel)e.getSource();
                ImageView.getWindow().setReadOnly().setImage(l.getImage()).setVisible(true);
            }
        };
    }

}

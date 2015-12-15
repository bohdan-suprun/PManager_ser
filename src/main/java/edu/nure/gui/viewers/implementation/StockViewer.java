package edu.nure.gui.viewers.implementation;

import edu.nure.bl.*;
import edu.nure.bl.Image;
import edu.nure.bl.constraints.ValidationException;
import edu.nure.containers.list.FormatContainer;
import edu.nure.containers.list.NewItemContainer;
import edu.nure.containers.list.StockContainer;
import edu.nure.containers.suggestion.AbstractSuggestionContainer;
import edu.nure.db.entity.basic.Transmittable;
import edu.nure.gui.components.AlbumTab;
import edu.nure.gui.components.PreviewImageLabel;
import edu.nure.gui.components.SuggestionComboBox;
import edu.nure.gui.components.SuggestionPerformer;
import edu.nure.gui.components.model.AlbumModel;
import edu.nure.gui.image.ImageView;
import edu.nure.gui.user.UserDescription;
import edu.nure.gui.util.MessagesManager;
import edu.nure.gui.util.SaveButtonObserver;
import edu.nure.gui.viewers.AbstractViewer;
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
import java.util.concurrent.CountDownLatch;

/**
 * Created by bod on 12.10.15.
 */
public class StockViewer extends AbstractViewer {
    private static StockViewer self;
    private SuggestionComboBox formatBox;
    private JTextArea descPanel;
    private PreviewImageLabel imagePreview;
    private JLabel checkImage;
    private AlbumTab imageTab;
    private JPanel mainPanel;
    private Order order;
    private User user;

    private StockViewer(Order order, User user) {
        super("stock");
        this.order = order;
        this.user = user;
        setTitle("PManager|Задачи по заказу ");
        createPanel();
        setContent(mainPanel);
        imageTab.setVisible(true);
    }

    public static synchronized StockViewer getWindow(Order order, User user) {
        if (self == null){
            self = new StockViewer(order, user);
        }  else {
            self.setUser(user);
            self.setOrder(order);
        }

        return self;
    }

    private void createPanel(){
        mainPanel = new JPanel();
        mainPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(5, 4, new Insets(0, 0, 0, 0), -1, -1));
        final JLabel label1 = new JLabel();
        label1.setText("Формат:");
        mainPanel.add(label1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        formatBox = new SuggestionComboBox(getFormatPerformer());
        mainPanel.add(formatBox, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Описание:");
        mainPanel.add(label2, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        descPanel = new JTextArea();
        descPanel.setLineWrap(true);
        descPanel.setWrapStyleWord(false);
        mainPanel.add(descPanel, new com.intellij.uiDesigner.core.GridConstraints(1, 1, 1, 3, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Изображение:");
        mainPanel.add(label3, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        checkImage = new JLabel();
        checkImage.setText("<html><a href=\"#\">Изменить</a></html>");
        checkImage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                imageTab.setVisible(true);
            }
        });
        mainPanel.add(checkImage, new com.intellij.uiDesigner.core.GridConstraints(2, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        imagePreview = new PreviewImageLabel(null, previewClicked());
        imagePreview.setText("");
        mainPanel.add(imagePreview, new com.intellij.uiDesigner.core.GridConstraints(2, 3, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(2, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        imageTab = new AlbumTab(user, imageClicked());
        AlbumModel model1 = UserDescription.getWindow().getImageTab().getModel();
        if(model1 != null && model1.getOwner() == user) {
            imageTab.setModel(model1);
        } else {
            imageTab.getModel().reload();
        }
        mainPanel.add(imageTab, new com.intellij.uiDesigner.core.GridConstraints(3, 0, 1, 4, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer2 = new com.intellij.uiDesigner.core.Spacer();
        mainPanel.add(spacer2, new com.intellij.uiDesigner.core.GridConstraints(4, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_VERTICAL, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    }

    private MouseAdapter previewClicked(){
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                PreviewImageLabel l = (PreviewImageLabel)e.getSource();
                ImageView.getWindow().setImage(l.getImage()).setReadOnly().setVisible(true);
            }
        };
    }

    private SuggestionPerformer getFormatPerformer() {
        return new SuggestionPerformer(){
            @Override
            public void ajax(SuggestionComboBox box, String text, CountDownLatch latch) {
                HttpManager.getManager().sendGet("?action=" + Action.GET_FORMAT
                +"&name="+text, new Priority(Priority.MAX),getDefaultResponse(latch));
            }

            @Override
            public void search(String text) {

            }
        };
    }

    private SelectAdapter getDefaultResponse(final CountDownLatch latch){
        return new SelectAdapter(){
            @Override
            public void onFormat(DBSelectResult res) {
                ResultItem[] items = res.getResult();
                for (ResultItem item: items){
                    Format format = (Format)item.getEntity();
                    formatBox.addElement(new FormatContainer(format));
                }
                latch.countDown();
            }

            @Override
            public void doError(DBResult result) {
                if(result.getAction() == Action.GET_FORMAT)
                    latch.countDown();
                else MessagesManager.errorBox("Ошибка при обработке запроса: "+result.getText()+
                " КОД("+result.getStatus()+")", "Ошибка");
            }
        };


    }

    private MouseAdapter imageClicked(){
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                PreviewImageLabel l = (PreviewImageLabel)e.getSource();
                imagePreview.setImage(l.getImage());
                imageTab.setVisible(false);
                saveButton.setEnabled(true);
            }
        };
    }

    @Override
    protected void load() {
        HttpManager.getManager().sendGet("?action="+Action.GET_STOCK+"&order="+order.getId(), new Priority(Priority.MAX),
                getDefaultAdapter());
    }

    @Override
    protected void update() {
        HttpManager.getManager().sendGet(curEntity.getEntity(),Action.UPDATE_STOCK,new Priority(Priority.MAX),
                getDefaultAdapter());
    }

    @Override
    public void addItem(Transmittable t) {
        model.addElement(new StockContainer(t));
    }

    @Override
    protected void observeSaveButton() {
        SaveButtonObserver.setListeners(new JComponent[]{formatBox, descPanel},saveButton);
    }

    @Override
    protected Transmittable createEntity() throws ValidationException {
        AbstractSuggestionContainer c = formatBox.getCurrentItem();
        if(c == null) throw new ValidationException("Нужно выбрать формат для печати");
        Format f = (Format)c.getEntity();
        String desc = descPanel.getText().replace("\"", "");
        if(imagePreview.getImage() == null) throw new ValidationException("Нужно выбрать изображение");
        if(curEntity instanceof NewItemContainer) {
            return new Stock(
                    Stock.ID_NOT_SET,
                    order.getId(),
                    imagePreview.getImage().getId(),
                    desc,
                    f.getName()
            );
        } else{
            return new Stock(
                    ((Stock)curEntity.getEntity()).getId(),
                    order.getId(),
                    imagePreview.getImage().getId(),
                    desc,
                    f.getName()
            );
        }
    }

    private void loadFormat(final String  name){
        HttpManager.getManager().sendGet("?action="+Action.GET_FORMAT+"&name="+name, new Priority(Priority.MAX),
                new SelectAdapter(){
                    @Override
                    public void onFormat(DBSelectResult res) {
                        ResultItem[] items = res.getResult();
                        for(ResultItem item: items){
                            FormatContainer container = new FormatContainer(item.getEntity());
                            formatBox.addElement(container);
                            int index = formatBox.indexOf(container);
                            formatBox.setSelectedIndex(index);
                            saveButton.setEnabled(false);
                        }
                    }
                    @Override
                    public void doError(DBResult result) {
                        formatBox.resetCurrentEntity();
                        super.doError(result);
                        saveButton.setEnabled(false);
                    }
                });

    }

    private void loadImage(final int image){
        HttpManager.getManager().getImage("id="+image+"&obj=",
                new SelectAdapter(){
                    @Override
                    public void doError(DBResult result) {
                        imagePreview.setImage(null);
                        super.doError(result);
                        saveButton.setEnabled(false);
                    }

                    @Override
                    public void onImage(DBSelectResult res) {
                        ResultItem[] items = res.getResult();
                        for(ResultItem item: items){
                            imagePreview.setImage((Image)item.getEntity());
                            saveButton.setEnabled(false);
                        }
                    }
                });
    }

    @Override
    protected void entityChanged() {
        Stock s = (Stock)curEntity.getEntity();
        if(s.getDesc() != null) {
            descPanel.setText(s.getDesc());
        }
        loadFormat(((Stock)curEntity.getEntity()).getFormat());
        loadImage(((Stock)curEntity.getEntity()).getImage());
        imageTab.setVisible(false);
    }

    @Override
    protected void setDefault() {
        descPanel.setText("");
        formatBox.resetCurrentEntity();
        imagePreview.setImage(null);
        imageTab.setVisible(!isReadOnly());

    }

    @Override
    protected void onCancel(){
        OrderViewer o = OrderViewer.getWindow();
        if(o != null) o.setVisible(true);
        super.onCancel();
    }

    public void setUser(User u){
        user = u;
        imageTab.setUser(user);
    }

    public void setOrder(Order o){
        order = o;
    }
}

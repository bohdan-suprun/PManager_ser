package edu.nure.gui.viewers;

import edu.nure.bl.constraints.ValidationException;
import edu.nure.containers.list.AbstractContainer;
import edu.nure.containers.list.NewItemContainer;
import edu.nure.db.entity.basic.Transmittable;
import edu.nure.gui.user.UserDescription;
import edu.nure.gui.util.MessagesManager;
import edu.nure.listener.ResponseAdapter;
import edu.nure.net.HttpManager;
import edu.nure.net.request.Priority;
import edu.nure.net.results.DBResult;
import edu.nure.net.results.DBSelectResult;
import edu.nure.net.results.ResultItem;
import edu.nure.util.Action;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Field;

abstract public class AbstractViewer extends JDialog {
    protected JButton buttonCancel;
    protected JButton saveButton;
    protected DefaultListModel<AbstractContainer> model;
    protected AbstractContainer curEntity;
    protected Transmittable oldValue;
    protected int getAct, insAct, updAct, delAct;
    protected JList<AbstractContainer> itemList;
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton deleteButton;
    private JSplitPane splitPane;
    private JPanel viewContent;
    private boolean readOnly = false;

    public AbstractViewer(String entityName) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

//  call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

//  call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        setActionsId(entityName);

        saveButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(saveButton.isEnabled() && !readOnly) {
                    int b = MessagesManager.questionBox("Сохранить запись?", "Сохранить", new String[]{"Да", "Нет"});
                    if (b == 0)
                        save();
                } else{
                    if(readOnly)
                        MessagesManager.errorBox("Разрешено только чтение", "Ошибка");
                }
            }
        });

        deleteButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(deleteButton.isEnabled() && !readOnly) {
                    int b = MessagesManager.questionBox("Вы действительно хотите удалить запись?", "Удалить", new String[]{"Да", "Нет"});
                    oldValue = model.getElementAt(itemList.getSelectedIndex()).getEntity();
                    if (b == 0)
                        delete();
                }else{
                    if(readOnly)
                        MessagesManager.errorBox("Разрешено только чтение", "Ошибка");
                }
            }
        });

        itemList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                if (itemList.getSelectedIndex() > -1) {
                    setCurrentEntity(model.elementAt(itemList.getSelectedIndex()));
                    saveButton.setEnabled(false);
                    deleteButton.setEnabled(itemList.getSelectedIndex() > 0);
                }
            }
        });
        setSize(new Dimension(UserDescription.getWindow().getWidth(),
                UserDescription.getWindow().getHeight()));
        splitPane.setResizeWeight(0.5);
        curEntity = new NewItemContainer();
    }

    private void createUIComponents() {
        model = new DefaultListModel<AbstractContainer>();
        itemList = new JList<AbstractContainer>(model);
        viewContent = new JPanel();
    }

    protected void onCancel() {
        if (saveButton.isEnabled() && !readOnly) {
            int b = MessagesManager.questionBox("Остались не сохраненные данные, сохранить?", "Сохранить", new String[]{"Да", "Нет"});
            if (b == 0)
                save();
        }
        dispose();
    }

    @Override
    public void setVisible(boolean v) {
        if (v) {
            reload();
        }
        super.setVisible(v);
    }

    protected void reload() {
        model.clear();
        curEntity = new NewItemContainer();
        model.addElement(new NewItemContainer());
        itemList.setSelectedIndex(0);
        itemList.repaint();
        load();
        deleteButton.setEnabled(false);
        saveButton.setEnabled(false);
    }

    private void save() {
        try {
            if (saveButton.isEnabled() && !readOnly) {
                Transmittable newEnt = createEntity();
                if (curEntity instanceof NewItemContainer) {
                    curEntity.setEntity(newEnt);
                    insert();
                } else {
                    oldValue = curEntity.getEntity();
                    curEntity.setEntity(newEnt);
                    update();
//                    reload();
                }
            }
        } catch (ValidationException ex) {
            MessagesManager.errorBox("Ошибка: " + ex.getMessage(), "Ошибка");
        }
    }

    public void setContent(JPanel panel) {
        viewContent.add(panel, BorderLayout.CENTER);
        observeSaveButton();
    }

    protected void setActionsId(String ent) {
        ent = ent.toUpperCase();
        try {
            try {
                Field f = Action.class.getDeclaredField("GET_" + ent);
                getAct = f.getInt(new Action());
            } catch (NoSuchFieldException ex) {
                getAct = -1;
            }

            try {
                Field f = Action.class.getDeclaredField("INSERT_" + ent);
                insAct = f.getInt(new Action());
            } catch (NoSuchFieldException ex) {
                insAct = -1;
            }

            try {
                Field f = Action.class.getDeclaredField("UPDATE_" + ent);
                updAct = f.getInt(new Action());
            } catch (NoSuchFieldException ex) {
                updAct = -1;
            }

            try {
                Field f = Action.class.getDeclaredField("DELETE_" + ent);
                delAct = f.getInt(new Action());
            } catch (NoSuchFieldException ex) {
                delAct = -1;
            }
        } catch (IllegalAccessException ex) {
            System.err.println(ex.getMessage());
        }
    }

    protected ResponseAdapter getDefaultAdapter() {
        return new ResponseAdapter() {
            @Override
            public void doSelect(DBSelectResult result) {
                for (ResultItem item : result.getResult()) {
                    addItem(item.getEntity());
                }
                deleteButton.setEnabled(model.getSize() > 1);
                itemList.repaint();
            }

            @Override
            public void doInsert(DBSelectResult result) {
                addItem(result.getResult()[0].getEntity());
                itemList.setSelectedIndex(model.size() - 1);
                deleteButton.setEnabled(model.getSize() > 1);
                saveButton.setEnabled(false);
                MessagesManager.infoBox("Операция прошла успешно", "Вставка");
            }

            @Override
            public void doUpdate(DBResult result) {
                for (int i = 0; i < model.getSize(); i++) {
                    if (oldValue.equals(model.getElementAt(i).getEntity())) {
                        model.getElementAt(i).setEntity(curEntity.getEntity());
                    }
                }
                MessagesManager.infoBox("Операция прошла успешно", "Обновление");
                saveButton.setEnabled(false);
                itemList.repaint();
            }

            @Override
            public void doDelete(DBResult result) {
                for (int i = 0; i < model.getSize(); i++) {
                    if (model.getElementAt(i).getEntity() == oldValue) {
                        model.removeElementAt(i);
                    }
                }
                itemList.repaint();
                deleteButton.setEnabled(model.getSize() > 1);
                MessagesManager.infoBox("Операция прошла успешно", "Удаление");
            }

            @Override
            public void doError(DBResult result) {
                MessagesManager.errorBox("Ошибка. " + result.getText(), "Ошибка");
            }
        };
    }

    protected void setCurrentEntity(AbstractContainer container) {
        curEntity = container;
        if (curEntity instanceof NewItemContainer) {
            setDefault();
            saveButton.setEnabled(false);
        } else {
            entityChanged();
            saveButton.setEnabled(false);
        }
    }

    protected void insert() {
        HttpManager.getManager().sendGet(curEntity.getEntity(), insAct, new Priority(Priority.MIDDLE),
                getDefaultAdapter());
    }

    protected void delete() {
        HttpManager.getManager().sendGet(curEntity.getEntity(), delAct, new Priority(Priority.MIDDLE),
                getDefaultAdapter());
    }

    public boolean isReadOnly(){
        return readOnly;
    }

    public AbstractViewer setReadOnly(boolean r) {
        readOnly = r;
        return this;
    }

    /*
    * abstract methods
     */

    abstract protected void load();

    abstract protected void update();

    abstract public void addItem(Transmittable t);

    abstract protected void observeSaveButton();

    // Create the entity from view
    abstract protected Transmittable createEntity() throws ValidationException;

    abstract protected void entityChanged();

    abstract protected void setDefault();

}



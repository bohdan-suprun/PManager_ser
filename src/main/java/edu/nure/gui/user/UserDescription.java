package edu.nure.gui.user;

import edu.nure.bl.Right;
import edu.nure.bl.User;
import edu.nure.bl.constraints.ValidationException;
import edu.nure.gui.components.AlbumTab;
import edu.nure.gui.components.EditableJLabel;
import edu.nure.gui.components.PreviewImageLabel;
import edu.nure.gui.image.ImageView;
import edu.nure.gui.mainwindow.MainForm;
import edu.nure.gui.util.MessagesManager;
import edu.nure.gui.util.SaveButtonObserver;
import edu.nure.gui.viewers.implementation.OrderViewer;
import edu.nure.listener.ResponseAdapter;
import edu.nure.net.HttpManager;
import edu.nure.net.request.Priority;
import edu.nure.net.results.DBResult;
import edu.nure.net.results.DBSelectResult;
import edu.nure.util.Action;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.*;
import java.text.ParseException;

public class UserDescription extends JDialog {
    private static UserDescription self;
    private JPanel contentPane;
    private JButton buttonCancel;
    private JButton saveButton;
    private JSplitPane mainPane;
    private JButton userOrderButton;
    private JLabel userId;
    private JFormattedTextField userPhone;
    private JPanel userEmail;
    private JComboBox<String> userRight;
    private JPanel userName;
    private User descriptor;
    private AlbumTab imagePanel;

    private UserDescription(final User descriptor) {
        this.descriptor = descriptor;

        setContentPane(contentPane);
        setModal(true);

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

        saveButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(!saveButton.isEnabled()) return;
                int n = MessagesManager.questionBox("Сохранить пользователя?", "Несохранены изменения",
                        new String[]{"Да", "Нет"});
                if (n == 0)
                    saveUser();
            }
        });

        setBounds(MainForm.getMainForm().getBounds());
        setListenersForFields();
        userOrderButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(UserDescription.this.descriptor != null)
                    OrderViewer.getWindow(UserDescription.this.descriptor).setVisible(true);
            }
        });
        imagePanel.setUser(descriptor);
    }

    public static UserDescription getWindow() {
        return (self == null) ? self = new UserDescription(null) : self;
    }

    private void onCancel() {
        if (saveButton.isEnabled()) {
            int n = MessagesManager.questionBox("Были внесены изменения.\nСохранить?", "Несохранены изменения",
                    new String[]{"Да", "Нет"});
            if (n == 0)
                saveUser();
        }
        descriptor = null;
        setCursor(Cursor.getDefaultCursor());
        dispose();
    }

    public User getUser() {
        return descriptor;
    }

    public UserDescription setUser(User u) {
        descriptor = u;
        setValues();
        manageEnable();
        return self;
    }

    public AlbumTab getImageTab(){
        return imagePanel;
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            setValues();
            imagePanel.getModel().reload();
        }
        super.setVisible(b);
    }

    private void createUIComponents() {
        mainPane = new JSplitPane();
        mainPane.setOneTouchExpandable(true);
        mainPane.setResizeWeight(0.15);
        mainPane.setRightComponent(imagePanel = new AlbumTab(descriptor, new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                PreviewImageLabel label = (PreviewImageLabel)e.getSource();
                ImageView.getWindow().setImage(label.getImage()).setVisible(true);
            }
        })); // set listener
        try {
            userName = new EditableJLabel("<unknown>");
            userEmail = new EditableJLabel("<unknown>");
            userPhone = new JFormattedTextField(new MaskFormatter("+38(###) ###-##-##"));
        } catch (ParseException ex) {
            System.err.println(ex.getMessage());
        }
    }

    private void manageEnable() {
        saveButton.setEnabled(false);
    }

    private void setListenersForFields() {
        SaveButtonObserver.setListeners(new JComponent[]{
                userName, userEmail, userPhone, userRight
        }, saveButton);
    }

    private void setValues() {
        String defValue = "<unknown>";
        if (descriptor != null) {
            userId.setText(String.valueOf(descriptor.getId()));
            ((EditableJLabel) userName).setText(descriptor.getName());
            ((EditableJLabel) userEmail).setText((descriptor.getEmail() == null) ? defValue : descriptor.getEmail());
            userPhone.setValue(descriptor.getPhone());

            for (int i = 0; i < userRight.getModel().getSize(); i++) {
                if (userRight.getModel().getElementAt(i).toLowerCase().equals(descriptor.getRight().getType())) {
                    userRight.setSelectedIndex(i);
                }
            }
            setTitle("PManager|" + descriptor.getName());
        } else {
            userId.setText("" + User.ID_NOT_SET);
            ((EditableJLabel) userName).setText(defValue);
            ((EditableJLabel) userEmail).setText(defValue);
            userPhone.setValue("");
            userRight.setSelectedIndex(-1);
            setTitle("PManager|" + defValue);
        }
        imagePanel.setUser(descriptor);
        manageEnable();
    }

    private void saveUser() {
        try {
            User user = createUser();
            if (user.getId() == User.ID_NOT_SET) {
                HttpManager.getManager()
                        .sendGet(user, Action.REGISTRY, new Priority(Priority.MIDDLE), doRegistry());

            } else {
                HttpManager.getManager()
                        .sendGet(user, Action.UPDATE_USER, new Priority(Priority.MIDDLE), doUpdate());
            }
            saveButton.setEnabled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        } catch (ValidationException ex) {
            MessagesManager.errorBox("Неверный формат данных " + ex.getMessage(), "Ошибка формата");

        }

    }

    private ResponseAdapter doUpdate() {
        return new ResponseAdapter() {
            @Override
            public void doUpdate(DBResult result) {
                MessagesManager.infoBox("Данные успешно были обновлены", "Обновление");
                setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void doError(DBResult result) {
                MessagesManager.errorBox("Ошибка обновления пользователя: " + result.getText() + " КОД(" + result.getStatus() + ")",
                        "Ошибка");
                setValues();
                setCursor(Cursor.getDefaultCursor());
            }
        };
    }

    private User createUser() throws ValidationException {
        String email = ((EditableJLabel) userEmail).getText();
        if (email.equals("<unknown>") || email.isEmpty()) email = null;
        String right = userRight.getItemAt(userRight.getSelectedIndex());
        return new User(
                Integer.valueOf(userId.getText()),
                ((EditableJLabel) userName).getText(),
                userPhone.getText(),
                email,
                null,
                new Right(right, null)
        );
    }

    private ResponseAdapter doRegistry() {
        return new ResponseAdapter() {
            @Override
            public void doInsert(DBSelectResult result) {
                User user = (User) result.getResult()[0].getEntity();
                setUser(user);
                setCursor(Cursor.getDefaultCursor());
                MessagesManager.infoBox("Операция создания пользователя прошла успешно", "Регистрация");
            }

            @Override
            public void doError(DBResult result) {
                setCursor(Cursor.getDefaultCursor());
                MessagesManager.errorBox("Ошибка во время создания нового пользователя " + result.getText() +
                        " КОД(" + result.getStatus() + ")", "Ошибка");
                saveButton.setEnabled(true);
            }
        };
    }


}

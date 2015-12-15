package edu.nure.gui.loginform;

import edu.nure.bl.User;
import edu.nure.bl.constraints.ValidationException;
import edu.nure.bl.constraints.Validator;
import edu.nure.gui.mainwindow.MainForm;
import edu.nure.gui.util.MessagesManager;
import edu.nure.listener.SelectAdapter;
import edu.nure.net.HttpManager;
import edu.nure.net.results.DBResult;
import edu.nure.net.results.DBSelectResult;
import edu.nure.net.results.ResultItem;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

/**
 * Created by bod on 02.10.15.
 */
public class LoginForm extends JFrame {
    private JPanel panel1;
    private JFormattedTextField phone;
    private JPasswordField password;
    private JButton loginButton;
    private JLabel image;

    public LoginForm() {
        setTitle("PManager | Авторизация");
        setContentPane(panel1);
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - 600) / 2, (screenSize.height - 300) / 2, 600, 300);
    }

    private void createUIComponents() {
        try {
            image = new JLabel(new ImageIcon("resources/cam.png"));
            phone = new JFormattedTextField(new MaskFormatter("+38(###) ###-##-##"));
            loginButton = new JButton("ВОЙТИ");
            loginButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        Validator.validate(String.valueOf(password.getPassword()), Validator.PASS_VALIDATOR);
                        Validator.validate(phone.getText(), Validator.PHONE_VALIDATOR);
                    } catch (ValidationException ex) {
                        MessagesManager.errorBox(ex.getMessage(), "Ошибка");
                        return;
                    }

                    HttpManager.getManager().login(phone.getText(), hashPass(password.getPassword()), new SelectAdapter() {
                        @Override
                        public void onLogin(DBSelectResult res) {
                            ResultItem user[] = res.getResult();
                            setVisible(false);
                            repaint();
                            MainForm.getMainForm().setWorker(((User) user[0].getEntity()));
                            MainForm.getMainForm().setVisible(true);
                        }

                        @Override
                        public void doError(DBResult result) {
                            if (result.getText() == null) {
                                if (result.getStatus() == 500)
                                    MessagesManager.errorBox("Нет такого пользователя", "Ошибка");
                                else
                                    MessagesManager.errorBox("Ошибка при выполнении запроса", "Ошибка");
                            } else {
                                MessagesManager.errorBox(result.getText(), "Ошибка");
                            }
                            panel1.setEnabled(true);
                            repaint();
                        }
                    });
                    MessagesManager.handleError();
                    MessagesManager.handleDMLSuccess();
                }
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private String hashPass(char[] pass) {
        String password = String.valueOf(pass);
        try {
            byte[] p = MessageDigest.getInstance("MD5").digest(password.getBytes());
            return toHex(p);
        } catch (NoSuchAlgorithmException e) {
            return "non";
        }
    }

    private String toHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            if ((0xff & hash[i]) < 0x10)
                hexString.append("0" + Integer.toHexString((0xFF & hash[i])));
            else
                hexString.append(Integer.toHexString(0xFF & hash[i]));
        }
        return hexString.toString();
    }

}

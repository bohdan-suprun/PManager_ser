package edu.nure.gui.util;

import edu.nure.listener.ResponseAdapter;
import edu.nure.net.HttpManager;
import edu.nure.net.results.DBResult;
import edu.nure.net.results.DBSelectResult;

import javax.swing.*;

/**
 * Created by bod on 02.10.15.
 */
public class MessagesManager {

    public static void handleError(){
        HttpManager.getManager().addResponseListener(new ResponseAdapter(){
            @Override
            public void doError(DBResult result) {
                String message;
                if(result.getStatus() < 600) {
                    message = "Ошибка при ";
                    String act = result.actionRepresentation();
                    if (act.contains("GET")) message = "Ошибка вовремя выполнения запроса";
                    if (act.contains("INSERT")) message += "вставке новых значений";
                    if (act.contains("UPDATE")) message += "при обновлении данных";
                    if (act.contains("DELETE")) message += "при удалении данных";
                    if (act.contains("LOGIN")) message += "при входе";
                    if (act.contains("REGISTRY")) message += "при создании нового пользователя";
                    message += ". ";

                    if (result.getText() != null)
                        message += result.getText() + ". ";
                    message += "КОД(" + result.getStatus() + ")";
                }else {
                    message = result.getText()+" КОД("+result.getStatus()+")";
                }
                errorBox(message, "Ошибка");
            }
        });
    }

    public static void handleDMLSuccess(){
        HttpManager.getManager().addResponseListener(new ResponseAdapter(){
            @Override
            public void doInsert(DBSelectResult result) {
                infoBox("Создание выполнилась успешно","PManager");
            }

            @Override
            public void doUpdate(DBResult result) {
                infoBox("Обновлени выполнилось успешно", "PManager");
            }

            @Override
            public void doDelete(DBResult result) {
                infoBox("Удаление выполнилась успешно", "PManager");
            }
        });
    }

    public static void infoBox(String infoMessage, String titleBar)
    {
        JOptionPane.showMessageDialog(null, infoMessage, "PManager: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void errorBox(String infoMessage, String titleBar)
    {
        JOptionPane.showMessageDialog(null, infoMessage, "PManager: " + titleBar, JOptionPane.ERROR_MESSAGE);
    }

    public static int questionBox(String question,String title, String[] buttons){
        return JOptionPane.showOptionDialog(null, question, title, JOptionPane.YES_OPTION,
                JOptionPane.QUESTION_MESSAGE,null,buttons,buttons[0]);
    }

    public static String inputBox(String info, String title){
        String s = (String)JOptionPane.showInputDialog(
                null,
                info,title,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "новый альбом");
        return s;
    }
}

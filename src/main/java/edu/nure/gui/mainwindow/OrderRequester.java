package edu.nure.gui.mainwindow;

import edu.nure.bl.Order;
import edu.nure.bl.User;
import edu.nure.containers.list.OrderContainer;
import edu.nure.listener.ResponseListener;
import edu.nure.listener.SelectAdapter;
import edu.nure.net.HttpManager;
import edu.nure.net.request.Priority;
import edu.nure.net.results.DBSelectResult;
import edu.nure.net.results.ResultItem;
import edu.nure.util.Action;

import javax.swing.*;

/**
 * Created by bod on 02.10.15.
 */
public class OrderRequester extends Thread {
    private User owner;
    private DefaultListModel<OrderContainer> model;


    public OrderRequester(User owner, DefaultListModel<OrderContainer> model) {
        this.owner = owner;
        this.model = model;
        setDaemon(true);
        setPriority(Thread.MIN_PRIORITY);
        start();
    }

    @Override
    public void run() {
        try {
            while (true) {
                HttpManager.getManager().sendGet("?action=" + Action.GET_ORDER + "&responsible=" +
                                owner.getId() + "&active=", new Priority(Priority.MIN),
                        getListener());
                Thread.sleep(60000);

            }
        } catch (InterruptedException ex) {
            System.err.println("interrupted");
        }
    }

    private ResponseListener getListener() {
        return new SelectAdapter() {
            @Override
            public void onOrder(DBSelectResult result) {
                DBSelectResult res = result;
                MainForm.getMainForm().getOrderModel().clear();
                for (ResultItem item : res.getResult()) {
                    Order order = (Order) item.getEntity();
                    model.addElement(new OrderContainer(order));
                }
            }
        };
    }
}
package edu.nure.gui.viewers.implementation;

import edu.nure.bl.Order;
import edu.nure.bl.User;
import edu.nure.gui.util.MessagesManager;
import edu.nure.containers.list.NewItemContainer;
import edu.nure.util.Action;
import edu.nure.listener.SelectAdapter;
import edu.nure.net.results.DBResult;
import edu.nure.net.results.DBSelectResult;
import edu.nure.net.results.ResultItem;
import edu.nure.net.HttpManager;
import edu.nure.net.request.Priority;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by bod on 15.10.15.
 */
public class ResponsibleOrderViewer extends OrderViewer {

    public ResponsibleOrderViewer(User resp) {
        super(resp);
        setReadOnly(true);
        titlePlayerFirst.setText("Ответственный");
        titlePlayerSecond.setText("Покупатель");
    }


    @Override
    protected void load() {
            HttpManager.getManager().sendGet("?action="+getAct+"&responsible="+ owner.getId()+"&active=",new Priority(Priority.MAX),
                    getDefaultAdapter());

    }

    @Override
    protected MouseAdapter taskClicked(){
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(!(curEntity instanceof NewItemContainer)) {
                    getCustomerAndShowWindow();
                }
            }
        };
    }

    protected void getCustomerAndShowWindow(){
        final Order o = (Order)curEntity.getEntity();
        HttpManager.getManager().sendGet("?action="+ Action.GET_USER+"&id="+o.getCustomer(), new Priority(Priority.MAX),
                new SelectAdapter(){
                    @Override
                    public void onUser(DBSelectResult res) {
                        ResultItem[] items = res.getResult();
                        if(items.length > 0) {
                            System.err.println(items[0].getEntity().toQuery());
                            StockViewer.getWindow(o, (User) items[0].getEntity()).setReadOnly(true).setVisible(true);;
                        }

                    }
                    @Override
                    public void doError(DBResult result) {
                        MessagesManager.errorBox("Невозможно просмотреть задачи", "ERROR");
                    }
                });

    }

    @Override
    protected void entityChanged() {
        Order o = (Order)curEntity.getEntity();
        super.entityChanged();
        loadUser(o.getCustomer());

    }
}

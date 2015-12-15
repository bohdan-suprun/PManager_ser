package edu.nure.containers.list;

import edu.nure.bl.Order;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by bod on 02.10.15.
 */
public class OrderContainer extends  AbstractContainer{

    public OrderContainer(Order order) {
        super(order);
    }

    protected String wrap(){
        Order order = (Order)t;
        Date now = new Date();
        Date term = order.getTerm();
        long dif = (term.getTime() - now.getTime())/(1000*60);
        String date = new SimpleDateFormat("HH:mm EEE dd MMMM yyyy").format(term);
        if(dif <= 15)
            return "<html><strong>Строк сдачи: </strong><font color=\"red\" size=4/>"+ date +"</font></html>";
        if(dif <= 60)
            return "<html><strong>Строк сдачи: </strong><font color=\"green\" size=4/>"+date+"</font></html>";
        return "<html><strong>Строк сдачи: </strong>"+date+"</html>";
    }
}

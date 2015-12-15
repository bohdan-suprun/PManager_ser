package edu.nure.containers.list;

import edu.nure.bl.Stock;
import edu.nure.db.entity.basic.Transmittable;

/**
 * Created by bod on 15.10.15.
 */
public class StockContainer extends AbstractContainer {

    public StockContainer(Transmittable t) {
        super(t);
    }

    @Override
    protected String wrap() {
        Stock s = (Stock)t;
        return "<html><strong>ID задания </strong><font color=\"blue\" size=\"4\">"+s.getId()+"</font></html>";
    }
}

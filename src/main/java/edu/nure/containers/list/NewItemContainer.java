package edu.nure.containers.list;

/**
 * Created by bod on 15.12.15.
 */
public class NewItemContainer extends AbstractContainer {

    public NewItemContainer() {
        super(null);
    }

    @Override
    protected String wrap() {
        return "<html><font color=\"red\">+</font><strong>Добавить запись</strong></html>";
    }
}

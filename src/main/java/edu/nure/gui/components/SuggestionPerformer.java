package edu.nure.gui.components;

import java.util.concurrent.CountDownLatch;

/**
 * Created by bod on 10.10.15.
 */
abstract public class  SuggestionPerformer{

    public abstract void ajax(final SuggestionComboBox box, final String text, final CountDownLatch latch);
    public abstract void search(final String text);

}

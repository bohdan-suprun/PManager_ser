package edu.nure.listener;

import edu.nure.net.results.DBResult;
import edu.nure.net.results.DBSelectResult;

/**
 * Created by bod on 01.10.15.
 */
public class ResponseAdapter implements ResponseListener {
    @Override
    public void doSelect(DBSelectResult result) {

    }

    @Override
    public void doInsert(DBSelectResult result) {

    }

    @Override
    public void doUpdate(DBResult result) {

    }

    @Override
    public void doDelete(DBResult result) {

    }

    @Override
    public void doError(DBResult result) {
        System.err.println(result.getStatus() + " " + result.getText());
    }

    @Override
    public void doBinaryImage(byte[] image) {

    }
}

package edu.nure.listener;

import edu.nure.net.results.DBSelectResult;
import edu.nure.util.Action;

/**
 * Created by bod on 01.10.15.
 */
public class SelectAdapter extends ResponseAdapter implements SelectListener{
    @Override
    final public void doSelect(DBSelectResult result) {
        switch (result.getAction()){
            case Action.GET_ORDER:
                onOrder(result);
                break;
            case Action.GET_IMAGE:
                onImage(result);
                break;
            case Action.GET_USER:
                onUser(result);
                break;
            case Action.LOGIN:
                onLogin(result);
                break;
            case Action.GET_FORMAT:
                onFormat(result);
                break;
            case Action.GET_RIGHT:
                onRight(result);
                break;
            case Action.GET_URGENCY:
                onUrgency(result);
                break;
            case Action.GET_STOCK:
                onStock(result);
                break;
            case Action.GET_ALBUM:
                onAlbum(result);
        }


    }
    @Override
    public void onOrder(DBSelectResult result) {

    }
    @Override
    public void onImage(DBSelectResult res) {

    }

    @Override
    public void onUser(DBSelectResult res) {

    }

    @Override
    public void onFormat(DBSelectResult res) {

    }

    @Override
    public void onRight(DBSelectResult res) {

    }

    @Override
    public void onUrgency(DBSelectResult res) {

    }

    @Override
    public void onStock(DBSelectResult res) {

    }

    @Override
    public void onLogin(DBSelectResult res) {

    }

    @Override
    public void onAlbum(DBSelectResult res) {

    }
}

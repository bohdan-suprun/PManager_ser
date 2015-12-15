package edu.nure.listener;

import edu.nure.net.results.DBSelectResult;

/**
 * Created by bod on 01.10.15.
 */
public interface SelectListener {

    void onOrder(DBSelectResult result);
    void onImage(DBSelectResult res);
    void onUser(DBSelectResult res);
    void onFormat(DBSelectResult res);
    void onRight(DBSelectResult res);
    void onUrgency(DBSelectResult res);
    void onStock(DBSelectResult res);
    void onLogin(DBSelectResult res);
    void onAlbum(DBSelectResult res);

}

package edu.nure.containers.list;

import edu.nure.bl.User;
import edu.nure.db.entity.basic.Transmittable;

/**
 * Created by bod on 03.10.15.
 */

public class UserContainer extends AbstractContainer{

    public UserContainer(Transmittable user) {
        super(user);
    }

    protected String wrap(){
        User user = (User)t;
        return "<html><font size=\"4\" color=\"blue\">"+user.getName()+"</font>" +
                        "<br/><strong>Телефон:</strong>"+
                        "<font size=\"2\" color=\"green\">"+user.getPhone()+
                        "</font></html>";
    }
}
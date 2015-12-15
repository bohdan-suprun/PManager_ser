package edu.nure.containers.suggestion;

import edu.nure.bl.User;
import edu.nure.db.entity.basic.Transmittable;

/**
 * Created by bod on 10.10.15.
 */
public class UserNameSuggestionContainer extends AbstractSuggestionContainer {

    public UserNameSuggestionContainer(Transmittable t) {
        super(t);
    }

    @Override
    public String getString() {
        return ((User)t).getName();
    }

    @Override
    protected String wrap() {
        User user = (User)t;
        return user.getName();
    }
}

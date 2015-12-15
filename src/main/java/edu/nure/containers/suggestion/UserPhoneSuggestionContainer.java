package edu.nure.containers.suggestion;

import edu.nure.bl.User;
import edu.nure.db.entity.basic.Transmittable;

/**
 * Created by bod on 10.10.15.
 */
public class UserPhoneSuggestionContainer extends UserNameSuggestionContainer {

    public UserPhoneSuggestionContainer(Transmittable t) {
        super(t);
    }

    @Override
    public String getString() {
        return ((User)t).getPhone().replaceAll("[^0-9]","");
    }

    @Override
    public String displayString() {
        return ((User)t).getPhone();
    }

    @Override
    protected String wrap() {
        return ((User)t).getPhone();
    }
}

package edu.nure.containers.list;

import edu.nure.bl.Format;
import edu.nure.containers.suggestion.AbstractSuggestionContainer;
import edu.nure.db.entity.basic.Transmittable;

/**
 * Created by bod on 13.10.15.
 */
public class FormatContainer extends AbstractSuggestionContainer {

    public FormatContainer(Transmittable t) {
        super(t);
    }

    @Override
    public String getString() {
        return ((Format)t).getName();
    }

    @Override
    protected String wrap() {
        return getString();
    }
}

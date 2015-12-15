package edu.nure.containers.list;

import edu.nure.bl.Urgency;
import edu.nure.containers.suggestion.AbstractSuggestionContainer;
import edu.nure.db.entity.basic.Transmittable;

/**
 * Created by bod on 10.10.15.
 */
public class UrgencyContainer extends AbstractSuggestionContainer {

    public UrgencyContainer(Transmittable t) {
        super(t);
    }

    @Override
    protected String wrap() {
        int time = ((Urgency) t).getTerm();

        return getDay(time) + " " + getHour(time) + " " + getMin(time);
    }

    private String getMin(int t) {
        return (t % 60) + " мин.";
    }

    private String getHour(int t) {
        return ((t / 60) % 24) + " часов";
    }

    private String getDay(int t) {
        return (t / (60 * 24)) + " дней";
    }

    @Override
    public String getString() {
        return displayString();
    }

    @Override
    public String displayString() {
        return wrap();
    }
}

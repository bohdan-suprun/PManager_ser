package edu.nure.net.results;

import edu.nure.db.entity.basic.Transmittable;

/**
 * Created by bod on 01.10.15.
 */
public class ResultItem {
    private Transmittable ent;
    private Class<?> className;

    public ResultItem(Class<?> className, Transmittable ent) {
        this.ent = ent;
        this.className = className;
    }

    public Transmittable getEntity() {
        return ent;
    }

    public Class<?> getClassName() {
        return className;
    }

    public String toString(){
        return className.getName()+" "+ent.toXML();
    }
}

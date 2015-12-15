package edu.nure.net.request;


/**
 * Created by bod on 30.09.15.
 */
public class Priority implements Comparable<Priority> {
    public static final int MIN = 1;
    public static final int MIDDLE = 5;
    public static final int MAX = 10;
    private int priority;

    public Priority(int priority){
        if(priority <= MAX && priority >= MIN)
            this.priority = priority;
        else this.priority = MIN;
    }

    public Priority(){
        this.priority = MIDDLE;
    }

    public int getPri(){
        return priority;
    }

    @Override
    public int compareTo(Priority pri) {
        return - Integer.valueOf(this.priority).compareTo(pri.getPri());
    }
}

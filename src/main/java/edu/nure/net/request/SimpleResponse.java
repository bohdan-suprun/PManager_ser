package edu.nure.net.request;

import edu.nure.listener.ResponseListener;

/**
 * Created by bod on 01.10.15.
 */
public class SimpleResponse implements Comparable<SimpleResponse> {
    private byte[] response;
    private Priority priority;
    private ResponseListener performer;

    SimpleResponse(byte[] response, Priority pri){
        this.response = response;
        this.priority = pri;
    }

    public SimpleResponse(ResponseListener performer, byte[] response, Priority priority) {
        this.performer = performer;
        this.response = response;
        this.priority = priority;
    }

    @Override
    public int compareTo(SimpleResponse simpleResponse) {
        return priority.compareTo(simpleResponse.getPriority());
    }

    public Priority getPriority() {
        return priority;
    }

    public byte[] getResponse() {
        return response;
    }

    public ResponseListener getPerformer() {
        return performer;
    }
}

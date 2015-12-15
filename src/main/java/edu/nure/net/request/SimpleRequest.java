package edu.nure.net.request;

import edu.nure.listener.ResponseListener;
import org.apache.http.client.methods.HttpRequestBase;

/**
 * Created by bod on 01.10.15.
 */
public class SimpleRequest implements Comparable<SimpleRequest> {
    private HttpRequestBase request;
    private Priority priority;
    private ResponseListener performer;

    SimpleRequest(HttpRequestBase req, Priority pri){
        request = req;
        priority = pri;
    }

    public SimpleRequest(HttpRequestBase request, Priority priority, ResponseListener performer) {
        this.request = request;
        this.priority = priority;
        this.performer = performer;
    }

    public HttpRequestBase getRequest() {
        return request;
    }

    @Override
    public int compareTo(SimpleRequest simpleRequest) {
        return priority.compareTo(simpleRequest.getPriority());
    }

    public Priority getPriority() {
        return priority;
    }

    public ResponseListener getPerformer() {
        return performer;
    }
}

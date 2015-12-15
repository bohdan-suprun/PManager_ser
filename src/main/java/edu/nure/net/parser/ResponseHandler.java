package edu.nure.net.parser;

import edu.nure.listener.ResponseListener;
import edu.nure.net.request.SimpleResponse;
import edu.nure.net.results.DBResult;
import edu.nure.net.results.DBSelectResult;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by bod on 01.10.15.
 */

public class ResponseHandler extends Thread {
    private static PriorityBlockingQueue<SimpleResponse> tasks;
    private static List<ResponseListener> listeners;

    static {
        tasks = new PriorityBlockingQueue<>();
        listeners = Collections.synchronizedList(new ArrayList<ResponseListener>());
    }

    public ResponseHandler() {
        setDaemon(true);
    }

    public static void putTask(SimpleResponse resp) {
        tasks.add(resp);
    }

    public static void addListener(ResponseListener listener) {
        listeners.add(listener);
    }

    public static void notifyListeners(DBResult result) {
        for (ResponseListener listener : listeners) {
            chooseMethod(result, listener);
        }
    }

    public static void notifyMe(DBResult result, ResponseListener listener) {
        chooseMethod(result, listener);
    }

    private static void chooseMethod(DBResult result, ResponseListener listener) {
        int status = result.getStatus();
        int action = result.getAction();
        if (status == 200) {

            if (action >= 100 && action <= 199) listener.doInsert((DBSelectResult) result);
            if (action >= 200 && action <= 299 || action == 1) listener.doSelect((DBSelectResult) result);
            if (action >= 300 && action <= 399) listener.doUpdate(result);
            if (action >= 400 && action <= 499) listener.doDelete(result);
        } else listener.doError(result);
    }

    @Override
    public void run() {
        try {
            while (true) {
                SimpleResponse response = tasks.take();
                parse(response);
            }
        } catch (InterruptedException ex) {
            System.err.println("Interrupted");
        }
    }

    private void parse(SimpleResponse response) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLParser handler = new XMLParser(response.getPerformer());
            parser.parse(new ByteArrayInputStream(response.getResponse()), handler);

        } catch (Exception e) {
            e.printStackTrace();
            notifyListeners(new DBResult(-1, 600, "Ошибка чтения ответа сервера"));
        }


    }
}

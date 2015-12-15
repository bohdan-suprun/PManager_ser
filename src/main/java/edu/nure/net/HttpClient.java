package edu.nure.net;

import edu.nure.gui.util.MessagesManager;
import edu.nure.net.parser.ResponseHandler;
import edu.nure.net.request.SimpleRequest;
import edu.nure.net.request.SimpleResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by bod on 06.10.15.
 */
class HttpClient extends Thread{
    private PriorityBlockingQueue<SimpleRequest> pool;
    private CloseableHttpClient client;

    HttpClient(BasicCookieStore store){
        setDaemon(true);
        pool = new PriorityBlockingQueue<>();
        HostnameVerifier hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return s.equals(sslSession.getPeerHost());
            }
        };

        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(60000)
                .setConnectTimeout(60000)
                .build();

        client = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setMaxConnPerRoute(10000)
                .setMaxConnTotal(10000)
                .setDefaultCookieStore(store)
                .setSSLHostnameVerifier(hostnameVerifier)
                .build();
    }

    public void put(SimpleRequest task){
        pool.add(task);
    }

    private void send(SimpleRequest request) throws IOException {
        client.execute(request.getRequest(), new Caller(request));
    }

    public void run() {
        try {
            while (true) {
                SimpleRequest request = pool.take();
                send(request);
            }
        } catch (InterruptedException ex) {
            System.err.println("Interrupted");
        } catch (IOException ex){
            System.err.println("IOException");
            MessagesManager.errorBox("Ошибка при отправке файла. Попробуйте еще раз.", "Error");
        }
    }

    private class Caller implements org.apache.http.client.ResponseHandler {
        SimpleRequest request;

        public Caller(SimpleRequest request) {
            this.request = request;
        }

        public Object handleResponse(HttpResponse httpResponse) throws IOException {
            byte[] buffer = new byte[httpResponse.getEntity().getContent().available()];
            httpResponse.getEntity().getContent().read(buffer);

            if (!httpResponse.getLastHeader("content-type").getValue().equals("image/jpg")) {
                SimpleResponse response = new SimpleResponse(request.getPerformer(), buffer,
                        request.getPriority());
                ResponseHandler.putTask(response);
            } else {
                request.getPerformer().doBinaryImage(buffer);
            }
            return null;
        }
    }
}
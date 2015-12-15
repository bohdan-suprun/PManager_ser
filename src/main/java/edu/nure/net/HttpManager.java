package edu.nure.net;

import edu.nure.bl.Image;
import edu.nure.db.entity.basic.Transmittable;
import edu.nure.listener.ResponseListener;
import edu.nure.net.parser.ResponseHandler;
import edu.nure.net.request.Priority;
import edu.nure.net.request.SimpleRequest;
import edu.nure.util.Action;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.nio.entity.NByteArrayEntity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by bod on 01.10.15.
 */
public class HttpManager {
    public static String HOST;
    private static int THREAD_COUNT = 8;
    private static String PATH;
    private static String IMAGE;
    private static HttpManager self;

    static {
        configure();
    }
    private HttpAsyncWorker worker;
    private HttpClient uploader, fileUploader;
    private ResponseHandler[] parsers;

    private HttpManager() {
        BasicCookieStore store = new BasicCookieStore();
        worker = new HttpAsyncWorker(store);
        uploader = new HttpClient(store);
        fileUploader = new HttpClient(store);
        worker.start();
        uploader.start();
        fileUploader.start();
        parsers = new ResponseHandler[THREAD_COUNT];
        for (int i = 0; i < THREAD_COUNT; i++) {
            parsers[i] = new ResponseHandler();
            parsers[i].start();
        }
    }

    private static void configure() {
        try {
            ResourceBundle rb = ResourceBundle.getBundle("config", new Locale("host"));
            HOST = rb.getString("host");
            PATH = rb.getString("manage");
            IMAGE = rb.getString("image");
            String thrcnt = rb.getString("thrcnt");
            THREAD_COUNT = Integer.valueOf(thrcnt);
            if (THREAD_COUNT < 1) {
                THREAD_COUNT = 8;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            HOST = "https://127.0.0.1";
            PATH = "/admin/";
            IMAGE = "/image/";
            THREAD_COUNT = 8;
        }
    }

    public  static HttpManager getManager(){
        return (self == null)? self = new HttpManager(): self;
    }

    private String escapeUrl(String uri){
        try {
            String[] params = uri.split("&[A-z]*=");
            params = Arrays.copyOfRange(params, 1, params.length);
            for(String value: params){
                String encValue = URLEncoder.encode(value, "utf-8");
                uri = uri.replace("="+value, "="+encValue);
            }
        } catch (UnsupportedEncodingException e) {
            System.err.println("Unsupported encoding " + e.getMessage());
        }
        return uri;
    }

    public void sendGet(String uri, Priority pr){
        sendGet(uri, pr, null);
    }

    public void sendGet(String uri, Priority pri, ResponseListener performer){
        uri = escapeUrl(HOST+PATH+uri);
        HttpGet httpGet = new HttpGet(uri);
        SimpleRequest request = new SimpleRequest(httpGet, pri, performer);
        worker.put(request);
    }

    public void sendGet(String url, String uri, Priority pri, ResponseListener performer){
        uri = escapeUrl(url+uri);
        HttpGet httpGet = new HttpGet(uri);
        SimpleRequest request = new SimpleRequest(httpGet, pri, performer);
        worker.put(request);
    }

    public void sendGet(Transmittable entity, int actionId, Priority pr, ResponseListener performer){
        sendGet(HOST,PATH+"?action="+actionId+"&"+entity.toQuery(), pr, performer);
    }

    public void getImage(Transmittable entity, ResponseListener performer){
        getImage(entity.toQuery(), performer);
    }

    public void getImage(String uri, ResponseListener performer){
        uri = escapeUrl(IMAGE + "?action="+ Action.GET_IMAGE+"&"+uri);
        sendGet(HOST,uri,new Priority(4), performer);
    }

    public void sendGet(Transmittable entity, int actionId, Priority pr){
        sendGet(HOST,PATH+"?action="+actionId+"&"+entity.toQuery(), pr, null);
    }

    public void sendGet(String uri) {
        sendGet(uri, new Priority());
    }

    public void sendGet(Transmittable entity, int actionId){
        sendGet(HOST+PATH+"?action="+actionId+entity.toQuery());
    }

    public void login(String phone, String pass, ResponseListener listener){
        String uri = HOST+PATH;
        HttpPost req = new HttpPost(uri);
        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        formParams.add(new BasicNameValuePair("phone", phone));
        formParams.add(new BasicNameValuePair("password", pass));
        formParams.add(new BasicNameValuePair("action", String.valueOf(1)));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, Consts.UTF_8);
        req.setEntity(entity);
        SimpleRequest request = new SimpleRequest(req, new Priority(), listener);
        worker.put(request);
    }

    public void sendFile(Image imageDesc, ResponseListener l){

        HttpPost httpPost = new HttpPost(HOST + IMAGE + "?action");
        final String BOUNDARY = "------gc0p4Jq0M2Yt08jU534c0p";
        HttpEntity httpEntity = MultipartEntityBuilder.create()
                .addBinaryBody("file", new ByteArrayInputStream(imageDesc.getImage()),
                        ContentType.create("image/jpeg"), "image.jpg")
                .addTextBody("album", String.valueOf(imageDesc.getAlbum()))
                .addTextBody("action", String.valueOf(Action.INSERT_IMAGE))
                .addTextBody("hash", imageDesc.getHash())
                .setBoundary(BOUNDARY)
                .build();
        try {
            ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
            httpEntity.writeTo(baoStream);
            ContentType ct = ContentType.create("multipart/form-data;  boundary=" + BOUNDARY);
            HttpEntity nByteEntity = new NByteArrayEntity(baoStream.toByteArray(), ct);
            httpPost.setEntity(nByteEntity);
            SimpleRequest request = new SimpleRequest(httpPost, new Priority(Priority.MIN), l);
            fileUploader.put(request);
            System.out.println(request.getRequest());
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public void loadPreview(int imId, int albumId,ResponseListener li){
        String uri = "?action="+Action.GET_IMAGE+"&id="+imId+"&albumId="+albumId+"&preview=";
        String url = escapeUrl(HOST + IMAGE + uri);
        HttpGet httpGet = new HttpGet(url);
        SimpleRequest request = new SimpleRequest(httpGet, new Priority(Priority.MIN), li);
        uploader.put(request);
    }

    public void addResponseListener(ResponseListener listener){
        ResponseHandler.addListener(listener);
    }
}

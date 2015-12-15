package edu.nure.gui.components.model;

import edu.nure.bl.Album;
import edu.nure.bl.User;
import edu.nure.gui.util.MessagesManager;
import edu.nure.listener.ModelChanged;
import edu.nure.listener.ResponseAdapter;
import edu.nure.listener.SelectAdapter;
import edu.nure.net.HttpManager;
import edu.nure.net.request.Priority;
import edu.nure.net.results.DBResult;
import edu.nure.net.results.DBSelectResult;
import edu.nure.net.results.ResultItem;
import edu.nure.util.Action;

import java.util.ArrayList;

/**
 * Created by bod on 14.10.15.
 */
public class AlbumModel{
    private final ArrayList<ImageModel> model;
    private final ArrayList<ModelChanged> listeners;
    private User owner;

    public AlbumModel(User owner) {
        this.model = new ArrayList<ImageModel>();
        listeners = new ArrayList<ModelChanged>();
        this.owner = owner;
    }

    private void addElement(ImageModel elem){
        synchronized (model) {
            model.add(elem);
        }
    }

    public void insert(Album a){
        if(owner != null)
            HttpManager.getManager().sendGet(a, Action.INSERT_ALBUM,
                    new Priority(Priority.MIDDLE), handleAlbums());

    }

    public void addListener(ModelChanged listener){
        listeners.add(listener);
    }

    private void fireListener(){
        synchronized (listeners) {
            for (ModelChanged listener : listeners)
                listener.modelChanged();
        }
    }

    public ImageModel get(int i) {
        synchronized (model) {
            return model.get(i);
        }
    }

    public void reload(){
        synchronized (model){
            model.clear();
            loadAlbums();
        }
    }

    public void remove(final Album a){
        HttpManager.getManager().sendGet(a,Action.DELETE_ALBUM, new Priority(Priority.MAX),
                new ResponseAdapter(){
                    @Override
                    public void doDelete(DBResult result) {
                        model.remove(indexOf(a));
                        fireListener();
                    }

                    @Override
                    public void doError(DBResult result) {
                        MessagesManager.errorBox("Ошибка удаления альбома: "+result.getText(),
                                "Ошибка");
                    }
                });
    }

    public int indexOf(Album a){
        for (int i = 0; i < model.size(); i++) {
            if(model.get(i).getAlbumName().getId() == a.getId())
                return i;

        }
        return -1;
    }

    private void loadAlbums(){

        if(owner != null) {
            HttpManager.getManager().sendGet("?action=" + Action.GET_ALBUM + "&id=" + owner.getId(),
                    new Priority(Priority.MIDDLE), handleAlbums());
        } else {
            fireListener();
        }
    }

    private SelectAdapter handleAlbums(){
        return new SelectAdapter(){
            @Override
            public void onAlbum(DBSelectResult res) {
                ResultItem[] items = res.getResult();
                for(ResultItem item: items){
                    Album album = (Album) item.getEntity();
                    addElement(new ImageModel(album));
                }
                fireListener();
            }

            @Override
            public void doInsert(DBSelectResult result) {
                onAlbum(result);
            }

            @Override
            public void doError(DBResult result) {
                MessagesManager.errorBox("Ошибка: " + (result.getText() != null ? result.getText() : ""), "Ошибка");
                fireListener();
            }
        };
    }

    public int size(){
        synchronized (model) {
            return model.size();
        }
    }

    public ImageModel getElement(int index){
        return model.get(index);
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User u){
        owner = u;
    }
}

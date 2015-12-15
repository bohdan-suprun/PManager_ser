package edu.nure.net.results;

import edu.nure.util.Action;

import java.lang.reflect.Field;

/**
 * Created by bod on 01.10.15.
 */
public class DBResult {
    private int action;
    private int status;
    private String text;

    public DBResult(int action){
        status = 200;
        this.action = action;
    }

    public DBResult(int action, int status, String text){
        this.status = status;
        this.action = action;
        this.text = text;
    }

    public boolean isSuccess(){
        return status == 200;
    }

    public String actionRepresentation(){
        Class<?> cl = Action.class;
        Field[] fields = cl.getDeclaredFields();
        try {
            for(Field field: fields) {
                if (field.getInt(new Action()) == action)
                    return  field.getName();
            }
        } catch (IllegalAccessException e) {
                e.printStackTrace();
        }
        return "<unknown>";
    }

    @Override
    public String toString() {
        if(isSuccess()){
            return "Операция "+actionRepresentation()+" прошла успешно";
        }
        return "Ошибка при выполнении " +actionRepresentation()+
                " "+((text != null)?text:"")+" код ("+status+")";
    }

    public int getAction() {
        return action;
    }

    public int getStatus() {
        return status;
    }

    public String getText() {
        return text;
    }
}

package edu.nure.net.results;

import edu.nure.db.entity.basic.AbstractEntity;
import edu.nure.db.entity.basic.Transmittable;
import edu.nure.exceptions.UnknownTagNameException;

import java.util.LinkedList;

/**
 * Created by bod on 01.10.15.
 */
public class DBSelectResult extends DBResult {
    private LinkedList<ResultItem> result;

    public DBSelectResult(int action) {
        super(action);
        result = new LinkedList<>();
    }

    public void addResult(String tagName, String code) throws UnknownTagNameException {
        try {
            Transmittable t = AbstractEntity.fromXml(removeQuotes(code));
            result.add(new ResultItem(t.getClass(), t));
        } catch (ClassNotFoundException ex) {
            throw new UnknownTagNameException("Неизыестный ответ сервера: " + tagName);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println(ex.getMessage());
            throw new UnknownTagNameException("Внутренняя ошибка");
        }

    }

    private String removeQuotes(String dirtyHex) {
        StringBuilder result = new StringBuilder();
        char[] hexArray = dirtyHex.toCharArray();
        for (int i = 0; i < hexArray.length; i++) {
            if (hexArray[i] != '\'' && hexArray[i] != '\"') {
                result.append(hexArray[i]);
            }
        }
        return result.toString();
    }

//    private Transmittable makeUser(HashMap<String, String> pairs) throws ValidationException {
//        return new User(
//                Integer.valueOf(pairs.get("id")),
//                pairs.get("name"),
//                pairs.get("phone"),
//                pairs.get("email"),
//                null,
//                new Right(pairs.get("right"), null)
//        );
//
//    }
//
//    private Transmittable makeOrder(HashMap<String, String> pairs) throws ValidationException {
//        Order or = new Order(
//                Integer.valueOf(pairs.get("id")),
//                Integer.valueOf(pairs.get("customer")),
//                Integer.valueOf(pairs.get("responsible")),
//                pairs.get("desc"),
//                pairs.get("term"),
//                Float.valueOf(pairs.get("for_pay")),
//                Integer.valueOf(pairs.get("status")),
//                Integer.valueOf(pairs.get("urgency"))
//        );
//        return or;
//
//    }
//
//    private Transmittable makeImage(HashMap<String, String> pairs) throws ValidationException, ParseException {
//        return new Image(
//                pairs.get("hash"),
//                Integer.valueOf(pairs.get("id")),
//                null,
//                Integer.valueOf(pairs.get("album")),
//                new SimpleDateFormat("yyyy-MM-dd").parse(pairs.get("createdIn"))
//        );
//    }
//
//    private Transmittable makeFormat(HashMap<String, String> pairs) throws ValidationException{
//        return new Format(
//                pairs.get("name"),
//                Integer.valueOf(pairs.get("width")),
//                Integer.valueOf(pairs.get("height")),
//                Float.valueOf(pairs.get("price"))
//        );
//
//    }
//
//    private Transmittable makeRight(HashMap<String, String> pairs) throws ValidationException{
//        return new Right(
//                pairs.get("type"),
//                pairs.get("desc")
//        );
//    }
//
//    private Transmittable makeUrgency(HashMap<String, String> pairs) throws ValidationException{
//        return new Urgency(
//                Integer.valueOf(pairs.get("term")),
//                Float.valueOf(pairs.get("factor"))
//        );
//    }
//
//    private Transmittable makeStock(HashMap<String, String> pairs) throws ValidationException{
//        return new Stock(
//                Integer.valueOf(pairs.get("id")),
//                Integer.valueOf(pairs.get("order")),
//                Integer.valueOf(pairs.get("image")),
//                pairs.get("desc"),
//                pairs.get("format")
//        );
//    }
//
//    private Transmittable makeAlbum(HashMap<String, String> pairs) throws ValidationException{
//        return new Album(
//                pairs.get("name"),
//                Integer.valueOf(pairs.get("id")),
//                Integer.valueOf(pairs.get("userId"))
//        );
//    }

    public ResultItem[] getResult(){
        return result.toArray(new ResultItem[result.size()]);
    }
}

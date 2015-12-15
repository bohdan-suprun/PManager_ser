package edu.nure.net.parser;

import edu.nure.exceptions.UnknownTagNameException;
import edu.nure.listener.ResponseListener;
import edu.nure.net.results.DBResult;
import edu.nure.net.results.DBSelectResult;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Created by bod on 01.10.15.
 */
class XMLParser extends DefaultHandler {
    private ResponseListener listener;
    private DBResult result;

    XMLParser(ResponseListener listener) {
        this.listener = listener;
    }

    XMLParser() {
        this.listener = null;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if(qName.equals("action")) {
            result = createResult(attributes);
        }
        else if(result != null && result.isSuccess()){
            if(attributes.getLength() > 0) {
                try {
                    for (int i = 0; i < attributes.getLength(); i++) {
                        if (attributes.getQName(i).equals("code")) {
                            ((DBSelectResult) result).addResult(qName, attributes.getValue(i));
                            break;
                        }
                    }
                } catch (UnknownTagNameException e) {
                    throw new SAXException("Unknown tag name: "+e.getMessage());
                }
            }
        }
    }

    @Override
    public void endDocument() throws SAXException {
        if(listener != null) {
            ResponseHandler.notifyMe(result, listener);
        } else {
            ResponseHandler.notifyListeners(result);
        }

    }

    private DBResult createResult(Attributes attributes) {
        DBResult result1 = null;
        int action = Integer.valueOf(attributes.getValue("id"));
        int status = Integer.valueOf(attributes.getValue("status"));
        String text = attributes.getValue("text");
        if(status != 200){
            result1 = new DBResult(action, status, text);
        }else{
            if(action >= 200 && action <= 299 || action == 1
                    || action >= 100 && action <= 199) {
                result1 = new DBSelectResult(action);
            } else {
                result1 = new DBResult(action);
            }
        }
        return result1;
    }

}

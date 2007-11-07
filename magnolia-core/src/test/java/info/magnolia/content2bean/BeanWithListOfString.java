package info.magnolia.content2bean;

import java.util.ArrayList;
import java.util.List;


public class BeanWithListOfString {
    private List values = new ArrayList();

    public void addValue(String value){
        values.add(value);
    }

    public List getValues() {
        return values;
    }

}

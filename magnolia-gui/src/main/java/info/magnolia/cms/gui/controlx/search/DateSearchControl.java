package info.magnolia.cms.gui.controlx.search;

import info.magnolia.cms.gui.query.DateSearchQueryParameter;
import info.magnolia.cms.gui.query.SearchQueryExpression;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.context.MgnlContext;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.time.FastDateFormat;


/**
 * Special Date Search Control
 * @author philipp
 */
public class DateSearchControl extends SearchControl {

    /**
     * 
     */
    public static final String RENDER_TYPE = "dateSearchControl";

    /**
     * 
     */
    public DateSearchControl() {
        this.setRenderType(RENDER_TYPE);
    }

    /**
     * @param definition
     * @param value
     * @param condition
     */
    public DateSearchControl(SearchControlDefinition definition, String value, String condition) {
        super(definition, value, condition);
        this.setRenderType(RENDER_TYPE);
    }

    /**
     * create the date query expression
     */
    public SearchQueryExpression getExpression() {
        Date date = null;
        if (this.getConstraint().equals(DateSearchQueryParameter.TODAY)) {
            date = new Date();
        }
        else {
            String value = getValue();
            try {
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                System.out.println(format.format(new Date()));
                date = format.parse(value);
            }
            catch (ParseException e) {
                try {
                    DateFormat format = DateFormat.getDateInstance(FastDateFormat.SHORT, MgnlContext.getLocale());
                    date = format.parse(value);
                }
                catch (ParseException e1) {
                    try {
                        DateFormat format = DateFormat.getDateInstance(FastDateFormat.SHORT, MgnlContext.getLocale());
                        date = format.parse(value);
                    }
                    catch (ParseException e2) {
                        AlertUtil.setMessage("The date is not properly formated [" + value + "] ");
                    }
                }
            }
        }

        return new DateSearchQueryParameter(this.getDefinition().getColumn(), date, this.getConstraint());
    }
}

package info.magnolia.module.admininterface.dialogs;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.MetaData;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.SaveHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by IntelliJ IDEA.
 * User: niko
 * Date: Jun 5, 2006
 * Time: 5:20:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class ActivationDatesDialog extends ParagraphEditDialog {
    Logger log = LoggerFactory.getLogger(ActivationDatesDialog.class);
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'hh:mm:ss"; //2006-06-27T18:18:37

    SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);

    public ActivationDatesDialog(String name, HttpServletRequest request, HttpServletResponse response, Content configNode) {
        super(name, request, response, configNode);
    }

    protected boolean onSave(SaveHandler control) {
        try {
            Content parentNode = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE).getContent(control.getPath());
            if(log.isDebugEnabled())
                log.debug("Updating dates for page:"+parentNode.getHandle());
            String startDate = getDialog().getSub("startDate").getValue();
            String endDate = getDialog().getSub("endDate").getValue();
            MetaData md = parentNode.getMetaData();

            Calendar startCalendar = Calendar.getInstance();
            Calendar endCalendar = Calendar.getInstance();

            try {
                startCalendar.setTime(format.parse(startDate));
                md.setStartTime(startCalendar);
            }  catch(Exception e) {
                //ignore
            }

            try {
                endCalendar.setTime(format.parse(endDate));
                if(endCalendar.after(startCalendar))
                    md.setEndTime(endCalendar);
                else
                    log.error("Not updating endTime since it is before startTime");
            }  catch(Exception e) {
                //ignore
            }

            parentNode.save();

        } catch (Exception e) {
            log.error("Could not save activation dates",e);
            return false;
        }
        return true;
    }
}

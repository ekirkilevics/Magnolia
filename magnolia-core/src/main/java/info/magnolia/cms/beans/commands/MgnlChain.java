package info.magnolia.cms.beans.commands;

import org.apache.commons.chain.Chain;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Date: Mar 28, 2006
 * Time: 5:12:42 PM
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class MgnlChain extends MgnlCommand implements Chain {
    ArrayList list = new ArrayList();

    /**
     * List of the parameters of all the sub commands
     *
     * @return a list of string describing the parameters needed. The parameters should have a  mapping in this class.
     */
    public String[] getExpectedParameters() {
        ArrayList params = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            String[] sub = ((MgnlCommand) list.get(i)).getExpectedParameters();
            params.addAll(Arrays.asList(sub));
        }
        int size = params.size();
        return (String[]) params.toArray(new String[size]);
    }

    public boolean exec(HashMap param, Context ctx) {
        for (int i = 0; i < list.size(); i++) {
            if (((MgnlCommand) list.get(i)).execute(ctx))
                continue;
            else
                return false;
        }
        return true;
    }

    public void addCommand(Command command) {
        list.add(command);
    }

    public int countCommands() {
        return list.size();
    }
}

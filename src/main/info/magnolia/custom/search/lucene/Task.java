package info.magnolia.custom.search.lucene;

import javax.servlet.ServletConfig;


/**
 * User: Sameer Charles Date: Mar 1, 2004 Time: 8:22:31 PM
 */
public class Task extends BaseTask {

    public Task(ServletConfig config) throws Exception {
        super(config);
    }

    public void run() {
        super.executeIndexer();
    }
}

package info.magnolia.custom.search.lucene;

import java.io.IOException;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;


/**
 * User: Sameer Charles Date: Mar 1, 2004 Time: 2:09:46 PM
 */
public class Search {

    private IndexSearcher searcher = null;

    public Hits query(Query query) {
        Hits hits = null;
        try {
            String indexDir = Index.getIndexDirectory();
            this.searcher = new IndexSearcher(indexDir);
            hits = this.searcher.search(query);
        }
        catch (IOException e) {
        }
        finally {
            return hits;
        }
    }

    public void close() {
        try {
            this.searcher.close();
        }
        catch (IOException e) {
        }
    }
}

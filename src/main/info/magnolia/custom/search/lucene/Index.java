package info.magnolia.custom.search.lucene;

import java.io.IOException;
import java.io.Reader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;


/**
 * User: Sameer Charles Date: Mar 1, 2004 Time: 2:07:40 PM
 */
public class Index {

    private Document document;

    private IndexWriter writer;

    private static String indexDirectory;

    Index(String path) {
        System.out.println("Setting index directory to - " + path);
        indexDirectory = path;
        String indexDir = getIndexDirectory();
        System.out.println("Unlock writer");
        unlock();
        Analyzer analyzer = new WhitespaceAnalyzer();
        try {
            if (IndexReader.indexExists(indexDir)) {
                unlock();
                this.writer = new IndexWriter(indexDir, analyzer, false);
            }
            else {
                this.writer = new IndexWriter(indexDir, analyzer, true);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static String getIndexDirectory() {
        return indexDirectory;
    }

    protected void addDocument() {
        this.document = new Document();
    }

    protected void closeDocument() {
        try {
            if (this.document != null)
                this.writer.addDocument(this.document);
        }
        catch (IOException e) {
        }
    }

    protected static void deleteDocument(String field, String value) {
        try {
            IndexReader indexReader = IndexReader.open(getIndexDirectory());
            indexReader.delete(new Term(field, value));
            indexReader.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void addField(String name, String value) {
        this.document.add(Field.Text(name, value));
    }

    protected void addField(String name, Reader value) {
        this.document.add(Field.Text(name, value));
    }

    public static void unlock() {
        try {
            if (IndexReader.isLocked(getIndexDirectory())) {
                FSDirectory directory = FSDirectory.getDirectory(getIndexDirectory(), false);
                IndexReader.unlock(directory);
            }
        }
        catch (IOException e) {
        }
    }

    protected void closeIndex() {
        try {
            this.writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

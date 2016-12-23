package lia.indexing;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

import lia.common.TestUtil;

public class IndexingTest {
	protected String[] ids = {"1", "2"};
	protected String[] unindexed = {"Netherlands", "Italy"};
	protected String[] unstored = {"Amsterdam has lots of bridges", 
								   "Venice has lots of canals"};
	protected String[] text = {"Amsterdam", "Venice"};
	
	private Directory directory;
	
	protected void setUp() throws Exception {
		directory = new RAMDirectory();
		
		IndexWriter writer = getWriter();
		
		for (int i = 0; i < ids.length; i++) {
			Document doc = new Document();
			doc.add(new Field("id", ids[i],
							  Field.Store.YES,
							  Field.Index.NOT_ANALYZED));
			doc.add(new Field("country", unindexed[i],
							  Field.Store.YES,
							  Field.Index.NO));
			doc.add(new Field("contents", unstored[i],
							  Field.Store.NO,
							  Field.Index.ANALYZED));
			doc.add(new Field("city", text[i],
							  Field.Store.YES,
							  Field.Index.ANALYZED));
			writer.addDocument(doc);
		}
		writer.close();
	}
	
	private IndexWriter getWriter() throws IOException {
		return new IndexWriter(directory, new WhitespaceAnalyzer(), 
							   IndexWriter.MaxFieldLength.UNLIMITED);
	}
	
	protected int getHitCount(String fieldName, String searchString)
	  throws IOException {
		IndexSearcher searcher = new IndexSearcher(directory);
		Term t = new Term(fieldName, searchString);
		Query query = new TermQuery(t);
		int hitCount = TestUtil.hitCount(searcher, query);
		searcher.close();
		return hitCount;
	}
	
	public void testIndexWriter() throws IOException {
		IndexWriter writer = getWriter();
	}
	
	@Test
	public void test() {
		fail("Not yet implemented");
	}

}

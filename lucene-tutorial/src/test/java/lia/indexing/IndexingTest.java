package lia.indexing;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import lia.common.TestUtil;

public class IndexingTest {
	protected String[] ids = {"1", "2"};
	protected String[] unindexed = {"Netherlands", "Italy"};
	protected String[] unstored = {"Amsterdam has lots of bridges", 
								   "Venice has lots of canals"};
	protected String[] text = {"Amsterdam", "Venice"};
	
	private Directory directory;
	
	//@BeforeClass
	@Before
	public void setUp() throws Exception {
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
	
	private IndexReader getReader() throws IOException {

		return IndexReader.open(directory);
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
	
	@Test
	public void testIndexWriter() throws IOException {
		IndexWriter writer = getWriter();
		assertEquals(ids.length, writer.numDocs());
		System.out.println("ids.length: " + Integer.toString(ids.length));
		writer.close();
	}
	
	@Test
	public void testIndexReader() throws IOException {
		IndexReader reader = IndexReader.open(directory);
		System.out.println("ids.length: " + Integer.toString(ids.length));
		assertEquals(ids.length,  reader.maxDoc());
		assertEquals(ids.length,  reader.numDocs());
		reader.close();
	}
	
	//@Test
	public void testDeleteBeforeOptimize() throws IOException {
		IndexWriter writer = getWriter();
		assertEquals(2,  writer.numDocs());
		writer.deleteDocuments(new Term("id", "1"));
		writer.commit();
		assertTrue(writer.hasDeletions());
		assertEquals(2, writer.maxDoc());
		assertEquals(1, writer.numDocs());
		writer.close();
	}
	
	//@Test
	public void testDeleteAfterOptimize() throws IOException {
		IndexWriter writer = getWriter();
		//System.out.println(writer.getInfoStream());
		assertEquals(2, writer.numDocs());
		writer.deleteDocuments(new Term("id", "1"));
		writer.optimize();
		writer.commit();
		assertFalse(writer.hasDeletions());
		assertEquals(1, writer.maxDoc());
		assertEquals(1, writer.numDocs());
		writer.close();
	}
	
    @Test
	public void testUpdate() throws IOException {
		
		assertEquals(1, getHitCount("city", "Amsterdam"));
		
		IndexWriter writer = getWriter();
		System.out.println("numDocs" + Integer.toString(writer.numDocs()));
		
		Document doc = new Document();
		doc.add(new Field("id", "1", 
						  Field.Store.YES,
						  Field.Index.NOT_ANALYZED));
        doc.add(new Field("country", "Netherlands",
                          Field.Store.YES,
                          Field.Index.NO));
        doc.add(new Field("contents", 
                          "Den Haag has a lot of museums",
                          Field.Store.NO,
                          Field.Index.ANALYZED));
        doc.add(new Field("city", "Den Haag",
                          Field.Store.YES,
                          Field.Index.NOT_ANALYZED));
        writer.updateDocument(new Term("id", "1"), 
                              doc);
        writer.close();
		System.out.println("numDocs: " + Integer.toString(writer.numDocs()));
		IndexReader reader = getReader();
		TermEnum termEnum = reader.terms();
		while(termEnum.next()) {
			Term term = termEnum.term();
			System.out.println(term.text());
		}
		System.out.println("reader terms: " + reader.terms());
		assertEquals(0, getHitCount("city", "Amsterdam"));
		assertEquals(1, getHitCount("city", "Den Haag"));
	}
	
	//@Test
	public void test() {
		fail("Not yet implemented");
	}

}

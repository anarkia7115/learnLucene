package lia.searching;

import static org.junit.Assert.*;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.Version;
import org.junit.Test;

import lia.common.TestUtil;

public class BasicSearchingTest {

	@Test
	public void testTerm() throws Exception {
		Directory dir = TestUtil.getBookIndexDirectory();
		IndexSearcher searcher = new IndexSearcher(dir);
		
		//Term t = new Term("isbn", "9781935182023");
		Term t = new Term("subject", "ant");
		Query query = new TermQuery(t);
		TopDocs docs = searcher.search(query, 10);
		assertEquals("Ant in Action", 
					 1, docs.totalHits);
		
		t = new Term("subject", "junit");
		docs = searcher.search(new TermQuery(t), 10);
		assertEquals("Ant in Action, " +
					 "JUnit in Action, Second Edition", 
					 2, docs.totalHits);
		searcher.close();
		dir.close();
	}
	
	@Test
	public void testKeyword() throws Exception {
		Directory dir = TestUtil.getBookIndexDirectory();
		IndexSearcher searcher = new IndexSearcher(dir);
		
		Term t = new Term("isbn", "9781935182023");
		Query query = new TermQuery(t);
		TopDocs docs = searcher.search(query, 10);
		assertEquals("JUnit in Action, Second Edition", 
					 1, docs.totalHits);
		
		searcher.close();
		dir.close();
	}
	
	@Test
	public void testQueryParser() throws Exception {
		Directory dir = TestUtil.getBookIndexDirectory();
		IndexSearcher searcher = new IndexSearcher(dir);
		
		QueryParser parser = new QueryParser(Version.LUCENE_30,
											 "contents",
											 new SimpleAnalyzer());
		
		Query query = parser.parse("+JUNIT +ANT -MOCK");
		TopDocs docs = searcher.search(query, 10);
		assertEquals(1, docs.totalHits);
		Document d = searcher.doc(docs.scoreDocs[0].doc);
		assertEquals("Ant in Action", d.get("title"));
		searcher.close();
		dir.close();
	}
}

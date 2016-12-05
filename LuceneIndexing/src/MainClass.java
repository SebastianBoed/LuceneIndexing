import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.xml.sax.SAXException;

public class MainClass {

	private static final String DOCUMENT_PATH = "C:/Users/Sebastian/OneDrive/Studium/TemporalInformation/LuceneIndexing/Documents";
	private static final String INDEX_PATH = "C:/Users/Sebastian/OneDrive/Studium/TemporalInformation/LuceneIndexing/Index";
	
	private static Double lambda = 0.7;
	private static Double müh = 1000.0;
	
	private static HashMap<Document, Double> ranking;
	private static int maxDocuments = 5;
	
	private static CustomQueryParser parser;
	
	public static void main(String[] args) throws IOException, ParseException, ParserConfigurationException, SAXException, java.text.ParseException, InterruptedException {
			
		//createIndex();
		createQueryParser();
		//search("america@2011-2013");
		//search("china@2011-2013");
		//search("republic of ireland@2011-2011");
		//search("diabetes in young children@2011-2012");
		//search("diabetes in young children@ 2011-2009");
		//search("Hannover@2010-2016");
		search("donald trump@2011-2013");
	}
	
	public static void createIndex() throws IOException, java.text.ParseException, InterruptedException {
		Indexer index = new Indexer(INDEX_PATH);
		index.createIndex(DOCUMENT_PATH, new TextFileFilter());
		System.out.println("Documente: " + CustomXMLParser.counterDocuments);
		index.close();
	}	
	
	public static void createQueryParser() {
		parser = new CustomQueryParser(CustomXMLParser.CONTENTS, new StandardAnalyzer());
	}
	
	public static int search(String q) throws IOException, ParseException, java.text.ParseException {
		IndexReader reader = DirectoryReader.open(FSDirectory.open((Paths.get(INDEX_PATH))));
		IndexSearcher searcher = new IndexSearcher(reader);
		Query query = parser.parse(q);
		TopDocs hits = searcher.search(query,1000000);	
		System.out.println("");
		System.out.println("Searching for " + q + " | Hits:" + hits.totalHits);		
				
		LinkedList<Term> terms = generateTerms(q);
		
		HashMap<Term, Double> termFrequencys = new HashMap<>();
		
		for(Term t : terms) {
			termFrequencys.put(t, ((double) reader.totalTermFreq(t) / ((double) reader.getDocCount(CustomXMLParser.CONTENTS))));

		}
		
		
		List<Document> documents = JelinekMercerSmoothing(termFrequencys, reader);
		System.out.println("JelinekMercerSmoothing");
		showResults(documents, reader, hits);
		
		documents = DirichletPriorSmoothing(termFrequencys, reader);
		System.out.println("DirichletPriorSmoothing");
		showResults(documents, reader, hits);
		
		
		
		return hits.totalHits;
	}
	
	@SuppressWarnings("resource")
	private static LinkedList<Term> generateTerms(String q) throws IOException {	
		LinkedList<Term> termLst = new LinkedList<>();
		String keywords = q.split("@")[0];
		TokenStream stream = new StandardAnalyzer().tokenStream(null, new StringReader(keywords));
		CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
		stream.reset();
		while (stream.incrementToken()) {
			termLst.add(new Term(CustomXMLParser.CONTENTS, cattr.toString()));
		}
		stream.end();
		stream.close();
		return termLst;
	}
	
	
	private static void showResults(List<Document> documents, IndexReader reader, TopDocs hits) throws IOException {
		LinkedList<Document> results = new LinkedList<>();
		
		for(int i = 0; i<documents.size() && results.size()<maxDocuments; i++) {
			Document doc = documents.get(i);
			String documentName = doc.getField(CustomXMLParser.FILENAME).stringValue();
			for(ScoreDoc scoredoc : hits.scoreDocs) {
				if(reader.document(scoredoc.doc).getField(CustomXMLParser.FILENAME).stringValue().equals(documentName)) {
					results.add(doc);
				}
			}
		}
		
		for(Document doc : results) {
			System.out.println(doc.getField(CustomXMLParser.FILENAME).stringValue() + " DocWordCount:" +doc.getField(CustomXMLParser.DOC_WORDCOUNT).stringValue() + " Ranking:" + ranking.get(doc));
		}
	}
	
	private static List<Document> JelinekMercerSmoothing(HashMap<Term, Double> terms, IndexReader reader) throws IOException {
		ranking = new HashMap<>();
		for(Term term : terms.keySet()) {
			Double collectionFreq = terms.get(term);
			PostingsEnum docEnum = MultiFields.getTermDocsEnum(reader, "contents", term.bytes());
			double wordProbability_Index = (1.0 - lambda) * (double) collectionFreq;
			while (docEnum != null && docEnum.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
				Document doc = reader.document(docEnum.docID());
				Long wordCount = Long.valueOf(doc.getField(CustomXMLParser.DOC_WORDCOUNT).stringValue());
				double wordProbability_Document = ((double) docEnum.freq() / (double) wordCount) * lambda;				
				if(ranking.get(doc) != null) {
					ranking.put(doc, ranking.get(doc) * (wordProbability_Document + wordProbability_Index));
				} else {				
					ranking.put(doc, (wordProbability_Document + wordProbability_Index));
				}
			}			
		}
		List<Document> sortedStats = ranking.entrySet().stream().sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())).map(Map.Entry::getKey).collect(Collectors.toList());
		return sortedStats;
	}
	
	private static List<Document> DirichletPriorSmoothing(HashMap<Term, Double> terms, IndexReader reader) throws IOException {
		ranking = new HashMap<>();
		for(Term term : terms.keySet()) {
			Double collectionFreq = terms.get(term);
			PostingsEnum docEnum = MultiFields.getTermDocsEnum(reader, "contents", term.bytes());
			double wordProbability_Index = müh * (double) collectionFreq;
			double documentCountPlusMüh = reader.numDocs() + müh;
			while (docEnum != null && docEnum.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
				Document doc = reader.document(docEnum.docID());
				Long wordCount = Long.valueOf(doc.getField(CustomXMLParser.DOC_WORDCOUNT).stringValue());
				double wordFrequency_Document = ((double) docEnum.freq() / (double) wordCount) * lambda;				
				if(ranking.get(doc) != null) {
					ranking.put(doc, ranking.get(doc) * ((wordFrequency_Document + wordProbability_Index)/documentCountPlusMüh));
				} else {				
					ranking.put(doc, ((wordFrequency_Document + wordProbability_Index)/documentCountPlusMüh));
				}
			}			
		}
		List<Document> sortedStats = ranking.entrySet().stream().sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())).map(Map.Entry::getKey).collect(Collectors.toList());
		return sortedStats;
	}
	
	
}

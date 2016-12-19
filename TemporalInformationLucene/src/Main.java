import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

public class Main {

	public static String INDEX_PATH = "Index/";
	public static String COLLECTION_PATH = "C:/Users/hseba/git/LuceneIndexing/TemporalInformationLucene/Documents";
	public static String TOPIC_PATH = "C:/Users/hseba/OneDrive/Studium/TemporalInformation/Topic/";

	static HashMap<String, String> LABELS = new HashMap<>();
	
	private static CustomQueryParser parser;
	private static IndexReader reader;
	private static IndexSearcher searcher;

	public static void main(String[] args) throws IOException, ParseException {
		//index();
				
		parser = new CustomQueryParser("content", "date", new StandardAnalyzer());
		reader = DirectoryReader.open(FSDirectory.open((Paths.get(INDEX_PATH))));
		searcher = new IndexSearcher(reader);

		//search("donald trump@2011-2013");
		
		importLabels();
		write(new PrintWriter("query_scores_features_a.txt"), "a");
		write(new PrintWriter("query_scores_features_f.txt"), "f");
		write(new PrintWriter("query_scores_features_p.txt"), "p");
		write(new PrintWriter("query_scores_features_r.txt"), "r");
		


	}

	public static void index() {
		Indexer indexer = new Indexer(INDEX_PATH, new LMMercerSimilarity((float) 0.7));
		indexer.indexCollection(COLLECTION_PATH);
	}

	public static TopDocs search(String queryString) throws IOException, ParseException {
		TopDocs hits = searcher.search(parser.parse(queryString), 1000000);

		System.out.println(hits.totalHits + " Hits for: " + queryString);
		//showTopDocs(hits);

		return hits;
	}
	
	public static void showTopDocs(TopDocs hits) throws IOException {
		for (int i = 0; i < 5 && i < hits.totalHits; i++) {
			Document doc = reader.document(hits.scoreDocs[i].doc);
			System.out.println(doc.getField("id").stringValue());
		}
	}
		
	public static void importLabels() throws IOException {
		LABELS = new HashMap<>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("tir_formalrun_2014080829.qrels")), "UTF8"));
		String line;
		while ((line = br.readLine()) != null)  {
			String[] splits = line.split("\\s+");
			if(splits.length>2) {
				String key = splits[0]+"-"+splits[1];
				LABELS.put(key, splits[2].substring(1));
			} else {
				System.out.println("importLabels: " + splits.length + " " + line);
			}
		}
		br.close();
	}
	
	public static void write(PrintWriter out, String str) throws IOException, ParseException {
		ArrayList<Topic> topics = TopicParser.readTopics(TOPIC_PATH);
		for(Topic t : topics) {
			TopDocs hits = search(t.title);
				for (int i = 0; i < hits.totalHits; i++) {
					Document doc = reader.document(hits.scoreDocs[i].doc);
					
					String queryId = t.id + str;
					String docName =  doc.getField("id").stringValue();
					String label = LABELS.get(queryId +"-"+ docName);
					if(label != null) {
						String output = label + " qid:" + t.id + " 1:" + hits.scoreDocs[i].score + " 2:" + (t.query_issue_time - Long.parseLong(doc.getField("date").stringValue())) + " # " + queryId;
						out.println(output);
					}
				
				
				
			}	
		}
		out.flush();
		out.close();
	}
	
	

}

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.stream.Stream;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

public class Indexer {

	StandardAnalyzer analyzer;
	Directory index;
	IndexWriter w;
	Integer count;
	public boolean intRange = false;
	public boolean termRange=false;
	
	
	public Indexer(String location, Similarity s) {
		analyzer = new StandardAnalyzer();
		Path path = FileSystems.getDefault().getPath(location);
		System.out.println("Index Path: "+path.toAbsolutePath());
		count=0;
	
		IndexWriterConfig config = new IndexWriterConfig(analyzer);
		try {
			index = FSDirectory.open(path );
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			config.setSimilarity(s);
			w = new IndexWriter(index, config);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
		
	
	public void indexCollection(String collectionLocation) {	
		System.out.println(Paths.get(collectionLocation));
		try(Stream<Path> paths = Files.walk(Paths.get(collectionLocation))) {
			
			paths.forEach(filePath -> {
		    	
		        if (Files.isRegularFile(filePath)) 
		        {
		            System.out.println(filePath);
		            
		            byte[] encoded = null;
		    		try {
		    			encoded = Files.readAllBytes(filePath);
		    		} catch (IOException e1) {
		    			
		    			e1.printStackTrace();
		    		}
		    		String html="";
		    		try {
		    			html = new String(encoded,"UTF8");
		    		} catch (UnsupportedEncodingException e1) {
		    			
		    			e1.printStackTrace();
		    		}
		    		org.jsoup.nodes.Document doc = Jsoup.parse(html, "UTF-8", Parser.xmlParser());
		    		Elements docs = doc.getElementsByTag("doc");
		    		
		    		for(Element e: docs){
		    			long date= 0L;
		    			String title = new String();
		    			String id = new String();
		    			String body = new String();
		    			Elements tags = e.getElementsByTag("tag");
		    			id = e.attr("id");
		    		
						for(Element t : tags)
						{
							if(t.attr("name").equals("date")){
								DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
								Date dateObj;
								try {
									dateObj = format.parse(t.text());
									//System.out.println(dateObj);
									date = dateObj.getTime();
								} catch (Exception e1) {
									
									e1.printStackTrace();
								}
								
							}
							else if(t.attr("name").equals("title"))
								title = t.text();
							
						}
						Elements text = e.getElementsByTag("text");
						for(Element t: text){
							body = t.text();
						}
						
						
						try {
								this.addDoc(id, title, body, date);
							
						} catch (Exception e1) {
							System.out.println(body);
							e1.printStackTrace();
						} 
						
		    		}
		        }
		    });
			
		} catch (IOException e) {
			e.printStackTrace();
		} 

		try {
			System.out.println(this.count);
			this.w.close();
		} catch (IOException e) {
		
			e.printStackTrace();
		}
	}
	
	
	
	public void addDoc(String id, String title, String body, long date) throws IOException {
		  Document doc = new Document();
		  doc.add(new StringField("title", title, Field.Store.YES));
		  doc.add(new StringField("id", id, Field.Store.YES));
		  
		  doc.add(new TextField("body", body, Field.Store.YES));
		  doc.add(new TextField("content", title + " " + body, Field.Store.YES));
		  doc.add(new StoredField("date", date));
		  count++;
		  w.addDocument(doc);
	}
	
}

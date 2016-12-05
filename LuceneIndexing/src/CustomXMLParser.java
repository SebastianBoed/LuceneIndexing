import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

public class CustomXMLParser {

	public static final String CONTENTS = "contents";
	public static final String FILENAME = "filename";
	public static final String FULLPATH = "fullpath";
	public static final String LAST_MODIFIED = "lastModified";
	public static final String DOC_WORDCOUNT = "wordcount";

	public static int counterDocuments = 0;
	
	public static ArrayList<Document> parseXML(File file) throws IOException, ParseException {
		ArrayList<Document> documentList = new ArrayList<>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8")); 
		Pattern patternBetween = Pattern.compile(">(.*?)<");
		String line = null;  
		while ((line = br.readLine()) != null)  
		{  
		   if(line.matches("<doc id=.*>")) {
			   counterDocuments++;
			   Document doc = new Document();
			   doc.add(new StringField(FILENAME, line, Field.Store.YES));
			   doc.add(new StringField(FULLPATH, file.getCanonicalPath(),  Field.Store.YES));
			   String content = "";
			   
			   while ((line = br.readLine()) != null)  {
				   if(line.matches("</doc>")) break;
				   if(line.matches("<tag name=\"date\">.*")) {
					   Matcher matcher = patternBetween.matcher(line);
					   if (matcher.find())  {
						   doc.add(new LongPoint(LAST_MODIFIED, DateHelper.getTimeInMillisFromString(matcher.group(1))));
					   } else {
						   System.out.println(line);
					   }
					   continue;
				   }

				   for(String split : line.split("<")) {
					   String[] splits = split.split(">");
					   for(int i = 1; i<splits.length; i++ ) {
						   content += " " + splits[i];
					   }
				   }
				   
			   }
			   Long wordcount = (long) content.split("\\s+").length;
			   doc.add(new StringField(DOC_WORDCOUNT, String.valueOf(wordcount), Field.Store.YES));
			   doc.add(new TextField(CONTENTS, new StringReader(content)));
			   documentList.add(doc);
		   }
		} 
		br.close();
		
		return documentList;
	}
	
	
}

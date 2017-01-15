import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.Query;

public class CustomQueryParser extends QueryParser {

	private final String dateFieldName;
	private final int anzWordsForExpanding;
	public static String words;
	
	public CustomQueryParser(String contentFieldName, String dateFieldName, Analyzer anylzer, int anzWordsForExpanding) {
		super(contentFieldName, anylzer);
		this.dateFieldName = dateFieldName;
		this.anzWordsForExpanding = anzWordsForExpanding;
	}

	@Override
	public Query parse(String queryString) throws ParseException {
		String[] splits = queryString.split("@");
		String queryTarget = splits[0];
		Builder query;
		if (splits.length > 1) {
			try {
			String[] years = splits[1].split("-");

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
			Date start = format.parse(years[0] + "-01-01");
			Date end;
			
				end = format.parse(years[0] + "-12-31");

			if (years.length > 1) {
				end = format.parse(years[1] + "-12-31");
			}
			Query yearQuery = LongPoint.newRangeQuery(dateFieldName, start.getTime(), end.getTime());
			query = new BooleanQuery.Builder().add(super.parse(queryTarget), (anzWordsForExpanding>0 ? BooleanClause.Occur.MUST : BooleanClause.Occur.SHOULD)).add(yearQuery,BooleanClause.Occur.MUST);
			} catch (java.text.ParseException e) {
				query = new BooleanQuery.Builder().add(super.parse(queryTarget), (anzWordsForExpanding>0 ? BooleanClause.Occur.MUST : BooleanClause.Occur.SHOULD));
				e.printStackTrace();
			}
		} else {
			query = new BooleanQuery.Builder().add(super.parse(queryTarget), (anzWordsForExpanding>0 ? BooleanClause.Occur.MUST : BooleanClause.Occur.SHOULD));
		}
		
		if(anzWordsForExpanding>0) {
			try {
				words = "";
				for(String s : getWordsForExpanding(queryTarget.toLowerCase(), anzWordsForExpanding)) {
					//System.out.println(s);
					if(s != null && s != "") {
						query.add(super.parse(s), BooleanClause.Occur.SHOULD);
						words = words + s + ", ";
					}
				}
				words = words.substring(0, words.length()-2);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
		return query.build();
	}
	
	
	public String[] getWordsForExpanding(String query, int anzWords) throws MalformedURLException, IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new URL("http://api.bing.com/osjson.aspx?query=" + query.replace(" ", "+") + "&cc=us").openStream()));
		String line;
		String[] words = new String[anzWords];
		int count = 0;
		while ((line = br.readLine()) != null) {
			String[] splits = line.split(",");
			for (int i = 2; i<splits.length && count<anzWords; i++) {
				if(!splits[i].contains("\\")) {
					words[count] = splits[i].replaceAll("\"", "").replaceAll(query, "").replaceAll("]]", "").trim();
					count++;
				}
			}
		}
		return words;
	}

	
	

}
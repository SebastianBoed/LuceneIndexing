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
import org.apache.lucene.search.Query;

public class CustomQueryParser extends QueryParser {

	private final String dateFieldName;
	
	public CustomQueryParser(String contentFieldName, String dateFieldName, Analyzer anylzer) {
		super(contentFieldName, anylzer);
		this.dateFieldName = dateFieldName;
	}

	@Override
	public Query parse(String queryString) throws ParseException {
		String[] splits = queryString.split("@");
		String queryTarget = splits[0];
		try {
			if (splits.length > 1) {
				String[] years = splits[1].split("-");

				DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
				Date start = format.parse(years[0] + "-01-01");
				Date end = format.parse(years[0] + "-12-31");

				if (years.length > 1) {
					end = format.parse(years[1] + "-12-31");
				}
				Query yearQuery = LongPoint.newRangeQuery(dateFieldName, start.getTime(), end.getTime());

				return new BooleanQuery.Builder().add(super.parse(queryTarget), BooleanClause.Occur.MUST).add(yearQuery, BooleanClause.Occur.MUST).build();
			}
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
		return super.parse(queryTarget);
	}

}
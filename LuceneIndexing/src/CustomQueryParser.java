import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;

public class CustomQueryParser extends QueryParser {

	public CustomQueryParser(String field, Analyzer anylzer) {
		super(field, anylzer);
	}
	
	@Override
	public Query parse(String queryString) throws org.apache.lucene.queryparser.classic.ParseException {		
		String[] splits = queryString.split("@");
		String queryTarget = splits[0];
		
		if(splits.length>1) {
			String[] years = splits[1].split("-");
			Date start = new Date(DateHelper.getTimeInMillisFromYearString(years[0], true));
			Date end = new Date(DateHelper.getTimeInMillisFromYearString(years[0], false));
			if(years.length>1) {
				end = new Date(DateHelper.getTimeInMillisFromYearString(years[1], false));
			}
			Query yearQuery = LongPoint.newRangeQuery(CustomXMLParser.LAST_MODIFIED, start.getTime(), end.getTime());
			
			return new BooleanQuery.Builder().add(super.parse(queryTarget), BooleanClause.Occur.MUST).add(yearQuery, BooleanClause.Occur.MUST).build();
		}
		return super.parse(queryTarget);	
	}
	

}

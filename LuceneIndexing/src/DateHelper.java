import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateHelper {

	public static Long getTimeInMillisFromString(String str) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date d = new Date();
		try {
			d = dateFormat.parse(str.trim());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return d.getTime();
	}
	
	public static Long getTimeInMillisFromYearString(String str, boolean isJanuar) {
		if(isJanuar) {
			return getTimeInMillisFromString(str + "-01-01");
		}
		return getTimeInMillisFromString(str + "-12-31");
	}

}

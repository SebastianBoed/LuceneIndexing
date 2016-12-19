import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.stream.Stream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

public class TopicParser {

	public static ArrayList<Topic> readTopics(String collectionLocation) {
		ArrayList<Topic> topics = new ArrayList<>();

		try (Stream<Path> paths = Files.walk(Paths.get(collectionLocation))) {

			paths.forEach(filePath -> {

				if (Files.isRegularFile(filePath)) {
					System.out.println(filePath);

					byte[] encoded = null;
					try {
						encoded = Files.readAllBytes(filePath);
					} catch (IOException e1) {

						e1.printStackTrace();
					}
					String html = "";
					try {
						html = new String(encoded, "UTF8");
					} catch (UnsupportedEncodingException e1) {

						e1.printStackTrace();
					}
					org.jsoup.nodes.Document doc = Jsoup.parse(html, "UTF-8", Parser.xmlParser());
					Elements docs = doc.getElementsByTag("topic");

					for (Element e : docs) {
						long query_issue_time = 0L;
						String id = e.getElementsByTag("id").first().text();
						String title = e.getElementsByTag("title").first().text();
						String description = e.getElementsByTag("description").first().text();					
						String query_time_string = e.getElementsByTag("query_issue_time").first().text();
						DateFormat format = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
						Date dateObj;
						try {
							dateObj = format.parse(query_time_string.split("GMT")[0]);
							query_issue_time = dateObj.getTime();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
						
						topics.add(new Topic(id, title, description, query_issue_time));
					}

				}
			});

		} catch (IOException e) {
			e.printStackTrace();
		}
		return topics;
	}
}

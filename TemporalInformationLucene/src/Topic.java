
public class Topic {

	final String id;
	final String title;
	final String description;
	final Long query_issue_time;
	
	public Topic(String id, String title, String description, Long query_issue_time) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.query_issue_time = query_issue_time;
	}
	
	@Override
	public String toString() {
		return id + " / " + title;
	}
	
}

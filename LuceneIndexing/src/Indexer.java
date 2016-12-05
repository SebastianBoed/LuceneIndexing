import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Indexer {

	private IndexWriter writer;

	public Indexer(String indexDirectoryPath) throws IOException {
		// this directory will contain the indexes
		Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));

		// create the indexer
		IndexWriterConfig cfg = new IndexWriterConfig(new StandardAnalyzer());
		cfg.setOpenMode(OpenMode.CREATE);
		writer = new IndexWriter(indexDirectory, cfg);
	}

	public void close() throws CorruptIndexException, IOException {
		writer.close();
	}

	public void indexFile(File file) throws IOException, ParseException {
		System.out.println("Indexing " + file.getCanonicalPath());
		for(Document doc : CustomXMLParser.parseXML(file)) {
			writer.addDocument(doc);
		}
	}

	public int createIndex(String dataDirPath, FileFilter filter) throws IOException, ParseException, InterruptedException {
		// get all files in the data directory
		File[] files = new File(dataDirPath).listFiles();
		for (File file : files) {
			if (!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && filter.accept(file)) {
				indexFile(file);
			}
		}		
		return writer.numDocs();
	}
}
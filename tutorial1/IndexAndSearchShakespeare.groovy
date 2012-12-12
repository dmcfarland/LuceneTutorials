// @Grab is a nice feature for getting dependencies added to the classpath.
@Grab(group='org.apache.lucene', module='lucene-core', version='4.0.0')
@Grab(group='org.apache.lucene', module='lucene-queryparser', version='4.0.0')
@Grab(group='org.apache.lucene', module='lucene-analyzers-common', version='4.0.0')
@Grab(group='org.apache.lucene', module='lucene-queries', version='4.0.0')

import org.apache.lucene.analysis.*
import org.apache.lucene.analysis.standard.*
import org.apache.lucene.document.*
import org.apache.lucene.index.*
import org.apache.lucene.queryparser.flexible.standard.*
import org.apache.lucene.search.*
import org.apache.lucene.store.*
import org.apache.lucene.util.*

// This script indexes the text from shakespeare.txt and indexes each line. 
// It then search this index for a value passed in as an argument.

// Search for a line containing the first past argument if passed, 
// otherwise search for lines with monkey. 
def searchTerm = this.args.length > 0 ? "line:${this.args[0]}" : "line:monkey"

// Setup required lucene objects for writing to the lucene index. 
def indexDirectory = new RAMDirectory();
def analyzer = new StandardAnalyzer(Version.LUCENE_40)
def writerConfiguration = new IndexWriterConfig(Version.LUCENE_40, analyzer)
def indexWriter = new IndexWriter(indexDirectory, writerConfiguration);

// Index the shakespeare text file line by line.
new File("shakespeare.txt").readLines().eachWithIndex { line, lineNumber ->
	Document doc = new Document();
	doc.add(new IntField("lineNumber", lineNumber, Field.Store.YES))
	doc.add(new TextField("line", line, Field.Store.YES))
	indexWriter.addDocument(doc)
}

// Print out each line which matches the search term, with a return limit of 10000 matches.
def indexReader = indexWriter.getReader()
def query = new StandardQueryParser(analyzer).parse(searchTerm, "")
def indexSearcher = new IndexSearcher(indexReader)
def hits =  indexSearcher.search(query, 10000).scoreDocs

hits.collect{indexSearcher.doc(it.doc)}.each{ println "${it.lineNumber} ${it.line}"}
println "${hits.length} matches for ${searchTerm - 'line:'} found."

// Tidy up resources
indexReader.close()
indexWriter.close()
package org.dotwebstack.ldtlegacy;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLWriter;
import org.dotwebstack.framework.frontend.ld.entity.GraphEntity;
import org.dotwebstack.framework.frontend.ld.entity.TupleEntity;
import org.dotwebstack.framework.frontend.ld.appearance.Appearance;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.ldtlegacy.pipe.StartTerminal;
import org.dotwebstack.ldtlegacy.pipe.EndTerminal;
import org.dotwebstack.ldtlegacy.pipe.Pipe;

public class CreatePage {
	
	private static final String CONTEXT = "<context staticroot='/assets'><title>LDT 2.0 alfa</title></context>";
	
	public static void write(OutputStream outputStream, GraphEntity graphEntity) throws IOException {

		try {
			//Convert rdf result to xml
			StartTerminal dataPipe = new StartTerminal(graphEntity) {
				@Override
				public void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception {
					QueryResults.report(((GraphEntity)input).getQueryResult(),new RDFXMLWriter(outputStream));
				}
			};
			write(outputStream,dataPipe,graphEntity.getRepresentation());

		} catch (Exception ex) {
			throw new IOException(ex);
		}
	}

	public static void write(OutputStream outputStream, TupleEntity tupleEntity) throws IOException {

		try {
			//Convert rdf result to xml
			StartTerminal dataPipe = new StartTerminal(tupleEntity) {
				@Override
				public void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception {
					QueryResults.report(((TupleEntity)input).getQueryResult(),new SPARQLResultsXMLWriter(outputStream));
				}
			};
			write(outputStream,dataPipe,tupleEntity.getRepresentation());

		} catch (Exception ex) {
			throw new IOException(ex);
		}
	}
	
	public static void write(OutputStream outputStream, Pipe dataPipe, Representation representation) throws IOException {

		try {
			//Construct config pipe
			//Get XML configuration according to elmo2 vocabulary
			StartTerminal configPipe1 = new StartTerminal(representation) {
				@Override
				public void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception {
          //Due to a missing feature in the Represention class, getApperance can return null, in such a case, an empty appearance is needed
          Appearance appearance = ((Representation)input).getAppearance();
          Model model;
          if (appearance!=null) {
            model = appearance.getModel();
          } else {
            model = new LinkedHashModel();
          }
					Rio.write(model, outputStream, RDFFormat.RDFXML);
				}
			};
			//Merge with identifier of appearance, if any exists
      Appearance appearance = representation.getAppearance();
      String appearanceIri = "";
      if (appearance!=null) {
        appearanceIri = appearance.getIdentifier().toString();
      }
			Pipe configPipe2 = new Pipe(appearanceIri,configPipe1) {
				@Override
				public void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception {
					XMLMerger.merge("root", outputStream, new StreamSource(new StringReader(String.format("<appearance>%s</appearance>",(String)input))), new StreamSource(inputStream));
				}
			};
			//Translate to elmo1 vocabulary configuration
			Pipe configPipe3 = new Pipe(configPipe2) {
				@Override
				public void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception {
					XMLEngine.transform(new StreamSource(inputStream), "xsl/config.xsl", new StreamResult(outputStream));
				}
			};
			//Merge configuration result with context (empty at this moment)
			Pipe configPipe4 = new Pipe(configPipe3) {
				@Override
				public void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception {
					XMLMerger.merge("root", outputStream, new StreamSource(inputStream));
				}
			};
			//rdf2view.xsl (create configuration XML from RDF). Result is used more than ones, so store
			ByteArrayOutputStream view = new ByteArrayOutputStream();
			EndTerminal configPipe5 = new EndTerminal(configPipe4,view) {
				@Override
				public void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception {
					XMLEngine.transform(new StreamSource(inputStream), "xsl/rdf2view.xsl", new StreamResult(outputStream));
				}
			};
			//Start the whole pipe
			configPipe5.start();

			//Construct data pipe (start has already been created: dataPipe)
			//Transform from sparql result to rdf (cleaned)
			Pipe dataPipe1 = new Pipe(view,dataPipe) {
				@Override
				public void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception {
					XMLMerger.merge("root",outputStream,new StreamSource(inputStream), new StreamSource(new ByteArrayInputStream(((ByteArrayOutputStream)input).toByteArray())));
				}
			};
			//sparql2rdfa doesn't expect a view, but a representation. But for now, a view can consists of at most one representation
			Pipe dataPipe2 = new Pipe(dataPipe1) {
				@Override
				public void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception {
					XMLEngine.transform(new StreamSource(inputStream), "xsl/sparql2rdfa.xsl", new StreamResult(outputStream));
				}
			};
			//Create one XML file with the results from all information products (at this moment only one information product is available)
			Pipe dataPipe3 = new Pipe(dataPipe2) {
				@Override
				public void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception {
					XMLMerger.merge("results", outputStream, new StreamSource(inputStream));
				}
			};
			//Merge view with context and original data
			Pipe dataPipe4 = new Pipe(view,dataPipe3) {
				@Override
				public void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception {
					XMLMerger.merge("root", outputStream, new StreamSource(new StringReader(CONTEXT)), new StreamSource(new ByteArrayInputStream(((ByteArrayOutputStream)input).toByteArray())),new StreamSource(inputStream));
				}
			};
			//rdf2rdfa.xsl (create RDF annotated with UI declarations)
			Pipe dataPipe5 = new Pipe(dataPipe4) {
				@Override
				public void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception {
					XMLEngine.transform(new StreamSource(inputStream), "xsl/rdf2rdfa.xsl", new StreamResult(outputStream));
				}
			};
			//rdf2html.xsl (create HTML from RDF annotated with UI declarations)
			Pipe dataPipe6 = new Pipe(dataPipe5) {
				@Override
				public void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception {
					XMLEngine.transform(new StreamSource(inputStream), "xsl/rdf2html.xsl", new StreamResult(outputStream));
				}
			};
			//convert xml to html (using xslt)
			EndTerminal dataPipe7 = new EndTerminal(dataPipe6,outputStream) {
				@Override
				public void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception {
					XMLEngine.transform(new StreamSource(inputStream), "xsl/to-html.xsl", new StreamResult(outputStream));
				}
			};
			//Start the whole pipe
			dataPipe7.start();

		} catch (Exception ex) {
			throw new IOException(ex);
		}
	}
}

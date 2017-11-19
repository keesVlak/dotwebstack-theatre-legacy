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

      //Get all appearance data (multiple representations)
      ByteArrayOutputStream appearanceData = new ByteArrayOutputStream();
      XMLMerger configMerger = new XMLMerger(appearanceData);
      configMerger.startMerging("appearances");
      addAppearance(configMerger,representation);
      //At this moment, only one subRepresentation can exists
      Representation subRepresentation = representation.getSubRepresentation();
      if (subRepresentation!=null) {
        addAppearance(configMerger,subRepresentation);
      }
      configMerger.finishMerging();

      //Translate to elmo1 vocabulary configuration
      StartTerminal configPipe1 = new StartTerminal(null,new ByteArrayInputStream(appearanceData.toByteArray())) {
        @Override
        public void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception {
          XMLEngine.transform(new StreamSource(inputStream), "xsl/config.xsl", new StreamResult(outputStream));
        }
      };
      //Merge configuration result with context (empty at this moment)
      Pipe configPipe2 = new Pipe(configPipe1) {
        @Override
        public void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception {
          XMLMerger.merge("root", outputStream, new StreamSource(inputStream));
        }
      };
      //rdf2view.xsl (create configuration XML from RDF). Result is used more than ones, so store
      ByteArrayOutputStream view = new ByteArrayOutputStream();
      EndTerminal configPipe3 = new EndTerminal(configPipe2,view) {
        @Override
        public void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception {
          XMLEngine.transform(new StreamSource(inputStream), "xsl/rdf2view.xsl", new StreamResult(outputStream));
        }
      };
      //Start the whole pipe
      configPipe3.start();

      //Create a buffer for the XML result from multiple information products, and create head
      ByteArrayOutputStream rdfData = new ByteArrayOutputStream();
      XMLMerger dataMerger = new XMLMerger(rdfData);
      dataMerger.startMerging("results");
      
      //Construct data pipe (start has already been created: dataPipe)
      //Transform from sparql result to rdf (cleaned)
      Pipe dataPipe1 = new Pipe(view,dataPipe) {
        @Override
        public void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception {
          XMLMerger.merge("root",outputStream,new StreamSource(inputStream), new StreamSource(new ByteArrayInputStream(((ByteArrayOutputStream)input).toByteArray())));
        }
      };
      //original sparql2rdfa doesn't expect a view, but a representation. In this case, we supply an index to the stylesheet
      Pipe dataPipe2 = new Pipe(dataPipe1) {
        @Override
        public void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception {
          XMLEngine.transform(new StreamSource(inputStream), "xsl/sparql2rdfa.xsl", new StreamResult(outputStream),0);
        }
      };
      //Add result of information product to result stream (buffered)
      EndTerminal dataPipe3 = new EndTerminal(dataMerger,dataPipe2,rdfData) {
        @Override
        public void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception {
          ((XMLMerger)input).addXML(new StreamSource(inputStream));
        }
      };
      
      //Start the datapipe
      dataPipe3.start();

      //Build a pipe to process a subrepresentation. The result should be part of the rdfData stream.
      //At this moment: only one subRepresentation is available, and no RDF data is currently available from the framework
      if (subRepresentation!=null) {
        
        //SubRepresentation is present, so start adding the subrepresentation
        StartTerminal subDataPipe1 = new StartTerminal(subRepresentation) {
          @Override
          public void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception {
            //The data of the subrepresentation isn't available yet, so we need to fetch it...
            FrameworkGhost.getXML((Representation)input,outputStream);
          }
        };
        //Transform from sparql result to rdf (cleaned)
        Pipe subDataPipe2 = new Pipe(view,subDataPipe1) {
          @Override
          public void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception {
            XMLMerger.merge("root",outputStream,new StreamSource(inputStream), new StreamSource(new ByteArrayInputStream(((ByteArrayOutputStream)input).toByteArray())));
          }
        };
      //original sparql2rdfa doesn't expect a view, but a representation. In this case, we supply an index to the stylesheet
        Pipe subDataPipe3 = new Pipe(subDataPipe2) {
          @Override
          public void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception {
            XMLEngine.transform(new StreamSource(inputStream), "xsl/sparql2rdfa.xsl", new StreamResult(outputStream),1);
          }
        };
        //Add result of information product to result stream (buffered)
        EndTerminal subDataPipe4 = new EndTerminal(dataMerger,subDataPipe3,rdfData) {
          @Override
          public void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception {
            ((XMLMerger)input).addXML(new StreamSource(inputStream));
          }
        };
        
        //Start the datapipe
        subDataPipe4.start();
      }
      
      //Finish merging
      dataMerger.finishMerging();

      //Start new pipe with combined results and view
      
      //Merge view with context and original data
      StartTerminal resultPipe1 = new StartTerminal(view,new ByteArrayInputStream(rdfData.toByteArray())) {
        @Override
        public void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception {
          XMLMerger.merge("root", outputStream, new StreamSource(new StringReader(CONTEXT)), new StreamSource(new ByteArrayInputStream(((ByteArrayOutputStream)input).toByteArray())),new StreamSource(inputStream));
        }
      };
      //rdf2rdfa.xsl (create RDF annotated with UI declarations)
      Pipe resultPipe2 = new Pipe(resultPipe1) {
        @Override
        public void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception {
          XMLEngine.transform(new StreamSource(inputStream), "xsl/rdf2rdfa.xsl", new StreamResult(outputStream));
        }
      };
      //rdf2html.xsl (create HTML from RDF annotated with UI declarations)
      Pipe resultPipe3 = new Pipe(resultPipe2) {
        @Override
        public void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception {
          XMLEngine.transform(new StreamSource(inputStream), "xsl/rdf2html.xsl", new StreamResult(outputStream));
        }
      };
      //convert xml to html (using xslt)
      EndTerminal resultPipe4 = new EndTerminal(resultPipe3,outputStream) {
        @Override
        public void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception {
          XMLEngine.transform(new StreamSource(inputStream), "xsl/to-html.xsl", new StreamResult(outputStream));
        }
      };
      //Start the whole pipe
      resultPipe4.start();

    } catch (Exception ex) {
      throw new IOException(ex);
    }
  }
  
  private static void addAppearance(XMLMerger merger, Representation representation) throws IOException {
    
    Appearance appearance = representation.getAppearance();
    StartTerminal pipe1 = new StartTerminal(appearance) {
      @Override
      public void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception {
        //Due to a missing feature in the Represention class, getApperance can return null, in such a case, an empty appearance is needed
        Model model;
        if (input!=null) {
          model = ((Appearance)input).getModel();
        } else {
          model = new LinkedHashModel();
        }
        Rio.write(model, outputStream, RDFFormat.RDFXML);
      }
    };
    //Merge with identifier of appearance, if any exists
    String appearanceIri = representation.getIdentifier().toString();
    if (appearance!=null) {
      appearanceIri = appearance.getIdentifier().toString();
    }
    Pipe pipe2 = new Pipe(appearanceIri,pipe1) {
      @Override
      public void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception {
        XMLMerger.merge("appearance", outputStream, new StreamSource(new StringReader(String.format("<id>%s</id>",(String)input))), new StreamSource(inputStream));
      }
    };
    //Merge
    EndTerminal pipe3 = new EndTerminal(merger,pipe2,null) {
      @Override
      public void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception {
        ((XMLMerger)input).addXML(new StreamSource(inputStream));
      }
    };
    //Start the whole pipe
    pipe3.start();
  }
}

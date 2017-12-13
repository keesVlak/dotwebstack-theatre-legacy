package org.dotwebstack.ldtlegacy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.container.ContainerRequestContext;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.dotwebstack.framework.frontend.ld.appearance.Appearance;
import org.dotwebstack.framework.frontend.ld.entity.GraphEntity;
import org.dotwebstack.framework.frontend.ld.entity.TupleEntity;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.param.Parameter;
import org.dotwebstack.ldtlegacy.pipe.EndTerminal;
import org.dotwebstack.ldtlegacy.pipe.Pipe;
import org.dotwebstack.ldtlegacy.pipe.StartTerminal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLWriter;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

public class CreatePage {

  public static void write(OutputStream outputStream, GraphEntity graphEntity,
      String linkstrategy) throws IOException {

    try {
      //Convert rdf result to xml
      StartTerminal dataPipe = new StartTerminal(graphEntity) {
        @Override
        public void filter(Object input, InputStream inputStream, OutputStream outputStream)
            throws Exception {
          QueryResults.report(((GraphEntity)input).getQueryResult(),new RDFXMLWriter(outputStream));
        }
      };
      write(outputStream,dataPipe,graphEntity.getRepresentation(),
          ((LegacyGraphEntity)graphEntity).getContainerRequestContext(), linkstrategy);

    } catch (Exception ex) {
      throw new IOException(ex);
    }
  }

  public static void write(OutputStream outputStream, TupleEntity tupleEntity,
      String linkstrategy) throws IOException {

    try {
      //Convert rdf result to xml
      StartTerminal dataPipe = new StartTerminal(tupleEntity) {
        @Override
        public void filter(Object input, InputStream inputStream, OutputStream outputStream)
            throws Exception {
          QueryResults.report(((TupleEntity)input).getQueryResult(),
              new SPARQLResultsXMLWriter(outputStream));
        }
      };
      write(outputStream,dataPipe,tupleEntity.getRepresentation(),
          ((LegacyTupleEntity)tupleEntity).getContainerRequestContext(), linkstrategy);

    } catch (Exception ex) {
      throw new IOException(ex);
    }
  }

  public static void write(OutputStream outputStream, Pipe dataPipe, Representation representation,
      ContainerRequestContext containerRequestContext, String linkstrategy) throws IOException {

    try {
      //Construct config pipe
      //Get XML configuration according to elmo2 vocabulary

      //Get appearance data
      ByteArrayOutputStream appearanceData = new ByteArrayOutputStream();
      XmlMerger configMerger = new XmlMerger(appearanceData);
      configMerger.startMerging("appearances");
      addAppearance(configMerger,representation);

      //Get appearances from all sub representations
      for (Representation subRepresentation : representation.getSubRepresentations()) {
        addAppearance(configMerger,subRepresentation);
      }
      
      configMerger.finishMerging();

      //Translate to elmo1 vocabulary configuration
      StartTerminal configPipe1 = new StartTerminal(null,
          new ByteArrayInputStream(appearanceData.toByteArray())) {
        @Override
        public void filter(Object input, InputStream inputStream, OutputStream outputStream)
            throws Exception {
          XmlEngine.transform(new StreamSource(inputStream), "xsl/config.xsl",
              new StreamResult(outputStream));
        }
      };
      //Merge configuration result with context (empty at this moment)
      Context context = new Context(containerRequestContext,linkstrategy);
      Pipe configPipe2 = new Pipe(configPipe1) {
        @Override
        public void filter(Object input, InputStream inputStream, OutputStream outputStream)
            throws Exception {
          XmlMerger.merge("root", outputStream, new StreamSource(
              new StringReader(context.getContextXml())), new StreamSource(inputStream));
        }
      };
      //rdf2view.xsl (create configuration XML from RDF). Result is used more than ones, so store
      ByteArrayOutputStream view = new ByteArrayOutputStream();
      EndTerminal configPipe3 = new EndTerminal(configPipe2,view) {
        @Override
        public void filter(Object input, InputStream inputStream, OutputStream outputStream)
            throws Exception {
          XmlEngine.transform(new StreamSource(inputStream), "xsl/rdf2view.xsl",
              new StreamResult(outputStream));
        }
      };
      //Start the whole pipe
      configPipe3.start();

      //Create a buffer for the XML result from multiple information products, and create head
      ByteArrayOutputStream rdfData = new ByteArrayOutputStream();
      XmlMerger dataMerger = new XmlMerger(rdfData);
      dataMerger.startMerging("results");
      
      //Construct data pipe (start has already been created: dataPipe)
      //Transform from sparql result to rdf (cleaned)
      Pipe dataPipe1 = new Pipe(view,dataPipe) {
        @Override
        public void filter(Object input, InputStream inputStream, OutputStream outputStream)
            throws Exception {
          XmlMerger.merge("root",outputStream,new StreamSource(inputStream), new StreamSource(
              new ByteArrayInputStream(((ByteArrayOutputStream)input).toByteArray())));
        }
      };
      //original sparql2rdfa doesn't expect a view, but a representation.
      //In this case, we supply an index to the stylesheet
      Pipe dataPipe2 = new Pipe(dataPipe1) {
        @Override
        public void filter(Object input, InputStream inputStream, OutputStream outputStream)
            throws Exception {
          XmlEngine.transform(new StreamSource(inputStream), "xsl/sparql2rdfa.xsl",
              new StreamResult(outputStream),1);
        }
      };
      //Add result of information product to result stream (buffered)
      EndTerminal dataPipe3 = new EndTerminal(dataMerger,dataPipe2,rdfData) {
        @Override
        public void filter(Object input, InputStream inputStream, OutputStream outputStream)
            throws Exception {
          ((XmlMerger)input).addXml(new StreamSource(inputStream));
        }
      };
      
      //Start the datapipe
      dataPipe3.start();

      // get parameters
      Map<String, Object> parameterValues = new HashMap<>();
      containerRequestContext.getUriInfo().getQueryParameters().forEach((name, value) -> {
        if (!value.isEmpty()) {
          parameterValues.put(name, value.get(0));
        }
      });

      //Fetch data from all sub representations. The result will be part of the rdfData stream.
      int index = 1;
      for (Representation subRepresentation : representation.getSubRepresentations()) {
        addData(dataMerger, view, subRepresentation, parameterValues, index++,
                containerRequestContext);
      }
      
      //Finish merging
      dataMerger.finishMerging();

      //Start new pipe with combined results and view
      
      //Merge view with context and original data
      StartTerminal resultPipe1 = new StartTerminal(view,
          new ByteArrayInputStream(rdfData.toByteArray())) {
        @Override
        public void filter(Object input, InputStream inputStream, OutputStream outputStream)
            throws Exception {
          XmlMerger.merge("root", outputStream,new StreamSource(
              new StringReader(context.getContextXml())), new StreamSource(
                new ByteArrayInputStream(((ByteArrayOutputStream)input).toByteArray())),
                    new StreamSource(inputStream));
        }
      };
      //rdf2rdfa.xsl (create RDF annotated with UI declarations)
      Pipe resultPipe2 = new Pipe(resultPipe1) {
        @Override
        public void filter(Object input, InputStream inputStream, OutputStream outputStream)
            throws Exception {
          XmlEngine.transform(new StreamSource(inputStream), "xsl/rdf2rdfa.xsl",
              new StreamResult(outputStream));
        }
      };
      //rdf2html.xsl (create HTML from RDF annotated with UI declarations)
      Pipe resultPipe3 = new Pipe(resultPipe2) {
        @Override
        public void filter(Object input, InputStream inputStream, OutputStream outputStream)
            throws Exception {
          XmlEngine.transform(new StreamSource(inputStream), "xsl/rdf2html.xsl",
              new StreamResult(outputStream));
        }
      };
      //convert xml to html (using xslt)
      EndTerminal resultPipe4 = new EndTerminal(resultPipe3,outputStream) {
        @Override
        public void filter(Object input, InputStream inputStream, OutputStream outputStream)
            throws Exception {
          XmlEngine.transform(new StreamSource(inputStream), "xsl/to-html.xsl",
              new StreamResult(outputStream));
        }
      };
      //Start the whole pipe
      resultPipe4.start();

    } catch (Exception ex) {
      throw new IOException(ex);
    }
  }
  
  private static void addAppearance(XmlMerger merger, Representation representation)
      throws IOException {
    
    StartTerminal pipe1 = new StartTerminal(representation.getAppearance()) {
      @Override
      public void filter(Object input, InputStream inputStream, OutputStream outputStream)
          throws Exception {
        //Due to a missing feature in the Represention class, getApperance can return null,
        //in such a case, an empty appearance is needed
        Model model;
        if (input != null) {
          model = ((Appearance)input).getModel();
        } else {
          model = new LinkedHashModel();
        }
        Rio.write(model, outputStream, RDFFormat.RDFXML);
      }
    };
    //Merge with identifier of appearance, if any exists
    Pipe pipe2 = new Pipe(representation,pipe1) {
      @Override
      public void filter(Object input, InputStream inputStream, OutputStream outputStream)
          throws Exception {
        String representationIri = ((Representation)input).getIdentifier().toString();
        String appearanceIri = representationIri;
        Appearance appearance = ((Representation)input).getAppearance();
        if (appearance != null) {
          appearanceIri = appearance.getIdentifier().toString();
        }
        XmlMerger.merge("appearance", outputStream, new StreamSource(new StringReader(
            String.format("<app uri='%s' rep='%s'/>",appearanceIri,representationIri))),
                new StreamSource(inputStream));
      }
    };
    //Merge
    EndTerminal pipe3 = new EndTerminal(merger,pipe2,null) {
      @Override
      public void filter(Object input, InputStream inputStream, OutputStream outputStream)
          throws Exception {
        ((XmlMerger)input).addXml(new StreamSource(inputStream));
      }
    };
    //Start the whole pipe
    pipe3.start();
  }
  
  private static void addData(XmlMerger merger, OutputStream view, Representation representation,
                              Map<String, Object> parameterValues, int index,
                              ContainerRequestContext containerRequestContext) throws IOException {

    //SubRepresentation is present, so start adding the subrepresentation
    StartTerminal pipe1 = new StartTerminal(representation) {
      @Override
      public void filter(Object input, InputStream inputStream, OutputStream outputStream)
          throws Exception {
        //The data of the subrepresentation isn't available yet, so we need to fetch it...
        FrameworkGhost.getXml((Representation)input, parameterValues, outputStream,
                containerRequestContext);
      }
    };
    //Transform from sparql result to rdf (cleaned)
    Pipe pipe2 = new Pipe(view,pipe1) {
      @Override
      public void filter(Object input, InputStream inputStream, OutputStream outputStream)
          throws Exception {
        XmlMerger.merge("root",outputStream,new StreamSource(inputStream),new
            StreamSource(new ByteArrayInputStream(((ByteArrayOutputStream)input).toByteArray())));
      }
    };
    //original sparql2rdfa doesn't expect a view, but a representation.
    //In this case, we supply an index to the stylesheet
    Pipe pipe3 = new Pipe(index,pipe2) {
      @Override
      public void filter(Object input, InputStream inputStream, OutputStream outputStream)
          throws Exception {
        XmlEngine.transform(new StreamSource(inputStream), "xsl/sparql2rdfa.xsl",
            new StreamResult(outputStream),(int)input);
      }
    };
    //Add result of information product to result stream (buffered)
    EndTerminal pipe4 = new EndTerminal(merger,pipe3,null) {
      @Override
      public void filter(Object input, InputStream inputStream, OutputStream outputStream)
          throws Exception {
        ((XmlMerger)input).addXml(new StreamSource(inputStream));
      }
    };

    //Start the datapipe
    pipe4.start();
  }
}

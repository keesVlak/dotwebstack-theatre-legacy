package org.dotwebstack.ldtlegacy;

import com.google.common.collect.ImmutableMap;
import java.io.OutputStream;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLWriter;

/********
 FrameworkGhost is a class that contains functionality that should be part of the framework, but isn't
 A separate class is made, to make clear where reengineering should take place.

 Data from subrepresentations aren't retrieved as part of RepresentationRequestHandler
 So we need to fetch the data ourselves. This means that data from subrepresentations are retrieved
 as part of the theatre legacy implementation, and not as part of the framework.
*********/

public class FrameworkGhost {

  public static Object fetchInformationProductData(Representation representation) {
    return representation.getInformationProduct().getResult(ImmutableMap.of());
  }
  
  public static void getXML(Representation representation, OutputStream outputStream) {
    Object result = representation.getInformationProduct().getResult(ImmutableMap.of());
    if (result instanceof GraphQueryResult) {
      QueryResults.report((GraphQueryResult)result,new RDFXMLWriter(outputStream));
    }
    if (result instanceof TupleQueryResult) {
      QueryResults.report((TupleQueryResult)result,new SPARQLResultsXMLWriter(outputStream));
    }
  }

}

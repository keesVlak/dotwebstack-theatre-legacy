package org.dotwebstack.ldtlegacy;

import com.google.common.collect.ImmutableMap;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.dotwebstack.framework.informationproduct.InformationProduct;
import org.dotwebstack.framework.param.Parameter;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLWriter;

import javax.ws.rs.container.ContainerRequestContext;

/********
 FrameworkGhost is a class that contains functionality that could be part of the framework.
 A separate class is made, to make clear where reengineering should take place.

 Data from subrepresentations aren't retrieved as part of RepresentationRequestHandler
 So we need to fetch the data ourselves. This means that data from subrepresentations are retrieved
 as part of the theatre legacy implementation, and not as part of the framework.
*********/

public class FrameworkGhost {

  public static Object fetchInformationProductData(Representation representation, Map<String, Object> parameterValues) {
    return representation.getInformationProduct().getResult(parameterValues);
  }
  
  public static void getXml(Representation representation, Map<String, Object> parameterValues, OutputStream outputStream) {
    Object result = representation.getInformationProduct().getResult(parameterValues);
    if (result instanceof GraphQueryResult) {
      QueryResults.report((GraphQueryResult)result,new RDFXMLWriter(outputStream));
    }
    if (result instanceof TupleQueryResult) {
      QueryResults.report((TupleQueryResult)result,new SPARQLResultsXMLWriter(outputStream));
    }
  }

}

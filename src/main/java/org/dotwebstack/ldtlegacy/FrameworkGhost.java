package org.dotwebstack.ldtlegacy;

import java.io.OutputStream;
import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLWriter;


/********
 FrameworkGhost is a class that contains functionality that could be part of the framework.
 A separate class is made, to make clear where reengineering should take place.

 Data from subrepresentations aren't retrieved as part of RepresentationRequestHandler
 So we need to fetch the data ourselves. This means that data from subrepresentations are retrieved
 as part of the theatre legacy implementation, and not as part of the framework.
*********/

public class FrameworkGhost {

  public static Object fetchInformationProductData(Representation representation,
                                                   Map<String, String> parameterValues,
                                                   ContainerRequestContext context) {
    representation.getParameterMappers().forEach(parameterMapper ->
        parameterValues.putAll(parameterMapper.map(context)));

    return representation.getInformationProduct().getResult(parameterValues);
  }
  
  public static void getXml(Representation representation, Map<String, String> parameterValues,
                            OutputStream outputStream,
                            ContainerRequestContext containerRequestContext) {
    representation.getParameterMappers().forEach(
        parameterMapper -> parameterValues.putAll(parameterMapper.map(containerRequestContext)));

    Object result = representation.getInformationProduct().getResult(parameterValues);
    if (result instanceof GraphQueryResult) {
      QueryResults.report((GraphQueryResult)result,new RDFXMLWriter(outputStream));
    }
    if (result instanceof TupleQueryResult) {
      QueryResults.report((TupleQueryResult)result,new SPARQLResultsXMLWriter(outputStream));
    }
  }

}

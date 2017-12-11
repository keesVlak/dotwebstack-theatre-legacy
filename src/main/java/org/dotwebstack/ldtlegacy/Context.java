package org.dotwebstack.ldtlegacy;

import java.net.URI;
import javax.ws.rs.container.ContainerRequestContext;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.entity.TupleEntity;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.eclipse.rdf4j.query.GraphQueryResult;

public class Context {

  private static final String CONTEXT_TEMPLATE =
      "<context staticroot='/assets' linkstrategy='%s'>"
          + "<title>LDT 2.0 alfa</title><url>%s</url></context>";

  private final String contextXml;
      
  public Context(@NonNull ContainerRequestContext containerRequestContext, String linkstrategy) {

    URI uri = containerRequestContext.getUriInfo().getAbsolutePath();
    
    /*
     * Remove first 'domain' part of path that we have added in HostPreMatchingRequestFilter
     */
    String path = uri.getPath().replaceAll("^/" + uri.getHost(), "");

    String fullUrl = String.format("%s://%s%s",uri.getScheme(),uri.getAuthority(),path);
    contextXml = String.format(CONTEXT_TEMPLATE,linkstrategy,fullUrl);
  }
  
  public String getContextXml() {
    return contextXml;
  }

}
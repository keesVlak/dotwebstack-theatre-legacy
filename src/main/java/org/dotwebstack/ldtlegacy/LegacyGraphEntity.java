package org.dotwebstack.ldtlegacy;

import javax.ws.rs.container.ContainerRequestContext;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.entity.GraphEntity;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.eclipse.rdf4j.query.GraphQueryResult;

public class LegacyGraphEntity extends GraphEntity {
  
  private ContainerRequestContext containerRequestContext;
  
  public LegacyGraphEntity(GraphEntity graphEntity,
      ContainerRequestContext containerRequestContext) {
    super(graphEntity.getQueryResult(),graphEntity.getRepresentation());
    this.containerRequestContext = containerRequestContext;
  }
  
  public ContainerRequestContext getContainerRequestContext() {
    return containerRequestContext;
  }

}
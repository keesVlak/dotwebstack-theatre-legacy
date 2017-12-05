package org.dotwebstack.ldtlegacy;

import javax.ws.rs.container.ContainerRequestContext;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.ld.entity.TupleEntity;
import org.dotwebstack.framework.frontend.ld.representation.Representation;
import org.eclipse.rdf4j.query.GraphQueryResult;

public class LegacyTupleEntity extends TupleEntity {
  
  private ContainerRequestContext containerRequestContext;
  
  public LegacyTupleEntity(TupleEntity tupleEntity,
      ContainerRequestContext containerRequestContext) {
    super(tupleEntity.getQueryResult(),tupleEntity.getRepresentation());
    this.containerRequestContext = containerRequestContext;
  }
  
  public ContainerRequestContext getContainerRequestContext() {
    return containerRequestContext;
  }

}
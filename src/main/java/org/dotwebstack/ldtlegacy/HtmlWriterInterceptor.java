package org.dotwebstack.ldtlegacy;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;
import org.dotwebstack.framework.frontend.ld.entity.GraphEntity;
import org.dotwebstack.framework.frontend.ld.entity.TupleEntity;

public class HtmlWriterInterceptor implements WriterInterceptor {
  @Override
  public void aroundWriteTo(WriterInterceptorContext context)
                  throws IOException {
    Object entity = context.getEntity();
    if (entity instanceof GraphEntity) {
      context.setEntity(new LegacyGraphEntity((GraphEntity)entity,
          (ContainerRequestContext)context.getProperty(
              ContainerRequestContext.class.getName())));
    }
    if (entity instanceof TupleEntity) {
      context.setEntity(new LegacyTupleEntity((TupleEntity)entity,
          (ContainerRequestContext)context.getProperty(
              ContainerRequestContext.class.getName())));
    }
    context.proceed();
  }
}

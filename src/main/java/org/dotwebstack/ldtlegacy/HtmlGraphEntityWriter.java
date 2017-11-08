package org.dotwebstack.ldtlegacy;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import lombok.NonNull;
import org.dotwebstack.framework.backend.ResultType;
import org.dotwebstack.framework.frontend.ld.writer.EntityWriter;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.query.GraphQueryResult;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.dotwebstack.framework.frontend.ld.entity.GraphEntity;
import org.dotwebstack.ldtlegacy.CreatePage;
import org.springframework.stereotype.Service;

import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.RDFFormat;

@Service
@EntityWriter(resultType = ResultType.GRAPH)
@Produces(MediaType.TEXT_HTML)
public class HtmlGraphEntityWriter implements MessageBodyWriter<GraphEntity> {

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations,
      MediaType mediaType) {
    return GraphEntity.class.isAssignableFrom(type) && mediaType.isCompatible(MediaType.TEXT_HTML_TYPE);
  }

  @Override
  public long getSize(GraphEntity graphEntity, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType) {
    // deprecated by JAX-RS 2.0 and ignored by Jersey runtime
    return -1;
  }
  
  @Override
  public void writeTo(GraphEntity graphEntity, Class<?> type, Type genericType,
      Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> multivaluedMap,
      OutputStream outputStream) throws IOException {

    CreatePage.write(outputStream,graphEntity);
  }
}

package org.dotwebstack.ldtlegacy;

import java.net.URI;
import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.layout.Layout;
import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.dotwebstack.ldtlegacy.vocabulary.XHTML;

public class Context {

  private static final String CONTEXT_TEMPLATE =
      "<context docroot='/%s' staticroot='/assets' linkstrategy='%s'>"
          + "<title>%s</title><request-path>%s</request-path>"
          + "<url>%s</url><subject>%s</subject>%s</context>";

  private final String contextXml;

  public Context(@NonNull ContainerRequestContext containerRequestContext, String linkstrategy,
                 Stage stage, Map<String, String> parameterValues) {
    URI uri = containerRequestContext.getUriInfo().getAbsolutePath();

    /*
     * Remove first 'domain' part of path that we have added in HostPreMatchingRequestFilter
     */
    final String path = uri.getPath().replaceAll("^/" + uri.getHost(), "");
    final String fullUrl = String.format("%s://%s%s", uri.getScheme(), uri.getAuthority(), path);
    String title = "LDT 2.0 alfa";
    if (stage.getTitle() != null) {
      title = stage.getTitle();
    }
    String stylesheet = "";
    String docRoot = "";
    if (stage.getSite().getBasePath() != null) {
      docRoot = stage.getSite().getBasePath();
    }
    Layout layout = stage.getLayout();
    if (layout == null) {
      layout = stage.getSite().getLayout();
    }
    if (layout != null) {
      stylesheet = String.format("<stylesheet href='%s/%s'/>", docRoot, layout.getOptions().size());
      if (layout.getOptions().containsKey(XHTML.STYLESHEET)) {
        stylesheet = String.format(
            "<stylesheet href='%s/assets/css/%s'/>", docRoot,
            layout.getOptions().get(XHTML.STYLESHEET).stringValue());
      }
    }
    String subject = "";
    if (parameterValues.containsKey("subject")) {
      subject = parameterValues.get("subject");
    }
    contextXml = String.format(CONTEXT_TEMPLATE, docRoot, docRoot, linkstrategy, title, path,
        fullUrl,
        subject,
        stylesheet);
    System.out.println("Create this contextXML: \n" + contextXml);
  }

  public String getContextXml() {
    return contextXml;
  }

}

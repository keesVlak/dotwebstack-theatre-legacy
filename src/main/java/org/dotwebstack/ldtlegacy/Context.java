package org.dotwebstack.ldtlegacy;

import java.net.URI;
import javax.ws.rs.container.ContainerRequestContext;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.layout.Layout;
import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.dotwebstack.ldtlegacy.vocabulary.XHTML;

public class Context {

  private static final String CONTEXT_TEMPLATE =
      "<context staticroot='/assets' linkstrategy='%s'>"
          + "<title>LDT 2.0 alfa</title><url>%s</url>"
          + "%s</context>";

  private final String contextXml;

  public Context(@NonNull ContainerRequestContext containerRequestContext, String linkstrategy,
                 Stage stage) {
    URI uri = containerRequestContext.getUriInfo().getAbsolutePath();

    /*
     * Remove first 'domain' part of path that we have added in HostPreMatchingRequestFilter
     */
    String path = uri.getPath().replaceAll("^/" + uri.getHost(), "");
    String fullUrl = String.format("%s://%s%s", uri.getScheme(), uri.getAuthority(), path);
    String stylesheet = "";
    Layout layout = stage.getLayout();
    if (layout == null) {
      layout = stage.getSite().getLayout();
    }
    if (layout != null) {
      stylesheet = String.format("<stylesheet href='%s'/>",layout.getOptions().size());
      if (layout.getOptions().containsKey(XHTML.STYLESHEET)) {
        stylesheet = String.format("<stylesheet href='/assets/css/%s'/>", layout.getOptions().get(XHTML.STYLESHEET).stringValue());
      }
    }
    contextXml = String.format(CONTEXT_TEMPLATE, linkstrategy, fullUrl, stylesheet);
  }

  public String getContextXml() {
    return contextXml;
  }

}
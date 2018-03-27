package org.dotwebstack.ldtlegacy;

import java.net.URI;
import javax.ws.rs.container.ContainerRequestContext;
import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.layout.Layout;
import org.dotwebstack.framework.frontend.http.stage.Stage;
import org.dotwebstack.ldtlegacy.vocabulary.XHTML;
import org.springframework.security.web.csrf.CsrfToken;

public class Context {

  private static final String CONTEXT_TEMPLATE =
      "<context staticroot='/assets' linkstrategy='%s'>"
          + "<title>%s</title><request-path>%s</request-path>"
              + "<url>%s</url><csrf>%s</csrf>%s</context>";

  private final String contextXml;
      
  public Context(@NonNull ContainerRequestContext containerRequestContext, String linkstrategy,
      Stage stage) {

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
    Layout layout = stage.getLayout();
    if (layout == null) {
      layout = stage.getSite().getLayout();
    }
    if (layout != null) {
      stylesheet = String.format("<stylesheet href='%s'/>",layout.getOptions().size());
      if (layout.getOptions().containsKey(XHTML.STYLESHEET)) {
        stylesheet = String.format("<stylesheet href='/assets/css/%s'/>",
            layout.getOptions().get(XHTML.STYLESHEET).stringValue());
      }
    }
    
    CsrfToken token = (CsrfToken) containerRequestContext.getProperty(CsrfToken.class.getName());
    String csrf = (token == null ? "" : token.getToken());

    contextXml = String.format(CONTEXT_TEMPLATE, linkstrategy, title, path, fullUrl, csrf,
        stylesheet);
  }

  public String getContextXml() {
    return contextXml;
  }

}

package org.dotwebstack.ldtlegacy;

import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import org.dotwebstack.framework.frontend.ld.handlers.DirectEndPointRequestHandler;
import org.dotwebstack.framework.frontend.ld.handlers.DynamicEndPointRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HtmlInterceptorFilterRegistrar implements DynamicFeature {

  private static final Logger LOG = LoggerFactory.getLogger(HtmlInterceptorFilterRegistrar.class);

  @Override
  public void configure(ResourceInfo resourceInfo, FeatureContext context) {
    if (DirectEndPointRequestHandler.class.equals(resourceInfo.getResourceClass())
        && resourceInfo.getResourceMethod().getName().equals("apply")
        || DynamicEndPointRequestHandler.class.equals(resourceInfo.getResourceClass())
        && resourceInfo.getResourceMethod().getName().equals("apply")) {
      context.register(HtmlWriterInterceptor.class);
      context.register(LegacyResponseFilter.class);
      LOG.info("Registered Interceptor and Filter for requestcontext passing");
    }
  }

}

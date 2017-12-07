package org.dotwebstack.ldtlegacy;

import lombok.NonNull;
import org.dotwebstack.framework.frontend.http.HttpConfiguration; 
import org.dotwebstack.framework.frontend.http.HttpModule; 
import org.springframework.stereotype.Service;

@Service
public class LegacyModule implements HttpModule {

  @Override
  public void initialize(@NonNull HttpConfiguration httpConfiguration) {
    httpConfiguration.register(HtmlInterceptorFilterRegistrar.class);
  }

}

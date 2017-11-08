package org.dotwebstack.ldtlegacy;

import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.URIResolver;
import javax.xml.transform.Source;

public class ClassPathResourceURIResolver implements URIResolver {

  @Override
  public Source resolve(String href, String base) throws TransformerException {
    return new StreamSource(ClassPathResourceURIResolver.class.getClassLoader().getResourceAsStream("xsl/"+href));
  }
}
	

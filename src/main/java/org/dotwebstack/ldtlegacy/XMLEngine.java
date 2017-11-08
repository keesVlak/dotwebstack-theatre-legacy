package org.dotwebstack.ldtlegacy;

import java.io.InputStream;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

public class XMLEngine {

	private static final TransformerFactory tfactory = TransformerFactory.newInstance();
	
	private static ClassPathResourceURIResolver uriResolver = null;

	public static void transform(StreamSource source, String xslResource, StreamResult result) throws TransformerConfigurationException,TransformerException { 

		// Set resolver, only ones
		if (uriResolver == null) {
			uriResolver = new ClassPathResourceURIResolver();
			tfactory.setURIResolver(uriResolver);
		}
	
		// Create input stream for the actual resource
		InputStream xslStream = XMLEngine.class.getClassLoader().getResourceAsStream(xslResource);
	
		// Create a transformer for the stylesheet. 
		Transformer transformer = tfactory.newTransformer(new StreamSource(xslStream)); 

		// Transform the source XML 
		transformer.transform(source, result);
		
	}
	
}

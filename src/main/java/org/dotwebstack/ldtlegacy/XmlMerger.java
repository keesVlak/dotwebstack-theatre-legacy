package org.dotwebstack.ldtlegacy;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;


public class XmlMerger {

  private static final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
  private static final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
  private static final XMLInputFactory inputFactory = XMLInputFactory.newFactory();

  private XMLEventWriter eventWriter;
  private String rootName = "";
  private boolean started = false;
  private boolean finished = false;

  public static void merge(String rootName, OutputStream result, StreamSource... source)
      throws XMLStreamException {

    XmlMerger merger = new XmlMerger(result);
    merger.startMerging(rootName);
    merger.addXml(source);
    merger.finishMerging();
  }

  public static void copy(OutputStream result, StreamSource source) throws XMLStreamException {
    
    XmlMerger merger = new XmlMerger(result);
    merger.copyXml(source);
  }

  public XmlMerger(OutputStream result) throws XMLStreamException {
    
    eventWriter = outputFactory.createXMLEventWriter(result,"UTF-8");
  }

  public void startMerging(String rootName) throws XMLStreamException {

    if (started) {
      throw new XMLStreamException("Merging already started");
    }
    started = true;
    
    this.rootName = rootName;

    // Create the root header of the document
    eventWriter.add(eventFactory.createStartDocument());
    eventWriter.add(eventFactory.createStartElement("","",rootName));
  }
  
  public void addXml(StreamSource... source) throws XMLStreamException {

    if (!started) {
      throw new XMLStreamException("Merging not started");
    }
    if (finished) {
      throw new XMLStreamException("Merging already finished");
    }
    
    // Copy original sources, without document begin and end
    for (int i = 0; i < source.length; i++) {
      XMLEventReader test = inputFactory.createXMLEventReader(source[i]);
      while (test.hasNext()) {
        XMLEvent event = test.nextEvent();
        //avoiding start(<?xml version="1.0"?>) and end of the documents;
        if (event.getEventType() != XMLEvent.START_DOCUMENT
            && event.getEventType() != XMLEvent.END_DOCUMENT) {
          eventWriter.add(event);
        }
      }
      test.close();
    }
  }

  public void finishMerging() throws XMLStreamException {
    
    if (!started) {
      throw new XMLStreamException("Merging not started");
    }
    if (finished) {
      throw new XMLStreamException("Merging already finished");
    }
    finished = true;
    
    eventWriter.add(eventFactory.createEndElement("", "", rootName));
    eventWriter.add(eventFactory.createEndDocument());

    eventWriter.close();
  }

  public void copyXml(StreamSource source) throws XMLStreamException {

    if (started) {
      throw new XMLStreamException("Merging already started");
    }
    if (finished) {
      throw new XMLStreamException("Merging already finished");
    }
    started = true;
    finished = true;

    XMLEventReader test = inputFactory.createXMLEventReader(source);
    while (test.hasNext()) {
      XMLEvent event = test.nextEvent();
      eventWriter.add(event);
    }
    test.close();

    eventWriter.close();
  }

}

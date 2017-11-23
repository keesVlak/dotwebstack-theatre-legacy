package org.dotwebstack.ldtlegacy.pipe;

import java.io.InputStream;
import java.io.OutputStream;

public abstract class EndTerminal {
  //Inputs to the pipe
  protected Pipe inputPipe = null;
  protected Object input = null;
  //Output from the pipe
  protected OutputStream outputStream = null;

  public EndTerminal(Object input, Pipe inputPipe, OutputStream output) {
    this.input = input;
    this.inputPipe = inputPipe;
    this.outputStream = output;
  }

  public EndTerminal(Pipe inputPipe, OutputStream output) {
    this.inputPipe = inputPipe;
    this.outputStream = output;
  }

  public abstract void filter(Object input, InputStream inputStream, OutputStream outputStream)
      throws Exception;

  public void start() {
    //Start the whole channel
    try {
      InputStream inputStream = null;
      if (inputPipe != null) {
        inputPipe.start();
        inputStream = inputPipe.getInputSinkStream();
      }
      filter(input,inputStream,outputStream);
    } catch (Exception ex) {
      //Nothing to catch
    }
  }
}
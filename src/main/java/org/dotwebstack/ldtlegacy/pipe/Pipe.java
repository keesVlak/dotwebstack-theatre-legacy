package org.dotwebstack.ldtlegacy.pipe;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream; 
import java.io.OutputStream; 

public abstract class Pipe extends Thread {
  //Inputs to the pipe
  protected Pipe inputPipe = null;
  protected Object input = null;
  protected InputStream inputStream = null; //This is an input to the terminal, that is not a pipe
  //Output from the pipe
  protected OutputStream outputStream = null;
  //Sink from output to another pipe
  protected PipedInputStream inputSinkStream = null;

  //Construct a pipe that is attached to an input-Pipe and has a piped output (and a object input)
  public Pipe(Object input, Pipe inputPipe) throws IOException {
    this.input = input;
    this.inputPipe = inputPipe;
    this.inputSinkStream = new PipedInputStream();
    this.outputStream = new PipedOutputStream(inputSinkStream);
  }

  //Construct a pipe that is attached to an input-Pipe and has a piped output
  public Pipe(Pipe inputPipe) throws IOException {
    this.inputPipe = inputPipe;
    this.inputSinkStream = new PipedInputStream();
    this.outputStream = new PipedOutputStream(inputSinkStream);
  }

  //Construct a pipe without any inputs
  protected Pipe() throws IOException {
    this.inputSinkStream = new PipedInputStream();
    this.outputStream = new PipedOutputStream(inputSinkStream);
  }

  //Construct a pipe with an object input
  protected Pipe(Object input) throws IOException {
    this.input = input;
    this.inputSinkStream = new PipedInputStream();
    this.outputStream = new PipedOutputStream(inputSinkStream);
  }

  //Construct a pipe with an object input and an inputstream
  protected Pipe(Object input, InputStream inputStream) throws IOException {
    this.input = input;
    this.inputStream = inputStream;
    this.inputSinkStream = new PipedInputStream();
    this.outputStream = new PipedOutputStream(inputSinkStream);
  }

  public InputStream getInputSinkStream() {
    return inputSinkStream;
  }

  public abstract void filter(Object input, InputStream inputStream, OutputStream outputStream) throws Exception;

  @Override
  public void start() {
    if (inputPipe != null) {
      inputPipe.start();
    }
    super.start();
  }

  @Override
  public void run() {
    try {
      InputStream inputStream = null;
      if (inputPipe != null) {
        inputStream = inputPipe.getInputSinkStream();
      } else {
        inputStream = this.inputStream;
      }
      filter(input,inputStream,outputStream);
    } catch (Exception ex) {
    } finally {
      try {
        outputStream.close();
      } catch (IOException ex) {
      }
    }
  }
}
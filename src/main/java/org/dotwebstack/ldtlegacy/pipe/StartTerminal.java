package org.dotwebstack.ldtlegacy.pipe;

import java.io.IOException;
import java.io.InputStream;

public abstract class StartTerminal extends Pipe {
  public StartTerminal() throws IOException {
    super();
  }
  public StartTerminal(Object input) throws IOException {
    super(input);
  }
  public StartTerminal(Object input, InputStream inputStream) throws IOException {
    super(input,inputStream);
  }

}
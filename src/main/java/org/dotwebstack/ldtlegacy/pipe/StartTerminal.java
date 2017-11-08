package org.dotwebstack.ldtlegacy.pipe;

import java.io.IOException;

public abstract class StartTerminal extends Pipe {
	public StartTerminal() throws IOException {
		super(null,null);
	}
	public StartTerminal(Object input) throws IOException {
		super(input,null);
	}
	
}
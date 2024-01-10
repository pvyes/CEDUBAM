package console;

import java.util.ArrayList;
import java.util.List;

public class CommandListener extends Thread {
	
	private List<ConsoleListener> listeners;
	
	public CommandListener() {
		listeners = new ArrayList<ConsoleListener>();
	}
	
	public CommandListener(ConsoleListener listener) {
		listeners = new ArrayList<ConsoleListener>();
		addListener(listener);
	}
	
	public void addListener(ConsoleListener listener) {
		listeners.add(listener);
	}	

	@Override
	public void start() {
		try {
			String command = ConsoleInOut.listen();
			boolean result = false;
			for (ConsoleListener listener: listeners) {
				result = listener.handleCommand(command);
			}
			if (!result) {
				ConsoleInOut.printErrormessage("The command could not be handled properly.");
			}
		} catch (Exception e) {
			ConsoleInOut.printErrormessage("The command could not be handled properly.");
			e.printStackTrace();
		} finally {
			start();
		}			
	}
}

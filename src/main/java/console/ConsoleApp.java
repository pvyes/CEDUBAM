package console;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import domain.Diagnoser;

public class ConsoleApp implements ConsoleListener {
	
	private static String REGEX_QUOTE = "(\"[^\"]+\"|'[^']+'|[^\\s\"])+";
	private static String REGEX_REMOVE = "[\"']";
	
	private static Diagnoser diagnoser;
	private static CommandListener listener;

	public ConsoleApp(String[] args) {
		diagnoser = new Diagnoser();
		listener = new CommandListener(this);
		listener.start();
		listener.setPriority(10);
		System.out.println("Console is listening...");
	}

	public void close() {
		ConsoleInOut.printMessage("Application has finished normally.", ConsoleInOut.Messagetype.CONFIRMATION);
		System.exit(0);
	}

	@Override
	public boolean handleCommand(String cmd) {		
		List<String> args = splitCmd(cmd);
		if (args.size() == 0) {
			return true; //nothing happens, no error
		} else {
			return handleCommand(args);
		}	
	}

	private boolean handleCommand(List<String> args) {
		ConsoleSet setters = new ConsoleSet(ConsoleApp.diagnoser);
		ConsoleDisplay display = new ConsoleDisplay(ConsoleApp.diagnoser);
		ConsoleRemove remove = new ConsoleRemove(ConsoleApp.diagnoser);
		ConsoleCompute compute = new ConsoleCompute(ConsoleApp.diagnoser);
		
		String base = args.get(0);
		
		switch (base) {
			case "quit":
			case "exit":
				close();
			case "run":
				return run(args, 1);
			case "set":
				return setters.handleCommand(args, 1);
			case "display":
				return display.handleCommand(args, 1);
			case "remove":
				return remove.handleCommand(args, 1);
			case "compute":
				return compute.handleCommand(args, 1);
			default:
				ConsoleInOut.printErrormessage("Invalid command");
		}
		return false;
	}
	
	private boolean run(List<String> args, int nextarg) {
		if (args.get(nextarg).trim().toLowerCase().equals("experiment")) {
			return runExperiment();
		} else {
			return runScript(args, nextarg);
		}
	}

	private boolean runExperiment() {
		try {
			ConsoleInOut.printMessage("The experiment is started ...");
			diagnoser.getExperiment().run();
			ConsoleInOut.printMessage("The experiment is finished succesfully.");
			return true;
		} catch (Exception e) {
			ConsoleInOut.printErrormessage("The experiment could not be executed. " + e.getMessage());
			return false;
		}
		
	}

	/* Runs a given script */
	private boolean runScript(List<String> args, int i) {
		try {
			List<String> cmds = getCommands(args.get(i));
			for (String cmd: cmds) {
				handleCommand(cmd);
			}
			ConsoleInOut.printMessage("The script is finished succesfully.");
			return true;
		} catch (Exception e) {
			ConsoleInOut.printErrormessage("The script could not be run." + e.getMessage());
			return false;
		}
	}
	
	private List<String> getCommands(String uri) throws IOException {
		List<String> cmds = new ArrayList<String>();
		BufferedReader reader;
		reader = new BufferedReader(new FileReader(uri));
		String line = reader.readLine();
		while (line != null) {
			if (!line.startsWith("#")) {
				cmds.add(line);
			}
			// read next line
			line = reader.readLine();
		}
		reader.close();
		return cmds;
	}

	private List<String> splitCmd(String cmd) {
		List<String> result = new ArrayList<String>();
		boolean stop = false;
		while (!stop) {
			String[] splitted = cmd.split(REGEX_QUOTE, 2);
			if (splitted.length > 1) {
				String arg = cmd.substring(0, cmd.length() - splitted[1].length());
				arg = arg.replaceAll(REGEX_REMOVE, "").trim();
				result.add(arg);
				cmd = splitted[1];
			} else {
				stop = true;
			}
		}
		return result;
	}
}

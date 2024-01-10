package app;

import com.bayesserver.License;

import console.ConsoleApp;

/**
 * Main class calling the interface.
 *
 */
public class App  {
	
	public static void main(String[] args) {
		boolean licensed = checklicense(args);
		String startmessage = "CEDBAM started";
		if (licensed) {
			startmessage += " with licensed Bayesserver version.";
		} else {
			startmessage += " with trial Bayesserver version.";
		}
		System.out.println(startmessage);
		new ConsoleApp(args);
	}

	private static boolean checklicense(String[] args) {
		if (args.length > 0) {
			if (args[0].split(":")[0].indexOf("license") > -1) {
				String license = args[0].split(":")[1];
				//set license
				try {
					License.validate(license);
				} catch(Exception e) {
					//licence is invalid
				}
			}
		}
		return License.getIsValid();
	}	
}
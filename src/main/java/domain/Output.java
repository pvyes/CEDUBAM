package domain;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Output {
	public static final String CRLF = "\n";
	public static final String CSV_DELIMITER = ";";
	
	public static void saveFile(String content, String folder, String name) throws IOException {
		File path = new File(folder);
		boolean dirExists = path.exists();
		if (!dirExists) {
			path.mkdir();
		}
		FileWriter out = new FileWriter(folder + "\\" + name);
		out.write(content);
		out.close();
	}
	

	public static void addToCsvfile(String folder, String csvname, List<String> csvkeys, List<String> csvrowvalues) throws IOException {
		getCsvFile(folder, csvname, csvkeys);
		FileWriter out = new FileWriter(folder + "\\" + csvname, true);
		out.write(toCsvString(csvrowvalues));
		out.close();		
	}
	
	private static File getCsvFile(String folder, String filename, List<String> csvkeys) throws IOException {
		File file = new File(folder + "\\" + filename);
		if (file.exists()) {
			return file;
		} else {
			file.createNewFile();
			FileWriter out = new FileWriter(folder + "\\" + filename);
			out.write(toCsvString(csvkeys));
			out.close();
			return file;
		}
	}
	
	public static boolean removeFile(String folder, String name) throws IOException {
		File file = new File(folder + "\\" + name);
		return file.delete();
	}
		
	/**
	 * Transforms the map in a csv string format.
	 * @param map
	 * @return
	 */	
	public static String toCsvString(List<String> list) {
		String output = "";
		for (String entry: list) {
			output += entry + CSV_DELIMITER;
		}
		output += CRLF;
		return output;
	}
}

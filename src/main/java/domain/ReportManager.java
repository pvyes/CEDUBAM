package domain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReportManager {
	public final String DEFAULT_EXPORT_FOLDER = "D:\\Documenten\\OU\\Graduation\\voorbeeldsysteem\\Results";
	public final String DEFAULT_FILENAME = "CEDBAM_report.txt";
	public final String DEFAULT_CSVNAME = "CEDBAM_report.csv";
	
	String[] csvkeys = {"filename","pdvs","costvariance","strategy","information","utility","expectedcost"};
	
	public enum Suffixtype {
		TIME,
		COUNT,
		NONE
	}
	
	public enum Detaillevel {
		SHORT,
		BASIC,
		DETAILED,
		HISTORY
	}
	
	private Diagnoser diagnoser;
	private String folder;
	private int displaydetail;
	private int exportdetail;
	private boolean export;
	private boolean display;
	private String filename;
	private String csvname;
	private Suffixtype suffixtype;
	private int counter;

	public ReportManager(Diagnoser diagnoser) {
		this.diagnoser = diagnoser;
		this.folder = DEFAULT_EXPORT_FOLDER;
		this.displaydetail = 1;
		this.exportdetail = 1;
		this.export = false;
		this.display = true;
		this.filename = DEFAULT_FILENAME;
		this.csvname = DEFAULT_CSVNAME;
		this.suffixtype = Suffixtype.TIME;
		this.counter = 0;
	}

	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
	}

	public int getDisplaydetail() {
		return displaydetail;
	}

	public void setDisplaydetail(int displaydetail) {
		this.displaydetail = displaydetail;
	}

	public int getExportdetail() {
		return exportdetail;
	}

	public void setExportdetail(int exportdetail) {
		this.exportdetail = exportdetail;
	}

	public boolean isExport() {
		return export;
	}

	public void setExport(boolean export) {
		this.export = export;
	}
	
	public boolean isDisplay() {
		return display;
	}

	public void setDisplay(boolean display) {
		this.display = display;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getCsvname() {
		return csvname;
	}

	public void setCsvname(String csvname) {
		this.csvname = csvname;
	}

	public Suffixtype getSuffixtype() {
		return suffixtype;
	}

	public void setSuffixtype(Suffixtype suffixtype) {
		this.suffixtype = suffixtype;
	}

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}
	
	public void resetCounter() {
		this.counter = 0;
	}

	public void export(ProbeScenario ps) throws Exception {
		String report = ps.makeReport(exportdetail);
		String suffix = getSuffix();
		String name = filename + "_" + suffix + ".txt";
		Output.saveFile(report, folder, name);
		//make csv row
		List<String> csvrow = makeCsvRow(name, ps);
		Output.addToCsvfile(folder, csvname, makeList(csvkeys), csvrow);		
	}

	private List<String> makeCsvRow(String name, ProbeScenario ps) {
		List<String> csvrow = new ArrayList<String>();
		for (int i = 0; i< csvkeys.length; i++) {
			if (csvkeys[i].equals("filename")) csvrow.add(i, name);
			if (csvkeys[i].equals("pdvs")) csvrow.add(i, diagnoser.getPdvs().toString());
			if (csvkeys[i].equals("costvariance")) csvrow.add(i, null);
			if (csvkeys[i].equals("strategy")) csvrow.add(i, diagnoser.getStrategy().getName().toString());
			if (csvkeys[i].equals("information")) csvrow.add(i, diagnoser.getInformationfunction().getInformationtype().toString());
			if (csvkeys[i].equals("utility")) csvrow.add(i, diagnoser.getUtilityfunction().getName());
			if (csvkeys[i].equals("expectedcost")) csvrow.add(i, Double.toString(ps.getExpectedCost()));
		}
		return csvrow;
	}

	private String getSuffix() {
		if (suffixtype == Suffixtype.COUNT) {
			return Integer.toString(counter++);
		}
		if (suffixtype == Suffixtype.TIME) {
			return new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + "_" + System.currentTimeMillis();
		}
		return "";
	}

	public List<File> getReportList() {
		List<File> reports = new ArrayList<File>();
	    File[] files = new File(folder).listFiles();
	    for (var i = 0; i < files.length; i++) {
	    	File f = files[i];
	    	if (!f.isDirectory()) {
	    		reports.add(f);
	    	}
	    }
	    return reports;
}

	public String getSettings() {
		String str = "";
		str += "display: " + this.display + "\n";
		str += "displaydetail: " + this.displaydetail + "\n";
		str += "export: " + this.export + "\n";
		str += "exportdetail: " + this.exportdetail + "\n";
		str += "folder: " + this.folder + "\n";
		str += "filename: " + this.filename + "\n";
		str += "csvname: " + this.csvname + "\n";
		str += "suffixtype: " + this.suffixtype + "\n";
		str += "counter: " + this.counter + "\n";		
		return str;
	}

	public String getReportFile(String arg) throws IOException {
		String str = "";
		List<File> list = getReportList();
		File f = null;
		if (hasIntegertype(arg)) {
			int index = Integer.parseInt(arg);
			f = list.get(index);
		} else {
			for (var i = 0; i < list.size(); i++) {
				if (list.get(i).getName().equals(arg)) {
					f = list.get(i);
					i = list.size();
				}
			}
		}
		BufferedReader br = new BufferedReader(new FileReader(f));
	    String s;
		while ((s = br.readLine()) != null) {
	    	str += s + "\n";
	    }
		br.close();
		return str;	
	}
	
	private boolean hasIntegertype(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	private <T> List<T> makeList(T[] list) {
		List<T> result =  new ArrayList<T>();
		for (T s: list) {
			result.add(s);
		}
		return result;
	}

	public boolean removeReport(String name) throws IOException {
		return Output.removeFile(folder, name);
	}
	
}

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class Teste {

	public static void main(String[] args) throws Exception {

		//loadFromFile();
	}

	public static void loadFromFile() throws Exception {
		List<String> lines = Files.readAllLines(Paths.get("C:/Users/Guisi/Desktop/SmartGrid/modelo.csv"), Charset.forName("ISO-8859-1"));
		
		List<String> loadLines = getLoadLines(lines);
		
		for (String load : loadLines) {
			System.out.println(load);
		}
	}
	
	private static List<String> getLoadLines(List<String> lines) {
		return getLines("-- Loads --", lines);
	}
	
	private static List<String> getLines(String label, List<String> lines) {
		List<String> filteredLines = new ArrayList<String>();

		boolean found = false;
		for (Iterator<String> iterator = lines.iterator(); iterator.hasNext();) {
			String line = iterator.next();
			
			if (!found && line.contains(label)) {
				iterator.next();
				line = iterator.next();
				found = true;
			}
			
			if (found) {
				if (isEmptyLine(line)) {
				 break;	
				}
				filteredLines.add(line);
			}
		}
		
		return filteredLines;
	}
	
	private static boolean isEmptyLine(String line) {
		return StringUtils.replace(line, ";", "").isEmpty();
	}
}

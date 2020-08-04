

package edgesys.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public final class FileUtils {
	private static final Logger LOG = 
		LoggerFactory.getLogger(FileUtils.class);


	public static void writeToFile(String outputFileName, String text) {
		BufferedWriter fileWriter = null;
		try {
			fileWriter = new BufferedWriter(new FileWriter(outputFileName, true));
			fileWriter.append(text);
		} catch (IOException ex) {
			System.out.println(ex);
			LOG.error(ex.getCause().toString());
			LOG.error(ex.toString());
		} finally {
			try { fileWriter.close(); } catch (Exception ex) {}
		}
	}
}
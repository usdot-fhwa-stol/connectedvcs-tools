package gov.usdot.cv.service.util;

import java.io.File;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

public class FileSystemHelper {
	
	static public String findFile(String filePattern) {
		return findFile(System.getProperty("user.dir"), filePattern);
	}
	
	static public String findFile(String dir, String filePattern) {
		Collection<File> files = findFiles(dir, filePattern);
		return files != null && !files.isEmpty() ? files.iterator().next().getAbsolutePath() : null;
	}
	
	static public Collection<File> findFiles(String dir, String filePattern) {
		try {
			IOFileFilter fileFilter =  new WildcardFileFilter(filePattern);
			fileFilter = FileFilterUtils.makeSVNAware(fileFilter);
			fileFilter = FileFilterUtils.makeCVSAware(fileFilter);
			IOFileFilter difFilter = TrueFileFilter.INSTANCE;
			difFilter = FileFilterUtils.makeSVNAware(difFilter);
			difFilter = FileFilterUtils.makeCVSAware(difFilter);
			return FileUtils.listFiles(new File(dir), fileFilter, difFilter);
		} catch ( IllegalArgumentException ex ) {
			return null;
		}
	}
}

/*

Copyright (C) 2025 LEIDOS.
Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy of
the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations under
the License.
*/
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

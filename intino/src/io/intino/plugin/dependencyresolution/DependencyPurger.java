package io.intino.plugin.dependencyresolution;

import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class DependencyPurger {
	private static final Logger LOG = Logger.getInstance(DependencyPurger.class.getName());


	public void purgeDependency(String id) {
		String[] split = id.split(":");
		split[0] = split[0].replace(".", File.separator);
		File file = new File(localRepository(), String.join(File.separator, split));
		if (!file.exists()) return;
		try {
			FileUtils.deleteDirectory(file);
		} catch (IOException e) {
			LOG.error(e);
		}
	}

	@NotNull
	private File localRepository() {
		return new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");
	}
}

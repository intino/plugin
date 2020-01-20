package io.intino.plugin.dependencyresolution;

import com.intellij.notification.Notification;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.LocalFileSystem;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import static com.intellij.notification.NotificationType.ERROR;

public class DependencyPurger {
	private static final Logger LOG = Logger.getInstance(DependencyPurger.class.getName());

	public void purgeDependency(String id) {
		String[] split = id.split(":");
		split[0] = split[0].replace(".", File.separator);
		File file = new File(localRepository(), String.join(File.separator, split));
		if (!file.exists()) return;
		try {
			FileUtils.deleteDirectory(file);
			LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file.getParentFile());
		} catch (IOException e) {
			Notifications.Bus.notify(new Notification("tara", "Dependency purge", "Imposible to remove dependency. Files are open", ERROR));
		}
	}

	@NotNull
	private File localRepository() {
		return new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository");
	}
}
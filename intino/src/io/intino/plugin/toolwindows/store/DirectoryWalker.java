package io.intino.plugin.toolwindows.store;

import com.intellij.openapi.project.Project;
import io.intino.plugin.toolwindows.store.Connection.File;

public class DirectoryWalker {
	private final String url;
	private final Connection connection;

	public DirectoryWalker(Project project, String url) {
		this.url = url;
		this.connection = isRemoteHost() ? new SshConnection(url) : new LocalConnection(project.getBasePath(), url);
	}

	public File root() {
		return connection.root();
	}


	private boolean isRemoteHost() {
		return !(url.startsWith("/") || url.startsWith("."));
	}

}
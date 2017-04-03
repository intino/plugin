package io.intino.plugin.settings;

public class ArtifactoryCredential {
	private static final String EMPTY = "";
	public String serverId = EMPTY;
	public String username = EMPTY;
	public String password = EMPTY;


	public ArtifactoryCredential(String serverId, String username, String password) {
		this.serverId = serverId;
		this.username = username;
		this.password = password;
	}
}

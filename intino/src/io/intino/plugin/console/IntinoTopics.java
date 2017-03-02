package io.intino.plugin.console;

import com.intellij.util.messages.Topic;

public class IntinoTopics {

	public static Topic<MavenListener> MAVEN = new Topic<>("maven-console", MavenListener.class);
}

package io.intino.plugin.console;

import com.intellij.util.messages.Topic;
import io.intino.plugin.actions.LegioListener;

public class IntinoTopics {

	public static Topic<MavenListener> MAVEN = new Topic<>("maven-console", MavenListener.class);
	public static Topic<LegioListener> LEGIO = new Topic<>("legio-modification", LegioListener.class);
}

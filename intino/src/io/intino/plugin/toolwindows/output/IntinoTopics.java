package io.intino.plugin.toolwindows.output;

import com.intellij.util.messages.Topic;
import io.intino.plugin.actions.LegioListener;
import io.intino.plugin.toolwindows.project.IntinoFileListener;

public class IntinoTopics {

	public static Topic<MavenListener> BUILD_CONSOLE = new Topic<>("build-console", MavenListener.class);
	public static Topic<LegioListener> LEGIO = new Topic<>("legio-modification", LegioListener.class);
	public static Topic<IntinoFileListener> FILE_MODIFICATION = new Topic<>("file-modification", IntinoFileListener.class);
}

package io.intino.plugin.toolwindows;

import com.intellij.util.messages.Topic;
import io.intino.plugin.actions.LegioListener;
import io.intino.plugin.toolwindows.factory.IntinoFileListener;
import io.intino.plugin.toolwindows.remote.IntinoRemoteConsoleListener;

public class IntinoTopics {
	public static Topic<IntinoRemoteConsoleListener> REMOTE_CONSOLE = new Topic<>("remove-console", IntinoRemoteConsoleListener.class);
	public static Topic<LegioListener> LEGIO = new Topic<>("legio-modification", LegioListener.class);
	public static Topic<IntinoFileListener> FILE_MODIFICATION = new Topic<>("file-modification", IntinoFileListener.class);
}
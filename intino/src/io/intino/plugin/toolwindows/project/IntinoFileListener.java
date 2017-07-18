package io.intino.plugin.toolwindows.project;

import java.util.EventListener;

public interface IntinoFileListener extends EventListener {
	void modified(String file);
}

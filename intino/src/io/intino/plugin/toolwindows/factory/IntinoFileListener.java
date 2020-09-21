package io.intino.plugin.toolwindows.factory;

import java.util.EventListener;

public interface IntinoFileListener extends EventListener {
	void modified(String file);
}

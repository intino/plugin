package io.intino.plugin.console;

import java.util.EventListener;

public interface MavenListener extends EventListener {
	void println(String line);
}

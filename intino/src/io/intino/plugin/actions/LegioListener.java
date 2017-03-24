package io.intino.plugin.actions;


import java.util.EventListener;

public interface LegioListener extends EventListener {
	void moduleJoinedToLegio(String moduleName);
}

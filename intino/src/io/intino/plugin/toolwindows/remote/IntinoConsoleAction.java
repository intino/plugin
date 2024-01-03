package io.intino.plugin.toolwindows.remote;

import io.intino.cesar.box.schemas.Application;

public interface IntinoConsoleAction {
	void onChanging();

	void onApplicationChange(Application application);
}

package io.intino.plugin.toolwindows.output;

import io.intino.cesar.box.schemas.ProcessInfo;
import io.intino.cesar.box.schemas.ProcessStatus;

public interface IntinoConsoleAction {

	void onChanging();

	void onProcessChange(ProcessInfo newProcess, ProcessStatus newProcessStatus);
}

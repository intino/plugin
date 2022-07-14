package io.intino.plugin.toolwindows.remote;

import io.intino.alexandria.logger.Logger;

public class Log {

	public String text;
	public Logger.Level level;

	public Log(String text, Logger.Level level) {
		this.text = text;
		this.level = level;
	}
}

package org.siani.legio.plugin.errorreporting;

import com.intellij.openapi.diagnostic.Logger;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;

import java.io.IOException;

class Slack {
	private static final Logger LOG = Logger.getInstance(Slack.class.getName());

	private SlackSession session = null;

	Slack(String token) throws IOException {
		if (!"".equals(token)) {
			session = SlackSessionFactory.createWebSocketSlackSession(token);
			session.connect();

		}
	}

	void sendMessageToAChannel(String channelName, String message, String body) throws IOException {
		this.sendMessageToAChannel(channelName, message);
		this.sendMessageToAChannel(channelName, body);
	}

	public void disconnect() throws IOException {
		session.disconnect();
	}

	private void sendMessageToAChannel(String channelName, String message) {
		if ((!"".equals(channelName)) && (session != null)) {
			//WARNING: First bot must be receive a invitation.
			SlackChannel channel = session.findChannelByName(channelName);
			session.sendMessage(channel, message);
		} else {
			if ("".equals(channelName)) LOG.warn("Slack channel name is empty.");
			if (session == null) LOG.warn("Slack session is null.");
		}
	}
}

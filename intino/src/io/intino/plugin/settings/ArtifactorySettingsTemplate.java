package io.intino.plugin.settings;

import org.siani.itrules.*;

import java.util.Locale;

import static org.siani.itrules.LineSeparator.*;

public class ArtifactorySettingsTemplate extends Template {

	protected ArtifactorySettingsTemplate(Locale locale, LineSeparator separator) {
		super(locale, separator);
	}

	public static Template create() {
		return new ArtifactorySettingsTemplate(Locale.ENGLISH, LF).define();
	}

	public Template define() {
		add(
			rule().add((condition("type", "artifactory"))).add(literal("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<settings xmlns=\"http://maven.apache.org/SETTINGS/1.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/SETTINGS/1.0.0\nhttps://maven.apache.org/xsd/settings-1.0.0.xsd\">\n\t<servers>\n\t\t")).add(mark("server").multiple("\n")).add(literal("\n\t</servers>\n</settings>")),
			rule().add((condition("type", "server"))).add(literal("<server>\n\t<id>")).add(mark("name")).add(literal("</id>\n\t<username>")).add(mark("username")).add(literal("</username>\n\t<password>")).add(mark("password")).add(literal("</password>\n</server>"))
		);
		return this;
	}
}
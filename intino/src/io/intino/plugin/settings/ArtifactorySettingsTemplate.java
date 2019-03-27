package io.intino.plugin.settings;

import org.siani.itrules.LineSeparator;
import org.siani.itrules.Template;

import java.util.Locale;

import static org.siani.itrules.LineSeparator.LF;

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
				rule().add((condition("type", "server"))).add(literal("<server>\n\t<id>")).add(mark("name")).add(literal("</id>\n\t<username>")).add(mark("username")).add(literal("</username>\n\t<password>")).add(mark("password")).add(literal("</password>\n\t<configuration>\n        <timeout>5000</timeout>\n        <httpConfiguration>\n          <all>\n            <connectionTimeout>5000</connectionTimeout>\n            <readTimeout>5000</readTimeout>\n          </all>\n        </httpConfiguration>\n    </configuration>\n</server>"))
		);
		return this;
	}
}
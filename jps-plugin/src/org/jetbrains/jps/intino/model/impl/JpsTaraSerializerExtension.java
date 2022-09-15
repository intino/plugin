package org.jetbrains.jps.intino.model.impl;

import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.intino.model.JpsIntinoExtensionService;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.module.JpsModule;
import org.jetbrains.jps.model.serialization.JpsModelSerializerExtension;
import org.jetbrains.jps.model.serialization.JpsProjectExtensionSerializer;

import java.util.List;

import static java.util.Collections.singletonList;

public class JpsTaraSerializerExtension extends JpsModelSerializerExtension {
	private static final String TARA_MODULE_OPTION_NAME = "io.intino.tara.isIntinoModule";

	@NotNull
	@Override
	public List<? extends JpsProjectExtensionSerializer> getProjectExtensionSerializers() {
		return singletonList(new IntinoSettingsSerializer());
	}

	@Override
	public void loadModuleOptions(@NotNull JpsModule module, @NotNull Element rootElement) {
		if (rootElement.getAttributeValue(TARA_MODULE_OPTION_NAME) != null)
			JpsIntinoExtensionService.instance().getOrCreateExtension(module);
	}

	private static class IntinoSettingsSerializer extends JpsProjectExtensionSerializer {
		private IntinoSettingsSerializer() {
			super(JpsIntinoSettings.FILE, JpsIntinoSettings.NAME);
		}

		@Override
		public void loadExtension(@NotNull JpsProject project, @NotNull Element componentTag) {
			JpsIntinoSettings settings = XmlSerializer.deserialize(componentTag, JpsIntinoSettings.class);
			IntinoJpsCompilerSettings component = new IntinoJpsCompilerSettings(settings);
			project.getContainer().setChild(IntinoJpsCompilerSettings.ROLE, component);
		}

		@Override
		public void loadExtensionWithDefaultSettings(@NotNull JpsProject project) {
			IntinoJpsCompilerSettings component = new IntinoJpsCompilerSettings(new JpsIntinoSettings());
			project.getContainer().setChild(IntinoJpsCompilerSettings.ROLE, component);
		}
	}
}

package io.intino.plugin.project.configuration;

import com.intellij.ide.projectView.ProjectView;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.intino.alexandria.logger.Logger;
import io.intino.magritte.dsl.Meta;
import io.intino.magritte.dsl.Proteo;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.module.IntinoModuleType;
import io.intino.plugin.project.module.IntinoWizardPanel;
import io.intino.plugin.project.module.IntinoWizardPanel.Components;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static io.intino.plugin.project.module.IntinoModuleType.Type.Business;
import static io.intino.plugin.project.module.IntinoWizardPanel.Components.MetaModel;

public class ModuleTemplateDeployer {
	private static final String url = "https://artifactory.intino.io/artifactory/infrastructure-templates/io/intino/$type/$version/$type-$version.zip";
	private final Module module;
	private final VirtualFile srcRoot;
	private List<Components> components;

	public ModuleTemplateDeployer(Module module, List<Components> components) {
		this.module = module;
		this.srcRoot = IntinoUtil.getSrcRoot(module);
		this.components = components;
	}

	public void deploy() {
		final IntinoModuleType.Type type = IntinoModuleType.type(module);
		final String groupId = IntinoModuleType.groupId(module);
		final LegioFileCreator legioFileCreator = new LegioFileCreator(module, components);
		final File srcDirectory = new File(srcRoot.toNioPath().toFile(), groupId.replace("-", "").replace(".", File.separator) + File.separator + module.getName().replace("-", ""));
		srcDirectory.mkdirs();
		if (components.contains(Components.Model) || components.contains(MetaModel)) writeModelFile(srcDirectory);
		if (type == null || Business.equals(type)) {
			final VirtualFile legio = legioFileCreator.getOrCreate(groupId);
			ProjectView.getInstance(module.getProject()).select(legio, legio, true);
			return;
		}
		final String realUrl = url.replace("$type", type.name().toLowerCase(Locale.ROOT)).replace("$version", "1.0.0");
		final ZipInputStream zipInputStream = new ZipInputStream(template(realUrl));
		Map<String, String> files = extract(zipInputStream);
		legioFileCreator.create(files.remove("artifact.legio").replace("$groupId", groupId).replace("$namePackage", module.getName().replace("-", "").toLowerCase()).replace("$name", module.getName().toLowerCase()));
		files.forEach((k, v) -> {
			try {
				Files.write(new File(srcDirectory, k).toPath(), v.replace("$package", groupId + "." + module.getName().replace("-", "").toLowerCase()).getBytes(StandardCharsets.UTF_8));
			} catch (IOException e) {
				Logger.error(e);
			}
		});
		final VirtualFile file = VfsUtil.findFile(srcDirectory.toPath(), true);
		ProjectView.getInstance(module.getProject()).select(file, file, true);
	}

	private void writeModelFile(File srcDirectory) {
		File model = new File(srcDirectory, "model");
		model.mkdirs();
		File taraModel = new File(model, "Model.tara");
		try {
			Files.writeString(taraModel.toPath(), "dsl " + (components.contains(Components.Model) ? Proteo.class.getSimpleName() : Meta.class.getSimpleName()) + "\n\n\n");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Map<String, String> extract(ZipInputStream zipInputStream) {
		Map<String, String> contents = new HashMap<>();
		try (ZipInputStream zin = zipInputStream) {
			ZipEntry ze;
			while ((ze = zin.getNextEntry()) != null) {
				contents.put(ze.getName(), new String(zin.readAllBytes()));
				zin.closeEntry();
			}
		} catch (IOException e) {
			Logger.error(e);
		}
		return contents;
	}

	private InputStream template(String url) {
		try {
			return new BufferedInputStream(new URL(url).openStream());
		} catch (IOException e) {
			Logger.error(e);
		}
		return InputStream.nullInputStream();
	}
}

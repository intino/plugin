package io.intino.plugin.project.configuration;

import com.amazonaws.util.StringInputStream;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.starters.local.StarterContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.intino.alexandria.logger.Logger;
import io.intino.itrules.FrameBuilder;
import io.intino.itrules.TemplateEngine;
import io.intino.itrules.parser.ITRulesSyntaxError;
import io.intino.itrules.readers.ItrRuleSetReader;
import io.intino.magritte.dsl.Meta;
import io.intino.magritte.dsl.Proteo;
import io.intino.plugin.file.KonosFileType;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.plugin.project.module.IntinoModuleType;
import io.intino.plugin.project.module.IntinoWizardPanel.Components;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static io.intino.plugin.lang.psi.impl.IntinoUtil.moduleRoot;
import static io.intino.plugin.project.module.IntinoModuleType.Type.Business;
import static io.intino.plugin.project.module.IntinoWizardPanel.Components.MetaModel;
import static java.io.File.separator;

public class ModuleTemplateDeployer {
	public static final String Artifactory = "https://artifactory.intino.io/artifactory/";
	private static final String url = Artifactory + "infrastructure-templates/io/intino/$type/$version/$type-$version.zip";
	private final Module module;
	private final VirtualFile srcRoot;
	private final List<Components> components;
	private final boolean gorosFramework;
	private final LegioFileCreator legioFileCreator;
	private final String groupId;

	public ModuleTemplateDeployer(Module module, List<Components> components, StarterContext starterContext, boolean gorosFramework) {
		this.module = module;
		this.srcRoot = IntinoUtil.getSrcRoot(module);
		this.components = components;
		this.gorosFramework = gorosFramework;
		this.legioFileCreator = new LegioFileCreator(module, components);
		this.groupId = starterContext.getGroup();
	}

	public void deploy() {
		IntinoModuleType.Type type = IntinoModuleType.type(module);
		if (type == null) type = Business;
		final File packageDirectory = new File(srcRoot.toNioPath().toFile(), groupId.replace("-", "").replace(".", separator) + separator + module.getName().replace("-", ""));
		packageDirectory.mkdirs();
		final VirtualFile packageVDirectory = VfsUtil.findFileByIoFile(packageDirectory, true);
		Map<String, String> files = download(type);
		writeModelAndBox(packageDirectory, files);
		if (Business.equals(type) || files.isEmpty())
			refreshView(packageVDirectory, legioFileCreator.getOrCreate(groupId));
		else {
			writeInfrastructureFiles(packageDirectory, files);
			refreshView(packageVDirectory);
		}
		if (gorosFramework) addModernizationToModule();
	}

	private void writeModelAndBox(File packageDirectory, Map<String, String> files) {
		if (components.contains(Components.Model) || components.contains(MetaModel)) writeModelFile(packageDirectory);
		if (components.stream().anyMatch(c -> c.ordinal() > 1)) writeBoxFile(packageDirectory, files.get("box.itr"));
	}

	private void refreshView(VirtualFile... files) {
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				projectView().refresh();
				Arrays.stream(files).forEach(f -> projectView().select(f, f, true));
			}
		}, 4000);
	}

	private ProjectView projectView() {
		return ProjectView.getInstance(module.getProject());
	}

	private void writeInfrastructureFiles(File srcDirectory, Map<String, String> files) {
		String artifactContent = files.remove("artifact.legio");
		legioFileCreator.create(artifactContent.replace("$groupId", groupId).replace("$namePackage", module.getName().replace("-", "").toLowerCase()).replace("$name", module.getName().toLowerCase()));
		files.forEach((k, v) -> {
			try {
				Files.write(new File(srcDirectory, k).toPath(), v.replace("$package", groupId + "." + module.getName().replace("-", "").toLowerCase()).getBytes(StandardCharsets.UTF_8));
			} catch (IOException e) {
				Logger.error(e);
			}
		});
	}

	private void addModernizationToModule() {
		try {
			File file = new File(moduleRoot(module), "modernization.goros");
			file.getParentFile().mkdirs();
			Files.writeString(file.toPath(), ("<modernization>\n" +
					"\t<platform></platform>\n" +
					"\t<model></model>\n" +
					"\t<project name=\"$project\" package=\"\" directory=\"\"/>\n" +
					"\t<module name=\"$module\"/>\n" +
					"\t<definitions excluded=\"\"/>\n" +
					"</modernization>").replace("$project", module.getProject().getName()).replace("$module", module.getName()));
			VirtualFile vFile = VfsUtil.findFileByIoFile(file, true);
			if (vFile != null) vFile.refresh(true, false);
		} catch (IOException ignored) {

		}
	}

	@NotNull
	private Map<String, String> download(IntinoModuleType.Type type) {
		final String realUrl = url.replace("$type", type.name().toLowerCase(Locale.ROOT)).replace("$version", "1.0.0");
		final ZipInputStream zipInputStream = new ZipInputStream(template(realUrl));
		return extract(zipInputStream);
	}

	private void writeBoxFile(File srcDirectory, String boxItr) {
		try {
			TemplateEngine engine = new TemplateEngine(new ItrRuleSetReader(new StringInputStream(boxItr)).read(Charset.defaultCharset()).ruleset(), new TemplateEngine.Configuration(Locale.ROOT, TemplateEngine.Configuration.LineSeparator.LF));
			FrameBuilder frameBuilder = new FrameBuilder("box");
			components.forEach(component -> frameBuilder.add(component.name(), new FrameBuilder(component.name()).toFrame()));
			String result = engine.render(frameBuilder.toFrame());
			File box = new File(srcDirectory, "box");
			box.mkdirs();
			File boxFile = new File(box, "Box." + KonosFileType.instance().getDefaultExtension());
			Files.writeString(boxFile.toPath(), result);
		} catch (IOException | ITRulesSyntaxError e) {
			Logger.error(e);
		}
	}

	private void writeModelFile(File srcDirectory) {
		File model = new File(srcDirectory, "model");
		model.mkdirs();
		File taraModel = new File(model, "Model.tara");
		try {
			Files.writeString(taraModel.toPath(), "dsl " + (components.contains(Components.Model) ? Proteo.class.getSimpleName() : Meta.class.getSimpleName()) + "\n\n\n");
		} catch (IOException e) {
			Logger.error(e);
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
		} catch (IOException ignored) {
		}
		return InputStream.nullInputStream();
	}
}
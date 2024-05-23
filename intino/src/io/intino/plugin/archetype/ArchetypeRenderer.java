package io.intino.plugin.archetype;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import io.intino.itrules.Engine;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.plugin.IntinoException;
import io.intino.plugin.PsiUtil;
import io.intino.plugin.archetype.lang.antlr.ArchetypeGrammar;
import io.intino.plugin.archetype.lang.antlr.ArchetypeParser;
import io.intino.plugin.project.configuration.ArtifactLegioConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaSourceRootType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

import static io.intino.plugin.lang.psi.impl.IntinoUtil.getSourceRoots;

public class ArchetypeRenderer {
	private static final Logger logger = Logger.getInstance(ArchetypeRenderer.class);

	private final String artifactId;
	private final Module module;
	private final ArtifactLegioConfiguration configuration;

	public ArchetypeRenderer(Module module, ArtifactLegioConfiguration configuration) {
		this.module = module;
		this.configuration = configuration;
		this.artifactId = configuration.artifact().name();
	}

	public void render(File archetypeFile) {
		ArchetypeGrammar.RootContext root;
		try {
			root = new ArchetypeParser(archetypeFile).parse();
		} catch (IntinoException e) {
			error(e);
			return;
		}
		FrameBuilder builder = new FrameBuilder("archetype");
		builder.add("package", configuration.artifact().code().generationPackage()).add("artifact", artifactId);
		builder.add("node", root.node().stream().map(this::frameOf).filter(Objects::nonNull).toArray(Frame[]::new));
		String output = Formatters.customize(new Engine(new ArchetypeTemplate())).render(builder.toFrame());
		writeFrame(directory(), Formatters.snakeCaseToCamelCase().format("Archetype").toString(), output);
		refreshDirectory(gen());
	}

	private @NotNull File directory() {
		return new File(gen(), configuration.artifact().code().generationPackage().replace(".", "/"));
	}

	private void refreshDirectory(File dir) {
		VirtualFile vDir = VfsUtil.findFileByIoFile(dir, true);
		if (vDir == null || !vDir.isValid()) return;
		VfsUtil.markDirtyAndRefresh(true, true, true, vDir);
		vDir.refresh(true, true);
	}

	private Frame frameOf(ArchetypeGrammar.NodeContext mogram) {
		String nodeName = mogram.declaration().IDENTIFIER().toString().replace(".", "_");
		FrameBuilder builder = new FrameBuilder("node").
				add("name", nodeName).
				add("artifact", artifactId);
		if (isLeaf(mogram)) builder.add("leaf");
		if (isSplitted(mogram)) {
			Frame[] splits = mogram.declaration().splitted().IDENTIFIER().stream().
					map(Object::toString).
					map(s -> new FrameBuilder("split").add("class", nodeName).add("value", s).toFrame()).toArray(Frame[]::new);
			builder.add("splitted").add("split", splits);
		}
		String parentIn = null;
		if (isModuleSplit(mogram)) {
			if (hasIn(mogram)) parentIn = mogram.declaration().LABEL(0).toString().replace("\"", "");
			mogram = findNodeModule(mogram);
			if (mogram == null) return null;
		}
		if (mogram.declaration().parameters() != null)
			builder.add("parameter", mogram.declaration().parameters().parameter().stream().
					map(p -> new FrameBuilder("parameter", type(p.type())).add("value", p.IDENTIFIER().toString()).toFrame()).
					toArray(Frame[]::new));
		if (hasIn(mogram))
			builder.add("filePath", (parentIn != null ? parentIn + "/" : "") + mogram.declaration().LABEL(0).toString().replace("\"", ""));
		else
			builder.add("filePath", (parentIn != null ? parentIn + "/" : "") + mogram.declaration().IDENTIFIER().toString());
		if (mogram.declaration().WITH() != null)
			builder.add("list").add(type(mogram.declaration().type())).
					add("with", mogram.declaration().LABEL(mogram.declaration().LABEL().size() - 1).toString());
		if (mogram.body() != null && !mogram.body().node().isEmpty())
			builder.add("node", mogram.body().node().stream().map(this::frameOf).toArray(Frame[]::new));
		return builder.toFrame();
	}

	private boolean isSplitted(ArchetypeGrammar.NodeContext node) {
		return node.declaration().splitted() != null;
	}

	private boolean hasIn(ArchetypeGrammar.NodeContext node) {
		return node.declaration().IN() != null;
	}

	private boolean isModuleSplit(ArchetypeGrammar.NodeContext node) {
		return node.declaration().starting().STAR() != null;
	}

	private boolean isLeaf(ArchetypeGrammar.NodeContext node) {
		return node.declaration().starting().MINUS() != null;
	}

	private ArchetypeGrammar.NodeContext findNodeModule(ArchetypeGrammar.NodeContext node) {
		return node.body().node().stream().filter(n -> n.declaration().IDENTIFIER().toString().equalsIgnoreCase(artifactId)).findFirst().orElse(null);
	}

	private String type(ArchetypeGrammar.TypeContext p) {
		if (p == null) return "default";
		if (p.REGEX() != null) return "regex";
		return "timetag";
	}

	private VirtualFile createGenDirectory(Module module) {
		final Application a = ApplicationManager.getApplication();
		if (!a.isWriteAccessAllowed()) {
			a.invokeAndWait(() -> a.runWriteAction((Computable<VirtualFile>) () -> create(module)));
			return getGenRoot(module);
		}
		return create(module);
	}

	private VirtualFile getGenRoot(Module module) {
		for (VirtualFile file : getSourceRoots(module))
			if (file.isDirectory() && "gen".equals(file.getName())) return file;
		final VirtualFile genDirectory = createGenDirectory(module);
		if (genDirectory == null) return null;
		PsiUtil.addSourceRoot(module, genDirectory, JavaSourceRootType.SOURCE);
		return genDirectory;
	}

	@Nullable
	private VirtualFile create(Module module) {
		final VirtualFile[] contentRoots = ModuleRootManager.getInstance(module).getContentRoots();
		try {
			return VfsUtil.createDirectoryIfMissing(contentRoots[0], "gen");
		} catch (IOException e) {
			Logger.getInstance(this.getClass()).error(e);
			File file = new File(contentRoots[0].getPath(), "gen");
			file.mkdirs();
			return VfsUtil.findFileByIoFile(file, true);
		}
	}

	private File gen() {
		for (VirtualFile file : getSourceRoots(module))
			if (file.isDirectory() && "gen".equals(file.getName())) return new File(file.getPath());
		final VirtualFile genDirectory = createGenDirectory(module);
		if (genDirectory == null) return null;
		PsiUtil.addSourceRoot(module, genDirectory, JavaSourceRootType.SOURCE);
		return new File(genDirectory.getPath());
	}

	private void error(Exception e) {
		Notifications.Bus.notify(new Notification("Intino", "Error parsing archetype.", e.getMessage() == null ? "Null" : e.getMessage(), NotificationType.ERROR));
	}

	private void writeFrame(File packageFolder, String name, String text) {
		try {
			packageFolder.mkdirs();
			File file = javaFile(packageFolder, name);
			Files.writeString(file.toPath(), text);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private File javaFile(File packageFolder, String name) {
		return preparedFile(packageFolder, name);
	}

	private File preparedFile(File packageFolder, String name) {
		return new File(packageFolder, prepareName(name) + "." + "java");
	}

	private String prepareName(String name) {
		return name.isEmpty() ? name : Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}

}

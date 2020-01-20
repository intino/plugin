package io.intino.plugin.toolwindows.project;

import com.intellij.ide.ui.LafManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleTypeWithWebFeatures;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.util.ui.UIUtil;
import io.intino.plugin.actions.ExportAction;
import io.intino.plugin.actions.IntinoGenerationAction;
import io.intino.plugin.actions.PurgeAndReloadConfigurationAction;
import io.intino.plugin.actions.ReloadConfigurationAction;
import io.intino.plugin.build.ArtifactBuilder;
import io.intino.plugin.build.FactoryPhase;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import io.intino.plugin.project.LegioConfiguration;
import io.intino.plugin.toolwindows.project.components.Element;
import io.intino.plugin.toolwindows.project.components.FactoryPanel;
import io.intino.plugin.toolwindows.project.components.Operation;
import io.intino.tara.compiler.shared.Configuration;
import io.intino.tara.lang.model.Node;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.time.Instant;
import java.util.Collections;

import static io.intino.plugin.DataContext.getContext;
import static io.intino.plugin.toolwindows.project.components.Element.*;
import static io.intino.plugin.toolwindows.project.components.Mode.Darcula;
import static io.intino.plugin.toolwindows.project.components.Mode.Light;
import static io.intino.plugin.toolwindows.project.components.Operation.*;
import static java.awt.event.ActionEvent.SHIFT_MASK;
import static java.time.temporal.ChronoUnit.SECONDS;

public class IntinoFactoryView extends JPanel {
	private JPanel contentPane;
	private JPanel factoryContainerPanel;
	private JLabel moduleLabel;
	private Project project;
	private Instant lastAction;

	IntinoFactoryView(Project project) {
		this.project = project;
		this.lastAction = Instant.now();
		this.contentPane.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				final Module module = selectedModule();
				if (module != null) moduleLabel.setText(module.getName());
			}
		});
		LafManager.getInstance().addLafManagerListener(source -> {
			mode(source.getCurrentLookAndFeel().getName().equalsIgnoreCase("darcula"));
			source.repaintUI();
		});
	}

	private void generateCode(int modifiers) {
		if (isRecurrent()) return;
		lastAction = Instant.now();
		final IntinoGenerationAction action = new IntinoGenerationAction();
		if ((modifiers & ActionEvent.SHIFT_MASK) != 0) action.force(selectedModule());
		else action.execute(selectedModule());
	}

	private void build() {
		if (isRecurrent()) return;
		lastAction = Instant.now();
		final CompilerManager compilerManager = CompilerManager.getInstance(project);
		CompileScope scope = compilerManager.createModulesCompileScope(new Module[]{selectedModule()}, true);
		compilerManager.make(scope, null);
	}

	private void build(Operation operation, int modifiers) {
		if (isRecurrent()) return;
		lastAction = Instant.now();
		FactoryPhase phase = phaseOf(operation, (modifiers & ActionEvent.SHIFT_MASK) != 0);
		if (phase == null) return;
		Module module = selectedModule();
		saveConfiguration(module);
		if (module != null) new ArtifactBuilder(project, Collections.singletonList(module), phase).build();
		else Notifications.Bus.notify(new Notification("Tara Language",
				phase.gerund() + " artifact", "Impossible identify module scope", NotificationType.ERROR));
	}

	private void saveConfiguration(Module module) {
		final FileDocumentManager manager = FileDocumentManager.getInstance();
		if (module == null || ModuleTypeWithWebFeatures.isAvailable(module)) return;
		manager.saveAllDocuments();
	}

	private void exportAccessors(int modifiers) {
		if (isRecurrent()) return;
		lastAction = Instant.now();
		boolean shift = (modifiers & SHIFT_MASK) != 0;
		new ExportAction().execute(selectedModule(), shift ? FactoryPhase.INSTALL : FactoryPhase.DISTRIBUTE);
	}

	private FactoryPhase phaseOf(Operation operation, boolean shift) {
		switch (operation) {
			case PackArtifact:
				return FactoryPhase.PACKAGE;
			case DistributeArtifact:
				return shift ? FactoryPhase.INSTALL : FactoryPhase.DISTRIBUTE;
			case DeployArtifact:
				return FactoryPhase.DEPLOY;
		}
		return null;
	}

	private boolean isRecurrent() {
		return Instant.now().minus(5, SECONDS).isBefore(lastAction);
	}

	private void reload(int modifiers) {
		if (isRecurrent()) return;
		lastAction = Instant.now();
		if (((modifiers & SHIFT_MASK) == SHIFT_MASK)) new PurgeAndReloadConfigurationAction().execute(selectedModule());
		else new ReloadConfigurationAction().execute(selectedModule());
	}

	private void createUIComponents() {
		createFactoryPanel();
	}

	private void createFactoryPanel() {
		factoryContainerPanel = new FactoryPanel(UIUtil.isUnderDarcula() ? Darcula : Light);
		((FactoryPanel) factoryContainerPanel).addActionListener(GenerateCode, e -> generateCode(e.getModifiers()));
		((FactoryPanel) factoryContainerPanel).addActionListener(ImportPackages, e -> reload(e.getModifiers()));
		((FactoryPanel) factoryContainerPanel).addActionListener(BuildArtifact, e -> build());
		((FactoryPanel) factoryContainerPanel).addActionListener(PackArtifact, e -> build(PackArtifact, e.getModifiers()));
		((FactoryPanel) factoryContainerPanel).addActionListener(ExportAccessors, e -> exportAccessors(e.getModifiers()));
		((FactoryPanel) factoryContainerPanel).addActionListener(DistributeArtifact, e -> build(DistributeArtifact, e.getModifiers()));
		((FactoryPanel) factoryContainerPanel).addActionListener(DeployArtifact, e -> build(DeployArtifact, e.getModifiers()));
		((FactoryPanel) factoryContainerPanel).addActionListener(Src, e -> navigate(Src, e.getModifiers()));
		((FactoryPanel) factoryContainerPanel).addActionListener(Gen, e -> navigate(Src, e.getModifiers()));
		((FactoryPanel) factoryContainerPanel).addActionListener(Imports, e -> navigate(Imports, e.getModifiers()));
		((FactoryPanel) factoryContainerPanel).addActionListener(Model, e -> navigate(Model, e.getModifiers()));
		((FactoryPanel) factoryContainerPanel).addActionListener(Box, e -> navigate(Box, e.getModifiers()));
		((FactoryPanel) factoryContainerPanel).addActionListener(Pack, e -> navigate(Pack, e.getModifiers()));
		((FactoryPanel) factoryContainerPanel).addActionListener(Dist, e -> navigate(Dist, e.getModifiers()));
		((FactoryPanel) factoryContainerPanel).addActionListener(Deploy, e -> navigate(Deploy, e.getModifiers()));
	}

	private void mode(boolean underDarcula) {
		((FactoryPanel) factoryContainerPanel).mode(underDarcula ? Darcula : Light);
	}

	private void navigate(Element element, int modifiers) {
		final Configuration configuration = TaraUtil.configurationOf(selectedModule());
		if (configuration == null) return;
		TaraModel model = ((TaraModel) ((LegioConfiguration) configuration).legioFile());
		final Node artifact = model.components().stream().filter(n -> n.type().endsWith("Artifact")).findAny().orElse(null);
		if (artifact == null) return;
		Node node;
		switch (element) {
			case Src:
				node = find(artifact, "Code");
				if (node != null) ((Navigatable) node).navigate(true);
				break;
			case Pack:
				node = find(artifact, "Package");
				if (node != null) ((Navigatable) node).navigate(true);
				break;
			case Dist:
				node = find(artifact, "Distribution");
				if (node != null) ((Navigatable) node).navigate(true);
				break;
			default:
				node = find(artifact, element.name());
				if (node != null) ((Navigatable) node).navigate(true);
				break;
		}
	}

	private Node find(Node artifact, String type) {
		return artifact.components().stream().filter(n -> n.type().endsWith(type)).findAny().orElse(null);
	}

	private Module selectedModule() {
		final DataContext resultSync = getContext();
		return resultSync != null ? resultSync.getData(LangDataKeys.MODULE) : null;
	}


	Component contentPane() {
		return contentPane;
	}

}
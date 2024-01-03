package io.intino.plugin.project.module;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;

import javax.swing.*;

public class IntinoWizardStep extends ModuleWizardStep {
	private final IntinoWizardPanel step;
	private final NewIntinoModuleBuilder builder;

	public IntinoWizardStep(NewIntinoModuleBuilder builder) {
		this.builder = builder;
		step = new IntinoWizardPanel();
	}

	@Override
	public JComponent getComponent() {
		return step.panel();
	}

	@Override
	public void updateDataModel() {
		builder.setIntinoModuleType(step.selected());
		builder.setStartingComponents(step.components());
	}
}
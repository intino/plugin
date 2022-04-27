package io.intino.plugin.project.module;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;

import javax.swing.*;

public class IntinoWizardStep extends ModuleWizardStep {
	private final IntinoWizardPanel step;
	private final IntinoModuleBuilder builder;

	public IntinoWizardStep(IntinoModuleBuilder builder) {
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
	}
}

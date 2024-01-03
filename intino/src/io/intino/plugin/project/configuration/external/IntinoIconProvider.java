package io.intino.plugin.project.configuration.external;

import com.intellij.openapi.externalSystem.ui.ExternalSystemIconProvider;
import icons.MavenIcons;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class IntinoIconProvider implements ExternalSystemIconProvider {
	@NotNull
	private final Icon reloadIcon;
	@NotNull
	private final Icon projectIcon;

	public IntinoIconProvider() {
		Icon icon = MavenIcons.MavenLoadChanges;
		Intrinsics.checkNotNullExpressionValue(icon, "MavenLoadChanges");
		this.reloadIcon = icon;
		icon = MavenIcons.MavenProject;
		Intrinsics.checkNotNullExpressionValue(icon, "MavenProject");
		this.projectIcon = icon;
	}

	@NotNull
	public Icon getReloadIcon() {
		return this.reloadIcon;
	}

	@NotNull
	public Icon getProjectIcon() {
		return this.projectIcon;
	}

}

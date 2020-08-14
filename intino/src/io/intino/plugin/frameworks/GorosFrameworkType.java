package io.intino.plugin.frameworks;

import com.intellij.framework.FrameworkTypeEx;
import com.intellij.framework.addSupport.FrameworkSupportInModuleProvider;
import io.intino.plugin.IntinoIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class GorosFrameworkType extends FrameworkTypeEx {

	public static final String ID = "goros";

	public GorosFrameworkType() {
		super(ID);
	}

	static GorosFrameworkType getFrameworkType() {
		return EP_NAME.findExtension(GorosFrameworkType.class);
	}

	@NotNull
	@Override
	public FrameworkSupportInModuleProvider createProvider() {
		return new GorosSupportProvider();
	}

	@NotNull
	@Override
	public String getPresentableName() {
		return "Goros";
	}

	@NotNull
	@Override
	public Icon getIcon() {
		return IntinoIcons.GOROS_13;
	}
}

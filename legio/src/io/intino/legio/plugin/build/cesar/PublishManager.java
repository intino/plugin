package io.intino.legio.plugin.build.cesar;

import com.intellij.openapi.module.Module;
import io.intino.legio.LifeCycle;
import io.intino.legio.plugin.build.LifeCyclePhase;
import io.intino.legio.plugin.project.LegioConfiguration;
import tara.intellij.lang.psi.impl.TaraUtil;

import java.util.List;

public class PublishManager {
	private final LifeCyclePhase phase;
	private final Module module;
	private final LegioConfiguration configuration;

	public PublishManager(LifeCyclePhase phase, Module module) {
		this.phase = phase;
		this.module = module;
		this.configuration = (LegioConfiguration) TaraUtil.configurationOf(module);
	}

	public void execute() {
		final LifeCycle.Publishing publishing = configuration.lifeCycle().publishing();
		final List<? extends LifeCycle.Publishing.Destiny> destinies = phase.equals(LifeCyclePhase.PREDEPLOY) ? publishing.preDeployList() : publishing.deployList();
		for (LifeCycle.Publishing.Destiny destiny : destinies) {
//			new CessarAccesor(publishing.cesarURL()).publish(destiny.publicURL(),)
		}

	}
}

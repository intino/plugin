package org.siani.legio.plugin.project;

import com.intellij.openapi.module.Module;
import org.siani.legio.Project;

import java.util.List;

class DependencyResolver {
	private final Module module;
	private final List<Project.Dependencies> dependencies;

	DependencyResolver(Module module, List<Project.Dependencies> dependencies) {
		this.module = module;
		this.dependencies = dependencies;
	}

	void resolve() {

	}
}

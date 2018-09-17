package io.intino.plugin.project;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import io.intino.cesar.box.CesarRestAccessor;
import io.intino.cesar.box.schemas.ProjectInfo;
import io.intino.konos.alexandria.exceptions.BadRequest;
import io.intino.konos.alexandria.exceptions.Unknown;
import io.intino.plugin.IntinoException;

import java.util.Map;

import static io.intino.plugin.deploy.ArtifactManager.urlOf;
import static io.intino.plugin.settings.IntinoSettings.getSafeInstance;

public class CesarAccessor {
	private static final Logger LOG = Logger.getInstance(CesarAccessor.class.getName());
	private final Project project;

	public CesarAccessor(Project project) {
		this.project = project;
	}

	public ProjectInfo projectInfo() {
		try {
			final CesarRestAccessor accessor = accessor();
			if (accessor == null) return null;
			return accessor.getProject(this.project.getName());
		} catch (BadRequest | Unknown e) {
			return null;
		}
	}


	CesarRestAccessor accessor() {
		try {
			final Map.Entry<String, String> cesar = getSafeInstance(this.project).cesar();
			return new CesarRestAccessor(urlOf(cesar.getKey()), cesar.getValue());
		} catch (IntinoException e) {
			return null;
		}
	}
}

package io.intino.plugin.project;

import com.intellij.openapi.project.Project;
import io.intino.alexandria.exceptions.BadRequest;
import io.intino.alexandria.exceptions.Unknown;
import io.intino.cesar.box.CesarRestAccessor;
import io.intino.cesar.box.schemas.ProcessInfo;
import io.intino.cesar.box.schemas.ProcessStatus;
import io.intino.cesar.box.schemas.ProjectInfo;
import io.intino.plugin.IntinoException;

import java.util.Map;

import static io.intino.plugin.deploy.ArtifactManager.urlOf;
import static io.intino.plugin.settings.IntinoSettings.getSafeInstance;

public class CesarAccessor {
	private final Project project;
	private CesarRestAccessor accessor;

	public CesarAccessor(Project project) {
		this.project = project;
		this.accessor = createAccessor();
	}

	public ProjectInfo projectInfo() {
		try {
			if (accessor == null) return null;
			return accessor.getProject(this.project.getName());
		} catch (BadRequest | Unknown e) {
			return null;
		}
	}

	public ProcessInfo processInfo(String id) {
		try {
			if (accessor == null) return null;
			return accessor.getProcess(this.project.getName(), id);
		} catch (BadRequest | Unknown e) {
			return null;
		}
	}

	public ProcessStatus processStatus(String project, String id) {
		try {
			if (accessor == null) return null;
			return accessor.getProcessStatus(this.project.getName(), id);
		} catch (BadRequest | Unknown e) {
			return null;
		}
	}

	public CesarRestAccessor accessor() {
		return accessor;
	}

	private CesarRestAccessor createAccessor() {
		try {
			final Map.Entry<String, String> credentials = getSafeInstance(this.project).cesar();
			return new CesarRestAccessor(urlOf(credentials.getKey().trim()), credentials.getValue());
		} catch (IntinoException e) {
			return null;
		}
	}

	public String talk(String text) {
		try {
			if (accessor == null) return null;
			return accessor.postBot(text);
		} catch (Unknown unknown) {
			return "Command not found";
		}
	}
}

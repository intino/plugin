package io.intino.plugin.actions.itrules;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.encoding.EncodingManager;
import io.intino.itrules.parser.ITRulesSyntaxError;
import io.intino.itrules.parser.ParsedTemplate;
import io.intino.itrules.readers.ItrRuleSetReader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class TemplateGenerator extends Task.Modal {

	public static final Logger LOG = Logger.getInstance("RunItrulesOnRulesFile");
	private final Module module;
	private final File destiny;
	private final String aPackage;
	private VirtualFile rulesFile;
	private ProgressIndicator indicator;

	public TemplateGenerator(VirtualFile rulesFile, Module module, String title, File destiny, String aPackage) {
		super(module.getProject(), title, true);
		this.rulesFile = rulesFile;
		this.module = module;
		this.destiny = destiny;
		this.aPackage = aPackage;
	}

	public void run(@NotNull ProgressIndicator indicator) {
		this.indicator = indicator;
		this.indicator.setIndeterminate(true);
		if (this.rulesFile == null) return;
		LOG.info("itrules(\"" + this.rulesFile.getPath() + "\")");
		task();
	}

	private void task() {
		try {
			toJava(rules());
			addFileToEncodings();
		} catch (Throwable e) {
			error(this.module.getProject(), e.getMessage());
			indicator.setText(e.getMessage());
			indicator.cancel();
		}
	}

	@NotNull
	private io.intino.itrules.parser.ParsedTemplate rules() throws IOException, ITRulesSyntaxError {
		return new ItrRuleSetReader(this.rulesFile.getInputStream()).read(rulesFile.getCharset());
	}

	private void toJava(ParsedTemplate template) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(this.destiny), StandardCharsets.UTF_8);
		String content = new TemplateRulesWriter(simpleFileName(), aPackage, locale(), lineSeparator()).toJava(template);
		writer.write(content);
		writer.close();
	}

	private void addFileToEncodings() {
		final VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(destiny);
		new Runnable() {
			public void run() {
				EncodingManager.getInstance().setEncoding(virtualFile, StandardCharsets.UTF_8);
			}
		};
	}

	private String locale() {
		return "Locale.ENGLISH";
	}

	private String lineSeparator() {
		return "LF";
	}

	@NotNull
	private String simpleFileName() {
		return rulesFile.getName().substring(0, rulesFile.getName().lastIndexOf("."));
	}

	private void error(Project project, String message) {
		Notifications.Bus.notify(new Notification("Itrules", "Error generating template", message, NotificationType.ERROR), project);
	}
}


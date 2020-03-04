package io.intino.plugin.codeinsight.languageinjection.imports;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.impl.text.PsiAwareTextEditorImpl;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.util.messages.MessageBusConnection;
import io.intino.plugin.lang.file.TaraFileType;
import io.intino.plugin.lang.psi.Valued;
import io.intino.plugin.lang.psi.impl.TaraPsiUtil;
import io.intino.plugin.project.module.ModuleProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction;
import static io.intino.plugin.codeinsight.languageinjection.helpers.QualifiedNameFormatter.qnOf;
import static io.intino.plugin.lang.psi.impl.IntinoUtil.importsFile;

public class ImportsSaverService implements ProjectComponent {

	private final Project project;
	private MessageBusConnection connection;

	protected ImportsSaverService(Project project, FileEditorManager fileEditorManager) {
		this.project = project;
	}

	private final FileEditorManagerListener myListener = new FileEditorManagerListener() {
		@Override
		public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
		}

		@Override
		public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile sourceFile) {
			if (!sourceFile.isValid() || !project.isInitialized()) return;
			final PsiFile file = PsiManager.getInstance(project).findFile(sourceFile);
			if (!isJavaNativeScratch(file)) return;
			Imports imports = new Imports(project);
			String moduleName = getModuleName(source);
			final Valued valued = findValued(source);
			String qn = qnOf(valued);
			if (moduleName == null || qn.isEmpty()) return;
			runWriteCommandAction(project, () -> imports.save(importsFile(valued), qn, getImports(file)));
		}

		@Override
		public void selectionChanged(@NotNull FileEditorManagerEvent event) {
		}
	};

	private Valued findValued(FileEditorManager source) {
		if (source.getSelectedFiles().length == 0) return null;
		final PsiFile taraFile = findFile(source.getSelectedFiles());
		if (taraFile == null) return null;
		final FileEditor editor = source.getSelectedEditor(taraFile.getVirtualFile());
		if (editor == null) return null;
		final PsiElement elementAt = taraFile.findElementAt(((PsiAwareTextEditorImpl) editor).getEditor().getCaretModel().getOffset());
		if (elementAt == null) return null;
		return TaraPsiUtil.getContainerByType(elementAt, Valued.class);
	}

	private PsiFile findFile(VirtualFile[] file) {
		for (VirtualFile vFile : file)
			if (TaraFileType.instance().getDefaultExtension().equals(vFile.getExtension()))
				return PsiManager.getInstance(project).findFile(vFile);
		return null;
	}

	private String getModuleName(FileEditorManager source) {
		if (source.getSelectedFiles().length == 0) return null;
		final PsiFile taraFile = PsiManager.getInstance(project).findFile(source.getSelectedFiles()[0]);
		source.getSelectedEditor(source.getSelectedFiles()[0]);
		if (taraFile == null) return null;
		final Module module = ModuleProvider.moduleOf(taraFile);
		return module == null ? "" : module.getName();
	}

	private boolean isJavaNativeScratch(PsiFile file) {
		return file != null && file.getName().startsWith("Java Fragment");
	}

	private Set<String> getImports(PsiFile file) {
		if (file == null) return Collections.emptySet();
		final PsiImportList importList = ((PsiJavaFile) file).getImportList();
		if (importList == null) return Collections.emptySet();
		return Arrays.asList(importList.getAllImportStatements()).stream().map((i) -> i.getText().trim()).collect(Collectors.toSet());
	}

	@Override
	public void projectOpened() {
		StartupManager.getInstance(project).runWhenProjectIsInitialized(this::initListener);
	}

	private void initListener() {
		connection = project.getMessageBus().connect(project);
		connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, myListener);
	}

	@Override
	public void projectClosed() {
		disposeComponent();
	}

	@Override
	public void initComponent() {

	}

	@Override
	public void disposeComponent() {
	}

	@NotNull
	@Override
	public String getComponentName() {
		return "Native Imports Saver";
	}


}

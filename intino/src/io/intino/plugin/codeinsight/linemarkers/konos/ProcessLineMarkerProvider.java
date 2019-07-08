package io.intino.plugin.codeinsight.linemarkers.konos;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzerSettings;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.impl.JavaLineMarkerProvider;
import com.intellij.codeInsight.daemon.impl.LineMarkerNavigator;
import com.intellij.codeInsight.daemon.impl.MarkerType;
import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import io.intino.plugin.file.konos.KonosFileType;
import io.intino.tara.lang.model.Node;
import io.intino.tara.plugin.lang.psi.impl.TaraPsiImplUtil;
import io.intino.tara.plugin.lang.psi.impl.TaraUtil;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.List;

public class ProcessLineMarkerProvider extends JavaLineMarkerProvider {
	private static final Logger logger = Logger.getInstance(ProcessLineMarkerProvider.class);
	private final MarkerType markerType = new MarkerType("ProcessEditor", element -> {
		if (!isProcessElement(element)) return null;
		InputStream stream = this.getClass().getResourceAsStream("/process_modeler.html");
		if (stream == null) return null;
		return "Edit process...";
	}, new LineMarkerNavigator() {
		@Override
		public void browse(MouseEvent e, PsiElement element) {
			InputStream stream = this.getClass().getResourceAsStream("/process_modeler.html");
			if (stream == null) return;
			if (DumbService.isDumb(element.getProject())) {
				DumbService.getInstance(element.getProject()).showDumbModeNotification("Navigation to process editor is not possible during index update");
				return;
			}
			try {
				Node node = node(element);
				VirtualFile resourcesRoot = TaraUtil.getResourcesRoot(element);
				File file = new File(resourcesRoot.getPath(), node.name() + ".bpmn");
				Path modelerFile = Files.createTempFile("process_modeler", ".html");
				String modelerText = IOUtils.toString(stream, "UTF-8").replace("$file", file.getAbsolutePath());
				Files.write(modelerFile, modelerText.getBytes(), StandardOpenOption.CREATE);
				BrowserUtil.browse(modelerFile.toFile());
			} catch (IOException ex) {
				logger.error(ex);
			}
		}
	});

	@SuppressWarnings("NonDefaultConstructor")
	public ProcessLineMarkerProvider(DaemonCodeAnalyzerSettings daemonSettings, EditorColorsManager colorsManager) {
		super(daemonSettings, colorsManager);
	}

	@Override
	public LineMarkerInfo getLineMarkerInfo(@NotNull final PsiElement element) {
		if (!(element instanceof Node)) return super.getLineMarkerInfo(element);
		if (isProcessElement(element)) {
			final Icon icon = AllIcons.General.Inline_edit;
			final MarkerType type = markerType;
			return new LineMarkerInfo(leafOf(element), element.getTextRange(), icon, Pass.UPDATE_ALL, type.getTooltip(),
					type.getNavigationHandler(), GutterIconRenderer.Alignment.LEFT);
		} else return super.getLineMarkerInfo(element);
	}


	private PsiElement leafOf(@NotNull PsiElement element) {
		PsiElement leaf = element;
		while (leaf.getFirstChild() != null) leaf = leaf.getFirstChild();
		return leaf;
	}

	public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
	}

	@NotNull
	@Override
	public String getName() {
		return "Edit process";
	}

	@Nullable
	@Override
	public Icon getIcon() {
		return AllIcons.General.Inline_edit;
	}


	private boolean isProcessElement(PsiElement e) {
		Node node = node(e);
		return e.getContainingFile().getFileType().equals(KonosFileType.instance()) && node != null && "Process".equals(node.type());
	}

	@Nullable
	private Node node(PsiElement e) {
		return e instanceof Node ? (Node) e : TaraPsiImplUtil.getContainerNodeOf(e);
	}
}
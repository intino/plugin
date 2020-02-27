package io.intino.plugin.annotator.fix;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.impl.file.PsiDirectoryImpl;
import com.intellij.util.IncorrectOperationException;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.plugin.codeinsight.languageinjection.helpers.Format;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.TaraNode;
import io.intino.plugin.lang.psi.impl.TaraUtil;
import org.jetbrains.annotations.NotNull;

import static io.intino.plugin.codeinsight.languageinjection.helpers.Format.firstUpperCase;
import static io.intino.plugin.codeinsight.languageinjection.helpers.Format.javaValidName;

public class SyncDecorableClassIntention extends ClassCreationIntention {
	private final TaraNode node;
	private final String graphPackage;

	public SyncDecorableClassIntention(TaraNode node, String graphPackage) {
		this.node = node;
		this.graphPackage = graphPackage;
	}

	@NotNull
	@Override
	public String getText() {
		return "Sync decorable class " + Format.javaValidName().format(node.name());
	}

	@NotNull
	@Override
	public String getFamilyName() {
		return "Sync decorable class";
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
		return element.getContainingFile() instanceof TaraModel;
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
		final PsiFile file = element.getContainingFile();
		VirtualFile srcDirectory = getSrcDirectory(TaraUtil.getSourceRoots(file));
		PsiDirectoryImpl srcPsiDirectory = new PsiDirectoryImpl((PsiManagerImpl) file.getManager(), srcDirectory);
		PsiClass aClass = createRuleClass(file, srcPsiDirectory);
		if (aClass != null) aClass.navigate(true);
	}

	private PsiClass createRuleClass(PsiFile file, PsiDirectoryImpl srcPsiDirectory) {
		PsiClass aClass;
		PsiDirectory destiny = findDestination(file, srcPsiDirectory, graphPackage);
		aClass = createClass(destiny, format(node.name()));
		return aClass;
	}

	private PsiClass createClass(PsiDirectory destiny, String className) {
		PsiFile file = destiny.findFile(className + ".java");
		if (file != null) return null;
		PsiFile element = PsiFileFactory.getInstance(node.getProject()).createFileFromText(className, JavaFileType.INSTANCE, content());
		return (PsiClass) destiny.add(element);
	}

	private String content() {
		return new DecorableTemplate().render(createFrame(this.node));
	}

	private Frame createFrame(TaraNode node) {
		FrameBuilder builder = new FrameBuilder("node").add("package", validName(graphPackage)).add("name", validName(node.name()));
		if (node.isAbstract()) builder.add("abstract", "abstract");
		builder.add("node", node.components().stream().map(c -> createFrame(node)).toArray(Frame[]::new));
		return builder.toFrame();
	}

	private String format(String name) {
		return firstUpperCase().format(validName(name)).toString();
	}

	private String validName(String name) {
		return javaValidName().format(name).toString();
	}


	@Override
	public boolean startInWriteAction() {
		return true;
	}
}

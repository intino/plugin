package io.intino.plugin.annotator.fix;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.impl.file.PsiDirectoryImpl;
import com.intellij.util.IncorrectOperationException;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.magritte.lang.model.NodeRoot;
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
		PsiDirectoryImpl srcPsiDirectory = new PsiDirectoryImpl((PsiManagerImpl) file.getManager(), getSrcDirectory(TaraUtil.getSourceRoots(file)));
		PsiDirectoryImpl genPsiDirectory = new PsiDirectoryImpl((PsiManagerImpl) file.getManager(), getGenDirectory(TaraUtil.getSourceRoots(file)));
		PsiClass aClass = createRuleClass(file, srcPsiDirectory, genPsiDirectory);
		if (aClass != null) aClass.navigate(true);
	}

	private PsiClass createRuleClass(PsiFile file, PsiDirectoryImpl srcPsiDirectory, PsiDirectoryImpl genPsiDirectory) {
		PsiClass aClass;
		PsiDirectory srcDestination = findDestination(file, srcPsiDirectory, graphPackage);
		PsiDirectory genDestination = findDestination(file, genPsiDirectory, graphPackage);
		aClass = createClass(srcDestination, genDestination, format(node.name()));
		return aClass;
	}

	private PsiClass createClass(PsiDirectory srcDestination, PsiDirectory genDestination, String className) {
		PsiFile file = srcDestination.findFile(className + ".java");
		if (file != null) return null;
		PsiFile srcClass = createSrcClass(className);
		PsiFile genClass = createGenClass(className);
		srcClass = (PsiFile) srcDestination.add(srcClass);
		genDestination.add(genClass);
		return ((PsiJavaFile) srcClass).getClasses()[0];
	}

	private PsiFile createGenClass(String className) {
		return PsiFileFactory.getInstance(node.getProject()).createFileFromText("Abstract" + className + ".java", JavaFileType.INSTANCE, genContent());
	}

	@NotNull
	private PsiFile createSrcClass(String className) {
		return PsiFileFactory.getInstance(node.getProject()).createFileFromText(className + ".java", JavaFileType.INSTANCE, srcContent());
	}

	private String srcContent() {
		return new DecorableTemplate().render(createFrame(this.node, false));
	}

	private String genContent() {
		return new DecorableTemplate().render(createFrame(this.node, true));
	}

	private Frame createFrame(TaraNode node, boolean gen) {
		return new FrameBuilder("decorable").add("package", validName(graphPackage)).add(gen ? "nodeGen" : "node", createNodeFrame(node, gen)).toFrame();
	}

	private Frame createNodeFrame(TaraNode node, boolean gen) {
		FrameBuilder builder = new FrameBuilder(gen ? "nodeGen" : "node").add("name", validName(node.name()));
		if (!(node.container() instanceof NodeRoot)) builder.add("inner", "static");
		if (node.isAbstract()) builder.add("abstract", "abstract");
		builder.add(gen ? "nodeGen" : "node", node.components().stream().map(c -> createNodeFrame((TaraNode) c, gen)).toArray(Frame[]::new));
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

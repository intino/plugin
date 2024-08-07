package io.intino.plugin.codeinsight.annotators.fix;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.impl.file.PsiDirectoryImpl;
import com.intellij.util.IncorrectOperationException;
import io.intino.itrules.Frame;
import io.intino.itrules.FrameBuilder;
import io.intino.plugin.codeinsight.languageinjection.helpers.Format;
import io.intino.plugin.lang.psi.TaraModel;
import io.intino.plugin.lang.psi.TaraMogram;
import io.intino.plugin.lang.psi.impl.IntinoUtil;
import io.intino.tara.language.model.Mogram;
import io.intino.tara.language.model.MogramRoot;
import org.jetbrains.annotations.NotNull;

import static io.intino.plugin.codeinsight.languageinjection.helpers.Format.firstUpperCase;
import static io.intino.plugin.codeinsight.languageinjection.helpers.Format.javaValidName;

public class SyncDecorableClassIntention extends ClassCreationIntention {
	private final TaraMogram node;
	private final String graphPackage;

	public SyncDecorableClassIntention(TaraMogram node, String graphPackage) {
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
		PsiDirectoryImpl srcPsiDirectory = new PsiDirectoryImpl((PsiManagerImpl) file.getManager(), getSrcDirectory(IntinoUtil.getSourceRoots(file)));
		PsiDirectoryImpl genPsiDirectory = new PsiDirectoryImpl((PsiManagerImpl) file.getManager(), getGenDirectory(IntinoUtil.getSourceRoots(file)));
		PsiClass aClass = createDecorableClass(file, srcPsiDirectory, genPsiDirectory);
		if (aClass != null) aClass.navigate(true);
	}

	private PsiClass createDecorableClass(PsiFile file, PsiDirectoryImpl srcPsiDirectory, PsiDirectoryImpl genPsiDirectory) {
		PsiClass aClass;
		PsiDirectory srcDestination = findDestination(file, srcPsiDirectory, graphPackage);
		PsiDirectory genDestination = findDestination(file, genPsiDirectory, graphPackage);
		aClass = syncClass(srcDestination, genDestination, format(node.name()));
		return aClass;
	}

	private PsiClass syncClass(PsiDirectory srcDestination, PsiDirectory genDestination, String className) {
		PsiFile srcFile = srcDestination.findFile(className + ".java");
		boolean existSrc = srcFile != null;
		PsiFile genFile = genDestination.findFile("Abstract" + className + ".java");
		boolean existGen = genFile != null;
		if (!existSrc) srcFile = (PsiFile) srcDestination.add(createSrcClass(className));
		if (!existGen) genFile = (PsiFile) genDestination.add(createGenClass(className));
		if (existSrc) return syncSubClasses(srcFile, genFile);
		return ((PsiJavaFile) srcFile).getClasses()[0];
	}

	private PsiClass syncSubClasses(PsiFile srcFile, PsiFile genFile) {
		PsiClass srcClass = ((PsiJavaFile) srcFile).getClasses()[0];
		PsiClass genClass = ((PsiJavaFile) genFile).getClasses()[0];
		syncSubClasses(srcClass, genClass, this.node);
		return srcClass;
	}

	private void syncSubClasses(PsiClass srcClass, PsiClass genClass, TaraMogram node) {
		for (Mogram component : node.components()) {
			if (component.isReference()) continue;
			PsiClass innerGenClass = genClass.findInnerClassByName("Abstract" + validName(component.name()), false);
			PsiClass innerSrcClass = srcClass.findInnerClassByName(validName(component.name()), false);
			if (innerGenClass == null) createTree(genClass, component, true);
			if (innerSrcClass == null) createTree(srcClass, component, false);
			else syncSubClasses(innerSrcClass, innerGenClass, (TaraMogram) component);
		}
	}

	private void createTree(PsiClass context, Mogram component, boolean isGen) {
		final JavaPsiFacade facade = JavaPsiFacade.getInstance(context.getProject());
		final PsiClass psiClass = facade.getElementFactory().createClassFromText(buildClass((TaraMogram) component, isGen), context).getInnerClasses()[0];
		context.add(psiClass);
	}

	private String buildClass(TaraMogram component, boolean isGen) {
		return new DecorableTemplate().render(createNodeFrame(component, isGen));
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

	private Frame createFrame(TaraMogram node, boolean gen) {
		return new FrameBuilder("decorable").add("package", validName(graphPackage)).add(gen ? "nodeGen" : "node", createNodeFrame(node, gen)).toFrame();
	}

	private Frame createNodeFrame(TaraMogram node, boolean gen) {
		FrameBuilder builder = new FrameBuilder(gen ? "nodeGen" : "node").add("name", validName(node.name()));
		if (!(node.container() instanceof MogramRoot)) builder.add("inner", "static");
		if (node.isAbstract()) builder.add("abstract", "abstract");
		builder.add(gen ? "nodeGen" : "node", node.components().stream().filter(c -> !c.isReference()).map(c -> createNodeFrame((TaraMogram) c, gen)).toArray(Frame[]::new));
		return builder.toFrame();
	}

	private String format(String name) {
		return firstUpperCase().format(validName(name)).toString();
	}

	private String validName(String name) {
		return javaValidName().format(name).toString();
	}
}
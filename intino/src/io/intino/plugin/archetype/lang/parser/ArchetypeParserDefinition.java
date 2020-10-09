package io.intino.plugin.archetype.lang.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.ParserDefinition;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import io.intino.plugin.archetype.lang.antlr.ArchetypeLanguage;
import io.intino.plugin.archetype.lang.lexer.ArchetypeLexerAdapter;
import io.intino.plugin.archetype.lang.psi.ArchetypeTemplateImpl;
import io.intino.plugin.archetype.lang.psi.ArchetypeTypes;
import org.jetbrains.annotations.NotNull;

public class ArchetypeParserDefinition implements ParserDefinition {
	public static final TokenSet WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE);
	public static final TokenSet COMMENTS = TokenSet.create();
	public static final IFileElementType FILE = new IFileElementType(Language.findInstance(ArchetypeLanguage.class));

	@NotNull
	@Override
	public com.intellij.lexer.Lexer createLexer(Project project) {
		return new io.intino.plugin.archetype.lang.lexer.ArchetypeLexerAdapter();
	}

	@NotNull
	public TokenSet getWhitespaceTokens() {
		return WHITE_SPACES;
	}

	@NotNull
	public TokenSet getCommentTokens() {
		return COMMENTS;
	}

	@NotNull
	public TokenSet getStringLiteralElements() {
		return TokenSet.EMPTY;
	}

	@NotNull
	public com.intellij.lang.PsiParser createParser(final Project project) {
		return new ArchetypeParser();
	}

	@Override
	public IFileElementType getFileNodeType() {
		return FILE;
	}

	public PsiFile createFile(FileViewProvider viewProvider) {
		return new ArchetypeTemplateImpl(viewProvider);
	}

	@NotNull
	public com.intellij.psi.PsiElement createElement(ASTNode node) {
		return ArchetypeTypes.Factory.createElement(node);
	}
}
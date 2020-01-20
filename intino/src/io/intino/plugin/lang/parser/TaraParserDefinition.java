package io.intino.plugin.lang.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.ParserDefinition;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import io.intino.plugin.lang.TaraLanguage;
import io.intino.plugin.lang.lexer.TaraLexerAdapter;
import io.intino.plugin.lang.psi.TaraTypes;
import io.intino.plugin.lang.psi.impl.TaraModelImpl;
import org.jetbrains.annotations.NotNull;

public class TaraParserDefinition implements ParserDefinition {
	private static final TokenSet WHITE_SPACES = TokenSet.create(TokenType.WHITE_SPACE);
	private static final TokenSet COMMENTS = TokenSet.create(TaraTypes.COMMENT);
	private static final TokenSet STRING_LITERAL = TokenSet.create(TaraTypes.STRING_VALUE);

	private static final IFileElementType FILE = new IFileElementType(Language.findInstance(TaraLanguage.class));

	@NotNull
	@Override
	public com.intellij.lexer.Lexer createLexer(Project project) {
		return new TaraLexerAdapter();
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
		return STRING_LITERAL;
	}

	@NotNull
	public com.intellij.lang.PsiParser createParser(final Project project) {
		return new TaraParser();
	}

	@Override
	public IFileElementType getFileNodeType() {
		return FILE;
	}

	public PsiFile createFile(FileViewProvider viewProvider) {
		return new TaraModelImpl(viewProvider);
	}

	public SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
		return SpaceRequirements.MAY;
	}

	@NotNull
	public com.intellij.psi.PsiElement createElement(ASTNode node) {
		return TaraTypes.Factory.createElement(node);
	}
}
package io.intino.plugin.codeinsight.linemarkers;

import com.intellij.ide.browsers.OpenInBrowserRequest;
import com.intellij.ide.browsers.WebBrowserUrlProvider;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Url;
import io.intino.plugin.lang.TaraLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class WebBrowserProvider extends WebBrowserUrlProvider {

	@Override
	public boolean canHandleElement(@NotNull OpenInBrowserRequest request) {
		return request.getFile().getLanguage().equals(TaraLanguage.INSTANCE);
	}

	@Nullable
	@Override
	protected Url getUrl(@NotNull OpenInBrowserRequest request, @NotNull VirtualFile file) throws BrowserException {
		return super.getUrl(request, file);
	}

	@NotNull
	@Override
	public Collection<Url> getUrls(@NotNull OpenInBrowserRequest request) throws BrowserException {
		return super.getUrls(request);
	}
}

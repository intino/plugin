package io.intino.plugin;

import com.intellij.ide.DataManager;
import org.jetbrains.concurrency.AsyncPromise;

public class DataContext {

	public static com.intellij.openapi.actionSystem.DataContext getContext() {
		AsyncPromise<com.intellij.openapi.actionSystem.DataContext> result = new AsyncPromise<>();
		DataManager.getInstance().getDataContextFromFocusAsync()
				.onSuccess(result::setResult)
				.onError(it -> result.setError(it.getMessage()));
		return result.get();
	}
}

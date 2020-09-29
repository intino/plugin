package io.intino.plugin.stash;

import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

final class StashEditorState implements FileEditorState {
	private final transient long myDocumentModificationStamp; // should not be serialized
	private final String[] mySelectedComponentIds;

	public StashEditorState(final long modificationStamp, @NotNull final String[] selectedComponents) {
		myDocumentModificationStamp = modificationStamp;
		mySelectedComponentIds = selectedComponents;
	}

	public String[] getSelectedComponentIds() {
		return mySelectedComponentIds;
	}

	public boolean equals(final Object o) {
		if (this == o) return true;
		if (!(o instanceof StashEditorState)) return false;

		final StashEditorState state = (StashEditorState) o;

		return myDocumentModificationStamp == state.myDocumentModificationStamp &&
				Arrays.equals(mySelectedComponentIds, state.mySelectedComponentIds);

	}

	public int hashCode() {
		return (int) (myDocumentModificationStamp ^ (myDocumentModificationStamp >>> 32));
	}

	@Override
	public boolean canBeMergedWith(FileEditorState otherState, FileEditorStateLevel level) {
		return otherState instanceof StashEditorState;
	}
}

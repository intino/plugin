package io.intino.plugin.project;

import com.intellij.openapi.roots.DependencyScope;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.libraries.Library;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class LibraryConflictResolver {

	public static boolean mustAdd(Library languageLibrary, List<Library> newLibraries) {
		Library library = libraryOf(newLibraries, languageLibrary.getName());
		return library == null || isNewer(languageLibrary, library);
	}

	public static boolean shouldAddEntry(Library languageLibrary, List<LibraryOrderEntry> newLibraries) {
		LibraryOrderEntry library = libraryEntryOf(newLibraries, languageLibrary.getName());
		return library == null || isNewer(languageLibrary, library.getLibrary());
	}

	private static boolean isNewer(Library aLibrary, Library library) {
		return compare(aLibrary, library) < 0;
	}

	private static int compare(Library aLibrary, Library library) {
		return versionOf(library.getName()).compareTo(versionOf(aLibrary.getName()));
	}

	public static Library libraryOf(List<Library> libraries, String name) {
		for (Library newLibrary : libraries)
			if (newLibrary.getName() != null && libraryName(newLibrary.getName()).equalsIgnoreCase(libraryName(name)))
				return newLibrary;
		return null;
	}

	public static LibraryOrderEntry libraryEntryOf(List<LibraryOrderEntry> libraries, String name) {
		List<LibraryOrderEntry> candidates = libraries.stream().filter(entry -> entry.getLibrary() != null && entry.getLibrary().getName() != null && libraryName(entry.getLibrary().getName()).equalsIgnoreCase(libraryName(name))).collect(Collectors.toList());
		if (candidates.isEmpty()) return null;
		candidates.sort((o1, o2) -> compare(o1.getLibrary(), o2.getLibrary()));
		return candidates.get(0);
	}


	@SuppressWarnings("ConstantConditions")
	public static List<LibraryOrderEntry> shouldReplace(Library library, List<LibraryOrderEntry> collection) {
		if (library == null) return Collections.emptyList();
		return collection.stream().
				filter(entry -> entry != null && entry.getLibrary() != null).
				filter(entry -> libraryName(entry.getLibrary().getName()).equals(libraryName(library.getName())) && isNewer(library, entry.getLibrary())).
				collect(Collectors.toList());
	}

	@NotNull
	private static String libraryName(String library) {
		return library.substring(0, library.lastIndexOf(":"));
	}

	private static Version versionOf(String library) {
		final String version = library.substring(library.lastIndexOf(":") + 1);
		final String toRemove = version.trim().replaceFirst("([0-9]+(\\.[0-9]+)*)", "");
		return new Version(version.trim().replace(toRemove, ""));
	}

	public static class Version implements Comparable<Version> {

		private String version;

		public final String get() {
			return this.version;
		}

		public Version(String version) {
			if (version == null)
				throw new IllegalArgumentException("Version can not be null");
			if (!version.matches("[0-9]+(\\.[0-9]+)*"))
				throw new IllegalArgumentException("Invalid version format: " + version);
			this.version = version;
		}

		@Override
		public int compareTo(Version that) {
			if (that == null)
				return 1;
			String[] thisParts = this.get().split("\\.");
			String[] thatParts = that.get().split("\\.");
			int length = Math.max(thisParts.length, thatParts.length);
			for (int i = 0; i < length; i++) {
				int thisPart = i < thisParts.length ?
						Integer.parseInt(thisParts[i]) : 0;
				int thatPart = i < thatParts.length ?
						Integer.parseInt(thatParts[i]) : 0;
				if (thisPart < thatPart)
					return -1;
				if (thisPart > thatPart)
					return 1;
			}
			return 0;
		}

		@Override
		public boolean equals(Object that) {
			if (this == that)
				return true;
			if (that == null)
				return false;
			if (this.getClass() != that.getClass())
				return false;
			return this.compareTo((Version) that) == 0;
		}

	}

}

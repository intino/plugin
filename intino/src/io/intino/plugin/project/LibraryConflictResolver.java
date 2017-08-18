package io.intino.plugin.project;

import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.libraries.Library;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LibraryConflictResolver {

	public static boolean mustAdd(Library languageLibrary, List<Library> newLibraries) {
		Library library = libraryOf(newLibraries, languageLibrary.getName());
		return library == null || versionOf(library.getName()).compareTo(versionOf(languageLibrary.getName())) < 0;
	}

	public static boolean mustAddEntry(Library languageLibrary, List<LibraryOrderEntry> newLibraries) {
		LibraryOrderEntry library = libraryEntryOf(newLibraries, languageLibrary.getName());
		return library == null || versionOf(library.getLibrary().getName()).compareTo(versionOf(languageLibrary.getName())) < 0;
	}

	public static Library libraryOf(List<Library> libraries, String name) {
		for (Library newLibrary : libraries)
			if (newLibrary.getName() != null && libraryName(newLibrary.getName()).equalsIgnoreCase(libraryName(name)))
				return newLibrary;
		return null;
	}

	public static LibraryOrderEntry libraryEntryOf(List<LibraryOrderEntry> libraries, String name) {
		for (LibraryOrderEntry newLibrary : libraries)
			if (newLibrary.getLibrary() != null && newLibrary.getLibrary().getName() != null && libraryName(newLibrary.getLibrary().getName()).equalsIgnoreCase(libraryName(name)))
				return newLibrary;
		return null;
	}


	@NotNull
	private static String libraryName(String library) {
		return library.substring(0, library.lastIndexOf(":"));
	}

	private static Version versionOf(String library) {
		return new Version(library.substring(library.lastIndexOf(":") + 1).trim().replaceFirst("\\.v.*", ""));
	}

	private static class Version implements Comparable<Version> {

		private String version;

		public final String get() {
			return this.version;
		}

		public Version(String version) {
			if (version == null)
				throw new IllegalArgumentException("Version can not be null");
			if (!version.matches("[0-9]+(\\.[0-9]+)*"))
				throw new IllegalArgumentException("Invalid version format");
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

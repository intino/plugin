package io.intino.plugin.project;

import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.libraries.Library;
import io.intino.plugin.IntinoException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
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
		final Version version = versionOf(library.getName());
		if (version == null) return 1;
		final Version that = versionOf(aLibrary.getName());
		if (that == null) return -1;
		return version.compareTo(that);
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
		int endIndex = library.lastIndexOf(":");
		return endIndex >= 0 ? library.substring(0, endIndex) : library;
	}

	private static Version versionOf(String library) {
		final String version = library.substring(library.lastIndexOf(":") + 1);
		final String toRemove = version.trim().replaceFirst("([0-9]+(\\.[0-9]+)*)", "");
		try {
			return new Version(version.trim().replace(toRemove, ""));
		} catch (IntinoException e) {
			try {
				return new Version(version.trim());
			} catch (IntinoException e1) {
				return null;
			}
		}
	}

	public static class Version implements Comparable<Version> {

		private String version;

		public Version(String version) throws IntinoException {
			if (version == null)
				throw new IntinoException("Version can not be null");
			if (!version.matches("[0-9]+(\\.[0-9]+)*"))
				throw new IntinoException("Invalid version format: " + version);
			this.version = version;
		}

		public final String get() {
			return this.version;
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

	public static class VersionRange {
		public static boolean isInRange(String value, List<RangeValue> range) {
			int leftRelation = getRelationOrder(value, range.get(0), true);
			if (leftRelation == 0) {
				return true;
			} else if (leftRelation < 0) {
				return false;
			} else {
				return getRelationOrder(value, range.get(1), false) <= 0;
			}
		}

		private static int getRelationOrder(String value, RangeValue rangeValue, boolean isLeft) {
			if (rangeValue.value.length() <= 0) {
				return isLeft ? 1 : -1;
			} else {
				value = value.replaceAll("[^0-9\\.\\-\\_]", "");
				List<String> valueTokens = new ArrayList(Arrays.asList(value.split("[\\.\\-\\_]")));
				List<String> rangeValueTokens = new ArrayList(Arrays.asList(rangeValue.value.split("\\.")));
				addZeroTokens(valueTokens, 3);
				addZeroTokens(rangeValueTokens, 3);
				for (int i = 0; i < 3; ++i) {
					int x = Integer.parseInt(valueTokens.get(i));
					int y = Integer.parseInt(rangeValueTokens.get(i));
					if (x < y) return -1;
					if (x > y) return 1;
				}
				if (!rangeValue.closed) return isLeft ? -1 : 1;
				else return 0;
			}
		}

		private static void addZeroTokens(List<String> tokens, int max) {
			while (tokens.size() < max) tokens.add("0");
		}

		public static boolean isRange(String value) {
			return value.startsWith("[") || value.startsWith("(");
		}

		public static List<RangeValue> rangeValuesOf(String version) {
			String[] values = version.split(",");
			if (values.length == 0) return Collections.emptyList();
			return Arrays.asList(new RangeValue(values[0].trim().substring(1), values[0].trim().charAt(0) == '['), new RangeValue(values[1].trim().substring(0, values[1].length() - 1), values[1].trim().charAt(values[1].trim().length() - 1) == ']'));
		}

		public static class RangeValue {
			private String value;
			private boolean closed;

			public RangeValue(String value, boolean closed) {
				this.value = value.trim();
				this.closed = closed;
			}

			public String toString() {
				return this.value;
			}
		}
	}
}

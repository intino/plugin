package io.intino.plugin.project.configuration;

import io.intino.plugin.IntinoException;

public class Version implements Comparable<Version> {
	public enum Level {Minor, Medium, Mayor}

	private static final String SNAPSHOT = "-SNAPSHOT";
	private String version;

	public Version(String version) throws IntinoException {
		if (version == null)
			throw new IntinoException("Version can not be null");
		if (!version.matches("[0-9]+(\\.[0-9]+)*" + "(-SNAPSHOT)?"))
			throw new IntinoException("Invalid version format: " + version);
		this.version = version;
	}

	public final String get() {
		return this.version;
	}

	public Level distanceTo(Version that) {
		String[] thisParts = this.get().split("\\.");
		String[] thatParts = that.get().split("\\.");
		if (!thisParts[0].equals(thatParts[0])) return Level.Mayor;
		if (!thisParts[1].equals(thatParts[1])) return Level.Medium;
		return Level.Minor;
	}

	@Override
	public String toString() {
		return version;
	}

	public boolean isSnapshot() {
		return version.endsWith(SNAPSHOT);
	}

	public Version nextSnapshot() throws IntinoException {
		String[] split = version.split("\\.");
		split[split.length - 1] = String.valueOf(Integer.parseInt(split[split.length - 1]) + 1);
		return new Version(String.join(".", split) + SNAPSHOT);
	}

	public Version nextRelease(Level level) throws IntinoException {
		String[] split = version.split("\\.");
		split[split.length - level.ordinal()] = String.valueOf(Integer.parseInt(split[split.length - level.ordinal()]) + 1);
		return new Version(String.join(".", split));
	}

	public Version next() throws IntinoException {
		String[] split = version.split("\\.");
		split[split.length - 1] = String.valueOf(Integer.parseInt(split[split.length - 1]) + 1);
		return new Version(String.join(".", split));
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

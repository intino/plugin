package io.intino.plugin.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LibraryConflictResolver {

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
				try {
					for (int i = 0; i < 3; ++i) {
						int x = Integer.parseInt(valueTokens.get(i));
						int y = Integer.parseInt(rangeValueTokens.get(i));
						if (x < y) return -1;
						if (x > y) return 1;
					}
					if (!rangeValue.closed) return isLeft ? -1 : 1;
					else return 0;
				} catch (NumberFormatException e) {
					return 0;
				}
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

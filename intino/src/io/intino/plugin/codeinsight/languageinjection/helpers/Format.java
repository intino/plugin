package io.intino.plugin.codeinsight.languageinjection.helpers;


import io.intino.itrules.Formatter;

public class Format {

	protected static final String DOT = ".";

	private Format() {
	}

	public static Formatter string() {
		return new StringFormatter();
	}

	public static Formatter qualifiedName() {
		return value -> {
			String val = value.toString();
			if (!val.contains(DOT)) return referenceFormat(val).replace(":", "");
			else {
				final String[] split = val.split("\\.");
				String result = "";
				for (String name : split) result += "." + referenceFormat(name);
				return result.substring(1).replace(":", "");
			}
		};
	}

	public static Formatter reference() {
		return s -> {
			String value = s.toString();
			if (value.isEmpty()) return "";
			if (!value.contains(DOT))
				return (value.substring(0, 1).toUpperCase() + value.substring(1)).replace("-", "");
			return value.replace("-", "");
		};
	}

	private static String referenceFormat(String val) {
		return (val.substring(0, 1).toUpperCase() + val.substring(1)).replace("-", "");
	}


	public static Formatter toCamelCase() {
		return s -> {
			final String value = s.toString();
			return toCamelCase(value, "_");
		};
	}

	public static Formatter snakeCasetoCamelCase() {
		return s -> {
			final String value = s.toString();
			return toCamelCase(value, "-");
		};
	}


	public static Formatter firstUpperCase() {
		return s -> {
			final String value = s.toString();
			if (value.isEmpty()) return "";
			return value.substring(0, 1).toUpperCase() + value.substring(1);
		};
	}


	public static Formatter withDollar() {
		return s -> {
			final String value = s.toString();
			return value.replace(".", "$");
		};
	}

	public static Formatter noPackage() {
		return s -> {
			final String value = s.toString();
			final String[] split = value.split("\\.");
			String result = "";
			for (String word : split) {
				if (word.toLowerCase().equals(word)) continue;
				result += "." + word;
			}
			return result.isEmpty() ? result : result.substring(1);
		};
	}

	public static Formatter javaQn() {
		return s -> {
			final String value = s.toString();
			return toCamelCase(value, "-");
		};
	}


	public static Formatter javaValidName() {
		return s -> {
			final String value = s.toString();
			return toCamelCase(value, "-");
		};
	}

	private static String toCamelCase(String value, String regex) {
		if (value.isEmpty()) return "";
		String[] parts = value.split(regex);
		if (parts.length == 1)
			return value.substring(0, 1).toUpperCase() + value.substring(1);
		String caseString = "";
		for (String part : parts)
			caseString = caseString + toProperCase(part);
		return caseString;
	}

	public static Formatter nativeParameter() {
		return parametersWithType -> {
			String result = "";
			for (String parameter : parametersWithType.toString().split(",")) {
				String[] split = parameter.trim().split(" ");
				result += ", " + split[split.length - 1];
			}
			return result.isEmpty() ? result : result.substring(2);
		};
	}

	static String toProperCase(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
	}

	public static Formatter key() {
		return value -> {
			try {
				Long.parseLong(String.valueOf(value));
				return "$(" + value.toString() + ")";
			} catch (NumberFormatException e) {
				return value;
			}
		};
	}


	private static class StringFormatter implements Formatter {
		@Override
		public Object format(Object value) {
			String val = value.toString().trim();
			if (val.isEmpty()) return val;
			if (val.startsWith("\n") || val.startsWith("---"))
				return transformMultiLineString((String) value);
			return val;
		}

		private String transformMultiLineString(String value) {
			String val = value.replace("\r", "");
			int i = value.indexOf('-');
			String indent = value.substring(0, i).replace("\t", "    ");
			val = val.replace(indent, "\n").trim();
			if (val.startsWith("---")) {
				val = val.replaceAll("----+", "").trim();
			}
			return val.replaceAll("\n\n+", "\n").replace("\n", "\" +\n\"").replace("\"\"", "");

		}
	}
}

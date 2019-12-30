package io.intino.plugin.actions.archetype;


import io.intino.itrules.Formatter;
import io.intino.itrules.Template;

public class Formatters {

	public static Formatter validName() {
		return (value) -> snakeCaseToCamelCase(value.toString().replace(".", "-"));
	}

	public static Formatter snakeCaseToCamelCase() {
		return value -> snakeCaseToCamelCase(value.toString());
	}

	public static Formatter camelCaseToSnakeCase() {
		return value -> camelCaseToSnakeCase(value.toString());
	}

	public static Formatter returnType() {
		return value -> value.equals("Void") ? "void" : value;
	}

	public static String firstLowerCase(String value) {
		return value.substring(0, 1).toLowerCase() + value.substring(1);
	}

	public static String firstUpperCase(String value) {
		return value.substring(0, 1).toUpperCase() + value.substring(1);
	}

	public static Formatter returnTypeFormatter() {
		return value -> {
			if (value.equals("Void")) return "void";
			else if (value.toString().contains(".")) return firstLowerCase(value.toString());
			else return value;
		};
	}

	public static Formatter quoted() {
		return value -> '"' + value.toString() + '"';
	}

	public static Formatter validPackage() {
		return value -> value.toString().replace("-", "").toLowerCase();
	}

	private static Formatter subPath() {
		return value -> {
			final String path = value.toString();
			return path.contains(":") ? path.substring(0, path.indexOf(":")) : path;
		};
	}

	public static Formatter shortType() {
		return value -> {
			String type = value.toString();
			final String[] s = type.split("\\.");
			return s[s.length - 1];
		};
	}

	public static String camelCaseToSnakeCase(String string) {
		if (string.isEmpty()) {
			return string;
		} else {
			String result = String.valueOf(Character.toLowerCase(string.charAt(0)));

			for (int i = 1; i < string.length(); ++i) {
				result = result + (Character.isUpperCase(string.charAt(i)) ? "-" + Character.toLowerCase(string.charAt(i)) : string.charAt(i));
			}

			return result;
		}
	}

	public static String snakeCaseToCamelCase(String string) {
		if (string.isEmpty()) {
			return string;
		} else {
			String result = "";
			String[] var2 = string.replace("_", "-").split("-");
			int var3 = var2.length;

			for (int var4 = 0; var4 < var3; ++var4) {
				String part = var2[var4];
				result = result + String.valueOf(Character.toUpperCase(part.charAt(0))) + part.substring(1);
			}

			return result;
		}
	}


	public static Template customize(Template template) {
		template.add("validname", validName());
		template.add("snakeCaseToCamelCase", snakeCaseToCamelCase());
		template.add("camelCaseToSnakeCase", camelCaseToSnakeCase());
		template.add("returnType", returnType());
		template.add("returnTypeFormatter", returnTypeFormatter());
		template.add("quoted", quoted());
		template.add("validPackage", validPackage());
		template.add("subpath", subPath());
		template.add("shortType", shortType());
		template.add("quoted", quoted());
		template.add("customParameter", customParameter());
		return template;
	}

	private static Formatter customParameter() {
		return value -> value.toString().substring(1, value.toString().length() - 1);
	}

}

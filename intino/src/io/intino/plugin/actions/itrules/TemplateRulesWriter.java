package io.intino.plugin.actions.itrules;

import io.intino.itrules.*;
import io.intino.itrules.Rule.Condition;
import io.intino.itrules.TemplateEngine.Configuration;
import io.intino.itrules.parser.ParsedTemplate;
import io.intino.itrules.rules.conditions.AttributeCondition;
import io.intino.itrules.rules.conditions.NegatedCondition;
import io.intino.itrules.rules.conditions.TriggerCondition;
import io.intino.itrules.rules.conditions.TypeCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import static io.intino.itrules.TemplateEngine.Configuration.LineSeparator;

public class TemplateRulesWriter {

	private final String name;
	private final String aPackage;
	private final String locale;
	private final String lineSeparator;
	private ParsedTemplate template;

	public TemplateRulesWriter(String name, String aPackage, String locale, String lineSeparator) {
		this.name = name;
		this.aPackage = aPackage;
		this.locale = locale;
		this.lineSeparator = lineSeparator;
	}

	@NotNull
	public String toJava(final ParsedTemplate template) {
		this.template = template;
		return new JavaItrulesTemplate(new Configuration(Locale.getDefault(), lineSeparator.equals("LF") ? LineSeparator.LF : LineSeparator.CRLF))
				.add("string", buildStringFormatter())
				.add(RuleSet.class, ruleSetAdapter())
				.add(Condition.class, conditionAdapter()).render(template.ruleset());
	}

	@NotNull
	private Adapter<RuleSet> ruleSetAdapter() {
		return (source, context) -> {
			context.add("name", name);
			context.add("locale", locale);
			context.add("lineSeparator", lineSeparator);
			if (!aPackage.isEmpty()) context.add("package", aPackage);
			source.forEach(r -> context.add("rule", r));
			template.formatters().forEach((key, value) -> context.add("formatter", new FrameBuilder("formatter").add("name", key).add("value", value).toFrame()));
		};
	}

	private Adapter<Condition> conditionAdapter() {
		return (condition, context) -> {
			if (condition instanceof NegatedCondition) context.add("negated", "not");
			context.add("parameter", parameter(condition));
			context.add("name", name(condition));
			addOperator(condition, context);
		};
	}

	private String name(Condition condition) {
		if (condition instanceof NegatedCondition) condition = ((NegatedCondition) condition).condition();
		String name = condition.getClass().getSimpleName().replace("Condition", "").toLowerCase();
		return condition instanceof TypeCondition && ((TypeCondition) condition).types().size() > 1 ? "Types" : name;
	}

	private void addOperator(Condition condition, FrameBuilderContext context) {
		if ((condition instanceof NegatedCondition)) condition = ((NegatedCondition) condition).condition();
		if (condition instanceof TypeCondition && ((TypeCondition) condition).types().size() > 1)
			context.add("operator", (((TypeCondition) condition).operator().name().toLowerCase()));
	}

	private Frame parameter(Condition condition) {
		if ((condition instanceof NegatedCondition)) condition = ((NegatedCondition) condition).condition();
		if (condition instanceof AttributeCondition) {
			Object value = ((AttributeCondition) condition).value();
			FrameBuilder builder = new FrameBuilder("attribute").add("attribute", ((AttributeCondition) condition).attribute());
			if (value != null) builder.add("value", value.toString());
			return builder.toFrame();
		}
		if (condition instanceof TriggerCondition)
			return new FrameBuilder("trigger").add("value", ((TriggerCondition) condition).name()).toFrame();
		return new FrameBuilder("type").add("value", ((TypeCondition) condition).types().toArray(new Object[0])).toFrame();
	}

	@NotNull
	private Formatter buildStringFormatter() {
		return object -> {
			String value = object.toString();
			if (value.contains("\r")) value = value.replace("\r", "\\r");
			value = value.replace("\n", "\\n");
			value = value.replace("\t", "\\t").replace("\"", "\\\"");
			if (value.equals("\\")) value = value.replace("\\", "\\\\");
			return '"' + value + '"';
		};
	}
}
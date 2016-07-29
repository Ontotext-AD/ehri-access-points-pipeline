import java.util.regex.Pattern

public class GenerateAdditionalLabels {
	private static final Pattern STUFF_TO_REMOVE = Pattern.compile("^(bezirk|kreis|kz) |( ((concentration|extermination) camp)|(ska(ya)?)? oblast)\$")

	public static Set<String> workFlow(String funk, String input, String clazz) {
		Set<String> result = new HashSet<String>()
		result.add(input.replace("ae", "a").replace("oe", "o").replace("ue", "u"))
		result.add(STUFF_TO_REMOVE.matcher(input).replaceAll(""))
		return result
	}
}

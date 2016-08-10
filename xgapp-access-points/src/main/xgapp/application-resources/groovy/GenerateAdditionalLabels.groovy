import java.text.Normalizer;
import java.util.regex.Pattern;

public class GenerateAdditionalLabels {
	private static final boolean DEBUG = false;

	private static final char L_POLISH = 322;
    private static final char[] A_UMLAUT = [ 97, 776 ];
    private static final char[] O_UMLAUT = [ 111, 776 ];
    private static final char[] U_UMLAUT = [ 117, 776 ];
    private static final String A_UMLAUT_STRING = String.valueOf(A_UMLAUT);
    private static final String O_UMLAUT_STRING = String.valueOf(O_UMLAUT);
	private static final String U_UMLAUT_STRING = String.valueOf(U_UMLAUT);
	private static final Pattern COMBINING_CHARACTER = Pattern.compile("['’`\\p{InSpacingModifierLetters}\\p{InCombiningDiacriticalMarks}\\p{InCombiningDiacriticalMarksSupplement}\\p{InCombiningMarksforSymbols}\\p{InCombiningHalfMarks}]");
	private static final Pattern PUNCTUATION_CHARACTER = Pattern.compile("\\p{IsPunct}");
	private static final Pattern SPACE_SEQUENCE = Pattern.compile("\\s+");

	private static final Pattern STUFF_TO_REMOVE = Pattern.compile("^(bezirk|kreis|kz) |( ((concentration|extermination) camp)|(ska(ya)?)? oblast)\$");

	public static Set<String> workFlow(String funk, String input, String clazz) {
		String fingerprint = extractFingerprint(input);
		Set<String> result = new HashSet<String>();
		result.add(fingerprint);
		result.add(fingerprint.replace("ae", "a").replace("oe", "o").replace("ue", "u"));
		result.add(STUFF_TO_REMOVE.matcher(fingerprint).replaceAll(""));
		if (DEBUG) System.out.println("GenerateAdditionalLabels: input = \"" + input + "\", output = " + result.toString());
		return result;
	}

	private static String extractFingerprint(String text) {
        text = text.toLowerCase();
        text = Normalizer.normalize(text, Normalizer.Form.NFKD);
        text = text.replace((char) L_POLISH, (char) 'l').replace(A_UMLAUT_STRING, "ae").replace(O_UMLAUT_STRING, "oe").replace(U_UMLAUT_STRING, "ue");
        text = COMBINING_CHARACTER.matcher(text).replaceAll("");
        text = PUNCTUATION_CHARACTER.matcher(text).replaceAll(" ");
        text = SPACE_SEQUENCE.matcher(text).replaceAll(" ");
        text = text.trim();
        return text;
	}
}

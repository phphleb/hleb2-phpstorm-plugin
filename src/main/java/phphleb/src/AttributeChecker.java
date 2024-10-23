package phphleb.src;

import org.jetbrains.annotations.Nullable;

/**
 * Проверка значения аргумента, так как там может быть переменная
 * или что-то еще кроме значения.
 */
public class AttributeChecker {

    /**
     * Проверяет строку из аргумента как строковое значение.
     */
    public static boolean checkOption(String value) {
        if (value == null || value.length() < 3) {
            return false;
        }
        String trimValue = value.trim();
        if (trimValue.length() < 3) {
            return false;
        }
        String innerValue = trimValue.substring(1, trimValue.length() - 1);

        if (innerValue.matches(".*[$'\"<>].*")) {
            return false;
        }

        char firstChar = trimValue.charAt(0);
        return firstChar == '\'' || firstChar == '\"';
    }

    /**
     * Проверяет строку из аргумента как значение, а не как переменную или функцию.
     */
    public static boolean checkValue(String value) {
        if (checkOption(value)) {
            return true;
        }
        if (value == null || value.isEmpty()) {
            return false;
        }
        if (isNumeric(value)) {
            return true;
        }
        String lower = value.toLowerCase();
        return lower.equals("null") || lower.equals("true") || lower.equals("false");
    }

    /**
     * Первоначальная проверка строки на значение без кавычек в контенте (не по краям).
     * Также проверяется, что в строке нет переменных.
     */
    public static boolean checkString(@Nullable String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        input = input.replaceAll("^[\"']+|[\"']+$", "");

        return !input.matches(".*[\"'$<>{}].*");
    }

    /**
     * Проверяет, является ли строка числовым значением.
     */
    private static boolean isNumeric(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}


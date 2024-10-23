package phphleb.src;

import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.JBColor;

import java.awt.*;

/**
 * Стили относящиеся к документации фреймворка.
 */
public class TextStyles {

    public static final JBColor CUSTOM_COLOR = new JBColor(0x01B1C2, 0x01BED0);

    public static final TextAttributes SPECIAL_TEXT_STYLE = new TextAttributes(
            CUSTOM_COLOR, // Цвет текста
            null, // Цвет фона (null - по умолчанию)
            null, // Цвет эффекта
            null, // Тип эффекта
            Font.BOLD // Стиль шрифта
    );

    public static final TextAttributes LINE_UNDERSCORE_STYLE = new TextAttributes(
            null, // Цвет текста (null - по умолчанию)
            null, // Цвет фона
            JBColor.YELLOW, // Цвет эффекта
            EffectType.BOLD_LINE_UNDERSCORE, // Тип эффекта
            Font.PLAIN // Стиль шрифта
    );

    public static final TextAttributes LINE_UNDERSCORE_STYLE_v2 = new TextAttributes(
            null, // Цвет текста (null - по умолчанию)
            null, // Цвет фона
            JBColor.ORANGE, // Цвет эффекта
            EffectType.BOLD_LINE_UNDERSCORE, // Тип эффекта
            Font.PLAIN // Стиль шрифта
    );

    public static final TextAttributes WARNING_TEXT_STYLE = new TextAttributes(
            JBColor.RED, // Цвет текста
            null, // Цвет фона (null - по умолчанию)
            null, // Цвет эффекта
            null, // Тип эффекта
            Font.PLAIN // Стиль шрифта
    );

    public static final TextAttributes SPECIAL_TEXT_STYLE_V2 = new TextAttributes(
            CUSTOM_COLOR, // Цвет текста
            null, // Цвет фона (null - по умолчанию)
            null, // Цвет эффекта
            null, // Тип эффекта
            Font.ITALIC // Стиль шрифта
    );

    public static final TextAttributes BRIGHT_TEXT_STYLE = new TextAttributes(
            JBColor.ORANGE, // Цвет текста
            null, // Цвет фона (null - по умолчанию)
            null, // Цвет эффекта
            null, // Тип эффекта
            Font.PLAIN // Стиль шрифта
    );

    public static final TextAttributes DEFAULT_TEXT_STYLE = new TextAttributes(
            null, // Цвет текста (null - по умолчанию)
            null, // Цвет фона
            null, // Цвет эффекта
            null, // Тип эффекта
            Font.PLAIN // Стиль шрифта
    );
}

package com.beetlecore.engine.infrastructure;

import com.beetlecore.engine.domain.BissMessage;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class BissLocalizationEngine {

    private static final String BUNDLE_BASE = "i18n.messages";

    /**
     * Resuelve y formatea un mensaje del sistema basado estrictamente en el locale del contrato BISS.
     */
    public static String getLocalizedMessage(BissMessage message, String key, Object... args) {
        String localeStr = (message.context() != null && message.context().systemLocale() != null)
                ? message.context().systemLocale() 
                : "en_US";
        
        Locale locale = Locale.forLanguageTag(localeStr.replace("_", "-"));
        
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_BASE, locale);
            String pattern = bundle.getString(key);
            return MessageFormat.format(pattern, args);
        } catch (Exception e) {
            // Fallback defensivo a la llave si el bundle o la propiedad no existen
            return key;
        }
    }
}

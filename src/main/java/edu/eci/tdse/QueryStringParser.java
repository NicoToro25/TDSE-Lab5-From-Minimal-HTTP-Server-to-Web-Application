package edu.eci.tdse;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class QueryStringParser {
    
    public static Map<String, String> parse (String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return params;
        }

        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq == -1) continue; // Parámetro sin valor, lo ignoramos

            try {
                String key = URLDecoder.decode(pair.substring(0, eq), "UTF-8");
                String value = URLDecoder.decode(pair.substring(eq + 1), "UTF-8");
                params.put(key, value);
            } catch (UnsupportedEncodingException e) {
                // UTF-8 siempre está disponible en la JVM; no deberia pasar nunca.
            }
        }
        return params;
    }
}

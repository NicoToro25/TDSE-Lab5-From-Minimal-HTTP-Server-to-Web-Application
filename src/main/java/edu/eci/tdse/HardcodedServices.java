package edu.eci.tdse;

import java.time.Instant;
import java.util.Map;

public class HardcodedServices {

	/**
	 * Devuelve el JSON de respuesta, o null si la ruta no es un servicio conocido.
	 */
	public static String handle(String path, Map<String, String> queryParams, int[] statusOut) {

		if (path.equals("/greeting")) {
			return greeting(queryParams, statusOut);
		}
		if (path.equals("/square")) {
			return square(queryParams, statusOut);
		}
		if (path.equals("/time")) {
			return time(statusOut);
		}
		if (path.equals("/health")) {
			return health(statusOut);
		}

		return null; // no es un servicio -> el servidor sigue buscando en archivos estáticos
	}

	private static String greeting(Map<String, String> params, int[] statusOut) {
		String name = params.get("name");
		if (name == null || name.isBlank()) {
			statusOut[0] = 400;
			return "{\"error\": \"Missing or empty 'name' parameter\"}";
		}

		statusOut[0] = 200;
		return "{\"message\": \"Hello, " + JsonUtil.escape(name) + "!\"}";

	}

    private static String square(Map<String, String> params, int[] statusOut) {
        String rawValue = params.get("value");
        if (rawValue == null || rawValue.isBlank()) {
            statusOut[0] = 400;
            return "{\"error\": \"Missing 'value' parameter\"}";
        }

        double value;
        try {
            value = Double.parseDouble(rawValue);
        } catch (NumberFormatException e) {
            statusOut[0] = 400;
            return "{\"error\": \"'value' must be a valid number\"}";
        }

        statusOut[0] = 200;
        double squared = value * value;
        return "{\"input\": " + value + ", \"square\": " + squared + "}";
    }

    private static String time (int[] statusOut) {
        statusOut[0] = 200;
        return "{\"serverTime\": \"" + Instant.now().toString() + "\"}";
    }

    private static String health(int[] statusOut) {
        statusOut[0] = 200;
        return "{\"status\": \"UP\"}";
    }
}

package edu.eci.tdse;

import java.util.Map;

public class ContentTypeResolver {

    private static final Map<String, String> TYPES = Map.of(
        "html", "text/html",
        "js", "application/javascript; charset=UTF-8",
        "png", "image/png",
        "jpg", "image/jpeg",
        "jpeg", "image/jpeg"
    );

    public static String resolve(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            return "application/octet-stream"; // Tipo genérico para archivos sin extensión
        }
        String extension = fileName.substring(dotIndex + 1).toLowerCase();
        return TYPES.getOrDefault(extension, "application/octet-stream");
    }
}

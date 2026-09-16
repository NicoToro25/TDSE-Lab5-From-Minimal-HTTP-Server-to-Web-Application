package edu.eci.tdse;

import java.io.IOException;
import java.nio.file.*;

public class ResourceResolver {

    private final Path publicRoot;

    public ResourceResolver(Path publicRoot) throws IOException {
        // Normalizamos la raiz de una vez, para tener un punto de comparación fijo y evitar problemas de seguridad (path traversal).
        this.publicRoot = publicRoot.toRealPath();
    }

    /**
     * Devuelve el Path real del recurso solicitado, o null si:
     * - el archivo no existe, o
     * - el path intenta salir de publicRoot (path traversal)
     */
    public Path resolve(String requestedPath) {

        String relative = requestedPath.equals("/") ? "index.html"
                                                      : requestedPath.substring(1);

        Path candidate = publicRoot.resolve(relative);

        try {
            Path realCandidate = candidate.toRealPath(); // aquí se "aplanan" los ..

            if (!realCandidate.startsWith(publicRoot)) {
                // Se salió del cuarto permitido.
                return null;
            }
            return realCandidate;

        } catch (IOException e) {
            // toRealPath() falla si el archivo no existe -> 404, no 500.
            return null;
        }
    }
    
}

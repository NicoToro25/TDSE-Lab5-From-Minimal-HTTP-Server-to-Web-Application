package edu.eci.tdse;

import java.io.*;
import java.net.*;
import java.nio.file.*;

public class MinimalHttpServer {

	private static ResourceResolver resourceResolver;

	public static void main(String[] args) throws IOException {
		int port = 35000;

		// Ubicacion de los recursos públicos dentro del classpath compilado
		Path publicRoot = Paths.get("src/main/resources/public");
		resourceResolver = new ResourceResolver(publicRoot);

		ServerSocket serverSocket = new ServerSocket(port);
		System.out.println("Servidor HTTP escuchando en el puerto " + port + "..." );

		while (true) {
			Socket clientSocket = serverSocket.accept();
			System.out.println("Cliente conectado: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
			try {
				handleClient(clientSocket);
			} catch (IOException e) {
				System.err.println("Error al manejar la conexión con el cliente: " + e.getMessage());
			}
		}
	}

    private static void handleClient(Socket clientSocket) throws IOException {
        clientSocket.setSoTimeout(5000);

        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
            OutputStream rawOut = clientSocket.getOutputStream()
        ) {
            String requestLine;
            try {
                requestLine = in.readLine();
            } catch (SocketTimeoutException e) {
                return;
            }

            if (requestLine == null || requestLine.isEmpty()) {
                return;
            }
            System.out.println("Request line: " + requestLine);

            // Consumir headers (aún no los usamos, pero hay que leerlos)
            String headerLine;
            while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
                // no-op por ahora
            }

            // "GET /images/logo.png HTTP/1.1" -> partes[0]=GET, partes[1]=/images/logo.png
            String[] parts = requestLine.split(" ");
            if (parts.length < 2) {
                sendError(rawOut, 400, "Bad Request");
                return;
            }
            String method = parts[0];
            String path = parts[1];

            if (!method.equals("GET")) {
                sendError(rawOut, 405, "Method Not Allowed");
                return;
            }

            // Quitar query string si la hay (la usaremos en la sección 4)
            int queryIndex = path.indexOf('?');
            String cleanPath = (queryIndex == -1) ? path : path.substring(0, queryIndex);

            Path resource = resourceResolver.resolve(cleanPath);
            if (resource == null) {
                sendError(rawOut, 404, "Not Found");
                return;
            }

            byte[] bodyBytes = Files.readAllBytes(resource);
            String contentType = ContentTypeResolver.resolve(resource.getFileName().toString());

            sendResponse(rawOut, 200, "OK", contentType, bodyBytes);

        } finally {
            clientSocket.close();
        }
    }

    private static void sendResponse(OutputStream out, int status, String statusText,
                                      String contentType, byte[] body) throws IOException {
        String headers = "HTTP/1.1 " + status + " " + statusText + "\r\n"
            + "Content-Type: " + contentType + "\r\n"
            + "Content-Length: " + body.length + "\r\n"
            + "Connection: close\r\n"
            + "\r\n";
        out.write(headers.getBytes("UTF-8"));
        out.write(body);
        out.flush();
    }

    private static void sendError(OutputStream out, int status, String statusText) throws IOException {
        byte[] body = ("<html><body><h1>" + status + " " + statusText + "</h1></body></html>")
            .getBytes("UTF-8");
        sendResponse(out, status, statusText, "text/html; charset=UTF-8", body);
    }
}
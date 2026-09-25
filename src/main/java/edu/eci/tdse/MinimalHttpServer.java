package edu.eci.tdse;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Map;

public class MinimalHttpServer {

	private static ResourceResolver resourceResolver;

	public static void main(String[] args) throws IOException {
		int port = resolvePort(args);
		Path publicRoot = resolvePublicRoot();

		resourceResolver = new ResourceResolver(publicRoot);

		ServerSocket serverSocket = new ServerSocket(port);
		System.out.println("Servidor escuchando en el puerto " + port + "...");
		System.out.println("Sirviendo recursos desde: " + publicRoot.toAbsolutePath());

		while (true) {
			Socket clientSocket = serverSocket.accept();
			try {
				handleClient(clientSocket);
			} catch (IOException e) {
				System.err.println("Error manejando el cliente: " + e.getMessage());
			}
		}
	}

	private static Path resolvePublicRoot() {
		// En desarrollo (mvn exec:java desde la raíz del proyecto), los recursos
		// están en src/main/resources/public.
		Path devPath = Paths.get("src/main/resources/public");
		if (Files.exists(devPath)) {
			return devPath;
		}

		// En EC2 (jar empaquetado), la carpeta "public" vive junto al jar,
		// en el directorio de trabajo actual.
		Path deployedPath = Paths.get("public");
		if (Files.exists(deployedPath)) {
			return deployedPath;
		}

		throw new IllegalStateException(
				"No se encontró la carpeta de recursos públicos ni en '" + devPath + "' ni en '" + deployedPath + "'");
	}

	private static int resolvePort(String[] args) {
		// Prioridad: argumento de línea de comandos > variable de entorno > default
		if (args.length > 0) {
			try {
				return Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				System.err.println("Puerto inválido en argumento, usando default.");
			}
		}

		String envPort = System.getenv("SERVER_PORT");
		if (envPort != null) {
			try {
				return Integer.parseInt(envPort);
			} catch (NumberFormatException e) {
				System.err.println("Puerto inválido en variable de entorno, usando default.");
			}
		}

		return 35000; // default
	}

	private static void handleClient(Socket clientSocket) throws IOException {
		clientSocket.setSoTimeout(5000);

		try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				OutputStream rawOut = clientSocket.getOutputStream()) {
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

			// Consumir headers (aún no los usamos más allá de loguearlos)
			String headerLine;
			while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
				System.out.println("Header: " + headerLine);
			}

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

			int queryIndex = path.indexOf('?');
			String cleanPath = (queryIndex == -1) ? path : path.substring(0, queryIndex);
			String query = (queryIndex == -1) ? null : path.substring(queryIndex + 1);

			Map<String, String> queryParams = QueryStringParser.parse(query);
			int[] statusOut = new int[1];
			String serviceJson = HardcodedServices.handle(cleanPath, queryParams, statusOut);

			if (serviceJson != null) {
				byte[] bodyBytes = serviceJson.getBytes("UTF-8");
				sendResponse(rawOut, statusOut[0], statusText(statusOut[0]), "application/json; charset=UTF-8",
						bodyBytes);
				return;
			}

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

	private static void sendResponse(OutputStream out, int status, String statusText, String contentType, byte[] body)
			throws IOException {
		String headers = "HTTP/1.1 " + status + " " + statusText + "\r\n" + "Content-Type: " + contentType + "\r\n"
				+ "Content-Length: " + body.length + "\r\n" + "Connection: close\r\n" + "\r\n";
		out.write(headers.getBytes("UTF-8"));
		out.write(body);
		out.flush();
	}

	private static void sendError(OutputStream out, int status, String statusText) throws IOException {
		byte[] body = ("<html><body><h1>" + status + " " + statusText + "</h1></body></html>").getBytes("UTF-8");
		sendResponse(out, status, statusText, "text/html; charset=UTF-8", body);
	}

	private static String statusText(int status) {
		return switch (status) {
		case 200 -> "OK";
		case 400 -> "Bad Request";
		case 404 -> "Not Found";
		case 405 -> "Method Not Allowed";
		default -> "Error";
		};
	}
}
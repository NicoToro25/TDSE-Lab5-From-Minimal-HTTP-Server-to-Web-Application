package edu.eci.tdse;

import java.io.*;
import java.net.*;

public class MinimalHttpServer {

	public static void main(String[] args) throws IOException {
		int port = 35000;

		ServerSocket serverSocket = new ServerSocket(port);
		System.out.println("Servidor escuchando en el puerto " + port + "...");

		// El servidor vive indefinidamente, aceptando una conexión
		// a la vez. No hay concurrencia: hasta que handleClient()
		// termina, no se vuelve a llamar accept().
		while (true) {
			Socket clientSocket = serverSocket.accept();
			try {
				handleClient(clientSocket);
			} catch (IOException e) {
				// Un request malformado no debe tumbar el servidor completo.
				System.err.println("Error manejando el cliente: " + e.getMessage());
			}
		}
	}

	private static void handleClient(Socket clientSocket) throws IOException {
		clientSocket.setSoTimeout(5000); // máx. 5s esperando datos del cliente

		try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				OutputStream rawOut = clientSocket.getOutputStream()) {
			String requestLine;
			try {
				requestLine = in.readLine();
			} catch (SocketTimeoutException e) {
				System.out.println("Conexión sin actividad, cerrando (posible pre-connect del navegador).");
				return;
			}

			if (requestLine == null || requestLine.isEmpty()) {
				return;
			}

			System.out.println("Request line: " + requestLine);

			String headerLine;
			while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
				System.out.println("Header: " + headerLine);
			}

			String body = "<!doctype html><html><head>" + "<meta charset=\"UTF-8\"><title>My Web Site</title></head>"
					+ "<body>My Web Site</body></html>";
			byte[] bodyBytes = body.getBytes("UTF-8");

			String headers = "HTTP/1.1 200 OK\r\n" + "Content-Type: text/html; charset=UTF-8\r\n" + "Content-Length: "
					+ bodyBytes.length + "\r\n" + "Connection: close\r\n" + "\r\n";

			rawOut.write(headers.getBytes("UTF-8"));
			rawOut.write(bodyBytes);
			rawOut.flush();
		} finally {
			clientSocket.close();
		}
	}
}
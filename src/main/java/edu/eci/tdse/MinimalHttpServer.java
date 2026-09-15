package edu.eci.tdse;


import java.io.*;
import java.net.*;

public class MinimalHttpServer {

    public static void main(String[] args) throws IOException {
        int port = 35000;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor escuchando en el puerto " + port + "...");

            // Por ahora: acepta UNA conexión, responde, y termina.
            // (2.2 lo cambiará a un loop)
            Socket clientSocket = serverSocket.accept();
            handleClient(clientSocket);
        }
    }

    private static void handleClient(Socket clientSocket) throws IOException {
        try (
            BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
            OutputStream rawOut = clientSocket.getOutputStream()
        ) {
            // 1. Leer la request line: "GET /index.html HTTP/1.1"
            String requestLine = in.readLine();
            System.out.println("Request line: " + requestLine);

            // 2. Consumir el resto de los headers (los necesitamos leer
            //    aunque no los usemos todavía, para no dejar el socket sucio)
            String headerLine;
            while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
                System.out.println("Header: " + headerLine);
            }

            // 3. Construir una respuesta mínima como bytes
            String body = "<!doctype html><html><head>"
                + "<meta charset=\"UTF-8\"><title>My Web Site</title></head>"
                + "<body>My Web Site</body></html>";
            byte[] bodyBytes = body.getBytes("UTF-8");

            String headers = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html; charset=UTF-8\r\n"
                + "Content-Length: " + bodyBytes.length + "\r\n"
                + "Connection: close\r\n"
                + "\r\n";

            rawOut.write(headers.getBytes("UTF-8"));
            rawOut.write(bodyBytes);
            rawOut.flush();
        } finally {
            clientSocket.close();
        }
    }
}
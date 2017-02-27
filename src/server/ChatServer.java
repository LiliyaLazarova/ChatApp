package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

public class ChatServer {

	private static final int PORT = 6666;
	private static HashSet<PrintWriter> writers = new HashSet<>();
	private static HashSet<Socket> connections = new HashSet<>();

	public static void main(String[] args) {

		ServerSocket serverSocket = null;

		try {
			serverSocket = new ServerSocket(PORT);
			System.out.println("Server is running.Waiting for connections...");

			while (true) {
				try {
					Socket clientSocket = serverSocket.accept();
					synchronized (connections) {
						connections.add(clientSocket);
					}
					System.out.println("Client connected from :" + clientSocket.getInetAddress());

					ChatServerThread server = new ChatServerThread(clientSocket);
					Thread serverThread = new Thread(server);
					serverThread.start();
				} catch (IOException e) {
					System.out.println("Could not connect to host");
				}
			}
		} catch (IOException e) {
			System.out.println("Could not listen on port:" + PORT + "! Please, check if server is already started ");
		} finally {
			closeOpenedClientSockets();
			try {
				if (serverSocket != null) {
					serverSocket.close();
				}
			} catch (IOException e) {
				System.out.println("Could not close the socket");
			}
		}
	}

	private static void closeOpenedClientSockets() {
		for (Socket connection : connections) {
			boolean connected = connection.isConnected() && !connection.isClosed();
			if (!connected) {
				connections.remove(connection);
				System.out.println("Closed open socket : " + connection.getLocalAddress());
			}
		}
	}

	private static class ChatServerThread implements Runnable {

		private Socket clientSocket;
		private String input;

		public ChatServerThread(Socket clientSocket) {
			if (clientSocket != null) {
				this.clientSocket = clientSocket;
			} else {
				System.out.println("Client socket doesn't exist");
			}
		}

		@Override
		public void run() {

			try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

				synchronized (writers) {
					writers.add(out);
				}
				while (true) {
					input = in.readLine();
					if (input == null) {
						System.out.println("Client disconnected from " + clientSocket.getInetAddress().getHostAddress());
						synchronized (connections) {
							connections.remove(clientSocket);
						}
						return;
					}
					for (PrintWriter writer : writers) {
						if (writer.equals(out)) {
							continue;
						}
						writer.println("Incomming MESSAGE : " + input);
					}
				}
			} catch (IOException e) {
				System.out.println("Client has been disconnected from server!");
			} finally {
				if (clientSocket != null) {
					synchronized (connections) {
						connections.remove(clientSocket);
					}
				}
				try {
					clientSocket.close();
					System.out.println("Client socket is closed now");
				} catch (IOException e) {
					System.out.println("Error occured trying to close client socket");
				}
			}
		}

	}
}

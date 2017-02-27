package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ChatClient {

	private static final int PORT = 6666;
	private static String hostname;

	public static void main(String[] args) {

		String chat = null;
		System.out.println("Please, enter the Chat server's IP address");
		Scanner scanner = new Scanner(System.in);
		hostname = scanner.nextLine();

		try (Socket client = new Socket(hostname, PORT);
				BufferedReader clientInput = new BufferedReader(new InputStreamReader(System.in));
				PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {

			System.out.println("Happy chatting :)");
			ChatClientReader clientReader = new ChatClientReader(client);
			Thread clientReaderThread = new Thread(clientReader);
			clientReaderThread.start();

			while ((chat = clientInput.readLine()) != null) {
				out.println(chat);
			}
		} catch (UnknownHostException e) {
			System.out.println("Unknown host, please enter a valid IP address");
		} catch (IOException e) {
			System.out.println("Could not connect to host " + hostname);
		} finally {
			scanner.close();
		}
	}

	private static class ChatClientReader implements Runnable {

		private Socket client;
		private String incomingMessage;

		public ChatClientReader(Socket client) {
			if (client != null) {
				this.client = client;
			}else {
				System.out.println("Client socket doesn't exist");
			}
		}

		@Override
		public void run() {

			try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {

				while (true) {
					incomingMessage = in.readLine();
					if (incomingMessage != null) {
						System.out.println(incomingMessage);
					} else {
						break;
					}
				}
			} catch (IOException e) {
				System.out.println("Disconnected from  server.");
				System.exit(-1);
			}

		}

	}
}

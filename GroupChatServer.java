// Package for I/O related stuff
import java.io.*;

// Package for socket related stuff
import java.net.*;

// Package for list related stuff
import java.util.*;

/*
 * This class does all the group chat server's job
 *
 * It consists of parent thread (code inside main method) which accepts
 * new client connections and then spawns a thread per connection
 *
 * Each child thread (code inside run method) reads messages
 * from its socket and relays the message to the all active connections
 *
 * Since a thread is being created with this class object,
 * this class declaration includes "implements Runnable"
 */
public class GroupChatServer implements Runnable
{
	// Each instance has a separate socket
	private Socket clientSock;

	// This class keeps track of active clients
	private static List<PrintWriter> clientList;

	// Constructor sets the socket for the child thread to process
	public GroupChatServer(Socket sock)
	{
		clientSock = sock;
	}

	// Add the given client to the active clients list
	// Since all threads share this, we use "synchronized" to make it atomic
	public static synchronized boolean addClient(PrintWriter toClientWriter)
	{
		return(clientList.add(toClientWriter));
	}

	// Remove the given client from the active clients list
	// Since all threads share this, we use "synchronized" to make it atomic
	public static synchronized boolean removeClient(PrintWriter toClientWriter)
	{
		return(clientList.remove(toClientWriter));
	}

	// Relay the given message to all the active clients
	// Since all threads share this, we use "synchronized" to make it atomic
	public static synchronized void relayMessage(
			PrintWriter fromClientWriter, String mesg, String username)
	{
		// Iterate through the client list and
		// relay message to each client (but not to the client where message came from)
		for (int i = 0; i < clientList.size(); i++) {
			PrintWriter temp = clientList.get(i);
			if (temp == fromClientWriter)
				continue;
			else
				temp.println(username + ": " + mesg);
		}
	}

	public static synchronized void joinMessage(
                        PrintWriter fromClientWriter, String username)
        {
                for (int i = 0; i < clientList.size(); i++) {
                        PrintWriter temp = clientList.get(i);
                        if (temp == fromClientWriter)
                                continue;
                        else
                                temp.println(username + " joined the chat");
                }
        }

	// The child thread starts here
	public void run()
	{
		// Read from the client and relay to other clients
		try {
			// Prepare to read from socket
			BufferedReader fromSockReader = new BufferedReader(
                                        new InputStreamReader(clientSock.getInputStream()));

			// Get the client name, expect client to send her name as the first message through the socket
			String username = fromSockReader.readLine();

			// Prepare to write to socket with auto flush on
			PrintWriter toSockWriter =
                                        new PrintWriter(clientSock.getOutputStream(), true);

			// Add this client to the active client list
			addClient(toSockWriter);

			toSockWriter.println("Greetings, begin typing to chat:");
			joinMessage(toSockWriter, username);

			// Keep doing till client sends EOF
			while (true) {
				// Read a line from the client
				String line = fromSockReader.readLine();

				// If we get null, it means client quit, break out of loop
				if (line == null)
                                        break;

				// Else, relay the line to all active clients
				else
					relayMessage(toSockWriter, line, username);
			}

			// Done with the client, remove it from client list
			removeClient(toSockWriter);

			// Done with the client, close everything related with this client
			System.exit(0);

		}
		catch (Exception e) {
			System.out.println(e);
			System.exit(1);
		}
	}

	/*
	 * The group chat server program starts from here.
	 * This main thread accepts new clients and spawns a thread for each client
	 * Each child thread does the stuff under the run() method
	 */
	public static void main(String args[])
	{
		// Server needs a port to listen on
		if (args.length != 1) {
			System.out.println("usage: java GroupChatServer <server port>");
			System.exit(1);
		}

		// Be prepared to catch socket related exceptions
		try {
			// Create a server socket with the given port
			ServerSocket servSock =
                                        new ServerSocket(Integer.parseInt(args[0]));

			// Keep track of active clients in a list
			clientList = new ArrayList<PrintWriter>();

			// Keep accepting/serving new clients
			while (true) {
				// Wait to accept another client
				Socket cliSock = servSock.accept();

				// Spawn a thread to read/relay messages from this client
				Thread child = new Thread(
                                        new GroupChatServer(cliSock));
                        	child.start();
			}
		}
		catch(Exception e) {
			System.out.println(e);
			System.exit(1);
		}
	}
}

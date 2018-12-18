package edu.northeastern.ccs.im.chatter.chatter.client;

import java.util.Scanner;

import edu.northeastern.ccs.im.chatter.IMConnection;
import edu.northeastern.ccs.im.chatter.KeyboardScanner;
import edu.northeastern.ccs.im.chatter.Message;
import edu.northeastern.ccs.im.chatter.MessageScanner;

/**
 * Class which can be used as a command-line IM client.
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 *
 * @version 1.3
 */
public class CommandLineMain {
	private static String username = "";

	public static IMConnection loginScenario(Scanner in, String[] args) {

		System.out.println("Enter your username : ");
		username = in.nextLine();
		System.out.println("Enter your password : ");
		String password = in.nextLine();

		return new IMConnection(args[0], Integer.parseInt(args[1]), username,
				password);
	}

	public static IMConnection registerScenario(Scanner in, String[] args) {

		System.out.println("Enter your desired username : ");
		username = in.nextLine();
		System.out.println("Enter your desired password : ");
		String password = in.nextLine();

		return new IMConnection(args[0], Integer.parseInt(args[1]), username,
				password);
	}

	/**
	 * This main method will perform all of the necessary actions for this phase
	 * of the course project.
	 *
	 * @param args Command-line arguments which we ignore
	 */
	public static void main(String[] args) {
		IMConnection connect = null;
		@SuppressWarnings("resource")
		Scanner in = new Scanner(System.in);

		String option;

		do {

			System.out.println("Enter Login for login : ");
			System.out.println("Enter Register for registering a user : ");

			option = in.nextLine();

			switch (option) {
			case "Login":
				connect = loginScenario(in, args);
				break;
			case "Register":
				connect = registerScenario(in, args);
				break;
			default:
				System.out.println("Wrong option");
				break;
			}

		} while (connect == null || !connect.connect(option));

		// Create the objects needed to read & write IM messages.
		KeyboardScanner scan = connect.getKeyboardScanner();
		MessageScanner mess = connect.getMessageScanner();
		
		
		if (username.equalsIgnoreCase("agency")) {

			if (connect.connectionActive()) {
				System.out.println("Enter comma separated name of the users "
						+ "or a group name for duping: ");
				int count =0;
				// Read in the text they typed
				while(count ==0) {
					if (scan.hasNext()) {
						// Read in the text they typed
						String line = scan.nextLine();
						connect.sendDupingMessage(line);
						count++;
					}
				}
				// Repeat the following loop
				while (connect.connectionActive()) {

					// Get any recent messages received from the IM server.
					if (mess.hasNext()) {

						Message message = mess.next();

						if (message.isNonAcknowledge()) {
							System.err.println(message.getText());
							connect.disconnect();
						} else if (!message.getSender()
								.equals(connect.getUserName())) {
							System.out.println("sender is : "+message.getSender()+
								" recipient is : "+message.getRecipientName()+
								" message is : "+message.getText());
						} else if (message.isAcknowledge()) {
							System.err.println(message.getText());

						}
					}
				}
			}

		}else {
			// Repeat the following loop
			while (connect.connectionActive()) {
				// Check if the user has typed in a line of text to broadcast to the
				// IM server.
				// If there is a line of text to be
				// broadcast:
				if (scan.hasNext()) {
					// Read in the text they typed
					String line = scan.nextLine();

					// If the line equals "/quit", close the connection to the IM
					// server.
					if (line.equals("/quit")) {
						connect.disconnect();
						break;
					} else if (line.contains("UPD")) {
						String[] updateMessage = line.split("\\s+");
						connect.sendUpdateMessage(updateMessage[2]);
					} else if (line.contains("DEL")) {
						connect.sendDeleteMessage();
					} else if(line.contains("PVT")) {

						String rec = getReceiver(line);
						String message = getMessage(line);
						connect.sendPrivateMessage(rec, message);
					} else if(line.contains("HST")){
						String with = getReceiver(line);
						String numMsgs = getMessage(line);
						numMsgs = numMsgs.replaceAll("\\s", "");
						connect.sendHistoryMessage(with, numMsgs);

					}else if(line.contains("GAD")){
						String[] group = line.split("\\s+");
						connect.sendGroupAddMessage(group[1], group[2]);
						
					}else if(line.contains("GDL")){
						String[] group = line.split("\\s+");
						connect.sendGroupDeleteMessage(group[1]);
						
					}else if(line.contains("GUP")){
						String[] group = line.split("\\s+");
			
						connect.sendGroupUpdateMessage(group[1], group[2]);
						
					}else if(line.contains("GRP")){

						String grp = getReceiver(line);
						String message = getMessage(line);
						connect.sendGroupMessage(grp, message);
					}
					else if(line.contains("RCL")) {
						String[] s = line.split(" ");
						String s1 = "";
						for(int i = 1; i < s.length; i++)
							s1 += s[i] + " ";
						connect.sendRecallMessage(s1);
					}else if (line.contains("SRC")){
						String[] searchMessage = line.split("\\s+");
						connect.sendSearchMessage(searchMessage[1], searchMessage[2], searchMessage[3] + " " + searchMessage[4], searchMessage[5] + " " + searchMessage[6]);
					}else if (line.contains("PRC")) {
						String[] msg = line.split("\\s+");
						connect.sendParentalControlMessage(msg[1]);
					}
					else {
						connect.sendMessage(line);
					}
				}
				// Get any recent messages received from the IM server.
				if (mess.hasNext()) {

					Message message = mess.next();
					
					if (message.isNonAcknowledge()) {
						System.err.println(message.getText());
						connect.disconnect();
					}else if (message.isHistoryResposeMessage()){
						System.out.println(message.getText());
					} else if (!message.getSender().equals(connect.getUserName())) {
						System.out.println(
								message.getSender() + ": " + message.getText());
					} else if (message.isAcknowledge()) {
						System.err.println(message.getText());
						System.err.println(
								"To update password use the format: UPD <oldPassword> <newPassword>");
						System.err.println("To delete your account: DEL");
						System.err.println(
								"send private messages as \"PVT <recipient name> message\"");
						System.err.println(
								"Add Group \"GAD <groupname name> <comma separated users>\"");
						System.err.println(
								"Delete Group \"GDL <groupname name>\"");
						System.err.println(
								"Update Group \"GUP <groupname name> <comma separated new users>\"");
						System.err.println("send group messages as "
								+ "\"GRP <group name> message\"");
						System.err.println("request history via "
								+ "\"HST <user or group name> <number of recent messages\"");
						System.err.println("recall previously sent message via "
								+ "\"RCL <message text>\"");
						System.err.println("Search messages (use GMT timezone) \"SRC <receivedFrom, sentTo, or both> <user or " +
								"groupname (* for all messages)> <start time (YYYY-MM-DD HH:MM:SS)> <end time " +
								"(YYYY-MM-DD HH:MM:SS)>\"");
						System.err.println("Change parental controls \"PRC <on or off>\"");

					}else if (message.isGroupAcknowledge()) {
						System.err.println(message.getText());
						
					}else if (message.isGroupNotAcknowledge()) {
						System.err.println(message.getText());
					}
				}
			}
		}
		System.out.println("Program complete.");
		System.exit(0);
	}
	
	public static String getReceiver(String line) {
		String lineClone = line;
		String[] arr = lineClone.split(" ");
		return arr[1];
	}
	
	public static String getMessage(String line) {
		String lineClone = line;
		String[] arr = lineClone.split(" ");
		String message = "";
		for(int i = 2; i < arr.length; i++)
			message = message + arr[i] + " ";
		
		return message;
	}
}
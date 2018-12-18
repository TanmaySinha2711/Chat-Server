package edu.northeastern.ccs.im;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

/**
 * This class is similar to the java.util.Scanner class, but this class's
 * methods return immediately and does not wait for network input (it is
 * &quot;non-blocking&quot; in technical parlance).
 * 
 * This work is licensed under the Creative Commons Attribution-ShareAlike 4.0
 * International License. To view a copy of this license, visit
 * http://creativecommons.org/licenses/by-sa/4.0/. It is based on work
 * originally written by Matthew Hertz and has been adapted for use in a class
 * assignment at Northeastern University.
 * 
 * @version 1.3
 */
public class ScanNetNB {
	private static final int BUFFER_SIZE = 64 * 1024;

	private static final int DECIMAL_RADIX = 10;

	private static final int HANDLE_LENGTH = 3;

	private static final int MIN_MESSAGE_LENGTH = 7;

	private static final String CHARSET_NAME = "us-ascii";

	private static final Logger LOGGER = LogManager
			.getLogger(ScanNetNB.class.getName());

	private SocketChannel channel;

	private Selector selector;

	private SelectionKey key;

	private ByteBuffer buff;

	private Queue<Message> messages;

	/**
	 * Creates a new instance of this class. Since, by definition, this class
	 * takes in input from the network, we need to supply the non-blocking
	 * Socket instance from which we will read.
	 * 
	 * @param sockChan Non-blocking SocketChannel from which we will receive
	 *                 communications.
	 */
	public ScanNetNB(SocketChannel sockChan) {
		// Create the queue that will hold the messages received from over the
		// network
		messages = new ConcurrentLinkedQueue<>();
		// Allocate the buffer we will use to read data
		buff = ByteBuffer.allocate(BUFFER_SIZE);
		// Remember the channel that we will be using.
		channel = sockChan;
		try {
			// Open the selector to handle our non-blocking I/O
			selector = Selector.open();
			// Register our channel to receive alerts to complete the connection
			key = channel.register(selector, SelectionKey.OP_READ);
		} catch (IOException e) {
			// For the moment we are going to simply cover up that there was a
			// problem.
			LOGGER.log(Level.WARN, e.toString(), e);
			assert false;
		}
	}

	/**
	 * Creates a new instance of this class. Since, by definition, this class
	 * takes in input from the network, we need to supply the non-blocking
	 * Socket instance from which we will read.
	 * 
	 * @param connection Non-blocking Socket instance from which we will receive
	 *                   communications.
	 */
	public ScanNetNB(SocketNB connection) {
		// Get the socket channel from the SocketNB instance and go.
		this(connection.getSocket());
	}

	/**
	 * Read in a new argument from the IM server.
	 * 
	 * @param charBuffer Buffer holding text from over the network.
	 * @return String holding the next argument sent over the network.
	 */
	private String readArgument(CharBuffer charBuffer) {
		// Compute the current position in the buffer
		int pos = charBuffer.position();
		// Compute the length of this argument
		int length = 0;
		// Track the number of locations visited.
		int seen = 0;
		// Assert that this character is a digit representing the length of the
		// first argument
		assert Character.isDigit(charBuffer.get(pos));
		// Now read in the length of the first argument
		while (Character.isDigit(charBuffer.get(pos))) {
			// My quick-and-dirty numeric converter
			length = length * DECIMAL_RADIX;
			length += Character.digit(charBuffer.get(pos), DECIMAL_RADIX);
			// Move to the next character
			pos += 1;
			seen += 1;
		}
		seen += 1;
		if (length == 0) {
			// Update our position
			charBuffer.position(pos);
			// If the length is 0, this argument is null
			return null;
		}
		String result = charBuffer.subSequence(seen, length + seen).toString();
		charBuffer.position(pos + length);
		return result;
	}

	/**
	 * Returns true if there is another line of input from this instance. This
	 * method will NOT block while waiting for input. This class does not
	 * advance past any input.
	 * 
	 * @return True if and only if this instance of the class has another line
	 *         of input
	 * @see java.util.Scanner#hasNextLine()
	 */
	public boolean hasNextMessage() {
		
		// If we have messages waiting for us, return true.
		if (!messages.isEmpty()) {
			return true;
		}
		try {
			// Otherwise, check if we can read in at least one new message
			if (selector.selectNow() != 0) {
				assert key.isReadable();
				// Read in the next set of commands from the channel.
				channel.read(buff);
				selector.selectedKeys().remove(key);
				buff.flip();
			} else {
				return false;
			}
			// Create a decoder which will convert our traffic to something
			// useful
			Charset charset = Charset.forName(CHARSET_NAME);
			CharsetDecoder decoder = charset.newDecoder();
			// Convert the buffer to a format that we can actually use.
			CharBuffer charBuffer = decoder.decode(buff);
			// get rid of any extra whitespace at the beginning
			// Start scanning the buffer for any and all messages.
			int start = 0;
			// Scan through the entire buffer; check that we have the minimum
			// message size
			while ((start + MIN_MESSAGE_LENGTH) <= charBuffer.limit()) {
				// If this is not the first message, skip extra space.
				if (start != 0) {
					charBuffer.position(start);
				}
				// First read in the handle
				final String handle = charBuffer.subSequence(0, HANDLE_LENGTH)
						.toString();
				// Skip past the handle
				charBuffer.position(start + HANDLE_LENGTH + 1);
				// Read the first argument containing the sender's name
				final String sender = readArgument(charBuffer);

				if (handle.equalsIgnoreCase("PVT")||
						handle.equals("HST") ||
						handle.equalsIgnoreCase("GAD") ||
						handle.equalsIgnoreCase("GUP")) {
					// Skip past the leading space
					charBuffer.position(charBuffer.position() + 2);
					// Read in the second argument containing the recipient
					final String recipientName = readArgument(charBuffer);
					// Skip past the leading space
					charBuffer.position(charBuffer.position() + 2);
					// Read in the third argument containing the message
					final String message = readArgument(charBuffer);
					// Add this message into our queue
					Message newMsg = Message.makeMessage(handle, sender,
							message, null, recipientName);
					messages.add(newMsg);
					// And move the position to the start of the next character
					start = charBuffer.position() + 1;
				} else if (handle.equalsIgnoreCase("SRC")){

					charBuffer.position(charBuffer.position() + 2);
					final String sentReceivedBoth = readArgument(charBuffer);

					charBuffer.position(charBuffer.position() + 2);
					final String recipientName = readArgument(charBuffer);

					charBuffer.position(charBuffer.position() + 2);
					final String startTime = readArgument(charBuffer);

					charBuffer.position(charBuffer.position() + 2);
					final String endTime = readArgument(charBuffer);

					// Add this message into our queue
					Message newMsg = Message.makeSearchMessage(sender, sentReceivedBoth,
									recipientName, startTime, endTime);
					messages.add(newMsg);
					// And move the position to the start of the next character
					start = charBuffer.position() + 1;
				}
				else if (handle.equalsIgnoreCase("GRP")) {
					// Skip past the leading space
					charBuffer.position(charBuffer.position() + 2);
					// Read in the second argument containing the recipient
					final String groupName = readArgument(charBuffer);
					// Skip past the leading space
					charBuffer.position(charBuffer.position() + 2);
					// Read in the third argument containing the message
					final String message = readArgument(charBuffer);
					// Add this message into our queue
					Message newMsg = Message.makeMessage(handle, sender,
							message, null, groupName);
					messages.add(newMsg);
					// And move the position to the start of the next character
					start = charBuffer.position() + 1;
				} else if (handle.equalsIgnoreCase("PRC")) {
					charBuffer.position(charBuffer.position() + 2);
					// Read in the second argument containing on or off
					final String newStatus = readArgument(charBuffer);
					Message newMsg = Message.makeParentalControlMessage(sender, newStatus);
					messages.add(newMsg);
					start = charBuffer.position() + 1;
				} else {
					// Skip past the leading space
					charBuffer.position(charBuffer.position() + 2);
					// Read in the second argument containing the message
					final String message = readArgument(charBuffer);
					// Add this message into our queue
					Message newMsg = Message.makeMessage(handle, sender,
							message, message,null);
					messages.add(newMsg);
					// And move the position to the start of the next character
					start = charBuffer.position() + 1;
				}
			}
			// Move any read messages out of the buffer so that we can add to
			// the end.
			buff.position(start);
			// Move all of the remaining data to the start of the buffer.
			buff.compact();
		} catch (IOException ioe) {
			// For the moment, we will cover up this exception and hope it never
			// occurs.
			assert false;
		}
		// Do we now have any messages?
		return !messages.isEmpty();
	}

	/**
	 * Advances past the current line and returns the line that was read. This
	 * method returns the rest of the current line, excluding any line separator
	 * at the end. The position in the input is set to the beginning of the next
	 * line.
	 * 
	 * @throws NextDoesNotExistException Exception thrown when hasNextLine
	 *                                   returns false.
	 * @return String containing the line that was skipped
	 * @see java.util.Scanner#nextLine()
	 */
	public Message nextMessage() {
		if (messages.isEmpty()) {
			throw new NextDoesNotExistException(
					"No next line has been typed in at the keyboard");
		}
		Message msg = messages.remove();

		LOGGER.log(Level.INFO, msg.toString());
		return msg;
	}

	public void close() {
		try {
			selector.close();
		} catch (IOException e) {
			LOGGER.log(Level.WARN, e.toString(), e);
			assert false;
		}
	}
}

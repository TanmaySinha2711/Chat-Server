package edu.northeastern.ccs.im;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import java.util.regex.Pattern;

/**
 * This class is used to check for/replace words within a string that contain vulgarities.
 */
public class ParentalControls {
  private static final Logger LOGGER = LogManager.getLogger(ParentalControls.class.getName());

  private HashMap<String, String> forbiddenWords;
  private List<String> order;
  private static final String PROPERTY_FILE="/forbiddenWords.txt";

  /**
   * Constructs an instance of parental controls.
   */
  public ParentalControls() {
    forbiddenWords = new HashMap<>();
    order = new ArrayList<>();
    populate();
  }

  /**
   * Reads the forbiddenWords file and adds the bad word as the key and their replacements
   * (asterisks) as the value in this forbiddenWords map.
   */
  private void populate() {

    if (forbiddenWords.isEmpty()) {
      InputStream in = getClass().getResourceAsStream(PROPERTY_FILE);
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));

      String badWord;

      try {
        while ((badWord = reader.readLine()) != null) {
          StringBuilder replacement = new StringBuilder();
          for (int i = 0; i < badWord.length(); i++) {
            replacement.append("*");
          }
          forbiddenWords.put(badWord, replacement.toString());
          order.add(badWord);
        }
        reader.close();
      } catch (IOException e) {
        LOGGER.log(Level.WARN, e.toString(), e);
      }
    }
  }


  /**
   * Replace all occurrences of bad words in the given text with asterisks.
   *
   * @param msgText text to change
   * @return a new string with bad words as asterisks
   */
  public String replaceWords(String msgText) {
    String newText = msgText;
    for (String aBadWord : order) {
      if (Pattern.compile(Pattern.quote(aBadWord), Pattern.CASE_INSENSITIVE).matcher(msgText).find()) {
        newText = newText.replaceAll("(?i)" + aBadWord, forbiddenWords.get(aBadWord));
      }
    }
    return newText;
  }

  /**
   * Checks if the given text contains a bad word.
   *
   * @param msgText text to check for bad words
   * @return true if the given text contains a bad word
   */
  public boolean checkWords(String msgText) {
    for (Entry<String, String> anEntry : forbiddenWords.entrySet()) {
      if (Pattern.compile(Pattern.quote(anEntry.getKey()), Pattern.CASE_INSENSITIVE).matcher(msgText).find()) {
        return true;
      }
    }
    return false;
  }
}

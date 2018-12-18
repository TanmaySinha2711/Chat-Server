package edu.northeastern.ccs.im;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParentalControlsTest {

  ParentalControls badWords;

  @BeforeEach
  void setup() {
    badWords = new ParentalControls();
  }

  @Test
  void testReplaceWords() {
    assertEquals("****", badWords.replaceWords("shit"));
    assertEquals("oh ****", badWords.replaceWords("oh shit"));
    assertEquals("**** dude", badWords.replaceWords("shit dude"));
    assertEquals("****", badWords.replaceWords("fuck"));
    assertEquals("*******", badWords.replaceWords("bitches"));
    assertEquals("*********************", badWords.replaceWords("fuckbitchmotherfucker"));
    assertEquals("no change", badWords.replaceWords("no change"));
  }

  @Test
  void testCheckWords() {
    assertTrue(badWords.checkWords("shit"));
    assertTrue(badWords.checkWords("fuck shit motherfucker"));
    assertTrue(badWords.checkWords("oh shit"));
    assertTrue(badWords.checkWords("shit test"));
    assertTrue(badWords.checkWords("test bitch test"));
    assertFalse(badWords.checkWords("test false test"));
  }


}
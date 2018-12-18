package edu.northeastern.ccs.im.server;

import edu.northeastern.ccs.im.chatter.IMConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.lang.reflect.Field;


import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PrattleMainTest {

  @Test
  void main() {
    // use reflection to prevent the main loop from running forever
    try {
      Field f = Prattle.class.getDeclaredField("breakLoopAfter");
      f.setAccessible(true);
      f.set(null, 10);
    }catch(Exception e){
      e.printStackTrace();
      fail("could not set breakLoopAfter via reflection");
    }

    // run the main in one thread, and add clients in another
    try {
      String[] args = new String[1];
      Thread t1 = new Thread(() -> {
        try{Prattle.main(args);}catch (Exception e){fail();e.printStackTrace();}
      });
      t1.start();
      Thread t2 = new Thread(() -> {
        // set up client thread
          IMConnection clientTerminal = new IMConnection("127.0.0.1", ServerConstants.PORT, "tim", "tim");
          clientTerminal.connect("Login");
          clientTerminal.sendMessage("this is a message!");
          clientTerminal.disconnect();
      });
      t2.start();
    }catch (Exception e){
      e.printStackTrace();
      fail("main did not work with valid args");
    }
  }

}
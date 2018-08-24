package net.technearts.rip;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;

public class RequestSpyTest {

  @Test @Ignore
  public void test() {
    RequestSpy rs = new RequestSpy("www.google.com", 80);
    assertTrue(rs.spyRequest("GET / HTTP/1.1").startsWith("HTTP/1.1 200 OK"));
  }

}

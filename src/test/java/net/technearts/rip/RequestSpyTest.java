package net.technearts.rip;

import org.junit.Ignore;
import org.junit.Test;

public class RequestSpyTest {

  @Test @Ignore
  public void test() {
    RequestSpy rs = new RequestSpy("www.google.com", 80);
    rs.spyRequest("GET / HTTP/1.1");
  }

}

package net.technearts.rip;

import static org.junit.Assert.*;

import org.junit.Test;

public class RequestSpyTest {

	@Test
	public void test() {
		RequestSpy rs = new RequestSpy();
		rs.spyRequest("www.google.com", 80, "GET / HTTP/1.1");
	}

}

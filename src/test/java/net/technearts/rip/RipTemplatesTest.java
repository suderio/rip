package net.technearts.rip;

import static net.technearts.rip.RipServer.localhost;
import static net.technearts.rip.RipServer.stop;
import static net.technearts.rip.RipServer.withFile;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import io.restassured.RestAssured;

public class RipTemplatesTest {
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RestAssured.port = 7777;
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        stop(7777);
    }
	@Test
	public void testBasicTemplate() {
		localhost().get("/test").buildResponse(withFile("/test.json.ftl"), 200, res -> res.put("x", "xpto"), res -> res.put("y", "0"));
	}

}

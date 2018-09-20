package net.technearts.rip;

import static io.restassured.RestAssured.when;
import static net.technearts.rip.RipServer.localhost;
import static org.hamcrest.CoreMatchers.containsString;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import io.restassured.RestAssured;

public class RipTemplatesTest {
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    RestAssured.port = 7777;
    localhost(7777).get("/test/:n").buildResponse("test.json.ftl", 200,
        att -> att.put("x", req -> "xpto"), att -> att.put("y", req -> req.params("n")));
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    // stop(6666);
  }

  @Test
  public void testBasicTemplate() {
    when().get("/test/0").then().content(containsString("xpto")).and().content(containsString("0"));
  }

}

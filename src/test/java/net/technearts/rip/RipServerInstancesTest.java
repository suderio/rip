package net.technearts.rip;

import static io.restassured.RestAssured.when;
import static net.technearts.rip.RipServer.localhost;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.hamcrest.CoreMatchers.containsString;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import io.restassured.RestAssured;

public class RipServerInstancesTest {
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    RestAssured.port = 7777;
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    // stop(7777);
    // stop(8888);
  }

  @Ignore
  @Test
  public void resetTest() throws InterruptedException {
    localhost(7777).get("/test").respond("Ok");
    when().get("/test").then().body(containsString("Ok"));
    localhost(7777).reset();
    localhost(7777).get("/test2").respond("Ok");
    // TODO TRATAR ERRO ABAIXO
    localhost(8888).get("/").respond("");
    localhost(8888).reset();
    when().get("/test2").then().body(containsString("Ok"));
    when().get("/test").then().statusCode(SC_NOT_FOUND);
  }

  public void tryResourcesTest() {
    try (RipRoute server = localhost(6666)) {
    }
  }

  // TODO TRATAR CASO DE CHAMADA DO RESET SEM ROTAS

}

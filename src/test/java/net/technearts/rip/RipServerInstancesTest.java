package net.technearts.rip;

import static io.restassured.RestAssured.when;
import static net.technearts.rip.RipServer.localhost;
import static net.technearts.rip.RipServer.stop;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.hamcrest.CoreMatchers.containsString;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import io.restassured.RestAssured;

public class RipServerInstancesTest {
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RestAssured.port = 9999;
    }
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        stop(9999);
        stop(8888);
        stop(6666);
    }
	@Test
	public void resetTest() throws InterruptedException {
		localhost(9999).get("/test").respond("Ok");
		when().get("/test").then().content(containsString("Ok"));
		localhost(9999).reset();
		localhost(9999).get("/test2").respond("Ok");
		// TODO TRATAR ERRO ABAIXO
		localhost(8888).get("/").respond("");
		localhost(8888).reset();
		when().get("/test2").then().content(containsString("Ok"));
		when().get("/test").then().statusCode(SC_NOT_FOUND);
	}
	
	public void tryResourcesTest() {
		try (RipRoute server = localhost(6666)){
			
		}
	}
	
	//TODO TRATAR CASO DE CHAMADA DO RESET SEM ROTAS

}

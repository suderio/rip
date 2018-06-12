package net.technearts.rip;

import static io.restassured.RestAssured.when;
import static net.technearts.rip.RipServer.localhost;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.hamcrest.CoreMatchers.containsString;

import org.junit.BeforeClass;
import org.junit.Test;

import io.restassured.RestAssured;

public class RipServerInstancesTest {
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RestAssured.port = 7777;
    }
    
	@Test
	public void resetTest() throws InterruptedException {
		localhost().get("/test").respond("Ok");
		when().get("/test").then().content(containsString("Ok"));
		localhost().reset();
		localhost().get("/test2").respond("Ok");
		// TODO TRATAR ERRO ABAIXO
		localhost(8888).reset();
		when().get("/test2").then().content(containsString("Ok"));
		when().get("/test").then().statusCode(SC_NOT_FOUND);
	}
	
	//TODO TRATAR CASO DE CHAMADA DO RESET SEM ROTAS

}

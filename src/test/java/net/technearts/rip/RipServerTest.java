package net.technearts.rip;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static net.technearts.rip.RipServer.localhost;
import static net.technearts.rip.RipServer.stop;
import static net.technearts.rip.RipServer.withFile;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.containsString;

import java.util.stream.IntStream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import io.restassured.RestAssured;

public class RipServerTest {
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RestAssured.port = 7777;
        localhost().get("/test").respond("Ok");
        localhost().post("/test").contains("teste1").respond(withFile("/teste1.json"));
        localhost().post("/test").contains("teste2").and().contains("something").respond(withFile("/teste2.json"));
        localhost().put("/test").containsAll("123", "456").respond("123456");
        localhost().put("/test").containsAny("789", "987").respond("789987");
        localhost().put("/test").respond("Ok");
        localhost().delete("/test").contains("xpto").or().contains("abcd").respond("Ok", SC_OK);
        localhost().delete("/test").respond("Not Ok", SC_FORBIDDEN);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        stop(7777);
    }

    @Test
    public void testDelete() {
        given().body("something abcd something...").when().delete("/test").then().statusCode(SC_OK);
        given().body("something xpto something...").when().delete("/test").then().statusCode(SC_OK);
        given().body("something abcd xpto something...").when().delete("/test").then().statusCode(SC_OK);
        when().delete("/test").then().statusCode(SC_FORBIDDEN);
    }

    @Test
    public void testDeterministic() {
        IntStream.rangeClosed(1, 1000).boxed().parallel().forEach(i -> {
            given().body("something abcd xpto something...").when().delete("/test").then().statusCode(SC_OK);
            given().body("something 987 something 789...").when().put("/test").then().content(containsString("789987"));
            given().body("teste1").when().post("/test").then().content(containsString("Ok"));
            when().get("/test").then().content(containsString("Ok"));
        });
    }

    @Test
    public void testGet() {
        when().get("/test").then().content(containsString("Ok"));
    }

    @Test
    public void testPost() {
        when().get("/test").then().content(containsString("Ok"));
        given().body("teste1").when().post("/test").then().content(containsString("Ok"));
        given().body("teste2 something xpto").when().post("/test").then().content(containsString("KO"));
    }

    @Test
    public void testPut() {
        given().body("something 123 something 456...").when().put("/test").then().content(containsString("123456"));
        given().body("something 987 something 789...").when().put("/test").then().content(containsString("789987"));
        when().put("/test").then().content(containsString("Ok"));
    }

}

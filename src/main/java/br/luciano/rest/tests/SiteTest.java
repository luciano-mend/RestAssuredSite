package br.luciano.rest.tests;

import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import br.luciano.rest.core.BaseTest;


public class SiteTest extends BaseTest{

	@Test
	public void naoDeveAcessarSemToken() {
		given()
		.when()
			.get("/contas")
		.then()
			.statusCode(401)
		;
	}
	
	@Test
	public void deveIncluriContaComSucesso() {
		Map<String, String> login = new HashMap<>();
		login.put("email", "luciano@email.com");
		login.put("senha", "123456");
		
		String token = given()
			.body(login)
		.when()
			.post("/signin")
		.then()
			.statusCode(200)
			.extract().path("token")
		;
		
		given()
			.header("Authorization", "JWT " + token)
			.body("{ \"nome\": \"conta nova\" }")
		.when()
			.post("/contas")
		.then()
			.statusCode(201)
		;
		
	}
}

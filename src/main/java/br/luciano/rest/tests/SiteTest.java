package br.luciano.rest.tests;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import br.luciano.rest.core.BaseTest;


public class SiteTest extends BaseTest{
	private String token;
	
	@Before
	public void login() {
		Map<String, String> login = new HashMap<>();
		login.put("email", "luciano@email.com");
		login.put("senha", "123456");
		
		token = given()
			.body(login)
		.when()
			.post("/signin")
		.then()
			.statusCode(200)
			.extract().path("token")
		;
	}

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
		given()
			.header("Authorization", "JWT " + token)
			.body("{ \"nome\": \"conta nova\" }")
		.when()
			.post("/contas")
		.then()
			.statusCode(201)
		;
		
	}
	
	@Test
	public void deveAlterarContaComSucesso() {
		given()
			.header("Authorization", "JWT " + token)
			.body("{ \"nome\": \"conta alterada\" }")
		.when()
			.put("/contas/2237412")
		.then()
			.statusCode(200)
			.body("nome", is("conta alterada"))
		;
		
	}
	
	@Test
	public void naoDeveInserirContaComMesmoNome() {
		given()
			.header("Authorization", "JWT " + token)
			.body("{ \"nome\": \"conta alterada\" }")
		.when()
			.post("/contas")
		.then()
			.statusCode(400)
			.body("error", is("Já existe uma conta com esse nome!"))
		;
		
	}
}

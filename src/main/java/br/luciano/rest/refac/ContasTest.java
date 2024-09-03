package br.luciano.rest.refac;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import br.luciano.rest.core.BaseTest;
import io.restassured.RestAssured;

public class ContasTest extends BaseTest{

	@BeforeClass
	public static void login() {
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
		
		RestAssured.requestSpecification.header("Authorization", "JWT " + token);
		
		RestAssured.get("/reset").then().statusCode(200);
	}
	
	@Test
	public void deveIncluriContaComSucesso() {
		Map<String, String> jsonBody = new HashMap<>();
		jsonBody.put("nome", "Conta inserida");
		
		given()
			.body(jsonBody)
		.when()
			.post("/contas")
		.then()
			.statusCode(201)
		;
		
	}
	
	@Test
	public void deveAlterarContaComSucesso() {
		Map<String, String> jsonBody = new HashMap<>();
		jsonBody.put("nome", "Conta alterada");
		
		given()
			.body(jsonBody)
		.when()
			.put("/contas/{contaId}", getIdContaPeloNome("Conta para alterar"))
		.then()
			.statusCode(200)
			.body("nome", is("Conta alterada"))
		;
		
	}
	
	@Test
	public void naoDeveInserirContaComMesmoNome() {
		Map<String, String> jsonBody = new HashMap<>();
		jsonBody.put("nome", "Conta mesmo nome");
		
		given()
			.body(jsonBody)
		.when()
			.post("/contas")
		.then()
			.statusCode(400)
			.body("error", is("Já existe uma conta com esse nome!"))
		;
		
	}
	
	public Integer getIdContaPeloNome(String nomeConta) {
		return RestAssured.get("/contas?nome={nomeConta}", nomeConta).then().extract().path("id[0]");
	}
	
}

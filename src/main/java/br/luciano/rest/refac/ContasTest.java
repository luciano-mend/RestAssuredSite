package br.luciano.rest.refac;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import br.luciano.rest.core.BaseTest;
import br.luciano.rest.utils.SiteUtils;

public class ContasTest extends BaseTest{

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
			.put("/contas/{contaId}", SiteUtils.getIdContaPeloNome("Conta para alterar"))
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
}

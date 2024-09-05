package br.luciano.rest.refac;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import br.luciano.rest.core.BaseTest;
import io.restassured.RestAssured;

public class SaldoTest extends BaseTest{

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
	public void deveCalcularSaldoContas() {
		Integer contaId = getIdContaPeloNome("Conta para saldo");
		given()
		.when()
			.get("/saldo")
		.then()
			.statusCode(200)
			.body("$", hasSize(4))
			.body("conta", hasItem("Conta para saldo"))
			.body("find{it.conta_id == " + contaId + "}.saldo", is("534.00"))
		;
	}
	
	public Integer getIdContaPeloNome(String nomeConta) {
		return RestAssured.get("/contas?nome={nomeConta}", nomeConta).then().extract().path("id[0]");
	}
	
}

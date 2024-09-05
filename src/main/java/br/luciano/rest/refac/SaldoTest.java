package br.luciano.rest.refac;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import org.junit.Test;

import br.luciano.rest.core.BaseTest;
import br.luciano.rest.utils.SiteUtils;

public class SaldoTest extends BaseTest{

	@Test
	public void deveCalcularSaldoContas() {
		Integer contaId = SiteUtils.getIdContaPeloNome("Conta para saldo");
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
}

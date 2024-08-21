package br.luciano.rest.tests;

import static io.restassured.RestAssured.given;

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
}

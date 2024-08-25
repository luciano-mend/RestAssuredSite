package br.luciano.rest.tests;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import br.luciano.rest.core.BaseTest;
import br.luciano.rest.model.Movimentacao;


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
	
	@Test
	public void deveInserirMovimentacaoComSucesso() {
		Movimentacao movimentacao = getMovimentacaoValida();		
		
		given()
			.header("Authorization", "JWT " + token)
			.body(movimentacao)
		.when()
			.post("/transacoes")
		.then()
			.statusCode(201)
			.body("descricao", is("Descricao da movimentacao"))
			.body("status", is(true))
		;
	}
	
	@Test
	public void deveValidarCamposMovimentacao() {
		
		given()
			.header("Authorization", "JWT " + token)
			.body("{}")
		.when()
			.post("/transacoes")
		.then()
			.statusCode(400)
			.body("$", hasSize(8))
			.body("msg", hasItems("Data da Movimentação é obrigatório",
					"Data do pagamento é obrigatório",
					"Descrição é obrigatório",
					"Interessado é obrigatório",
					"Valor é obrigatório",
					"Valor deve ser um número",
					"Conta é obrigatório",
					"Situação é obrigatório"))
		;
	}
	
	@Test
	public void naoDeveCadastrarMovimentacaoFutura() {
		Movimentacao movimentacao = getMovimentacaoValida();
		movimentacao.setData_transacao("01/08/2200");
		
		given()
			.header("Authorization", "JWT " + token)
			.body(movimentacao)
		.when()
			.post("/transacoes")
		.then()
			.statusCode(400)
			.body("$", hasSize(1))
			.body("msg[0]", is("Data da Movimentação deve ser menor ou igual à data atual"))
			.body("msg", hasItem("Data da Movimentação deve ser menor ou igual à data atual"))
			.body("value", notNullValue())
		;
	}

	private Movimentacao getMovimentacaoValida() {
		Movimentacao movimentacao = new Movimentacao();
		movimentacao.setConta_id(2237412);
		movimentacao.setDescricao("Descricao da movimentacao");
		movimentacao.setEnvolvido("Envolvido da movimentacao");
		movimentacao.setTipo("REC");
		movimentacao.setData_transacao("01/08/2024");
		movimentacao.setData_pagamento("10/08/2024");
		movimentacao.setValor(100f);
		movimentacao.setStatus(true);
		return movimentacao;
	}
	
	@Test
	public void naoDeveRemoverContaComMovimentacao() {
		given()
			.header("Authorization", "JWT " + token)
		.when()
			.delete("/contas/2237412")
		.then()
			.statusCode(500)
			.body("constraint", is("transacoes_conta_id_foreign"))
		;
	}
	
	@Test
	public void deveCalcularSaldoContas() {
		given()
			.header("Authorization", "JWT " + token)
		.when()
			.get("/saldo")
		.then()
			.statusCode(200)
			.body("conta", hasItem("conta alterada"))
			.body("saldo", hasItem("100.00"))
			.body("find{it.conta_id == 2237412}.saldo", is("100.00"))
		;
	}
}

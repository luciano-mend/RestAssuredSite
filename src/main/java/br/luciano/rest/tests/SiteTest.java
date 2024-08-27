package br.luciano.rest.tests;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import br.luciano.rest.core.BaseTest;
import br.luciano.rest.model.Movimentacao;
import br.luciano.rest.utils.DataUtils;
import io.restassured.RestAssured;
import io.restassured.specification.FilterableRequestSpecification;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SiteTest extends BaseTest{
	private static String contaName = "Conta " + System.nanoTime();
	private static Integer contaId;
	private static Integer movimentacaoId;
	
	
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
	}

	@Test
	public void t11_naoDeveAcessarSemToken() {
		//manipula os dados, removendo dados do header
		FilterableRequestSpecification req = (FilterableRequestSpecification) RestAssured.requestSpecification;
		req.removeHeader("Authorization");
		
		given()
		.when()
			.get("/contas")
		.then()
			.statusCode(401)
		;
	}
	
	@Test
	public void t02_deveIncluriContaComSucesso() {
		Map<String, String> jsonBody = new HashMap<>();
		jsonBody.put("nome", contaName);
		
		contaId = given()
			.body(jsonBody)
		.when()
			.post("/contas")
		.then()
			.statusCode(201)
			.extract().path("id")
		;
		
	}
	
	@Test
	public void t03_deveAlterarContaComSucesso() {
		Map<String, String> jsonBody = new HashMap<>();
		jsonBody.put("nome", contaName + " alterada");
		
		given()
			.body(jsonBody)
		.when()
			.put("/contas/{contaId}", contaId)
		.then()
			.statusCode(200)
			.body("nome", is(contaName + " alterada"))
		;
		
	}
	
	@Test
	public void t04_naoDeveInserirContaComMesmoNome() {
		Map<String, String> jsonBody = new HashMap<>();
		jsonBody.put("nome", contaName + " alterada");
		
		given()
			.body(jsonBody)
		.when()
			.post("/contas")
		.then()
			.statusCode(400)
			.body("error", is("Já existe uma conta com esse nome!"))
		;
		
	}
	
	@Test
	public void t05_deveInserirMovimentacaoComSucesso() {
		Movimentacao movimentacao = getMovimentacaoValida();		
		
		movimentacaoId = given()
			.body(movimentacao)
		.when()
			.post("/transacoes")
		.then()
			.statusCode(201)
			.body("descricao", is("Descricao da movimentacao"))
			.body("status", is(true))
			.extract().path("id")
		;
	}
	
	@Test
	public void t06_deveValidarCamposMovimentacao() {
		
		given()
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
	public void t07_naoDeveCadastrarMovimentacaoFutura() {
		Movimentacao movimentacao = getMovimentacaoValida();
		movimentacao.setData_transacao(DataUtils.getDataDiferencaDias(2));
		
		given()
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
		movimentacao.setConta_id(contaId);
		movimentacao.setDescricao("Descricao da movimentacao");
		movimentacao.setEnvolvido("Envolvido da movimentacao");
		movimentacao.setTipo("REC");
		movimentacao.setData_transacao(DataUtils.getDataDiferencaDias(-1));
		movimentacao.setData_pagamento(DataUtils.getDataDiferencaDias(5));
		movimentacao.setValor(100f);
		movimentacao.setStatus(true);
		return movimentacao;
	}
	
	@Test
	public void t08_naoDeveRemoverContaComMovimentacao() {
		given()
			.pathParam("contaId", contaId)
		.when()
			.delete("/contas/{contaId}")
		.then()
			.statusCode(500)
			.body("constraint", is("transacoes_conta_id_foreign"))
		;
	}
	
	@Test
	public void t09_deveCalcularSaldoContas() {
		given()
		.when()
			.get("/saldo")
		.then()
			.statusCode(200)
			.body("conta", hasItem("conta alterada"))
			.body("saldo", hasItem("100.00"))
			.body("find{it.conta_id == " + contaId + "}.saldo", is("100.00"))
		;
	}
	
	@Test
	public void t10_deveRemoverMovimentacao() {
		given()
			.pathParam("movimentacaoId", movimentacaoId)
		.when()
			.delete("/transacoes/{movimentacaoId}")
		.then()
			.statusCode(204)
		;
	}
}

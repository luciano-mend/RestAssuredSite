package br.luciano.rest.refac;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import br.luciano.rest.core.BaseTest;
import br.luciano.rest.model.Movimentacao;
import br.luciano.rest.utils.DataUtils;
import io.restassured.RestAssured;

public class MovimentacaoTest extends BaseTest{

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
	public void deveInserirMovimentacaoComSucesso() {
		Movimentacao movimentacao = getMovimentacaoValida("Conta para movimentacoes");		
		
		given()
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
		Movimentacao movimentacao = getMovimentacaoValida("Conta para movimentacoes");
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
	
	@Test
	public void naoDeveRemoverContaComMovimentacao() {
		given()
			.pathParam("contaId", getIdContaPeloNome("Conta com movimentacao"))
		.when()
			.delete("/contas/{contaId}")
		.then()
			.statusCode(500)
			.body("constraint", is("transacoes_conta_id_foreign"))
		;
	}
	
	@Test
	public void deveRemoverMovimentacao() {
		given()
			.pathParam("movimentacaoId", getIdMovimentacaoPeloNome("Movimentacao para exclusao"))
		.when()
			.delete("/transacoes/{movimentacaoId}")
		.then()
			.statusCode(204)
		;
	}
	
	public Integer getIdContaPeloNome(String nomeConta) {
		return RestAssured.get("/contas?nome={nomeConta}", nomeConta).then().extract().path("id[0]");
	}
	
	public Integer getIdMovimentacaoPeloNome(String movimentacaoNome) {
		return RestAssured.get("/transacoes?descricao={movimentacaoNome}", movimentacaoNome).then().extract().path("id[0]");
	}
	
	private Movimentacao getMovimentacaoValida(String nomeConta) {
		Movimentacao movimentacao = new Movimentacao();
		movimentacao.setConta_id(getIdContaPeloNome(nomeConta));
		movimentacao.setDescricao("Descricao da movimentacao");
		movimentacao.setEnvolvido("Envolvido da movimentacao");
		movimentacao.setTipo("REC");
		movimentacao.setData_transacao(DataUtils.getDataDiferencaDias(-1));
		movimentacao.setData_pagamento(DataUtils.getDataDiferencaDias(5));
		movimentacao.setValor(100f);
		movimentacao.setStatus(true);
		return movimentacao;
	}
	
}

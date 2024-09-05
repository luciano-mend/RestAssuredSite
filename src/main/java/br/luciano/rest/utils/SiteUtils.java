package br.luciano.rest.utils;

import io.restassured.RestAssured;

public class SiteUtils {
	
	public static Integer getIdContaPeloNome(String nomeConta) {
		return RestAssured.get("/contas?nome={nomeConta}", nomeConta).then().extract().path("id[0]");
	}
	
	public static Integer getIdMovimentacaoPeloNome(String movimentacaoNome) {
		return RestAssured.get("/transacoes?descricao={movimentacaoNome}", movimentacaoNome).then().extract().path("id[0]");
	}
	

}

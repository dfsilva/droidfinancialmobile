package br.com.diego.financialmobile.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Custo {

	public static final String[] COLUNAS = new String[] { Custo.ID_CUSTO,
			Custo.DESCRICAO_GASTO, Custo.ID_CATEGORIA_GASTO,
			Custo.DATA_VENCIMENTO, Custo.VALOR_PARCELA, Custo.PARCELADO,
			Custo.TIPO_REPETICAO, Custo.QTD_PARCELAS };

	public static final String ID_CUSTO = "idCusto";
	public static final String DESCRICAO_GASTO = "descricaoGasto";
	public static final String ID_CATEGORIA_GASTO = "idCategoriaGasto";
	public static final String DATA_VENCIMENTO = "dataVencimento";
	public static final String VALOR_PARCELA = "valorParcela";

	public static final String PARCELADO = "parcelado";
	public static final String TIPO_REPETICAO = "tipoRepeticao";
	public static final String QTD_PARCELAS = "qtdParcelas";

	public long idCusto;
	public String descricaoGasto;
	public int idCategoriaGasto;

	public Date dataVencimento;
	public String valorParcela;

	public boolean parcelado;
	public int tipoRepeticao;
	public int qtdParcelas;

	public Custo() {
	}

	public static List<Custo> fromJsonArray(JSONArray arr) {
		List<Custo> retorno = new ArrayList<Custo>();
		if (arr != null) {
			for (int i = 0; i < arr.length(); i++) {
				try {
					retorno.add(fromJsonObject(arr.getJSONObject(i)));
				} catch (JSONException e) {
					// do nothing
				}
			}
		}
		return retorno;
	}

	public static Custo fromJsonObject(JSONObject obj) {
		Custo retorno = new Custo();
		if (obj != null) {
			try {
				retorno.idCusto = obj.getLong("idCusto");
				retorno.descricaoGasto = obj.getString("descricaoGasto");
			} catch (Exception e) {
				// do nothing
			}
		}
		return retorno;
	}

	@Override
	public String toString() {
		return this.descricaoGasto;
	}
}

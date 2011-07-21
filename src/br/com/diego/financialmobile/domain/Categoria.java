package br.com.diego.financialmobile.domain;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Categoria {

	public static final String[] COLUNAS = new String[] { Categoria.ID_CATEGORIA,
			Categoria.DESC_CATEGORIA };

	public static final String ID_CATEGORIA = "idCategoria";
	public static final String DESC_CATEGORIA = "descCategoria";

	public int idCategoria;
	public String descCategoria;

	public Categoria() {
	}

	public static List<Categoria> fromJsonArray(JSONArray arr) {
		List<Categoria> retorno = new ArrayList<Categoria>();
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

	public static Categoria fromJsonObject(JSONObject obj) {
		Categoria retorno = new Categoria();
		if (obj != null) {
			try {
				retorno.idCategoria = obj.getInt("idCategoria");
				retorno.descCategoria = obj.getString("descCategoria");
			} catch (Exception e) {
				// do nothing
			}

		}
		return retorno;
	}

	@Override
	public String toString() {
		return this.descCategoria;
	}
}

package br.com.diego.financialmobile.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import br.com.diego.financialmobile.domain.Categoria;
import br.com.diego.financialmobile.domain.Custo;

public class DataBaseUtils {

	private static final String SCRIPT_DATABASE_DELETE[] = new String[] {
			"DROP TABLE IF EXISTS categoria", "DROP TABLE IF EXISTS custo" };

	private static final String[] SCRIPT_DATABASE_CREATE = new String[] {
			"create table categoria ( " + Categoria.ID_CATEGORIA
					+ " integer primary key, " + Categoria.DESC_CATEGORIA
					+ " text not null);",
			"create table custo ( " + Custo.ID_CUSTO
					+ " integer primary key autoincrement, " + Custo.DESCRICAO_GASTO
					+ " text not null, " + Custo.ID_CATEGORIA_GASTO
					+ " integer not null, " + Custo.DATA_VENCIMENTO
					+ " numeric not null, " + Custo.VALOR_PARCELA
					+ " numeric not null, " + Custo.PARCELADO
					+ " numeric not null default 0, " + Custo.TIPO_REPETICAO
					+ " integer null, " + Custo.QTD_PARCELAS
					+ " integer null);" };

	private static final int VERSAO_BANCO = 3;

	public DataBaseUtils(Context ctx) {
		SQLiteHelper helper = new SQLiteHelper(ctx, Constantes.NOME_BANCO,
				VERSAO_BANCO, SCRIPT_DATABASE_CREATE, SCRIPT_DATABASE_DELETE);
		SQLiteDatabase db = helper.getWritableDatabase();
		db.close();
	}
}

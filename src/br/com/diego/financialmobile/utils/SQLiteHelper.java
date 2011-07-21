package br.com.diego.financialmobile.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Implementacao de SQLiteOpenHelper
 * 
 * Classe utilitaria para abrir, criar, e atualizar o banco de dados
 * 
 * @author ricardo
 */
public class SQLiteHelper extends SQLiteOpenHelper {

	private static final String CATEGORIA = "financial_mobile";

	private String[] scriptSQLCreate;
	private String[] scriptSQLDelete;

	/**
	 * Cria uma instancia de SQLiteHelper
	 * 
	 * @param context
	 * @param nomeBanco
	 *            nome do banco de dados
	 * @param versaoBanco
	 *            versao do banco de dados (se for diferente a para atualizar)
	 * @param scriptSQLCreate
	 *            SQL com o create table..
	 * @param scriptSQLDelete
	 *            SQL com o drop table...
	 */
	public SQLiteHelper(Context context, String nomeBanco, int versaoBanco,
			String[] scriptSQLCreate, String[] scriptSQLDelete) {
		super(context, nomeBanco, null, versaoBanco);
		this.scriptSQLCreate = scriptSQLCreate;
		this.scriptSQLDelete = scriptSQLDelete;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(CATEGORIA, "Criando banco com sql");
		int qtdeScripts = scriptSQLCreate.length;

		// Executa cada sql passado como parametro
		for (int i = 0; i < qtdeScripts; i++) {
			String sql = scriptSQLCreate[i];
			Log.i(CATEGORIA, sql);
			// Cria o banco de dados executando o script de criacao
			db.execSQL(sql);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int versaoAntiga, int novaVersao) {
		Log.w(CATEGORIA, "Atualizando da versao " + versaoAntiga + " para "
				+ novaVersao + ". Todos os registros serao deletados.");
		for (int i = 0; i < scriptSQLDelete.length; i++) {
			Log.i(CATEGORIA, scriptSQLDelete[i]);
			// Deleta as tabelas...
			db.execSQL(scriptSQLDelete[i]);
		}
		// Cria novamente...
		onCreate(db);
	}
}
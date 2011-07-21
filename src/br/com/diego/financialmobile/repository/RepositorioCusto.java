package br.com.diego.financialmobile.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
import br.com.diego.financialmobile.domain.Custo;
import br.com.diego.financialmobile.utils.Constantes;

public class RepositorioCusto {

	private static final String CATEGORIA = "FINANCIAL_MOBILE";

	public static final String NOME_TABELA = "custo";

	protected SQLiteDatabase db;

	protected RepositorioCusto() {
	}

	public RepositorioCusto(Context ctx) {
		db = ctx.openOrCreateDatabase(Constantes.NOME_BANCO,
				Context.MODE_PRIVATE, null);
	}

	public long salvar(Custo custo) {
		long id = custo.idCusto;

		if (id != 0) {
			atualizar(custo);
		} else {
			id = inserir(custo);
		}
		return id;
	}

	public long inserir(Custo custo) {
		ContentValues values = new ContentValues();
		values.put(Custo.DESCRICAO_GASTO, custo.descricaoGasto);
		values.put(Custo.ID_CATEGORIA_GASTO, custo.idCategoriaGasto);
		values.put(Custo.DATA_VENCIMENTO, custo.dataVencimento.getTime());
		values.put(Custo.VALOR_PARCELA, custo.valorParcela);
		values.put(Custo.PARCELADO, custo.parcelado);
		values.put(Custo.TIPO_REPETICAO, custo.tipoRepeticao);
		values.put(Custo.QTD_PARCELAS, custo.qtdParcelas);
		long id = inserir(values);
		return id;
	}

	public long inserir(ContentValues valores) {
		long id = db.insert(NOME_TABELA, "", valores);
		return id;
	}

	public int atualizar(Custo custo) {
		ContentValues values = new ContentValues();
		values.put(Custo.DESCRICAO_GASTO, custo.descricaoGasto);
		values.put(Custo.ID_CATEGORIA_GASTO, custo.idCategoriaGasto);
		values.put(Custo.DATA_VENCIMENTO, custo.dataVencimento.getTime());
		values.put(Custo.VALOR_PARCELA, custo.valorParcela);
		values.put(Custo.PARCELADO, custo.parcelado);
		values.put(Custo.TIPO_REPETICAO, custo.tipoRepeticao);
		values.put(Custo.QTD_PARCELAS, custo.qtdParcelas);

		String _id = String.valueOf(custo.idCusto);

		String where = Custo.ID_CUSTO + "=?";
		String[] whereArgs = new String[] { _id };

		int count = atualizar(values, where, whereArgs);

		return count;
	}

	public int atualizar(ContentValues valores, String where, String[] whereArgs) {
		int count = db.update(NOME_TABELA, valores, where, whereArgs);
		Log.i(CATEGORIA, "Atualizou [" + count + "] registros");
		return count;
	}

	public int deletar(long id) {
		String where = Custo.ID_CUSTO + "=?";
		String _id = String.valueOf(id);
		String[] whereArgs = new String[] { _id };
		int count = deletar(where, whereArgs);
		return count;
	}

	public int deletar(String where, String[] whereArgs) {
		int count = db.delete(NOME_TABELA, where, whereArgs);
		Log.i(CATEGORIA, "Deletou [" + count + "] registros");
		return count;
	}

	public Custo buscarCusto(long id) {
		Cursor c = db.query(true, NOME_TABELA, Custo.COLUNAS, Custo.ID_CUSTO
				+ "=" + id, null, null, null, null, null);
		if (c.getCount() > 0) {
			c.moveToFirst();
			Custo custo = new Custo();
			custo.idCusto = c.getLong(0);
			custo.descricaoGasto = c.getString(1);
			custo.idCategoriaGasto = c.getInt(2);
			custo.dataVencimento = new Date(c.getLong(3));
			custo.valorParcela = c.getString(4);
			custo.parcelado = c.getInt(5) == 0 ? false : true;
			custo.tipoRepeticao = c.getInt(6);
			custo.qtdParcelas = c.getInt(7);
			return custo;
		}
		return null;
	}

	// Retorna um cursor com todos os carros
	public Cursor getCursor() {
		try {
			return db.query(NOME_TABELA, Custo.COLUNAS, null, null, null, null,
					null, null);
		} catch (SQLException e) {
			Log.e(CATEGORIA, "Erro ao buscar os carros: " + e.toString());
			return null;
		}
	}

	public int countCustos() {
		return getCursor().getCount();
	}

	public List<Custo> getCustos() {
		Cursor c = getCursor();

		List<Custo> custos = new ArrayList<Custo>();

		if (c.moveToFirst()) {

			// Recupera os indices das colunas
			int idxIDCusto = c.getColumnIndex(Custo.ID_CUSTO);
			int idxDescricao = c.getColumnIndex(Custo.DESCRICAO_GASTO);
			int idxCategoria = c.getColumnIndex(Custo.ID_CATEGORIA_GASTO);
			int idxDtVencimento = c.getColumnIndex(Custo.DATA_VENCIMENTO);
			int idxVrParcela = c.getColumnIndex(Custo.VALOR_PARCELA);
			int idxParcelado = c.getColumnIndex(Custo.PARCELADO);
			int idxTipoRepeticao = c.getColumnIndex(Custo.TIPO_REPETICAO);
			int idxQtdParcelas = c.getColumnIndex(Custo.QTD_PARCELAS);

			// Loop ate o final
			do {
				Custo custo = new Custo();
				custos.add(custo);
				custo.idCusto = c.getLong(idxIDCusto);
				custo.descricaoGasto = c.getString(idxDescricao);
				custo.idCategoriaGasto = c.getInt(idxCategoria);
				custo.dataVencimento = new Date(c.getLong(idxDtVencimento));
				custo.valorParcela = c.getString(idxVrParcela);
				custo.parcelado = c.getInt(idxParcelado) == 0 ? false : true;
				custo.tipoRepeticao = c.getInt(idxTipoRepeticao);
				custo.qtdParcelas = c.getInt(idxQtdParcelas);

			} while (c.moveToNext());
		}

		return custos;
	}

	public Cursor query(SQLiteQueryBuilder queryBuilder, String[] projection,
			String selection, String[] selectionArgs, String groupBy,
			String having, String orderBy) {
		Cursor c = queryBuilder.query(this.db, projection, selection,
				selectionArgs, groupBy, having, orderBy);
		return c;
	}

	public void fechar() {
		if (db != null) {
			db.close();
		}
	}
}

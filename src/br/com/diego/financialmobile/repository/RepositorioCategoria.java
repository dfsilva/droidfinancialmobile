package br.com.diego.financialmobile.repository;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
import br.com.diego.financialmobile.domain.Categoria;
import br.com.diego.financialmobile.utils.Constantes;

/**
 * <pre>
 * Reposit�rio para carros que utiliza o SQLite internamente
 * 
 * Para visualizar o banco pelo adb shell:
 * 
 * &gt;&gt; sqlite3 /data/data/br.livro.android.exemplos.banco/databases/BancoCarro
 * 
 * &gt;&gt; Mais info dos comandos em: http://www.sqlite.org/sqlite.html
 * 
 * &gt;&gt; .exit para sair
 * 
 * </pre>
 * 
 * @author rlecheta
 * 
 */
public class RepositorioCategoria {

	private static final String CATEGORIA = "FINANCIAL_MOBILE";

	// Nome da tabela
	public static final String NOME_TABELA = "categoria";

	protected SQLiteDatabase db;

	protected RepositorioCategoria() {
		// Apenas para criar uma subclasse...
	}

	public RepositorioCategoria(Context ctx) {
		// Abre o banco de dados ja existente
		db = ctx.openOrCreateDatabase(Constantes.NOME_BANCO,
				Context.MODE_PRIVATE, null);
	}

	// Salva o carro, insere um novo ou atualiza
	public long salvar(Categoria categoria) {
		long id = categoria.idCategoria;

		if (id != 0) {
			atualizar(categoria);
		} else {
			// Insere novo
			id = inserir(categoria);
		}
		return id;
	}

	// Insere um novo carro
	public long inserir(Categoria categoria) {
		ContentValues values = new ContentValues();
		values.put(Categoria.DESC_CATEGORIA, categoria.descCategoria);
		long id = inserir(values);
		return id;
	}

	// Insere um novo carro
	public long inserir(ContentValues valores) {
		long id = db.insert(NOME_TABELA, "", valores);
		return id;
	}

	// Atualiza o carro no banco. O id do carro � utilizado.
	public int atualizar(Categoria categoria) {
		ContentValues values = new ContentValues();
		values.put(Categoria.DESC_CATEGORIA, categoria.descCategoria);

		String _id = String.valueOf(categoria.idCategoria);

		String where = Categoria.ID_CATEGORIA + "=?";
		String[] whereArgs = new String[] { _id };

		int count = atualizar(values, where, whereArgs);

		return count;
	}

	// Atualiza o carro com os valores abaixo
	// A cl�usula where � utilizada para identificar o carro a ser atualizado
	public int atualizar(ContentValues valores, String where, String[] whereArgs) {
		int count = db.update(NOME_TABELA, valores, where, whereArgs);
		Log.i(CATEGORIA, "Atualizou [" + count + "] registros");
		return count;
	}

	// Deleta o carro com o id fornecido
	public int deletar(long id) {
		String where = Categoria.ID_CATEGORIA + "=?";

		String _id = String.valueOf(id);
		String[] whereArgs = new String[] { _id };

		int count = deletar(where, whereArgs);

		return count;
	}

	// Deleta o carro com os argumentos fornecidos
	public int deletar(String where, String[] whereArgs) {
		int count = db.delete(NOME_TABELA, where, whereArgs);
		Log.i(CATEGORIA, "Deletou [" + count + "] registros");
		return count;
	}

	public void atualizarTodosRegistros(List<Categoria> cat) {
		db.delete(NOME_TABELA, null, null);
		for (Categoria categoria : cat) {
			inserir(categoria);
		}
	}

	// Busca o carro pelo id
	public Categoria buscarCategoria(long id) {
		// select * from carro where _id=?
		Cursor c = db
				.query(true, NOME_TABELA, Categoria.COLUNAS,
						Categoria.ID_CATEGORIA + "=" + id, null, null, null,
						null, null);

		if (c.getCount() > 0) {

			// Posicinoa no primeiro elemento do cursor
			c.moveToFirst();

			Categoria categoria = new Categoria();

			// Le os dados
			categoria.idCategoria = c.getInt(0);
			categoria.descCategoria = c.getString(1);

			return categoria;
		}

		return null;
	}

	// Retorna um cursor com todos os carros
	public Cursor getCursor() {
		try {
			// select * from categorias
			return db.query(NOME_TABELA, new String[] { Categoria.ID_CATEGORIA,
					Categoria.DESC_CATEGORIA }, null, null, null, null, null,
					null);
		} catch (SQLException e) {
			Log.e(CATEGORIA, "Erro ao buscar os categorias: " + e.toString());
			return null;
		}
	}

	public int countCategorias() {
		return getCursor().getCount();
	}

	// Retorna uma lista com todos os carros
	public List<Categoria> listarCategorias() {
		Cursor c = getCursor();

		List<Categoria> categorias = new ArrayList<Categoria>();

		if (c.moveToFirst()) {

			// Recupera os indices das colunas
			int idxId = c.getColumnIndex(Categoria.ID_CATEGORIA);
			int idxDesc = c.getColumnIndex(Categoria.DESC_CATEGORIA);

			// Loop ate o final
			do {
				Categoria cat = new Categoria();
				categorias.add(cat);

				// recupera os atributos de carro
				cat.idCategoria = c.getInt(idxId);
				cat.descCategoria = c.getString(idxDesc);

			} while (c.moveToNext());
		}

		return categorias;
	}

	// Busca o carro pelo nome "select * from carro where nome=?"
	public Categoria buscarCategoriaPelaDescricao(String nome) {
		Categoria cat = null;

		try {
			// Idem a: SELECT _id,nome,placa,ano from CARRO where nome = ?
			Cursor c = db.query(NOME_TABELA, Categoria.COLUNAS,
					Categoria.DESC_CATEGORIA + "='" + nome + "'", null, null,
					null, null);

			// Se encontrou...
			if (c.moveToNext()) {

				cat = new Categoria();

				// utiliza os metodos getLong(), getString(), getInt(), etc para
				// recuperar os valores
				cat.idCategoria = c.getInt(0);
				cat.descCategoria = c.getString(1);
			}
		} catch (SQLException e) {
			Log.e(CATEGORIA,
					"Erro ao buscar o carro pelo nome: " + e.toString());
			return null;
		}

		return cat;
	}

	// Busca um objeto utilizando as configuracoes definidas no
	// SQLiteQueryBuilder
	// Utilizado pelo Content Provider de categoria
	public Cursor query(SQLiteQueryBuilder queryBuilder, String[] projection,
			String selection, String[] selectionArgs, String groupBy,
			String having, String orderBy) {
		Cursor c = queryBuilder.query(this.db, projection, selection,
				selectionArgs, groupBy, having, orderBy);
		return c;
	}

	// Fecha o banco
	public void fechar() {
		// fecha o banco de dados
		if (db != null) {
			db.close();
		}
	}
}

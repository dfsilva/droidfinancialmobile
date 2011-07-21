package br.com.diego.financialmobile;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import br.com.diego.financialmobile.dialog.DialogUtils;
import br.com.diego.financialmobile.dialog.Execucao;
import br.com.diego.financialmobile.domain.Categoria;
import br.com.diego.financialmobile.domain.Custo;
import br.com.diego.financialmobile.domain.enums.TipoRepeticao;
import br.com.diego.financialmobile.repository.RepositorioCategoria;
import br.com.diego.financialmobile.repository.RepositorioCusto;
import br.com.diego.financialmobile.utils.Constantes;
import br.com.diego.financialmobile.utils.HttpClientUtils;

public class ManterCusto extends Activity implements Runnable {

	private RepositorioCategoria repositorioCategoria;
	private List<Categoria> categorias;
	private RepositorioCusto repositorioCusto;

	private ProgressDialog dialog;

	private static final int SALVAR = 1;
	private static final int CANCELAR = 2;

	private boolean customTitleSupported;
	private ProgressBar pb;
	private TextView titleText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		customTitleSupported = getWindow().requestFeature(
				Window.FEATURE_CUSTOM_TITLE);

		setContentView(R.layout.manter_custo);
		
		customTitleBar();
		
		repositorioCategoria = new RepositorioCategoria(this);
		repositorioCusto = new RepositorioCusto(this);
		
		CheckBox chkParcelado = (CheckBox) findViewById(R.id.checkParcelado);

		chkParcelado.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					habilitaCamposTela();
				} else {
					desabilitaCampos();
				}
			}
		});

		try {
			popularListaCategoriasBd();
		} catch (Exception e1) {
			Toast.makeText(this, e1.getMessage(), Toast.LENGTH_LONG).show();
		}

		popularComboCategoria();
		populaComboQtdParcelas();
		populaComboTipoRepecicao();
		sincronizarCategoriasComServidorWeb();
	}

	public void customTitleBar() {

		if (customTitleSupported) {
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
					R.layout.custom_title_bar);
			titleText = (TextView) findViewById(R.id.titleMsg);
			pb = (ProgressBar) findViewById(R.id.leadProgressBar);
			pb.setVisibility(ProgressBar.GONE);
		}
	}

	public void setaMensagem(String msg) {
		pb.setVisibility(View.VISIBLE);
		titleText.setText(msg);
	}

	public void removeMsg() {
		pb.setVisibility(View.GONE);
		titleText.setText("");
	}

	private void sincronizarCategoriasComServidorWeb() {
		setaMensagem("Atualizando categorias.");
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Looper.prepare();
					popularListaCategoriasWeb();
					ManterCusto.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							popularComboCategoria();
							removeMsg();
							Toast.makeText(ManterCusto.this, "atualizado...",
									Toast.LENGTH_SHORT).show();
						}
					});
				} catch (final Exception e) {
					ManterCusto.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							removeMsg();
							Toast.makeText(ManterCusto.this, e.getMessage(),
									Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		}).start();
	}

	private void populaComboQtdParcelas() {
		Spinner combo = (Spinner) findViewById(R.id.spQtdParcelas);
		ArrayAdapter<Integer> adaptador = new ArrayAdapter<Integer>(this,
				android.R.layout.simple_spinner_item, Constantes.qtdParcelas);
		combo.setAdapter(adaptador);
	}

	private void populaComboTipoRepecicao() {
		Spinner combo = (Spinner) findViewById(R.id.spTipoRepeticao);
		ArrayAdapter<TipoRepeticao> adaptador = new ArrayAdapter<TipoRepeticao>(
				this, android.R.layout.simple_spinner_item,
				TipoRepeticao.values());
		combo.setAdapter(adaptador);
	}

	private void habilitaCamposTela() {
		TextView tx1 = (TextView) findViewById(R.id.tx_qtd_parcelas);
		tx1.setVisibility(View.VISIBLE);
		TextView tx2 = (TextView) findViewById(R.id.tx_tipo_repeticao);
		tx2.setVisibility(View.VISIBLE);
		Spinner sp1 = (Spinner) findViewById(R.id.spQtdParcelas);
		sp1.setVisibility(View.VISIBLE);
		Spinner sp2 = (Spinner) findViewById(R.id.spTipoRepeticao);
		sp2.setVisibility(View.VISIBLE);
	}

	private void desabilitaCampos() {
		TextView tx1 = (TextView) findViewById(R.id.tx_qtd_parcelas);
		tx1.setVisibility(View.INVISIBLE);
		TextView tx2 = (TextView) findViewById(R.id.tx_tipo_repeticao);
		tx2.setVisibility(View.INVISIBLE);
		Spinner sp1 = (Spinner) findViewById(R.id.spQtdParcelas);
		sp1.setVisibility(View.INVISIBLE);
		Spinner sp2 = (Spinner) findViewById(R.id.spTipoRepeticao);
		sp2.setVisibility(View.INVISIBLE);
	}

	private void popularListaCategoriasBd() throws Exception {
		try {
			categorias = repositorioCategoria.listarCategorias();
			if (categorias == null || categorias.isEmpty()) {
				throw new Exception(
						"Nenhuma categoria registrada no banco de dados do aparelho.");
			}
		} catch (Exception e) {
			throw e;
		}
	}

	private void popularListaCategoriasWeb() throws Exception {
		try {
			JSONObject json = HttpClientUtils.doPostJsonLogedUser(
					Constantes.URL_APP_WEB + "in/ccategoria/getCategorias",
					this, null);
			categorias = Categoria.fromJsonArray(json
					.getJSONArray("categorias"));
			if (categorias == null) {
				throw new Exception();
			} else {
				if (categorias.size() != repositorioCategoria.countCategorias()) {
					repositorioCategoria.atualizarTodosRegistros(categorias);
				}
			}
		} catch (Exception e) {
			throw new Exception("Erro ao resgatar categorias do banco local: "
					+ e.getMessage());
		}

	}

	private void popularComboCategoria() {
		try {
			Spinner combo = (Spinner) findViewById(R.id.spCategoriaGasto);
			ArrayAdapter<Categoria> adaptador = new ArrayAdapter<Categoria>(
					this, android.R.layout.simple_spinner_item, categorias);
			combo.setAdapter(adaptador);
		} catch (Exception e) {
			DialogUtils.showAlertError(ManterCusto.this,
					"Erro ao popular combo de categorias: " + e.getMessage(),
					new Execucao() {
						@Override
						public void executarSim() {
							finish();
						}

						@Override
						public void executarNao() {
						}
					});
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, SALVAR, 0, "Salvar").setIcon(R.drawable.save);
		menu.add(0, CANCELAR, 0, "Cancelar").setIcon(R.drawable.cancel);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case SALVAR:
			dialog = ProgressDialog.show(this, "", "Salvando Gasto...", true);
			new Thread(this).start();
			break;
		case CANCELAR:
			DialogUtils.showConfirmDialog(this, new Execucao() {
				@Override
				public void executarSim() {
					finish();
				}

				@Override
				public void executarNao() {
					// do nothing;
				}
			}, "Confirma deixar esta funcionalidade?");
			break;
		}
		return true;
	}

	private void salvarGasto() throws Exception {
		EditText txGasto = (EditText) findViewById(R.id.tx_descricao_gasto);
		Spinner spCategoriaGasto = (Spinner) findViewById(R.id.spCategoriaGasto);
		DatePicker dtGasto = (DatePicker) findViewById(R.id.dt_gasto);
		EditText txValorGasto = (EditText) findViewById(R.id.tx_valor_gasto);
		CheckBox chkParcelado = (CheckBox) findViewById(R.id.checkParcelado);
		Spinner spQtdParcelas = (Spinner) findViewById(R.id.spQtdParcelas);
		Spinner spTipoRepeticao = (Spinner) findViewById(R.id.spTipoRepeticao);

		try {
			Map<String, Object> param = new HashMap<String, Object>();

			param.put("descricaoGasto", txGasto.getText().toString());
			param.put(
					"idCategoriaGasto",
					((Categoria) spCategoriaGasto.getSelectedItem()).idCategoria);
			param.put("dataVencimento", dtGasto.getDayOfMonth() + "/"
					+ (dtGasto.getMonth() + 1) + "/" + dtGasto.getYear());
			param.put("valorParcela", txValorGasto.getText().toString());
			param.put("lancamentoParcelado", chkParcelado.isChecked());

			if (chkParcelado.isChecked()) {
				param.put("tipoRepeticao", ((TipoRepeticao) spTipoRepeticao
						.getSelectedItem()).getNumero());
				param.put("qtdParcelas",
						(Integer) spQtdParcelas.getSelectedItem());
			}

			JSONObject json = HttpClientUtils.doPostJsonLogedUser(
					Constantes.URL_APP_WEB + "in/ccusto/cadastrarAlterar",
					this, param);

			if (!json.getBoolean("success")) {
				throw new Exception(
						"Custo não pode ser adicionado por um erro nao identificado!");
			}
		} catch (Exception e) {
			try {
				Custo c = new Custo();
				c.descricaoGasto = txGasto.getText().toString();
				Calendar cal = Calendar.getInstance();
				cal.set(dtGasto.getYear(), dtGasto.getMonth(),
						dtGasto.getDayOfMonth());
				c.dataVencimento = cal.getTime();
				c.valorParcela = txValorGasto.getText().toString();
				c.idCategoriaGasto = ((Categoria) spCategoriaGasto
						.getSelectedItem()).idCategoria;
				c.parcelado = chkParcelado.isChecked();

				if (chkParcelado.isChecked()) {
					c.tipoRepeticao = ((TipoRepeticao) spTipoRepeticao
							.getSelectedItem()).getNumero();
					c.qtdParcelas = (Integer) spQtdParcelas.getSelectedItem();
				}
				repositorioCusto.salvar(c);
			} catch (Exception e2) {
				Log.e("CADASTRO_CUSTO", e2.getMessage());
				throw new Exception(e.getMessage() + " : " + e2.getMessage());
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Fecha o banco
		repositorioCategoria.fechar();
	}

	@Override
	public void run() {
		try {
			Looper.prepare();
			salvarGasto();
			this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					dialog.dismiss();
					DialogUtils.showAlertInfo(ManterCusto.this,
							"Custo adicionado com sucesso!", new Execucao() {
								@Override
								public void executarSim() {
									finish();
								}
								@Override
								public void executarNao() {
									// do nothing
								}
							});
				}
			});

		} catch (final Exception e) {
			this.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					dialog.dismiss();
					DialogUtils.showAlertError(ManterCusto.this,
							e.getMessage(), null);
				}
			});
		}
	}
}

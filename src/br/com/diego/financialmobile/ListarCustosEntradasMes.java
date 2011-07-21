package br.com.diego.financialmobile;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import br.com.diego.financialmobile.dialog.DialogUtils;
import br.com.diego.financialmobile.dialog.Execucao;
import br.com.diego.financialmobile.domain.Custo;
import br.com.diego.financialmobile.repository.RepositorioCusto;
import br.com.diego.financialmobile.utils.Constantes;
import br.com.diego.financialmobile.utils.DataBaseUtils;
import br.com.diego.financialmobile.utils.HttpClientUtils;

public class ListarCustosEntradasMes extends Activity {

	private RepositorioCusto repositorioCusto;
	private ProgressDialog dialog;

	private String custoMesStr = "R$0,00";
	private String entradaMesStr = "R$0,00";;
	private String diffMesStr = "R$0,00";;
	private int qtdCustos;
	private BigDecimal diffMes = BigDecimal.ZERO;

	private boolean customTitleSupported;
	private ProgressBar pb;
	private TextView titleText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		customTitleSupported = getWindow().requestFeature(
				Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.listar_custo_entrada_mes);
		customTitleBar();

		if (isUserLoged()) {
			repositorioCusto = new RepositorioCusto(this);
			atribuirEventosTela();
			atualizarValoresServidor();
		} else {
			startActivity(new Intent(this, Login.class));
			finish();
		}

	}

	private boolean isUserLoged() {
		new DataBaseUtils(this);
		SharedPreferences pref = getSharedPreferences(Constantes.APP_NAME_KEY,
				0);
		String idUsuarioLogado = pref.getString(Constantes.ID_USUARIO_KEY, "");
		if ("".equals(idUsuarioLogado)) {
			return false;
		} else {
			SharedPreferences.Editor editor = pref.edit();
			editor.putString(Constantes.ID_USUARIO_TEMP_KEY, idUsuarioLogado);
			editor.commit();
			return true;
		}
	}

	public void customTitleBar() {
		if (customTitleSupported) {
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
					R.layout.custom_title_bar);
			titleText = (TextView) findViewById(R.id.titleMsg);
			pb = (ProgressBar) findViewById(R.id.leadProgressBar);
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

	private void atribuirEventosTela() {
		Button btnSinc = (Button) findViewById(R.id.btnSincronize);
		btnSinc.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog = ProgressDialog.show(ListarCustosEntradasMes.this, "",
						"Sincronizando Custos...", true);

				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Looper.prepare();
							sincronizarCustos();
							ListarCustosEntradasMes.this
									.runOnUiThread(new Runnable() {
										@Override
										public void run() {
											dialog.dismiss();
											DialogUtils
													.showAlertInfo(
															ListarCustosEntradasMes.this,
															"Sincronização concluída!",
															new Execucao() {
																@Override
																public void executarSim() {
																	atualizarValoresServidor();

																}

																@Override
																public void executarNao() {
																}
															});
										}
									});
						} catch (final Exception e) {
							ListarCustosEntradasMes.this
									.runOnUiThread(new Runnable() {
										@Override
										public void run() {
											dialog.dismiss();
											DialogUtils
													.showAlertError(
															ListarCustosEntradasMes.this,
															e.getMessage(),
															new Execucao() {
																@Override
																public void executarSim() {
																	atualizarValoresServidor();
																}

																@Override
																public void executarNao() {
																}
															});
										}
									});
						}
					}
				}).start();
			}
		});
	}

	/**
	 * 
	 */
	private void atualizarValoresServidor() {
		setaMensagem("Sincronizando...");
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Looper.prepare();
					populaValoresServidor();
					ListarCustosEntradasMes.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							atribuirValoresDoServidorNaTela();
							removeMsg();
							Toast.makeText(ListarCustosEntradasMes.this,
									"Atualizado...", Toast.LENGTH_SHORT).show();
						}
					});
				} catch (final Exception e) {
					ListarCustosEntradasMes.this.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							atribuirValoresDoServidorNaTela();
							removeMsg();
							Toast.makeText(
									ListarCustosEntradasMes.this,
									"Erro ao atualizar valores com o servidor: "
											+ e.getMessage(),
									Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
		}).start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.list_options_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add:
			startActivityForResult(new Intent(this, ManterCusto.class), 0);
			return true;
		}

		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0) {
			atualizarValoresServidor();
		}
	}

	private void populaValoresServidor() throws Exception {
		try {
			qtdCustos = repositorioCusto.countCustos();
			JSONObject json = HttpClientUtils.doPostJsonLogedUser(
					Constantes.URL_APP_WEB
							+ "in/ccusto/buscarCustosEntradaMesAtual", this,
					null);
			BigDecimal custosMes = BigDecimal.ZERO;

			try {
				custosMes = new BigDecimal(json.getString("custosMes"));
			} catch (Exception e) {
				// do nothing
			}
			BigDecimal entradasMes = BigDecimal.ZERO;
			try {
				entradasMes = new BigDecimal(json.getString("entradasMes"));
			} catch (Exception e) {
				// do nothing
			}

			diffMes = entradasMes.subtract(custosMes);
			NumberFormat format = NumberFormat.getCurrencyInstance(new Locale(
					"pt", "BR"));
			this.custoMesStr = format.format(custosMes);
			this.entradaMesStr = format.format(entradasMes);
			this.diffMesStr = format.format(diffMes);

		} catch (Exception e) {
			Log.e("ListarCustosEntradasMes", e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * Carrega a lista das informaÃ§Ãµes diretamente do servidor web
	 */
	private void atribuirValoresDoServidorNaTela() {
		try {
			Button btnSincronizar = (Button) findViewById(R.id.btnSincronize);
			btnSincronizar.setText("Sincronizar: " + qtdCustos + " custos.");

			TextView tx1 = (TextView) findViewById(R.id.tx_custos_mes);
			TextView tx2 = (TextView) findViewById(R.id.tx_entradas_mes);
			TextView tx3 = (TextView) findViewById(R.id.tx_diff_mes);

			tx1.setText(custoMesStr);
			tx2.setText(entradaMesStr);
			tx3.setText(diffMesStr);

			TableRow row = (TableRow) findViewById(R.id.row_diff);

			if (BigDecimal.ZERO.compareTo(diffMes) > 0) {
				row.setBackgroundColor(Color.RED);
			} else {
				row.setBackgroundColor(Color.GREEN);
			}
		} catch (Exception e) {
			Log.e("ListarCustosEntradasMes", e.getMessage(), e);
			Toast.makeText(ListarCustosEntradasMes.this,
					"Erro ao atualizar valores: " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	}

	public void sincronizarCustos() throws Exception {
		try {
			List<Custo> custos = repositorioCusto.getCustos();
			for (Custo c : custos) {
				if (salvarCustoServidor(c)) {
					repositorioCusto.deletar(c.idCusto);
				}
			}
		} catch (Exception e) {
			throw e;
		}
	}

	private boolean salvarCustoServidor(Custo c) {
		try {
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("descricaoGasto", c.descricaoGasto);
			param.put("idCategoriaGasto", c.idCategoriaGasto);
			param.put("dataVencimento",
					new SimpleDateFormat("dd/MM/yyyy").format(c.dataVencimento));
			param.put("valorParcela", c.valorParcela);
			param.put("lancamentoParcelado", c.parcelado);

			if (c.parcelado) {
				param.put("tipoRepeticao", c.tipoRepeticao);
				param.put("qtdParcelas", c.qtdParcelas);
			}

			JSONObject json = HttpClientUtils.doPostJsonLogedUser(
					Constantes.URL_APP_WEB + "in/ccusto/cadastrarAlterar",
					this, param);

			if (!json.getBoolean("success")) {
				throw new Exception(
						"Custo não pode ser adicionado por um erro nao identificado!");
			} else {
				return true;
			}
		} catch (Exception e) {
			return false;
		}
	}

}

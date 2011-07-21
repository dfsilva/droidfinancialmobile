package br.com.diego.financialmobile.domain.enums;

public enum TipoRepeticao {

	SEMANA(1, "Semanas"), QUINZENA(2, "Quinzenas"), MES(3, "Meses"), BIMESTRE(
			4, "Bimestres"), TRIMESTRE(5, "Trimestrais"), SEMESTRE(6,
			"Semestres");

	private TipoRepeticao(int numero, String descricao) {
		this.numero = numero;
		this.descricao = descricao;
	}

	private int numero;
	private String descricao;

	public int getNumero() {
		return numero;
	}

	public String getDescricao() {
		return descricao;
	}
	
	@Override
	public String toString() {
		return descricao;
	}

}

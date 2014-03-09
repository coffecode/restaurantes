package codecoffe.restaurantes.utilitarios;

public enum Usuario {
	INSTANCE;

	private int level;
	private String nome;
	private int olhando = -1;

	public void setNome(String texto) {
		nome = texto;
	}

	public void setLevel(int lvl) {
		level = lvl;
	}

	public void setOlhando(int mesa) {
		olhando = mesa;
	}

	public String getNome() {
		return nome;
	}

	public int getLevel() {
		return level;
	}

	public int getOlhando() {
		return olhando;
	}	
}
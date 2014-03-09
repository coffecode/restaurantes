package codecoffe.restaurantes.sockets;
import java.io.Serializable;

public class CacheAviso implements Serializable 
{
	private static final long serialVersionUID = 1L;
	private int tipo, classe;
	private String mensagem, titulo;
	
	public CacheAviso(int tipo, int classe, String msg, String title) {
		this.tipo = tipo;
		this.classe = classe;
		this.mensagem = msg;
		this.titulo = title;
	}
	
	public CacheAviso(int tipo, int classe)  {
		this.tipo = tipo;
		this.classe = classe;
	}

	public int getTipo() {
		return this.tipo;
	}
	
	public int getClasse() {
		return this.classe;
	}	
	
	public String getMensagem() {
		return this.mensagem;
	}
	
	public String getTitulo() {
		return this.titulo;
	}
}
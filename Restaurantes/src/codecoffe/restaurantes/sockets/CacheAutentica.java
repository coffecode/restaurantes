package codecoffe.restaurantes.sockets;
import java.io.Serializable;

public class CacheAutentica implements Serializable
{
	private static final long serialVersionUID = 1L;
	private String username, password, nome;
	private int header, level;
	
	public CacheAutentica(String u, String p)
	{
		this.username = u;
		this.password = p;
	}
	
	public int getHeader() {
		return header;
	}

	public void setHeader(int header) {
		this.header = header;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
}

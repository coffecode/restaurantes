package codecoffe.restaurantes.primitivas;
import java.io.Serializable;

import codecoffe.restaurantes.utilitarios.UtilCoffe;

public class Produto implements Serializable 
{
	private static final long serialVersionUID = 1L;
	private String nome, referencia;
	private int idUnico, codigo;
	protected double preco;
	
	public Produto(String nome, double preco)  {
		this.nome = nome;
		this.preco = preco;
	}

	public Produto(String nome, String referencia, double preco, int idUnico, int codigo) {
		this.nome = nome;
		this.referencia = referencia;
		this.preco = preco;
		this.idUnico = idUnico;
		this.codigo = codigo;
	}

	public Produto() {}

	public String getReferencia() {
		return referencia;
	}

	public void setReferencia(String referencia) {
		this.referencia = referencia;
	}

	public int getIdUnico() {
		return idUnico;
	}

	public void setIdUnico(int idUnico) {
		this.idUnico = idUnico;
	}

	public int getCodigo() {
		return codigo;
	}

	public void setCodigo(int codigo) {
		this.codigo = codigo;
	}

	public String getNome() {
		return this.nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public double getPreco() {
		return this.preco;
	}

	public void setPreco(double preco) {
		this.preco = preco;
	}
	
	@Override
	public String toString() {
		return this.nome;
	}
	
	public boolean contains(String texto) {
		if(UtilCoffe.isNumeric(texto))
		{
			if(this.codigo == Integer.parseInt(texto)) return true;
			
			if(texto.length() > 2)
			{
				String cd = "" + this.codigo;
				
				if(texto.length() < cd.length())
				{
					for(int i = 0; i < texto.length(); i++)
						if(texto.charAt(i) != cd.charAt(i)) return false;
					
					return true;
				}
			}
		}
		else
		{
			String[] partes = texto.split("\\s+");
			for(int i = 0; i < partes.length; i++)
				if(!UtilCoffe.removeAcentos(this.nome.toLowerCase()).contains(partes[i])) return false;
			
			return true;
		}
		return false;
	}
}

package codecoffe.restaurantes.primitivas;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Categoria implements Serializable
{
	private static final long serialVersionUID = 1L;
	private int idCategoria;
	private String titulo, imagem;
	private List<Produto> produtos = new ArrayList<Produto>(); 
	
	public Categoria(int idCategoria, String titulo, String imagem) {
		this.idCategoria = idCategoria;
		this.titulo = titulo;
		this.imagem = imagem;
	}
	
	public Categoria() {}
	
	public void addProduto(Produto p)
	{
		this.produtos.add(p);
	}
	
	public boolean removeProduto(Produto p)
	{
		return this.produtos.remove(p);
	}
	
	public int getIdCategoria() {
		return idCategoria;
	}
	public void setIdCategoria(int idCategoria) {
		this.idCategoria = idCategoria;
	}
	public String getTitulo() {
		return titulo;
	}
	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}
	public String getImagem() {
		return imagem;
	}
	public void setImagem(String imagem) {
		this.imagem = imagem;
	}

	public List<Produto> getProdutos() {
		return produtos;
	}

	public void setProdutos(List<Produto> produtos) {
		this.produtos = produtos;
	}

	@Override
	public String toString() {
		return this.titulo;
	}
}
package codecoffe.restaurantes.graficos.produtos;

import java.util.List;

import codecoffe.restaurantes.primitivas.Categoria;
import codecoffe.restaurantes.primitivas.Produto;
import codecoffe.restaurantes.sockets.CacheTodosProdutos;

public class ProdutosTreeModel extends AbstractTreeModel 
{
	private List<Categoria> categorias;
	private String fakeRoot = "Produtos";

	public ProdutosTreeModel(CacheTodosProdutos produtos) {
		categorias = produtos.getCategorias();
	}

	public Object getChild(Object parent, int index) {
		if(parent == fakeRoot) 			// É o nó principal?
			return categorias.get(index); 	// Pegamos da lista de categorias

		if(parent instanceof Categoria) 	// O pai é uma categoria?
		{
			// Devolvemos um produto
			return ((Categoria) parent).getProdutos().get(index);
		}

		throw new IllegalArgumentException("Invalid parent class" + parent.getClass().getSimpleName());
	}

	/**
	 * Retornamos quantos filhos um pai tem. No caso de um livro, é a contagem
	 * de autores. No caso da lista de livros, é a quantidade de livros.
	 */
	
	public int getChildCount(Object parent) {
		if (parent == fakeRoot)
			return categorias.size();

		if (parent instanceof Categoria)
			return ((Categoria) parent).getProdutos().size();

		throw new IllegalArgumentException("Invalid parent class" + parent.getClass().getSimpleName());
	}

	/**
	 * Dado um pai, indicamos qual é o índice do filho correspondente.
	 */
	
	public int getIndexOfChild(Object parent, Object child) {
		if (parent == fakeRoot)
			return categorias.indexOf(child);
		if (parent instanceof Categoria)
			return ((Categoria) parent).getProdutos().indexOf(child);

		return 0;
	}
	
	public Object getRoot() {
		return fakeRoot;
	}

	/**
	 * Indicamos se um nó é ou não uma folha. Isso é, se ele não tem filhos. No
	 * nosso caso, os autores são as folhas da árvore.
	 */
	
	public boolean isLeaf(Object node) {
		return node instanceof Produto;
	}
	
	public List<Categoria> getTodosProdutos()
	{
		return categorias;
	}
	
	public Categoria getCategoria(int id)
	{
		for(int i = 0; i < categorias.size(); i++)
		{
			if(categorias.get(i).getIdCategoria() == id)
			{
				return categorias.get(i);
			}
		}
		return null;
	}
	
	public Categoria getCategoria(Produto p)
	{
		for(int i = 0; i < categorias.size(); i++)
		{
			for(int x = 0; x < categorias.get(i).getProdutos().size(); x++)
			{
				if(categorias.get(i).getProdutos().get(x).getIdUnico() == p.getIdUnico())
				{
					return categorias.get(i);
				}
			}
		}
		return null;
	}
	
	public Produto getProduto(int id, Categoria categoria)
	{
		for(int i = 0; i < categoria.getProdutos().size(); i++)
			if(categoria.getProdutos().get(i).getIdUnico() == id)
				return categoria.getProdutos().get(i);
		
		return null;
	}

	public void adicionarCategoria(Categoria categoria)
	{
		categorias.add(categoria);
		fireLastPathComponentInserted(fakeRoot, categoria);
	}
	
	public void atualizarCategoria(Categoria categoria)
	{
		fireLastPathComponentChanged(fakeRoot, categoria);
	}
	
	public void atualizarProduto(Categoria categoriaNova, Categoria categoriaAntiga, Produto produto)
	{
		if(categoriaNova.getIdCategoria() == categoriaAntiga.getIdCategoria())
		{
			fireLastPathComponentChanged(fakeRoot, categoriaAntiga, produto);
		}
		else
		{
			adicionarProduto(categoriaNova, produto);
			removerProduto(categoriaAntiga, produto);
		}
	}

	public void adicionarProduto(Categoria categoria, Produto produto)
	{
		categoria.addProduto(produto);
		fireLastPathComponentInserted(fakeRoot, categoria, produto);
	}

	public void removerCategoria(Categoria categoria)
	{
		fireLastPathComponentRemoved(fakeRoot, categoria);
		categorias.remove(categoria);
	}
	
	public void removerProduto(Categoria categoria, Produto produto)
	{
		fireLastPathComponentRemoved(fakeRoot, categoria, produto);
		categoria.removeProduto(produto);
	}
	
	public void countTudo()
	{
		int produtos = 0;
		int cat = 0;
		for(int i = 0; i < categorias.size(); i++)
		{
			for(int x = 0; x < categorias.get(i).getProdutos().size(); x++) {
				produtos++;
			}
			
			cat++;
		}
		System.out.println("Contagem, Produtos: " + produtos + " - Categorias: " + cat);
	}
}
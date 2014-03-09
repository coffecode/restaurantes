package codecoffe.restaurantes.eventos;

import codecoffe.restaurantes.primitivas.Categoria;
import codecoffe.restaurantes.primitivas.Produto;

public interface ProdutoAlterado {
	void categoriaDeletada(Categoria c);
	void categoriaAdicionada(Categoria c);
	void categoriaEditada(Categoria c);
	
	void produtoAdicionado(Produto p, Categoria c);
	void produtoDeletado(Produto p, Categoria c);
	void produtoEditado(Produto p, Categoria nova, Categoria antiga);
}

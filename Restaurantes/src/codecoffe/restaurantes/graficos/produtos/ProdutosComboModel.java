package codecoffe.restaurantes.graficos.produtos;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.MutableComboBoxModel;

import codecoffe.restaurantes.primitivas.Categoria;
import codecoffe.restaurantes.primitivas.Produto;

public class ProdutosComboModel extends AbstractListModel<Object> implements MutableComboBoxModel<Object> 
{
	private static final long serialVersionUID = 1L;
	private List<Categoria> listaProdutos;
	private Produto produtoSelecionado;
	private int modo;
	
	public ProdutosComboModel(List<Categoria> categorias, int m)
	{
		modo = m;
		this.listaProdutos = categorias;
		
		if(getFirstItem() != null)
			setSelectedItem(getFirstItem());
	}
	
	public void refreshModel() {
		fireContentsChanged(this, 0, 0);
	}
	
	public ProdutosComboModel()
	{
		modo = 1;
		this.listaProdutos = new ArrayList<Categoria>();
	}
	
	public Produto getFirstItem()
	{
		if(modo == 1)
		{
			if(this.listaProdutos.size() > 1)
				if(this.listaProdutos.get(1).getProdutos().size() > 0)
					return this.listaProdutos.get(1).getProdutos().get(0);
				else
					return null;
			else
				return null;	
		}
		else
		{
			if(this.listaProdutos.get(0).getProdutos().size() > 0)
				return this.listaProdutos.get(0).getProdutos().get(0);
			else
				return null;
		}
	}
	
	@Override
	public int getSize() {
		int cont = 0;
		
		if(modo == 1)
		{
			for(int i = 1; i < listaProdutos.size(); i++)
			{
				cont += listaProdutos.get(i).getProdutos().size();
			}	
		}
		else
		{
			return listaProdutos.get(0).getProdutos().size();
		}
		
		return cont;
	}

	@Override
	public Object getElementAt(int index) 
	{
		if(modo == 1)
		{
			int linha = 1;
			int coluna = 0;
			    
			while(true)
			{
				int i = listaProdutos.get(linha).getProdutos().size();
				if(i > index) {
					coluna = index;
					break;
				}
				else {
					index -= i;
					linha++;
				}
			}
			
			return listaProdutos.get(linha).getProdutos().get(coluna);
		}
		else
		{
			int cont = 0;
			
			for(int x = 0; x < listaProdutos.get(0).getProdutos().size(); x++)
			{
				if(cont == index)
					return listaProdutos.get(0).getProdutos().get(x);
				
				cont++;
			}
			
			return null;
		}
	}

	@Override
	public void setSelectedItem(Object anItem) {
		this.produtoSelecionado = (Produto) anItem;
	}

	@Override
	public Object getSelectedItem() {
		return this.produtoSelecionado;
	}

	@Override
	public void addElement(Object item) {
		this.listaProdutos.add((Categoria) item);
	}

	@Override
	public void removeElement(Object obj) {
		this.listaProdutos.remove((Produto) obj);
	}

	@Override
	public void insertElementAt(Object item, int index) {
		this.listaProdutos.add(index, (Categoria) item);
	}

	@Override
	public void removeElementAt(int index) {
		this.listaProdutos.remove(index);
	}
}
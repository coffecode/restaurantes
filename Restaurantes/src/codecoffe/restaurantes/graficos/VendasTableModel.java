package codecoffe.restaurantes.graficos;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import codecoffe.restaurantes.primitivas.ProdutoVenda;
import codecoffe.restaurantes.utilitarios.UtilCoffe;

public class VendasTableModel extends AbstractTableModel
{
	private static final long serialVersionUID = 1L;
	private String[] colunas = {"+/-", "Nome", "Qntd", "Preço", "Pago", "Adicionais", "Comentário", "Deletar"};
	private List<ProdutoVenda> produtoVenda;
	
	public VendasTableModel() {
		this.produtoVenda = new ArrayList<ProdutoVenda>();
	}
	
	public List<ProdutoVenda> getProdutoVenda() {
		return produtoVenda;
	}

	public void setProdutoVenda(List<ProdutoVenda> produtoVenda) {
		this.produtoVenda = produtoVenda;
	}

	public void addRow(ProdutoVenda pv) {
		this.produtoVenda.add(pv);
		this.fireTableDataChanged();
	}
	
	public void removeRow(int linha) {
		this.produtoVenda.remove(linha);
		this.fireTableRowsDeleted(linha, linha);
	}
	
	public void refreshTable() {
		this.fireTableDataChanged();
	}
	
	public ProdutoVenda getProdutoVenda(int index) {
		return this.produtoVenda.get(index);
	}
	
	public boolean isCellEditable(int linha, int coluna) {
	    if(coluna == 0 || coluna == 7)
	    	return true;
		
	    return false;
	}
	
	public String getColumnName(int num) {
        return this.colunas[num];
    }
	
	@Override
	public int getRowCount() {
		return produtoVenda.size();
	}

	@Override
	public int getColumnCount() {
		return colunas.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch(columnIndex)
		{
			case 0: return "";
			case 1: return this.produtoVenda.get(rowIndex).getNome();
			case 2: return this.produtoVenda.get(rowIndex).getQuantidade();
			case 3: return UtilCoffe.doubleToPreco(this.produtoVenda.get(rowIndex).getTotalProduto2());
			case 4: return this.produtoVenda.get(rowIndex).getPagos();
			case 5: return this.produtoVenda.get(rowIndex).getAllAdicionais();
			case 6: return this.produtoVenda.get(rowIndex).getComentario();
			case 7: return "";
		}
		return null;
	}
}
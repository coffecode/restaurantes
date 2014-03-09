package codecoffe.restaurantes.primitivas;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Venda implements Serializable 
{
	private static final long serialVersionUID = 1L;
	private ArrayList<ProdutoVenda> produtos = new ArrayList<ProdutoVenda>();
	private double total;
	private Date data;

	public Venda() {}

	public void clear()
	{
		this.produtos.clear();
		this.total = 0;
	}

	public ProdutoVenda getProduto(int index)
	{
		return this.produtos.get(index);
	}

	public int adicionarProduto(ProdutoVenda p)
	{
		boolean flag = false;
		int id = 0;
		if(this.produtos.size() > 0)
		{
			for(int i = 0 ; i < this.produtos.size() ; i++)
			{
				if(this.produtos.get(i).compareTo(p))
				{
					flag = true;
					this.produtos.get(i).setQuantidade(p.getQuantidade(), 1);
					id = i;
					break;
				}
			}
		}

		this.calculaTotal();

		if(!flag)
		{
			this.produtos.add(p);
			return (this.produtos.size()-1);
		}

		return id;
	}

	public void removerProdutoIndex(int index)
	{
		this.produtos.remove(index);
	}		

	public void removerProduto(String nome, double precoTotal)
	{
		if(this.produtos.size() > 0)
		{
			for(int i = 0 ; i < this.produtos.size() ; i++)
			{
				ProdutoVenda x = new ProdutoVenda();
				x = this.produtos.get(i);

				if(x.getNome() == nome && x.getTotalProduto() == precoTotal)
				{
					this.produtos.remove(i);
					break;
				}
			}
		}
	}

	public int getQuantidadeProdutos()
	{
		return this.produtos.size();
	}

	public void calculaTotal()
	{
		this.total = 0;

		if(this.produtos.size() > 0)
		{
			for(int i = 0 ; i < this.produtos.size() ; i++)
			{
				ProdutoVenda p = new ProdutoVenda();
				p = this.produtos.get(i);
				this.total += (p.getTotalProduto()*p.getQuantidade());
			}
		}
	}

	public double getTotal() {
		return this.total;
	}

	public void setTotal(double preco) {
		this.total = preco;
	}

	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}
	
	public String getDataString()
	{
		SimpleDateFormat formataData = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss a");
		return formataData.format(this.data);
	}
	
	public void setDataString(String texto)
	{
		SimpleDateFormat formataData = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss a");
		try {
			this.data = formataData.parse(texto);
		} catch (ParseException e) {
			this.data = new Date();
		}
	}
}
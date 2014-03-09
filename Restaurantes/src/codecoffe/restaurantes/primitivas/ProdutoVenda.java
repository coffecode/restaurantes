package codecoffe.restaurantes.primitivas;

import java.io.Serializable;
import java.util.ArrayList;

public class ProdutoVenda extends Produto implements Serializable 
{
	private static final long serialVersionUID = 1L;
	private ArrayList<Produto> adicionais = new ArrayList<Produto>();
	private int quantidade, pagos;
	private double totalProduto;
	private String comentario = "";
	
	public ProdutoVenda(String nome, double preco, int quantidade, int pagos) {
		super(nome, preco);
		this.quantidade = quantidade;
		this.pagos = pagos;
	}
	
	public ProdutoVenda(String nome, double preco) {
		super(nome, preco);
	}
	
	public ProdutoVenda(String nome, String referencia, double preco, int idUnico, int codigo) {
		super(nome, referencia, preco, idUnico, codigo);
	}

	public ProdutoVenda(String nome, String referencia, double preco,
			int idUnico, int codigo, ArrayList<Produto> adicionais,
			int quantidade, int pagos, double totalProduto, String comentario) {
		super(nome, referencia, preco, idUnico, codigo);
		this.adicionais = adicionais;
		this.quantidade = quantidade;
		this.pagos = pagos;
		this.totalProduto = totalProduto;
		this.comentario = comentario;
	}

	public ProdutoVenda() {}
	
	public boolean compareTo(ProdutoVenda p)
	{
		if(this.getIdUnico() != p.getIdUnico()) return false;
		if(this.adicionais.size() != p.getAdicionaisList().size()) return false;
		if(!this.getComentario().equals(p.getComentario())) return false;
		
		for(int i = 0; i < this.adicionais.size(); i++)
			if(this.adicionais.get(i).getIdUnico() != p.getAdicionaisList().get(i).getIdUnico()) return false;
		
		return true;
	}
	
	public void calcularPreco()
	{
		this.totalProduto = 0;
		if(this.adicionais.size() > 0)
			for(int i = 0; i < this.adicionais.size() ; i++)
				this.totalProduto += this.adicionais.get(i).getPreco();
	}
	
	public void adicionrAdc(Produto adc)
	{
		this.adicionais.add(adc);
		this.calcularPreco();
	}
	
	public void setAdicionaisList(ArrayList<Produto> adc) {
		this.adicionais = adc;
	}
	
	public ArrayList<Produto> getAdicionaisList() {
		return this.adicionais;
	}
	
	public Produto getAdicional(int index) {
		return this.adicionais.get(index);
	}
	
	public int getTotalAdicionais() {
		return this.adicionais.size();
	}
	
	public String getAllAdicionais()
	{
		String todosAdicionais = "";
		
		for(int i = 0 ; i < adicionais.size() ; i++)
		{
			todosAdicionais += adicionais.get(i).getNome();
			
			if(i != (adicionais.size()-1))
				todosAdicionais += ", ";
		}
		
		return todosAdicionais;
	}
	
	public String getAllAdicionaisId() 
	{
		String todosAdicionais = "";
		
		for(int i = 0 ; i < adicionais.size() ; i++)
		{
			todosAdicionais += adicionais.get(i).getIdUnico();
			
			if(i != (adicionais.size()-1))
				todosAdicionais += " ";
		}		
		
		return todosAdicionais;
	}

	public int getQuantidade() {
		return quantidade;
	}
	
	public void setQuantidade(int quantidade, int tipo) {
		//O = Substitue
		//1 = Soma
		//2 = subtrai
		//3 = multiplica
		//4 = divide
		
		switch (tipo) {
		case 0:
			this.quantidade = quantidade;
			break;
		case 1:
			this.quantidade += quantidade;
			break;
		case 2:
			this.quantidade -= quantidade;
			break;
		case 3:
			this.quantidade = this.quantidade* quantidade;
			break;
		case 4:
			this.quantidade = this.quantidade/ quantidade;
			break;	
		default:
			break;
		}
	}
	
	public void setTotalProduto(double totalProduto) {
		this.totalProduto = totalProduto;
	}
	
	public double getTotalProduto() {
		return (this.preco + this.totalProduto);
	}
	
	public void setPagos(int setar) {
		this.pagos += setar;
	}
	
	public int getPagos() {
		return this.pagos;
	}
	
	public String getComentario() {
		return comentario;
	}
	
	public void setComentario(String comentario) {
		this.comentario = comentario;
	}
}

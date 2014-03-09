package codecoffe.restaurantes.sockets;
import java.io.Serializable;
import codecoffe.restaurantes.primitivas.ProdutoVenda;
import codecoffe.restaurantes.primitivas.Venda;

public class CacheMesaHeader implements Serializable
{
	private static final long serialVersionUID = 1L;
	private Venda vendaMesa;
	private ProdutoVenda produtoMesa;
	private int mesa_id, header, header_extra;
	private String atendente;
	
	public CacheMesaHeader(int id, Venda v, int hd)
	{
		this.vendaMesa = v;
		this.mesa_id = id;
		this.header = hd;
	}
	
	public CacheMesaHeader(int id, ProdutoVenda p, int hd)
	{
		this.produtoMesa = p;
		this.mesa_id = id;
		this.header = hd;
	}
	
	public CacheMesaHeader(int id, ProdutoVenda p, Venda v, int hd, int hd_extra)
	{
		this.vendaMesa = v;
		this.produtoMesa = p;
		this.mesa_id = id;
		this.header = hd;
		this.header_extra = hd_extra;
	}
	
	public CacheMesaHeader(int id, ProdutoVenda p, Venda v, int hd, int hd_extra, String atd)
	{
		this.vendaMesa = v;
		this.produtoMesa = p;
		this.mesa_id = id;
		this.header = hd;
		this.header_extra = hd_extra;
		this.atendente = atd;
	}
	
	public String getAtendente() {
		return atendente;
	}

	public void setAtendente(String atendente) {
		this.atendente = atendente;
	}

	public int getHeaderExtra() {
		return this.header_extra;
	}
	
	public ProdutoVenda getProdutoMesa() {
		return this.produtoMesa;
	}
	
	public Venda getMesaVenda() {
		return this.vendaMesa;
	}
	
	public int getMesaId() {
		return this.mesa_id;
	}
	
	public int getHeader() {
		return this.header;
	}	
}

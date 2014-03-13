package codecoffe.restaurantes.primitivas;
import java.io.Serializable;
import java.util.Date;

public class Pedido implements Serializable
{
	private static final long serialVersionUID = 1L;
	private ProdutoVenda p;
	private Date horario, ultimaEdicao;
	private String nomeAtendido;
	private int local, header, status, idUnico;
	
	public Pedido(ProdutoVenda prod, String atendimento, int local)
	{
		this.p = prod;
		this.nomeAtendido = atendimento;
		this.local = local;
		this.header = 1;
		this.status = 1;
		this.idUnico = 0;
	}
	
	public Pedido(ProdutoVenda p, Date horario, String nomeAtendido, int local, int status, int idUnico) {
		this.p = p;
		this.horario = horario;
		this.nomeAtendido = nomeAtendido;
		this.local = local;
		this.status = status;
		this.idUnico = idUnico;
		this.header = 0;
		this.ultimaEdicao = new Date();
	}

	public Pedido() {}
	
	public int getIdUnico() {
		return idUnico;
	}

	public void setIdUnico(int idUnico) {
		this.idUnico = idUnico;
	}

	public ProdutoVenda getProduto()
	{
		return this.p;
	}
	
	public Date getHora()
	{
		return this.horario;
	}
	
	public String getAtendido()
	{
		return this.nomeAtendido;
	}

	public void setLocal(int local)
	{
		this.local = local;
	}
	
	public int getLocal()
	{
		return this.local;
	}
	
	public int getHeader()
	{
		return this.header;
	}
	
	public int getStatus()
	{
		return this.status;
	}
	
	public Date getUltimaEdicao()
	{
		return this.ultimaEdicao;
	}
	
	public void setHora(Date now)
	{
		this.horario = now;
	}
	
	public void setHeader(int hd)
	{
		this.header = hd;
	}

	public void setStatus(int ss)
	{
		this.status = ss;
	}
	
	public void setUltimaEdicao(Date tempo)
	{
		this.ultimaEdicao = tempo;
	}
	
	public void setProduto(ProdutoVenda produto)
	{
		this.p = produto;
	}
}
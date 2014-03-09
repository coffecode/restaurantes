package codecoffe.restaurantes.sockets;
import java.io.Serializable;
import java.util.List;
import codecoffe.restaurantes.primitivas.Venda;

public class CacheTodasMesas implements Serializable 
{
	private static final long serialVersionUID = 1L;
	private List<Venda> vendaMesas;
	
	public CacheTodasMesas(List<Venda> v) {
		this.vendaMesas = v;
	}
	
	public List<Venda> getTodasMesas() {
		return this.vendaMesas;
	}
}
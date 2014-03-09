package codecoffe.restaurantes.sockets;
import java.io.Serializable;
import java.util.List;

import codecoffe.restaurantes.primitivas.Pedido;

public class CacheTodosPedidos implements Serializable
{	
	private static final long serialVersionUID = 1L;
	private List<Pedido> pedidos;
	
	public CacheTodosPedidos(List<Pedido> list) {
		this.pedidos = list;
	}
	
	public List<Pedido> getTodosPedidos() {
		return this.pedidos;
	}
}

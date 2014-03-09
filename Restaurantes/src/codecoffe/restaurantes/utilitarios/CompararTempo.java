package codecoffe.restaurantes.utilitarios;
import java.util.Comparator;

import codecoffe.restaurantes.primitivas.Pedido;

public class CompararTempo implements Comparator<Pedido>
{
	@Override
	public int compare(Pedido p1, Pedido p2) {
		return (p1.getHora().compareTo(p2.getHora()))*-1;
	}
}
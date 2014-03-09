package codecoffe.restaurantes.eventos;

import java.io.ObjectOutputStream;

import codecoffe.restaurantes.sockets.CacheMesaHeader;

public interface MesaAlterada {
	void atualizarMesa(CacheMesaHeader m, ObjectOutputStream socket);
	void atualizarMesa(int id);
}

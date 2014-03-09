package codecoffe.restaurantes.eventos;

import java.io.IOException;
import java.io.ObjectOutputStream;

public interface SocketsRecebido {
	
	public void objetoRecebido(Object objeto, ObjectOutputStream client) throws IOException;
}

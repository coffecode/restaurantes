package codecoffe.restaurantes.sockets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import codecoffe.restaurantes.primitivas.Clientes;

public class CacheClientes implements Serializable
{
	private static final long serialVersionUID = 1L;
	private int header;
	private String atendente;
	private List<Clientes> listaClientes;
	private Clientes cliente;
	
	public CacheClientes(List<Clientes> clientes, int hd)
	{
		this.listaClientes = clientes;
		this.header = hd;
	}
	
	public CacheClientes(Clientes c, int hd, String atendente)
	{
		this.cliente = c;
		this.header = hd;
		this.atendente = atendente;
	}

	public List<Clientes> getListaClientes() {
		return listaClientes;
	}

	public void setListaClientes(ArrayList<Clientes> listaClientes) {
		this.listaClientes = listaClientes;
	}
	
	public Clientes getCliente() {
		return cliente;
	}

	public void setCliente(Clientes cliente) {
		this.cliente = cliente;
	}

	public String getAtendente() {
		return atendente;
	}

	public void setAtendente(String atendente) {
		this.atendente = atendente;
	}

	public int getHeader() {
		return header;
	}

	public void setHeader(int header) {
		this.header = header;
	}
}
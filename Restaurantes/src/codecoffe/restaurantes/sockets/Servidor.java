package codecoffe.restaurantes.sockets;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import codecoffe.restaurantes.eventos.SocketsRecebido;
import codecoffe.restaurantes.graficos.PainelErro;
import codecoffe.restaurantes.utilitarios.UtilCoffe;

public class Servidor implements Runnable
{
	private int porta;
	private boolean procurandoConexoes;
	private int uniqueId = 1;
	private ArrayList<ClienteThread> listaClientes;
	private SocketsRecebido serverListener;
	private ServerSocket serverSocket;
	
	public Servidor(SocketsRecebido listener, int port)
	{
		serverListener = listener;
		porta = port;
		procurandoConexoes = true;
		listaClientes = new ArrayList<ClienteThread>();
		System.out.println("Server TCP iniciado na porta: " + porta);
	}
	
	/* adiciona e remove todos os listeners, isto é, 
	 * qualquer objeto que esteja esperando um callback do servidor */
	
	synchronized void remove(int id)
	{
		for(int i = 0; i < listaClientes.size(); ++i)
		{
			ClienteThread ct = listaClientes.get(i);
			if(ct.id == id)
			{
				listaClientes.remove(i);
				return;
			}
		}
	}
	
	public void terminate()
	{
		enviaTodos("BYE");
		procurandoConexoes = false;
		
		for(int i = 0; i < listaClientes.size(); ++i)
		{
			ClienteThread tc = listaClientes.get(i);
			try {
				tc.sInput.close();
				tc.sOutput.close();
				tc.socket.close();
			}catch(IOException e) {}	// erro não interessa.
		}
	}
	
	public void restart()
	{
		enviaTodos("BYE");
		procurandoConexoes = false;
		
		for(int i = 0; i < listaClientes.size(); ++i)
		{
			ClienteThread tc = listaClientes.get(i);
			try {
				tc.sInput.close();
				tc.sOutput.close();
				tc.socket.close();
			}catch(IOException e) {}	// erro não interessa.
		}
		
		listaClientes.clear();
		
		try {
			serverSocket.close();
		} catch (IOException e) {}
	}
	
	public synchronized void enviaTodos(Object ob) 
	{
		if(listaClientes.size() > 0)
		{
			for(int i = listaClientes.size(); --i >= 0;) {
				ClienteThread ct = listaClientes.get(i);
				if(!ct.enviarObjeto(ob)) {
					listaClientes.remove(i);
				}
			}
		}
	}

	public synchronized void enviaTodos(Object ob, int ex) 
	{
		for(int i = listaClientes.size(); --i >= 0;) {
			ClienteThread ct = listaClientes.get(i);
			if(ct.id != ex)
			{
				if(!ct.enviarObjeto(ob)) {
					listaClientes.remove(i);
				}	
			}
		}
	}
	
	public synchronized void enviaTodos(Object ob, ObjectOutputStream ex) 
	{
		for(int i = listaClientes.size(); --i >= 0;) {
			ClienteThread ct = listaClientes.get(i);
			if(ct.sOutput != ex)
			{
				if(!ct.enviarObjeto(ob)) {
					listaClientes.remove(i);
				}	
			}
		}
	}

	public synchronized void enviaObjeto(Object ob, int cliente)
	{
		for(int i = listaClientes.size(); --i >= 0;) {
			ClienteThread ct = listaClientes.get(i);
			if(ct.id == cliente)
			{
				if(!ct.enviarObjeto(ob)) {
					listaClientes.remove(i);
				}
				break;
			}
		}		
	}
	
	public void enviaObjeto(Object ob, ObjectOutputStream socket)
	{
		try {
			socket.reset();
			socket.writeObject(ob);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() 
	{
		try {
			// criando o servidor na porta marcada
			serverSocket = new ServerSocket(porta);

			while(procurandoConexoes)	//fica procurando conexao para aceitar.
			{
				try {
					Socket socket = serverSocket.accept();
					System.out.println("Conexao recebida de: " + socket.getInetAddress());
					ClienteThread cliente = new ClienteThread(socket);		// Cria uma Thread para esse cliente.
					listaClientes.add(cliente);								// Adiciona na lista de clientes.
					cliente.start();	
				} catch (ClassNotFoundException | IOException e) {
					if(!e.getMessage().toLowerCase().equals("socket closed") && !e.getMessage().toLowerCase().equals("connection reset") )
						e.printStackTrace();
				}
			}

			for(int i = 0; i < listaClientes.size(); ++i)
			{
				ClienteThread tc = listaClientes.get(i);
				try {
					tc.sInput.close();
					tc.sOutput.close();
					tc.socket.close();
				}catch(IOException e) {}	// erro não interessa.
			}

			serverSocket.close();

		} catch (IOException e) {
			e.printStackTrace();
			new PainelErro(e);
			System.exit(0);
		}
	}
	
	/* Para ter infinitos terminais, é necessário criar uma thread para
	 * cada cliente conectado no servidor, dessa forma é possível
	 * se comunicar com cada um separadamente e sem travar o programa */
	
	class ClienteThread extends Thread
	{
		int id;
		boolean clienteConectado = true;
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;

		ClienteThread(Socket socket) throws IOException, ClassNotFoundException
		{
			this.id = ++uniqueId;
			this.socket = socket;

			sOutput = new ObjectOutputStream(socket.getOutputStream());
			sInput  = new ObjectInputStream(socket.getInputStream());

			Object data = sInput.readUnshared();

			if(data instanceof String)
			{
				if(!data.toString().equals(UtilCoffe.VERSAO))
				{
					sOutput.reset();
					sOutput.writeObject("WRONG VERSION");						
					this.clienteConectado = false;	// kicka, versão diferente do servidor.
				}
			}
			else
				this.clienteConectado = false;	// kicka pois não informou versão.
		}

		public void run()
		{			
			while(this.clienteConectado)
			{		
				try {
					Object dataRecebida = sInput.readUnshared();
					
					if(dataRecebida instanceof String)
						if(dataRecebida.toString().equals("ADEUS"))
							this.clienteConectado = false;
					
					serverListener.objetoRecebido(dataRecebida, sOutput);					
				} catch (IOException | ClassNotFoundException e) {
					if(e.getMessage() != null)
					{
						if(e.getMessage().contains("Connection reset") || e.getMessage().toLowerCase().contains("socket closed"))
						{
							System.out.println("Cliente desconectado");
							this.clienteConectado = false;
						}
						else
						{
							e.printStackTrace();
							new PainelErro(e);
							terminate();
							System.exit(0);
						}
					}
				}
			}

			remove(id);
			close();
		}
		
		private boolean enviarObjeto(Object ob) {
			if(!socket.isConnected()) {
				close();
				return false;
			}

			try {
				sOutput.reset();
				sOutput.writeObject(ob);
				return true;
			}
			catch(IOException e) {
				e.printStackTrace();
				close();
				return false;
			}
		}		

		private void close()
		{
			if(sOutput != null)
				try {
					sOutput.close();
				} catch (IOException e) {}

			if(sInput != null)
				try {
					sInput.close();
				} catch (IOException e) {}

			if(socket != null)
				try {
					socket.close();
				} catch (IOException e) {}
		}
	}
}
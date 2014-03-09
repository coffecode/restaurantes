package codecoffe.restaurantes.sockets;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import codecoffe.restaurantes.eventos.AtualizarPainel;
import codecoffe.restaurantes.eventos.SocketsRecebido;
import codecoffe.restaurantes.graficos.PainelDisconnect;
import codecoffe.restaurantes.graficos.PainelErro;
import codecoffe.restaurantes.utilitarios.Header;
import codecoffe.restaurantes.utilitarios.UtilCoffe;

import com.alee.utils.ThreadUtils;

public class Client implements Runnable
{
	private ObjectInputStream sInput;
	private ObjectOutputStream sOutput;
	private Socket socket;
	private InetAddress hostname;
	private int port;
	private boolean clienteConectado;
	private boolean finalizarPrograma = false;
	private boolean reLoad = false;
	private Date ultimoPing;
	private Timer timerPing;
	private SocketsRecebido clientListeners;
	private AtualizarPainel painelListener;
	
	public Client() {}
	
	public void atualizaConexao(InetAddress host, int porta) throws IOException
	{
		hostname = host;
		port = porta;
		socket = new Socket(hostname, port);
	}
	
	public void setListenerSockets(SocketsRecebido toAdd) {
		clientListeners = toAdd;
    }
	
	public void setListenerPainel(AtualizarPainel toAdd) {
		painelListener = toAdd;
    }

	@Override
	public void run() {
		conexaoEstabelecida();
	}
	
	private void conexaoEstabelecida()
	{
		System.out.println("Conexao aceita: " + socket.getInetAddress() + ":" + socket.getPort());
		clienteConectado = true;

		try {
			sInput  = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());

			System.out.println("Input/Output criados.");
		} catch (IOException e) {
			System.out.println("Error ao criar imput e output cliente: " + e);
		}
		
		enviarObjeto("" + UtilCoffe.VERSAO);
		new ListenFromServer().start();
		
		timerPing = new Timer();
		timerPing.schedule(new PingPeriodico(), 30*1000, 3*1000);
		
	  	System.out.println("Enviando pedido de configuracao atualizada.");
    	enviarObjeto("UPDATE CONFIGURACAO");
		
    	System.out.println("Enviando pedido da lista de produtos atualizado.");
    	enviarObjeto("UPDATE PRODUTOS");
    	
    	System.out.println("Enviando pedido da lista de mesas atualizada.");
    	enviarObjeto("UPDATE MESAS");        	
    	
    	System.out.println("Enviando pedido da lista de pedidos atualizada.");
    	enviarObjeto("UPDATE PEDIDOS");
    	
    	System.out.println("Enviando pedido da lista de clientes atualizada.");
    	enviarObjeto("UPDATE CLIENTES");
    	
    	System.out.println("Enviando pedido da lista de funcionários atualizada.");
    	enviarObjeto("UPDATE FUNCIONARIOS");
	}
	
	private void conexaoEstabelecida(boolean reconexao)
	{
		System.out.println("Conexao aceita: " + socket.getInetAddress() + ":" + socket.getPort());
		
		try {
			sInput  = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
			clienteConectado = true;
			System.out.println("Input/Output criados.");
		} catch (IOException e) {
			System.out.println("Error ao criar imput e output cliente: " + e);
		}
		
		enviarObjeto("" + UtilCoffe.VERSAO);
		
	  	System.out.println("Enviando pedido de configuracao atualizada.");
    	enviarObjeto("UPDATE CONFIGURACAO");
		
    	System.out.println("Enviando pedido da lista de produtos atualizado.");
    	enviarObjeto("UPDATE PRODUTOS");
    	
    	System.out.println("Enviando pedido da lista de mesas atualizada.");
    	enviarObjeto("UPDATE MESAS");        	
    	
    	System.out.println("Enviando pedido da lista de pedidos atualizada.");
    	enviarObjeto("UPDATE PEDIDOS");
    	
    	System.out.println("Enviando pedido da lista de clientes atualizada.");
    	enviarObjeto("UPDATE CLIENTES");
    	
    	System.out.println("Enviando pedido da lista de funcionários atualizada.");
    	enviarObjeto("UPDATE FUNCIONARIOS");
    	
    	painelListener.atualizarPainel(new Header(UtilCoffe.ENABLED, true));
	}	
	
	public void clientReconnect()
	{		
		if(!reLoad)
		{
			reLoad = true;
			painelListener.atualizarPainel(new Header(UtilCoffe.ENABLED, false));
			disconnect();
			
			new Thread(new Runnable()
	        {
	            @Override
	            public void run()
	            {
	            	boolean sucesso = false;
	            	PainelDisconnect dcPanel = new PainelDisconnect();
	            	dcPanel.setVisible(true);
	            	
	            	while(!sucesso)
	            	{
	            		ThreadUtils.sleepSafely(3000);
	            		
	            		try 
	            		{
	            			System.out.println("Tentando reconectar com o servidor.");
	            			socket = new Socket(hostname, port);
	            			sucesso = true;
	            			dcPanel.dispose();
	            			conexaoEstabelecida(true);
	            			break;
	            		} 
	            		catch (IOException e) {}            		
	            	}
	            }
	        } ).start ();				
		}	
	}
	
	public void finalizaPrograma(int motivo)
	{
		clienteConectado = false;
		
		if(motivo == 1)
		{
			JOptionPane.showMessageDialog(null, "O programa principal fechou todas conexões.");
		}
		else if(motivo == 2)
		{
			JOptionPane.showMessageDialog(null, "A versão desse programa difere da versão do Principal.");
		}
		
		finalizarPrograma = true;
		System.exit(0);
	}
	
	class PingPeriodico extends TimerTask 
	{
        public void run() 
        {
        	if(clienteConectado)
        	{
            	long duration = System.currentTimeMillis() - ultimoPing.getTime();
            	long seconds = TimeUnit.MILLISECONDS.toSeconds(duration);
            	
            	if(seconds > 10)	// ja faz 10 segundos desde a última resposta
            	{
            		enviarObjeto(new String("ping!"));	// testando a conexão
            		ultimoPing = new Date();
            	}        		
        	}
        }
	}
	
	public void enviarObjeto(Object objeto)
	{
		try {
			sOutput.reset();
			sOutput.writeObject(objeto);
		} catch (IOException e) {
			
			if(e.getMessage().contains("Connection reset"))
			{
				System.out.println("Conexão perdida com o servidor.");
				clientReconnect();
			}
			else	// erro desconhecido
			{
				e.printStackTrace();
				new PainelErro(e);
				finalizarPrograma = true;
				clienteConectado = false;
				System.exit(0);
			}
		}
	}
	
	public void disconnect()
	{
		clienteConectado = false;

		try {
			sInput.close();
		} catch (IOException e) {}

		try {
			sOutput.close();
		} catch (IOException e) {}

		try {
			socket.close();
		} catch (IOException e) {}
	}
	
	class ListenFromServer extends Thread
	{
		public void run()
		{
			while(!finalizarPrograma)
			{
				if(clienteConectado)
				{
					try {
						Object dataRecebida = sInput.readUnshared();
						ultimoPing = new Date();
						
						if(dataRecebida != null && clientListeners != null)
						{
							clientListeners.objetoRecebido(dataRecebida, sOutput);
						}
						
					} catch (ClassNotFoundException | IOException e) {
						if(e.getMessage().contains("Connection reset"))
						{
							System.out.println("Conexão perdida com o servidor.");
							clienteConectado = false;
							clientReconnect();
						}
						else if(e.getMessage().toLowerCase().contains("socket closed"))
						{
							// de boa
						}
						else	// erro desconhecido.
						{
							System.err.println(e.getMessage());
							e.printStackTrace();
							new PainelErro(e);
							finalizarPrograma = true;
							clienteConectado = false;
							System.exit(0);
						}
					}					
				}
			}
		}
	}
}

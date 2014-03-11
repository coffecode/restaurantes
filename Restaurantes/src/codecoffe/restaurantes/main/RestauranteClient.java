package codecoffe.restaurantes.main;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import com.alee.managers.notification.NotificationManager;
import com.alee.utils.ThreadUtils;

import codecoffe.restaurantes.eventos.AtualizarPainel;
import codecoffe.restaurantes.eventos.SocketsRecebido;
import codecoffe.restaurantes.graficos.FramePrincipal;
import codecoffe.restaurantes.graficos.PainelClientes;
import codecoffe.restaurantes.graficos.PainelCozinha;
import codecoffe.restaurantes.graficos.PainelMesas;
import codecoffe.restaurantes.graficos.PainelVendaMesa;
import codecoffe.restaurantes.graficos.PainelVendaRapida;
import codecoffe.restaurantes.primitivas.Clientes;
import codecoffe.restaurantes.primitivas.Pedido;
import codecoffe.restaurantes.primitivas.Venda;
import codecoffe.restaurantes.sockets.CacheAutentica;
import codecoffe.restaurantes.sockets.CacheAviso;
import codecoffe.restaurantes.sockets.CacheClientes;
import codecoffe.restaurantes.sockets.CacheMesaHeader;
import codecoffe.restaurantes.sockets.CacheTodasMesas;
import codecoffe.restaurantes.sockets.CacheTodosFuncionarios;
import codecoffe.restaurantes.sockets.CacheTodosPedidos;
import codecoffe.restaurantes.sockets.CacheTodosProdutos;
import codecoffe.restaurantes.sockets.Client;
import codecoffe.restaurantes.utilitarios.Configuracao;
import codecoffe.restaurantes.utilitarios.Header;
import codecoffe.restaurantes.utilitarios.Usuario;
import codecoffe.restaurantes.utilitarios.UtilCoffe;

/* Coração do programa, gerencia e conecta todas as ações. */
public class RestauranteClient implements SocketsRecebido, AtualizarPainel
{
	private Configuracao config;
	private Recursos recursos;
	private Client cliente;
	private FramePrincipal framePrincipal;
	private PainelClientes painelClientes;
	private PainelVendaRapida painelVendaRapida;
	private PainelCozinha painelCozinha;
	private PainelMesas painelMesas;
	private PainelVendaMesa painelVendaMesa;
	
	public RestauranteClient(InetAddress host, int portaConnect) throws IOException
	{
		new Loader(this, host, portaConnect, this);
		
		/*
		// obtendo as configurações
		config = new Configuracao();
		config.setModo(UtilCoffe.CLIENT);
		
		// iniciando o cliente em thread
		cliente = new Client(this, this);
		cliente.atualizaConexao(host, portaConnect);
		new Thread(cliente).start();
		
		// puxar todos os dados do banco de dados, esse objeto vai guardar tudo
		// e todos os outros vão usar esse como referência.
		recursos = new Recursos(config);
		
		ThreadUtils.sleepSafely(2000);
		
		framePrincipal = new FramePrincipal("CodeCoffe Restaurantes (TERMINAL) " + UtilCoffe.VERSAO, config, cliente, this);
		painelClientes = new PainelClientes(config, cliente, recursos.getClientes(), this);
		painelVendaRapida = new PainelVendaRapida(config, cliente, recursos.getProdutos(), recursos.getFuncionarios(), this);
		painelCozinha = new PainelCozinha(config, cliente, recursos.getPedidos(), recursos.getProdutos());
		painelMesas = new PainelMesas(config, cliente, recursos.getMesas(), this);
		painelVendaMesa = new PainelVendaMesa(config, cliente, recursos.getProdutos(), 
				recursos.getFuncionarios(), recursos.getPedidos(), this, painelMesas);
		
		framePrincipal.adicionarPainel(painelMesas, "Menu Mesas");
		framePrincipal.adicionarPainel(painelClientes, "Menu Clientes");
		framePrincipal.adicionarPainel(painelVendaRapida, "Menu Venda Rapida");
		framePrincipal.adicionarPainel(painelCozinha, "Menu Cozinha");
		framePrincipal.adicionarPainel(painelVendaMesa, "Menu Venda Mesa");
		
		framePrincipal.abrir();*/
	}
	
	class Loader implements Runnable
	{
		private Loading loadingFrame;
		private RestauranteClient chamando;
		private List<Boolean> checkConfig;
		
		public Loader(RestauranteClient restaurante, InetAddress host, int portaConnect, SocketsRecebido listener) throws IOException
		{
			config = new Configuracao();
			config.setModo(UtilCoffe.CLIENT);
			
			recursos = new Recursos(config);
			// iniciando o cliente em thread
			cliente = new Client();
			cliente.atualizaConexao(host, portaConnect);
			new Thread(cliente).start();
			
			checkConfig = new ArrayList<Boolean>();
			checkConfig.add(new Boolean(false));
			checkConfig.add(new Boolean(false));
			checkConfig.add(new Boolean(false));
			checkConfig.add(new Boolean(false));
			checkConfig.add(new Boolean(false));
			checkConfig.add(new Boolean(false));
			
			cliente.setListenerSockets(new SocketsRecebido() {
				@Override
				public void objetoRecebido(Object objeto,
						ObjectOutputStream client) throws IOException {
					
					if(objeto instanceof Configuracao)
					{
						config.atualizarConfiguracao((Configuracao) objeto);
						setBool(checkConfig, 0);
					}
					else if(objeto instanceof CacheTodosFuncionarios)
					{
						recursos.atualizarTodosFuncionarios(((CacheTodosFuncionarios) objeto).getFuncionarios());
						setBool(checkConfig, 1);
					}
					else if(objeto instanceof CacheTodosPedidos)
					{
						recursos.atualizarTodosPedidos(((CacheTodosPedidos) objeto).getTodosPedidos());
						setBool(checkConfig, 2);
					}
					else if(objeto instanceof CacheTodasMesas)
					{
						recursos.atualizarTodasMesas((CacheTodasMesas) objeto);
						setBool(checkConfig, 3);
					}
					else if(objeto instanceof CacheTodosProdutos)
					{
						recursos.atualizarTodosProdutos((CacheTodosProdutos) objeto);
						setBool(checkConfig, 4);
					}
					else if(objeto instanceof CacheClientes)
					{
						if(((CacheClientes) objeto).getHeader() == UtilCoffe.CLIENTE_ATUALIZAR) {
							recursos.atualizarTodosClientes(((CacheClientes) objeto).getListaClientes());
							setBool(checkConfig, 5);
						}
					}
					
					if(checkBool(checkConfig)) {
						cliente.setListenerSockets(chamando);
						cliente.setListenerPainel(chamando);
						System.out.println("Todos os recursos recebidos. Começar loading!");
						iniciar();
					}
				}
			});
			
			chamando = restaurante;
			loadingFrame = new Loading();
			loadingFrame.setProgress("Aguardando recursos do servidor...", 1);
		}
		
		protected void iniciar()  {
			new Thread(this).start();
		}

		@Override
		public void run() 
		{
			ThreadUtils.sleepSafely(2000);
			loadingFrame.setProgress("Recursos recebidos.", 5);
			ThreadUtils.sleepSafely(1000);
			
			int i = 5;
			while(i < 101)
			{
				switch(i)
				{
					case 10:
					{
						ThreadUtils.sleepSafely(300);
						loadingFrame.setProgress("Carregando painel principal...");
						framePrincipal = new FramePrincipal("CodeCoffe Restaurantes (TERMINAL) " + UtilCoffe.VERSAO, config, cliente, chamando);
						break;
					}
					case 30:
					{
						loadingFrame.setProgress("Carregando programa...");
						painelClientes = new PainelClientes(config, cliente, recursos.getClientes(), chamando);
						painelVendaRapida = new PainelVendaRapida(config, cliente, recursos.getProdutos(), recursos.getFuncionarios(), chamando);
						painelCozinha = new PainelCozinha(config, cliente, recursos.getPedidos(), recursos.getProdutos());
						painelMesas = new PainelMesas(config, cliente, recursos.getMesas(), chamando);
						painelVendaMesa = new PainelVendaMesa(config, cliente, recursos.getProdutos(), 
								recursos.getFuncionarios(), recursos.getPedidos(), chamando, painelMesas);
						break;
					}
					case 80:
					{
						framePrincipal.adicionarPainel(painelMesas, "Menu Mesas");
						framePrincipal.adicionarPainel(painelClientes, "Menu Clientes");
						framePrincipal.adicionarPainel(painelVendaRapida, "Menu Venda Rapida");
						framePrincipal.adicionarPainel(painelCozinha, "Menu Cozinha");
						framePrincipal.adicionarPainel(painelVendaMesa, "Menu Venda Mesa");
						break;
					}
				}
				
				loadingFrame.setProgress(i);
				i++;
				ThreadUtils.sleepSafely(30);
			}
			
			framePrincipal.abrir();
			loadingFrame.dispose();
		}
		
		public void setBool(List<Boolean> load, int index) {
			load.set(index, true);
		}
		
		public boolean checkBool(List<Boolean> load)
		{
			boolean flag_bool = true;
			
			for(Boolean bool : load)
			{
				if(!bool) {
					flag_bool = false;
					break;
				}
			}
			
			return flag_bool;
		}
	}

	@Override
	public void objetoRecebido(Object dataRecebida, ObjectOutputStream client) 
	{
		if(dataRecebida instanceof String)
		{
			String decodifica = (String)dataRecebida;
			if(decodifica.equals("BYE"))
			{
				cliente.finalizaPrograma(1);
			}
			else if(decodifica.equals("WRONG VERSION"))
			{
				cliente.finalizaPrograma(2);
			}
		}
		else if(dataRecebida instanceof CacheMesaHeader)
		{
			CacheMesaHeader m = (CacheMesaHeader) dataRecebida;
			
			if(m.getHeader() == UtilCoffe.MESA_TRANSFERIR)
			{
				painelMesas.setMesa(m.getMesaId(), new Venda());
				painelMesas.setMesa(m.getHeaderExtra(), m.getMesaVenda());
				painelMesas.atualizarMesa(m.getMesaId());
				painelMesas.atualizarMesa(m.getHeaderExtra());
				
				if(Usuario.INSTANCE.getOlhando() == m.getMesaId())
					painelVendaMesa.setMesa(m.getMesaId(), painelMesas.getMesa(m.getMesaId()));
				else if(Usuario.INSTANCE.getOlhando() == m.getHeaderExtra())
					painelVendaMesa.setMesa(m.getHeaderExtra(), painelMesas.getMesa(m.getHeaderExtra()));
				
				NotificationManager.setLocation(2);
				NotificationManager.showNotification(framePrincipal, config.getTipoNome() + " " + (m.getMesaId()+1) + " transferida para " + (m.getHeaderExtra()+1), 
						new ImageIcon(getClass().getClassLoader().getResource("imgs/notifications_ok.png"))).setDisplayTime(2000);
			}
			else
			{
				painelMesas.setMesa(m.getMesaId(), m.getMesaVenda());
				painelMesas.atualizarMesa(m.getMesaId());
				
				if(Usuario.INSTANCE.getOlhando() == m.getMesaId())
					painelVendaMesa.setMesa(m.getMesaId(), painelMesas.getMesa(m.getMesaId()));
			}
			
			if(m.getHeader() == UtilCoffe.MESA_ERROR)
				JOptionPane.showMessageDialog(null, "Houve um erro no programa principal e não foi possível fazer a ação!");
		}
		else if(dataRecebida instanceof CacheAviso)
		{
			CacheAviso aviso = (CacheAviso) dataRecebida;
			if(aviso.getClasse() == UtilCoffe.CLASSE_CLIENTES)
			{
				JOptionPane.showMessageDialog(null, aviso.getMensagem(), 
						aviso.getTitulo(), JOptionPane.ERROR_MESSAGE);
			}
			else if(aviso.getClasse() == UtilCoffe.CLASSE_VENDA_MESA)
			{
				painelVendaMesa.avisoRecebido((CacheAviso) dataRecebida);
			}
			else if(aviso.getClasse() == UtilCoffe.CLASSE_VENDA_RAPIDA)
			{
				painelVendaRapida.avisoRecebido((CacheAviso) dataRecebida);
			}
			else if(aviso.getClasse() == UtilCoffe.CLASSE_CLIENTES)
			{
				JOptionPane.showMessageDialog(null, aviso.getMensagem(), 
						aviso.getTitulo(), JOptionPane.ERROR_MESSAGE);
			}
		}
		else if(dataRecebida instanceof CacheTodosFuncionarios)
		{
			recursos.atualizarTodosFuncionarios(((CacheTodosFuncionarios) dataRecebida).getFuncionarios());
			
			if(painelVendaRapida != null)
				painelVendaRapida.refreshModelFuncionarios();
			
			if(painelVendaMesa != null)
				painelVendaMesa.refreshModelFuncionarios();
		}
		else if(dataRecebida instanceof CacheTodosPedidos)
		{
			recursos.atualizarTodosPedidos(((CacheTodosPedidos) dataRecebida).getTodosPedidos());
			if(painelCozinha != null)
				painelCozinha.atualizaPainelPedidos();
		}
		else if(dataRecebida instanceof CacheTodosProdutos)
		{
			recursos.atualizarTodosProdutos((CacheTodosProdutos) dataRecebida);
			
			if(painelVendaRapida != null)
				painelVendaRapida.refreshModel();
			
			if(painelVendaMesa != null)
				painelVendaMesa.refreshModel();
		}
		else if(dataRecebida instanceof CacheTodasMesas)
		{
			recursos.atualizarTodasMesas((CacheTodasMesas) dataRecebida);
			if(painelMesas != null)
				painelMesas.atualizarTodasMesas();
		}
		else if(dataRecebida instanceof Pedido)
		{
			if(painelCozinha != null)
				painelCozinha.atualizaPedido((Pedido) dataRecebida);
		}
		else if(dataRecebida instanceof Configuracao)
		{
			config.atualizarConfiguracao((Configuracao) dataRecebida);
			
			if(painelVendaRapida != null)
				painelVendaRapida.refreshConfig();
			
			if(painelVendaMesa != null)
				painelVendaMesa.refreshConfig();
		}
		else if(dataRecebida instanceof CacheAutentica)
		{
			CacheAutentica ca = (CacheAutentica)dataRecebida;
			switch(ca.getHeader())
			{
				case 1:
				{			
					Usuario.INSTANCE.setNome(ca.getNome());
					Usuario.INSTANCE.setLevel(ca.getLevel());
					framePrincipal.logar(ca.getNome());
					break;
				}
				case 2:
				{
					JOptionPane.showMessageDialog(null, "Usuário não encontrado!");
					break;
				}
				case 3:
				{
					JOptionPane.showMessageDialog(null, "Senha incorreta!");
					break;
				}
				default:
				{
					JOptionPane.showMessageDialog(null, "Não pode entrar com o mesmo login do Principal!");
				}
			}
		}
		else if(dataRecebida instanceof CacheClientes)
		{
			switch(((CacheClientes) dataRecebida).getHeader())
			{
				case UtilCoffe.CLIENTE_ADICIONAR:
				{
					painelClientes.adicionarCliente(((CacheClientes) dataRecebida).getCliente(), 
							config, ((CacheClientes) dataRecebida).getAtendente(), null);
					break;
				}
				case UtilCoffe.CLIENTE_REMOVER:
				{
					painelClientes.removerClientes(((CacheClientes) dataRecebida).getCliente());
					break;
				}
				case UtilCoffe.CLIENTE_EDITAR:
				{
					painelClientes.editarClientes(((CacheClientes) dataRecebida).getCliente(), 
							config, ((CacheClientes) dataRecebida).getAtendente(), null);
					break;
				}
				default:	// update de todos os clientes!
				{
					recursos.atualizarTodosClientes(((CacheClientes) dataRecebida).getListaClientes());
				}
			}
		}
	}

	@Override
	public void atualizarPainel(Object objeto) 
	{
		if(objeto instanceof Header)
		{
			Header h = (Header) objeto;
			gerenciaHeaders(h);
		}
		else if(objeto instanceof Pedido)
		{
			Pedido p = (Pedido)objeto;
			cliente.enviarObjeto(p);
		}
	}
	
	private void gerenciaHeaders(Header h) 
	{
		switch(h.getHeader())
		{
			case UtilCoffe.UPDATE_PRODUTOS:
			{
				painelVendaRapida.refreshModel();
				painelVendaMesa.refreshModel();
				break;
			}
			case UtilCoffe.ABRIR_CLIENTES:
			{
				framePrincipal.abrirMenu("Menu Clientes");
				break;
			}
			case UtilCoffe.ABRIR_MENU:
			{
				framePrincipal.abrirMenu((String) h.getExtra());
				break;
			}
			case UtilCoffe.ABRIR_VENDA_RAPIDA:
			{
				painelVendaRapida.setFiado((Clientes) h.getExtra());
				framePrincipal.abrirMenu("Menu Venda Rapida");
				break;
			}
			case UtilCoffe.UPDATE_CALLBACK:
			{
				painelClientes.setCallBack(h, h.getExtra());
				break;
			}
			case UtilCoffe.UPDATE_LEGENDA:
			{
				framePrincipal.alterarLegenda((String) h.getExtra());
				break;
			}
			case UtilCoffe.UPDATE_VENDA_MESA:
			{
				Usuario.INSTANCE.setOlhando((int) h.getExtra());
				painelVendaMesa.setMesa((int) h.getExtra(), (Venda) h.getExtra2());
				break;
			}
			case UtilCoffe.ENABLED:
			{
				framePrincipal.setEnabled((boolean) h.getExtra());
				break;
			}
		}
	}
}
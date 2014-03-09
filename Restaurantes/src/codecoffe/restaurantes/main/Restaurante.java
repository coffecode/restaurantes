package codecoffe.restaurantes.main;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.Date;

import com.alee.utils.ThreadUtils;

import codecoffe.restaurantes.eventos.AtualizarPainel;
import codecoffe.restaurantes.eventos.BancoVerificado;
import codecoffe.restaurantes.eventos.SocketsRecebido;
import codecoffe.restaurantes.graficos.FramePrincipal;
import codecoffe.restaurantes.graficos.PainelClientes;
import codecoffe.restaurantes.graficos.PainelConfiguracao;
import codecoffe.restaurantes.graficos.PainelCozinha;
import codecoffe.restaurantes.graficos.PainelErro;
import codecoffe.restaurantes.graficos.PainelFuncionarios;
import codecoffe.restaurantes.graficos.PainelMesas;
import codecoffe.restaurantes.graficos.PainelProdutos;
import codecoffe.restaurantes.graficos.PainelVendaMesa;
import codecoffe.restaurantes.graficos.PainelVendaRapida;
import codecoffe.restaurantes.graficos.PainelVendas;
import codecoffe.restaurantes.mysql.Query;
import codecoffe.restaurantes.primitivas.Clientes;
import codecoffe.restaurantes.primitivas.Pedido;
import codecoffe.restaurantes.primitivas.Venda;
import codecoffe.restaurantes.sockets.CacheAutentica;
import codecoffe.restaurantes.sockets.CacheClientes;
import codecoffe.restaurantes.sockets.CacheImpressao;
import codecoffe.restaurantes.sockets.CacheMesaHeader;
import codecoffe.restaurantes.sockets.CacheTodasMesas;
import codecoffe.restaurantes.sockets.CacheTodosFuncionarios;
import codecoffe.restaurantes.sockets.CacheTodosPedidos;
import codecoffe.restaurantes.sockets.CacheVendaFeita;
import codecoffe.restaurantes.sockets.Servidor;
import codecoffe.restaurantes.sockets.ServidorBroadcast;
import codecoffe.restaurantes.utilitarios.Configuracao;
import codecoffe.restaurantes.utilitarios.DiarioLog;
import codecoffe.restaurantes.utilitarios.Header;
import codecoffe.restaurantes.utilitarios.Recibo;
import codecoffe.restaurantes.utilitarios.Usuario;
import codecoffe.restaurantes.utilitarios.UtilCoffe;

/* Coração do programa, gerencia e conecta todas as ações. */

public class Restaurante implements SocketsRecebido, AtualizarPainel
{
	private Configuracao config;
	private Recursos recursos;
	private Servidor servidor;
	private ServidorBroadcast servidorUDP;
	private FramePrincipal framePrincipal;
	private PainelConfiguracao painelConfiguracao;
	private PainelProdutos painelProdutos;
	private PainelClientes painelClientes;
	private PainelFuncionarios painelFuncionarios;
	private PainelVendas painelVendas;
	private PainelVendaRapida painelVendaRapida;
	private PainelCozinha painelCozinha;
	private PainelMesas painelMesas;
	private PainelVendaMesa painelVendaMesa;
	
	public Restaurante()
	{
		final Loader carregar = new Loader(this, this);
		carregar.getLoadingFrame().verificarMYSQL(new BancoVerificado() {
			@Override
			public void bancoVerificado() {
				new Thread(carregar).start();
			}
		});
		
		/*
		// obtendo as configurações do MySQL
		config = new Configuracao();
		config.setModo(UtilCoffe.SERVER);
		config.atualizarConfiguracao();
		
		// iniciando os servidores em threads pois ficam ativos 100% do tempo
		servidor = new Servidor(this, portaConnect);
		new Thread(servidor).start();
		new Thread(new ServidorBroadcast(portaConnect)).start();
		
		// puxar todos os dados do banco de dados, esse objeto vai guardar tudo
		// e todos os outros vão usar esse como referência.
		recursos = new Recursos(config);
		recursos.atualizarTodosProdutos();
		recursos.atualizarTodosClientes();
		recursos.atualizarTodosPedidos();
		recursos.atualizarTodosFuncionarios();
		recursos.atualizarTodasMesas();
		
		framePrincipal = new FramePrincipal("CodeCoffe Restaurantes (PRINCIPAL) " + UtilCoffe.VERSAO, config, servidor);
		painelConfiguracao = new PainelConfiguracao(config);
		painelProdutos = new PainelProdutos(servidor, recursos.getProdutos(), this);
		painelClientes = new PainelClientes(config, servidor, recursos.getClientes(), this);
		painelCozinha = new PainelCozinha(config, servidor, recursos.getPedidos(), recursos.getProdutos());
		painelFuncionarios = new PainelFuncionarios(recursos.getFuncionarios(), servidor);
		painelVendaRapida = new PainelVendaRapida(config, servidor, recursos.getProdutos(), recursos.getFuncionarios(), this);
		painelMesas = new PainelMesas(config, servidor, recursos.getMesas(), this);
		painelVendaMesa = new PainelVendaMesa(config, servidor, recursos.getProdutos(), 
									recursos.getFuncionarios(), recursos.getPedidos(), this, painelMesas);
		painelVendas = new PainelVendas(config, this);

		framePrincipal.adicionarPainel(painelMesas, "Menu Mesas");
		framePrincipal.adicionarPainel(painelConfiguracao, "Menu Configuracao");
		framePrincipal.adicionarPainel(painelProdutos, "Menu Produtos");
		framePrincipal.adicionarPainel(painelClientes, "Menu Clientes");
		framePrincipal.adicionarPainel(painelCozinha, "Menu Cozinha");
		framePrincipal.adicionarPainel(painelFuncionarios, "Menu Funcionarios");
		framePrincipal.adicionarPainel(painelVendas, "Menu Vendas");
		framePrincipal.adicionarPainel(painelVendaRapida, "Menu Venda Rapida");
		framePrincipal.adicionarPainel(painelVendaMesa, "Menu Venda Mesa");*/
	}
	
	class Loader implements Runnable
	{
		private Loading loadingFrame;
		private Restaurante chamando;
		
		public Loader(Restaurante restaurante, SocketsRecebido listener)
		{
			chamando = restaurante;
			loadingFrame = new Loading();
			
			servidor = new Servidor(chamando, Inicio.portaConnect);
			servidorUDP = new ServidorBroadcast(Inicio.portaConnect);
		}
		
		@Override
		public void run() 
		{	
			int i = 1;
			while(i < 101)
			{
				switch(i)
				{
					case 2:
					{
						ThreadUtils.sleepSafely(250);
						config = new Configuracao();
						config.setModo(UtilCoffe.SERVER);
						config.atualizarConfiguracao();
						loadingFrame.setProgress("Carregando configuração...");
						break;
					}
					case 5:
					{
						ThreadUtils.sleepSafely(250);
						recursos = new Recursos(config);
						recursos.atualizarTodosProdutos();
						recursos.atualizarTodosClientes();
						recursos.atualizarTodosPedidos();
						recursos.atualizarTodosFuncionarios();
						recursos.atualizarTodasMesas();
						loadingFrame.setProgress("Carregando conteúdo do banco...");
						break;
					}
					case 7:
					{
						ThreadUtils.sleepSafely(250);
						framePrincipal = new FramePrincipal("CodeCoffe Restaurantes (PRINCIPAL) " + UtilCoffe.VERSAO, config, servidor, chamando);
						loadingFrame.setProgress("Carregando painel principal...");
						break;
					}
					case 9:
					{
						loadingFrame.setProgress("Carregando programa...");
						ThreadUtils.sleepSafely(1000);
						painelConfiguracao = new PainelConfiguracao(config, chamando);
						painelProdutos = new PainelProdutos(servidor, recursos.getProdutos(), chamando);
						painelClientes = new PainelClientes(config, servidor, recursos.getClientes(), chamando);
						painelCozinha = new PainelCozinha(config, servidor, recursos.getPedidos(), recursos.getProdutos());
						painelFuncionarios = new PainelFuncionarios(recursos.getFuncionarios(), servidor, chamando);
						painelVendaRapida = new PainelVendaRapida(config, servidor, recursos.getProdutos(), recursos.getFuncionarios(), chamando);
						painelMesas = new PainelMesas(config, servidor, recursos.getMesas(), chamando);
						painelVendaMesa = new PainelVendaMesa(config, servidor, recursos.getProdutos(), 
													recursos.getFuncionarios(), recursos.getPedidos(), chamando, painelMesas);
						painelVendas = new PainelVendas(config, chamando);
						break;
					}
					case 80:
					{
						framePrincipal.adicionarPainel(painelMesas, "Menu Mesas");
						framePrincipal.adicionarPainel(painelConfiguracao, "Menu Configuracao");
						framePrincipal.adicionarPainel(painelProdutos, "Menu Produtos");
						framePrincipal.adicionarPainel(painelClientes, "Menu Clientes");
						framePrincipal.adicionarPainel(painelCozinha, "Menu Cozinha");
						framePrincipal.adicionarPainel(painelFuncionarios, "Menu Funcionarios");
						framePrincipal.adicionarPainel(painelVendas, "Menu Vendas");
						framePrincipal.adicionarPainel(painelVendaRapida, "Menu Venda Rapida");
						framePrincipal.adicionarPainel(painelVendaMesa, "Menu Venda Mesa");
						break;
					}
				}
				
				loadingFrame.setProgress(i);
				i++;
				ThreadUtils.sleepSafely(40);
			}
			
			framePrincipal.abrir();
			
			// iniciando os servidores em threads pois ficam ativos 100% do tempo
			new Thread(servidor).start();
			new Thread(servidorUDP).start();
			
			loadingFrame.dispose();
		}

		public Loading getLoadingFrame() {
			return loadingFrame;
		}
	}

	@Override
	public void objetoRecebido(Object dataRecebida, ObjectOutputStream client) throws IOException 
	{
		if(dataRecebida instanceof String)
		{
			String decodifica = (String)dataRecebida;
			if(decodifica.equals("UPDATE PRODUTOS"))
			{
				System.out.println("Enviando lista de produtos");
				client.reset();
				client.writeObject(recursos.getProdutos());
			}
			else if(decodifica.equals("UPDATE CONFIGURACAO"))
			{
				System.out.println("Enviando configuracao");
				client.reset();
				client.writeObject(config);
			}
			else if(decodifica.equals("UPDATE MESAS"))
			{
				System.out.println("Enviando lista de mesas");
				client.reset();
				client.writeObject(new CacheTodasMesas(recursos.getMesas()));
			}
			else if(decodifica.equals("UPDATE FUNCIONARIOS"))
			{
				System.out.println("Enviando lista de funcionários");
				client.reset();
				client.writeObject(new CacheTodosFuncionarios(recursos.getFuncionarios()));
			}
			else if(decodifica.equals("UPDATE PRODUTOS"))
			{
				System.out.println("Enviando lista de produtos");
				client.reset();
				client.writeObject(recursos.getProdutos());
			}
			else if(decodifica.equals("UPDATE PEDIDOS"))
			{
				System.out.println("Enviando lista de pedidos");
				client.reset();
				client.writeObject(new CacheTodosPedidos(recursos.getPedidos()));
			}
			else if(decodifica.equals("UPDATE CLIENTES"))
			{
				System.out.println("Enviando lista de clientes");
				client.reset();
				client.writeObject(new CacheClientes(recursos.getClientes(), 
						UtilCoffe.CLIENTE_ATUALIZAR));
			}
			else if(decodifica.contains(";QUIT"))
			{
				String nome = "";

				for(int i = 0; i < decodifica.length(); i++)
				{
					if(decodifica.charAt(i) == ';')
						break;

					nome += decodifica.charAt(i);
				}

				if(!UtilCoffe.vaziu(nome))
					DiarioLog.add(nome, "Saiu do sistema.", 9);
			}
		}
		else if(dataRecebida instanceof CacheMesaHeader)
		{
			painelMesas.atualizarMesa((CacheMesaHeader) dataRecebida, client);
		}
		else if(dataRecebida instanceof CacheImpressao)
		{
			Recibo.gerarNotaVenda(config, ((CacheImpressao)dataRecebida));
		}
		else if(dataRecebida instanceof CacheVendaFeita)
		{
			if(((CacheVendaFeita)dataRecebida).getClasse() == UtilCoffe.CLASSE_VENDA_RAPIDA)
				painelVendaRapida.enviarVenda(((CacheVendaFeita)dataRecebida), client);
			else
				painelVendaMesa.enviarVenda(((CacheVendaFeita)dataRecebida), client);
		}
		else if(dataRecebida instanceof Pedido)
		{
			Pedido p = (Pedido) dataRecebida;
			
			if(p.getHeader() == UtilCoffe.PEDIDO_ADICIONA)
				p.setHora(new Date());
			
			p.setUltimaEdicao(new Date());
			painelCozinha.atualizaPedido(p);
		}
		else if(dataRecebida instanceof CacheClientes)
		{
			switch(((CacheClientes) dataRecebida).getHeader())
			{
				case UtilCoffe.CLIENTE_ADICIONAR:
				{
					painelClientes.adicionarCliente(((CacheClientes) dataRecebida).getCliente(), 
							config, ((CacheClientes) dataRecebida).getAtendente(), client);
					break;
				}
				case UtilCoffe.CLIENTE_EDITAR:
				{
					painelClientes.editarClientes(((CacheClientes) dataRecebida).getCliente(), 
							config, ((CacheClientes) dataRecebida).getAtendente(), client);
					break;
				}
				default: {}
			}
		}
		else if(dataRecebida instanceof CacheAutentica)
		{
			CacheAutentica autentica = (CacheAutentica)dataRecebida;
			Query teste = new Query();

			try {
				String formatacao;
				formatacao = "SELECT password, level, nome FROM funcionarios WHERE username = '" + autentica.getUsername() + "';";							
				teste.executaQuery(formatacao);

				if(teste.next())
				{
					if(teste.getString("password").equals(autentica.getPassword()))
					{
						if(teste.getString("nome").equals(Usuario.INSTANCE.getNome()))
						{
							autentica.setHeader(4);
							client.reset();
							client.writeObject(autentica);									
						}
						else
						{
							autentica.setHeader(1);
							autentica.setNome(teste.getString("nome"));
							autentica.setLevel(teste.getInt("level"));
							DiarioLog.add(teste.getString("nome"), "Fez login no sistema.", 8);

							client.reset();
							client.writeObject(autentica);
						}

						teste.fechaConexao();
					}
					else
					{
						teste.fechaConexao();
						autentica.setHeader(3);
						client.reset();
						client.writeObject(autentica);
					}
				}
				else
				{
					autentica.setHeader(2);
					client.reset();
					client.writeObject(autentica);
				}							
			} catch (SQLException | ClassNotFoundException e) {
				e.printStackTrace();
				new PainelErro(e);
				autentica.setHeader(2);
				client.reset();
				client.writeObject(autentica);
				teste.fechaConexao();
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
			Pedido p = (Pedido) objeto;
			
			if(p.getHeader() == UtilCoffe.PEDIDO_ADICIONA)
				p.setHora(new Date());
			
			p.setUltimaEdicao(new Date());
			painelCozinha.atualizaPedido(p);
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
				painelProdutos.refreshCategorias();
				break;
			}
			case UtilCoffe.UPDATE_VENDAS:
			{
				painelVendas.ultimasVendasRefresh();
				break;
			}
			case UtilCoffe.UPDATE_FIADOS:
			{
				painelVendas.refresh();
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
			case UtilCoffe.RELOAD:
			{
				System.out.println("Terminando o servidor");
				servidor.restart();
				servidorUDP.setServerON(false);
				framePrincipal.dispose();
				
				ThreadUtils.sleepSafely(500);
				
				final Loader carregar = new Loader(this, this);
				carregar.getLoadingFrame().verificarMYSQL(new BancoVerificado() {
					@Override
					public void bancoVerificado() {
						new Thread(carregar).start();
					}
				});
				break;
			}
			case UtilCoffe.UPDATE_FUNCIONARIOS:
			{
				painelVendaRapida.refreshModelFuncionarios();
				painelVendaMesa.refreshModelFuncionarios();
				break;
			}
			case UtilCoffe.UPDATE_CONFIG:
			{
				painelVendaRapida.refreshConfig();
				painelVendaMesa.refreshConfig();
				servidor.enviaTodos(config);
				break;
			}
		}
	}
}
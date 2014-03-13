package codecoffe.restaurantes.graficos;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;

import net.miginfocom.swing.MigLayout;
import codecoffe.restaurantes.eventos.PedidoAlterado;
import codecoffe.restaurantes.mysql.Query;
import codecoffe.restaurantes.primitivas.Categoria;
import codecoffe.restaurantes.primitivas.Pedido;
import codecoffe.restaurantes.primitivas.ProdutoVenda;
import codecoffe.restaurantes.sockets.CacheTodosProdutos;
import codecoffe.restaurantes.sockets.Client;
import codecoffe.restaurantes.sockets.Servidor;
import codecoffe.restaurantes.utilitarios.CompararTempo;
import codecoffe.restaurantes.utilitarios.Configuracao;
import codecoffe.restaurantes.utilitarios.UtilCoffe;

import com.alee.extended.window.WebPopOver;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.managers.notification.NotificationManager;

public class PainelCozinha extends JPanel
{
	private static final long serialVersionUID = 1L;
	private JPanel pedidosCozinha;
	private List<Pedido> todosPedidos;
	private Timer timer, timer2;
	private WebScrollPane scroll;
	private boolean flag_refresh, flag_musica;
	private URL musica;
	private JPopupMenu popup;
	private List<Integer> blacklist;
	private CacheTodosProdutos todosProdutos;
	private Configuracao config;
	private Object modoPrograma;
	
	public PainelCozinha(Configuracao cfg, Object modo, List<Pedido> pedidos, CacheTodosProdutos produtos)
	{
		config = cfg;
		modoPrograma = modo;
		todosPedidos = pedidos;
		todosProdutos = produtos;
		
		blacklist = new ArrayList<Integer>();
		flag_refresh = false;
		flag_musica = false;
		musica = getClass().getClassLoader().getResource("imgs/novo_pedido.wav");
		
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Pedidos"));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		pedidosCozinha = new JPanel();
		pedidosCozinha.setLayout(new GridBagLayout());
		scroll = new WebScrollPane(pedidosCozinha, false);
		scroll.setPreferredSize(getSize());
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.getVerticalScrollBar().setUnitIncrement(20);
		//scroll.getVerticalScrollBar().setPreferredSize(new Dimension(50, 200));
		
		add(scroll);
		
		ActionListener al = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				filtroCozinha();
			}
		};
		
        popup = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("Filtrar pedidos");
        menuItem.addActionListener(al);
        popup.add(menuItem);
        
        pedidosCozinha.addMouseListener(new MouseAdapter()
        {
            public void mouseReleased(MouseEvent e)
            {
                if (e.isPopupTrigger()) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
		
		timer = new Timer();
		timer.schedule(new UpdateTask(), 0, 8*1000);

		timer2 = new Timer();
		timer2.schedule(new VerificaTask(), 0, 5*1000);
		
		flag_refresh = true;
	}
	
	public void filtroCozinha() 
	{
		JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
		final WebPopOver popOver = new WebPopOver(topFrame);
		popOver.setModal(true);
		popOver.setMargin(10);
		popOver.setMovable(false);
		popOver.setLayout(new MigLayout());
		
		popOver.add(new JLabel("<html><b>Filtro de Categorias</b></html>"), "span, align center, wrap");
		
		final JPanel panelCategorias = new JPanel(new MigLayout());
		panelCategorias.setFocusable(false);
		
		List<Categoria> categorias = todosProdutos.getCategorias();
		
		for(int i = 0; i < categorias.size() ; i ++)
		{
			CategoriaCheckBox checkbox = new CategoriaCheckBox(categorias.get(i).toString(), categorias.get(i).getIdCategoria());
			checkbox.setPreferredSize(new Dimension(150, 30));
			checkbox.setSelected(true);
			
			for(int x = 0; x < blacklist.size() ; x ++)
			{
				if(categorias.get(i).getIdCategoria() == blacklist.get(x))
				{
					checkbox.setSelected(false);
					break;
				}
			}
			
			checkbox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					
					CategoriaCheckBox checkbox = (CategoriaCheckBox) e.getSource();
					if(checkbox.isSelected())
					{
						blacklist.remove((Object) checkbox.getCatID());
						updateFiltro();
					}
					else
					{						
						blacklist.add(checkbox.getCatID());
						updateFiltro();
					}
				}
			});
			
			panelCategorias.add(checkbox, "wrap");
		}
		
		JButton close = new JButton("Fechar");
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				popOver.dispose();
			}
		});
		
		WebScrollPane scroll = new WebScrollPane(panelCategorias, true);
		scroll.setPreferredSize(new Dimension(220, 250));
		scroll.getViewport().setBackground(new Color(237, 237, 237));
		scroll.setFocusable(false);
		
		popOver.add(scroll, "gaptop 15px, align center, wrap");
		popOver.add(close, "gaptop 15px, span, align right");
		popOver.show(topFrame);
	}
	
	class CategoriaCheckBox extends JCheckBox
	{
		private static final long serialVersionUID = 1L;
		private int catID;
		
		public CategoriaCheckBox(String txt, int id)
		{
			super(txt);
			this.catID = id;
		}

		public int getCatID() {
			return catID;
		}

		public void setCatID(int catID) {
			this.catID = catID;
		}
	}
	
	public void updateFiltro()	/* código mal otimizado, mas é o que tem pra agora */
	{
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				for(int i = 0; i < pedidosCozinha.getComponentCount(); i++)
				{
					PainelPedido pp = (PainelPedido)pedidosCozinha.getComponent(i);
					int idproduto = pp.getPedido().getProduto().getIdUnico();
					List<Categoria> categorias = todosProdutos.getCategorias();
					boolean flag_quit = false;
					
					for(int x = 0; x < categorias.size() ; x ++)
					{
						if(blacklist.contains((Object) categorias.get(x).getIdCategoria()))
						{
							for(int z = 0; z < categorias.get(x).getProdutos().size(); z++)
							{
								if(categorias.get(x).getProdutos().get(z).getIdUnico() == idproduto) {
									flag_quit = true;
									pp.setVisible(false);
									break;
								}
							}
							
							if(flag_quit)
								break;	
						}
						else
						{
							if(!pp.isVisible())
								pp.setVisible(true);
						}
					}
				}
			}
		});
	}
	
	public void atualizaPainelPedidos()
	{
		flag_refresh = true;
	}
	
	class VerificaTask extends TimerTask 
	{
        public void run() 
        {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
		        	for(int i = 0; i < todosPedidos.size(); i++)
		        	{
		        		if(todosPedidos.get(i).getHeader() == UtilCoffe.PEDIDO_ADICIONA)
		        		{
		        			long duration = System.currentTimeMillis() - todosPedidos.get(i).getUltimaEdicao().getTime();
		        			long seconds = TimeUnit.MILLISECONDS.toSeconds(duration);
		        			
		        			if(seconds > 5) // demorar um pouco antes de adicionar na lista
		        			{
		        				flag_refresh = true;
		        				todosPedidos.get(i).setHeader(UtilCoffe.PEDIDO_STATUS);
		        				todosPedidos.get(i).setStatus(UtilCoffe.PEDIDO_NOVO);
		        			}
		        		}
		        		else if(todosPedidos.get(i).getStatus() == UtilCoffe.PEDIDO_NOVO)
		        		{
		        			long duration = System.currentTimeMillis() - todosPedidos.get(i).getUltimaEdicao().getTime();
		        			long seconds = TimeUnit.MILLISECONDS.toSeconds(duration);
		        			
		        			if(seconds > 110) // fica uns 2 minutos com status de novo
		        			{
		        				todosPedidos.get(i).setHeader(UtilCoffe.PEDIDO_STATUS);
		        				todosPedidos.get(i).setStatus(UtilCoffe.PEDIDO_NORMAL);
								PainelPedido pp = (PainelPedido)pedidosCozinha.getComponent(i);
								pp.setPedido(todosPedidos.get(i));
								pp.refreshStatus();
		        			}        			
		        		}
		        		else if((todosPedidos.get(i).getStatus() == UtilCoffe.PEDIDO_REMOVER || todosPedidos.get(i).getStatus() == UtilCoffe.PEDIDO_DELETADO) 
		        				&& config.getModo() == UtilCoffe.SERVER)
		        		{
		        			long duration = System.currentTimeMillis() - todosPedidos.get(i).getUltimaEdicao().getTime();
		        			long seconds = TimeUnit.MILLISECONDS.toSeconds(duration);
		        			
		        			if(seconds > 10) // 10 segundos de cooldown para deletar
		        			{
		        				todosPedidos.get(i).setHeader(UtilCoffe.PEDIDO_DELETA);
		        				todosPedidos.get(i).setUltimaEdicao(new Date());
								atualizaPedido(todosPedidos.get(i));
		        			}
		        		}
		        		else if(config.getModo() == UtilCoffe.SERVER)
		        		{
		            		long duration = System.currentTimeMillis() - todosPedidos.get(i).getHora().getTime();
		            		long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);  
		            		
		            		if(minutes > config.getIntervaloPedido())	// deleta o pedido, nao importa o status
		            		{
		        				todosPedidos.get(i).setHeader(UtilCoffe.PEDIDO_DELETA);		        				
		        				todosPedidos.get(i).setUltimaEdicao(new Date());
								atualizaPedido(todosPedidos.get(i));
		            		}
		        		}
		        	}	
				}
			});
        }
    }
	
	class UpdateTask extends TimerTask 
	{
		public void run() 
		{
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					for(int i = 0; i < pedidosCozinha.getComponentCount(); i++)
					{
						PainelPedido pp = (PainelPedido)pedidosCozinha.getComponent(i);
						if(pp.isVisible())
							pp.atualizaTempo();
					}

					if(flag_refresh)
					{
						//new ScrollSetter(scroll.getVerticalScrollBar(), scroll.getVerticalScrollBar().getValue()).start(); 
						
						GridBagConstraints gbc = new GridBagConstraints();		
						gbc.insets = new Insets(5,5,5,5);
						gbc.anchor = GridBagConstraints.NORTH;
						gbc.fill = GridBagConstraints.HORIZONTAL;
						gbc.weightx = 1.0;
						gbc.gridx = 1;	// colunas
						int linha = 1;
						
						int blacklists = 0;
						int totalnew = 0;
						Collections.sort(todosPedidos, new CompararTempo());
						pedidosCozinha.removeAll();
						for(int x = 0; x < todosPedidos.size(); x++)
						{
							if(todosPedidos.get(x).getHeader() != UtilCoffe.PEDIDO_ADICIONA)
							{
								totalnew++;
								gbc.gridy = linha;
								pedidosCozinha.add(new PainelPedido(config, todosPedidos.get(x), new PedidoAlterado() {
									@Override
									public void pedidoAlterado(Pedido p) {
										if(config.getModo() == UtilCoffe.SERVER) {
											p.setUltimaEdicao(new Date());
											atualizaPedido(p);
										}
										else
										{
											((Client) modoPrograma).enviarObjeto(p);
										}
									}
									
								}), gbc);
								linha++;
								boolean flag_quit = false;
								if(blacklist.size() > 0)
								{
									for(int a = 0; a < todosProdutos.getCategorias().size(); a++)
									{
										if(blacklist.contains((Object) todosProdutos.getCategorias().get(a).getIdCategoria()))
										{
											for(int b = 0; b < todosProdutos.getCategorias().get(a).getProdutos().size(); b++)
											{
												if(todosProdutos.getCategorias().get(a).getProdutos().get(b).getIdUnico() == todosPedidos.get(x).getProduto().getIdUnico())
												{
													pedidosCozinha.getComponent((pedidosCozinha.getComponentCount()-1)).setVisible(false);
													flag_quit = true;
													blacklists++;
													break;
												}
											}
										}
										
										if(flag_quit)
											break;
									}
								}
							}
						}

						pedidosCozinha.revalidate();
						pedidosCozinha.repaint();
						
						if(flag_musica)
						{
							if(blacklists >= totalnew)
								flag_musica = false;
						}
						
						if(flag_musica && config.isSomCozinha() && isVisible())
						{
							try {
								Clip clip = AudioSystem.getClip();
								AudioInputStream ais = AudioSystem.getAudioInputStream(musica);
								clip.open(ais);
								clip.start();
							} catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
								e.printStackTrace();
							}
						}
						
						flag_refresh = false;
						flag_musica = false;
					}
				}
			});
		}
	}
		
	public synchronized void atualizaPedido(Pedido p)
	{		
		ProdutoVenda pNovo = UtilCoffe.cloneProdutoVenda(p.getProduto());
		
		if(p.getHeader() == UtilCoffe.PEDIDO_ADICIONA) // adiciona
		{
			flag_musica = true;
			if(config.getModo() == UtilCoffe.SERVER)
			{
				int idPedido = 0;
				try {
					SimpleDateFormat formataData = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss a");
					
					Query envia = new Query();
					envia.executaUpdate("INSERT INTO pedidos(local, status, observacao, atendido, data, produto, adicionais, quantidade)"
							+ " VALUES(" + p.getLocal() + ", "
							+ UtilCoffe.PEDIDO_NORMAL + ", '" 
							+ pNovo.getComentario() + "', '" 
							+ p.getAtendido() + "', '" 
							+ formataData.format(p.getHora()) + "', " 
							+ pNovo.getIdUnico() + ", '" 
							+ pNovo.getAllAdicionaisId() + "', " 
							+ pNovo.getQuantidade() + ");");
					
					envia.executaQuery("SELECT id FROM pedidos ORDER BY id DESC limit 0, 1");
					
					if(envia.next())
					{
						idPedido = envia.getInt("id");
					}
					
					envia.fechaConexao();
					
					if(idPedido > 0)
					{
			    		Pedido pedNovo = p;
			    		pedNovo.setProduto(pNovo);
			    		pedNovo.setIdUnico(idPedido);
						todosPedidos.add(pedNovo);
						System.out.println("Pedido recebido na cozinha. #" + pedNovo.getIdUnico());
						((Servidor) modoPrograma).enviaTodos(pedNovo);
					}
				} catch (ClassNotFoundException | SQLException e) {
					e.printStackTrace();
					new PainelErro(e);
				}
			}
			else
			{
	    		Pedido pedNovo = p;
	    		pedNovo.setProduto(pNovo);
				todosPedidos.add(pedNovo);
				System.out.println("Pedido recebido na cozinha.");	
			}
		}
		else if(p.getHeader() == UtilCoffe.PEDIDO_EDITADO)
		{
			for(int i = 0; i < todosPedidos.size(); i++)
			{
				if(todosPedidos.get(i).getIdUnico() == p.getIdUnico())
				{
					todosPedidos.get(i).getProduto().setQuantidade(pNovo.getQuantidade(), 0);
					flag_refresh = true;
					break;
				}
			}
		}
		else if(p.getHeader() == UtilCoffe.PEDIDO_DELETA) // deleta
		{
			for(int i = 0; i < todosPedidos.size(); i++)
			{
				if(todosPedidos.get(i).getIdUnico() == p.getIdUnico())
				{
					if(config.getModo() == UtilCoffe.SERVER)
					{
	        			try {
	        				Query deleta = new Query();
							deleta.executaUpdate("DELETE FROM pedidos WHERE `id` = "+ todosPedidos.get(i).getIdUnico() +";");
							deleta.fechaConexao();
						} catch (ClassNotFoundException | SQLException e) {
							e.printStackTrace();
						}	
					}
					
					todosPedidos.remove(i);
					
					if(i < pedidosCozinha.getComponentCount())
					{
						pedidosCozinha.remove(i);
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								pedidosCozinha.revalidate();
								pedidosCozinha.repaint();
							}
						});						
					}
					
					if(config.getModo() == UtilCoffe.SERVER)
						((Servidor) modoPrograma).enviaTodos(p);
					
					break;				
				}
			}	
		}
		else if(p.getHeader() == UtilCoffe.PEDIDO_STATUS) // edita status
		{
			if(p.getIdUnico() == 0)
			{
				List<Pedido> listaNormal = new ArrayList<Pedido>();
				List<Pedido> listaFazendo = new ArrayList<Pedido>();
				
				for(int i = 0; i < todosPedidos.size(); i++)
				{
					if(todosPedidos.get(i).getLocal() == p.getLocal())
					{
						if(todosPedidos.get(i).getProduto().compareTo(p.getProduto())) 
						{
							if(todosPedidos.get(i).getStatus() != UtilCoffe.PEDIDO_DELETADO && todosPedidos.get(i).getStatus() != UtilCoffe.PEDIDO_REMOVER)
							{
								if(todosPedidos.get(i).getStatus() == UtilCoffe.PEDIDO_FAZENDO)
									listaFazendo.add(todosPedidos.get(i));
								else
									listaNormal.add(todosPedidos.get(i));	
							}
						}
					}
				}
				
				int quantidade = p.getProduto().getQuantidade();
				if(quantidade < 0)
					quantidade = 1;
				
				for(int i = 0; i < listaNormal.size(); i++)
				{
					if(listaNormal.get(i).getProduto().getQuantidade() - quantidade > 0)
					{
						if(listaNormal.get(i).getHeader() == UtilCoffe.PEDIDO_ADICIONA || listaNormal.get(i).getHeader() == UtilCoffe.PEDIDO_EDITADO)
						{
							listaNormal.get(i).setHeader(UtilCoffe.PEDIDO_EDITADO);
							listaNormal.get(i).setStatus(UtilCoffe.PEDIDO_NOVO);
							listaNormal.get(i).getProduto().setQuantidade(quantidade, 2);
						}
						else
						{
							listaNormal.get(i).setHeader(UtilCoffe.PEDIDO_STATUS);
							listaNormal.get(i).setStatus(UtilCoffe.PEDIDO_EDITAR);
							listaNormal.get(i).getProduto().setQuantidade(quantidade, 2);					
						}

						atualizaPedido(listaNormal.get(i));
						quantidade = 0;
					}
					else
					{
						if(listaNormal.get(i).getHeader() == UtilCoffe.PEDIDO_ADICIONA || listaNormal.get(i).getHeader() == UtilCoffe.PEDIDO_EDITADO)
						{
							listaNormal.get(i).setHeader(UtilCoffe.PEDIDO_DELETA);
						}
						else
						{
							listaNormal.get(i).setHeader(UtilCoffe.PEDIDO_STATUS);
							listaNormal.get(i).setStatus(UtilCoffe.PEDIDO_DELETADO);				
						}

						atualizaPedido(listaNormal.get(i));
						quantidade -= listaNormal.get(i).getProduto().getQuantidade();
					}
					
					if(quantidade <= 0)
						break;
				}
				
				if(quantidade > 0)
				{
					for(int i = 0; i < listaFazendo.size(); i++)
					{
						if(listaFazendo.get(i).getProduto().getQuantidade() - quantidade > 0)
						{
							listaFazendo.get(i).setHeader(UtilCoffe.PEDIDO_STATUS);
							listaFazendo.get(i).setStatus(UtilCoffe.PEDIDO_EDITAR);
							atualizaPedido(listaFazendo.get(i));
							quantidade = 0;
						}
						else
						{
							listaFazendo.get(i).setHeader(UtilCoffe.PEDIDO_STATUS);
							listaFazendo.get(i).setStatus(UtilCoffe.PEDIDO_DELETADO);
							atualizaPedido(listaFazendo.get(i));
							quantidade -= listaFazendo.get(i).getProduto().getQuantidade();
						}
						
						if(quantidade <= 0)
							break;
					}			
				}
			}
			else
			{
				for(int i = 0; i < todosPedidos.size(); i++)
				{
					if(todosPedidos.get(i).getIdUnico() == p.getIdUnico())
					{						
						if(p.getStatus() == UtilCoffe.PEDIDO_EDITAR)
							todosPedidos.get(i).getProduto().setQuantidade(pNovo.getQuantidade(), 0);
						
						//if(p.getStatus() == UtilCoffe.PEDIDO_DELETADO || p.getStatus() == UtilCoffe.PEDIDO_EDITAR)
							//avisaPedido(p, p.getStatus());
						
						todosPedidos.get(i).setUltimaEdicao(new Date());
						todosPedidos.get(i).setHeader(p.getHeader());
						todosPedidos.get(i).setStatus(p.getStatus());
						
						final int idAgora = i;
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								if(idAgora < pedidosCozinha.getComponentCount())
								{
									PainelPedido pp = (PainelPedido)pedidosCozinha.getComponent(idAgora);
									pp.setPedido(todosPedidos.get(idAgora));
									pp.refreshStatus();
									pp.refreshQuantidade();	
								}	
							}
						});
						
						//flag_refresh = true;
						
						if(config.getModo() == UtilCoffe.SERVER)
						{
							((Servidor) modoPrograma).enviaTodos(p);
							
		        			try {
		        				Query atualiza = new Query();
		        				atualiza.executaUpdate("UPDATE pedidos SET `status` = " + todosPedidos.get(i).getStatus() 
		        						+ ", `quantidade` = " + todosPedidos.get(i).getProduto().getQuantidade() 
		        						+ " WHERE `id` = " + todosPedidos.get(i).getIdUnico() + ";");
		        				atualiza.fechaConexao();
							} catch (ClassNotFoundException | SQLException e) {
								e.printStackTrace();
							}
						}
						
						break;
					}
				}
			}
		}
	}
	
	public void avisaPedido(Pedido p, int tipo) 
	{
		if(isVisible())
		{
			NotificationManager.setLocation(2);
			String local = "";
			
			if(p.getLocal() == 0) {
				local = "Balcão";
			}
			else {
				local = config.getTipoNome() + " " + p.getLocal();
			}
			
			switch(tipo)
			{
				case UtilCoffe.PEDIDO_DELETADO:
				{
					NotificationManager.showNotification(this, "Pedido Deletado (" + local + " - " 
								+ p.getProduto().getNome() + ")", new ImageIcon(getClass().getClassLoader().getResource("imgs/notifications_cancel.png"))).setDisplayTime(5000);
					break;
				}
				case UtilCoffe.PEDIDO_EDITAR:
				{
					NotificationManager.showNotification(this, "Pedido Alterado (" + local + " - " 
							+ p.getProduto().getNome() + ")", new ImageIcon(getClass().getClassLoader().getResource("imgs/notifications_warning.png"))).setDisplayTime(5000);
					break;		
				}
			}
		}
	}
	
	/*class ScrollSetter extends Thread{
		JScrollBar scrollBar;
		int value;

		public ScrollSetter(JScrollBar scroll, int value){
			this.scrollBar = scroll;
			this.value = value;
		}

		public void run(){
			try{
				Thread.sleep(100);
				scrollBar.getModel().setValue(value);
			} catch (InterruptedException e){
				e.printStackTrace();
			}
		}
	}*/
}
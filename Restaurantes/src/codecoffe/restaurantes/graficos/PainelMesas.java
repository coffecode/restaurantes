package codecoffe.restaurantes.graficos;
import java.awt.*;

import javax.swing.*;
import javax.swing.border.EtchedBorder;

import codecoffe.restaurantes.eventos.AtualizarPainel;
import codecoffe.restaurantes.eventos.MesaAlterada;
import codecoffe.restaurantes.mysql.Query;
import codecoffe.restaurantes.primitivas.Pedido;
import codecoffe.restaurantes.primitivas.ProdutoVenda;
import codecoffe.restaurantes.primitivas.Venda;
import codecoffe.restaurantes.sockets.CacheMesaHeader;
import codecoffe.restaurantes.sockets.Servidor;
import codecoffe.restaurantes.utilitarios.Configuracao;
import codecoffe.restaurantes.utilitarios.Header;
import codecoffe.restaurantes.utilitarios.Usuario;
import codecoffe.restaurantes.utilitarios.UtilCoffe;

import com.alee.laf.button.WebButton;
import com.alee.laf.scroll.WebScrollPane;

import java.awt.event.*;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.List;

public class PainelMesas extends JPanel implements MesaAlterada
{
	private static final long serialVersionUID = 1L;
	private JPanel mesasPainel;
	private List<Venda> vendaMesas;
	private TheListener pegaMouse;
	private ImageIcon mesaOcupada, mesaNormal;
	private Configuracao config;
	private AtualizarPainel painelListener;
	private Object modoPrograma;
	
	public PainelMesas(Configuracao cfg, Object modo, List<Venda> mesas, AtualizarPainel listener)
	{
		config = cfg;
		modoPrograma = modo;
		vendaMesas = mesas;
		painelListener = listener;
		
		if(config.getTipoPrograma() == UtilCoffe.TIPO_MESA) {
			mesaOcupada = new ImageIcon(getClass().getClassLoader().getResource("imgs/mesa_ocupada_mini.png"));
			mesaNormal = new ImageIcon(getClass().getClassLoader().getResource("imgs/mesa_mini.png"));	
		}
		else {
			mesaOcupada = new ImageIcon(getClass().getClassLoader().getResource("imgs/comanda_ocupada_26.png"));
			mesaNormal = new ImageIcon(getClass().getClassLoader().getResource("imgs/comanda_26.png"));
		}
		
		pegaMouse = new TheListener();
		mesasPainel = new JPanel();
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), config.getTipoNome() + "s"));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		mesasPainel.setLayout(new GridBagLayout());
		
		WebScrollPane scroll = new WebScrollPane(mesasPainel, false);
		scroll.setPreferredSize(getSize());
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.getVerticalScrollBar().setUnitIncrement(20);
		add(scroll);
		
		atualizarTodasMesas();
	}
	
	public void atualizarTodasMesas()
	{
		mesasPainel.removeAll();
		GridBagConstraints gbc = new GridBagConstraints();
		int colunas = 0;
		int linhas  = 0;
		int quebra_linhas = 0;
		
		gbc.fill 			= GridBagConstraints.BOTH;
		gbc.weightx 		= 1.0;
		gbc.weighty 		= 1.0;
		gbc.insets = new Insets(7,7,7,7);  //top padding
		
		for(int i = 0; i < vendaMesas.size(); i++)
		{	
			BotaoMesa mesa = new BotaoMesa(config.getTipoNome() + " " + (i+1), vendaMesas.get(i), i);
			mesa.setFont(new Font("Verdana", Font.PLAIN, 12));
			mesa.setPreferredSize(new Dimension(100, 60));
			mesa.setHorizontalTextPosition(AbstractButton.CENTER);
			mesa.setVerticalTextPosition(AbstractButton.BOTTOM);
			mesa.setRolloverDecoratedOnly(true);
			mesa.addMouseListener(pegaMouse);
			
			if(vendaMesas.get(i).getQuantidadeProdutos() > 0)
			{
				vendaMesas.get(i).calculaTotal();
				mesa.setIcon(mesaOcupada);
				mesa.setForeground(new Color(191, 93, 12));			
			}
			else
			{
				mesa.setIcon(mesaNormal);
				mesa.setForeground(Color.BLACK);
			}
			
			gbc.gridx = colunas;
			gbc.gridy = linhas;			
			
			mesasPainel.add(mesa, gbc);
			
			quebra_linhas++;
			colunas++;
			
			if(quebra_linhas >= 7)
			{
				colunas = 0;
				linhas++;
				quebra_linhas = 0;
			}
		}
		
		mesasPainel.revalidate();
		mesasPainel.repaint();		
	}
	
	private class TheListener implements MouseListener
	{
		@Override
		public void mouseClicked(MouseEvent e) {}

		@Override
		public void mousePressed(MouseEvent e) {
			BotaoMesa x = (BotaoMesa)e.getSource();
			painelListener.atualizarPainel(new Header(UtilCoffe.UPDATE_VENDA_MESA, new Integer(x.getId()), 
																			vendaMesas.get(x.getId())));
			painelListener.atualizarPainel(new Header(UtilCoffe.ABRIR_MENU, new String("Menu Venda Mesa")));
		}

		@Override
		public void mouseReleased(MouseEvent e) {}

		@Override
		public void mouseEntered(MouseEvent e) {
			BotaoMesa x = (BotaoMesa)e.getSource();
			x.getVenda().calculaTotal();
			painelListener.atualizarPainel(new Header(UtilCoffe.UPDATE_LEGENDA, "Total na conta: R$" 
							+ UtilCoffe.doubleToPreco(x.getVenda().getTotal())));
		}

		@Override
		public void mouseExited(MouseEvent e) {
			painelListener.atualizarPainel(new Header(UtilCoffe.UPDATE_LEGENDA, "Desenvolvido por CodeCoffe (C) - 2014"));
		}
	}
	
	private class BotaoMesa extends WebButton
	{
		private static final long serialVersionUID = 1L;
		private int id;
		private Venda venda;
		
	    public BotaoMesa(String txt, Venda v, int id) {
	        super(txt);
	        this.venda = v;
	        this.id = id;
	    }

		public Venda getVenda() {
			return this.venda;
		}

		public int getId() {
			return id;
		}
	}
	
	public synchronized void setMesa(int id, Venda v) {
		vendaMesas.set(id, v);
	}
	
	public Venda getMesa(int id) {
		return vendaMesas.get(id);
	}

	@Override
	public void atualizarMesa(int id) {
		if(vendaMesas.get(id).getQuantidadeProdutos() > 0)
		{
			((BotaoMesa) mesasPainel.getComponent(id)).setIcon(mesaOcupada);
			((BotaoMesa) mesasPainel.getComponent(id)).setForeground(new Color(191, 93, 12));
		}
		else
		{
			((BotaoMesa) mesasPainel.getComponent(id)).setIcon(mesaNormal);
			((BotaoMesa) mesasPainel.getComponent(id)).setForeground(Color.BLACK);
		}
	}

	@Override
	public synchronized void atualizarMesa(CacheMesaHeader m, ObjectOutputStream socket) 
	{
		switch(m.getHeader())
		{
			case UtilCoffe.MESA_ADICIONAR:
			{
				try {
					String formatacao;
					Query envia = new Query();
					formatacao = "INSERT INTO mesas(mesas_id, produto, quantidade, pago, adicionais, comentario, data) VALUES("
					+ m.getMesaId() + ", " + m.getProdutoMesa().getIdUnico() + ", " + m.getHeaderExtra() + ", 0, '" 
					+ m.getProdutoMesa().getAllAdicionaisId() + "', '" + m.getProdutoMesa().getComentario() + "', '"
					+ m.getMesaVenda().getDataString() + "');";
					envia.executaUpdate(formatacao);
					envia.fechaConexao();
					
					if(socket != null) {
						vendaMesas.set(m.getMesaId(), m.getMesaVenda());
						atualizarMesa(m.getMesaId());
					}
					
					((Servidor) modoPrograma).enviaTodos(m, socket);
					Pedido ped = new Pedido(m.getProdutoMesa(), m.getAtendente(), (m.getMesaId()+1));
					ped.getProduto().setQuantidade(m.getHeaderExtra(), 0);
					painelListener.atualizarPainel(ped);
					
					if(Usuario.INSTANCE.getOlhando() == m.getMesaId() && socket != null) {
						painelListener.atualizarPainel(new Header(UtilCoffe.UPDATE_VENDA_MESA, new Integer(m.getMesaId()), 
								vendaMesas.get(m.getMesaId())));
					}
				} catch (ClassNotFoundException | SQLException e) {
					e.printStackTrace();
					((Servidor) modoPrograma).enviaObjeto(new CacheMesaHeader(m.getMesaId(), 
									vendaMesas.get(m.getMesaId()), UtilCoffe.MESA_ERROR), socket);
					new PainelErro(e);
				}
				
				break;
			}
			case UtilCoffe.MESA_ATUALIZAR:
			{
				try {
					String formatacao;
					Query envia = new Query();
					formatacao = "UPDATE mesas SET `quantidade` = (`quantidade` + " + m.getHeaderExtra() + ") WHERE `mesas_id` = " + m.getMesaId()
					+ " AND `produto` = " + m.getProdutoMesa().getIdUnico() 
					+ " AND `adicionais` = '" + m.getProdutoMesa().getAllAdicionaisId() 
					+ "' AND `comentario` = '" + m.getProdutoMesa().getComentario() + "';";						
					envia.executaUpdate(formatacao);
					envia.fechaConexao();
					
					if(socket != null) {
						vendaMesas.set(m.getMesaId(), m.getMesaVenda());
						atualizarMesa(m.getMesaId());
					}
					
					((Servidor) modoPrograma).enviaTodos(m, socket);
					ProdutoVenda pNovo = UtilCoffe.cloneProdutoVenda(m.getProdutoMesa());
					
					/* adicionar pedido */
					if(m.getHeaderExtra() > 0) // se for menor que zero ele ta deletando um pedido..
					{
						Pedido ped = new Pedido(pNovo, m.getAtendente(), (m.getMesaId()+1));
						painelListener.atualizarPainel(ped);
					}
					else
					{
						Pedido ped = new Pedido(pNovo, m.getAtendente(), (m.getMesaId()+1));
						ped.setHeader(UtilCoffe.PEDIDO_STATUS);
						painelListener.atualizarPainel(ped);						
					}
					
					if(Usuario.INSTANCE.getOlhando() == m.getMesaId() && socket != null) {
						painelListener.atualizarPainel(new Header(UtilCoffe.UPDATE_VENDA_MESA, new Integer(m.getMesaId()), 
								vendaMesas.get(m.getMesaId())));
					}
				} catch (ClassNotFoundException | SQLException e) {
					e.printStackTrace();
					((Servidor) modoPrograma).enviaObjeto(new CacheMesaHeader(m.getMesaId(), 
									vendaMesas.get(m.getMesaId()), UtilCoffe.MESA_ERROR), socket);
					new PainelErro(e);
				}
				
				break;
			}
			case UtilCoffe.MESA_ATUALIZAR2:
			{
				if(socket != null) {
					vendaMesas.set(m.getMesaId(), m.getMesaVenda());
					atualizarMesa(m.getMesaId());
				}
				
				if(Usuario.INSTANCE.getOlhando() == m.getMesaId() && socket != null) {
					painelListener.atualizarPainel(new Header(UtilCoffe.UPDATE_VENDA_MESA, new Integer(m.getMesaId()), 
							vendaMesas.get(m.getMesaId())));
				}
				
				((Servidor) modoPrograma).enviaTodos(m, socket);
				break;
			}
			default:	// delete ou limpar
			{
				boolean termina = false;
				try {
					Query pega = new Query();
					if(m.getHeader() == UtilCoffe.MESA_DELETAR)
					      pega.executaUpdate("DELETE FROM mesas WHERE `produto` = " + m.getProdutoMesa().getIdUnico() 
					    		  + " AND `adicionais` = '" + m.getProdutoMesa().getAllAdicionaisId()
					    		  + "' AND `comentario` = '" + m.getProdutoMesa().getComentario()
					    		  + "' AND `mesas_id` = " + m.getMesaId() + ";");
					
					pega.executaQuery("SELECT * FROM mesas WHERE `quantidade` != `pago` AND `mesas_id` = "+ m.getMesaId() +";");
					if(!pega.next())
					{
						pega.executaUpdate("DELETE FROM mesas WHERE `mesas_id` = "+ m.getMesaId() +";");
						m.getMesaVenda().clear();
						termina = true;
					}
					
					pega.fechaConexao();
					
					if(m.getHeader() == UtilCoffe.MESA_DELETAR)
					{
						m.getProdutoMesa().setQuantidade(m.getHeaderExtra(), 0);
						Pedido ped = new Pedido(m.getProdutoMesa(), m.getAtendente(), (m.getMesaId()+1));
						ped.setHeader(UtilCoffe.PEDIDO_STATUS);
						painelListener.atualizarPainel(ped);
					}
					
				} catch (ClassNotFoundException | SQLException e) {
					e.printStackTrace();
					((Servidor) modoPrograma).enviaObjeto(new CacheMesaHeader(m.getMesaId(), 
									vendaMesas.get(m.getMesaId()), UtilCoffe.MESA_ERROR), socket);
					new PainelErro(e);
				} finally {
					if(m.getHeader() == UtilCoffe.MESA_LIMPAR)
					{
						if(termina)
						{
							vendaMesas.set(m.getMesaId(), m.getMesaVenda());
							atualizarMesa(m.getMesaId());
							((Servidor) modoPrograma).enviaTodos(m);
							
							if(Usuario.INSTANCE.getOlhando() == m.getMesaId() && socket != null) {
								painelListener.atualizarPainel(new Header(UtilCoffe.UPDATE_VENDA_MESA, new Integer(m.getMesaId()), 
										vendaMesas.get(m.getMesaId())));
							}
						}					
					}
					else if(m.getHeader() == UtilCoffe.MESA_DELETAR)
					{
						if(!termina)
						{
							if(socket != null) {
								vendaMesas.set(m.getMesaId(), m.getMesaVenda());
								atualizarMesa(m.getMesaId());
							}
							
							if(Usuario.INSTANCE.getOlhando() == m.getMesaId() && socket != null) {
								painelListener.atualizarPainel(new Header(UtilCoffe.UPDATE_VENDA_MESA, new Integer(m.getMesaId()), 
										vendaMesas.get(m.getMesaId())));
							}
							
							((Servidor) modoPrograma).enviaTodos(m, socket);
						}
						else
						{
							vendaMesas.set(m.getMesaId(), m.getMesaVenda());
							atualizarMesa(m.getMesaId());
							((Servidor) modoPrograma).enviaTodos(m);
							
							if(Usuario.INSTANCE.getOlhando() == m.getMesaId() && socket != null) {
								painelListener.atualizarPainel(new Header(UtilCoffe.UPDATE_VENDA_MESA, new Integer(m.getMesaId()), 
										vendaMesas.get(m.getMesaId())));
							}
						}	
					}
				}
			}
		}
	}
}
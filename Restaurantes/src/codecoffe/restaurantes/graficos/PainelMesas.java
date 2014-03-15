package codecoffe.restaurantes.graficos;
import java.awt.*;

import javax.swing.*;
import javax.swing.border.EtchedBorder;

import net.miginfocom.swing.MigLayout;
import codecoffe.restaurantes.eventos.AtualizarPainel;
import codecoffe.restaurantes.primitivas.Venda;
import codecoffe.restaurantes.sockets.CacheMesaHeader;
import codecoffe.restaurantes.sockets.Client;
import codecoffe.restaurantes.utilitarios.Configuracao;
import codecoffe.restaurantes.utilitarios.Header;
import codecoffe.restaurantes.utilitarios.UtilCoffe;

import com.alee.extended.window.WebPopOver;
import com.alee.laf.button.WebButton;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.text.WebTextField;

import java.awt.event.*;
import java.util.List;

public class PainelMesas extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;
	private JPanel mesasPainel;
	private List<Venda> vendaMesas;
	private TheListener pegaMouse;
	private ImageIcon mesaOcupada, mesaNormal;
	private Configuracao config;
	private AtualizarPainel painelListener;
	private Object modoPrograma;
	private JPopupMenu popup;
	private PainelVendaMesa painelVendaMesa;
	private int mesaTransferir;
	
	public PainelMesas(Configuracao cfg, Object modo, List<Venda> mesas, AtualizarPainel listener)
	{
		config = cfg;
		modoPrograma = modo;
		vendaMesas = mesas;
		painelListener = listener;
		mesaTransferir = -1;
		
        popup = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("Transferir " + config.getTipoNome());
        //menuItem.setHorizontalTextPosition(JMenuItem.RIGHT);
        menuItem.addActionListener(this);
        popup.add(menuItem);
		
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
            if(!SwingUtilities.isRightMouseButton(e)) {
    			BotaoMesa x = (BotaoMesa)e.getSource();
    			painelListener.atualizarPainel(new Header(UtilCoffe.UPDATE_VENDA_MESA, new Integer(x.getId()), 
    																			vendaMesas.get(x.getId())));
    			painelListener.atualizarPainel(new Header(UtilCoffe.ABRIR_MENU, new String("Menu Venda Mesa")));
            }
		}

		@Override
		public void mouseReleased(MouseEvent e) {
            if(SwingUtilities.isRightMouseButton(e)) {
            	BotaoMesa x = (BotaoMesa)e.getSource();
            	mesaTransferir = x.getId();
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
		}

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
	
	/*public synchronized void setMesa(int id, Venda v) {
		vendaMesas.set(id, v);
	}*/
	
	public Venda getMesa(int id) {
		return vendaMesas.get(id);
	}

	public List<Venda> getVendaMesas() {
		return vendaMesas;
	}

	public void setVendaMesas(List<Venda> vendaMesas) {
		this.vendaMesas = vendaMesas;
	}

	public PainelVendaMesa getPainelVendaMesa() {
		return painelVendaMesa;
	}

	public void setPainelVendaMesa(PainelVendaMesa painelVendaMesa) {
		this.painelVendaMesa = painelVendaMesa;
	}

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
	public void actionPerformed(ActionEvent e) {
		if(mesaTransferir != -1)
		{	
			if(vendaMesas.get(mesaTransferir).getQuantidadeProdutos() <= 0) {
				JOptionPane.showMessageDialog(null, "Essa mesa está vazia!", "Erro", JOptionPane.ERROR_MESSAGE);
			}
			else
			{
				JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
				final WebPopOver popOver = new WebPopOver(topFrame);
	            popOver.setCloseOnFocusLoss(false);
	            popOver.setModal(true);
	            popOver.setMargin(10);
	            popOver.setLayout(new MigLayout());
	            
	            popOver.add(new JLabel("<html><b>Transferência da " + config.getTipoNome() + " " + (mesaTransferir+1) +"</b></html>"), "span, wrap");
	            
	            popOver.add(new JLabel("Digite para qual " + config.getTipoNome() + " você deseja transferir:"), "gaptop 15px, span, wrap");
	            
	            final WebTextField mesaCampo = new WebTextField();
	            mesaCampo.setMargin(5, 5, 5, 5);
	            mesaCampo.setHorizontalAlignment(SwingConstants.CENTER);
	            mesaCampo.setPreferredSize(new Dimension(60, 35));
	            popOver.add(mesaCampo, "gaptop 10px, align center, wrap");
	            
	            final WebButton botao = new WebButton("Transferir");
	            botao.setRolloverShine(true);
	            botao.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/notifications_ok.png")));
	            botao.setPreferredSize(new Dimension(100, 35));
	            botao.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						try {
							mesaCampo.setText(UtilCoffe.limpaNumero(mesaCampo.getText()));	
							if(UtilCoffe.vaziu(mesaCampo.getText()))
								mesaCampo.setText("0");
							
							int novaMesa = Integer.parseInt(mesaCampo.getText());
							
							if(novaMesa > 0 && novaMesa <= config.getMesas() && novaMesa != (mesaTransferir+1))
							{
								int confirmacao = JOptionPane.showConfirmDialog(null, "Transferir todos os produtos da " 
										+ config.getTipoNome() + " " + (mesaTransferir+1) + " para a " + config.getTipoNome() + " " + novaMesa + "."
										+ "\n\nVocê tem certeza que quer transferir?", "Confirmar Transferência", JOptionPane.YES_NO_OPTION);
								
								if(confirmacao == JOptionPane.YES_OPTION)
								{
									CacheMesaHeader mesaCache = new CacheMesaHeader(mesaTransferir, (novaMesa-1), UtilCoffe.MESA_TRANSFERIR);
									
									if(config.getModo() == UtilCoffe.SERVER) {
										painelVendaMesa.atualizaMesa(mesaCache, null, (short)0);
									}
									else {
										((Client) modoPrograma).enviarObjeto(mesaCache);
									}
							
									popOver.dispose();
									mesaTransferir = -1;		
								}
							}
							else
								JOptionPane.showMessageDialog(null, config.getTipoNome() + " Inválida", "Erro", JOptionPane.ERROR_MESSAGE);
						} catch (NumberFormatException | HeadlessException e1) {
							e1.printStackTrace();
							new PainelErro(e1);
						}
					}
	            });
	            
	    		mesaCampo.addKeyListener(new KeyAdapter() {
	            	public void keyPressed(KeyEvent e) {
	            		if(e.getKeyCode() == KeyEvent.VK_ENTER) {
	            			botao.doClick();
	            		}
	                }
	            });
	            
	            popOver.add(botao, "gaptop 15px, split 2, span, align center");
	            
	            final WebButton fechar = new WebButton("Cancelar");
	            fechar.setRolloverShine(true);
	            fechar.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/notifications_cancel.png")));
	            fechar.setPreferredSize(new Dimension(100, 35));
	            fechar.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						mesaTransferir = -1;
						popOver.dispose();
					}
	            });
	            
	            popOver.add(fechar, "gapleft 15px");
	            popOver.show(topFrame);
			}
		}
	}
}
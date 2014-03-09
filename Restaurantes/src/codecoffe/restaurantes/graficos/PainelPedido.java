package codecoffe.restaurantes.graficos;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.BevelBorder;

import codecoffe.restaurantes.eventos.PedidoAlterado;
import codecoffe.restaurantes.primitivas.Pedido;
import codecoffe.restaurantes.utilitarios.Configuracao;
import codecoffe.restaurantes.utilitarios.UtilCoffe;

import com.alee.extended.label.HotkeyPainter;
import com.alee.laf.StyleConstants;
import com.alee.laf.panel.WebPanel;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;
import com.alee.utils.ThreadUtils;

public class PainelPedido extends WebPanel
{
	private static final long serialVersionUID = 1L;
	private JLabel labelLocal, labelQuantidade, labelProduto, labelTempo;
	private JPopupMenu popup;
	private JStatusBar statusBar;
	private Pedido pedidoAtt;
	private PedidoAlterado cozinhaListener;
	
	public PainelPedido(Configuracao cfg, Pedido p, PedidoAlterado listener)
	{
		pedidoAtt = p;
		cozinhaListener = listener;
		
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(120, 45));
		setUndecorated(false);
		setMargin(4);
		setRound(StyleConstants.largeRound);
		HotkeyPainter pintura = new HotkeyPainter();
		setPainter(pintura);
		TooltipManager.addTooltip(this, "Feito por: " + pedidoAtt.getAtendido(), TooltipWay.up, 200);
		statusBar = new JStatusBar();
		refreshStatus();
		
		if(p.getLocal() > 0)
		{
			labelLocal = new JLabel(" " + cfg.getTipoNome() + " " + p.getLocal());
			if(cfg.getTipoPrograma() == UtilCoffe.TIPO_MESA) {
				labelLocal.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/mesa_mini_mini.png")));
				labelLocal.setPreferredSize(new Dimension(95, 30));
			}
			else {
				labelLocal.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/comanda_16.png")));
				labelLocal.setPreferredSize(new Dimension(95, 30));
			}
		}
		else
		{
			labelLocal = new JLabel(" Balcão");
			labelLocal.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/balcao_mini.png")));
			labelLocal.setPreferredSize(new Dimension(95, 30));			
		}
		
		labelQuantidade = new JLabel(" " + p.getProduto().getQuantidade());
		labelQuantidade.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/icon_food.png")));
		labelQuantidade.setPreferredSize(new Dimension(45, 30));
		
		String formataNome = " ";
		formataNome += p.getProduto().getNome();
		
		if(p.getProduto().getTotalAdicionais() > 0)
			formataNome += " com " + p.getProduto().getAllAdicionais();
		
		if(!UtilCoffe.vaziu(p.getProduto().getComentario()))
			formataNome += " (" + p.getProduto().getComentario() + ")";
		
		labelProduto = new JLabel("<html><b>" + formataNome + ".</b></html>");
		labelProduto.setPreferredSize(new Dimension(400, 30));
		
		labelTempo = new JLabel("0 segundos atrás.");
		labelTempo.setPreferredSize(new Dimension(105, 30));
		
		statusBar.addLeftComponent(labelLocal);
		statusBar.addLeftComponent(labelQuantidade);
		statusBar.addLeftComponent(labelProduto);
		statusBar.addRightComponent(labelTempo);
		add(statusBar, BorderLayout.CENTER);
		
		popup = new JPopupMenu();
		ActionListener menuListener = new ActionListener() 
		{
			public void actionPerformed(ActionEvent event) {
				
				if(pedidoAtt.getStatus() != UtilCoffe.PEDIDO_DELETADO) 
				{
					if(event.getActionCommand().equals("Status: Normal"))
					{
						pedidoAtt.setStatus(UtilCoffe.PEDIDO_NORMAL);
						pedidoAtt.setHeader(UtilCoffe.PEDIDO_STATUS);
						cozinhaListener.pedidoAlterado(pedidoAtt);
					}
					else if(event.getActionCommand().equals("Status: Fazendo"))
					{
						pedidoAtt.setStatus(UtilCoffe.PEDIDO_FAZENDO);
						pedidoAtt.setHeader(UtilCoffe.PEDIDO_STATUS);
						cozinhaListener.pedidoAlterado(pedidoAtt);
						
						new Thread(new Runnable() {
							public void run()
							{
								while(true)
								{
									ThreadUtils.sleepSafely(2000);
									pedidoAtt.setStatus(UtilCoffe.PEDIDO_FAZENDO);
									pedidoAtt.setHeader(UtilCoffe.PEDIDO_STATUS);
									cozinhaListener.pedidoAlterado(pedidoAtt);
								}
							}
						}).start();
					}
					else
					{
						pedidoAtt.setStatus(UtilCoffe.PEDIDO_REMOVER);
						pedidoAtt.setHeader(UtilCoffe.PEDIDO_STATUS);
						cozinhaListener.pedidoAlterado(pedidoAtt);				
					}
				}
				else {
					JOptionPane.showMessageDialog(null, "Esse pedido foi deletado, não é mais possível alterá-lo!");
				}
			}
		 };
		 
		 JMenuItem item;
		 popup.add(item = new JMenuItem("Status: Normal", new ImageIcon(getClass().getClassLoader().getResource("imgs/pedido_normal.png"))));
		 item.setHorizontalTextPosition(JMenuItem.RIGHT);
		 item.addActionListener(menuListener);
		 popup.addSeparator();
		 
		 popup.add(item = new JMenuItem("Status: Fazendo", new ImageIcon(getClass().getClassLoader().getResource("imgs/pedido_fazendo.png"))));
		 item.setHorizontalTextPosition(JMenuItem.RIGHT);
		 item.addActionListener(menuListener);
		 popup.addSeparator();		 
		 
		 popup.add(item = new JMenuItem("Status: Remover", new ImageIcon(getClass().getClassLoader().getResource("imgs/pedido_remover.png"))));
		 item.setHorizontalTextPosition(JMenuItem.RIGHT);
		 item.addActionListener(menuListener);

		 popup.setLabel("Menu Pedido");
		 popup.setBorder(new BevelBorder(BevelBorder.RAISED));
		 addMouseListener(new MousePopupListener());
		 
		 atualizaTempo();
	}
	
	class MousePopupListener extends MouseAdapter {
	    public void mousePressed(MouseEvent e) {
	      checkPopup(e);
	    }

	    public void mouseClicked(MouseEvent e) {
	      checkPopup(e);
	    }

	    public void mouseReleased(MouseEvent e) {
	      checkPopup(e);
	    }

	    private void checkPopup(MouseEvent e) {
	      if (!e.isPopupTrigger()) {
	        popup.show(PainelPedido.this, e.getX(), e.getY());
	      }
	    }
	}
	
	public void atualizaTempo()
	{
	    long duration = System.currentTimeMillis() - pedidoAtt.getHora().getTime();
	    long seconds = TimeUnit.MILLISECONDS.toSeconds(duration);
	    long hours = TimeUnit.MILLISECONDS.toHours(duration);
	    long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
	    
	    if (hours > 0) {
	    	labelTempo.setText(hours + " horas atrás.");
	    }
	    else if (minutes > 0) {
	    	labelTempo.setText(minutes + " minutos atrás.");
	    }
	    else
	    {
	    	labelTempo.setText(seconds + " segundos atrás.");
	    }		
	}
	
	public void setPedido(Pedido ped) {
		pedidoAtt = ped;
	}
	
	public Pedido getPedido() {
		return pedidoAtt;
	}
	
	public void refreshStatus()
	{
		switch(pedidoAtt.getStatus())
		{
			case UtilCoffe.PEDIDO_FAZENDO:
			{
				statusBar.setBackground(new Color(251, 242, 197));
				break;
			}
			case UtilCoffe.PEDIDO_REMOVER:
			{
				statusBar.setBackground(new Color(251, 197, 197));
				break;
			}
			case UtilCoffe.PEDIDO_NOVO:
			{
				statusBar.setBackground(new Color(218, 238, 220));
				break;
			}
			case UtilCoffe.PEDIDO_DELETADO:
			{
				statusBar.setBackground(new Color(227, 216, 231));
				break;
			}
			case UtilCoffe.PEDIDO_EDITAR:
			{
				statusBar.setBackground(new Color(227, 239, 250));
				break;
			}
			default:
			{
				statusBar.setBackground(new Color(237, 237, 237));	
			}
		}
	}
	
	public void refreshQuantidade() {
		labelQuantidade.setText(" " + pedidoAtt.getProduto().getQuantidade());
	}
	
	public class JStatusBar extends JPanel 
	{
	    private static final long serialVersionUID = 1L;
	    protected JPanel leftPanel;
	    protected JPanel rightPanel;
	    private boolean firstTime;

	    public JStatusBar() {
	        createPartControl();
	    }

	    protected void createPartControl() {    
	        setLayout(new BorderLayout());
	        setPreferredSize(new Dimension(getWidth(), 40));
	        firstTime = true;

	        leftPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 2));
	        leftPanel.setOpaque(false);
	        add(leftPanel, BorderLayout.WEST);

	        rightPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING, 5, 2));
	        rightPanel.setOpaque(false);
	        add(rightPanel, BorderLayout.EAST);
	    }

	    public void addLeftComponent(JComponent component) {
	        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
	        panel.setOpaque(false);
	        
	        if(!firstTime)
	        	panel.add(new SeparatorPanel(Color.GRAY, Color.WHITE));
	        
	        firstTime = false;        
	        
	        panel.add(component);
	        leftPanel.add(panel);
	    }

	    public void addRightComponent(JComponent component) {
	        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
	        panel.setOpaque(false);
	        panel.add(new SeparatorPanel(Color.GRAY, Color.WHITE));
	        panel.add(component);
	        rightPanel.add(panel);
	    }
	}
	
	public class SeparatorPanel extends JPanel 
	{
	    private static final long serialVersionUID = 1L;
	    protected Color leftColor;
	    protected Color rightColor;

	    public SeparatorPanel(Color leftColor, Color rightColor) {
	        this.leftColor = leftColor;
	        this.rightColor = rightColor;
	        setOpaque(false);
	    }

	    @Override
	    protected void paintComponent(Graphics g) {
	        g.setColor(leftColor);
	        g.drawLine(0, 0, 0, getHeight());
	        g.setColor(rightColor);
	        g.drawLine(1, 0, 1, getHeight());
	    }
	}
}
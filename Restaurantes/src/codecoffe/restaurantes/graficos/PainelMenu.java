package codecoffe.restaurantes.graficos;
import java.awt.*;

import javax.swing.*;

import codecoffe.restaurantes.eventos.LegendaAlterada;
import codecoffe.restaurantes.eventos.MenuSelecionado;
import codecoffe.restaurantes.utilitarios.Configuracao;
import codecoffe.restaurantes.utilitarios.Usuario;
import codecoffe.restaurantes.utilitarios.UtilCoffe;

import com.alee.laf.button.WebButton;

import java.awt.event.*;

public class PainelMenu extends JPanel implements MouseListener
{
	private static final long serialVersionUID = 1L;
	private MenuSelecionado menuListener;
	private LegendaAlterada legendaListener;
	private WebButton vendaRapida, consulta, inicio, produtos, cozinha, clientes;
	
	public PainelMenu(Configuracao config, MenuSelecionado listener, LegendaAlterada listenerLegenda)
	{	
		menuListener = listener;
		legendaListener = listenerLegenda;
		
		setLayout(new FlowLayout(FlowLayout.CENTER, 6, 20));
		setMaximumSize(new Dimension(1920, 80));
		setMinimumSize(new Dimension(980, 80));
		
		inicio = new WebButton("Início");
		inicio.setRolloverShine(true);
		inicio.setPreferredSize(new Dimension(130, 60));
		ImageIcon iconeInicio = new ImageIcon(getClass().getClassLoader().getResource("imgs/inicio.png"));
		inicio.setIcon(iconeInicio);
		inicio.addMouseListener(this);
		add(inicio);		
		
		vendaRapida = new WebButton("Venda Rápida");
		vendaRapida.setRolloverShine(true);
		ImageIcon iconeRapida = new ImageIcon(getClass().getClassLoader().getResource("imgs/vrapida.png"));
		vendaRapida.setIcon(iconeRapida);
		vendaRapida.setPreferredSize(new Dimension(160, 60));
		vendaRapida.addMouseListener(this);
		add(vendaRapida);
		
		if(config.getModo() == UtilCoffe.SERVER)
		{
			produtos = new WebButton("Produtos");
			produtos.setRolloverShine(true);
			produtos.setPreferredSize(new Dimension(140, 60));
			ImageIcon iconeProdutos = new ImageIcon(getClass().getClassLoader().getResource("imgs/produtos.png"));
			produtos.setIcon(iconeProdutos);
			produtos.addMouseListener(this);
			add(produtos);			
		}
		
		clientes = new WebButton("Clientes");
		clientes.setRolloverShine(true);
		clientes.setPreferredSize(new Dimension(140, 60));
		clientes.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/report_user.png")));
		clientes.addMouseListener(this);
		add(clientes);
		
		cozinha = new WebButton("Cozinha");
		cozinha.setRolloverShine(true);
		cozinha.setPreferredSize(new Dimension(140, 60));
		ImageIcon iconeFuncionarios = new ImageIcon(getClass().getClassLoader().getResource("imgs/chef.png"));
		cozinha.setIcon(iconeFuncionarios);
		cozinha.addMouseListener(this);
		add(cozinha);			
		
		if(config.getModo() == UtilCoffe.SERVER)
		{			
			consulta = new WebButton("Vendas");
			consulta.setRolloverShine(true);
			consulta.setPreferredSize(new Dimension(130, 60));
			ImageIcon iconeVendas = new ImageIcon(getClass().getClassLoader().getResource("imgs/consultar.png"));
			consulta.setIcon(iconeVendas);
			consulta.addMouseListener(this);
			add(consulta);			
		}
		
		if(config.getModo() == UtilCoffe.SERVER)
		{
			ActionMap actionMap = getActionMap();
			actionMap.put("botao1", new SpaceAction(0));
			actionMap.put("botao2", new SpaceAction(1));
			actionMap.put("botao3", new SpaceAction(2));
			actionMap.put("botao4", new SpaceAction(3));
			actionMap.put("botao5", new SpaceAction(4));
			actionMap.put("botao6", new SpaceAction(5));
			actionMap.put("botao7", new SpaceAction(6));
			setActionMap(actionMap);
			
			InputMap imap = getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
			imap.put(KeyStroke.getKeyStroke("F1"), "botao1");
			imap.put(KeyStroke.getKeyStroke("F2"), "botao2");
			imap.put(KeyStroke.getKeyStroke("F3"), "botao3"); 
			imap.put(KeyStroke.getKeyStroke("F4"), "botao4"); 
			imap.put(KeyStroke.getKeyStroke("F5"), "botao5");
			imap.put(KeyStroke.getKeyStroke("F6"), "botao6");
			imap.put(KeyStroke.getKeyStroke("ESCAPE"), "botao7");	
		}
		else
		{
			ActionMap actionMap = getActionMap();
			actionMap.put("botao1", new SpaceAction(0));
			actionMap.put("botao2", new SpaceAction(1));
			actionMap.put("botao4", new SpaceAction(3));
			actionMap.put("botao5", new SpaceAction(4));
			actionMap.put("botao7", new SpaceAction(6));
			setActionMap(actionMap);
			
			InputMap imap = getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
			imap.put(KeyStroke.getKeyStroke("F1"), "botao1");
			imap.put(KeyStroke.getKeyStroke("F2"), "botao2");
			imap.put(KeyStroke.getKeyStroke("F3"), "botao4"); 
			imap.put(KeyStroke.getKeyStroke("F4"), "botao5");
			imap.put(KeyStroke.getKeyStroke("ESCAPE"), "botao7");				
		}
	}
	
	private class SpaceAction extends AbstractAction 
	{
		private static final long serialVersionUID = 1L;
		private int tipo = 0;

		public SpaceAction(int tipo) {
			this.tipo = tipo;
		}		

		@Override
		public void actionPerformed(ActionEvent e) {
			
			switch(this.tipo)
			{
				case 0:
				{
					menuListener.abrirMenu("Menu Mesas");
					break;
				}
				case 1:
				{
					menuListener.abrirMenu("Menu Venda Rapida");
					break;
				}
				case 2:
				{
					if(Usuario.INSTANCE.getLevel() > 1)
						menuListener.abrirMenu("Menu Produtos");
					else
						JOptionPane.showMessageDialog(null, "Você não tem permissão para ver isso.");
					
					break;
				}
				case 3:
				{
					menuListener.abrirMenu("Menu Clientes");
					break;
				}
				case 4:
				{
					menuListener.abrirMenu("Menu Cozinha");
					break;
				}
				case 5:
				{
					if(Usuario.INSTANCE.getLevel() > 1)
						menuListener.abrirMenu("Menu Vendas");
					else
						JOptionPane.showMessageDialog(null, "Você não tem permissão para ver isso.");
					
					break;
				}
				default:
				{
					//int opcao = JOptionPane.showConfirmDialog(null, "Você tem certeza que deseja sair?", "Logout", JOptionPane.YES_NO_OPTION);

					//if(opcao == JOptionPane.YES_OPTION)
						//PainelPrincipal.getInstance().logout();
				}
			}
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {
		if(e.getSource() == vendaRapida)
		{
			menuListener.abrirMenu("Menu Venda Rapida");
		}
		else if(e.getSource() == produtos)
		{
			if(Usuario.INSTANCE.getLevel() > 1)
				menuListener.abrirMenu("Menu Produtos");
			else
				JOptionPane.showMessageDialog(null, "Você não tem permissão para ver isso.");
		}
		else if(e.getSource() == consulta)
		{
			if(Usuario.INSTANCE.getLevel() > 1)
				menuListener.abrirMenu("Menu Vendas");
			else
				JOptionPane.showMessageDialog(null, "Você não tem permissão para ver isso.");
		}
		else if(e.getSource() == inicio)
		{
			menuListener.abrirMenu("Menu Mesas");
		}
		else if(e.getSource() == clientes)
		{
			menuListener.abrirMenu("Menu Clientes");
			//PainelClientes.getInstance().setCallBack(0);
		}
		else if(e.getSource() == cozinha)
		{
			menuListener.abrirMenu("Menu Cozinha");
		}		
	}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseEntered(MouseEvent e)
	{
		if(e.getSource() == vendaRapida)
		{
			legendaListener.alterarLegenda("Venda Rápida, direto no balcão.");
		}
		else if(e.getSource() == produtos)
		{
			legendaListener.alterarLegenda("Gerenciamento de produtos/adicionais.");
		}
		else if(e.getSource() == cozinha)
		{
			legendaListener.alterarLegenda("Gerenciamento de pedidos.");
		}
		else if(e.getSource() == consulta)
		{
			legendaListener.alterarLegenda("Consulte as vendas de determinada data.");
		}
		else if(e.getSource() == inicio)
		{
			legendaListener.alterarLegenda("Início do programa (mesas).");
		}
	}

	@Override
	public void mouseExited(MouseEvent e) {
		legendaListener.alterarLegenda("Desenvolvido por CodeCoffe (C) - 2014");
	}
}
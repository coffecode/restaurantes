package codecoffe.restaurantes.graficos.produtos;

import java.awt.Color;
import java.awt.Component;
//import java.awt.Container;
//import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
//import javax.swing.JScrollBar;
//import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;

import com.alee.laf.panel.WebPanel;

import net.miginfocom.swing.MigLayout;
import codecoffe.restaurantes.primitivas.Categoria;
import codecoffe.restaurantes.primitivas.Produto;
import codecoffe.restaurantes.utilitarios.UtilCoffe;

public class ProdutosComboBox extends JComboBox<Object> implements KeyListener
{
	private static final long serialVersionUID = 1L;
	private ProdutosComboModel comboModelCompleto;
	private ProdutosComboEditor comboEditor;
	private boolean flag_aciona = true;
	private boolean comboAdicional = false;
	
	public ProdutosComboBox(List<Categoria> categorias, int m)
	{
		if(m != 1)
			comboAdicional = true;
		
		comboModelCompleto = new ProdutosComboModel(categorias, m);		
		makeComboBox();
	}
	
	public void refreshModel()
	{
		comboModelCompleto.refreshModel();
	}
	
	public void makeComboBox()
	{
		setModel(comboModelCompleto);
		setEditable(true);
		setUI(new BasicComboBoxUI());
		setBackground(new Color(237, 237, 237));
		
		for(int i = 0; i < getComponentCount(); i++) 
		{
		    if(getComponent(i) instanceof AbstractButton) {
		        ((AbstractButton) getComponent(i)).setVisible(false);
		        break;
		    }
		}
		
		comboEditor = new ProdutosComboEditor(comboAdicional);
		setEditor(comboEditor);
		
		setMaximumRowCount(6);
		setFocusTraversalKeysEnabled(true);
		
		/*Object popup = this.getUI().getAccessibleChild(this , 0);
		Component c = ((Container) popup).getComponent(0);
		if(c instanceof JScrollPane)
		{
			JScrollPane spane = (JScrollPane) c;
			JScrollBar scrollBar = spane .getVerticalScrollBar();
			Dimension scrollBarDim = new Dimension(25, scrollBar.getPreferredSize().height);
			scrollBar.setPreferredSize(scrollBarDim);
		}*/
		
		setRenderer(new ListCellRenderer<Object>() {
			private ProdutoCellPainel painelRender = new ProdutoCellPainel();
			
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				Produto produto = (Produto) value;
				
				painelRender.setLabelNome(produto.getNome());
				painelRender.setLabelCodigo("" + produto.getCodigo());
				painelRender.setLabelPreco(UtilCoffe.doubleToPreco(produto.getPreco()));
				
				if(isSelected) {
					painelRender.setUndecorated(false);
				}
				else
					painelRender.setUndecorated(true);
				
				return painelRender;
			}
		});
		
		addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					comboEditor.setProduto((Produto) e.getItem());
				}
			}
        });
		
		comboEditor.getTextField().addKeyListener(this);
		
		if(getModel().getSelectedItem() != null)
		{
			comboEditor.setProduto((Produto) getModel().getSelectedItem());
		}
	}
	
	@Override
    public void requestFocus() {
		comboEditor.getTextField().requestFocus();
    }
	
	public Component getEditorTextField()
	{
		return comboEditor.getTextField();
	}
	
	public Produto getProdutoSelecionado()
	{
		return (Produto) comboEditor.getItem();
	}
	
	public void setProdutoNull() 
	{
		comboEditor.setProdutoNull();
	}
	
	public ProdutosComboModel getModelPossivel(String texto)
	{
		String comparar = UtilCoffe.removeAcentos(texto.toLowerCase());
		ProdutosComboModel modelMutavel = new ProdutosComboModel();
		Categoria c = new Categoria();
		
		for(int i = 0; i < comboModelCompleto.getSize(); i++)
		{
			Produto p = (Produto) comboModelCompleto.getElementAt(i);
			if(p.contains(comparar)) c.addProduto(p);
		}
		
		modelMutavel.addElement(new Categoria());
		modelMutavel.addElement(c);
		
		return modelMutavel;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				if(flag_aciona)
				{
					String text = comboEditor.getTextField().getText();
	                if(text.length() == 0)
	                {
	                	setModel(comboModelCompleto);
	                	showPopup();
	                }
	                else
	                {
	                	ProdutosComboModel m = getModelPossivel(text);
	                	
	                	if(m.getSize() == 0)
	                	{
	                		hidePopup();
	                	}
	                	else
	                	{
	                		setModel(m);
	                		showPopup();
	                	}
	                }	
				}
				else
					flag_aciona = true;
           }
		});
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		if(code==KeyEvent.VK_ENTER)
		{
			if(getModel().getSize() == 1) {
				comboEditor.setProduto((Produto) getModel().getElementAt(0));
			}
			
			comboEditor.getTextField().setText("");
			setModel(comboModelCompleto);
			flag_aciona = false;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {}
	
	class ProdutoCellPainel extends WebPanel
	{
		private static final long serialVersionUID = 1L;
		private JLabel labelNome, labelCodigo, labelPreco;
		
		public ProdutoCellPainel()
		{
			setBackground(Color.WHITE);
			setLayout(new MigLayout());
			setBorder(new EmptyBorder(2, 2, 2, 2));
			labelNome = new JLabel();
			
			if(comboAdicional)
				labelNome.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/plus2.png")));
			else
				labelNome.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/icon_food.png")));
			
			labelNome.setFont(new Font("Verdana", Font.BOLD, 12));
			
			labelCodigo = new JLabel();
			labelCodigo.setFont(new Font("Verdana", Font.PLAIN, 10));
			labelCodigo.setForeground(Color.RED);
			
			labelPreco = new JLabel();
			labelPreco.setFont(new Font("Verdana", Font.PLAIN, 10));
			labelPreco.setForeground(Color.BLUE);
			
			add(labelNome, "span, wrap");
			add(labelCodigo);
			add(labelPreco, "gapleft 20px");
		}
		
		public void setLabelNome(String texto) {
			labelNome.setText(" " + texto);
		}
		
		public void setLabelPreco(String texto) {
			labelPreco.setText("Preço: R$" + texto);
		}
		
		public void setLabelCodigo(String texto) {
			labelCodigo.setText("Código: " + texto);
		}
	}
}
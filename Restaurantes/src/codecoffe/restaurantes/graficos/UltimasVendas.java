package codecoffe.restaurantes.graficos;
import java.awt.*;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;
import codecoffe.restaurantes.eventos.AtualizarPainel;
import codecoffe.restaurantes.mysql.Query;
import codecoffe.restaurantes.utilitarios.Configuracao;
import codecoffe.restaurantes.utilitarios.DiarioLog;
import codecoffe.restaurantes.utilitarios.Usuario;
import codecoffe.restaurantes.utilitarios.UtilCoffe;

import com.alee.laf.scroll.WebScrollPane;

import java.awt.Color;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.Vector;

public class UltimasVendas extends JPanel
{
	private static final long serialVersionUID = 1L;
	private JTable tabela;
	private DefaultTableModel tabelaModel;
	private JPopupMenu popup;
	private Configuracao config;
	private boolean configDelivery, configDez;
	private AtualizarPainel painelListener;

	public UltimasVendas(Configuracao cfg, AtualizarPainel listener)
	{
		config = cfg;
		painelListener = listener;
		configDez = config.getDezPorcento();
		if(config.getTaxaEntrega() > 0)	configDelivery = true;
		else
			configDelivery = false;

		setLayout(new MigLayout("fill"));

		tabela = new JTable();
		tabelaModel = new DefaultTableModel() {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int column) {
				if(column == (tabela.getColumnCount()-1)) return true;
				return false;
			}
		};

		ActionListener al = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(tabela.getSelectedRowCount() == 1)
				{
					new VisualizarVenda(Integer.parseInt(tabela.getValueAt(tabela.getSelectedRow(), 0).toString()), config, painelListener)
					.setLocationRelativeTo(SwingUtilities.getRoot(popup));
				}
			}
		};

		popup = new JPopupMenu();
		JMenuItem menuItem = new JMenuItem("Ver detalhes...");
		menuItem.addActionListener(al);
		popup.add(menuItem);

		tabela.addMouseListener(new MouseAdapter()
		{
			public void mouseReleased(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					JTable source = (JTable)e.getSource();
					int row = source.rowAtPoint( e.getPoint() );
					int column = source.columnAtPoint( e.getPoint() );

					if (!source.isRowSelected(row))
						source.changeSelection(row, column, false, false);

					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});

		tabelaModel.addColumn("ID");
		tabelaModel.addColumn("Data");
		tabelaModel.addColumn("Pagamento");
		tabelaModel.addColumn("Total");
		tabelaModel.addColumn("Status");
		tabelaModel.addColumn("Atendente");

		if(configDelivery)
			tabelaModel.addColumn("Delivery");

		if(configDez)
			tabelaModel.addColumn("10%");

		tabelaModel.addColumn("Deletar");

		tabela.setModel(tabelaModel);
		tabela.setRowHeight(30);
		tabela.getTableHeader().setReorderingAllowed(false);
		tabela.setFocusable(false);
		tabela.setDefaultRenderer(Object.class, new TabelaVendasRenderer());

		tabela.getColumn("ID").setPreferredWidth(80);
		tabela.getColumn("Status").setPreferredWidth(70);
		tabela.getColumn("Data").setPreferredWidth(150);
		tabela.getColumn("Pagamento").setPreferredWidth(150);
		tabela.getColumn("Total").setPreferredWidth(120);
		tabela.getColumn("Atendente").setPreferredWidth(200);

		if(configDelivery)
			tabela.getColumn("Delivery").setPreferredWidth(70);

		if(configDez)
			tabela.getColumn("10%").setPreferredWidth(60);

		tabela.getColumn("Deletar").setMinWidth(60);
		tabela.getColumn("Deletar").setMaxWidth(60);
		tabela.getColumn("Deletar").setCellEditor(new ButtonEditor(new JCheckBox()));
		
		try {
			Query pega = new Query();
			pega.executaQuery("SELECT * FROM vendas ORDER BY vendas_id DESC limit 0, 25");
			
			while(pega.next())
			{
				Vector<String> linha = new Vector<String>();
						
				linha.add("" + pega.getInt("vendas_id"));
				linha.add(pega.getString("horario"));
				linha.add(pega.getString("forma_pagamento"));
				linha.add(pega.getString("total"));
					
				if(UtilCoffe.precoToDouble(pega.getString("total")) > UtilCoffe.precoToDouble(pega.getString("valor_pago")))
				{
					if(pega.getString("forma_pagamento").equals("Fiado")) {
						linha.add("0");
					}
					else
						linha.add("1");
				}
				else
					linha.add("1");
					
				linha.add(pega.getString("atendente"));
				
				if(configDelivery)
				{
					if(UtilCoffe.precoToDouble(pega.getString("delivery")) > 0) {
						linha.add("1");
					}
					else
						linha.add("0");
				}
				
				if(configDez)
				{
					if(UtilCoffe.precoToDouble(pega.getString("dezporcento")) > 0) {
						linha.add("1");
					}
					else
						linha.add("0");
				}
				
				linha.add("");
				tabelaModel.addRow(linha);
			}
		} catch (NumberFormatException | ClassNotFoundException | SQLException e1) {
			e1.printStackTrace();
			new PainelErro(e1);
		}	
		
		WebScrollPane scrolltabela = new WebScrollPane(tabela, true);
		scrolltabela.getViewport().setBackground(new Color(237, 237, 237));
		scrolltabela.setFocusable(false);
		
		add(scrolltabela, "grow, pushy, span, wrap");
		ToolTipManager.sharedInstance().setInitialDelay(0);
	}

	public void refresh()
	{
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				tabelaModel.setNumRows(0);
				
				try {
					Query pega = new Query();
					pega.executaQuery("SELECT * FROM vendas ORDER BY vendas_id DESC limit 0, 25");
					
					while(pega.next())
					{
						Vector<String> linha = new Vector<String>();
								
						linha.add("" + pega.getInt("vendas_id"));
						linha.add(pega.getString("horario"));
						linha.add(pega.getString("forma_pagamento"));
						linha.add(pega.getString("total"));
							
						if(UtilCoffe.precoToDouble(pega.getString("total")) > UtilCoffe.precoToDouble(pega.getString("valor_pago")))
						{
							if(pega.getString("forma_pagamento").equals("Fiado")) {
								linha.add("0");
							}
							else
								linha.add("1");
						}
						else
							linha.add("1");
							
						linha.add(pega.getString("atendente"));
						
						if(configDelivery)
						{
							if(UtilCoffe.precoToDouble(pega.getString("delivery")) > 0) {
								linha.add("1");
							}
							else
								linha.add("0");
						}
						
						if(configDez)
						{
							if(UtilCoffe.precoToDouble(pega.getString("dezporcento")) > 0) {
								linha.add("1");
							}
							else
								linha.add("0");
						}
						
						linha.add("");
						tabelaModel.addRow(linha);
					}
				} catch (NumberFormatException | ClassNotFoundException | SQLException e1) {
					e1.printStackTrace();
					new PainelErro(e1);
				}
			}
		});
	}
	
	private class TabelaVendasRenderer extends DefaultTableCellRenderer
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Color alternate = new Color(206, 220, 249);
		private ImageIcon iconLabel = new ImageIcon(getClass().getClassLoader().getResource("imgs/documento.png"));
		private ImageIcon iconDelete = new ImageIcon(getClass().getClassLoader().getResource("imgs/delete.png"));
		private ImageIcon iconOK = new ImageIcon(getClass().getClassLoader().getResource("imgs/pago.png"));
		private ImageIcon iconNO = new ImageIcon(getClass().getClassLoader().getResource("imgs/npago.png"));
		private JButton bDeletar = new JButton(iconDelete);

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, 
				boolean isSelected, boolean hasFocus, int row, int column) {
			JLabel cellComponent = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			
			if((tabelaModel.getColumnCount() - 1) == column) {
				return bDeletar;
			}
			else if(column == 4)
			{
				if(Integer.parseInt(value.toString()) == 1) {
					setIcon(iconOK);
					setText("");
				}
				else {
					setIcon(iconNO);
					setText("");
				}
			}
			else if(column == 6 && configDelivery)
			{
				if(Integer.parseInt(value.toString()) == 1) {
					setIcon(iconOK);
					setText("");
				}
				else {
					setIcon(iconNO);
					setText("");
				}				
			}
			else if(tabelaModel.getColumnName(column).equals("10%") && configDez)
			{
				if(Integer.parseInt(value.toString()) == 1) {
					setIcon(iconOK);
					setText("");
				}
				else {
					setIcon(iconNO);
					setText("");
				}				
			}
			else if(column == 3) {
				setIcon(iconLabel);
				if(isSelected) {
					try {
						String formataTip = "<html>";
						formataTip += "<b>Venda #" + table.getValueAt(row,0) + "</b>  (<i>" + table.getValueAt(row,1) +")</i><br>";
						Query pega = new Query();
						pega.executaQuery("SELECT * FROM vendas_produtos WHERE `id_link` = " + table.getValueAt(row,0) + "");

						while(pega.next())
						{
							formataTip += pega.getInt("quantidade_produto") + "x .......... <b>" + pega.getString("nome_produto") + "</b>";
							if(!"".equals(pega.getString("adicionais_produto").trim()))
								formataTip += " com " + pega.getString("adicionais_produto");

							formataTip += " - R$" +  pega.getString("preco_produto") + "<br>";
						}

						pega.fechaConexao();
						formataTip += "</html>";
						setToolTipText(formataTip);
					} catch (ClassNotFoundException | SQLException e) {
						e.printStackTrace();
						setToolTipText("Erro ao receber banco de dados.");
					}
				}
			}
			else {
				setIcon(null);
				setToolTipText(null);
			}
			
			if(isSelected) {
				cellComponent.setBackground(tabela.getSelectionBackground());
			}
			else if(row % 2 == 0) {
				cellComponent.setBackground(alternate);
			}
			else {
				cellComponent.setBackground(Color.WHITE);
			}
			
			setHorizontalAlignment(JLabel.CENTER);
			return cellComponent;
		}
	}

	private class ButtonEditor extends DefaultCellEditor {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		protected JButton button;
		private boolean isPushed;

		public ButtonEditor(JCheckBox checkBox) {
			super(checkBox);
			button = new JButton(new ImageIcon(getClass().getClassLoader().getResource("imgs/delete.png")));
			button.setOpaque(true);	    
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					fireEditingStopped();
				}
			});
		}

		public Component getTableCellEditorComponent(JTable table, Object value,
				boolean isSelected, int row, int column) {
			if (isSelected) {
				button.setForeground(table.getSelectionForeground());
				button.setBackground(table.getSelectionBackground());
			} else {
				button.setForeground(table.getForeground());
				button.setBackground(table.getBackground());
			}
			isPushed = true;
			return button;
		}

		public Object getCellEditorValue() {
			if (isPushed) {
				if(tabela.getSelectedRowCount() == 1)
				{
					int opcao = JOptionPane.showConfirmDialog(null, "Essa opção irá deletar a venda.\n\n"
							+ "Você tem certeza?\n\n", "Deletar Venda", JOptionPane.YES_NO_OPTION);

					if(opcao == JOptionPane.YES_OPTION)
					{ 
						try {
							Query pega = new Query();
							pega.executaUpdate("DELETE FROM vendas WHERE `vendas_id` = " + tabela.getValueAt(tabela.getSelectedRow(), 0));
							pega.executaUpdate("DELETE FROM vendas_produtos WHERE `id_link` = " + tabela.getValueAt(tabela.getSelectedRow(), 0));
							DiarioLog.add(Usuario.INSTANCE.getNome(), "Deletou a venda #" + tabela.getValueAt(tabela.getSelectedRow(), 0) 
									+ " de valor R$" + tabela.getValueAt(tabela.getSelectedRow(), 3) + ".", 7);	
							pega.fechaConexao();

							SwingUtilities.invokeLater(new Runnable() {  
								public void run() {  
									tabelaModel.removeRow(tabela.getSelectedRow());
								}  
							});
						} catch (ClassNotFoundException | SQLException e) {
							e.printStackTrace();
							new PainelErro(e);
						}		    			
					}
				}
			}
			isPushed = false;
			return "";
		}

		public boolean stopCellEditing() {
			isPushed = false;
			return super.stopCellEditing();
		}

		protected void fireEditingStopped() {
			super.fireEditingStopped();
		}
	}
}
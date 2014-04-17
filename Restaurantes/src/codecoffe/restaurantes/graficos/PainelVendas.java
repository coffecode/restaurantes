package codecoffe.restaurantes.graficos;
import java.awt.*;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import codecoffe.restaurantes.eventos.AtualizarPainel;
import codecoffe.restaurantes.mysql.Query;
import codecoffe.restaurantes.utilitarios.Configuracao;
import codecoffe.restaurantes.utilitarios.DiarioLog;
import codecoffe.restaurantes.utilitarios.GraficoFiados;
import codecoffe.restaurantes.utilitarios.Usuario;
import codecoffe.restaurantes.utilitarios.UtilCoffe;

import com.alee.laf.scroll.WebScrollPane;

import java.awt.Color;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.Vector;

public class PainelVendas extends JPanel
{
	private static final long serialVersionUID = 1L;
	private JTable tabelaFiados;
	private DefaultTableModel tabela;
	private UltimasVendas painelUltimas;
	private GraficoFiados graFiados;
	private Configuracao config;
	private AtualizarPainel painelListener;

	public PainelVendas(Configuracao cfg, AtualizarPainel listener)
	{
		config = cfg;
		painelListener = listener;
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setFocusable(false);
		JPanel visualizarFiado = new JPanel();
		visualizarFiado.setLayout(new BoxLayout(visualizarFiado, BoxLayout.Y_AXIS));
		visualizarFiado.setPreferredSize(new Dimension(700, 200));

		JPanel painelTabela = new JPanel(new BorderLayout());
		painelTabela.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Dívidas em aberto"));
		painelTabela.setMinimumSize(new Dimension(975, 200));		// Horizontal , Vertical
		painelTabela.setMaximumSize(new Dimension(1920, 680));

		tabela = new DefaultTableModel() {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int column) {
				if(column == 0 || column == 5)
					return true;

				return false;
			}
		};

		tabela.addColumn("Dívida");
		tabela.addColumn("Nome");
		tabela.addColumn("Apelido");
		tabela.addColumn("Telefone");
		tabela.addColumn("CPF");
		tabela.addColumn("Deletar");
		tabela.addColumn("ID");

		tabelaFiados = new JTable() {
			private static final long serialVersionUID = 1L;
			Color alternate = new Color(206, 220, 249);
			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
				Component stamp = super.prepareRenderer(renderer, row, column);
				if (row % 2 == 0)
					stamp.setBackground(alternate);
				else
					stamp.setBackground(this.getBackground());
				return stamp;
			}
		};

		tabelaFiados.setFocusable(false);
		tabelaFiados.setModel(tabela);
		tabelaFiados.setAutoResizeMode( JTable.AUTO_RESIZE_ALL_COLUMNS );
		tabelaFiados.getColumnModel().getColumn(0).setMinWidth(120);
		tabelaFiados.getColumnModel().getColumn(0).setMaxWidth(240);			
		tabelaFiados.getColumnModel().getColumn(1).setMinWidth(230);
		tabelaFiados.getColumnModel().getColumn(1).setMaxWidth(460);
		tabelaFiados.getColumnModel().getColumn(4).setMinWidth(140);
		tabelaFiados.getColumnModel().getColumn(4).setMaxWidth(280);	
		tabelaFiados.getColumnModel().getColumn(5).setMinWidth(60);
		tabelaFiados.getColumnModel().getColumn(5).setMaxWidth(60);
		tabelaFiados.getColumnModel().getColumn(6).setMinWidth(0);
		tabelaFiados.getColumnModel().getColumn(6).setMaxWidth(0);		
		tabelaFiados.setRowHeight(30);
		tabelaFiados.getTableHeader().setReorderingAllowed(false);

		tabelaFiados.getColumn("Deletar").setCellRenderer(new ButtonRenderer());
		tabelaFiados.getColumn("Deletar").setCellEditor(new ButtonEditor(new JCheckBox()));		
		tabelaFiados.getColumn("Dívida").setCellRenderer(new ButtonRenderer());
		tabelaFiados.getColumn("Dívida").setCellEditor(new ButtonEditor(new JCheckBox()));
		tabelaFiados.getColumn("Nome").setCellRenderer(new CustomRenderer());
		tabelaFiados.getColumn("Apelido").setCellRenderer(new CustomRenderer());
		tabelaFiados.getColumn("Telefone").setCellRenderer(new CustomRenderer());
		tabelaFiados.getColumn("CPF").setCellRenderer(new CustomRenderer());
		tabelaFiados.setPreferredScrollableViewportSize(new Dimension(800, 150));

		WebScrollPane scrolltabela = new WebScrollPane(tabelaFiados, true);
		scrolltabela.setFocusable(false);
		scrolltabela.getViewport().setBackground(new Color(237, 237, 237));
		painelTabela.add(scrolltabela, BorderLayout.CENTER);

		graFiados = new GraficoFiados();
		graFiados.setMinimumSize(new Dimension(500, 200));
		graFiados.setMaximumSize(new Dimension(700, 300));		

		visualizarFiado.add(painelTabela);
		visualizarFiado.add(graFiados);

		painelUltimas = new UltimasVendas(config, painelListener);
		TabelaVendas painelConsultar = new TabelaVendas(config, painelListener);
		ConsultarDiario2 painelConsultarDiario = new ConsultarDiario2();
		PainelGastos painelGastos = new PainelGastos(listener);

		ImageIcon iconeUltimas = new ImageIcon(getClass().getClassLoader().getResource("imgs/ultimas_vendas_aba_mini.png"));
		tabbedPane.addTab("Últimas Vendas", iconeUltimas, painelUltimas, "Últimas vendas realizadas.");

		ImageIcon iconeConsultar = new ImageIcon(getClass().getClassLoader().getResource("imgs/consultar_vendas_aba_mini.png"));
		tabbedPane.addTab("Consultar Vendas", iconeConsultar, painelConsultar, "Consultar vendas em determinada data.");			

		ImageIcon iconeFinalizar = new ImageIcon(getClass().getClassLoader().getResource("imgs/fiados_aba_mini.png"));
		tabbedPane.addTab("Fiados", iconeFinalizar, visualizarFiado, "Todas as dívidas em aberto.");
		
		ImageIcon iconeGastos = new ImageIcon(getClass().getClassLoader().getResource("imgs/gastos.png"));
		tabbedPane.addTab("Caderno", iconeGastos, painelGastos, "Gerenciamento de anotações.");

		ImageIcon iconeDiario = new ImageIcon(getClass().getClassLoader().getResource("imgs/diario_mini.png"));
		tabbedPane.addTab("Diário", iconeDiario, painelConsultarDiario, "Registro de todas as ações de funcionários.");	

		add(tabbedPane);
		ToolTipManager.sharedInstance().setDismissDelay(40000);
		refresh();
	}

	class CustomRenderer extends DefaultTableCellRenderer 
	{
		private static final long serialVersionUID = 1L;
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if(isSelected)
			{
				setHorizontalAlignment( JLabel.CENTER );
				c.setForeground(new Color(72, 61, 139));
				return c;
			}
			else
			{
				setHorizontalAlignment( JLabel.CENTER );
				c.setForeground(Color.BLACK);
				return c;
			}
		}
	}

	public void ultimasVendasRefresh() {
		painelUltimas.refresh();
	}

	public void refresh() {
		SwingUtilities.invokeLater(new Runnable() {  
			public void run() {
				graFiados.refresh();
				tabela.setNumRows(0);

				try {
					Query pega = new Query();
					pega.executaQuery("SELECT * FROM fiados ORDER BY nome");

					while(pega.next())
					{
						Vector<String> linha = new Vector<String>();
						double totalDivida = 0.0;
						Query pega2 = new Query();
						pega2.executaQuery("SELECT * FROM vendas WHERE `fiado_id` = " + pega.getInt("fiador_id") + "");

						while(pega2.next())
						{
							double totalPagoVenda = UtilCoffe.precoToDouble(pega2.getString("valor_pago"));
							Query verifica = new Query();
							verifica.executaQuery("SELECT valor FROM gastos WHERE `venda_fiado` = " + pega2.getInt("vendas_id"));
							while(verifica.next()) {
								totalPagoVenda += UtilCoffe.precoToDouble(verifica.getString("valor"));
							}
							verifica.fechaConexao();
							
							if(UtilCoffe.precoToDouble(pega2.getString("total")) > totalPagoVenda) {
								totalDivida += (UtilCoffe.precoToDouble(pega2.getString("total")) - totalPagoVenda);
							}
						}

						linha.add(UtilCoffe.doubleToPreco(totalDivida));
						linha.add(pega.getString("nome"));
						linha.add(pega.getString("apelido"));
						linha.add(pega.getString("telefone"));
						linha.add(pega.getString("cpf"));
						linha.add("");
						linha.add("" + pega.getInt("fiador_id"));

						if(totalDivida > 0) {
							tabela.addRow(linha);
						}
						
						pega2.fechaConexao();
					}

					pega.fechaConexao();
				} catch (NumberFormatException | ClassNotFoundException | SQLException e) {
					e.printStackTrace();
					new PainelErro(e);
				}
			}  
		});		
	}

	class ButtonRenderer extends JButton implements TableCellRenderer
	{
		private static final long serialVersionUID = 1L;

		public ButtonRenderer() {
			setOpaque(true);
		}		  

		@SuppressWarnings("finally")
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {

			setHorizontalTextPosition(AbstractButton.LEFT);

			if(column == 5)
				setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/delete.png")));
			else
				setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/fiados1.png")));

			String formataTip = "<html>";
			try {
				Query pega = new Query();
				pega.executaQuery("SELECT * FROM vendas WHERE `fiado_id` = " + table.getValueAt(row,6) + "");

				while(pega.next())
				{
					if(UtilCoffe.precoToDouble(pega.getString("total")) > UtilCoffe.precoToDouble(pega.getString("valor_pago")))
					{
						double totalPagoVenda = UtilCoffe.precoToDouble(pega.getString("valor_pago"));
						
						Query verifica = new Query();
						verifica.executaQuery("SELECT valor FROM gastos WHERE `venda_fiado` = " + pega.getInt("vendas_id"));
						while(verifica.next()) {
							totalPagoVenda += UtilCoffe.precoToDouble(verifica.getString("valor"));
						}
						verifica.fechaConexao();
						
						if((UtilCoffe.precoToDouble(pega.getString("total")) - totalPagoVenda) > 0)
						{
							formataTip += "<b>Venda #" + pega.getInt("vendas_id") + "</b>  (<i>" + pega.getString("horario") +")</i><br>";
							Query pega2 = new Query();
							pega2.executaQuery("SELECT * FROM vendas_produtos WHERE `id_link` = " + pega.getInt("vendas_id"));
	
							while(pega2.next())
							{
								formataTip += pega2.getInt("quantidade_produto") + "x .......... <b>" + pega2.getString("nome_produto") + "</b>";
	
								if(!"".equals(pega2.getString("adicionais_produto").trim()))
								{
									formataTip += " com " + pega2.getString("adicionais_produto");
								}
	
								formataTip += " - R$" +  pega2.getString("preco_produto") + "<br>";
							}
	
							formataTip += "Total: " + pega.getString("total") + " | Pago: " + UtilCoffe.doubleToPreco(totalPagoVenda);
							formataTip += "<br><br>";
							pega2.fechaConexao();
						}
					}
				}				

				pega.fechaConexao();
				formataTip += "</html>";
			} catch (NumberFormatException | ClassNotFoundException | SQLException e) {
				e.printStackTrace();
				formataTip = "Erro ao acessar bando de dados.";
			}
			finally
			{
				setToolTipText(formataTip);

				if (isSelected) {
					setForeground(table.getSelectionForeground());
					setBackground(table.getSelectionBackground());
				} else {
					setForeground(table.getSelectionForeground());
					setBackground(table.getSelectionBackground());
				}

				setForeground(Color.BLACK);
				setText((value == null) ? "" : value.toString());
				return this;					
			}
		}
	}

	class ButtonEditor extends DefaultCellEditor {
		private static final long serialVersionUID = 1L;
		protected JButton button;
		private String label;
		private boolean isPushed;

		public ButtonEditor(JCheckBox checkBox) {
			super(checkBox);
			button = new JButton();
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

			button.setHorizontalTextPosition(AbstractButton.LEFT);
			label = (value == null) ? "" : value.toString();
			button.setText(label);

			if(column == 5)
				button.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/delete.png")));
			else
				button.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/fiados1.png")));		    

			isPushed = true;
			return button;
		}

		public Object getCellEditorValue() {
			if (isPushed) {
				if(tabelaFiados.getSelectedRowCount() == 1)
				{
					if(tabelaFiados.getSelectedColumn() == 0)
					{
						String bla = "Escreva o valor a ser deduzido da dívida.\n";
						bla += "Importante:\n\n1- Não é possível aumentar a dívida, apenas reduzí-la.\n";
						bla += "2- Não é possível desfazer essa ação.\n3- Se o valor digitado for maior ou igual que o da dívida, a mesma será quitada.\n\n";
						String pegaResposta = JOptionPane.showInputDialog(null, bla, "Reduzir Dívida", JOptionPane.QUESTION_MESSAGE);

						if(pegaResposta == null)
							pegaResposta = "";

						if(!"".equals(pegaResposta.trim()))
						{
							pegaResposta = pegaResposta.replaceAll("[^0-9.,]+","");
							pegaResposta = pegaResposta.replaceAll(",",".");

							double deduzindo = Double.parseDouble(pegaResposta);
							if(deduzindo > 0)
							{
								DiarioLog.add(Usuario.INSTANCE.getNome(), "Reduziu R$" + pegaResposta + " da dívida do " + (String) tabelaFiados.getValueAt(tabelaFiados.getSelectedRow(), 1) 
																	+ " (TEL: " + (String) tabelaFiados.getValueAt(tabelaFiados.getSelectedRow(), 3) + " ) de R$" 
																					+ (String) tabelaFiados.getValueAt(tabelaFiados.getSelectedRow(), 0) + ".", 6);
								try {
									Query pega = new Query();
									pega.executaQuery("SELECT * FROM vendas WHERE `fiado_id` = " + tabelaFiados.getValueAt(tabelaFiados.getSelectedRow(), 6) + "");

									while(pega.next())
									{
										if(UtilCoffe.precoToDouble(pega.getString("total")) > UtilCoffe.precoToDouble(pega.getString("valor_pago")))
										{
											double totalPagoVenda = UtilCoffe.precoToDouble(pega.getString("valor_pago"));
											Query verifica = new Query();
											verifica.executaQuery("SELECT valor FROM gastos WHERE `venda_fiado` = " + pega.getInt("vendas_id"));
											while(verifica.next()) {
												totalPagoVenda += UtilCoffe.precoToDouble(verifica.getString("valor"));
											}
											verifica.fechaConexao();									
											
											totalPagoVenda = UtilCoffe.precoToDouble(pega.getString("total")) - totalPagoVenda;
											if(totalPagoVenda > 0)
											{
												if(totalPagoVenda >= deduzindo)
												{
													Query manda = new Query();
													manda.executaUpdate("INSERT INTO gastos(nome, descricao, data, valor, venda_fiado) VALUES('"
															+ "Pagamento Dívida', '" 
															+ tabelaFiados.getValueAt(tabelaFiados.getSelectedRow(), 1).toString() + " pagou uma parcela de sua dívida da Venda #" + pega.getInt("vendas_id") + "', " 
															+ "CURDATE(), '"
															+ UtilCoffe.doubleToPreco(deduzindo) + "', "
															+ pega.getInt("vendas_id") + ")");
													manda.fechaConexao();
													break;
												}
												else
												{
													Query manda = new Query();
													manda.executaUpdate("INSERT INTO gastos(nome, descricao, data, valor, venda_fiado) VALUES('"
															+ "Pagamento Dívida', '" 
															+ tabelaFiados.getValueAt(tabelaFiados.getSelectedRow(), 1).toString() + " quitou sua dívida da Venda #" + pega.getInt("vendas_id") + "', " 
															+ "CURDATE(), '"
															+ UtilCoffe.doubleToPreco(totalPagoVenda) + "', "
															+ pega.getInt("vendas_id") + ")");
													manda.fechaConexao();
													deduzindo = (deduzindo - totalPagoVenda);
												}
											}
										}
									}

									pega.fechaConexao();
									refresh();
									painelUltimas.refresh();
								} catch (NumberFormatException | ClassNotFoundException | SQLException e) {
									e.printStackTrace();
									new PainelErro(e);
								}
							}
						}
					}
					else	// deletar
					{
						int opcao = JOptionPane.showConfirmDialog(null, "Essa opção irá quitar a dívida.\n\nVocê tem certeza?\n\n", "Quitar Dívida", JOptionPane.YES_NO_OPTION);
						if(opcao == JOptionPane.YES_OPTION)
						{
							DiarioLog.add(Usuario.INSTANCE.getNome(), "Quitou a dívida de " + (String) tabelaFiados.getValueAt(tabelaFiados.getSelectedRow(), 1) 
																	+ " (TEL: " + (String) tabelaFiados.getValueAt(tabelaFiados.getSelectedRow(), 3) + " ) de R$" 
																				+ (String) tabelaFiados.getValueAt(tabelaFiados.getSelectedRow(), 0) + ".", 6);
							try {
								Query pega = new Query();
								pega.executaQuery("SELECT * FROM vendas WHERE `fiado_id` = " + tabelaFiados.getValueAt(tabelaFiados.getSelectedRow(), 6));
								
								while(pega.next())
								{
									if(UtilCoffe.precoToDouble(pega.getString("total")) > UtilCoffe.precoToDouble(pega.getString("valor_pago")))
									{
										double totalPagoVenda = UtilCoffe.precoToDouble(pega.getString("valor_pago"));
										Query verifica = new Query();
										verifica.executaQuery("SELECT valor FROM gastos WHERE `venda_fiado` = " + pega.getInt("vendas_id"));
										while(verifica.next()) {
											totalPagoVenda += UtilCoffe.precoToDouble(verifica.getString("valor"));
										}
										verifica.fechaConexao();
										
										totalPagoVenda = UtilCoffe.precoToDouble(pega.getString("total")) - totalPagoVenda;
										if(totalPagoVenda > 0)
										{
											Query manda = new Query();
											manda.executaUpdate("INSERT INTO gastos(nome, descricao, data, valor, venda_fiado) VALUES('"
													+ "Pagamento Dívida', '" 
													+ tabelaFiados.getValueAt(tabelaFiados.getSelectedRow(), 1).toString() + " quitou sua dívida da Venda #" + pega.getInt("vendas_id") + "', " 
													+ "CURDATE(), '"
													+ UtilCoffe.doubleToPreco(totalPagoVenda) + "', "
													+ pega.getInt("vendas_id") + ")");
											manda.fechaConexao();	
										}
									}
								}
								pega.fechaConexao();
								refresh();
								painelUltimas.refresh();
							} catch (NumberFormatException | ClassNotFoundException | SQLException e) {
								e.printStackTrace();
								new PainelErro(e);
							}
						}
					}
				}
			}
			isPushed = false;
			return new String(label);
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
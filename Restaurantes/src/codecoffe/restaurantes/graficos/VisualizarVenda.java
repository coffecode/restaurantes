package codecoffe.restaurantes.graficos;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import net.miginfocom.swing.MigLayout;
import codecoffe.restaurantes.eventos.AtualizarPainel;
import codecoffe.restaurantes.mysql.Query;
import codecoffe.restaurantes.primitivas.Clientes;
import codecoffe.restaurantes.utilitarios.Configuracao;
import codecoffe.restaurantes.utilitarios.DiarioLog;
import codecoffe.restaurantes.utilitarios.Header;
import codecoffe.restaurantes.utilitarios.Usuario;
import codecoffe.restaurantes.utilitarios.UtilCoffe;

import com.alee.laf.WebLookAndFeel;
import com.alee.laf.button.WebButton;
import com.alee.laf.combobox.WebComboBox;
import com.alee.laf.rootpane.WebDialog;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;

public class VisualizarVenda extends JFrame
{
	private static final long serialVersionUID = 1L;
	private int vendaID, clienteID;
	private double totalVenda, taxaEntrega, taxaDezPorcento;
	private JTextField campoValor, campoTroco, campoTotal;
	private WebComboBox campoForma, campoAtendente;
	private WebButton bSalvarVenda, bCliente, bDeletarCliente;
	private JCheckBox dezPorcento, delivery;
	private DefaultTableModel tabela;
	private Configuracao config;
	private AtualizarPainel painelListener;
	private VisualizarVenda vvcache;

	public VisualizarVenda(int vendaid, Configuracao cfg, AtualizarPainel listener)
	{
		config = cfg;
		painelListener = listener;
		vendaID = vendaid;
		clienteID = 0;
		WebLookAndFeel.install();
		setIconImage(new ImageIcon(getClass().getClassLoader().getResource("imgs/icone_programa.png")).getImage());
		setDefaultCloseOperation(WebDialog.DO_NOTHING_ON_CLOSE);
		
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{	
				dispose();
			}
		});
		
		setResizable(false);
		setTitle("Visualizar Venda #" + vendaid);
		setPreferredSize(new Dimension(600, 500));
		JPanel visualizaPainel = new JPanel(new MigLayout("align left", "[]20[]"));
		visualizaPainel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		
		try {
			Query pega = new Query();
			pega.executaQuery("SELECT * FROM vendas WHERE vendas_id = " + vendaid);
			
			if(pega.next())
			{
				tabela = new DefaultTableModel() {
					private static final long serialVersionUID = 1L;
					@Override
				    public boolean isCellEditable(int row, int column) {				       
				       return false;
				    }
				};
				
				tabela.addColumn("Nome");
				tabela.addColumn("Adicionais");
				tabela.addColumn("Qntd");
				tabela.addColumn("Total");
				
				Query pega2 = new Query();
				pega2.executaQuery("SELECT * FROM vendas_produtos WHERE id_link = " + vendaid);
				
				while(pega2.next())
				{
					Vector<String> linha = new Vector<String>();		
					
					linha.add(pega2.getString("nome_produto"));
					linha.add(pega2.getString("adicionais_produto"));
					linha.add("" + pega2.getInt("quantidade_produto"));
					linha.add(pega2.getString("preco_produto"));
					tabela.addRow(linha);	
				}
				
				pega2.fechaConexao();
				
				JTable tabelaVendas = new JTable() {
					private static final long serialVersionUID = 1L;
					Color alternate = new Color(206, 220, 249);
				    @Override
				    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
				        Component stamp = super.prepareRenderer(renderer, row, column);
				        if (row % 2 == 0 && column != 6)
				            stamp.setBackground(alternate);
				        else
				            stamp.setBackground(this.getBackground());
				        return stamp;
				    }    
				};
				
				tabelaVendas.setModel(tabela);
				tabelaVendas.setFocusable(false);
				tabelaVendas.getColumnModel().getColumn(0).setMinWidth(100);
				tabelaVendas.getColumnModel().getColumn(0).setMaxWidth(200);
				tabelaVendas.getColumnModel().getColumn(1).setMinWidth(200);
				tabelaVendas.getColumnModel().getColumn(1).setMaxWidth(300);
				tabelaVendas.getColumnModel().getColumn(2).setMinWidth(50);
				tabelaVendas.getColumnModel().getColumn(2).setMaxWidth(50);		
				tabelaVendas.getColumnModel().getColumn(3).setMinWidth(100);
				tabelaVendas.getColumnModel().getColumn(3).setMaxWidth(100);		
				tabelaVendas.setRowHeight(25);
				tabelaVendas.getTableHeader().setReorderingAllowed(false);
				tabelaVendas.getColumn("Nome").setCellRenderer(new CustomRenderer());
				tabelaVendas.getColumn("Adicionais").setCellRenderer(new CustomRenderer());
				tabelaVendas.getColumn("Qntd").setCellRenderer(new CustomRenderer());
				tabelaVendas.getColumn("Total").setCellRenderer(new CustomRenderer());
				tabelaVendas.setPreferredScrollableViewportSize(new Dimension(565, 80));
				JScrollPane scrolltabela = new JScrollPane(tabelaVendas, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
				scrolltabela.getViewport().setBackground(new Color(237, 237, 237));
				scrolltabela.setFocusable(false);
				
				ItemListener il = new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent e) {
						if(e.getItemSelectable() == delivery)
						{
							if(delivery.isSelected())
							{
								dezPorcento.setSelected(false);
								taxaEntrega = config.getTaxaEntrega();
								delivery.setText("+ Delivery (" + UtilCoffe.doubleToPreco(taxaEntrega) + ")");
								campoTotal.setText(UtilCoffe.doubleToPreco(totalVenda + taxaEntrega));
							}
							else
							{
								campoTotal.setText(UtilCoffe.doubleToPreco(totalVenda));
								delivery.setText("+ Delivery");
								taxaEntrega = 0.0;
							}
						}
						else
						{
							if(dezPorcento.isSelected())
							{
								delivery.setSelected(false);
								taxaDezPorcento = (totalVenda*0.10);
								dezPorcento.setText("+ 10% Opcional (" + UtilCoffe.doubleToPreco(taxaDezPorcento) + ")");
								campoTotal.setText(UtilCoffe.doubleToPreco(totalVenda + taxaDezPorcento));								
							}
							else
							{
								campoTotal.setText(UtilCoffe.doubleToPreco(totalVenda));
								dezPorcento.setText("+ 10% Opcional");
								taxaDezPorcento = 0.0;								
							}
						}
					}
				};
				
				if(UtilCoffe.precoToDouble(pega.getString("dezporcento")) > 0) {
					dezPorcento = new JCheckBox("+ 10% Opcional (" + pega.getString("dezporcento") + ")");
					dezPorcento.setSelected(true);
				}
				else {
					dezPorcento = new JCheckBox("+ 10% Opcional");
				}
				
				dezPorcento.setPreferredSize(new Dimension(220, 30));
				dezPorcento.addItemListener(il);
				
				if(UtilCoffe.precoToDouble(pega.getString("delivery")) > 0) {
					delivery = new JCheckBox("+ Delivery (" + pega.getString("delivery") + ")");
					delivery.setSelected(true);
				}
				else {
					delivery = new JCheckBox("+ Delivery");
				}
				
				delivery.setPreferredSize(new Dimension(220, 30));
				delivery.addItemListener(il);
				
				if(pega.getString("forma_pagamento").equals("Fiado")) {
					double totalPagoVenda = UtilCoffe.precoToDouble(pega.getString("valor_pago"));
					Query verifica = new Query();
					verifica.executaQuery("SELECT valor FROM gastos WHERE `venda_fiado` = " + pega.getInt("vendas_id"));
					while(verifica.next()) {
						totalPagoVenda += UtilCoffe.precoToDouble(verifica.getString("valor"));
					}
					verifica.fechaConexao();
					
					campoValor = new JTextField(UtilCoffe.doubleToPreco(totalPagoVenda));
				}
				else
					campoValor = new JTextField(pega.getString("valor_pago"));
				
				campoValor.setHorizontalAlignment(SwingConstants.CENTER);
				campoValor.setPreferredSize(new Dimension(90, 30));
				campoValor.addKeyListener(new KeyAdapter()
		        {
		        	public void keyPressed(KeyEvent e)
		        	{
		        		int code = e.getKeyCode();
		        		if(code==KeyEvent.VK_ENTER) {
		        			calculaTroco(campoValor.getText(), campoTotal.getText());
		        		}
		            }
		        });	
				
				campoTroco = new JTextField(pega.getString("troco"));
				campoTroco.setHorizontalAlignment(SwingConstants.CENTER);
				campoTroco.setEnabled(false);
				campoTroco.setPreferredSize(new Dimension(90, 30));
				
				totalVenda = UtilCoffe.precoToDouble(pega.getString("total"));
				taxaEntrega = UtilCoffe.precoToDouble(pega.getString("delivery"));
				taxaDezPorcento = UtilCoffe.precoToDouble(pega.getString("dezporcento"));
				totalVenda -= taxaEntrega;
				totalVenda -= taxaDezPorcento;
				
				campoTotal = new JTextField(pega.getString("total"));
				campoTotal.setHorizontalAlignment(SwingConstants.CENTER);
				campoTotal.setEnabled(false);
				campoTotal.setPreferredSize(new Dimension(90, 30));
				
				String[] tiposPagamento = {"Dinheiro", "Ticket Refeição", "Cartão de Crédito", 
						"Cartão de Débito", "Cheque", "Fiado" };
				campoForma = new WebComboBox(tiposPagamento);
				campoForma.setSelectedItem(pega.getString("forma_pagamento"));
				campoForma.setPreferredSize(new Dimension(120, 35));
				
				Query pega3 = new Query();
				pega3.executaQuery("SELECT nome FROM funcionarios WHERE nome != '" + pega.getString("atendente") + "' ORDER BY nome");
				ArrayList<String> listaFunc = new ArrayList<String>();
				listaFunc.add(pega.getString("atendente"));
				
				while(pega3.next()) {
					listaFunc.add(pega3.getString("nome"));
				}
				
				pega3.fechaConexao();
				
				campoAtendente = new WebComboBox(listaFunc.toArray());
				campoAtendente.setSelectedIndex(0);
				campoAtendente.setPreferredSize(new Dimension(150, 35));
				
				vvcache = this;
				
				ActionListener al = new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if(e.getSource() == bCliente)
						{
							painelListener.atualizarPainel(new Header(UtilCoffe.UPDATE_CALLBACK, vvcache, new Integer(clienteID)));
							painelListener.atualizarPainel(new Header(UtilCoffe.ABRIR_CLIENTES));
						}
						else if(e.getSource() == bDeletarCliente)
						{
							bCliente.setText("Nenhum");
							clienteID = 0;
						}
						else if(e.getSource() == bSalvarVenda)
						{
							try {	// verificar se a venda ainda existe e nao foi deletada.
								Query pega9 = new Query();
								pega9.executaQuery("SELECT ano FROM vendas WHERE vendas_id = " + vendaID);
								
								if(!pega9.next())
								{
									pega9.fechaConexao();
									dispose();
								}
								
								pega9.fechaConexao();
							} catch (ClassNotFoundException | SQLException e2) {
								e2.printStackTrace();
								new PainelErro(e2);
							}
							
							campoValor.setText(UtilCoffe.limpaNumeroDecimal(campoValor.getText()));
							
							if(UtilCoffe.vaziu(campoValor.getText())) {
								campoValor.setText("0,00");
							}
							
							if(campoForma.getSelectedItem() != "Fiado" && UtilCoffe.precoToDouble(campoValor.getText()) < UtilCoffe.precoToDouble(campoTotal.getText()))
							{
								campoValor.setText(campoTotal.getText());
							}
							
							calculaTroco(campoValor.getText(), campoTotal.getText());
							campoValor.setText(UtilCoffe.doubleToPreco(UtilCoffe.precoToDouble(campoValor.getText())));
							
							if(campoForma.getSelectedItem() == "Fiado" && clienteID == 0)
							{
								JOptionPane.showMessageDialog(null, "Escolha um cliente para o fiado antes!");
							}
							else
							{
								if(campoForma.getSelectedItem() == "Fiado" && 
										UtilCoffe.precoToDouble(campoValor.getText()) >= UtilCoffe.precoToDouble(campoTotal.getText()))
								{
									JOptionPane.showMessageDialog(null, "Pagamento fiado não pode ter valor recebido maior ou igual ao total!");
								}
								else
								{
									try {
										Query pega6 = new Query();
										
										String formata = "UPDATE vendas SET ";
										formata += "`atendente` = '" + campoAtendente.getSelectedItem().toString() + "', ";
										formata += "`forma_pagamento` = '" + campoForma.getSelectedItem().toString() + "', ";
										formata += "`total` = '" + campoTotal.getText() + "', ";
										formata += "`valor_pago` = '" + campoValor.getText() + "', ";
										formata += "`troco` = '" + campoTroco.getText() + "', ";
										formata += "`delivery` = '" + UtilCoffe.doubleToPreco(taxaEntrega) + "', ";
										formata += "`dezporcento` = '" + UtilCoffe.doubleToPreco(taxaDezPorcento) + "', ";
										formata += "`fiado_id` = " + clienteID;
										formata += " WHERE `vendas_id` = " + vendaID;
										
										pega6.executaUpdate(formata);
										pega6.fechaConexao();
										DiarioLog.add(Usuario.INSTANCE.getNome(), "Editou a venda #" + vendaID + " de valor R$" + campoTotal.getText() + ".", 7);
										
										painelListener.atualizarPainel(new Header(UtilCoffe.UPDATE_VENDAS));
										if(clienteID > 0)
											painelListener.atualizarPainel(new Header(UtilCoffe.UPDATE_FIADOS));
										
										TooltipManager.showOneTimeTooltip(bSalvarVenda, null, "Venda salva!", TooltipWay.up);
									} catch (ClassNotFoundException | SQLException e1) {
										e1.printStackTrace();
										new PainelErro(e1);
									}
								}
							}
						}
					}
				};
				
				bCliente = new WebButton("Cliente");
				bCliente.setHorizontalTextPosition(AbstractButton.LEFT);
				bCliente.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/report_user_mini.png")));	
				bCliente.setPreferredSize(new Dimension(150, 35));
				bCliente.setMaximumSize(new Dimension(150, 35));
				bCliente.setRolloverShine(true);
				bCliente.addActionListener(al);
				
				bDeletarCliente = new WebButton("");
				bDeletarCliente.setUndecorated(true);
				bDeletarCliente.setPreferredSize(new Dimension(20, 20));
				bDeletarCliente.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/delete.png")));	
				bDeletarCliente.addActionListener(al);
				
				bSalvarVenda = new WebButton("Salvar Venda");
				bSalvarVenda.setPreferredSize(new Dimension(130, 40));
				bSalvarVenda.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/salvar.png")));
				bSalvarVenda.setRolloverShine(true);
				bSalvarVenda.addActionListener(al);
				
				visualizaPainel.add(new JLabel("Data: "));
				visualizaPainel.add(new JLabel(pega.getString("horario") + ",  " + getDiaSemana(pega.getInt("dia_semana"))), "wrap");
				visualizaPainel.add(new JLabel("Local: "));
				
				if(pega.getInt("caixa") > 0)
					visualizaPainel.add(new JLabel(config.getTipoNome() + " " + pega.getInt("caixa")), "wrap");
				else
					visualizaPainel.add(new JLabel("Balcão"), "wrap");
				
				visualizaPainel.add(new JLabel("Total: "), "gaptop 30px");
				visualizaPainel.add(campoTotal);
				
				visualizaPainel.add(new JLabel("Cliente: "), "gapleft 50px, split 3");
				
				if(pega.getInt("fiado_id") > 0)
				{
					clienteID = pega.getInt("fiado_id");
					
					Query pega4 = new Query();
					pega4.executaQuery("SELECT nome FROM fiados WHERE fiador_id = " + pega.getInt("fiado_id"));
					
					if(pega4.next())
					{
						bCliente.setText(pega4.getString("nome"));
						visualizaPainel.add(bCliente);
						visualizaPainel.add(bDeletarCliente, "gaptop 20px, wrap");
					}
					else
					{
						bCliente.setText("Indisponível");
						visualizaPainel.add(bCliente);
						visualizaPainel.add(bDeletarCliente, "gaptop 20px, wrap");
					}
					
					pega4.fechaConexao();
				}
				else
				{
					bCliente.setText("Nenhum");
					visualizaPainel.add(bCliente);
					visualizaPainel.add(bDeletarCliente, "gaptop 20px, wrap");
				}
				
				visualizaPainel.add(new JLabel("Recebido: "));
				visualizaPainel.add(campoValor);
				visualizaPainel.add(dezPorcento, "gapleft 50px, span 3, wrap");
				
				visualizaPainel.add(new JLabel("Troco: "));
				
				if(pega.getInt("caixa") == 0) {
					visualizaPainel.add(campoTroco);
					visualizaPainel.add(delivery, "gapleft 50px, span 3, wrap");
				}
				else {
					visualizaPainel.add(campoTroco, "wrap");
				}
				
				visualizaPainel.add(new JLabel("Atendente: "), "gaptop 30px");
				visualizaPainel.add(campoAtendente, "wrap");
				visualizaPainel.add(new JLabel("Pagamento: "), "gaptop 15px");
				visualizaPainel.add(campoForma, "wrap");
				
				visualizaPainel.add(scrolltabela, "span, gaptop 20px");
				visualizaPainel.add(bSalvarVenda, "align right, span, gaptop 20px");
			}
			else
			{
				visualizaPainel.add(new JLabel("Nenhuma venda encontrada."), "align center, gaptop 50px");
			}
			
			pega.fechaConexao();
		} catch (ClassNotFoundException | SQLException e1) {
			e1.printStackTrace();
			new PainelErro(e1);
		}
		
		add(visualizaPainel);
		pack();
		setVisible(true);
		
		ActionMap actionMap = visualizaPainel.getActionMap();
		actionMap.put("botao1", new SpaceAction());
		visualizaPainel.setActionMap(actionMap);
		
		InputMap imap = visualizaPainel.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
		imap.put(KeyStroke.getKeyStroke("ESCAPE"), "botao1");
	}
	
	private class SpaceAction extends AbstractAction 
	{
		private static final long serialVersionUID = 1L;
		
		public SpaceAction() {
	        super();
	    }
		
        @Override
        public void actionPerformed(ActionEvent e) 
        {	
        	dispose();
        }
    }
	
	public String getDiaSemana(int dia)
	{
		switch(dia)
		{
			case 1:
			{
				return "Domingo";
			}
			case 2:
			{
				return "Segunda-feira";
			}
			case 3:
			{
				return "Terça-feira";
			}
			case 4:
			{
				return "Quarta-feira";
			}	
			case 5:
			{
				return "Quinta-feira";
			}
			case 6:
			{
				return "Sexta-feira";
			}
			case 7:
			{
				return "Sábado";
			}										
		}
		
		return null;
	}
	
	public void setCliente(Clientes cliente)
	{
		clienteID = cliente.getIdUnico();
		bCliente.setText(cliente.getNome());
	}
	
	public void calculaTroco(String recebido, String total)
	{
		String limpeza = recebido.replaceAll("[^0-9.,]+","");

		if(!"".equals(limpeza.trim()))
		{
			double pegaTotal = Double.parseDouble(total.replaceAll(",", "."));
			double pegaRecebido = Double.parseDouble(limpeza.replaceAll(",", "."));

			if(((pegaTotal - pegaRecebido)*-1) <= 0) {
				campoTroco.setText("0,00");
			}
			else
			{
				String resultado = String.format("%.2f", (pegaTotal - pegaRecebido)*-1);
				resultado.replaceAll(",", ".");
				campoTroco.setText(resultado);					
			}
		}		
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
}
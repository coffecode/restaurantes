package codecoffe.restaurantes.graficos;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;
import codecoffe.restaurantes.mysql.Query;
import codecoffe.restaurantes.utilitarios.DiarioLog;
import codecoffe.restaurantes.utilitarios.Usuario;
import codecoffe.restaurantes.utilitarios.UtilCoffe;
import codecoffe.restaurantes.utilitarios.GraficoGastos;

import com.alee.extended.date.WebDateField;
import com.alee.extended.window.PopOverDirection;
import com.alee.extended.window.WebPopOver;
import com.alee.laf.button.WebButton;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.text.WebTextField;
import com.alee.managers.notification.NotificationManager;

public class PainelGastos extends WebPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;
	private static final int vendasPagina = 50;
	private JTable tabela;
	private DefaultTableModel tabelaModel;
	private WebDateField dataInicial, dataFinal, campoData;
	private WebButton pesquisar, adicionarGasto, verGraficoGastos;
	private JComboBox<String> paginacao;
	private String pesquisaFormat;
	private WebPopOver popOver;
	private WebTextField campoNome, campoDescricao, campoValor;
	private JLabel labelGasto;
	private double gastoTotal;

	public PainelGastos()
	{
		setLayout(new MigLayout("fill", "15[]15[]20[]15", "10[]10[]"));
		gastoTotal = 0;

		tabela = new JTable();
		tabelaModel = new DefaultTableModel() {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int column) {
				if(column == (tabela.getColumnCount()-1)) return true;
				return false;
			}
		};

		tabelaModel.addColumn("ID");
		tabelaModel.addColumn("Data");
		tabelaModel.addColumn("Nome");
		tabelaModel.addColumn("Descrição");
		tabelaModel.addColumn("Valor");
		tabelaModel.addColumn("Deletar");

		tabela.setModel(tabelaModel);
		tabela.setRowHeight(28);
		tabela.getTableHeader().setReorderingAllowed(false);
		tabela.setFocusable(false);
		tabela.setDefaultRenderer(Object.class, new TabelaVendasRenderer());

		tabela.getColumn("ID").setMinWidth(0);
		tabela.getColumn("ID").setMaxWidth(0);
		tabela.getColumn("Data").setPreferredWidth(120);
		tabela.getColumn("Nome").setPreferredWidth(200);
		tabela.getColumn("Descrição").setPreferredWidth(400);
		tabela.getColumn("Valor").setPreferredWidth(130);
		tabela.getColumn("Deletar").setMinWidth(60);
		tabela.getColumn("Deletar").setMaxWidth(60);
		tabela.getColumn("Deletar").setCellEditor(new ButtonEditor(new JCheckBox()));

		dataInicial = new WebDateField(new Date());
		dataInicial.setHorizontalAlignment(SwingConstants.CENTER);
		dataInicial.setMinimumSize(new Dimension(130, 35));
		dataInicial.setEditable(false);

		dataFinal = new WebDateField(new Date());
		dataFinal.setHorizontalAlignment(SwingConstants.CENTER);
		dataFinal.setMinimumSize(new Dimension(130, 35));
		dataFinal.setEditable(false);
		
		labelGasto = new JLabel("Total: R$0,00");
		labelGasto.setFont(new Font("Verdana", Font.BOLD, 12));

		pesquisar = new WebButton("Pesquisar");
		pesquisar.setRolloverShine(true);
		pesquisar.setPreferredSize(new Dimension(120, 35));
		pesquisar.setHorizontalTextPosition(AbstractButton.LEFT);
		pesquisar.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/pesquisa_mini.png")));
		pesquisar.addActionListener(this);

		adicionarGasto = new WebButton();
		adicionarGasto.setToolTipText("Adicionar Anotação");
		adicionarGasto.setRolloverDecoratedOnly(true);
		adicionarGasto.setPreferredSize(new Dimension(32, 32));
		adicionarGasto.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/concluir.png")));
		adicionarGasto.addActionListener(this);
		
		verGraficoGastos = new WebButton();
		verGraficoGastos.setToolTipText("Gráfico de Anotações");
		verGraficoGastos.setRolloverDecoratedOnly(true);
		verGraficoGastos.setPreferredSize(new Dimension(32, 32));
		verGraficoGastos.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/estatisticas_aba.png")));
		verGraficoGastos.addActionListener(this);

		WebScrollPane scrolltabela = new WebScrollPane(tabela, true);
		scrolltabela.getViewport().setBackground(new Color(237, 237, 237));
		scrolltabela.setFocusable(false);

		String[] paginas = {"Página 1/1"};
		paginacao = new JComboBox<String>(paginas);
		paginacao.setMaximumRowCount(5);
		paginacao.setMinimumSize(new Dimension(120, 30));

		add(new JLabel("Início: "), "gapleft 10px, split 5");
		add(dataInicial, "gapleft 10px");
		add(new JLabel("Fim: "), "gapleft 20px");
		add(dataFinal, "gapleft 10px");
		add(pesquisar, "gapleft 30px");
		add(adicionarGasto, "align 100%, split 2, span");
		add(verGraficoGastos, "align 100%, wrap");
		add(scrolltabela, "grow, pushy, span, wrap");
		add(labelGasto, "align left, gapleft 10px");
		add(paginacao, "align 100%, span");

		ToolTipManager.sharedInstance().setInitialDelay(0);

		paginacao.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					if(!UtilCoffe.vaziu(pesquisaFormat))
					{
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								int start = (paginacao.getSelectedIndex() * vendasPagina);
								pesquisarGastos(start, vendasPagina);
							}
						});
					}
				}
			}
		});

		campoNome = new WebTextField();
		campoNome.setMinimumSize(new Dimension(200, 35));
		campoNome.setMargin(5);

		campoDescricao = new WebTextField();
		campoDescricao.setMinimumSize(new Dimension(300, 35));
		campoDescricao.setMargin(5);

		campoValor = new WebTextField();
		campoValor.setMinimumSize(new Dimension(100, 35));
		campoValor.setMargin(5);
		campoValor.setHorizontalAlignment(SwingConstants.CENTER);

		campoData = new WebDateField(new Date());
		campoData.setHorizontalAlignment(SwingConstants.CENTER);
		campoData.setMinimumSize(new Dimension(130, 35));
		campoData.setEditable(false);

		popOver = new WebPopOver(adicionarGasto);
		popOver.setModal(true);
		popOver.setMargin(10);
		popOver.setMovable(false);
		popOver.setLayout(new MigLayout());

		popOver.add(new JLabel("<html><b>Adicionar Anotação</b></html>"), "wrap, span, align center");
		popOver.add(new JLabel("Nome:"), "gaptop 20px");
		popOver.add(campoNome, "gapleft 15px, wrap");
		popOver.add(new JLabel("Descrição:"));
		popOver.add(campoDescricao, "gapleft 15px, wrap");
		popOver.add(new JLabel("Valor:"));
		popOver.add(campoValor, "gapleft 15px, wrap");
		popOver.add(new JLabel("Data:"));
		popOver.add(campoData, "gapleft 15px, wrap");

		WebButton adicionar = new WebButton("Adicionar");
		adicionar.setPreferredSize(new Dimension(100, 28));
		adicionar.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/plus2.png")));
		adicionar.addActionListener(this);
		popOver.add(adicionar, "gaptop 15px, span, split 2, align right");

		JButton close = new JButton("Fechar");
		close.setPreferredSize(new Dimension(50, 28));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				popOver.dispose();
			}
		});
		popOver.add(close, "gapleft 20px");
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if(e.getSource() == pesquisar)
		{
			SimpleDateFormat formataDataSQL = new SimpleDateFormat("yyyy-M-dd");	
			pesquisaFormat = "SELECT * FROM gastos WHERE data BETWEEN ('" 
					+ formataDataSQL.format(dataInicial.getDate()) + "') " 
					+ "AND ('" + formataDataSQL.format(dataFinal.getDate()) + "')";

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						Query pega = new Query();
						pega.executaQuery(pesquisaFormat);
						int quantidade = pega.getRowCount();
						pega.fechaConexao();

						gerarPaginas((int) Math.ceil(((double) quantidade)/((double) vendasPagina)));

						pesquisaFormat += " LIMIT ";
						pesquisarGastos(0, vendasPagina);
					} catch (ClassNotFoundException | SQLException e1) {
						e1.printStackTrace();
						new PainelErro(e1);
					}
				}
			});
		}
		else if(e.getSource() == adicionarGasto)
		{
			popOver.show((WebButton) e.getSource (), PopOverDirection.left);
			campoNome.requestFocus();
		}
		else if(e.getSource() == verGraficoGastos)
		{
			JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
			final WebPopOver popOver = new WebPopOver(topFrame);
            popOver.setCloseOnFocusLoss(true);
            popOver.setMargin(10);
            popOver.setLayout(new MigLayout());
            popOver.add(new GraficoGastos());
            popOver.show(topFrame);
		}
		else
		{
			campoNome.setText((campoNome.getText().replaceAll("'", "")));
			campoDescricao.setText((campoDescricao.getText().replaceAll("'", "")));
			campoValor.setText((campoValor.getText().replaceAll("'", "")));
			campoValor.setText((UtilCoffe.limpaNumeroDecimalNegativo(campoValor.getText())));

			if(UtilCoffe.vaziu(campoValor.getText()))
				campoValor.setText("0,00");

			if(UtilCoffe.vaziu(campoNome.getText()))
			{
				JOptionPane.showMessageDialog(null, "O campo nome é de preenchimento obrigatório!");
			}
			else if(campoNome.getText().length() > 100) {
				JOptionPane.showMessageDialog(null, "Máximo de 100 caracteres no nome!");
			}
			else if(campoDescricao.getText().length() > 200) {
				JOptionPane.showMessageDialog(null, "Máximo de 200 caracteres na descrição!");
			}
			else if(campoValor.getText().length() > 20 || UtilCoffe.precoToDouble(campoValor.getText()) == 0) {
				JOptionPane.showMessageDialog(null, "Valor inválido!");
			}
			else
			{
				campoValor.setText(UtilCoffe.doubleToPreco(UtilCoffe.precoToDouble(campoValor.getText())));

				try {
					SimpleDateFormat formataDataSQL = new SimpleDateFormat("yyyy-M-dd");
					SimpleDateFormat formataDataTabela = new SimpleDateFormat("dd/MM/yyyy");
					int novoid = 0;

					Query envia = new Query();
					envia.executaUpdate("INSERT INTO gastos(nome, descricao, valor, data) VALUES('"
							+ campoNome.getText() + "', '"
							+ campoDescricao.getText() + "', '"
							+ campoValor.getText() + "', '"
							+ formataDataSQL.format(campoData.getDate()) + "');");

					envia.executaQuery("SELECT id FROM gastos ORDER BY id DESC limit 0,1");
					if(envia.next()) {
						novoid = envia.getInt("id");
					}
					envia.fechaConexao();
					
					SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
					
					if(dataInicial.getDate().compareTo(campoData.getDate()) * campoData.getDate().compareTo(dataFinal.getDate()) > 0 ||
							fmt.format(dataInicial.getDate()).equals(fmt.format(campoData.getDate())) ||
							fmt.format(dataFinal.getDate()).equals(fmt.format(campoData.getDate()))) {
						
						Vector<String> linha = new Vector<String>();
						linha.add("" + novoid);
						linha.add(formataDataTabela.format(campoData.getDate()));
						linha.add(campoNome.getText());
						linha.add(campoDescricao.getText());
						linha.add(campoValor.getText());
						linha.add(" ");
						tabelaModel.addRow(linha);
						
						gastoTotal += UtilCoffe.precoToDouble(campoValor.getText());
						labelGasto.setText("Total: R$" + UtilCoffe.doubleToPreco(gastoTotal));
						
						tabela.getSelectionModel().setSelectionInterval((tabelaModel.getRowCount()-1), (tabelaModel.getRowCount()-1));
						tabela.scrollRectToVisible(new Rectangle(tabela.getCellRect((tabelaModel.getRowCount()-1), 0, true)));
					}
					
					DiarioLog.add(Usuario.INSTANCE.getNome(), "Adicionou a anotação " + campoNome.getText() 
							+ " de valor R$" + campoValor.getText() + ".", 10);

					campoNome.setText("");
					campoDescricao.setText("");
					campoValor.setText("");
					campoData.setDate(new Date());
					popOver.dispose();

					NotificationManager.setLocation(2);
					NotificationManager.showNotification(tabela, "Anotação Adicionada!", 
							new ImageIcon(getClass().getClassLoader().getResource("imgs/notifications_ok.png"))).setDisplayTime(2000);

				} catch (ClassNotFoundException | SQLException e1) {
					e1.printStackTrace();
					new PainelErro(e1);
				}
			}
		}
	}

	public void gerarPaginas(int paginas)
	{
		if(paginas > 0)
		{
			DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) paginacao.getModel();
			model.removeAllElements();

			for(int i = 0; i < paginas; i++) {
				model.addElement("Página " + (i+1) + "/" + paginas);
			}	
		}
		else
		{
			DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) paginacao.getModel();
			model.removeAllElements();
			model.addElement("Página 1/1");
		}
	}

	public void pesquisarGastos(int inicio, int fim)
	{
		tabelaModel.setNumRows(0);
		gastoTotal = 0;

		try {
			SimpleDateFormat formataDataSQL = new SimpleDateFormat("yyyy-M-dd");
			SimpleDateFormat formataDataTabela = new SimpleDateFormat("dd/MM/yyyy");
			Query pega = new Query();
			pega.executaQuery(pesquisaFormat + inicio + ", " + fim);

			while(pega.next())
			{
				Vector<String> linha = new Vector<String>();
				linha.add("" + pega.getInt("id"));

				try {
					Date dia = formataDataSQL.parse(pega.getString("data"));
					linha.add(formataDataTabela.format(dia));
				} catch (ParseException e) {
					e.printStackTrace();
					linha.add("-");
				}

				linha.add(pega.getString("nome"));
				linha.add(pega.getString("descricao"));
				linha.add(pega.getString("valor"));
				linha.add(" ");
				tabelaModel.addRow(linha);
				gastoTotal += UtilCoffe.precoToDouble(pega.getString("valor"));
			}

			pega.fechaConexao();
			labelGasto.setText("Total: R$" + UtilCoffe.doubleToPreco(gastoTotal));
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			new PainelErro(e);
		}
	}

	private class TabelaVendasRenderer extends DefaultTableCellRenderer
	{
		private static final long serialVersionUID = 1L;
		private Color lucro = new Color(206, 249, 209);
		private Color lucro_selecionado = new Color(97, 161, 97);
		private Color gasto = new Color(249, 206, 206);
		private Color gasto_selecionado = new Color(171, 91, 91);
		private ImageIcon iconDelete = new ImageIcon(getClass().getClassLoader().getResource("imgs/delete.png"));
		private JButton bDeletar = new JButton(iconDelete);

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, 
				boolean isSelected, boolean hasFocus, int row, int column) {
			JLabel cellComponent = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if((tabelaModel.getColumnCount() - 1) == column) {
				return bDeletar;
			}

			if(isSelected) {
				if(UtilCoffe.precoToDouble(tabela.getValueAt(row, 4).toString()) >= 0)
					cellComponent.setBackground(lucro_selecionado);
				else
					cellComponent.setBackground(gasto_selecionado);
			}
			else if(UtilCoffe.precoToDouble(tabela.getValueAt(row, 4).toString()) >= 0)
				cellComponent.setBackground(lucro);
			else
				cellComponent.setBackground(gasto);

			setHorizontalAlignment(JLabel.CENTER);
			return cellComponent;
		}
	}

	private class ButtonEditor extends DefaultCellEditor {
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
					int opcao = JOptionPane.showConfirmDialog(null, "Essa opção irá deletar a anotação.\n\n"
							+ "Você tem certeza?\n\n", "Deletar Anotação", JOptionPane.YES_NO_OPTION);

					if(opcao == JOptionPane.YES_OPTION)
					{ 
						try {
							Query pega = new Query();
							pega.executaUpdate("DELETE FROM gastos WHERE `id` = " + tabela.getValueAt(tabela.getSelectedRow(), 0));
							gastoTotal -= UtilCoffe.precoToDouble(tabela.getValueAt(tabela.getSelectedRow(), 4).toString());
							DiarioLog.add(Usuario.INSTANCE.getNome(), "Deletou a anotação " + tabela.getValueAt(tabela.getSelectedRow(), 2) 
									+ " de valor R$" + tabela.getValueAt(tabela.getSelectedRow(), 4) + ".", 10);	
							pega.fechaConexao();

							SwingUtilities.invokeLater(new Runnable() {  
								public void run() {  
									tabelaModel.removeRow(tabela.getSelectedRow());
								}  
							});

							NotificationManager.setLocation(2);
							NotificationManager.showNotification(tabela, "Anotação Deletada!", 
									new ImageIcon(getClass().getClassLoader().getResource("imgs/notifications_ok.png"))).setDisplayTime(2000);
							
							labelGasto.setText("Total: R$" + UtilCoffe.doubleToPreco(gastoTotal));
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
package codecoffe.restaurantes.graficos;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;
import codecoffe.restaurantes.mysql.Query;
import codecoffe.restaurantes.utilitarios.Usuario;
import codecoffe.restaurantes.utilitarios.UtilCoffe;

import com.alee.extended.date.WebDateField;
import com.alee.laf.button.WebButton;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.itextpdf.text.Anchor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class ConsultarDiario2 extends WebPanel implements ActionListener
{
	private static final long serialVersionUID = 1L;
	private static final int vendasPagina = 100;
	private JTable tabela;
	private DefaultTableModel tabelaModel;
	private WebDateField dataInicial, dataFinal;
	private WebButton pesquisar, exportarPDF;
	private JComboBox<String> paginacao;
	private String pesquisaFormat;
	private com.itextpdf.text.Font catFont;
	private com.itextpdf.text.Font paFont;
	private com.itextpdf.text.Font subFont;
	private Document document;
	private Paragraph escrever;
	private Timer t;
	private Query pega3;
	private int index, totalLinhas;
	private JProgressBar progressBar;
	private JFileChooser chooser;
	private String nomeArquivo, dataDia;
	private JLabel labelProgresso;

	public ConsultarDiario2()
	{
		catFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 18, com.itextpdf.text.Font.BOLD);
		paFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 16, com.itextpdf.text.Font.BOLD);
		subFont = new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.HELVETICA, 10, com.itextpdf.text.Font.NORMAL);	
		
		setLayout(new MigLayout("fill", "15[]15[]20[]15", "10[]10[]"));

		tabela = new JTable();
		tabelaModel = new DefaultTableModel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int column) {
				if(column == (tabela.getColumnCount()-1)) return true;
				return false;
			}
		};

		tabelaModel.addColumn("ID");
		tabelaModel.addColumn("Data");
		tabelaModel.addColumn("Atendente");
		tabelaModel.addColumn("Ação");
		tabelaModel.addColumn("Tipo");

		tabela.setModel(tabelaModel);
		tabela.setRowHeight(28);
		tabela.getTableHeader().setReorderingAllowed(false);
		tabela.setFocusable(false);
		tabela.setDefaultRenderer(Object.class, new TabelaVendasRenderer());

		tabela.getColumn("ID").setMinWidth(0);
		tabela.getColumn("ID").setMaxWidth(0);
		tabela.getColumn("Data").setPreferredWidth(140);
		tabela.getColumn("Atendente").setPreferredWidth(180);
		tabela.getColumn("Ação").setPreferredWidth(550);
		tabela.getColumn("Tipo").setMinWidth(0);
		tabela.getColumn("Tipo").setMaxWidth(0);

		dataInicial = new WebDateField(new Date());
		dataInicial.setHorizontalAlignment(SwingConstants.CENTER);
		dataInicial.setMinimumSize(new Dimension(130, 35));
		dataInicial.setEditable(false);

		dataFinal = new WebDateField(new Date());
		dataFinal.setHorizontalAlignment(SwingConstants.CENTER);
		dataFinal.setMinimumSize(new Dimension(130, 35));
		dataFinal.setEditable(false);

		pesquisar = new WebButton("Pesquisar");
		pesquisar.setRolloverShine(true);
		pesquisar.setPreferredSize(new Dimension(120, 35));
		pesquisar.setHorizontalTextPosition(AbstractButton.LEFT);
		pesquisar.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/pesquisa_mini.png")));
		pesquisar.addActionListener(this);

		exportarPDF = new WebButton();
		exportarPDF.setToolTipText("Exportar para PDF");
		exportarPDF.setRolloverDecoratedOnly(true);
		exportarPDF.setPreferredSize(new Dimension(32, 32));
		exportarPDF.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/export_pdf.png")));
		exportarPDF.addActionListener(this);

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
		add(exportarPDF, "align 100%, span");
		add(scrolltabela, "grow, pushy, span, wrap");
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
								pesquisarVendas(start, vendasPagina);
							}
						});
					}
				}
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if(e.getSource() == pesquisar)
		{
			SimpleDateFormat formataDataSQL = new SimpleDateFormat("yyyy-M-dd");	
			pesquisaFormat = "SELECT * FROM diario WHERE data BETWEEN ('" 
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
						pesquisarVendas(0, vendasPagina);
					} catch (ClassNotFoundException | SQLException e1) {
						e1.printStackTrace();
						new PainelErro(e1);
					}
				}
			});
		}
		else if(e.getSource() == exportarPDF)
		{
			exportarPDF();
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

	public void pesquisarVendas(int inicio, int fim)
	{
		tabelaModel.setNumRows(0);

		try {
			Query pega = new Query();
			pega.executaQuery(pesquisaFormat + inicio + ", " + fim);

			while(pega.next())
			{	
				Vector<String> linha = new Vector<String>();
				linha.add("" + pega.getInt("diario_id"));
				linha.add(pega.getString("horario"));
				linha.add(pega.getString("atendente"));
				linha.add(" " + pega.getString("acao"));
				linha.add("" + pega.getInt("tipo"));
				tabelaModel.addRow(linha);
			}

			pega.fechaConexao();

		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			new PainelErro(e);
		}
	}

	private class TabelaVendasRenderer extends DefaultTableCellRenderer
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Color alternate = new Color(206, 220, 249);

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, 
				boolean isSelected, boolean hasFocus, int row, int column) {
			JLabel cellComponent = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			if(isSelected) {
				cellComponent.setBackground(tabela.getSelectionBackground());
			}
			else if(row % 2 == 0) {
				cellComponent.setBackground(alternate);
			}
			else {
				cellComponent.setBackground(Color.WHITE);
			}
			
			if(column != 3)
				setHorizontalAlignment(JLabel.CENTER);
			else
				setHorizontalAlignment(JLabel.LEFT);
			
			return cellComponent;
		}
	}

	public void exportarPDF()
	{
		SimpleDateFormat formataDataSQL = new SimpleDateFormat("dd-M-yyyy");
		
		nomeArquivo = "/Diario_" + formataDataSQL.format(dataInicial.getDate()) + "__" + 
				formataDataSQL.format(dataFinal.getDate()) + ".pdf";

		chooser = new JFileChooser(); 
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle("Selecione a pasta para salvar");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);	    

		if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
		{
			document = new Document();
			try {
				PdfWriter.getInstance(document, new FileOutputStream(chooser.getCurrentDirectory() + "/" + chooser.getSelectedFile().getName() + nomeArquivo));
			} catch (FileNotFoundException | DocumentException e1) {
				e1.printStackTrace();
				new PainelErro(e1);
			}

			document.open();
			document.addTitle("Diário do Sistema " + dataInicial.getText() + " - " + dataFinal.getText());
			document.addSubject("diário");
			document.addKeywords("diário, codecoffe, sistema");
			document.addAuthor("CodeCoffe");
			document.addCreator("CodeCoffe");

			Paragraph preface = new Paragraph();
			addEmptyLine(preface, 1);
			preface.add(new Paragraph("Diário do Sistema " + dataInicial.getText() + " - " + dataFinal.getText(), catFont));
			addEmptyLine(preface, 1);
			preface.add(new Paragraph("Relatório gerado por: " + Usuario.INSTANCE.getNome() + ", " + new Date(), paFont));
			try {
				document.add(preface);
			} catch (DocumentException e1) {
				JOptionPane.showMessageDialog(null, "Ocorreu o seguine erro no sistema:\n" + e1.getMessage(), "Houve um erro ;(", JOptionPane.ERROR_MESSAGE);
			}
			
			document.newPage();
			pega3 = new Query();
			try {
				SimpleDateFormat formataDataSQL2 = new SimpleDateFormat("yyyy-M-dd");
				pega3.executaQuery("SELECT count(*) FROM diario WHERE data BETWEEN ('" 
					+ formataDataSQL2.format(dataInicial.getDate()) + "') " 
					+ "AND ('" + formataDataSQL2.format(dataFinal.getDate()) + "')");
				
			} catch (ClassNotFoundException | SQLException e1) {
				document.close();
				pega3.fechaConexao();
				e1.printStackTrace();
				new PainelErro(e1);
			}

			if(pega3.next())
				totalLinhas = pega3.getInt(1);

			JFrame salvando = new JFrame();
			salvando.setTitle("Exportando Diário para PDF");
			salvando.setSize(436, 186);
			salvando.setLayout(null);
			salvando.setLocationRelativeTo(null);
			salvando.setResizable(false);

			labelProgresso = new JLabel();
			labelProgresso.setFont(new Font("Helvetica", Font.BOLD, 16));
			labelProgresso.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/export_pdf.png")));
			labelProgresso.setHorizontalTextPosition(AbstractButton.LEFT);
			labelProgresso.setBounds(15, 10, 480, 40);				    	

			progressBar = new JProgressBar(0, totalLinhas);
			progressBar.setValue(0);
			progressBar.setStringPainted(true);
			progressBar.setBounds(15, 51, 400, 50); // Coluna, Linha, Largura, Altura

			JTextField campoSalvando = new JTextField();
			campoSalvando.setFont(new Font("Verdana", Font.PLAIN, 10));
			campoSalvando.setEditable(false);
			campoSalvando.setText(chooser.getCurrentDirectory() + "/" + chooser.getSelectedFile().getName() + nomeArquivo);
			campoSalvando.setBounds(15, 110, 400, 30);

			salvando.add(campoSalvando);
			salvando.add(labelProgresso);
			salvando.add(progressBar);
			salvando.setVisible(true);					

			try {
				SimpleDateFormat formataDataSQL2 = new SimpleDateFormat("yyyy-M-dd");
				pega3.executaQuery("SELECT * FROM diario WHERE data BETWEEN ('" 
					+ formataDataSQL2.format(dataInicial.getDate()) + "') " 
					+ "AND ('" + formataDataSQL2.format(dataFinal.getDate()) + "') ORDER BY data;");

			} catch (ClassNotFoundException | SQLException e1) {
				document.close();
				pega3.fechaConexao();
				e1.printStackTrace();
				new PainelErro(e1);
			}

			index = 1;
			dataDia = null;

			t = new Timer(30, new ActionListener()
			{
				public void actionPerformed(ActionEvent ae)
				{				    			 	
					if(pega3.next())
					{
						if(dataDia == null)
						{
							String[] splited = pega3.getString("horario").split("\\s+");
							dataDia = splited[0];
							Anchor anchor = new Anchor(" Dia: " + dataDia, catFont);
							anchor.setName(" Dia: " + dataDia);

							// Second parameter is the number of the chapter
							Chapter catPart = new Chapter(new Paragraph(anchor), 1);											

							try {
								document.add(catPart);
							} catch (DocumentException e) {
								document.close();
								new PainelErro(e);
							}
						}

						String[] splited = pega3.getString("horario").split("\\s+");

						if(!dataDia.equals(splited[0]))
						{
							dataDia = splited[0];
							Anchor anchor = new Anchor(" Dia: " + dataDia, catFont);
							anchor.setName(" Dia: " + dataDia);

							// Second parameter is the number of the chapter
							Chapter catPart = new Chapter(new Paragraph(anchor), 1);											

							try {
								document.add(catPart);
							} catch (DocumentException e) {
								document.close();
								new PainelErro(e);
							}
						}

						escrever = new Paragraph();
						
						SwingUtilities.invokeLater(new Runnable() {  
							public void run() {
								labelProgresso.setText("Exportando linha " + index + " de " + totalLinhas + ".");
								progressBar.setValue(index);	
							}
						});

						escrever.add(new Paragraph("(" + splited[2] + ") " + pega3.getString("atendente") + " - " + pega3.getString("acao"), subFont));									    

						try {
							document.add(escrever);
						} catch (DocumentException e) {
							document.close();
							new PainelErro(e);
						}

						index++;
					}
					else
					{
						pega3.fechaConexao();
						document.close();
						SwingUtilities.invokeLater(new Runnable() {  
							public void run() {
								labelProgresso.setText("Finalizado.");
							}
						});
						t.stop();
					}
				}
			});

			t.start();			    	
		}
	}
	
	private void addEmptyLine(Paragraph paragraph, int number) {
		for (int i = 0; i < number; i++) {
			paragraph.add(new Paragraph(" "));
		}
	}
}

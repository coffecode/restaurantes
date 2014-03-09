package codecoffe.restaurantes.utilitarios;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Graphics2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import com.alee.laf.progressbar.WebProgressBar;
import com.alee.laf.progressbar.WebProgressBarStyle;
import com.alee.utils.ThreadUtils;
import com.itextpdf.awt.DefaultFontMapper;

import java.awt.geom.Rectangle2D;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

import codecoffe.restaurantes.graficos.PainelErro;
import codecoffe.restaurantes.mysql.Query;

public class ExportarVendasPDF implements Runnable
{
	private JFrame salvando;
	private JLabel labelProgresso;
	private JTextField campoSalvando;
	private WebProgressBar progressBar;
	private JSystemFileChooser chooser;
	private Date inicio, fim;
	private int totalLinhas;
	private String pesquisa;
	
	public ExportarVendasPDF(Date dataInicial, Date dataFinal, JSystemFileChooser arquivo, String filtro)
	{	
		inicio = dataInicial;
		fim = dataFinal;
		chooser = arquivo;
		pesquisa = filtro;
		
		SimpleDateFormat formataDataSQL = new SimpleDateFormat("dd-M-yyyy");
		String nomeArquivo = "/Vendas_" + formataDataSQL.format(inicio) + "__" + 
				formataDataSQL.format(fim) + ".pdf";
		
		totalLinhas = 0;
		
    	try {
			Query pega3 = new Query();
			pega3.executaQuery(pesquisa);
			if(pega3.next())
				totalLinhas = pega3.getRowCount();
			
			salvando = new JFrame();
	    	salvando.setTitle("Exportando Vendas para PDF");
	    	salvando.setIconImage(new ImageIcon(getClass().getClassLoader().getResource("imgs/icone_programa.png")).getImage());
	    	salvando.setSize(436, 186);
	    	salvando.setLayout(null);
	    	salvando.setLocationRelativeTo(null);
	    	salvando.setResizable(false);
	    	salvando.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    	
	    	labelProgresso = new JLabel();
	    	labelProgresso.setFont(new java.awt.Font("Helvetica", java.awt.Font.BOLD, 16));
	    	labelProgresso.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/export_pdf.png")));
	    	labelProgresso.setHorizontalTextPosition(AbstractButton.LEFT);
	    	labelProgresso.setBounds(15, 10, 480, 40);				    	
	    	
	    	WebProgressBarStyle.progressTopColor = new Color(152, 10, 10);
	    	WebProgressBarStyle.progressBottomColor = new Color(186, 25, 25);
	    	
	    	progressBar = new WebProgressBar(0, totalLinhas);
	    	progressBar.setValue(0);
	    	progressBar.setStringPainted(true);
	    	progressBar.setBounds(15, 51, 400, 50); // Coluna, Linha, Largura, Altura
	    	
	    	campoSalvando = new JTextField();
	    	campoSalvando.setFont(new java.awt.Font("Verdana", java.awt.Font.PLAIN, 10));
	    	campoSalvando.setEditable(false);
	    	campoSalvando.setText(chooser.getCurrentDirectory() + "/" + arquivo.getSelectedFile().getName() + nomeArquivo);
	    	campoSalvando.setText(campoSalvando.getText().replaceAll("\\\\", "/"));
	    	campoSalvando.setBounds(15, 110, 400, 30);
	    	
	    	salvando.add(campoSalvando);
	    	salvando.add(labelProgresso);
	    	salvando.add(progressBar);
	    	salvando.setVisible(true);
		} catch (ClassNotFoundException | SQLException e1) {
			e1.printStackTrace();
			new PainelErro(e1);
		}
	}

	@Override
	public void run() 
	{
		labelProgresso.setText("Inicializando exportação...");
		ThreadUtils.sleepSafely(500);
		salvando.setEnabled(false);
		
		SimpleDateFormat formataDataSQLConsulta = new SimpleDateFormat("yyyy-M-dd");
		SimpleDateFormat formataDataSQL = new SimpleDateFormat("dd-M-yyyy");
		String nomeArquivo = "/Vendas_" + formataDataSQL.format(inicio) + "__" + 
				formataDataSQL.format(fim) + ".pdf";
		
		Font catFont 		= new Font(FontFamily.TIMES_ROMAN, 18, Font.BOLD, BaseColor.BLUE);
		Font catFont2 		= new Font(FontFamily.TIMES_ROMAN, 18, Font.BOLD);
		Font paFont 		= new Font(FontFamily.TIMES_ROMAN, 16, Font.BOLD);
		Font vendaFontRed 	= new Font(FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.RED);
		Font subFont 		= new Font(FontFamily.HELVETICA, 9, Font.ITALIC);
		Font vendaFontFiado = new Font(FontFamily.HELVETICA, 9, Font.NORMAL);
		
		
		Font fontCategoria 	= new Font(FontFamily.HELVETICA, 16, Font.BOLD);
		Font fontVenda 		= new Font(FontFamily.HELVETICA, 10, Font.NORMAL);
		Font fontVendaRed 		= new Font(FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.RED);
		Font fontVendaBold = new Font(FontFamily.HELVETICA, 10, Font.BOLD);
		Font fontVendaBold12 = new Font(FontFamily.HELVETICA, 12, Font.BOLD);
		Font fontTexto = new Font(FontFamily.HELVETICA, 12, Font.NORMAL);
		Font fontTextoBold = new Font(FontFamily.HELVETICA, 12, Font.BOLD);
		Font fontTextoBoldRed = new Font(FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.RED);
		Font fontTextoBoldBlue = new Font(FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.BLUE);
		
		Document document = new Document();
		double totalPeriodo = 0.0;
		double totalPeriodoSemBonus = 0.0;
		double totalDezPorcento = 0.0;
		int vendas_fiadas = 0;
		int vendas_delivery = 0;
		int vendas_dezporcento = 0;
		
		try {
			/*
			 * COLETANDO INFORMAÇÕES
			 */
		
			List<FuncionarioRelatorio> funcionarios = new ArrayList<FuncionarioRelatorio>();
			
			PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(chooser.getCurrentDirectory() + "/" + chooser.getSelectedFile().getName() + nomeArquivo));
			Query pega = new Query();
			pega.executaQuery(pesquisa);
			
			while(pega.next())
			{
				boolean flag_funcionario = false;
				
				if(funcionarios.size() > 0) {
					for(int i = 0; i < funcionarios.size(); i++) {
						if(funcionarios.get(i).getNome().equals(pega.getString("atendente"))) {
							funcionarios.get(i).setTotalVendas((funcionarios.get(i).getTotalVendas()+1));
							funcionarios.get(i).setTotalDezPorcento(funcionarios.get(i).getTotalDezPorcento() 
									+ UtilCoffe.precoToDouble(pega.getString("dezporcento")));
							flag_funcionario = true;
							break;
						}
					}
				}
				
				if(!flag_funcionario) {
					FuncionarioRelatorio fun = new FuncionarioRelatorio(pega.getString("atendente"));
					fun.setTotalVendas(1);
					fun.setTotalDezPorcento(UtilCoffe.precoToDouble(pega.getString("dezporcento")));
					funcionarios.add(fun);
				}
				
				if(!pega.getString("delivery").equals("0,00"))
					vendas_delivery++;
				
				if(!pega.getString("dezporcento").equals("0,00"))
					vendas_dezporcento++;
				
				if(pega.getString("forma_pagamento").equals("Fiado"))
					vendas_fiadas++;
				
				totalPeriodo += UtilCoffe.precoToDouble(pega.getString("total"));
				double conta = UtilCoffe.precoToDouble(pega.getString("total"));
				conta -= UtilCoffe.precoToDouble(pega.getString("delivery"));
				conta -= UtilCoffe.precoToDouble(pega.getString("dezporcento"));
				totalPeriodoSemBonus += (conta);
				totalDezPorcento += UtilCoffe.precoToDouble(pega.getString("dezporcento"));
			}
			
			/*
			 * COMEÇANDO A ESCREVER O RELATÓRIO
			 */
			
			SimpleDateFormat formataDataRelatorio = new SimpleDateFormat("dd/M/yyyy");
			
			document.open();
			document.addTitle("Relatório de Vendas (" + formataDataRelatorio.format(inicio) + " até " + formataDataRelatorio.format(fim) + ")");
		    document.addSubject("Relatório de Vendas");
		    document.addKeywords("diário, codecoffe, sistema, relatório");
		    document.addAuthor("CodeCoffe");
		    document.addCreator("CodeCoffe");
		    
		    formataDataRelatorio = new SimpleDateFormat("dd/M/yyyy - HH:mm");
		    Paragraph preface = new Paragraph();
		    
		    Image image1 = Image.getInstance(getClass().getClassLoader().getResource("imgs/logo.png"));
		    image1.setAlignment(Element.ALIGN_CENTER);
	        document.add(image1);
		    
		    addEmptyLine(preface, 1);
		    preface.add(new Paragraph("Relatório de Vendas (" + formataDataRelatorio.format(inicio) + " até " + formataDataRelatorio.format(fim) + ")", catFont2));
		    addEmptyLine(preface, 1);
		    preface.add(new Paragraph("Relatório gerado por: " + Usuario.INSTANCE.getNome() + " em " + formataDataRelatorio.format(new Date()), paFont));
		    addEmptyLine(preface, 14);
		    preface.add(new Paragraph("CodeCoffe Restaurantes " + UtilCoffe.VERSAO, fontTextoBold));
		    preface.add(new Paragraph("www.codecoffe.com.br - contato@codecoffe.com.br", fontTexto));
		    document.add(preface);
		    document.newPage();
		    Paragraph escrever = null;
		    
			/*
			 * INFORMAÇÕES EXTRAS
			 */
		    
		    escrever = new Paragraph();
		    escrever.add(new Paragraph("DADOS", fontCategoria));
		    addEmptyLine(escrever, 1);
		    document.add(escrever);
		    
		    List<ProdutosRelatorio> produtos = new ArrayList<ProdutosRelatorio>();
		    
		    pega.executaQuery("SELECT nome, preco FROM produtos_new");
		    
		    while(pega.next())
		    {
		    	produtos.add(new ProdutosRelatorio(pega.getString("nome"), UtilCoffe.precoToDouble(pega.getString("preco"))));
		    }
		    
		    for(int i = 0; i < produtos.size(); i++)
		    {
				String pesquisaProdutos = "SELECT * FROM vendas_produtos WHERE data BETWEEN ('" 
						+ formataDataSQLConsulta.format(inicio) + "') " 
						+ "AND ('" + formataDataSQLConsulta.format(fim) + "') AND nome_produto = '" + produtos.get(i).getNome() 
						+ "' OR adicionais_produto LIKE '%" + produtos.get(i).getNome() + "%'";
				
				pega.executaQuery(pesquisaProdutos);
				int qntd = 0;
				
				while(pega.next())
				{
					if(!UtilCoffe.vaziu(pega.getString("nome_produto")))
					{
						if(pega.getString("nome_produto").equals(produtos.get(i).getNome()))
						{
							qntd += pega.getInt("quantidade_produto");
						}
					}
					
					if(!UtilCoffe.vaziu(pega.getString("adicionais_produto")))
					{
						if(pega.getString("adicionais_produto").contains(produtos.get(i).getNome()))
						{
							int pos = -1;  
							int contagem = 0;  
							while (true) {  
							    pos = pega.getString("adicionais_produto").indexOf(produtos.get(i).getNome(), pos + 1);   
							    if (pos < 0) break;  
							    contagem++;  
							}
							
							qntd += (pega.getInt("quantidade_produto") * contagem);
						}
					}
				}
				
				produtos.get(i).setTotalVendas(qntd);
		    }

		    Collections.sort(produtos, new Comparator<ProdutosRelatorio>()  {
				@Override
				public int compare(ProdutosRelatorio o1, ProdutosRelatorio o2) {
					return (o1.getTotalVendas() - o2.getTotalVendas())*-1;
				}
		    });
		    
			escrever = new Paragraph();
			PdfPTable table = new PdfPTable(3);
			table.setWidthPercentage(100);
			table.setSpacingBefore(20f);
			table.setSpacingAfter(10f); 

		    PdfPCell c1 = new PdfPCell(new Paragraph("Produto", fontVendaBold));
		    c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		    c1.setPadding(5);
		    table.addCell(c1);
		    
		    c1 = new PdfPCell(new Paragraph("Vendas", fontVendaBold));
		    c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		    c1.setPadding(5);
		    table.addCell(c1);
		    
		    c1 = new PdfPCell(new Paragraph("Total", fontVendaBold));
		    c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		    c1.setPadding(5);
		    table.addCell(c1);
		    
		    for(int i = 0; i < produtos.size(); i++)
		    {
		    	c1 = new PdfPCell(new Paragraph(produtos.get(i).getNome(), fontVenda));
		    	c1.setPadding(5);
		    	c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			    table.addCell(c1);
		    	
		    	c1 = new PdfPCell(new Paragraph("" + produtos.get(i).getTotalVendas(), fontVenda));
		    	c1.setPadding(5);
		    	c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			    table.addCell(c1);
		    	
		    	c1 = new PdfPCell(new Paragraph(UtilCoffe.doubleToPreco(produtos.get(i).getTotalVendas() * produtos.get(i).getValor()), fontVenda));
		    	c1.setPadding(5);
		    	c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			    table.addCell(c1);
		    }
		    
		    escrever.add(table);
		    document.add(escrever);
		    
		    escrever = new Paragraph();
		    addEmptyLine(escrever, 2);
		    
		    com.itextpdf.text.List overview = new com.itextpdf.text.List(false, 10);
		    overview.add(new ListItem(totalLinhas + " vendas no total."));
		    overview.add(vendas_fiadas + " (" + ((100*vendas_fiadas)/totalLinhas) + "%) vendas fiadas.");
		    overview.add(vendas_dezporcento + " (" + ((100*vendas_dezporcento)/totalLinhas) + "%) vendas com 10% opcional.");
		    overview.add(vendas_delivery + " (" + ((100*vendas_delivery)/totalLinhas) + "%) vendas de delivery.");
		    
		    escrever.add(overview);
		    document.add(escrever);
		    document.newPage();
		    
			/*
			 * FUNCIONÁRIOS
			 */
		    
		    escrever = new Paragraph();
		    escrever.add(new Paragraph("FUNCIONÁRIOS", fontCategoria));
		    addEmptyLine(escrever, 1);
		    document.add(escrever);
		    
			escrever = new Paragraph();
			table = new PdfPTable(3);
			table.setWidthPercentage(100);
			table.setSpacingBefore(20f);
			table.setSpacingAfter(10f); 

		    c1 = new PdfPCell(new Paragraph("Nome", fontVendaBold));
		    c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		    c1.setPadding(5);
		    table.addCell(c1);

		    c1 = new PdfPCell(new Paragraph("Vendas", fontVendaBold));
		    c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		    c1.setPadding(5);
		    table.addCell(c1);
		    
		    c1 = new PdfPCell(new Paragraph("10% Arrecadado R$", fontVendaBold));
		    c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		    c1.setPadding(5);
		    table.addCell(c1);
		    
		    DefaultPieDataset dataSet = new DefaultPieDataset();
		    
		    for(int i = 0; i < funcionarios.size(); i++)
		    {
		    	c1 = new PdfPCell(new Paragraph(funcionarios.get(i).getNome(), fontVenda));
		    	c1.setPadding(5);
		    	c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			    table.addCell(c1);			    
			    
		    	c1 = new PdfPCell(new Paragraph(funcionarios.get(i).getTotalVendas() + " (" + ((100*funcionarios.get(i).getTotalVendas())/totalLinhas) + "%)", fontVenda));
		    	c1.setPadding(5);
		    	c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			    table.addCell(c1);
			    
		    	c1 = new PdfPCell(new Paragraph(UtilCoffe.doubleToPreco(funcionarios.get(i).getTotalDezPorcento()), fontVenda));
		    	c1.setPadding(5);
		    	c1.setHorizontalAlignment(Element.ALIGN_CENTER);
			    table.addCell(c1);
			    
			    dataSet.setValue(funcionarios.get(i).getNome(), ((double) (100*funcionarios.get(i).getTotalVendas())/(double) totalLinhas));
		    }
		    
		    escrever.add(table);
		    addEmptyLine(escrever, 1);
		    document.add(escrever);
	 
	        JFreeChart chart = ChartFactory.createPieChart("Vendas por Funcionários", dataSet, true, true, true);
	        
	        PdfContentByte contentByte = writer.getDirectContent();
	        PdfTemplate template = contentByte.createTemplate(500, 380);
		    
	        @SuppressWarnings("deprecation")
			Graphics2D graphics2d = template.createGraphics(500, 380, new DefaultFontMapper());
	        java.awt.geom.Rectangle2D rectangle2d = new Rectangle2D.Double(0, 0, 500, 380);
	        chart.draw(graphics2d, rectangle2d);
	        graphics2d.dispose();
	        
	        Image chartImage = Image.getInstance(template);
	        chartImage.setAlignment(Element.ALIGN_CENTER);
	        document.add(chartImage);
		    
		    escrever = new Paragraph();
		    addEmptyLine(escrever, 2);
		    escrever.add(new Paragraph("Total em 10% Opcional: R$" + UtilCoffe.doubleToPreco(totalDezPorcento), fontTextoBold));
		    document.add(escrever);
		    document.newPage();
		    
			/*
			 * GASTOS
			 */
		    
		    int index = 1;
		    
		    escrever = new Paragraph();
		    escrever.add(new Paragraph("ANOTAÇÕES / LUCRO", fontCategoria));
		    addEmptyLine(escrever, 1);
		    document.add(escrever);
		    
			escrever = new Paragraph();
			table = new PdfPTable(4);
			table.setWidthPercentage(100);
			table.setSpacingBefore(20f);
			table.setSpacingAfter(10f); 

		    c1 = new PdfPCell(new Paragraph("Data", fontVendaBold));
		    c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		    c1.setPadding(5);
		    table.addCell(c1);

		    c1 = new PdfPCell(new Paragraph("Nome", fontVendaBold));
		    c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		    c1.setPadding(5);
		    table.addCell(c1);
		    
		    c1 = new PdfPCell(new Paragraph("Descrição", fontVendaBold));
		    c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		    c1.setPadding(5);
		    table.addCell(c1);
		    
		    c1 = new PdfPCell(new Paragraph("Valor R$", fontVendaBold));
		    c1.setHorizontalAlignment(Element.ALIGN_CENTER);
		    c1.setPadding(5);
		    table.addCell(c1);
		    	
			String pesquisaFormat = "SELECT * FROM gastos WHERE data BETWEEN ('" 
					+ formataDataSQLConsulta.format(inicio) + "') " 
					+ "AND ('" + formataDataSQLConsulta.format(fim) + "')";
		    
		    pega.executaQuery(pesquisaFormat);
		    int linhasGasto = pega.getRowCount();
		    
			SimpleDateFormat formataDataSQLL = new SimpleDateFormat("yyyy-M-dd");
			SimpleDateFormat formataDataTabela = new SimpleDateFormat("dd/MM/yyyy");
			
			double totalGastos = 0.0;
		    
		    while(pega.next())
		    {
		    	ThreadUtils.sleepSafely(150);
		    	labelProgresso.setText("Exportando anotação " + index + " de " + linhasGasto + ".");
		    	progressBar.setValue(index);
		    	
		    	totalGastos += UtilCoffe.precoToDouble(pega.getString("valor"));
		    	
				try {
					Date dia = formataDataSQLL.parse(pega.getString("data"));
					 
				    PdfPCell cc1 = new PdfPCell(new Paragraph(formataDataTabela.format(dia), fontVenda));
				    cc1.setPadding(5);
				    cc1.setHorizontalAlignment(Element.ALIGN_CENTER);
				    table.addCell(cc1);
				} catch (ParseException e) {
					e.printStackTrace();
				    PdfPCell cc1 = new PdfPCell(new Paragraph("-", fontVenda));
				    cc1.setPadding(5);
				    cc1.setHorizontalAlignment(Element.ALIGN_CENTER);
				    table.addCell(cc1);
				}
				
			    PdfPCell cc2 = new PdfPCell(new Paragraph(pega.getString("nome"), fontVenda));
			    cc2.setPadding(5);
			    cc2.setHorizontalAlignment(Element.ALIGN_CENTER);
			    table.addCell(cc2);
			    
			    PdfPCell cc3 = new PdfPCell(new Paragraph(pega.getString("descricao"), fontVenda));
			    cc3.setPadding(5);
			    cc3.setHorizontalAlignment(Element.ALIGN_CENTER);
			    table.addCell(cc3);
			    
			    PdfPCell cc4 = null;
			    if(UtilCoffe.precoToDouble(pega.getString("valor")) < 0)
			    {
			    	cc4 = new PdfPCell(new Paragraph(pega.getString("valor"), fontVendaRed));
			    }
			    else
			    {
			    	cc4 = new PdfPCell(new Paragraph(pega.getString("valor"), fontVenda));
			    }
			    cc4.setPadding(5);
			    cc4.setHorizontalAlignment(Element.ALIGN_CENTER);
			    table.addCell(cc4);
		    	
		    	index++;
		    }
		    
	        float[] columnWidths = new float[] {15f, 25f, 45f, 15f};
            table.setWidths(columnWidths);
		    
		    escrever.add(table);
		    document.add(escrever);
		    
		    escrever = new Paragraph();
		    
		    if(totalGastos < 0)
		    	escrever.add(new Paragraph("Total Anotações: R$" + UtilCoffe.doubleToPreco(totalGastos), fontTextoBoldRed));
		    else
		    	escrever.add(new Paragraph("Total Anotações: R$" + UtilCoffe.doubleToPreco(totalGastos), fontTextoBoldBlue));
		    
		    addEmptyLine(escrever, 1);
		    
		    escrever.add(new Paragraph("Total em Vendas: R$" + UtilCoffe.doubleToPreco(totalPeriodo) + " (c/ 10% opcional)", fontTexto));
		    
		    if(totalPeriodo-totalGastos < 0)
		    	escrever.add(new Paragraph("Lucro: R$" + UtilCoffe.doubleToPreco(totalPeriodo+totalGastos), fontTextoBoldRed));
		    else
		    	escrever.add(new Paragraph("Lucro: R$" + UtilCoffe.doubleToPreco(totalPeriodo+totalGastos), fontTextoBoldBlue));
		    
		    addEmptyLine(escrever, 1);
		    
		    escrever.add(new Paragraph("Total em Vendas: R$" + UtilCoffe.doubleToPreco(totalPeriodoSemBonus) + " (s/ 10% opcional)", fontTexto));
		    
		    if(totalPeriodoSemBonus-totalGastos < 0)
		    	escrever.add(new Paragraph("Lucro: R$" + UtilCoffe.doubleToPreco(totalPeriodoSemBonus+totalGastos), fontTextoBoldRed));
		    else
		    	escrever.add(new Paragraph("Lucro: R$" + UtilCoffe.doubleToPreco(totalPeriodoSemBonus+totalGastos), fontTextoBoldBlue));
		    
		    document.add(escrever);
		    document.newPage();
		    
			/*
			 * LISTA VENDAS
			 */
		    
		    escrever = new Paragraph();
		    escrever.add(new Paragraph("VENDAS", fontCategoria));
		    addEmptyLine(escrever, 1);
		    document.add(escrever);
		    
		    pega.executaQuery(pesquisa);
		    index = 1;
		    String dataDia = null;
		    
		    while(pega.next())
		    {
		    	ThreadUtils.sleepSafely(30);
		    	labelProgresso.setText("Exportando venda " + index + " de " + totalLinhas + ".");
		    	progressBar.setValue(index);
		    	
				if(dataDia == null)
				{							
					String[] splited = pega.getString("horario").split("\\s+");
					dataDia = splited[0];
					escrever = new Paragraph();
					addEmptyLine(escrever, 1);
					escrever.add(new Paragraph(dataDia + " - " + getDiaSemana(pega.getInt("dia_semana")), catFont));
					document.add(escrever);
				}
				
				String[] splited = pega.getString("horario").split("\\s+");
				
				if(!dataDia.equals(splited[0]))
				{
					dataDia = splited[0];
					escrever = new Paragraph();
					addEmptyLine(escrever, 1);
					escrever.add(new Paragraph(dataDia + " - " + getDiaSemana(pega.getInt("dia_semana")), catFont));
					document.add(escrever);
				}
				
				escrever = new Paragraph();
				
				if(UtilCoffe.precoToDouble(pega.getString("total")) > UtilCoffe.precoToDouble(pega.getString("valor_pago")))
				{
					if(pega.getString("forma_pagamento").equals("Fiado"))
					{
						escrever.add(new Paragraph(("(" + splited[2] + ") " + pega.getString("atendente") + " - " 
								+ pega.getString("forma_pagamento") + " - Total: R$" + pega.getString("total") 
								+ " - " + "Valor Pago: R$" + pega.getString("valor_pago")).replaceAll("\\s+", "  "), vendaFontRed));
					}
					else
					{
						escrever.add(new Paragraph(("(" + splited[2] + ") " + pega.getString("atendente") + " - " 
								+ pega.getString("forma_pagamento") + " - Total: R$" + pega.getString("total") 
								+ " - " + "Valor Pago: R$" + pega.getString("total")).replaceAll("\\s+", "  "), fontVendaBold12));
					}
				}
				else
				{
					escrever.add(new Paragraph(("(" + splited[2] + ") " + pega.getString("atendente") + " - " + pega.getString("forma_pagamento") 
							+ " - Total: R$" + pega.getString("total") + " - " + "Valor Pago: R$" 
							+ pega.getString("valor_pago")).replaceAll("\\s+", "  "), fontVendaBold12));
				}
				
				if(pega.getString("forma_pagamento").equals("Fiado"))
				{
					Query pega4 = new Query();
					pega4.executaQuery("SELECT nome, apelido, telefone, cpf FROM fiados WHERE `fiador_id` = " + pega.getInt("fiado_id"));
					
					if(pega4.next())
					{
						escrever.add(new Paragraph(("Fiado para: " + pega4.getString("nome") + " (" + pega4.getString("apelido") + ") - TEL: " 
									+ pega4.getString("telefone") + " - CPF: " + pega4.getString("cpf")).replaceAll("\\s+", "  "), vendaFontFiado));
					}
					
					pega4.fechaConexao();
				}
				
				if(UtilCoffe.precoToDouble(pega.getString("delivery")) > 0)
				{
					Query pega4 = new Query();
					pega4.executaQuery("SELECT nome, cpf FROM fiados WHERE `fiador_id` = " + pega.getInt("fiado_id"));
					
					if(pega4.next())
					{
						escrever.add(new Paragraph(("Delivery para: " + pega4.getString("nome") + " - CPF: " 
											+ pega4.getString("cpf")).replaceAll("\\s+", "  "), vendaFontFiado));
					}
					
					pega4.fechaConexao();
				}
				
				Query pega2 = new Query();
				pega2.executaQuery("SELECT * FROM vendas_produtos WHERE `id_link` = " + pega.getInt("vendas_id"));
				
				while(pega2.next())
				{	
					if(!"".equals(pega2.getString("adicionais_produto").trim()))
						escrever.add(new Paragraph(("   " + pega2.getInt("quantidade_produto") + "x...... " + pega2.getString("nome_produto") + " com " 
								+ pega2.getString("adicionais_produto") + " [R$" +  pega2.getString("preco_produto") + "]").replaceAll("\\s+", "  "), subFont));
					else
						escrever.add(new Paragraph(("   " + pega2.getInt("quantidade_produto") + "x...... " + pega2.getString("nome_produto") 
															+ " [R$" +  pega2.getString("preco_produto") + "]").replaceAll("\\s+", "  "), subFont));
				}
				
				pega2.fechaConexao();
				
				addEmptyLine(escrever, 1);
				document.add(escrever);
				
		    	index++;
		    }
			
		    pega.fechaConexao();
			document.close();
			labelProgresso.setText("Finalizado.");
			salvando.setEnabled(true);
			Desktop dt = Desktop.getDesktop();
			dt.open(new File(chooser.getCurrentDirectory() + "/" + chooser.getSelectedFile().getName() + nomeArquivo));
		} catch (DocumentException | ClassNotFoundException | SQLException | IOException e) {
			e.printStackTrace();
			salvando.setEnabled(true);
			document.close();
			new PainelErro(e);
		}
	}
	
	private class ProdutosRelatorio
	{
		private String nome;
		private int totalVendas;
		private double valor;
		
		public ProdutosRelatorio(String nome, double vl) {
			this.nome = nome;
			this.valor = vl;
			this.totalVendas = 0;
		}
		
		public double getValor() {
			return valor;
		}

		public String getNome() {
			return nome;
		}

		public int getTotalVendas() {
			return totalVendas;
		}
		public void setTotalVendas(int totalVendas) {
			this.totalVendas = totalVendas;
		}
	}
	
	private class FuncionarioRelatorio
	{
		private String nome;
		private double totalDezPorcento;
		private int totalVendas;
		
		public FuncionarioRelatorio(String string) {
			this.nome = string;
		}
		
		public String getNome() {
			return nome;
		}

		public double getTotalDezPorcento() {
			return totalDezPorcento;
		}
		public void setTotalDezPorcento(double totalDezPorcento) {
			this.totalDezPorcento = totalDezPorcento;
		}
		public int getTotalVendas() {
			return totalVendas;
		}
		public void setTotalVendas(int totalVendas) {
			this.totalVendas = totalVendas;
		}
	}
	
	private static void addEmptyLine(Paragraph paragraph, int number) {
		for (int i = 0; i < number; i++) {
			paragraph.add(new Paragraph(" "));
		}
	}
	
	private String getDiaSemana(int dia)
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
}
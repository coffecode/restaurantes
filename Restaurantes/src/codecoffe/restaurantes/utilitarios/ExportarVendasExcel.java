package codecoffe.restaurantes.utilitarios;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;

import com.alee.laf.progressbar.WebProgressBar;
import com.alee.laf.progressbar.WebProgressBarStyle;
import com.alee.utils.ThreadUtils;

import codecoffe.restaurantes.graficos.PainelErro;
import codecoffe.restaurantes.mysql.Query;

public class ExportarVendasExcel implements Runnable
{
	private JFrame salvando;
	private JLabel labelProgresso;
	private JTextField campoSalvando;
	private WebProgressBar progressBar;
	private JSystemFileChooser chooser;
	private Date inicio, fim;
	private int totalLinhas;
	private String pesquisa;
	
	public ExportarVendasExcel(Date dataInicial, Date dataFinal, JSystemFileChooser arquivo, String filtro)
	{	
		inicio = dataInicial;
		fim = dataFinal;
		chooser = arquivo;
		pesquisa = filtro;
		
		SimpleDateFormat formataDataSQL = new SimpleDateFormat("dd-M-yyyy");
		String nomeArquivo = "/Vendas_" + formataDataSQL.format(inicio) + "__" + 
				formataDataSQL.format(fim) + ".xls";
		
		totalLinhas = 0;
		
    	try {
			Query pega3 = new Query();
			pega3.executaQuery(pesquisa);
			if(pega3.next())
				totalLinhas = pega3.getRowCount();
			
			salvando = new JFrame();
	    	salvando.setTitle("Exportando Vendas para Excel");
	    	salvando.setIconImage(new ImageIcon(getClass().getClassLoader().getResource("imgs/icone_programa.png")).getImage());
	    	salvando.setSize(436, 186);
	    	salvando.setLayout(null);
	    	salvando.setLocationRelativeTo(null);
	    	salvando.setResizable(false);
	    	salvando.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    	
	    	labelProgresso = new JLabel();
	    	labelProgresso.setFont(new Font("Helvetica", Font.BOLD, 16));
	    	labelProgresso.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/export_excel.png")));
	    	labelProgresso.setHorizontalTextPosition(AbstractButton.LEFT);
	    	labelProgresso.setBounds(15, 10, 480, 40);				    	
	    	
	    	WebProgressBarStyle.progressTopColor = new Color(6, 152, 47);
	    	WebProgressBarStyle.progressBottomColor = new Color(40, 178, 79);
	    	
	    	progressBar = new WebProgressBar(0, totalLinhas);
	    	progressBar.setValue(0);
	    	progressBar.setStringPainted(true);
	    	progressBar.setBounds(15, 51, 400, 50); // Coluna, Linha, Largura, Altura
	    	
	    	campoSalvando = new JTextField();
	    	campoSalvando.setFont(new Font("Verdana", Font.PLAIN, 10));
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
		ThreadUtils.sleepSafely(500);
		
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("Tabela de Vendas");				
		HSSFRow rowhead = sheet.createRow(0);
		
		CellStyle cs = wb.createCellStyle();
		CellStyle cs2 = wb.createCellStyle();
		CellStyle cs3 = wb.createCellStyle();
		org.apache.poi.ss.usermodel.Font f = wb.createFont();
		org.apache.poi.ss.usermodel.Font f2 = wb.createFont();
		org.apache.poi.ss.usermodel.Font f3 = wb.createFont();
		cs.setAlignment(CellStyle.ALIGN_CENTER);
		cs2.setAlignment(CellStyle.ALIGN_CENTER);
		cs3.setAlignment(CellStyle.ALIGN_CENTER);
		
		f.setColor(HSSFColor.BLACK.index);
		f2.setColor(HSSFColor.BLUE.index);
		f3.setColor(HSSFColor.RED.index);
		
		f.setBoldweight(org.apache.poi.ss.usermodel.Font.BOLDWEIGHT_BOLD);
		f2.setBoldweight(org.apache.poi.ss.usermodel.Font.BOLDWEIGHT_BOLD);
		f3.setBoldweight(org.apache.poi.ss.usermodel.Font.BOLDWEIGHT_BOLD);
		
		cs.setFont(f);
		cs2.setFont(f2);
		cs3.setFont(f3);	
		
		sheet.setDefaultColumnStyle(9,cs2);
		sheet.setDefaultColumnStyle(10,cs3);
		sheet.setDefaultColumnStyle(11,cs);
		sheet.setDefaultColumnStyle(12,cs);	
		sheet.setDefaultColumnStyle(0,cs);
		
		rowhead.createCell(0).setCellValue("VENDA ID");
		rowhead.createCell(1).setCellValue("LOCAL");
		rowhead.createCell(2).setCellValue("DATA");
		rowhead.createCell(3).setCellValue("DIA SEMANA");
		rowhead.createCell(4).setCellValue("ATENDENTE");
		rowhead.createCell(5).setCellValue("10% OPCIONAL");
		rowhead.createCell(6).setCellValue("DELIVERY");
		rowhead.createCell(7).setCellValue("CLIENTE");
		rowhead.createCell(8).setCellValue("FORMA PAGAMENTO");
		rowhead.createCell(9).setCellValue("TOTAL R$");
		rowhead.createCell(10).setCellValue("TOTAL2 R$");
		rowhead.createCell(11).setCellValue("VALOR PAGO R$");
		rowhead.createCell(12).setCellValue("TROCO R$");
		
		int index = 1;
	 	double totalTudo = 0.0;
	 	double totalTudoSemBonus = 0.0;
	 	double totalPago = 0.0;
	 	double totalTroco = 0.0;
	 	double gastos = 0.0;
	 	
	 	try {
	 		SimpleDateFormat formataDataSQL = new SimpleDateFormat("yyyy-M-dd");
			String pesquisaFormat = "SELECT valor FROM gastos WHERE data BETWEEN ('" 
					+ formataDataSQL.format(inicio) + "') " 
					+ "AND ('" + formataDataSQL.format(fim) + "')";
	 		
			Query pega2 = new Query();
			pega2.executaQuery(pesquisaFormat);
			
			while(pega2.next()) {
				gastos += UtilCoffe.precoToDouble(pega2.getString("valor"));
			}
			
			pega2.fechaConexao();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			new PainelErro(e);
		}
		
    	try {
			Query pega3 = new Query();
			pega3.executaQuery(pesquisa);
			
			while(pega3.next())
			{
				ThreadUtils.sleepSafely(40);
				
				salvando.setEnabled(false);
				labelProgresso.setText("Exportando venda " + index + " de " + totalLinhas + ".");
				progressBar.setValue(index);					
				
				HSSFRow row = sheet.createRow(index);
				row.createCell(0).setCellValue(pega3.getInt("vendas_id"));
				
				if(pega3.getInt("caixa") == 0)
					row.createCell(1).setCellValue("Balcão");
				else
					row.createCell(1).setCellValue("Mesa " + pega3.getInt("caixa"));
				
				row.createCell(2).setCellValue(pega3.getString("horario"));
				
				switch(pega3.getInt("dia_semana"))
				{
					case 1:
					{
						row.createCell(3).setCellValue("Domingo");
						break;
					}
					case 2:
					{
						row.createCell(3).setCellValue("Segunda-feira");
						break;
					}
					case 3:
					{
						row.createCell(3).setCellValue("Terça-feira");
						break;
					}
					case 4:
					{
						row.createCell(3).setCellValue("Quarta-feira");
						break;
					}	
					case 5:
					{
						row.createCell(3).setCellValue("Quinta-feira");
						break;
					}
					case 6:
					{
						row.createCell(3).setCellValue("Sexta-feira");
						break;
					}
					case 7:
					{
						row.createCell(3).setCellValue("Sábado");
					}										
				}
				
				row.createCell(4).setCellValue(pega3.getString("atendente"));
				row.createCell(5).setCellValue(pega3.getString("dezporcento").replaceAll(",", "."));
				row.createCell(6).setCellValue(pega3.getString("delivery").replaceAll(",", "."));
				
				if(pega3.getInt("fiado_id") > 0)
				{
					Query pega4 = new Query();
					pega4.executaQuery("SELECT nome FROM fiados WHERE fiador_id = " + pega3.getInt("fiado_id"));
					
					if(pega4.next())
						row.createCell(7).setCellValue(pega4.getString("nome"));
					else
						row.createCell(7).setCellValue("Deletado");
					
					pega4.fechaConexao();
				}
				else
				{
					row.createCell(7).setCellValue("-");
				}
				
				row.createCell(8).setCellValue(pega3.getString("forma_pagamento"));
				row.createCell(9).setCellValue(pega3.getString("total").replaceAll(",", "."));
				totalTudo += UtilCoffe.precoToDouble(pega3.getString("total"));
				
				double conta = UtilCoffe.precoToDouble(pega3.getString("total"));
				conta -= UtilCoffe.precoToDouble(pega3.getString("dezporcento"));
				conta -= UtilCoffe.precoToDouble(pega3.getString("delivery"));
				row.createCell(10).setCellValue((UtilCoffe.doubleToPreco(conta).replaceAll(",", ".")));				
				totalTudoSemBonus += conta;
				
				if(UtilCoffe.precoToDouble(pega3.getString("total")) > UtilCoffe.precoToDouble(pega3.getString("valor_pago")))
				{
					if(pega3.getString("forma_pagamento").equals("Fiado"))
					{
						double totalPagoVenda = UtilCoffe.precoToDouble(pega3.getString("valor_pago"));
						Query verifica = new Query();
						verifica.executaQuery("SELECT valor FROM gastos WHERE `venda_fiado` = " + pega3.getInt("vendas_id"));
						while(verifica.next()) {
							totalPagoVenda += UtilCoffe.precoToDouble(verifica.getString("valor"));
						}
						verifica.fechaConexao();
						
						row.createCell(11).setCellValue(UtilCoffe.doubleToPreco(totalPagoVenda));
						totalPago += totalPagoVenda;
					}
					else
					{
						row.createCell(11).setCellValue(pega3.getString("valor_pago").replaceAll(",", "."));
						totalPago += UtilCoffe.precoToDouble(pega3.getString("total"));
					}
				}
				else
				{
					row.createCell(11).setCellValue(pega3.getString("valor_pago").replaceAll(",", "."));
					totalPago += UtilCoffe.precoToDouble(pega3.getString("valor_pago"));
				}
				
				row.createCell(12).setCellValue(pega3.getString("troco").replaceAll(",", "."));
				totalTroco += UtilCoffe.precoToDouble(pega3.getString("troco"));
				index++;
			}
			
	    	pega3.fechaConexao();
	    	
			sheet.createRow(index+1);
			HSSFRow row = sheet.createRow(index+2);
			
			row.createCell(8).setCellValue("TOTAL:");
			row.createCell(9).setCellValue(UtilCoffe.doubleToPreco(totalTudo));
			row.createCell(10).setCellValue(UtilCoffe.doubleToPreco(totalTudoSemBonus));
			row.createCell(11).setCellValue(UtilCoffe.doubleToPreco(totalPago));
			row.createCell(12).setCellValue(UtilCoffe.doubleToPreco(totalTroco));
			
			sheet.createRow(index+3);
			row = sheet.createRow(index+4);
			row.createCell(8).setCellValue("GASTOS:");
			row.createCell(9).setCellValue("-" + UtilCoffe.doubleToPreco(gastos));
			row.createCell(10).setCellValue("-" + UtilCoffe.doubleToPreco(gastos));
			
			sheet.createRow(index+5);
			row = sheet.createRow(index+6);
			
			row.createCell(8).setCellValue("LUCRO:");
			row.createCell(9).setCellValue(UtilCoffe.doubleToPreco(totalTudo-gastos));
			row.createCell(10).setCellValue(UtilCoffe.doubleToPreco(totalTudoSemBonus-gastos));
			
			sheet.autoSizeColumn(0);
			sheet.autoSizeColumn(1);
			sheet.autoSizeColumn(2);
			sheet.autoSizeColumn(3);
			sheet.autoSizeColumn(4);
			sheet.autoSizeColumn(5);
			sheet.autoSizeColumn(6);
			sheet.autoSizeColumn(7);
			sheet.autoSizeColumn(8);
			sheet.autoSizeColumn(9);
			sheet.autoSizeColumn(10);
			sheet.autoSizeColumn(11);
			sheet.autoSizeColumn(12);
			
			SimpleDateFormat formataDataSQL = new SimpleDateFormat("dd-M-yyyy");
			String nomeArquivo = "/Vendas_" + formataDataSQL.format(inicio) + "__" + 
					formataDataSQL.format(fim) + ".xls";
			
			FileOutputStream fileOut = new FileOutputStream(chooser.getCurrentDirectory() + "/" + chooser.getSelectedFile().getName() + nomeArquivo);
			wb.write(fileOut);
			fileOut.close();
			labelProgresso.setText("Finalizado.");
			salvando.setEnabled(true);
			
			Desktop dt = Desktop.getDesktop();
			dt.open(new File(chooser.getCurrentDirectory() + "/" + chooser.getSelectedFile().getName() + nomeArquivo));
		} catch (ClassNotFoundException | SQLException | IOException e1) {
			e1.printStackTrace();
			salvando.setEnabled(true);
			new PainelErro(e1);
		}
	}
}
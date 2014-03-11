package codecoffe.restaurantes.utilitarios;
//import java.awt.Desktop;
import java.awt.print.PrinterJob;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.ServiceUI;
import javax.print.SimpleDoc;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;
//import javax.print.attribute.standard.Copies;
import javax.swing.JOptionPane;

import codecoffe.restaurantes.graficos.PainelErro;
import codecoffe.restaurantes.mysql.Query;
import codecoffe.restaurantes.sockets.CacheVendaFeita;

public class Recibo
{
	static public synchronized void imprimir(int mesa)
	{
		try {
			String nomeRecibo = "codecoffe/recibo_";
			nomeRecibo += mesa;
			nomeRecibo += ".txt";

			java.io.InputStream is = new FileInputStream(nomeRecibo);
			Scanner sc = new Scanner(is);
			FileOutputStream fs = new FileOutputStream("LPT1:");
			PrintStream ps = new PrintStream(fs);

			while(sc.hasNextLine()){
				String linhas = sc.nextLine();
				ps.println(linhas);
			}
			fs.close();
			sc.close();
			is.close();
		} catch (IOException ex) {
			if(ex.getMessage().toLowerCase().contains("arquivo especificado")) {
				JOptionPane.showMessageDialog(null, "Impressora não está instalada ou não foi encontrada "
						+ "na porta LPT1.", "Erro ao Imprimir", JOptionPane.ERROR_MESSAGE);
			}
			else
			{
				ex.printStackTrace();
				new PainelErro(ex);
			}
		}			
	}

	public synchronized void imprimir2(Configuracao cfg, int mesa)
	{
		try {
			PrintService mPrinter = null;
			PrintService[] printServices = PrinterJob.lookupPrintServices();
	        for (PrintService printService : printServices) {
	            if(cfg.getImpressora().equals(printService.getName())) {
	            	mPrinter = printService;
	            	break;
	            }
	        }

			// Definição de atributos do conteúdo a ser impresso:   
			DocFlavor docFlavor = DocFlavor.INPUT_STREAM.AUTOSENSE; 

			// Atributos de impressão do documento   
			HashDocAttributeSet attributes = new HashDocAttributeSet();

			// Arquivo para imprimir
			String nomeRecibo = "codecoffe/recibo_";
			nomeRecibo += mesa;
			nomeRecibo += ".txt";
			
			FileInputStream fi = new FileInputStream(nomeRecibo);   

			// Cria um Doc para impressão a partir do arquivo exemplo.txt   
			Doc documentoTexto = new SimpleDoc(fi, docFlavor, attributes);   

			// Configura o conjunto de parametros para a impressora   
			PrintRequestAttributeSet printerAttributes = new HashPrintRequestAttributeSet();
			// Adiciona propriedade de impressão: imprimir uma cópia
			printerAttributes.add(new Copies(1));
			
			if(mPrinter == null)
			{
				// exibe um dialogo de configuracoes de impressao   
				PrintService servico = ServiceUI.printDialog(null, 320, 240, printServices, mPrinter, docFlavor, printerAttributes);   

				if(servico != null) 
				{
					DocPrintJob printJob = servico.createPrintJob();
					PrintJobWatcher pjw = new PrintJobWatcher(printJob);
					
					try {
						String result = null;
						if(servico.getName().length() > 190)
							result = servico.getName().substring(0, 190);
						else
							result = servico.getName();
						
						result = result.replaceAll("'", "");
						Query envia = new Query();
						envia.executaUpdate("UPDATE opcoes SET impressora = '" + result + "'");
						envia.fechaConexao();
						cfg.setImpressora(servico.getName());
					} catch (ClassNotFoundException | SQLException e1) {
						e1.printStackTrace();
					}
					
					printJob.print(documentoTexto, printerAttributes);
					pjw.waitForDone();
					fi.close();
					
					 // send FF to eject the page
				    InputStream ff = new ByteArrayInputStream("\f".getBytes());
				    Doc docff = new SimpleDoc(ff, docFlavor, null);
				    DocPrintJob jobff = servico.createPrintJob();
				    pjw = new PrintJobWatcher(jobff);
				    jobff.print(docff, null);
				    pjw.waitForDone();
				}
			}
			else
			{
				// Cria uma tarefa de impressão   
				DocPrintJob printJob = mPrinter.createPrintJob();
				PrintJobWatcher pjw = new PrintJobWatcher(printJob);

				// Imprime o documento sem exibir uma tela de dialogo   
				printJob.print(documentoTexto, printerAttributes);
				pjw.waitForDone();
				fi.close();
				
				 // send FF to eject the page
			    InputStream ff = new ByteArrayInputStream("\f".getBytes());
			    Doc docff = new SimpleDoc(ff, docFlavor, null);
			    DocPrintJob jobff = mPrinter.createPrintJob();
			    pjw = new PrintJobWatcher(jobff);
			    jobff.print(docff, null);
			    pjw.waitForDone();
			}
		}   
		catch(IOException | PrintException e) {
			e.printStackTrace();
			new PainelErro(e);
		}
	}

	static public void gerarNotaVenda(Configuracao cfg, CacheVendaFeita v)
	{
		/* Sempre verificar se a pasta existe antes */
		File theDir = new File("codecoffe");
		if (!theDir.exists()) {
			theDir.mkdir();
		}

		try{
			String nomeRecibo = "codecoffe/recibo_";
			nomeRecibo += v.getCaixa();
			nomeRecibo += ".txt";

			File arquivo = new File(nomeRecibo);
			FileWriter arquivoTxt = new FileWriter(arquivo, false);
			PrintWriter linhasTxt = new PrintWriter(arquivoTxt);

			linhasTxt.println("===========================================");

			/////////////////////////////////////////////////////////////////////////////////////
			String[] mSuperior = UtilCoffe.removeAcentos(cfg.getMensagemSuperior()).split("\\s+");
			int letras = 42;
			for(int i = 0; i < mSuperior.length; i++)
			{
				if((mSuperior[i].length()+1) < letras) {
					linhasTxt.print(" " + mSuperior[i]);
					letras -= (mSuperior[i].length()+1);
				}
				else {
					linhasTxt.println();
					linhasTxt.print(" " + mSuperior[i]);
					letras = (42 - (mSuperior[i].length()+1));
				}
			}
			linhasTxt.println();
			/////////////////////////////////////////////////////////////////////////////////////

			linhasTxt.println("===========================================");
			linhasTxt.println("*********** NAO TEM VALOR FISCAL **********");
			linhasTxt.println("===========================================");
			linhasTxt.println("PRODUTO              QTDE  VALOR UN.  VALOR");

			for(int i = 0; i < v.getVendaFeita().getQuantidadeProdutos(); i++)
			{
				linhasTxt.print(String.format("%-20.20s", v.getVendaFeita().getProduto(i).getReferencia()));
				linhasTxt.print(String.format("%3s     ", v.getVendaFeita().getProduto(i).getQuantidade()));
				linhasTxt.print(String.format("%5s    ", UtilCoffe.doubleToPreco(v.getVendaFeita().getProduto(i).getPreco())));
				linhasTxt.print(String.format("%6s    ", UtilCoffe.doubleToPreco((v.getVendaFeita().getProduto(i).getPreco()*v.getVendaFeita().getProduto(i).getQuantidade()))));
				linhasTxt.println();

				for(int j = 0; j < v.getVendaFeita().getProduto(i).getTotalAdicionais(); j++)
				{
					linhasTxt.print(String.format("%-20.20s", "+" + v.getVendaFeita().getProduto(i).getAdicional(j).getReferencia()));
					linhasTxt.print(String.format("%3s     ", v.getVendaFeita().getProduto(i).getQuantidade()));
					linhasTxt.print(String.format("%5s    ", UtilCoffe.doubleToPreco(v.getVendaFeita().getProduto(i).getAdicional(j).getPreco())));
					linhasTxt.print(String.format("%6s    ", 
							UtilCoffe.doubleToPreco((v.getVendaFeita().getProduto(i).getAdicional(j).getPreco()*v.getVendaFeita().getProduto(i).getQuantidade()))));
					linhasTxt.println();
				}
			}            

			linhasTxt.println("===========================================");
			linhasTxt.println("   INFORMACOES PARA FECHAMENTO DE CONTA    ");
			linhasTxt.println("===========================================");

			if(v.getClasse() != 2) {
				linhasTxt.print(String.format("%-18.18s", "Local: "));
				linhasTxt.println(cfg.getTipoNome() + " " + v.getCaixa());
			}
			else {
				linhasTxt.print(String.format("%-18.18s", "Local: "));
				linhasTxt.println("Balcao");
			}

			if(v.getClasse() != 2) {
				linhasTxt.print(String.format("%-18.18s", "Permanencia: "));

				long duration = System.currentTimeMillis() - v.getVendaFeita().getData().getTime();
				long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);

				String formatap = "";

				if((minutes/60) > 0)
				{
					formatap += (minutes/60) + " hora";

					if((minutes/60) > 1) {
						formatap += "s";
					}

					if((minutes % 60) > 1) {
						formatap += " e " + (minutes % 60) + " minutos";
					}
				}
				else
				{
					if((minutes % 60) > 1)
						formatap += (minutes % 60) + " minutos";
					else
						formatap += (minutes % 60) + " minuto";
				}

				linhasTxt.println(formatap);
			}

			linhasTxt.print(String.format("%-18.18s", "Atendido por: "));
			linhasTxt.println(UtilCoffe.removeAcentos(v.getAtendente()));

			Locale locale = new Locale("pt","BR"); 
			GregorianCalendar calendar = new GregorianCalendar(); 
			SimpleDateFormat formatador = new SimpleDateFormat("EEE, dd'/'MM'/'yyyy' - 'HH':'mm", locale);		                

			linhasTxt.print(String.format("%-18.18s", "Data: "));
			linhasTxt.println(UtilCoffe.removeAcentos(formatador.format(calendar.getTime())));

			if(UtilCoffe.precoToDouble(v.getDelivery()) > 0 && v.getFiado_id() > 0 && v.getClasse() == 2)
			{
				try {
					linhasTxt.println();
					Query pegaCliente = new Query();
					pegaCliente.executaQuery("SELECT nome, telefone, endereco, numero, bairro,"
							+ " complemento FROM fiados WHERE fiador_id = " + v.getFiado_id());

					if(pegaCliente.next())
					{
						/////////////////////////////////////////////////////////////////////////////////////
						String dadosCliente = UtilCoffe.removeAcentos((pegaCliente.getString("nome")  + " - TEL: " + pegaCliente.getString("telefone")));

						String[] dados1 = dadosCliente.split("\\s+");
						letras = 42;
						for(int i = 0; i < dados1.length; i++)
						{
							if((dados1[i].length()+1) < letras) {
								linhasTxt.print(" " + dados1[i]);
								letras -= (dados1[i].length()+1);
							}
							else {
								linhasTxt.println();
								linhasTxt.print(" " + dados1[i]);
								letras = (42 - (dados1[i].length()+1));
							}
						}
						linhasTxt.println();
						/////////////////////////////////////////////////////////////////////////////////////

						/////////////////////////////////////////////////////////////////////////////////////
						dadosCliente = UtilCoffe.removeAcentos((pegaCliente.getString("endereco") + " - " + pegaCliente.getString("numero") 
								+ " - " + pegaCliente.getString("complemento")));

						String[] dados2 = dadosCliente.split("\\s+");
						letras = 42;
						for(int i = 0; i < dados2.length; i++)
						{
							if((dados2[i].length()+1) < letras) {
								linhasTxt.print(" " + dados2[i]);
								letras -= (dados2[i].length()+1);
							}
							else {
								linhasTxt.println();
								linhasTxt.print(" " + dados2[i]);
								letras = (42 - (dados2[i].length()+1));
							}
						}
						linhasTxt.println();
						/////////////////////////////////////////////////////////////////////////////////////
					}

					pegaCliente.fechaConexao();
				} catch (ClassNotFoundException | SQLException e) {
					e.printStackTrace();
					new PainelErro(e);
				}
			}
			else if(v.getFiado_id() > 0)
			{
				try {
					linhasTxt.println();
					Query pegaCliente = new Query();
					pegaCliente.executaQuery("SELECT nome, cpf FROM fiados WHERE fiador_id = " + v.getFiado_id());

					if(pegaCliente.next())
					{
						/////////////////////////////////////////////////////////////////////////////////////
						String dadosCliente = UtilCoffe.removeAcentos((pegaCliente.getString("nome")  + " " + pegaCliente.getString("cpf")));

						String[] dados1 = dadosCliente.split("\\s+");
						letras = 42;
						for(int i = 0; i < dados1.length; i++)
						{
							if((dados1[i].length()+1) < letras) {
								linhasTxt.print(" " + dados1[i]);
								letras -= (dados1[i].length()+1);
							}
							else {
								linhasTxt.println();
								linhasTxt.print(" " + dados1[i]);
								letras = (42 - (dados1[i].length()+1));
							}
						}
						linhasTxt.println();
						////////////////////////////////////////////////////////////////////////////////////
					}

					pegaCliente.fechaConexao();
				} catch (ClassNotFoundException | SQLException e) {
					e.printStackTrace();
					new PainelErro(e);
				}
			}

			linhasTxt.println("===========================================");
			
			if(v.getClasse() == 2)
			{
				if(UtilCoffe.precoToDouble(v.getDelivery()) > 0)
					linhasTxt.println("Taxa de Entrega                  R$" + v.getDelivery());              

				linhasTxt.println("                     ----------------------");
				linhasTxt.println("Total                            R$" + UtilCoffe.doubleToPreco(UtilCoffe.precoToDouble(v.getTotal()) 
																		+ UtilCoffe.precoToDouble(v.getDelivery())));

				if(UtilCoffe.precoToDouble(v.getDelivery()) <= 0)
				{
					if(UtilCoffe.precoToDouble(v.getDezporcento()) > 0 && cfg.isDezPorcentoRapida())
					{
						linhasTxt.println("                     ----------------------");
						linhasTxt.println("10% Opcional                     R$" + v.getDezporcento());            	  
					}	
				}
			}
			else
			{
				linhasTxt.println("                     ----------------------");
				linhasTxt.println("Total                            R$" + v.getTotal());

				if(UtilCoffe.precoToDouble(v.getDezporcento()) > 0 && cfg.getDezPorcento())
				{
					linhasTxt.println("                     ----------------------");
					linhasTxt.println("10% Opcional                     R$" + v.getDezporcento());            	  
				}	
			}

			linhasTxt.println("===========================================");

			/////////////////////////////////////////////////////////////////////////////////////
			String[] mInferior = UtilCoffe.removeAcentos(cfg.getMensagemInferior()).split("\\s+");
			letras = 42;
			for(int i = 0; i < mInferior.length; i++)
			{
				if((mInferior[i].length()+1) < letras) {
					linhasTxt.print(" " + mInferior[i]);
					letras -= (mInferior[i].length()+1);
				}
				else {
					linhasTxt.println();
					linhasTxt.print(" " + mInferior[i]);
					letras = (42 - (mInferior[i].length()+1));
				}
			}
			linhasTxt.println();
			/////////////////////////////////////////////////////////////////////////////////////

			linhasTxt.println("===========================================");
			linhasTxt.println("        Sistema CodeCoffe " + UtilCoffe.VERSAO);

			int i = 0;
			while(i < 10){
				i++;
				linhasTxt.println();
			}

			arquivoTxt.close();
			linhasTxt.close();
			
			if(cfg.getImpressora().equals("LPT1")) {
				Recibo.imprimir(v.getCaixa());
			}
			else {
				new Recibo().imprimir2(cfg, v.getCaixa());
				//Desktop.getDesktop().print(new File("codecoffe/recibo_0.txt")); 
			}
		}
		catch(IOException error)
		{
			error.printStackTrace();
			new PainelErro(error);
		}			
	}
	
	class PrintJobWatcher {
		boolean done = false;

		PrintJobWatcher(DocPrintJob job) 
		{
			job.addPrintJobListener(new PrintJobAdapter() {
				public void printJobCanceled(PrintJobEvent pje) {
					allDone();
				}
				public void printJobCompleted(PrintJobEvent pje) {
					allDone();
				}
				public void printJobFailed(PrintJobEvent pje) {
					allDone();
				}
				public void printJobNoMoreEvents(PrintJobEvent pje) {
					allDone();
				}
				void allDone() {
					synchronized (PrintJobWatcher.this) {
						done = true;
						PrintJobWatcher.this.notify();
					}
				}
			});
		}
		public synchronized void waitForDone() {
			try {
				while (!done) {
					wait();
				}
			} catch (InterruptedException e) {}
		}
	}
}
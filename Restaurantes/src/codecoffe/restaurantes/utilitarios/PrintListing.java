package codecoffe.restaurantes.utilitarios;

import java.awt.*;  
import java.awt.print.*;  
import java.io.*; 
import java.util.Scanner;

public class PrintListing  
{    
	public static void main(String[] args)  
	{      
		// Get a PrinterJob 
		PrinterJob job = PrinterJob.getPrinterJob(); 
		// Ask user for page format (e.g., portrait/landscape) 
		PageFormat pf = job.pageDialog(job.defaultPage()); 
		// Specify the Printable is an instance of 
		// PrintListingPainter; also provide given PageFormat 
		job.setPrintable(new PrintListingPainter(), pf); 
		// Print 1 copy    
		job.setCopies(1);      
		// Put up the dialog box      
		if (job.printDialog())  
		{ 
			// Print the job if the user didn't cancel printing 
			try { job.print(); } 
			catch (Exception e) { /* handle exception */ }      
		}      
		System.exit(0);    
	}  
} 

class PrintListingPainter implements Printable  
{
	public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException
	{ 
		try  
		{
			java.io.InputStream is = new FileInputStream("codecoffe/recibo_0.txt");
			Scanner sc = new Scanner(is);
			g.setColor(Color.black);
			
			int x = (int) pf.getImageableX() + 10; 
			int y = (int) pf.getImageableY() + 12;     

			while(sc.hasNextLine())
			{
				String linha = sc.nextLine();
				g.drawString(linha, x, y);  
				y += 12; 
			}
			
			sc.close();
			return Printable.PAGE_EXISTS;     
		}  
		catch (Exception e) { return Printable.NO_SUCH_PAGE;} }  
}
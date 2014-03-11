package codecoffe.restaurantes.main;

//Import packages
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;

public class Program1{
	//Static swing components
	static JFrame frmMain;
	static Container pane;
	static JButton btnPrint;

	public static void main (String[] args){
		//Apply system look and feel
		try {UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());}
			  catch (Exception e){}
		
		//Create and resize frame
		frmMain = new JFrame ("Sample printing application");
		frmMain.setSize(300, 200); //300x200 pixels
		pane = frmMain.getContentPane();
		pane.setLayout(null); //Use null layout

		//Create components
		btnPrint = new JButton ("Print"); //Create our button

		//Add components to pane
		pane.add(btnPrint);

		//Set components' bounds
		btnPrint.setBounds(5, 5, 100, 25); //Arguments: x, y, width, height

		//Make frame visible
		frmMain.setVisible(true);

		//Add the button's action
		btnPrint.addActionListener(new btnPrintAction());
	}

	public static class btnPrintAction implements ActionListener, Printable{
		public int print(Graphics gx, PageFormat pf, int page) throws PrinterException {
			if (page>0){return NO_SUCH_PAGE;} //Only one page
			Graphics2D g = (Graphics2D)gx; //Cast to Graphics2D object
			g.translate(pf.getImageableX(), pf.getImageableY()); //Match origins to imageable area
			g.drawString ("Hello world", 100, 100); //Print Hello World at offset (100, 100)
			return PAGE_EXISTS; //Page exists (offsets start at zero!)
		}
		public void actionPerformed(ActionEvent e) {
			PrinterJob job = PrinterJob.getPrinterJob(); //Get the printer's job list
			job.setPrintable(this); //We print with this class (btnPrintAction, which implements Printable)
			if (job.printDialog() == true) { //If we clicked OK in the print dialog
				try {job.print();} catch (PrinterException ex){
					//It did not work (PrinterException thrown), so add any error handling routines.
				}
			}
		}
	}
}
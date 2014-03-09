package codecoffe.restaurantes.utilitarios;

import java.awt.Dimension;
import java.sql.SQLException;
import java.util.GregorianCalendar;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import codecoffe.restaurantes.graficos.PainelErro;
import codecoffe.restaurantes.mysql.Query;

public class GraficoTotal extends JPanel 
{
	private static final long serialVersionUID = 1L;

	public GraficoTotal() {

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setMaximumSize(new Dimension(600, 280));
		setMinimumSize(new Dimension(600, 280));

		GregorianCalendar c = new GregorianCalendar();
		double[] meses = new double[12];

		for(int i = 0; i < 12; i++)
			meses[i] = 0;     

		Query pega = new Query();
		try {
			pega.executaQuery("SELECT total, mes FROM vendas WHERE `data` BETWEEN '" 
					+ c.get(GregorianCalendar.YEAR) + "-01-01' AND '" + c.get(GregorianCalendar.YEAR) + "-12-31'");

			while(pega.next())
			{
				meses[pega.getInt("mes")] += UtilCoffe.precoToDouble(pega.getString("total"));
			}

			pega.fechaConexao();			
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			new PainelErro(e);	
		}
		finally
		{
			
	        DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
	        dataSet.setValue(meses[0], "Total", "1");
	        dataSet.setValue(meses[1], "Total", "2");
	        dataSet.setValue(meses[2], "Total", "3");
	        dataSet.setValue(meses[3], "Total", "4");
	        dataSet.setValue(meses[4], "Total", "5");
	        dataSet.setValue(meses[5], "Total", "6");
	        dataSet.setValue(meses[6], "Total", "7");
	        dataSet.setValue(meses[7], "Total", "8");
	        dataSet.setValue(meses[8], "Total", "9");
	        dataSet.setValue(meses[9], "Total", "10");
	        dataSet.setValue(meses[10], "Total", "11");
	        dataSet.setValue(meses[11], "Total", "12");
	 
	        JFreeChart chart = ChartFactory.createBarChart(
	        		"Relação Total x Mês (" + c.get(GregorianCalendar.YEAR) + ")", "Mês", "Total em Vendas R$",
	                dataSet, PlotOrientation.VERTICAL, false, true, false);

			ChartPanel chartPanel = new ChartPanel(chart);
			add(chartPanel);      	
		}             
	}
}
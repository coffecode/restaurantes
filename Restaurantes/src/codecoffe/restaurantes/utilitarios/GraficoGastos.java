package codecoffe.restaurantes.utilitarios;

import java.awt.Color;
import java.awt.Dimension;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import codecoffe.restaurantes.graficos.PainelErro;
import codecoffe.restaurantes.mysql.Query;

public class GraficoGastos extends JPanel 
{
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("deprecation")
	public GraficoGastos() {

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setMaximumSize(new Dimension(600, 280));
		setMinimumSize(new Dimension(600, 280));

		XYSeries series1   = new XYSeries("Gastos");
		GregorianCalendar c = new GregorianCalendar();
		double[] meses = new double[12];
		
		SimpleDateFormat formataDataSQL = new SimpleDateFormat("yyyy-M-dd");

		for(int i = 0; i < 12; i++)
			meses[i] = 0;     

		Query pega = new Query();
		try {
			pega.executaQuery("SELECT valor, data FROM gastos WHERE `data` BETWEEN '" 
					+ c.get(GregorianCalendar.YEAR) + "-01-01' AND '" + c.get(GregorianCalendar.YEAR) + "-12-31'");

			while(pega.next())
			{
				Date data = formataDataSQL.parse(pega.getString("data"));
				meses[data.getMonth()] += UtilCoffe.precoToDouble(pega.getString("valor"));
			}

			pega.fechaConexao();			
		} catch (ClassNotFoundException | SQLException | ParseException e) {
			e.printStackTrace();
			new PainelErro(e);	
		}
		finally
		{
			for(int i = 0; i < 12; i++)
				series1.add(i+1, meses[i]);

			XYSeriesCollection xyDataset = new XYSeriesCollection();
			xyDataset.addSeries(series1);

			JFreeChart chart = ChartFactory.createXYLineChart("Relação Gastos x Mês (" + c.get(GregorianCalendar.YEAR) + ")", 
					"Mês", "Gastos", xyDataset, PlotOrientation.VERTICAL, true, false, false);
			chart.setBackgroundPaint(getBackground());

			XYPlot plot = (XYPlot) chart.getPlot();
			plot.setBackgroundPaint       (Color.white);
			plot.setDomainGridlinePaint   (Color.blue);
			plot.setRangeGridlinePaint    (Color.blue);
			plot.setDomainCrosshairVisible(true);
			plot.setRangeCrosshairVisible (true);

			XYLineAndShapeRenderer renderer  = (XYLineAndShapeRenderer) plot.getRenderer();      
			renderer.setBaseShapesVisible(true);
			renderer.setBaseShapesFilled (true);

			ChartPanel chartPanel = new ChartPanel(chart);
			add(chartPanel);      	
		}             
	}
}
package codecoffe.restaurantes.utilitarios;
import java.awt.Color;
import java.awt.Dimension;
import java.sql.SQLException;
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

public class GraficoFiados extends JPanel 
{
	private static final long serialVersionUID = 1L;

	public GraficoFiados() {

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setMaximumSize(new Dimension(600, 150));
		setMinimumSize(new Dimension(600, 150));

		XYSeries series1   = new XYSeries("Fiados");
		GregorianCalendar c = new GregorianCalendar();
		int[] meses = new int[12];

		for(int i = 0; i < 12; i++)
			meses[i] = 0;     

		Query pega = new Query();
		try {
			pega.executaQuery("SELECT mes FROM vendas WHERE ano = " + c.get(GregorianCalendar.YEAR) + " AND forma_pagamento = 'Fiado'");

			while(pega.next())
				meses[pega.getInt("mes")] ++;

			pega.fechaConexao();			
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			new PainelErro(e);	
		}
		finally
		{
			for(int i = 0; i < 12; i++)
				series1.add(i+1, meses[i]);

			XYSeriesCollection xyDataset = new XYSeriesCollection();
			xyDataset.addSeries(series1);

			JFreeChart chart = ChartFactory.createXYLineChart("Relação Fiados x Mês (" + c.get(GregorianCalendar.YEAR) + ")", "Mês", "Vendas", xyDataset, PlotOrientation.VERTICAL, true, false, false);
			chart.setBackgroundPaint(getBackground());

			XYPlot plot = (XYPlot) chart.getPlot();
			plot.setBackgroundPaint       (Color.white);
			plot.setDomainGridlinePaint   (Color.blue);
			plot.setRangeGridlinePaint    (Color.blue);
			//plot.setAxisOffset            (new RectangleInsets(1, 10, 10, 5));
			plot.setDomainCrosshairVisible(true);
			plot.setRangeCrosshairVisible (true);

			XYLineAndShapeRenderer renderer  = (XYLineAndShapeRenderer) plot.getRenderer();      
			renderer.setBaseShapesVisible(true);
			renderer.setBaseShapesFilled (true);

			ChartPanel chartPanel = new ChartPanel(chart);
			add(chartPanel);      	
		}             
	}
	
	public void refresh()
	{
		removeAll();
		XYSeries series1   = new XYSeries("Fiados");
		GregorianCalendar c = new GregorianCalendar();
		int[] meses = new int[12];

		for(int i = 0; i < 12; i++)
			meses[i] = 0;     

		Query pega = new Query();
		try {
			pega.executaQuery("SELECT mes FROM vendas WHERE ano = " + c.get(GregorianCalendar.YEAR) + " AND forma_pagamento = 'Fiado'");

			while(pega.next())
				meses[pega.getInt("mes")] ++;

			pega.fechaConexao();			
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			new PainelErro(e);	
		}
		finally
		{
			for(int i = 0; i < 12; i++)
				series1.add(i+1, meses[i]);

			XYSeriesCollection xyDataset = new XYSeriesCollection();
			xyDataset.addSeries(series1);

			JFreeChart chart = ChartFactory.createXYLineChart("Relação Fiados x Mês (" + c.get(GregorianCalendar.YEAR) + ")", "Mês", "Vendas", xyDataset, PlotOrientation.VERTICAL, true, false, false);
			chart.setBackgroundPaint(getBackground());

			XYPlot plot = (XYPlot) chart.getPlot();
			plot.setBackgroundPaint       (Color.white);
			plot.setDomainGridlinePaint   (Color.blue);
			plot.setRangeGridlinePaint    (Color.blue);
			//plot.setAxisOffset            (new RectangleInsets(1, 10, 10, 5));
			plot.setDomainCrosshairVisible(true);
			plot.setRangeCrosshairVisible (true);

			XYLineAndShapeRenderer renderer  = (XYLineAndShapeRenderer) plot.getRenderer();      
			renderer.setBaseShapesVisible(true);
			renderer.setBaseShapesFilled (true);

			ChartPanel chartPanel = new ChartPanel(chart);
			add(chartPanel);
			revalidate();
			repaint();
		} 
	}
}
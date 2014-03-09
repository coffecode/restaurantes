package codecoffe.restaurantes.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import codecoffe.restaurantes.eventos.BancoVerificado;
import codecoffe.restaurantes.graficos.PainelErro;

import com.alee.utils.ThreadUtils;

public class Loading
{
	private JFrame splash;
	private JPanel inicio;
	private JProgressBar progressBar;
	private JLabel statusProgress;
	
	public Loading()
	{
		splash = new JFrame();
		inicio = new JPanel();
		inicio.setLayout(null);
		
		JLabel splashImage = new JLabel(new ImageIcon(getClass().getClassLoader().getResource("imgs/splashscreen.png")));
		splashImage.setBounds(1, 1, 445, 276);
		
		progressBar = new JProgressBar(0, 100);
    	progressBar.setValue(0);
    	progressBar.setStringPainted(true);
    	progressBar.setBounds(1, 220, 443, 55);
    	
    	statusProgress = new JLabel("Preparando inicialização...");
    	statusProgress.setBounds(10, 184, 443, 55);
    	
    	inicio.add(statusProgress);
    	inicio.add(progressBar);
    	inicio.add(splashImage);
		splash.add(inicio);
		
		splash.setUndecorated(true);
		splash.setSize(445,276);
		splash.setLocationRelativeTo(null);
		splash.setIconImage(new ImageIcon(getClass().getClassLoader().getResource("imgs/icone_programa.png")).getImage());
		splash.setResizable(false);
		splash.setVisible(true);
	}
	
	public void dispose() {
		splash.dispose();
	}
	
	public void setProgress(String text, int progress)
	{
		final String texto = text;
		final int progresso = progress;
		SwingUtilities.invokeLater(new Runnable() {
		    @Override
		    public void run() {
		    	statusProgress.setText(texto);
		    	progressBar.setValue(progresso);
		    }
		});
	}
	
	public void setProgress(String text)
	{
		final String texto = text;
		SwingUtilities.invokeLater(new Runnable() {
		    @Override
		    public void run() {
		    	statusProgress.setText(texto);
		    }
		});
	}
	
	public void setProgress(int progress)
	{
		final int progresso = progress;
		SwingUtilities.invokeLater(new Runnable() {
		    @Override
		    public void run() {
		    	progressBar.setValue(progresso);
		    }
		});
	}
	
	public void verificarMYSQL(BancoVerificado listen)
	{
		final BancoVerificado listener = listen;
		new Thread(new Runnable()
        {
            @Override
            public void run()
            {
            	setProgress("Verificando banco de dados...", 1);
            	ThreadUtils.sleepSafely(500);

        		try {
            		String line;
            		String pidInfo ="";
        			Process p = Runtime.getRuntime().exec(System.getenv("windir") +"\\system32\\"+"tasklist.exe");
        			BufferedReader input =  new BufferedReader(new InputStreamReader(p.getInputStream()));
        			
        			while ((line = input.readLine()) != null) {
        			    pidInfo+=line; 
        			}
        			input.close();
        			
        			if(pidInfo.contains("mysqld"))
        			{
        				setProgress("Banco de dados ligado.");
        				ThreadUtils.sleepSafely(500);
        			}
        			else
        			{
        				setProgress("Iniciando banco de dados...");
    					String command = System.getProperty("user.dir") + "\\mysql\\bin\\mysqld.exe";
    					Runtime.getRuntime().exec(command);
        				ThreadUtils.sleepSafely(3000);
        			}
        			
        			listener.bancoVerificado();
        		} catch (IOException e) {
        			e.printStackTrace();
        			new PainelErro(e);
        			System.exit(0);
        		}
            }
        }).start();
	}
}
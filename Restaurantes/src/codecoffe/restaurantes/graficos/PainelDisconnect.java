package codecoffe.restaurantes.graficos;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.alee.laf.button.WebButton;
import com.alee.laf.progressbar.WebProgressBar;

import net.miginfocom.swing.MigLayout;

public class PainelDisconnect extends JFrame
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PainelDisconnect()
	{
		setTitle("CodeCoffe - Conexão perdida");
		
		JPanel painelDc = new JPanel(new MigLayout());
		painelDc.setPreferredSize(new Dimension(500, 300));
		painelDc.setBorder(new EmptyBorder(6, 6, 6, 6));
		
		painelDc.add(new JLabel("<html><FONT COLOR='#FF0000'>Houve uma perda de conexão entre esse terminal e o computador principal.</font><br><br>"
				+ "Verifique se o computador principal está ligado e conectado na rede.</html>"), "wrap, align center");
		
		WebProgressBar barraBuscando = new WebProgressBar();
		barraBuscando.setIndeterminate(true);
		barraBuscando.setStringPainted(true);
		barraBuscando.setString("Tentando reconectar...");
		barraBuscando.setPreferredSize(new Dimension(250, 50));
		
		painelDc.add(barraBuscando, "wrap, gaptop 15, align center");
		
		ActionListener acListener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		};
		
		WebButton bCancelar = new WebButton("Cancelar");
		bCancelar.setPreferredSize(new Dimension(120, 40));
		bCancelar.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/cancelar.png")));
		bCancelar.setRolloverShine(true);
		bCancelar.addActionListener(acListener);
		
		painelDc.add(bCancelar, "wrap, gaptop 20, align center");
		
		add(painelDc);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(465, 240);
		setLocationRelativeTo(null);
		setIconImage(new ImageIcon(getClass().getClassLoader().getResource("imgs/icone_programa.png")).getImage());
		setResizable(false);	
	}
}

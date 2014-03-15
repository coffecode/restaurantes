package codecoffe.restaurantes.graficos;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import codecoffe.restaurantes.eventos.AtualizarPainel;
import codecoffe.restaurantes.eventos.FazLogin;
import codecoffe.restaurantes.eventos.LegendaAlterada;
import codecoffe.restaurantes.eventos.MenuSelecionado;
import codecoffe.restaurantes.sockets.Client;
import codecoffe.restaurantes.utilitarios.Configuracao;
import codecoffe.restaurantes.utilitarios.DiarioLog;
import codecoffe.restaurantes.utilitarios.Usuario;
import codecoffe.restaurantes.utilitarios.UtilCoffe;

public class FramePrincipal extends JFrame implements MenuSelecionado, LegendaAlterada, FazLogin
{
	private static final long serialVersionUID = 1L;
	private JPanel componentes, componentesCentrais;
	private Configuracao config;
	private PainelLegenda painelLegenda;
	private Object modoPrograma;
	private Login login;
	
	public FramePrincipal(String titulo, Configuracao cfg, Object modo, AtualizarPainel listener)
	{
		super(titulo);
		config = cfg;
		modoPrograma = modo;
		
		setResizable(true);
		setIconImage(new ImageIcon(getClass().getClassLoader().getResource("imgs/icone_programa.png")).getImage());
		setMinimumSize(new Dimension(980, 650));	
		setMaximumSize(new Dimension(1920, 1080));	
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				int opcao = JOptionPane.showConfirmDialog(null, 
					"Você tem certeza que deseja sair?", "Logout", JOptionPane.YES_NO_OPTION);
		
				if(opcao == JOptionPane.YES_OPTION)
					logout();
			}
		});
		
		componentes = new JPanel();
		componentes.setLayout(new BoxLayout(componentes, BoxLayout.Y_AXIS));
		componentes.setMaximumSize(new Dimension(980, 400));
		componentesCentrais = new JPanel(new CardLayout());
		componentesCentrais.setMinimumSize(new Dimension(980, 400));
		componentesCentrais.setMaximumSize(new Dimension(1920, 910));
		componentes.add(new PainelStatus(config, modoPrograma, this, this, listener));
		componentes.add(new PainelMenu(config, this, this));
		componentes.add(componentesCentrais);
		painelLegenda = new PainelLegenda();
		componentes.add(painelLegenda);
		
		add(componentes);
	}
	
	public void abrir()
	{
		login = new Login(config, modoPrograma, new FazLogin() {
		@Override
		public void login(String usuario) {
			logar(usuario);
		}

		@Override
		public void logout() {}
		});

		//Usuario.INSTANCE.setLevel(2);
		//Usuario.INSTANCE.setNome("André Alves " + config.getModo());
		//((PainelStatus) componentes.getComponent(0)).setNome("André Alves " + config.getModo());
		//setVisible(true);
		System.gc();
	}
	
	public void logar(String nome)
	{
		((PainelStatus) componentes.getComponent(0)).setNome(nome);
		setVisible(true);
		
		if(login != null)
			login.dispose();
		
		((PainelVendaMesa) componentesCentrais.getComponent((componentesCentrais.getComponentCount()-1))).setFuncionarioSelected(nome);
		((PainelVendaRapida) componentesCentrais.getComponent((componentesCentrais.getComponentCount()-2))).setFuncionarioSelected(nome);			
	}
	
	public void adicionarPainel(JPanel painel, String titulo) {
		componentesCentrais.add(painel, titulo);
	}
	
	public void adicionarPainel(JTabbedPane painel, String titulo) {
		componentesCentrais.add(painel, titulo);
	}

	@Override
	public void abrirMenu(String text) {
		CardLayout cardLayout = (CardLayout) componentesCentrais.getLayout();
		cardLayout.show(componentesCentrais, text);
	}

	@Override
	public void alterarLegenda(String text) {
		painelLegenda.atualizarLegenda(text);
	}

	@Override
	public void login(String usuario) {}

	@Override
	public void logout() {
		if(config.getModo() == UtilCoffe.SERVER) {
			DiarioLog.add(Usuario.INSTANCE.getNome(), "Saiu do sistema.", 9);
		}
		else {
			((Client) modoPrograma).enviarObjeto(Usuario.INSTANCE.getNome() + ";QUIT");
		}	
		
		setVisible(false);
		login = new Login(config, modoPrograma, new FazLogin() {
			@Override
			public void login(String usuario) {
				logar(usuario);
				
				CardLayout cardLayout = (CardLayout) componentesCentrais.getLayout();
				cardLayout.show(componentesCentrais, "Menu Mesas");	
			}

			@Override
			public void logout() {}
		});
	}
}
package codecoffe.restaurantes.main;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import javax.xml.stream.XMLStreamException;

import net.miginfocom.swing.MigLayout;
import codecoffe.restaurantes.graficos.PainelErro;
import codecoffe.restaurantes.utilitarios.UtilCoffe;
import codecoffe.restaurantes.xml.LerStarterConfig;
import codecoffe.restaurantes.xml.SalvarStarterConfig;

import com.alee.laf.WebLookAndFeel;
import com.alee.laf.button.WebButton;
import com.alee.laf.progressbar.WebProgressBar;
import com.alee.laf.text.WebTextField;
import com.alee.managers.language.LanguageManager;

public class Inicio
{
	private JFrame seleciona;
	private JTabbedPane selecionaPanel;
	private JLabel descricaoOperacao;
	private WebTextField campoIP;
	private WebButton bTerminal, bPrincipal;
	private WebProgressBar verificandoBar;
	public static final int portaConnect = 27013;
	
	public Inicio()
	{
		System.setProperty("java.net.preferIPv4Stack", "true");
		LanguageManager.DEFAULT = LanguageManager.PORTUGUESE;
		WebLookAndFeel.install();
		ToolTipManager.sharedInstance().setInitialDelay(500);
		ToolTipManager.sharedInstance().setDismissDelay(40000);
		painelInicial();
	}
	
	public void comecarLoading(int modo, InetAddress host, int porta)
	{		
		if(modo == UtilCoffe.CLIENT)
		{			
			try {
				new RestauranteClient(host, porta);
				
				seleciona.dispose();
				seleciona = null;
				selecionaPanel = null;
				descricaoOperacao = null;
				campoIP = null;
				bTerminal = null;
				bPrincipal = null;
				verificandoBar = null;
			} catch (IOException e) {
				if(e.getMessage().toLowerCase().contains("refused"))
				{
					descricaoOperacao.setText("Conexão recusada em: " + host.getHostAddress());
					verificandoBar.setVisible(false);
				}
				else if(e.getMessage().toLowerCase().contains("timed out"))
				{
					descricaoOperacao.setText("Não foi possível conectar em: " + host.getHostAddress());
					verificandoBar.setVisible(false);
				}
				else
				{
					e.printStackTrace();
					new PainelErro(e);
				}
			}
		}
		else
		{
			seleciona.dispose();
			seleciona = null;
			selecionaPanel = null;
			descricaoOperacao = null;
			campoIP = null;
			bTerminal = null;
			bPrincipal = null;
			verificandoBar = null;
			
			new Restaurante();
		}
	}
	
	private void painelInicial()
	{
		seleciona = new JFrame("CodeCoffe " + UtilCoffe.VERSAO);
		seleciona.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		selecionaPanel = new JTabbedPane();
		selecionaPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		JPanel iniciar = new JPanel(new MigLayout("align center"));
		
		bTerminal = new WebButton("Terminal");
		bTerminal.setRolloverShine(true);
		bTerminal.setHorizontalTextPosition(AbstractButton.CENTER);
		bTerminal.setVerticalTextPosition(AbstractButton.BOTTOM);
		bTerminal.setPreferredSize(new Dimension(100, 100));
		bTerminal.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/terminal.png")));
		bTerminal.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!verificandoBar.isVisible())
				{
					try {
						SalvarStarterConfig salvarXML = new SalvarStarterConfig();
						if(UtilCoffe.vaziu(campoIP.getText()))
							salvarXML.saveConfig("none");
						else
							salvarXML.saveConfig(campoIP.getText());
					} catch (Exception e1) {
						e1.printStackTrace();
						new PainelErro(e1);
					}			
					
					if(UtilCoffe.vaziu(campoIP.getText()))
					{
						descricaoOperacao.setText("");
						verificandoBar.setString("Verificando conexões...");
						verificandoBar.setVisible(true);
						new CheckServer(2).start();
					}
					else
					{
						verificandoBar.setString("Conectando em " + campoIP.getText());
						verificandoBar.setVisible(true);
						
						new Thread(new Runnable()
				        {
				            @Override
				            public void run()
				            {
				            	try {
									comecarLoading(UtilCoffe.CLIENT, InetAddress.getByName(campoIP.getText()), portaConnect);
								} catch (UnknownHostException e) {
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											descricaoOperacao.setText("Erro: IP Inválido.");
											verificandoBar.setVisible(false);
										}
									});
								}
				            }
				        }).start();
					}
				}
			}
		});
		iniciar.add(bTerminal, "gaptop 15, align center, split 2");
		
		bPrincipal = new WebButton("Principal");
		bPrincipal.setRolloverShine(true);
		bPrincipal.setPreferredSize(new Dimension(100, 100));
		bPrincipal.setHorizontalTextPosition(AbstractButton.CENTER);
		bPrincipal.setVerticalTextPosition(AbstractButton.BOTTOM);
		bPrincipal.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/principal.png")));
		bPrincipal.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!verificandoBar.isVisible())
				{
					try {
						SalvarStarterConfig salvarXML = new SalvarStarterConfig();
						if(UtilCoffe.vaziu(campoIP.getText()))
							salvarXML.saveConfig("none");
						else
							salvarXML.saveConfig(campoIP.getText());
					} catch (Exception e1) {
						e1.printStackTrace();
						new PainelErro(e1);
					}
					
					descricaoOperacao.setText("");
					verificandoBar.setString("Iniciando principal...");
					verificandoBar.setVisible(true);
					new CheckServer(1).start();
				}
			}
		});
		iniciar.add(bPrincipal, "gaptop 15, gapleft 20, align center, wrap");
		
		verificandoBar = new WebProgressBar();
		verificandoBar.setIndeterminate(true);
        verificandoBar.setStringPainted(true);
        verificandoBar.setString("Verificando conexões...");
        verificandoBar.setPreferredSize(new Dimension(230, 40));
        verificandoBar.setVisible(false);
        iniciar.add(verificandoBar, "gaptop 15, align center, wrap");
		
		descricaoOperacao = new JLabel("Escolha o modo de operação do programa.");
		descricaoOperacao.setFont(new Font("Verdana", Font.PLAIN, 10));
		descricaoOperacao.setPreferredSize(new Dimension(250, 50));
		iniciar.add(descricaoOperacao, "align center");		
		
		JPanel configuracoes = new JPanel(new MigLayout("alignx center, aligny center"));
		
		configuracoes.add(new JLabel("<html>IP de Conexão (Terminal)</html>"), "gaptop 10, wrap");
		
		campoIP = new WebTextField("");
		campoIP.setInputPrompt("Automático");
		campoIP.setMargin(5, 5, 5, 5);
		campoIP.setPreferredSize(new Dimension(150, 30));
		
		configuracoes.add(campoIP, "split 2");
		
		JLabel help = new JLabel(new ImageIcon(getClass().getClassLoader().getResource("imgs/help.png")));
		help.setToolTipText("<html>Digite o IP do computador que está<br>com o programa principal rodando.<br>"
				+ "O IP é mostrado no canto superior direito do programa.<br>"
				+ "Exemplo: 192.168.1.2<br>"
				+ "Deixe em branco para que o programa tente encontrar automaticamente.<br><br>"
				+ "Só altere essa configuração caso o programa não consiga encontrar<br>o IP automaticamente na rede.</html>");
		configuracoes.add(help, "wrap");
		
		selecionaPanel.addTab("Iniciar", iniciar);
		selecionaPanel.addTab("Conexão", configuracoes);
		seleciona.add(selecionaPanel);
		seleciona.setSize(350,300);
		seleciona.setLocationRelativeTo(null);
		seleciona.setIconImage(new ImageIcon(getClass().getClassLoader().getResource("imgs/icone_programa.png")).getImage());
		seleciona.setResizable(false);
		seleciona.setVisible(true);
		selecionaPanel.setFocusable(false);
		
		try {
			LerStarterConfig configXML = new LerStarterConfig();
			List<String> configSalva = configXML.readConfig();
			
			if(configSalva.size() > 0)
			{
				String ipSalvo = configSalva.get(0);
				if(!UtilCoffe.vaziu(ipSalvo))
					if(!ipSalvo.equals("none"))
						campoIP.setText(ipSalvo);
			}
			
		} catch (FileNotFoundException e) {
			try {
				SalvarStarterConfig salvarXML = new SalvarStarterConfig();
				salvarXML.saveConfig("none");
			} catch (Exception e1) {
				e1.printStackTrace();
				new PainelErro(e1);
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
			new PainelErro(e);
		}
	}
	
	class CheckServer extends Thread
	{
		private int operacao;
		private DatagramSocket socket;
		private InetAddress host;
		
		public CheckServer(int modo)
		{
			this.operacao = modo;
		}
		
		public void run()
		{
			boolean flag_procura = false;
			
			try 
			{
				this.socket = new DatagramSocket(portaConnect);
				
				try 
				{
					this.socket.setBroadcast(true);
					this.socket.setSoTimeout(5000);
					
					byte[] buf = new byte[1];
					DatagramPacket packet = new DatagramPacket(buf, buf.length);
					this.socket.receive(packet);
					
					/*if(packet.getData()[0] == -16)
						JOptionPane.showMessageDialog(null, "pacote autentico! " + packet.getAddress());
					else
						JOptionPane.showMessageDialog(null, "pacote recebido! " + packet.getAddress());*/
					
					this.host = packet.getAddress();
					flag_procura = true;
					
					if(!this.socket.isClosed())
						this.socket.close();
					
					verificandoBar.setVisible(false);	// nem precisa verificar, ctz que é cliente
					comecarLoading(this.operacao, this.host, portaConnect);	// começar o programa aqui
					
				} 
				catch (IOException e) 
				{
					if(e.getMessage().contains("Receive timed out"))	// nao recebeu nenhum sinal
					{
						if(!this.socket.isClosed())
							this.socket.close();					
						
						flag_procura = false;
						verificandoBar.setVisible(false);
						
					    if(this.operacao == 1)
					    {
					    	if(flag_procura)
					    		descricaoOperacao.setText("Erro: já existe um principal conectado!");
					    	else
					    		comecarLoading(this.operacao, this.host, portaConnect);	// começar o programa aqui
					    }
					    else
					    {
					    	if(flag_procura)
					    		comecarLoading(this.operacao, this.host, portaConnect);	// começar o programa aqui
					    	else
					    		descricaoOperacao.setText("Erro: nenhum principal conectado!");
					    }					
					}
					else
					{
						e.printStackTrace();
						verificandoBar.setVisible(false);
						descricaoOperacao.setText("Houve um erro e não foi possível verificar.");					
					}
				}				
			} 
			catch (SocketException e1) 
			{
				if(e1.getMessage().contains("Cannot bind"))	// já está ligado o servidor aqui.
				{
					flag_procura = true;
					verificandoBar.setVisible(false);
					
					if(this.operacao == 1)
					{
					   if(flag_procura)
						   descricaoOperacao.setText("Erro: já existe um principal conectado!");
					   else
					    comecarLoading(this.operacao, this.host, portaConnect);	// começar o programa aqui						 
					}
				    else
				    {
				    	if(flag_procura)
							try 
				    		{
								comecarLoading(this.operacao, InetAddress.getLocalHost(), portaConnect);
							} 
				    		catch (UnknownHostException e)
				    		{
				    			descricaoOperacao.setText("Erro: ao obter localhost!");
								e.printStackTrace();
								new PainelErro(e);
								System.exit(0);
							}
						else
				    		descricaoOperacao.setText("Erro: nenhum principal conectado!");
				    }					
				}
				else
				{
					e1.printStackTrace();
					verificandoBar.setVisible(false);
					descricaoOperacao.setText("Houve um erro e não foi possível verificar.");					
				}				
			}
		}
	}

	public static void main(String[] args) {
		new Inicio();
	}
}
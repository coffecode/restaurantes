package codecoffe.restaurantes.graficos;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import codecoffe.restaurantes.eventos.FazLogin;
import codecoffe.restaurantes.mysql.Query;
import codecoffe.restaurantes.sockets.CacheAutentica;
import codecoffe.restaurantes.sockets.Client;
import codecoffe.restaurantes.sockets.Servidor;
import codecoffe.restaurantes.utilitarios.Configuracao;
import codecoffe.restaurantes.utilitarios.DiarioLog;
import codecoffe.restaurantes.utilitarios.Usuario;
import codecoffe.restaurantes.utilitarios.UtilCoffe;

import com.alee.laf.StyleConstants;
import com.alee.laf.button.WebButton;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.rootpane.WebDialog;
import com.alee.managers.hotkey.Hotkey;
import com.alee.managers.hotkey.HotkeyManager;

public class Login extends WebDialog
{
	private static final long serialVersionUID = 1L;
	private JLabel labelUsername, labelPassword;
	private JTextField campoUsername, campoPassword;
	private WebButton bEntrar;
	private Configuracao config;
	private Object modoPrograma;
	private FazLogin loginListener;
	
	public Login(Configuracao cfg, Object modo, FazLogin listener)
	{
		config = cfg;
		modoPrograma = modo;
		loginListener = listener;
		
		setIconImage(new ImageIcon(getClass().getClassLoader().getResource("imgs/icone_programa.png")).getImage());
		setDefaultCloseOperation(WebDialog.DO_NOTHING_ON_CLOSE);
		
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{	
				if(config.getModo() == UtilCoffe.CLIENT)
				{
					((Client) modoPrograma).enviarObjeto("ADEUS");
					((Client) modoPrograma).disconnect();						
				}
				else
				{
					((Servidor) modoPrograma).terminate();
				}
				
				System.exit(0);
			}
		});				
		
		setResizable(false);
		setTitle("Login");
		setPreferredSize(new Dimension(280, 240));
		
		JPanel loginPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		WebPanel login = new WebPanel(new MigLayout());
		login.setMargin(15, 15, 15, 15);
		login.setOpaque(false);
		login.setUndecorated(false);
		login.setRound(StyleConstants.largeRound);
		
		labelUsername = new JLabel("Usuário:");
		labelUsername.setFont(new Font("Verdana", Font.BOLD, 12));
		login.add(labelUsername);
		
		campoUsername = new JTextField();
		//campoUsername.setHorizontalAlignment(SwingConstants.CENTER);
		campoUsername.setPreferredSize(new Dimension(120, 30));
		login.add(campoUsername, "gapleft 20, wrap");
		
		labelPassword = new JLabel("Senha:");
		labelPassword.setFont(new Font("Verdana", Font.BOLD, 12));
		login.add(labelPassword);
		
		campoPassword = new JPasswordField();
		//campoPassword.setHorizontalAlignment(SwingConstants.CENTER);
		campoPassword.setPreferredSize(new Dimension(120, 30));
		login.add(campoPassword, "gapleft 20, wrap");
		
		login.add(new JLabel(""), "gaptop 5");
		
		bEntrar = new WebButton("Entrar ");
		bEntrar.setRolloverShine(true);
		bEntrar.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/login.png")));
		bEntrar.setFont(new Font("Helvetica", Font.BOLD, 12));
		bEntrar.setPreferredSize(new Dimension(100, 35));
		bEntrar.setHorizontalTextPosition(AbstractButton.LEFT);			
		login.add(bEntrar, "gaptop 5, align right, wrap");
		
		login.add(new JLabel("<html><font size='2'>www.codecoffe.com.br</font></html>"), "align right, gaptop 10, span");
		
		loginPanel.add(login);
		add(loginPanel);		

		ActionListener alistener = new ActionListener ()
		{
			@Override
			public void actionPerformed ( ActionEvent e )
			{
				if(e.getSource() == bEntrar)
				{
					campoUsername.setText(campoUsername.getText().replaceAll("'", ""));
					campoPassword.setText(campoPassword.getText().replaceAll("'", ""));
					
					if(UtilCoffe.vaziu(campoUsername.getText())) {
						JOptionPane.showMessageDialog(null, "Preencha o usuário!");
					}
					else if(UtilCoffe.vaziu(campoPassword.getText())) {
						JOptionPane.showMessageDialog(null, "Preencha a senha!");
					}
					else if(campoUsername.getText().length() > 50) {
						JOptionPane.showMessageDialog(null, "Máximo de 50 caracteres no usuário!");
					}
					else if(campoPassword.getText().length() > 50) {
						JOptionPane.showMessageDialog(null, "Máximo de 50 caracteres na senha!");
					}
					else
					{
						if(config.getModo() == UtilCoffe.SERVER)
							autentica(campoUsername.getText(), campoPassword.getText());
						else
							((Client) modoPrograma).enviarObjeto(new CacheAutentica(campoUsername.getText(), campoPassword.getText()));
					}
				}
            }
		};
		
		bEntrar.addActionListener(alistener);
		HotkeyManager.registerHotkey(this, bEntrar, Hotkey.ENTER);
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	public void autentica(String username, String password)
	{
		String formatacao;
		Query teste = new Query();
		formatacao = "SELECT password, level, nome FROM funcionarios WHERE username = '" + username + "';";
		
		try {
			teste.executaQuery(formatacao);
			if(teste.next())
			{
				if(teste.getString("password").equals(password))
				{
					campoUsername.setText("");
					campoPassword.setText("");
					
					Usuario.INSTANCE.setNome(teste.getString("nome"));
					Usuario.INSTANCE.setLevel(teste.getInt("level"));
					
					teste.fechaConexao();
					DiarioLog.add(Usuario.INSTANCE.getNome(), "Fez login no sistema.", 8);
					loginListener.login(Usuario.INSTANCE.getNome());
					dispose();
				}
				else
				{
					JOptionPane.showMessageDialog(null, "Senha incorreta!");
					teste.fechaConexao();
				}
			}
			else
			{
				JOptionPane.showMessageDialog(null, "Usuário não encontrado!");
				teste.fechaConexao();
			}				
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			new PainelErro(e);
			System.exit(0);
		}	
	}
	
	public void autentica(CacheAutentica ca)
	{		
		switch(ca.getHeader())
		{
			case 1:
			{
				System.out.println("1");
				campoUsername.setText("");
				campoPassword.setText("");				
				Usuario.INSTANCE.setNome(ca.getNome());
				Usuario.INSTANCE.setLevel(ca.getLevel());
				loginListener.login(Usuario.INSTANCE.getNome());
				dispose();
				break;
			}
			case 2:
			{
				JOptionPane.showMessageDialog(null, "Usuário não encontrado!");
				break;
			}
			case 3:
			{
				JOptionPane.showMessageDialog(null, "Senha incorreta!");
				break;
			}
			default:
			{
				JOptionPane.showMessageDialog(null, "Não pode entrar com o mesmo login do Principal!");
			}
		}
	}
}

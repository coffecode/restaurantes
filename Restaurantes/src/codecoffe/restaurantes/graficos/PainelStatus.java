package codecoffe.restaurantes.graficos;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JFileChooser;

import codecoffe.restaurantes.eventos.AtualizarPainel;
import codecoffe.restaurantes.eventos.FazLogin;
import codecoffe.restaurantes.eventos.MenuSelecionado;
import codecoffe.restaurantes.mysql.Query;
import codecoffe.restaurantes.utilitarios.Configuracao;
import codecoffe.restaurantes.utilitarios.Header;
import codecoffe.restaurantes.utilitarios.JSystemFileChooser;
import codecoffe.restaurantes.utilitarios.Usuario;
import codecoffe.restaurantes.utilitarios.UtilCoffe;

import com.alee.extended.button.WebSwitch;
import com.alee.extended.layout.ToolbarLayout;
import com.alee.laf.button.WebButton;
import com.alee.laf.menu.WebMenu;
import com.alee.laf.menu.WebMenuBar;
import com.alee.laf.menu.WebMenuItem;
import com.alee.laf.rootpane.WebDialog;
import com.alee.laf.slider.WebSlider;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;
import com.alee.utils.FileUtils;

public class PainelStatus extends WebMenuBar implements ActionListener 
{
	private static final long serialVersionUID = 1L;
	private Configuracao config;
	private MenuSelecionado menuListener;
	private FazLogin loginListener;
	private WebMenu menuConfiguracoes;
	private WebMenuItem itemUserSair, itemConfGerais, itemBackup, itemFuncionarios;
	private BackupDialog menuBackup;
	protected JLabel labelUltimo;
	private Timer timerBackup;
	private AtualizarPainel painelListener;
    
    public PainelStatus(Configuracao cfg, Object modo, MenuSelecionado listener, FazLogin listenerL, AtualizarPainel listenerP)
    {
    	config = cfg;
    	menuListener = listener;
    	loginListener = listenerL;
    	painelListener = listenerP;
    	
		setMaximumSize(new Dimension(1920, 30));
		setMinimumSize(new Dimension(980, 30));
		
		itemUserSair = new WebMenuItem("Usuário", new ImageIcon(getClass().getClassLoader().getResource("imgs/usuario.png")));
		itemUserSair.addActionListener(this);
		add(itemUserSair);
		
		if(config.getModo() == UtilCoffe.SERVER)
		{
			//servidor = (Servidor) modo;
			menuConfiguracoes = new WebMenu("Configurações", new ImageIcon(getClass().getClassLoader().getResource("imgs/opcoes.png")));
			itemConfGerais  = new WebMenuItem("Gerais", new ImageIcon(getClass().getClassLoader().getResource("imgs/opcoes.png")));
			itemConfGerais.addActionListener(this);
			itemBackup  = new WebMenuItem("Backup", new ImageIcon(getClass().getClassLoader().getResource("imgs/backup_mini.png")));
			itemBackup.addActionListener(this);
			itemFuncionarios  = new WebMenuItem("Funcionários", new ImageIcon(getClass().getClassLoader().getResource("imgs/funcionarios_mini.png")));
			itemFuncionarios.addActionListener(this);					
			menuConfiguracoes.add(itemConfGerais);
			menuConfiguracoes.add(itemBackup);
			menuConfiguracoes.add(itemFuncionarios);
			add(menuConfiguracoes);
			
			try {
				InetAddress ipMaquina = InetAddress.getLocalHost();
				JLabel labelIP = new JLabel("<html><b>IP:  </b>" + ipMaquina.getHostAddress() + "</html>");
				labelIP.setPreferredSize(new Dimension(120, 20));
				add(labelIP, ToolbarLayout.END);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				JLabel labelIP = new JLabel("<html><b>IP:  </b>Indisponível</html>");
				labelIP.setPreferredSize(new Dimension(120, 20));
				add(labelIP, ToolbarLayout.END);
			}
		}
    }
    
	class BackupAutomatico extends TimerTask 
	{
        public void run() 
        {
        	long duration = System.currentTimeMillis() - config.getUltimoBackup().getTime();
        	long hours = TimeUnit.MILLISECONDS.toHours(duration);
        	
        	if(hours >= config.getBackupAutoIntervalo() && config.getBackupAutoIntervalo() >= 1)
        	{
    			if(!UtilCoffe.vaziu(config.getBackupAutoCaminho()))
    			{
    				File file = new File(config.getBackupAutoCaminho());
    				if(file.exists())
    				{
	                    SimpleDateFormat formatter = new SimpleDateFormat("dd_MM_yyyy__HH_mm");
	                    
						Process p = null;
			            try {
			                Runtime runtime = Runtime.getRuntime();
			                p = runtime.exec(System.getProperty("user.dir") + "\\mysql\\bin\\mysqldump.exe -ucodecoffe -ppuc4321 --add-drop-database -B restaurante -r " 
			                + file.getPath()
			                + "/BackupRest_" + formatter.format(new Date()) +".sql");
			                
			                int processComplete = p.waitFor();
			 
			                if (processComplete == 0) {
			                	System.out.println("Backup automático criado com sucesso!");
			                    
			        			Query envia = new Query();
			        			envia.executaUpdate("UPDATE opcoes SET ultimobackup = '" + formatter.format(new Date()) + "'");	
			        			envia.fechaConexao();
			        			
			        			config.setUltimoBackup(new Date());
			        			
			        			SwingUtilities.invokeLater(new Runnable() {
			        				@Override
			        				public void run() {
					        			SimpleDateFormat formatter2 = new SimpleDateFormat("dd/MM/yyyy - HH:mm.");
					        			labelUltimo.setText("Último backup realizado: " + formatter2.format(config.getUltimoBackup()));		
			        				}
			        			});			        			
			                    
			                } else {
			                	System.out.println("Erro, não foi possível criar o backup automático.");
			                }
			            } catch (Exception ex) {
			            	System.out.println("Erro, não foi possível criar o backup automático.");
			            	ex.printStackTrace();
			                new PainelErro(ex);
			            }	    					
    				}
    				else
    					System.out.println("Erro, não foi possível criar o backup automático (diretório inexistente).");
    			}
        	}
        }
    }    
    
    public void setNome(String nomeFuncionario)
    {
    	itemUserSair.setText(nomeFuncionario);
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == itemUserSair)
		{
			int opcao = JOptionPane.showConfirmDialog(null, "Você tem certeza que deseja sair?", "Logout", JOptionPane.YES_NO_OPTION);
			
			if(opcao == JOptionPane.YES_OPTION)
			{
				loginListener.logout();
			}	
		}
		else if(e.getSource() == itemConfGerais)
		{
			if(Usuario.INSTANCE.getLevel() > 1)
			{
				menuListener.abrirMenu("Menu Configuracao");
			}
			else
				JOptionPane.showMessageDialog(null, "Você não tem permissão para ver isso.");
		}
		else if(e.getSource() == itemBackup)
		{
			if(Usuario.INSTANCE.getLevel() > 1)
			{
				menuBackup = new BackupDialog();
				menuBackup.pack ();
				menuBackup.setLocationRelativeTo(null);
				menuBackup.setVisible(true);
			}
			else
				JOptionPane.showMessageDialog(null, "Você não tem permissão para ver isso.");
		}
		else if(e.getSource() == itemFuncionarios)
		{
			if(Usuario.INSTANCE.getLevel() > 1)
				menuListener.abrirMenu("Menu Funcionarios");
			else
				JOptionPane.showMessageDialog(null, "Você não tem permissão para ver isso.");
		}			
	}
	
	private class BackupDialog extends WebDialog
	{
		private static final long serialVersionUID = 1L;
		private JLabel labelSalvarAuto, labelBackupAuto, labelHoras, labelCria, labelRestaura;
		private WebSwitch backupAuto;
		private WebButton backupLocalButton;
		private WebSlider backupHoras;
		private JSystemFileChooser directoryChooser;
		
		public BackupDialog()
		{
			setTitle("Backup do Banco de Dados");
			JPanel backupPainel = new JPanel() {
				private static final long serialVersionUID = 1L;

				@Override
			    protected void paintComponent(Graphics g) {
			        super.paintComponent(g);
			        
			        g.setColor(new Color(196, 194, 183));
			        g.drawLine(0, 165, getWidth(), 165);
			        g.drawLine(255, 190, 255, getHeight()-40);
			    }				
				
			};
			backupPainel.setLayout(null);			
			
			setIconImage(new ImageIcon(getClass().getClassLoader().getResource("imgs/icone_programa.png")).getImage());
			setDefaultCloseOperation(WebDialog.DO_NOTHING_ON_CLOSE);
			
			addWindowListener(new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
				{
					atualizaCampos();
					dispose();
				}
			});
			
			ActionListener actionLi = new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) {
					if(e.getSource() == backupLocalButton)
					{
						directoryChooser = new JSystemFileChooser();
						directoryChooser.setDialogTitle("Escolha a pasta");
						directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						directoryChooser.setLocale(getLocale());
		                
		                int returnVal = directoryChooser.showOpenDialog(getOwner());
		                if(returnVal == JFileChooser.APPROVE_OPTION)
		                {
		                    File file = directoryChooser.getSelectedFile();
		                    backupLocalButton.setIcon(FileUtils.getFileIcon(file));
		                    backupLocalButton.setText(FileUtils.getDisplayFileName(file));
		                    
		        			try {
		        				Query envia = new Query();
								envia.executaUpdate("UPDATE opcoes SET caminhobackupauto = '" + (file.getPath().replace(File.separator, "/")) + "'");
								envia.fechaConexao();
							} catch (ClassNotFoundException | SQLException e1) {
								e1.printStackTrace();
								new PainelErro(e1);
							}
		        			finally
		        			{
		        				config.setBackupAutoCaminho(file.getPath());
		        			}
		                }					
					}
					else if(e.getSource() == backupAuto)
					{
						if(backupAuto.isSelected())
						{
				    		timerBackup = new Timer();
				    		timerBackup.schedule(new BackupAutomatico(), 30*1000, 60*1000); 							
						}
						else
						{
							timerBackup.cancel();
						}
					}
				}	
			};
			
			MouseListener mouseLi = new MouseListener()
			{
				@Override
				public void mouseClicked(MouseEvent e) {
				}

				@Override
				public void mousePressed(MouseEvent e) {
					if(e.getSource() == labelCria)
					{
						directoryChooser = new JSystemFileChooser();
						directoryChooser.setDialogTitle("Escolha a pasta");
						directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		                
		                int returnVal = directoryChooser.showOpenDialog(getOwner());
		                if(returnVal == JFileChooser.APPROVE_OPTION)
		                {
		                    File file = directoryChooser.getSelectedFile();
		                    SimpleDateFormat formatter = new SimpleDateFormat("dd_MM_yyyy__HH_mm");
		                    
							Process p = null;
				            try {
				                Runtime runtime = Runtime.getRuntime();
				                String command = System.getProperty("user.dir") + "\\mysql\\bin\\mysqldump.exe -ucodecoffe -ppuc4321 --add-drop-database -B restaurante -r ";
				                p = runtime.exec(command 
				                + file.getPath()
				                + "/BackupRest_" + formatter.format(new Date()) +".sql");
				                
				                int processComplete = p.waitFor();
				 
				                if (processComplete == 0) {
				                	TooltipManager.showOneTimeTooltip (labelCria, null, "Backup criado com sucesso!", TooltipWay.up );
				                    
				        			Query envia = new Query();
				        			envia.executaUpdate("UPDATE opcoes SET ultimobackup = '" + formatter.format(new Date()) + "'");	
				        			envia.fechaConexao();
				        			
				        			config.setUltimoBackup(new Date());
				        			
				        			SimpleDateFormat formatter2 = new SimpleDateFormat("dd/MM/yyyy - HH:mm.");
				        			labelUltimo.setText("Último backup realizado: " + formatter2.format(config.getUltimoBackup()));				        			
				                    
				                } else {
				                	TooltipManager.showOneTimeTooltip (labelCria, null, "Erro, não foi possível criar o backup.", TooltipWay.up );
				                }
				            } catch (Exception ex) {
				            	TooltipManager.showOneTimeTooltip (labelCria, null, "Erro, não foi possível criar o backup.", TooltipWay.up );
				                new PainelErro(ex);
				            }		                    
		                }
					}
					else if(e.getSource() == labelRestaura)
					{
						int opcao = JOptionPane.showConfirmDialog(null, "<html>Todo o banco de dados atual será substituido na restauração. "
								+ "<b>Não é possível reverter esta ação.</b><br><br>"
								+ "Você tem certeza que quer continuar?</html>", "Aviso", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
						
						if(opcao == JOptionPane.YES_OPTION)
						{							
							JSystemFileChooser escolherBackup = new JSystemFileChooser();
							escolherBackup.setDialogTitle("Selecione o arquivo de Backup");
							escolherBackup.setAcceptAllFileFilterUsed(false);
							FileFilter sqlType = new FileNameExtensionFilter("Backup Restaurante (.sql)", "sql");
							escolherBackup.addChoosableFileFilter(sqlType);
							escolherBackup.setFileFilter(sqlType);
							
							if(escolherBackup.showOpenDialog(getOwner()) == JFileChooser.APPROVE_OPTION)
							{
								File file = escolherBackup.getSelectedFile();
								if(file.getPath().contains(".sql"))
								{
									String[] restoreCmd = new String[]{/*System.getProperty("user.dir")*/"C:\\Users\\andre\\Desktop\\novo mysql" + "\\mysql\\bin\\mysql.exe"
											+ " ", "--user=" + "codecoffe", "--password=" + "puc4321", "-e", "source " + file.getPath()};
									
							        try {
							        	Process runtimeProcess = Runtime.getRuntime().exec(restoreCmd);
							            int processComplete = runtimeProcess.waitFor();
							 
							            if (processComplete == 0) {
							            	TooltipManager.showOneTimeTooltip(labelRestaura, null, "Banco de dados restaurado com sucesso!", TooltipWay.up );
							            	JOptionPane.showMessageDialog(null, "O programa precisa ser reiniciado para concluir a restauração.");
							            	dispose();
							            	painelListener.atualizarPainel(new Header(UtilCoffe.RELOAD));
							            } else {
							            	TooltipManager.showOneTimeTooltip(labelRestaura, null, "Erro, não foi possível restaurar o banco.", TooltipWay.up );
							            }
							        } catch (Exception ex) {
							        	new PainelErro(ex);
							        }										
								}
								else
								{
									TooltipManager.showOneTimeTooltip(labelRestaura, null, "Arquivo de Backup inválido.", TooltipWay.up );
								}
							}
						}					
					}
				}

				@Override
				public void mouseReleased(MouseEvent e) {
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					if(e.getSource() == labelCria)
					{
						labelCria.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/criar_backup_in.png")));
					}
					else if(e.getSource() == labelRestaura)
					{
						labelRestaura.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/restaurar_backup_in.png")));
					}
				}

				@Override
				public void mouseExited(MouseEvent e) {
					if(e.getSource() == labelCria)
					{
						labelCria.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/criar_backup_out.png")));
					}
					else if(e.getSource() == labelRestaura)
					{
						labelRestaura.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/restaurar_backup_out.png")));
					}					
				}
			};
			
			timerBackup = new Timer();
			
			labelBackupAuto = new JLabel("Backup Automático:");
			labelBackupAuto.setFont(new Font("Helvetica", Font.BOLD, 14));
			labelBackupAuto.setBounds(20,20,250,30); // Coluna, Linha, Largura, Altura!
			backupPainel.add(labelBackupAuto);
			
			backupAuto = new WebSwitch();
			backupAuto.setBounds(175,20,70,30);
			backupAuto.addActionListener(actionLi);
			backupPainel.add(backupAuto);	
			
			labelSalvarAuto = new JLabel("Salvar em:");
			labelSalvarAuto.setFont(new Font("Helvetica", Font.BOLD, 14));
			labelSalvarAuto.setBounds(20,60,250,30); // Coluna, Linha, Largura, Altura!
			backupPainel.add(labelSalvarAuto);
			
			backupLocalButton = new WebButton("Escolha a pasta");
			backupLocalButton.setBounds(175,60,200,30);
			backupLocalButton.addActionListener(actionLi);
			backupPainel.add(backupLocalButton);
			
			labelHoras = new JLabel("Intervalo em Horas:");
			labelHoras.setFont(new Font("Helvetica", Font.BOLD, 14));
			labelHoras.setBounds(20,100,250,30);
			backupPainel.add(labelHoras);			
			
			backupHoras = new WebSlider(WebSlider.HORIZONTAL);
			backupHoras.setMinimum(0);
			backupHoras.setMaximum(48);
			backupHoras.setMinorTickSpacing(2);
			backupHoras.setMajorTickSpacing(12);
			backupHoras.setPaintTicks(true);
			backupHoras.setPaintLabels(true);
			backupHoras.setBounds(170,110,300,45);
			backupPainel.add(backupHoras);
			
			labelCria = new JLabel(new ImageIcon(getClass().getClassLoader().getResource("imgs/criar_backup_out.png")));
			labelCria.setBounds(50, 200, 150, 100); // Coluna, Linha, Largura, Altura
			labelCria.addMouseListener(mouseLi);
			backupPainel.add(labelCria);
			
			labelRestaura = new JLabel(new ImageIcon(getClass().getClassLoader().getResource("imgs/restaurar_backup_out.png")));
			labelRestaura.setBounds(280, 200, 210, 100); // Coluna, Linha, Largura, Altura
			labelRestaura.addMouseListener(mouseLi);
			backupPainel.add(labelRestaura);			
			
			labelUltimo = new JLabel("Último backup realizado: nunca");
			labelUltimo.setFont(new Font("Verdana", Font.ITALIC, 10));
			labelUltimo.setBounds(20,330,300,20); // Coluna, Linha, Largura, Altura!
			backupPainel.add(labelUltimo);
			
			add(backupPainel);
			setPreferredSize(new Dimension(510, 390));			
			setResizable(false);
			
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy - HH:mm.");
			labelUltimo.setText("Último backup realizado: " + formatter.format(config.getUltimoBackup()));
			
			if(config.getBackupAuto())
				backupAuto.setSelected(true);	// ja inicia o timer aqui.
			
			backupHoras.setValue(config.getBackupAutoIntervalo());
			if(!UtilCoffe.vaziu(config.getBackupAutoCaminho()))
			{
				File file = new File(config.getBackupAutoCaminho());
				if(file.exists())
				{
	                backupLocalButton.setIcon(FileUtils.getFileIcon(file));
	                backupLocalButton.setText(FileUtils.getDisplayFileName(file));				
				}
				else
				{
					config.setBackupAutoCaminho("");
					backupLocalButton.setText("Escolha uma pasta");	
				}				
			}
			else
			{
				backupLocalButton.setText("Escolha uma pasta");
			}
		}

		public void atualizaCampos()
		{	
			try {
				Query envia = new Query();
				if(backupAuto.isSelected())
					envia.executaUpdate("UPDATE opcoes SET backupauto = 1");
				else
					envia.executaUpdate("UPDATE opcoes SET backupauto = 0");
				
				envia.executaUpdate("UPDATE opcoes SET intervalobackupauto = " + backupHoras.getValue());	
				envia.fechaConexao();
			} catch (ClassNotFoundException | SQLException e1) {
				e1.printStackTrace();
				new PainelErro(e1);
			}
		}
	}	
}
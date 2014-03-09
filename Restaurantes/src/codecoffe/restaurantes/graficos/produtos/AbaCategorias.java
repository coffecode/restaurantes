package codecoffe.restaurantes.graficos.produtos;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import codecoffe.restaurantes.eventos.ProdutoAlterado;
import codecoffe.restaurantes.graficos.PainelErro;
import codecoffe.restaurantes.mysql.Query;
import codecoffe.restaurantes.primitivas.Categoria;
import codecoffe.restaurantes.sockets.CacheTodosProdutos;
import codecoffe.restaurantes.sockets.Servidor;
import codecoffe.restaurantes.utilitarios.UtilCoffe;

import com.alee.laf.button.WebButton;
import com.alee.laf.text.WebTextField;
import com.alee.managers.notification.NotificationManager;

import net.miginfocom.swing.MigLayout;

public class AbaCategorias extends JPanel
{
	private Servidor servidor;
	private CacheTodosProdutos todosProdutos;
	private ProdutoAlterado produtoListener;
	private WebTextField campoTitulo;
	private WebButton bSalvar, bDeletar;
	private JLabel titulo;
	private Categoria categoriaEditando;
	private static final long serialVersionUID = 1L;

	public AbaCategorias(CacheTodosProdutos tp, Servidor svr, ProdutoAlterado listener)
	{
		produtoListener = listener;
		servidor = svr;
		todosProdutos = tp;
		
		setOpaque(false);
		setLayout(new MigLayout("aligny center, alignx center", "[]10[]", "[]35[]35[]"));
		
		titulo = new JLabel("Editando Categoria");
		titulo.setFont(new Font("Helvetica", Font.ITALIC, 16));
		add(titulo, "span, align right");
		
		add(new JLabel("Título:"));
		
		campoTitulo = new WebTextField();
		campoTitulo.setInputPrompt("Escolha o nome da categoria.");
		campoTitulo.setMargin(5, 5, 5, 5);
		campoTitulo.setPreferredSize(new Dimension(350, 40));
		add(campoTitulo, "wrap");
		
		ActionListener al = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e) {
				if(e.getSource() == bSalvar)
				{
					campoTitulo.setText((campoTitulo.getText().replaceAll("'", "")));
					
					if(campoTitulo.getText().length() > 30)
					{
						JOptionPane.showMessageDialog(null, "Máximo de 30 caracteres no título!");
					}
					else if(UtilCoffe.vaziu(campoTitulo.getText()))
					{
						JOptionPane.showMessageDialog(null, "O título precisa ser preenchido!");
					}
					else
					{
						boolean flag_salvar = false;
						try {
							Query pega = new Query();
							pega.executaQuery("SELECT id FROM categorias WHERE titulo = '" + campoTitulo.getText() + "'");
							
							if(pega.next())
							{
								if(pega.getInt("id") == categoriaEditando.getIdCategoria())
									flag_salvar = true;
								else
									flag_salvar = false;
							}
							else
								flag_salvar = true;
							
							pega.fechaConexao();
						} catch (ClassNotFoundException | SQLException e1) {
							e1.printStackTrace();
							flag_salvar = false;
							new PainelErro(e1);
						}
						
						if(categoriaEditando.getIdCategoria() == 1)
						{
							JOptionPane.showMessageDialog(null, "A categoria adicionais não pode ser alterada!");
						}
						else if(!flag_salvar)
						{
							JOptionPane.showMessageDialog(null, "Já existe uma categoria com esse nome!");
						}
						else
						{
							try {
								Query pega = new Query();
								if(categoriaEditando.getIdCategoria() == 0)
								{
									pega.executaUpdate("INSERT INTO categorias(titulo, imagem) VALUES('" + campoTitulo.getText() + "', 'none');");
									pega.executaQuery("SELECT id FROM categorias ORDER BY id DESC limit 0, 1");
									
									if(pega.next())
									{
										categoriaEditando.setIdCategoria(pega.getInt("id"));
										categoriaEditando.setTitulo(campoTitulo.getText());
										categoriaEditando.setImagem("none");
									}
									
									produtoListener.categoriaAdicionada(categoriaEditando);
									NotificationManager.setLocation(2);
									NotificationManager.showNotification(campoTitulo, "Categoria Adicionada!", 
											new ImageIcon(getClass().getClassLoader().getResource("imgs/notifications_ok.png"))).setDisplayTime(2000);
								}
								else
								{
									pega.executaUpdate("UPDATE categorias SET imagem = 'none', titulo = '" 
											+ campoTitulo.getText() + "' WHERE id = " + categoriaEditando.getIdCategoria());
									
									categoriaEditando.setTitulo(campoTitulo.getText());
									produtoListener.categoriaEditada(categoriaEditando);
									NotificationManager.setLocation(2);
									NotificationManager.showNotification(campoTitulo, "Categoria Salva!", 
											new ImageIcon(getClass().getClassLoader().getResource("imgs/notifications_ok.png"))).setDisplayTime(2000);
								}
								
								pega.fechaConexao();
								servidor.enviaTodos(todosProdutos);
							} catch (ClassNotFoundException | SQLException e1) {
								e1.printStackTrace();
								new PainelErro(e1);
							}
						}	
					}
				}
				else 
				{
					if(categoriaEditando.getIdCategoria() > 0)
					{
						boolean flag_deletar = false;
						try {
							Query pega = new Query();
							pega.executaQuery("SELECT id FROM produtos_new WHERE categoria = " + categoriaEditando.getIdCategoria());
							
							if(pega.next())
								flag_deletar = false;
							else
								flag_deletar = true;
							
							pega.fechaConexao();
						} catch (ClassNotFoundException | SQLException e1) {
							e1.printStackTrace();
							flag_deletar = false;
							new PainelErro(e1);
						}
						if(categoriaEditando.getIdCategoria() == 1)
						{
							JOptionPane.showMessageDialog(null, "A categoria adicionais não pode ser deletada.");
						}
						else if(!flag_deletar)
						{
							JOptionPane.showMessageDialog(null, "Remova todos os produtos dessa categoria antes de deleta-la.");
						}
						else
						{
							try {
								Query envia = new Query();
								envia.executaUpdate("DELETE FROM categorias WHERE id = " + categoriaEditando.getIdCategoria());
								envia.fechaConexao();
								
								produtoListener.categoriaDeletada(categoriaEditando);
								NotificationManager.setLocation(2);
								NotificationManager.showNotification(campoTitulo, "Categoria Deletada!", 
										new ImageIcon(getClass().getClassLoader().getResource("imgs/notifications_ok.png"))).setDisplayTime(2000);
								
								servidor.enviaTodos(todosProdutos);
							} catch (ClassNotFoundException | SQLException e1) {
								e1.printStackTrace();
								new PainelErro(e1);
							}
						}
					}
				}
			}
		};
		
		bSalvar = new WebButton("Salvar");
		bSalvar.setRolloverShine(true);
		bSalvar.setPreferredSize(new Dimension(100, 40));
		bSalvar.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/salvar.png")));
		bSalvar.addActionListener(al);
		add(bSalvar, "span, split 2, align right");
		
		bDeletar = new WebButton("Deletar");
		bDeletar.setRolloverShine(true);
		bDeletar.setPreferredSize(new Dimension(100, 40));
		bDeletar.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/deletecliente.png")));
		bDeletar.addActionListener(al);
		add(bDeletar, "gapleft 25px");
	}
	
	public void carregarCategoria(Categoria c)
	{
		categoriaEditando = c;
		if(categoriaEditando.getIdCategoria() > 0)
			titulo.setText("Editando Categoria");
		else
			titulo.setText("Nova Categoria");
		
		campoTitulo.setText(categoriaEditando.getTitulo());
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				campoTitulo.requestFocus();
			}
		});
	}
}
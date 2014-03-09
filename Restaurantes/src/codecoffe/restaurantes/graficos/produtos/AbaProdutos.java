package codecoffe.restaurantes.graficos.produtos;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import codecoffe.restaurantes.eventos.ProdutoAlterado;
import codecoffe.restaurantes.graficos.PainelErro;
import codecoffe.restaurantes.mysql.Query;
import codecoffe.restaurantes.primitivas.Categoria;
import codecoffe.restaurantes.primitivas.Produto;
import codecoffe.restaurantes.sockets.CacheTodosProdutos;
import codecoffe.restaurantes.sockets.Servidor;
import codecoffe.restaurantes.utilitarios.UtilCoffe;

import com.alee.laf.button.WebButton;
import com.alee.laf.text.WebTextField;
import com.alee.managers.notification.NotificationManager;

import net.miginfocom.swing.MigLayout;

public class AbaProdutos extends JPanel
{
	private Servidor servidor;
	private CacheTodosProdutos todosProdutos;
	private ProdutoAlterado produtoListener;
	private WebTextField campoNome, campoPreco, campoReferencia, campoCodigo;
	private WebButton bSalvar, bDeletar;
	private JComboBox<Object> campoCategoria;
	private CategoriaComboModel modelCategoria;
	private JLabel titulo;
	private Categoria categoriaEditando;
	private Produto produtoEditando;
	private static final long serialVersionUID = 1L;

	public AbaProdutos(CacheTodosProdutos tp, Servidor svr, ProdutoAlterado listener)
	{
		produtoListener = listener;
		servidor = svr;
		todosProdutos = tp;
		
		setOpaque(false);
		setLayout(new MigLayout("aligny center, alignx center", "[]10[]20[]10[]", "[]35[]30[]30[]40[]"));
		
		titulo = new JLabel("Editando Produto");
		titulo.setFont(new Font("Helvetica", Font.ITALIC, 16));
		add(titulo, "span, align right");
		
		add(new JLabel("Nome:"));
		campoNome = new WebTextField();
		campoNome.setInputPrompt("Nome do produto.");
		campoNome.setMargin(5, 5, 5, 5);
		campoNome.setPreferredSize(new Dimension(350, 40));
		add(campoNome);
		
		add(new JLabel("Preço:"));
		campoPreco = new WebTextField();
		campoPreco.setMargin(5, 5, 5, 5);
		campoPreco.setHorizontalAlignment(SwingConstants.CENTER);
		campoPreco.setPreferredSize(new Dimension(90, 40));
		add(campoPreco, "wrap");
		
		add(new JLabel("Referência:"));
		campoReferencia = new WebTextField();
		campoReferencia.setInputPrompt("Breve descrição do produto (aparece no recibo).");
		campoReferencia.setMargin(5, 5, 5, 5);
		campoReferencia.setPreferredSize(new Dimension(350, 40));
		add(campoReferencia);
		
		add(new JLabel("Código:"));
		campoCodigo = new WebTextField();
		campoCodigo.setMargin(5, 5, 5, 5);
		campoCodigo.setHorizontalAlignment(SwingConstants.CENTER);
		campoCodigo.setPreferredSize(new Dimension(90, 40));
		add(campoCodigo, "wrap");
		
		add(new JLabel("Categoria:"));
		campoCategoria = new JComboBox<Object>();
		modelCategoria = new CategoriaComboModel(todosProdutos.getCategorias());
		campoCategoria.setModel(modelCategoria);
		campoCategoria.setPreferredSize(new Dimension(160, 40));
		add(campoCategoria, "wrap");
		
		ActionListener al = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(e.getSource() == bSalvar)
				{
					campoReferencia.setText((campoReferencia.getText().replaceAll("'", "")));
					campoNome.setText((campoNome.getText().replaceAll("'", "")));
					
					campoCodigo.setText((UtilCoffe.limpaNumero(campoCodigo.getText())));
					campoPreco.setText((UtilCoffe.limpaNumeroDecimal(campoPreco.getText())));
					
					if(UtilCoffe.vaziu(campoNome.getText()) || UtilCoffe.vaziu(campoReferencia.getText())
							|| UtilCoffe.vaziu(campoPreco.getText()) || UtilCoffe.vaziu(campoCodigo.getText()) )
					{
						JOptionPane.showMessageDialog(null, "Todos os campos precisam estar preenchidos!");
					}
					else if(campoCodigo.getText().length() > 5)
					{
						JOptionPane.showMessageDialog(null, "Máximo de 5 caracteres para o código.");
					}
					else if(campoNome.getText().length() > 50)
					{
						JOptionPane.showMessageDialog(null, "Máximo de 50 caracteres para o nome.");
					}
					else if(campoReferencia.getText().length() > 20)
					{
						JOptionPane.showMessageDialog(null, "Máximo de 20 caracteres para a referência.");
					}
					else if(Integer.parseInt(campoCodigo.getText()) <= 0)
					{
						JOptionPane.showMessageDialog(null, "O Código precisa ser maior que 0.");
					}
					else if(UtilCoffe.precoToDouble(campoPreco.getText()) < 0 && categoriaEditando.getIdCategoria() != 1)
					{
						JOptionPane.showMessageDialog(null, "O preço só pode ser negativo para produtos da categoria Adicionais.");
					}
					else if(UtilCoffe.precoToDouble(campoPreco.getText()) == 0 || UtilCoffe.precoToDouble(campoPreco.getText()) > 99999)
					{
						JOptionPane.showMessageDialog(null, "Preço Inválido.");
					}
					else
					{
						campoReferencia.setText((campoReferencia.getText().toUpperCase()));
						campoReferencia.setText(UtilCoffe.removeAcentos(campoReferencia.getText()));
						campoPreco.setText(UtilCoffe.doubleToPreco(UtilCoffe.precoToDouble(campoPreco.getText())));
						
						int flag_salva = 0;
						try {
							Query pega = new Query();
							pega.executaQuery("SELECT id FROM produtos_new WHERE nome = '" 
									+ campoNome.getText() + "' AND id != " + produtoEditando.getIdUnico());
							
							if(!pega.next())
							{
								pega.executaQuery("SELECT id FROM produtos_new WHERE referencia = '" 
										+ campoReferencia.getText() + "' AND id != " + produtoEditando.getIdUnico());
								
								if(!pega.next())
								{
									pega.executaQuery("SELECT id FROM produtos_new WHERE codigo = " 
											+ Integer.parseInt(campoCodigo.getText()) + " AND id != " + produtoEditando.getIdUnico());
									
									if(!pega.next())
									{
										flag_salva = 0;
									}
									else
										flag_salva = 3;
								}
								else
									flag_salva = 2;
							}
							else
								flag_salva = 1;
							
							pega.fechaConexao();
						} catch (ClassNotFoundException | SQLException e1) {
							e1.printStackTrace();
							flag_salva = 1;
							new PainelErro(e1);
						}
						
						switch(flag_salva)
						{
							case 1:
							{
								JOptionPane.showMessageDialog(null, "Já existe um produto com esse nome!");
								break;
							}
							case 2:
							{
								JOptionPane.showMessageDialog(null, "Já existe um produto com essa referência!");
								break;
							}
							case 3:
							{
								JOptionPane.showMessageDialog(null, "Já existe um produto com esse código!");
								break;
							}
							default:
							{
								try {
									Query envia = new Query();
									
									if(produtoEditando.getIdUnico() == 0)
									{
										envia.executaUpdate("INSERT INTO produtos_new(nome, referencia, codigo, preco, categoria) VALUES('"
												+ campoNome.getText() + "', '"
												+ campoReferencia.getText() + "', "
												+ Integer.parseInt(campoCodigo.getText()) + ", '"
												+ campoPreco.getText() + "', "
												+ modelCategoria.getCategoriaSelecionada().getIdCategoria() + ");");
										
										envia.executaQuery("SELECT id FROM produtos_new ORDER BY id DESC limit 0, 1");
										if(envia.next())
										{
											produtoEditando.setIdUnico(envia.getInt("id"));
											produtoEditando.setNome(campoNome.getText());
											produtoEditando.setReferencia(campoReferencia.getText());
											produtoEditando.setPreco(UtilCoffe.precoToDouble(campoPreco.getText()));
											produtoEditando.setCodigo(Integer.parseInt(campoCodigo.getText()));
											
											NotificationManager.setLocation(2);
											NotificationManager.showNotification(titulo, "Produto Adicionado!", 
													new ImageIcon(getClass().getClassLoader().getResource("imgs/notifications_ok.png"))).setDisplayTime(2000);
											
											produtoListener.produtoAdicionado(produtoEditando, modelCategoria.getCategoriaSelecionada());
										}
									}
									else
									{
										envia.executaUpdate("UPDATE produtos_new SET nome = '" + campoNome.getText() + "', "
												+ "referencia = '" + campoReferencia.getText() + "', "
												+ "codigo = " + Integer.parseInt(campoCodigo.getText()) + ", "
												+ "preco = '" + campoPreco.getText() + "', "
												+ "categoria = " + modelCategoria.getCategoriaSelecionada().getIdCategoria()
												+ " WHERE id = " + produtoEditando.getIdUnico());
										
										produtoEditando.setNome(campoNome.getText());
										produtoEditando.setReferencia(campoReferencia.getText());
										produtoEditando.setPreco(UtilCoffe.precoToDouble(campoPreco.getText()));
										produtoEditando.setCodigo(Integer.parseInt(campoCodigo.getText()));
										
										NotificationManager.setLocation(2);
										NotificationManager.showNotification(titulo, "Produto Salvo!", 
												new ImageIcon(getClass().getClassLoader().getResource("imgs/notifications_ok.png"))).setDisplayTime(2000);
																			
										produtoListener.produtoEditado(produtoEditando, modelCategoria.getCategoriaSelecionada(), categoriaEditando);
									}
									
									servidor.enviaTodos(todosProdutos);
									envia.fechaConexao();
								} catch (ClassNotFoundException | SQLException e1) {
									e1.printStackTrace();
									new PainelErro(e1);
								}
							}
						}
					}
				}
				else 
				{
					int opcao = JOptionPane.showConfirmDialog(null, "Deletar produto: " + produtoEditando.getNome() 
							+ ".\n\nVocê tem certeza?\n\n", "Deletar Produto", JOptionPane.YES_NO_OPTION);

					if(opcao == JOptionPane.YES_OPTION)
					{
						try {
							Query envia = new Query();
							envia.executaUpdate("DELETE FROM produtos_new WHERE id = " + produtoEditando.getIdUnico());
							envia.fechaConexao();
							
							produtoListener.produtoDeletado(produtoEditando, categoriaEditando);
							NotificationManager.setLocation(2);
							NotificationManager.showNotification(titulo, "Produto Deletado!", 
									new ImageIcon(getClass().getClassLoader().getResource("imgs/notifications_ok.png"))).setDisplayTime(2000);
							
							servidor.enviaTodos(todosProdutos);
						} catch (ClassNotFoundException | SQLException e1) {
							e1.printStackTrace();
							new PainelErro(e1);
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
	
	public void refreshCategorias()
	{
		modelCategoria.refreshModel();
	}
	
	public void carregarProduto(Produto p, Categoria c)
	{
		produtoEditando = p;
		categoriaEditando = c;
		
		if(produtoEditando.getIdUnico() > 0)
		{
			titulo.setText("Editando Produto");
			campoCodigo.setText("" + produtoEditando.getCodigo());
			campoPreco.setText(UtilCoffe.doubleToPreco(produtoEditando.getPreco()));
		}
			
		else
		{
			titulo.setText("Novo Produto");
			campoPreco.setText("");
			campoCodigo.setText("");
		}
		
		campoNome.setText(produtoEditando.getNome());
		campoReferencia.setText(produtoEditando.getReferencia());
		
		if(categoriaEditando.getIdCategoria() > 0)
		{
			modelCategoria.setSelectedItem(categoriaEditando);
			campoCategoria.revalidate();
			campoCategoria.repaint();
		}
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				campoNome.requestFocus();
			}
		});
	}
	
	private class CategoriaComboModel extends AbstractListModel<Object> implements ComboBoxModel<Object> 
	{
		private static final long serialVersionUID = 1L;
		private List<Categoria> listCategorias;
	    private Categoria selectedCategoria;
	    private final static int FIRSTINDEX = 0;
	     
	    public CategoriaComboModel(List<Categoria> listCategorias) {
	    	this.listCategorias = listCategorias;
	        if (getSize() > 0) {
	            setSelectedItem(this.listCategorias.get(FIRSTINDEX));
	        }
	    }
	    
		public void refreshModel() {
			fireContentsChanged(this, 0, 0);
		}
	     
	    @Override
	    public Object getElementAt(int index) {
	        return listCategorias.get(index);
	    }
	 
	    @Override
	    public int getSize() {
	        return listCategorias.size();
	    }
	 
	    @Override
	    public Object getSelectedItem() {
	        return selectedCategoria;
	    }
	    
	    public Categoria getCategoriaSelecionada() {
	    	return selectedCategoria;
	    }
	 
	    @Override
	    public void setSelectedItem(Object anItem) {
	        selectedCategoria = (Categoria) anItem;
	    }
	}
}
package codecoffe.restaurantes.main;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import codecoffe.restaurantes.graficos.PainelErro;
import codecoffe.restaurantes.mysql.Query;
import codecoffe.restaurantes.primitivas.Clientes;
import codecoffe.restaurantes.primitivas.Funcionario;
import codecoffe.restaurantes.primitivas.Pedido;
import codecoffe.restaurantes.primitivas.Produto;
import codecoffe.restaurantes.primitivas.ProdutoVenda;
import codecoffe.restaurantes.primitivas.Venda;
import codecoffe.restaurantes.sockets.CacheTodasMesas;
import codecoffe.restaurantes.sockets.CacheTodosProdutos;
import codecoffe.restaurantes.utilitarios.Configuracao;
import codecoffe.restaurantes.utilitarios.UtilCoffe;

public class Recursos 
{
	private Configuracao config;
	private CacheTodosProdutos produtos;
	private List<Clientes> clientes;
	private List<Funcionario> funcionarios;
	private List<Pedido> pedidos;
	private List<Venda> mesas;
	
	public Recursos(Configuracao cfg)
	{
		config = cfg;
		produtos 		= new CacheTodosProdutos();
		clientes 		= new ArrayList<Clientes>();
		funcionarios 	= new ArrayList<Funcionario>();
		pedidos			= new ArrayList<Pedido>();
		mesas 			= new ArrayList<Venda>();
	}
	
	public void atualizarTodasMesas() {
		mesas.clear();
		
		SimpleDateFormat formataData = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss a");
		Query pega = new Query();
		for(int i = 0; i < config.getMesas(); i++)
		{			
			try {
				Venda vd = new Venda();
				ProdutoVenda p = null;
				Date data = null;
				pega.executaQuery("SELECT * FROM mesas WHERE mesas_id = " + i + ";");
				
				while(pega.next())
				{
					p = new ProdutoVenda();
					p.setIdUnico(pega.getInt("produto"));
					p.setQuantidade(pega.getInt("quantidade"), 0);
					p.setPagos(pega.getInt("pago"));
					p.setComentario(pega.getString("comentario"));
					
					if(data == null) {
						try {
							data = formataData.parse(pega.getString("data"));
						} catch (ParseException e) {
							e.printStackTrace();
							data = new Date();
						}
					}
					else {
						try {
							if(data.after(formataData.parse(pega.getString("data"))))
								data = formataData.parse(pega.getString("data"));
						} catch (ParseException e) {
							e.printStackTrace();
						}
					}
					
					String[] adcArray = pega.getString("adicionais").split("\\s+");
					Query pega1 = new Query();
					pega1.executaQuery("SELECT * FROM produtos_new WHERE id = " + p.getIdUnico());
					
					if(pega1.next())
					{
						p.setNome(pega1.getString("nome"));
						p.setPreco(UtilCoffe.precoToDouble(pega1.getString("preco")));
						p.setCodigo(pega1.getInt("codigo"));
						p.setReferencia(pega1.getString("referencia"));
						
						if(adcArray.length > 0)
						{
							for(int x = 0; x < adcArray.length; x++)
							{
								if(UtilCoffe.isNumeric(adcArray[x]) && !UtilCoffe.vaziu(adcArray[x]))
								{
									Query pega2 = new Query();
									pega2.executaQuery("SELECT * FROM produtos_new WHERE id = " + Integer.parseInt(adcArray[x]));
									if(pega2.next())
									{
										Produto adicional = new Produto(pega2.getString("nome"), pega2.getString("referencia"), 
												UtilCoffe.precoToDouble(pega2.getString("preco")), pega2.getInt("id"), pega2.getInt("codigo"));
										
										p.adicionrAdc(adicional);
									}
									pega2.fechaConexao();	
								}
							}	
						}
						
						p.calcularPreco();
						vd.adicionarProduto(p);
					}
					
					pega1.fechaConexao();			
				}
				
				if(vd.getQuantidadeProdutos() > 0)
					vd.calculaTotal();
				
				if(data != null)
					vd.setData(data);
				else
					vd.setData(new Date());
				
				mesas.add(vd);
			} catch (NumberFormatException | ClassNotFoundException | SQLException e) {
				e.printStackTrace();
				new PainelErro(e);
				System.exit(0);
			}			
		}
	}
	
	public void atualizarTodasMesas(CacheTodasMesas cacheMesas) {
		mesas.clear();
		mesas.addAll(cacheMesas.getTodasMesas());
	}
	
	public List<Venda> getMesas() {
		return mesas;
	}
	
	public Venda getMesaID(int id) {
		return mesas.get(id);
	}

	public void atualizarTodosProdutos() {
		produtos.atualizarProdutos();
	}
	
	public void atualizarTodosProdutos(CacheTodosProdutos todosProdutos) {
		produtos.getCategorias().clear();
		produtos.getCategorias().addAll(todosProdutos.getCategorias());
	}

	public CacheTodosProdutos getProdutos() {
		return produtos;
	}
	
	public void atualizarTodosClientes() {
		try {
			clientes.clear();

			Query pega = new Query();
			pega.executaQuery("SELECT * FROM fiados ORDER BY nome");

			while(pega.next())
			{
				Clientes cliente = new Clientes(pega.getInt("fiador_id"), pega.getString("nome"), 
				pega.getString("apelido"), pega.getString("telefone"), pega.getString("endereco"), 
				pega.getString("bairro"), pega.getString("complemento"), pega.getString("cpf"), 
				pega.getString("cep"), pega.getString("numero"));
				clientes.add(cliente);
			}
			
			pega.fechaConexao();
		} catch (ClassNotFoundException | SQLException e1) {
			e1.printStackTrace();
			new PainelErro(e1);
			System.exit(0);
		}
	}
	
	public void atualizarTodosClientes(List<Clientes> listaClientes) {
		clientes.clear();
		clientes.addAll(listaClientes);
	}

	public List<Clientes> getClientes() {
		return clientes;
	}
	
	public void atualizarTodosFuncionarios() {
		try {
			Query pega = new Query();
			pega.executaQuery("SELECT * FROM funcionarios ORDER BY nome");

			while(pega.next())
			{
				if(pega.getInt("level") < 3)
				{
					funcionarios.add(new Funcionario(pega.getInt("id"), pega.getInt("level"), pega.getString("username"), 
							pega.getString("password"), pega.getString("nome")));
				}
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			new PainelErro(e);
		}
	}

	public void atualizarTodosFuncionarios(List<Funcionario> funcionarios) {
		this.funcionarios.clear();
		this.funcionarios.addAll(funcionarios);
	}
	
	public List<Funcionario> getFuncionarios() {
		return funcionarios;
	}
	
	public void atualizarTodosPedidos() {
		pedidos.clear();
		
		try {
			Query pega = new Query();
			pega.executaQuery("SELECT * FROM pedidos");
			
			SimpleDateFormat formataData = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss a");
			
			while(pega.next())
			{
				ProdutoVenda produto = new ProdutoVenda();
				produto.setIdUnico(pega.getInt("produto"));
				produto.setQuantidade(pega.getInt("quantidade"), 0);
				produto.setComentario(pega.getString("observacao"));
				
				Query pega1 = new Query();
				pega1.executaQuery("SELECT * FROM produtos_new WHERE id = " + produto.getIdUnico());
				
				if(pega1.next())
				{
					produto.setNome(pega1.getString("nome"));
					produto.setReferencia(pega1.getString("referencia"));
					produto.setCodigo(pega1.getInt("codigo"));
					produto.setPreco(0);
					
					if(!UtilCoffe.vaziu(pega.getString("adicionais")))
					{
						String[] adcArray = pega.getString("adicionais").split("\\s+");
						
						if(adcArray.length > 0)
						{
							for(int x = 0; x < adcArray.length; x++)
							{
								if(UtilCoffe.isNumeric(adcArray[x]) && !UtilCoffe.vaziu(adcArray[x]))
								{
									Query pega2 = new Query();
									pega2.executaQuery("SELECT * FROM produtos_new WHERE id = " + Integer.parseInt(adcArray[x]));
									if(pega2.next())
									{
										Produto adicional = new Produto(pega2.getString("nome"), pega2.getString("referencia"), 
												UtilCoffe.precoToDouble(pega2.getString("preco")), pega2.getInt("id"), pega2.getInt("codigo"));
										
										produto.adicionrAdc(adicional);
									}
									pega2.fechaConexao();	
								}
							}	
						}
					}
					
					Date date = formataData.parse(pega.getString("data"));
					
	        		long duration = System.currentTimeMillis() - date.getTime();
	        		long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);  
	        		
	        		if(minutes > 120)	// deleta o pedidos antigos...
	        		{
	        			Query deleta = new Query();
	        			deleta.executaUpdate("DELETE FROM pedidos WHERE `id` = "+ pega.getInt("id") +";");
	        			deleta.fechaConexao();
	        		}
	        		else
	        		{
	        			pedidos.add(new Pedido(produto, date, pega.getString("atendido"), 
	    						pega.getInt("local"), pega.getInt("status"), pega.getInt("id")));	
	        		}
				}
				else	// esse produto não existe mais, então flw!
				{
        			Query deleta = new Query();
        			deleta.executaUpdate("DELETE FROM pedidos WHERE `id` = "+ pega.getInt("id") +";");
        			deleta.fechaConexao();
				}
				
				pega1.fechaConexao();
			}
			
			pega.fechaConexao();
		} catch (ClassNotFoundException | SQLException | ParseException e) {
			e.printStackTrace();
			new PainelErro(e);
			System.exit(0);
		}
	}
	
	public void atualizarTodosPedidos(List<Pedido> pedido) {
		this.pedidos.clear();
		this.pedidos.addAll(pedido);
	}

	public List<Pedido> getPedidos() {
		return pedidos;
	}
}
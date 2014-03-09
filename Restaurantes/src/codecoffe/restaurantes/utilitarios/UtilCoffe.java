package codecoffe.restaurantes.utilitarios;

import java.text.Normalizer;
import codecoffe.restaurantes.primitivas.Produto;
import codecoffe.restaurantes.primitivas.ProdutoVenda;

public abstract class UtilCoffe
{
	public static final String VERSAO = "v2.00.07";

	public static final short SERVER = 1;
	public static final short CLIENT = 2;

	public static final short PEDIDO_NORMAL = 1;
	public static final short PEDIDO_FAZENDO = 2;
	public static final short PEDIDO_REMOVER = 3;
	public static final short PEDIDO_NOVO = 4;
	public static final short PEDIDO_DELETADO = 5;
	public static final short PEDIDO_EDITAR = 6;

	public static final short PEDIDO_ADICIONA = 1;
	public static final short PEDIDO_DELETA = 2;
	public static final short PEDIDO_STATUS = 3;
	public static final short PEDIDO_EDITADO = 4;

	public static final short CLASSE_VENDA_MESA = 1;
	public static final short CLASSE_VENDA_RAPIDA = 2;
	public static final short CLASSE_CLIENTES = 3;

	public static final short MESA_ADICIONAR = 1;
	public static final short MESA_ATUALIZAR = 2;
	public static final short MESA_ATUALIZAR2 = 3;
	public static final short MESA_DELETAR = 4;
	public static final short MESA_LIMPAR = 5;
	public static final short MESA_ERROR = 6;

	public static final short CLIENTE_ADICIONAR = 1;
	public static final short CLIENTE_EDITAR = 2;
	public static final short CLIENTE_REMOVER = 3;
	public static final short CLIENTE_ATUALIZAR = 4;

	public static final short TIPO_MESA = 0;
	public static final short TIPO_COMANDA = 1;

	public static final short UPDATE_PRODUTOS = 0;
	public static final short UPDATE_VENDAS = 1;
	public static final short UPDATE_FIADOS = 2;
	public static final short ABRIR_CLIENTES = 3;
	public static final short ABRIR_MENU = 4;
	public static final short ABRIR_VENDA_RAPIDA = 5;
	public static final short UPDATE_CALLBACK = 6;
	public static final short UPDATE_LEGENDA = 7;
	public static final short UPDATE_VENDA_MESA = 8;
	public static final short RELOAD = 9;
	public static final short ENABLED = 10;
	public static final short UPDATE_FUNCIONARIOS = 11;
	public static final short UPDATE_CONFIG = 12;

	public static String limpaNumero(String campo)
	{
		String limpeza = campo.replaceAll("[^0-9]+","");
		return limpeza;
	}

	public static String limpaNumeroDecimal(String campo)
	{
		String limpeza = campo.replaceAll(",", ".");
		return limpeza.replaceAll("[^\\d.]", "");
	}
	
	public static String limpaNumeroDecimalNegativo(String campo)
	{
		if(campo.contains("-"))
		{
			String limpeza = campo.replaceAll(",", ".");
			return "-" + limpeza.replaceAll("[^\\d.]", "");
		}
		else
		{
			String limpeza = campo.replaceAll(",", ".");
			return limpeza.replaceAll("[^\\d.]", "");
		}
	}

	public static boolean vaziu(String texto)
	{
		if(texto == null)
			return true;

		if("".equals(texto.trim()))
			return true;

		return false;
	}

	public static double precoToDouble(String preco)
	{
		double doub = Double.parseDouble(preco.replaceAll(",", "."));
		return doub;
	}

	public static String doubleToPreco(double doub)
	{
		String pegaPreco = String.format("%.2f", doub);
		pegaPreco.replaceAll(",", ".");
		return pegaPreco;
	}

	public static String removeAcentos(String str) 
	{
		str = Normalizer.normalize(str, Normalizer.Form.NFD);
		str = str.replaceAll("[^\\p{ASCII}]", "");
		return str;
	}

	public static boolean isNumeric(String str)
	{
		for (char c : str.toCharArray())
		{
			if (!Character.isDigit(c)) return false;
		}
		return true;
	}

	public static ProdutoVenda cloneProdutoVenda(ProdutoVenda p)
	{
		ProdutoVenda prod = new ProdutoVenda(p.getNome(), p.getReferencia(), p.getPreco(), 
				p.getIdUnico(), p.getCodigo(), p.getAdicionaisList(), p.getQuantidade(), 
				p.getPagos(), p.getTotalProduto(), p.getComentario());

		prod.calcularPreco();
		return prod;
	}

	public static Produto cloneProduto(Produto p)
	{
		return new Produto(p.getNome(), p.getReferencia(), p.getPreco(), 
				p.getIdUnico(), p.getCodigo());
	}

	/*public static void printaMemoria()
	{
		Runtime runtime = Runtime.getRuntime();
	    NumberFormat format = NumberFormat.getInstance();

	    StringBuilder sb = new StringBuilder();
	    long maxMemory = runtime.maxMemory();
	    long allocatedMemory = runtime.totalMemory();
	    long freeMemory = runtime.freeMemory();

	    sb.append("##################################################################################\n");
	    sb.append("free memory: " + format.format(freeMemory / 1024) + "\n");
	    sb.append("allocated memory: " + format.format(allocatedMemory / 1024) + "\n");
	    sb.append("max memory: " + format.format(maxMemory / 1024) + "\n");
	    sb.append("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024) + "\n");
	    sb.append("##################################################################################\n");

	    System.out.println(sb);
	}*/

	private static final int[] pesoCPF = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};
	private static final int[] pesoCNPJ = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

	private static int calcularDigito(String str, int[] peso) {
		int soma = 0;
		for (int indice=str.length()-1, digito; indice >= 0; indice-- ) {
			digito = Integer.parseInt(str.substring(indice,indice+1));
			soma += digito*peso[peso.length-str.length()+indice];
		}
		soma = 11 - soma % 11;
		return soma > 9 ? 0 : soma;
	}

	public static boolean isValidCPF(String cpf) {
		if ((cpf==null) || (cpf.length()!=11)) return false;

		Integer digito1 = calcularDigito(cpf.substring(0,9), pesoCPF);
		Integer digito2 = calcularDigito(cpf.substring(0,9) + digito1, pesoCPF);
		return cpf.equals(cpf.substring(0,9) + digito1.toString() + digito2.toString());
	}

	public static boolean isValidCNPJ(String cnpj) {
		if ((cnpj==null)||(cnpj.length()!=14)) return false;

		Integer digito1 = calcularDigito(cnpj.substring(0,12), pesoCNPJ);
		Integer digito2 = calcularDigito(cnpj.substring(0,12) + digito1, pesoCNPJ);
		return cnpj.equals(cnpj.substring(0,12) + digito1.toString() + digito2.toString());
	}
}
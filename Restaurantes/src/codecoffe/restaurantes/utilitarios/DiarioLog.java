package codecoffe.restaurantes.utilitarios;
/*	TIPOs:
 * 	1 - Venda R�pida concluida
 * 	2 - Mesa conclu�da
 * 	3 - Altera��o nos produtos
 * 	4 - Altera��o nos funcion�rios
 * 	5 - Cadastro de cliente fiados
 * 	6 - Altera��o de uma conta fiada;
 * 	7 - Deletar uma venda;
 * 	8 - Login;
 * 	9 - Logout;
 * 10 - Gastos; */

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

import codecoffe.restaurantes.graficos.PainelErro;
import codecoffe.restaurantes.mysql.Query;

public abstract class DiarioLog 
{
	static public void add(String atendente, String acao, int tipo)
	{
		Locale locale = new Locale("pt","BR"); 
		GregorianCalendar calendar = new GregorianCalendar(); 
		SimpleDateFormat formatador = new SimpleDateFormat("dd'/'MM'/'yyyy' - 'HH':'mm",locale);		
		
		Query envia = new Query();
		
		String formatacao = "INSERT INTO diario(atendente, horario, data, acao, tipo) VALUES('";
		formatacao += atendente + "', '";
		formatacao += formatador.format(calendar.getTime()) + "', CURDATE(), '";
		formatacao += acao + "', " + tipo + ");";

		try {
			envia.executaUpdate(formatacao);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			new PainelErro(e);
		}
		finally
		{
			envia.fechaConexao();			
		}
	}
}

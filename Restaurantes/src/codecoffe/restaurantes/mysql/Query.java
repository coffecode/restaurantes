package codecoffe.restaurantes.mysql;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import codecoffe.restaurantes.graficos.PainelErro;

public class Query
{
    private Connection con = null;
    private PreparedStatement st = null;
    private ResultSet resultado = null;
	
	public void executaQuery(String query) throws ClassNotFoundException, SQLException
	{
		Class.forName("com.mysql.jdbc.Driver");
		this.con = DriverManager.getConnection("jdbc:mysql://localhost:3306/restaurante", "codecoffe", "puc4321");
		this.st = this.con.prepareStatement(query);
		this.resultado = this.st.executeQuery();
	}
	
	public void executaUpdate(String query) throws SQLException, ClassNotFoundException
	{
		Class.forName("com.mysql.jdbc.Driver");
		this.con = DriverManager.getConnection("jdbc:mysql://localhost:3306/restaurante", "codecoffe", "puc4321");
		this.st = this.con.prepareStatement(query);
		this.st.executeUpdate();
	}
	
	public int getRowCount() throws SQLException
	{
		int qntd = 0;
		this.resultado.last();
		qntd = this.resultado.getRow();
		this.resultado.beforeFirst();
		return qntd;
	}
	
	public boolean next()
	{
		try {
			if(this.resultado.next())
			{
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			new PainelErro(e);
			return false;
		}
		
		return false;
	}
	
	public String getString(String coluna)
	{
		try {
			return this.resultado.getString(coluna);
		} catch (SQLException e) {
			e.printStackTrace();
			new PainelErro(e);
			return null;
		}
	}
	
	public int getInt(String coluna)
	{
		try {
			return this.resultado.getInt(coluna);
		} catch (SQLException e) {
			e.printStackTrace();
			new PainelErro(e);
			return 0;
		}
	}
	
	public int getInt(int coluna)
	{
		try {
			return this.resultado.getInt(coluna);
		} catch (SQLException e) {
			e.printStackTrace();
			new PainelErro(e);
			return 0;
		}
	}	
	
	public void fechaConexao()
	{
        try {
            if (this.resultado != null) {
            	this.resultado.close();
            }
            if (this.st != null) {
                this.st.close();
            }
            if (this.con != null) {
                this.con.close();
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }		
	}
}
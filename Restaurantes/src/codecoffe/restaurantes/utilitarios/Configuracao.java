package codecoffe.restaurantes.utilitarios;
import java.io.Serializable;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import codecoffe.restaurantes.graficos.PainelErro;
import codecoffe.restaurantes.mysql.Query;

public class Configuracao implements Serializable
{	
	private static final long serialVersionUID = 1L;
	private int qntdMesas, modo, backupAutoIntervalo, intervaloPedido, tipoPrograma;
	private double taxaEntrega;
	private String nomeRest, caminhoBackupAuto, mensagemSuperior, mensagemInferior, impressora;
	private Date ultimoBackup;
	private boolean dezPorcento, dezPorcentoRapida, reciboFim, backupAuto, somCozinha;
	private String tipoNome = "Mesa";
	
	public void atualizarConfiguracao()
	{		
		try {
			Query pega = new Query();
			pega.executaQuery("SELECT * FROM opcoes");
			if(pega.next())
			{
				SimpleDateFormat formatter = new SimpleDateFormat("dd_MM_yyyy__HH_mm");
				
				nomeRest = pega.getString("restaurante");
				qntdMesas = pega.getInt("mesas");
				tipoPrograma = pega.getInt("tipoprograma");
				intervaloPedido = pega.getInt("intervalopedido");
				mensagemSuperior = pega.getString("msuperior");
				mensagemInferior = pega.getString("minferior");
				impressora = pega.getString("impressora");
				
				if(tipoPrograma == UtilCoffe.TIPO_MESA)
					tipoNome = "Mesa";
				else
					tipoNome = "Comanda";
				
				try {
					ultimoBackup = formatter.parse(pega.getString("ultimobackup"));
				} catch (ParseException e) {
					e.printStackTrace();
					new PainelErro(e);
					ultimoBackup = new Date();
				}
				finally
				{
					backupAutoIntervalo = pega.getInt("intervalobackupauto");
					caminhoBackupAuto = pega.getString("caminhobackupauto");
					
					if(pega.getInt("backupauto") == 1)
						backupAuto = true;
					else
						backupAuto = false;			
					
					if(pega.getInt("dezporcento") == 1)
						dezPorcento = true;
					else
						dezPorcento = false;
					
					if(pega.getInt("dezporcentorapida") == 1)
						dezPorcentoRapida = true;
					else
						dezPorcentoRapida = false;
					
					if(pega.getInt("recibofim") == 1)
						reciboFim = true;
					else
						reciboFim = false;
					
					if(pega.getInt("somcozinha") == 1)
						somCozinha = true;
					else
						somCozinha = false;
					
					taxaEntrega = UtilCoffe.precoToDouble(pega.getString("taxaentrega"));
					pega.fechaConexao();
				}
			}
			else	// não carregou nada do banco? quitar
			{
				System.exit(0);
			}
			
		} catch (ClassNotFoundException | SQLException e1) {
			e1.printStackTrace();
			new PainelErro(e1);
			System.exit(0);
		}		
	}
	
	public void atualizarConfiguracao(Configuracao cc)
	{
		nomeRest = cc.getRestaurante();
		dezPorcento = cc.getDezPorcento();
		dezPorcentoRapida = cc.isDezPorcentoRapida();
		reciboFim = cc.getReciboFim();
		taxaEntrega = cc.getTaxaEntrega();
		mensagemSuperior = cc.getMensagemSuperior();
		mensagemInferior = cc.getMensagemInferior();
		tipoPrograma = cc.getTipoPrograma();
		somCozinha = cc.isSomCozinha();
		
		if(tipoPrograma == UtilCoffe.TIPO_MESA)
			tipoNome = "Mesa";
		else
			tipoNome = "Comanda";
	}
	
	public void setModo(int md) {
		modo = md;
	}
	
	public void setBackupAutoCaminho(String caminho) {
		caminhoBackupAuto = caminho;
	}
	
	public void setUltimoBackup(Date quando) {
		ultimoBackup = quando;
	}
	
	public void setBackupAuto(boolean set) {
		backupAuto = set;
	}
	
	public void setBackupAutoIntervalo(int intervalo) {
		backupAutoIntervalo = intervalo;
	}
	
	public void setRestaurante(String texto) {
		nomeRest = texto;
	}
	
	public void setMesas(int qtd) {
		qntdMesas = qtd;
	}
	
	public void setReciboFim(boolean set) {
		reciboFim = set;
	}
	
	public void setDezPorcento(boolean set) {
		dezPorcento = set;
	}
	
	public void setTaxaEntrega(double quantia) {
		taxaEntrega = quantia;
	}
	
	public String getRestaurante() {
		return nomeRest;
	}
	
	public int getMesas() {
		return qntdMesas;
	}
	
	public boolean getReciboFim() {
		return reciboFim;
	}
	
	public boolean getDezPorcento() {
		return dezPorcento;
	}
	
	public double getTaxaEntrega() {
		return taxaEntrega;
	}
	
	public int getModo() {
		return modo;
	}
	
	public String getBackupAutoCaminho() {
		return caminhoBackupAuto;
	}
	
	public Date getUltimoBackup() {
		return ultimoBackup;
	}
	
	public boolean getBackupAuto() {
		return backupAuto;
	}
	
	public int getBackupAutoIntervalo() {
		return backupAutoIntervalo;
	}

	public int getIntervaloPedido() {
		return intervaloPedido;
	}

	public void setIntervaloPedido(int intervaloPedido) {
		this.intervaloPedido = intervaloPedido;
	}

	public int getTipoPrograma() {
		return tipoPrograma;
	}

	public void setTipoPrograma(int tipoPrograma) {
		this.tipoPrograma = tipoPrograma;
	}

	public boolean isSomCozinha() {
		return somCozinha;
	}

	public void setSomCozinha(boolean somCozinha) {
		this.somCozinha = somCozinha;
	}

	public String getMensagemSuperior() {
		return mensagemSuperior;
	}

	public void setMensagemSuperior(String mensagemSuperior) {
		this.mensagemSuperior = mensagemSuperior;
	}

	public String getMensagemInferior() {
		return mensagemInferior;
	}

	public void setMensagemInferior(String mensagemInferior) {
		this.mensagemInferior = mensagemInferior;
	}

	public String getTipoNome() {
		return tipoNome;
	}

	public void setTipoNome(String tipoNome) {
		this.tipoNome = tipoNome;
	}

	public boolean isDezPorcentoRapida() {
		return dezPorcentoRapida;
	}

	public void setDezPorcentoRapida(boolean dezPorcentoRapida) {
		this.dezPorcentoRapida = dezPorcentoRapida;
	}

	public String getImpressora() {
		return impressora;
	}

	public void setImpressora(String impressora) {
		this.impressora = impressora;
	}
}
package codecoffe.restaurantes.sockets;
import java.io.Serializable;

import codecoffe.restaurantes.primitivas.Venda;

public class CacheVendaFeita implements Serializable 
{
	private static final long serialVersionUID = 1L;
	private Venda vendaFeita, vendaTotal;
	private CacheMesaHeader vendaMesa;
	private String total, atendente, horario, forma_pagamento, valor_pago, troco, delivery, dezporcento;
	private int ano, mes, dia_mes, dia_semana, fiado_id, caixa, classe;
	
	public CacheVendaFeita(Venda v) {
		this.vendaFeita = v;
	}
	
	public CacheVendaFeita(Venda v, Venda vt, CacheMesaHeader m) {
		this.vendaFeita = v;
		this.vendaTotal = vt;
		this.vendaMesa = m;
	}	

	public Venda getVendaFeita() {
		return vendaFeita;
	}

	public void setVendaFeita(Venda vendaFeita) {
		this.vendaFeita = vendaFeita;
	}

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public String getAtendente() {
		return atendente;
	}

	public void setAtendente(String atendente) {
		this.atendente = atendente;
	}

	public String getHorario() {
		return horario;
	}

	public void setHorario(String horario) {
		this.horario = horario;
	}

	public String getForma_pagamento() {
		return forma_pagamento;
	}

	public void setForma_pagamento(String forma_pagamento) {
		this.forma_pagamento = forma_pagamento;
	}

	public String getValor_pago() {
		return valor_pago;
	}

	public void setValor_pago(String valor_pago) {
		this.valor_pago = valor_pago;
	}

	public String getTroco() {
		return troco;
	}

	public void setTroco(String troco) {
		this.troco = troco;
	}

	public String getDelivery() {
		return delivery;
	}

	public void setDelivery(String delivery) {
		this.delivery = delivery;
	}

	public String getDezporcento() {
		return dezporcento;
	}

	public void setDezporcento(String dezporcento) {
		this.dezporcento = dezporcento;
	}

	public int getAno() {
		return ano;
	}

	public void setAno(int ano) {
		this.ano = ano;
	}

	public int getMes() {
		return mes;
	}

	public void setMes(int mes) {
		this.mes = mes;
	}

	public int getDia_mes() {
		return dia_mes;
	}

	public void setDia_mes(int dia_mes) {
		this.dia_mes = dia_mes;
	}

	public int getDia_semana() {
		return dia_semana;
	}

	public void setDia_semana(int dia_semana) {
		this.dia_semana = dia_semana;
	}

	public int getFiado_id() {
		return fiado_id;
	}

	public void setFiado_id(int fiado_id) {
		this.fiado_id = fiado_id;
	}

	public int getCaixa() {
		return caixa;
	}

	public void setCaixa(int caixa) {
		this.caixa = caixa;
	}

	public int getClasse() {
		return classe;
	}

	public void setClasse(int classe) {
		this.classe = classe;
	}

	public Venda getVendaTotal() {
		return vendaTotal;
	}

	public void setVendaTotal(Venda vendaTotal) {
		this.vendaTotal = vendaTotal;
	}

	public CacheMesaHeader getVendaMesa() {
		return vendaMesa;
	}

	public void setVendaMesa(CacheMesaHeader vendaMesa) {
		this.vendaMesa = vendaMesa;
	}
}

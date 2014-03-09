package codecoffe.restaurantes.primitivas;

import java.io.Serializable;

import codecoffe.restaurantes.utilitarios.UtilCoffe;

public class Clientes implements Serializable
{
	private static final long serialVersionUID = 1L;
	private int idUnico;
	private String nome, apelido, telefone, endereco, 
	bairro, complemento, cpf, cep, numero;

	public Clientes(int idUnico, String nome, String apelido, String telefone,
			String endereco, String bairro, String complemento, String cpf,
			String cep, String numero) {
		
		this.idUnico = idUnico;
		this.nome = nome;
		this.apelido = apelido;
		this.telefone = telefone;
		this.endereco = endereco;
		this.bairro = bairro;
		this.complemento = complemento;
		this.cpf = cpf;
		this.cep = cep;
		this.numero = numero;
	}
	
	public Clientes()
	{
		this.idUnico = 0;
		this.nome = "";
		this.apelido = "";
		this.telefone = "";
		this.endereco = "";
		this.bairro = "";
		this.complemento = "";
		this.cpf = "";
		this.cep = "";
		this.numero = "";		
	}
	
	public boolean containCliente(String texto)
	{
		if(!UtilCoffe.vaziu(this.nome))
			if(this.nome.toLowerCase().contains(texto.toLowerCase()))
				return true;
		
		if(!UtilCoffe.vaziu(this.apelido))
			if(this.apelido.toLowerCase().contains(texto.toLowerCase()))
				return true;
		
		if(!UtilCoffe.vaziu(this.telefone))
			if(this.telefone.toLowerCase().contains(texto.toLowerCase()))
				return true;
		
		if(!UtilCoffe.vaziu(this.endereco))
			if(this.endereco.toLowerCase().contains(texto.toLowerCase()))
				return true;
		
		if(!UtilCoffe.vaziu(this.bairro))
			if(this.bairro.toLowerCase().contains(texto.toLowerCase()))
				return true;
		
		if(!UtilCoffe.vaziu(this.complemento))
			if(this.complemento.toLowerCase().contains(texto.toLowerCase()))
				return true;
		
		if(!UtilCoffe.vaziu(this.cpf))
			if(this.cpf.toLowerCase().contains(texto.toLowerCase()))
				return true;
		
		if(!UtilCoffe.vaziu(this.cep))
			if(this.cep.toLowerCase().contains(texto.toLowerCase()))
				return true;
		
		if(!UtilCoffe.vaziu(this.numero))
			if(this.numero.toLowerCase().contains(texto.toLowerCase()))
				return true;	
		
		return false;
	}

	public int getIdUnico() {
		return idUnico;
	}

	public void setIdUnico(int idUnico) {
		this.idUnico = idUnico;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getApelido() {
		return apelido;
	}

	public void setApelido(String apelido) {
		this.apelido = apelido;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public String getEndereco() {
		return endereco;
	}

	public void setEndereco(String endereco) {
		this.endereco = endereco;
	}

	public String getBairro() {
		return bairro;
	}

	public void setBairro(String bairro) {
		this.bairro = bairro;
	}

	public String getComplemento() {
		return complemento;
	}

	public void setComplemento(String complemento) {
		this.complemento = complemento;
	}

	public String getCpf() {
		return cpf;
	}

	public void setCpf(String cpf) {
		this.cpf = cpf;
	}

	public String getCep() {
		return cep;
	}

	public void setCep(String cep) {
		this.cep = cep;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}
}
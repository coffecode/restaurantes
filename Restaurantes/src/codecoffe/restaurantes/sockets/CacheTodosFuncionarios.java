package codecoffe.restaurantes.sockets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import codecoffe.restaurantes.primitivas.Funcionario;

public class CacheTodosFuncionarios implements Serializable
{
	private static final long serialVersionUID = 1L;
	private List<Funcionario> funcionarios = new ArrayList<Funcionario>();
	
	public CacheTodosFuncionarios(List<Funcionario> funcionarios) {
		this.funcionarios = funcionarios;
	}
	
	public CacheTodosFuncionarios() {}
	
	public List<Funcionario> getFuncionarios() {
		return funcionarios;
	}
	public void setFuncionarios(List<Funcionario> funcionarios) {
		this.funcionarios = funcionarios;
	}
}

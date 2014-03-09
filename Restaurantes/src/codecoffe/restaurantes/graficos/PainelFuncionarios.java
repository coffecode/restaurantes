package codecoffe.restaurantes.graficos;
import java.awt.*;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import java.sql.SQLException;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;

import com.alee.laf.panel.WebPanel;
import com.alee.laf.text.WebTextField;
import com.alee.managers.notification.NotificationManager;

import net.miginfocom.swing.MigLayout;
import codecoffe.restaurantes.sockets.CacheTodosFuncionarios;
import codecoffe.restaurantes.sockets.Servidor;
import codecoffe.restaurantes.eventos.AtualizarPainel;
import codecoffe.restaurantes.mysql.Query;
import codecoffe.restaurantes.primitivas.Funcionario;
import codecoffe.restaurantes.utilitarios.DiarioLog;
import codecoffe.restaurantes.utilitarios.Header;
import codecoffe.restaurantes.utilitarios.Usuario;
import codecoffe.restaurantes.utilitarios.UtilCoffe;

public class PainelFuncionarios extends JPanel implements TableModelListener
{
	private static final long serialVersionUID = 1L;
	private DefaultTableModel tabelaModel;
	private JTable tabelaFuncionarios;
	private WebPanel addPainel;
	private Servidor servidor;
	private List<Funcionario> funcionarios;
	private AtualizarPainel painelListener;

	public PainelFuncionarios(List<Funcionario> func, Servidor svr, AtualizarPainel listener)
	{
		servidor = svr;
		funcionarios = func;
		painelListener = listener;
		
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Gerenciar Funcionários"));
		setLayout(new MigLayout("fill"));

		tabelaModel = new DefaultTableModel() {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int column) {
				if(column == 1 || column == 2 || column == 3)
					return true;
				
				String pega = tabelaModel.getValueAt(row, 1).toString();
				if(pega.equals(Usuario.INSTANCE.getNome()))
					return false;

				return true;
			}
		};

		tabelaModel.addColumn("ID");
		tabelaModel.addColumn("Nome");
		tabelaModel.addColumn("Usuário");
		tabelaModel.addColumn("Senha");
		tabelaModel.addColumn("Cargo");
		tabelaModel.addColumn("Deletar");
		
		for(int i = 0; i < funcionarios.size(); i++)
		{
			Vector<String> linha = new Vector<String>();

			linha.add("" + funcionarios.get(i).getIdUnico());
			linha.add(funcionarios.get(i).getNome());
			linha.add(funcionarios.get(i).getUsuario());
			linha.add(funcionarios.get(i).getPassword());

			if(funcionarios.get(i).getLevel() < 2)
				linha.add("Funcionário");
			else
				linha.add("Gerente");

			linha.add("");
			tabelaModel.addRow(linha);
		}

		String[] tiposFuncionario = {"Funcionário", "Gerente"};

		tabelaFuncionarios = new JTable();
		tabelaFuncionarios.setModel(tabelaModel);
		tabelaFuncionarios.getColumnModel().getColumn(0).setMinWidth(0);
		tabelaFuncionarios.getColumnModel().getColumn(0).setMaxWidth(0);
		tabelaFuncionarios.getColumnModel().getColumn(1).setPreferredWidth(270);
		tabelaFuncionarios.getColumnModel().getColumn(2).setPreferredWidth(250);
		tabelaFuncionarios.getColumnModel().getColumn(3).setPreferredWidth(250);	
		tabelaFuncionarios.getColumnModel().getColumn(4).setMinWidth(140);
		tabelaFuncionarios.getColumnModel().getColumn(4).setMaxWidth(180);
		tabelaFuncionarios.getColumnModel().getColumn(5).setMinWidth(60);
		tabelaFuncionarios.getColumnModel().getColumn(5).setMaxWidth(60);
		tabelaFuncionarios.getColumn("Cargo").setCellRenderer(new MyComboBoxRenderer(tiposFuncionario));
		tabelaFuncionarios.getColumn("Cargo").setCellEditor(new MyComboBoxEditor(tiposFuncionario));
		tabelaFuncionarios.getColumn("Deletar").setCellEditor(new ButtonEditor(new JCheckBox()));
		tabelaFuncionarios.setRowHeight(30);
		tabelaFuncionarios.getTableHeader().setReorderingAllowed(false);
		tabelaFuncionarios.setFocusable(false);
		tabelaFuncionarios.setDefaultRenderer(Object.class, new TabelaFuncionariosRenderer());
		tabelaFuncionarios.getModel().addTableModelListener(this);

		JScrollPane scrolltabela = new JScrollPane(tabelaFuncionarios);
		scrolltabela.getViewport().setFocusable(false);
		scrolltabela.getViewport().setBackground(new Color(237, 237, 237));
		add(scrolltabela, "grow, wrap");

		addPainel = new WebPanel(new MigLayout("aligny center, alignx center", "[]20[]20[]", "[]15[]15[]"));
		addPainel.setMargin(10, 10, 10, 10);
		addPainel.setUndecorated(false);

		final JComboBox<String> campoLevel = new JComboBox<String>(tiposFuncionario);
		campoLevel.setSelectedIndex(0);
		campoLevel.setPreferredSize(new Dimension(150, 40));

		final WebTextField campoNome = new WebTextField();
		campoNome.setPreferredSize(new Dimension(150, 40));
		campoNome.setMargin(5, 5, 5, 5);

		final WebTextField campoUser = new WebTextField();
		campoUser.setPreferredSize(new Dimension(150, 40));
		campoUser.setMargin(5, 5, 5, 5);

		final WebTextField campoSenha = new WebTextField();
		campoSenha.setPreferredSize(new Dimension(150, 40));
		campoSenha.setMargin(5, 5, 5, 5);

		final JButton adicionar = new JButton("Adicionar");
		adicionar.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/plus2.png")));
		adicionar.setPreferredSize(new Dimension(150, 40));
		adicionar.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				campoNome.setText((campoNome.getText().replaceAll("'", "")));
				campoUser.setText((campoUser.getText().replaceAll("'", "")));
				campoSenha.setText((campoSenha.getText().replaceAll("'", "")));

				if(UtilCoffe.vaziu(campoNome.getText())) {
					JOptionPane.showMessageDialog(null, "Digite o nome do funcionário!");
				}
				else if(UtilCoffe.vaziu(campoUser.getText())) {
					JOptionPane.showMessageDialog(null, "Digite o usuário do funcionário!");
				}
				else if(UtilCoffe.vaziu(campoSenha.getText())) {
					JOptionPane.showMessageDialog(null, "Digite a senha do funcionário!");
				}
				else if(campoNome.getText().length() > 30) {
					JOptionPane.showMessageDialog(null, "Máximo de 30 caracteres no nome!");
				}
				else if(campoUser.getText().length() > 20) {
					JOptionPane.showMessageDialog(null, "Máximo de 20 caracteres no usuário!");
				}
				else if(campoSenha.getText().length() > 10) {
					JOptionPane.showMessageDialog(null, "Máximo de 10 caracteres na senha!");
				}
				else
				{
					int level = 1;
					if(campoLevel.getSelectedIndex() == 0)
						level = 1;
					else
						level = 2;

					try {
						Query envia = new Query();
						envia.executaUpdate("INSERT INTO funcionarios(username, password, level, nome) VALUES('"
								+ campoUser.getText() +
								"', '" + campoSenha.getText() +
								"', " + level + ", '" + campoNome.getText() + "');");

						DiarioLog.add(Usuario.INSTANCE.getNome(), "Adicionou o funcionário " + campoUser.getText() + ".", 4);

						int novo_id = 0;

						envia.executaQuery("SELECT id FROM funcionarios ORDER BY id DESC limit 0, 1");
						if(envia.next()) {
							novo_id = envia.getInt("id");
						}

						envia.fechaConexao();

						NotificationManager.setLocation(2);
						NotificationManager.showNotification(tabelaFuncionarios, "Funcionário Adicionado!",
								new ImageIcon(getClass().getClassLoader().getResource("imgs/notifications_ok.png"))).setDisplayTime(2000);

						funcionarios.add(new Funcionario(novo_id, level, campoUser.getText(), campoSenha.getText(), campoNome.getText()));						
						Object[] linha = {novo_id, campoNome.getText(), campoUser.getText(), 
								campoSenha.getText(), campoLevel.getSelectedItem().toString(), ""};
						tabelaModel.addRow(linha);
						
						painelListener.atualizarPainel(new Header(UtilCoffe.UPDATE_FUNCIONARIOS));
						servidor.enviaTodos(new CacheTodosFuncionarios(funcionarios));

						campoNome.setText("");
						campoUser.setText("");
						campoSenha.setText("");
						campoNome.requestFocus();
					} catch (ClassNotFoundException | SQLException e1) {
						e1.printStackTrace();
						new PainelErro(e1);
					}
				}
			}
		});

		addPainel.add(new JLabel("Nome:"));
		addPainel.add(campoNome);
		addPainel.add(campoLevel, "wrap");

		addPainel.add(new JLabel("Usuário:"));
		addPainel.add(campoUser, "wrap");

		addPainel.add(new JLabel("Senha:"));
		addPainel.add(campoSenha);
		addPainel.add(adicionar);

		add(addPainel, "grow");
	}
	
	public CacheTodosFuncionarios getTodosFuncionarios() {
		return new CacheTodosFuncionarios(funcionarios);
	}

	private class TabelaFuncionariosRenderer extends DefaultTableCellRenderer
	{
		private static final long serialVersionUID = 1L;
		private Color alternate = new Color(206, 220, 249);
		private ImageIcon iconDelete = new ImageIcon(getClass().getClassLoader().getResource("imgs/delete.png"));
		private JButton bDeletar = new JButton(iconDelete);

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, 
				boolean isSelected, boolean hasFocus, int row, int column) {
			JLabel cellComponent = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			if((tabelaModel.getColumnCount() - 1) == column) {
				return bDeletar;
			}

			if(isSelected) {
				cellComponent.setBackground(table.getSelectionBackground());
			}
			else if(row % 2 == 0) {
				cellComponent.setBackground(alternate);
			}
			else {
				cellComponent.setBackground(Color.WHITE);
			}

			setHorizontalAlignment(JLabel.CENTER);
			return cellComponent;
		}
	}

	class MyComboBoxRenderer extends JComboBox<String> implements TableCellRenderer 
	{
		private static final long serialVersionUID = 1L;
		public MyComboBoxRenderer(String[] items) {
			super(items);
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
				boolean hasFocus, int row, int column) {
			if (isSelected) {
				setForeground(new Color(72, 61, 139));
				super.setBackground(table.getSelectionBackground());
			} else {
				setForeground(Color.BLACK);
				setBackground(table.getBackground());
			}
			setSelectedItem(value);
			return this;
		}
	}

	class MyComboBoxEditor extends DefaultCellEditor 
	{
		private static final long serialVersionUID = 1L;
		public MyComboBoxEditor(String[] items) {
			super(new JComboBox<String>(items));
		}
	}

	class ButtonEditor extends DefaultCellEditor 
	{
		private static final long serialVersionUID = 1L;
		protected JButton button;
		private boolean isPushed;
		private ImageIcon iconDelete;

		public ButtonEditor(JCheckBox checkBox) {
			super(checkBox);
			iconDelete = new ImageIcon(getClass().getClassLoader().getResource("imgs/delete.png"));
			button = new JButton(iconDelete);
			button.setOpaque(true);
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					fireEditingStopped();
				}
			});
		}

		public Component getTableCellEditorComponent(JTable table, Object value,
				boolean isSelected, int row, int column) {
			if (isSelected) {
				button.setForeground(table.getSelectionForeground());
				button.setBackground(table.getSelectionBackground());
			} else {
				button.setForeground(table.getForeground());
				button.setBackground(table.getBackground());
			}

			isPushed = true;
			return button;
		}

		public Object getCellEditorValue() {
			if (isPushed) {
				if(tabelaFuncionarios.getSelectedRowCount() == 1)
				{
					try {
						int pega = Integer.parseInt(tabelaFuncionarios.getValueAt(tabelaFuncionarios.getSelectedRow(), 0).toString());
						Query envia = new Query();
						envia.executaUpdate("DELETE FROM funcionarios WHERE `id` = " + pega);
						envia.fechaConexao();
						DiarioLog.add(Usuario.INSTANCE.getNome(), "Deletou o funcionário " + 
								tabelaFuncionarios.getValueAt(tabelaFuncionarios.getSelectedRow(), 1).toString() + ".", 4);

						NotificationManager.setLocation(2);
						NotificationManager.showNotification(tabelaFuncionarios, "Funcionário Deletado!",
								new ImageIcon(getClass().getClassLoader().getResource("imgs/notifications_ok.png"))).setDisplayTime(2000);

						funcionarios.remove(tabelaFuncionarios.getSelectedRow());
						painelListener.atualizarPainel(new Header(UtilCoffe.UPDATE_FUNCIONARIOS));
						servidor.enviaTodos(new CacheTodosFuncionarios(funcionarios));

						SwingUtilities.invokeLater(new Runnable() {  
							public void run() {  
								tabelaModel.removeRow(tabelaFuncionarios.getSelectedRow());
							}  
						});
					} catch (ClassNotFoundException | SQLException e) {
						e.printStackTrace();
						new PainelErro(e);
					}
				}
			}
			isPushed = false;
			return "";
		}

		public boolean stopCellEditing() {
			isPushed = false;
			return super.stopCellEditing();
		}

		protected void fireEditingStopped() {
			super.fireEditingStopped();
		}
	}

	@Override
	public void tableChanged(TableModelEvent e) 
	{
		int row = e.getFirstRow();
		TableModel model = (TableModel)e.getSource();

		if(e.getColumn() > 0 && e.getColumn() < 5)
		{
			if(model.getValueAt(row, e.getColumn()).toString().contains("'")) {
				model.setValueAt((model.getValueAt(row, e.getColumn()).toString().replaceAll("'", "")), row, e.getColumn());
			}
			else
			{
				if(UtilCoffe.vaziu((model.getValueAt(row, 1).toString().replaceAll("'", ""))) 
						|| model.getValueAt(row, 1).toString().length() > 30)
				{
					model.setValueAt(funcionarios.get(row).getNome(), row, 1);
					JOptionPane.showMessageDialog(null, "Nome inválido!");
				}
				else if(UtilCoffe.vaziu((model.getValueAt(row, 2).toString().replaceAll("'", ""))) 
						|| model.getValueAt(row, 2).toString().length() > 20)
				{
					model.setValueAt(funcionarios.get(row).getUsuario(), row, 2);
					JOptionPane.showMessageDialog(null, "Usuário inválido!");
				}
				else if(UtilCoffe.vaziu((model.getValueAt(row, 3).toString().replaceAll("'", ""))) 
						|| model.getValueAt(row, 3).toString().length() > 10)
				{
					model.setValueAt(funcionarios.get(row).getPassword(), row, 3);
					JOptionPane.showMessageDialog(null, "Senha inválida!");
				}
				else
				{
					try {
						Query envia = new Query();

						int lvl = 0;
						if(model.getValueAt(row, 4).toString().equals("Gerente")) {
							lvl = 2;
						}
						else {
							lvl = 1;
						}

						envia.executaUpdate("UPDATE funcionarios SET nome = '" + model.getValueAt(row, 1).toString() + "', "
								+ "username = '" + model.getValueAt(row, 2).toString() + "', "
								+ "password = '" + model.getValueAt(row, 3).toString() + "', "
								+ "level = " + lvl + " WHERE id = " + model.getValueAt(row, 0));
						envia.fechaConexao();

						DiarioLog.add(Usuario.INSTANCE.getNome(), "Atualizou o funcionário " + funcionarios.get(row).getNome() + ".", 4);

						funcionarios.get(row).setLevel(lvl);
						funcionarios.get(row).setNome(model.getValueAt(row, 1).toString());
						funcionarios.get(row).setUsuario(model.getValueAt(row, 2).toString());
						funcionarios.get(row).setPassword(model.getValueAt(row, 3).toString());
						
						painelListener.atualizarPainel(new Header(UtilCoffe.UPDATE_FUNCIONARIOS));
						servidor.enviaTodos(new CacheTodosFuncionarios(funcionarios));
					} catch (ClassNotFoundException | SQLException e1) {
						e1.printStackTrace();
						new PainelErro(e1);
					}
				}
			}	
		}
	}
}
package codecoffe.restaurantes.graficos;
import java.awt.*;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import net.miginfocom.swing.MigLayout;
import codecoffe.restaurantes.eventos.AtualizarPainel;
import codecoffe.restaurantes.graficos.produtos.ProdutosComboBox;
import codecoffe.restaurantes.mysql.Query;
import codecoffe.restaurantes.primitivas.Clientes;
import codecoffe.restaurantes.primitivas.Funcionario;
import codecoffe.restaurantes.primitivas.Pedido;
import codecoffe.restaurantes.primitivas.ProdutoVenda;
import codecoffe.restaurantes.primitivas.Venda;
import codecoffe.restaurantes.sockets.CacheAviso;
import codecoffe.restaurantes.sockets.CacheImpressao;
import codecoffe.restaurantes.sockets.CacheMesaHeader;
import codecoffe.restaurantes.sockets.CacheTodosProdutos;
import codecoffe.restaurantes.sockets.CacheVendaFeita;
import codecoffe.restaurantes.sockets.Client;
import codecoffe.restaurantes.sockets.Servidor;
import codecoffe.restaurantes.utilitarios.Configuracao;
import codecoffe.restaurantes.utilitarios.DiarioLog;
import codecoffe.restaurantes.utilitarios.FocusTraversal;
import codecoffe.restaurantes.utilitarios.Header;
import codecoffe.restaurantes.utilitarios.Recibo;
import codecoffe.restaurantes.utilitarios.Usuario;
import codecoffe.restaurantes.utilitarios.UtilCoffe;

import com.alee.extended.painter.DashedBorderPainter;
import com.alee.extended.painter.TitledBorderPainter;
import com.alee.laf.button.WebButton;
import com.alee.laf.combobox.WebComboBox;
import com.alee.laf.panel.WebPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.text.WebTextField;
import com.alee.managers.notification.NotificationManager;
import com.alee.managers.tooltip.TooltipManager;
import com.alee.managers.tooltip.TooltipWay;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceMotionListener;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PainelVendaMesa extends JPanel implements ActionListener, FocusListener, ItemListener
{
	private static final long serialVersionUID = 1L;
	private JPanel pedidoPainel, painelProdutos, painelProdutos1, painelPagamento;
	private JLabel labelQuantidade, labelValor, labelTotal, labelRecebido, labelTroco, labelForma, labelCliente;
	private JTabbedPane divisaoPainel;
	private JButton calcular;
	private VendasTableModel tabelaModel;
	private JTable tabelaPedido;
	private WebTextField campoComentario;
	private JTextField campoTotal, campoRecebido, campoTroco;
	private JTextField campoValor;
	private JTextField campoQuantidade;
	private ProdutosComboBox addProduto;
	private ArrayList<ProdutosComboBox> addAdicional;
	private ArrayList<JButton> addRemover;
	private Clientes clienteVenda;
	private WebPanel adicionaisPainel, adicionaisPainel1;
	private WebButton adicionarProduto, finalizarVenda, imprimir, flecha1, flecha2, escolherCliente, deletarCliente;
	private JEditorPane campoRecibo;
	private WebComboBox campoForma;
	private JComboBox<Object> campoFuncionario;
	private FuncionariosComboModel funcionarioModel;
	private DragPanel painelDropIn, painelDropOut;
	private int mesaID = 0;
	private ImageIcon iconeFinalizar;
	private JCheckBox adicionarDezPorcento;
	private double taxaOpcional;
	private CacheTodosProdutos todosProdutos;
	private CacheAviso aviso;
	private Configuracao config;
	private Object modoPrograma;
	private AtualizarPainel painelListener;
	private PainelMesas mesaListener;
	private List<Pedido> todosPedidos;
	private Venda vendaMesa;

	@SuppressWarnings("rawtypes")
	public PainelVendaMesa(Configuracao cfg, Object modo, CacheTodosProdutos produtos, 
			List<Funcionario> funs, List<Pedido> pedidos, AtualizarPainel listener, PainelMesas mesal) {
		
		config = cfg;
		modoPrograma = modo;
		todosProdutos = produtos;
		todosPedidos = pedidos;
		painelListener = listener;
		mesaListener = mesal;
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		iconeFinalizar = new ImageIcon(getClass().getClassLoader().getResource("imgs/finalizar.png"));
		divisaoPainel = new JTabbedPane();
		divisaoPainel.setFocusable(false);

		painelProdutos = new JPanel(new MigLayout("align center", "[]15[]15[]15[]15[]15[]15[]"));
		painelProdutos.setMinimumSize(new Dimension(1020, 260));
		painelProdutos.setMaximumSize(new Dimension(1920, 450));

		labelValor = new JLabel("Preço:");
		labelValor.setFont(new Font("Helvetica", Font.BOLD, 16));
		labelQuantidade = new JLabel("Qntd:");
		labelQuantidade.setFont(new Font("Helvetica", Font.BOLD, 16));

		campoValor = new JTextField(5);
		campoQuantidade = new JTextField("1", 2);
		addAdicional = new ArrayList<>();
		addRemover = new ArrayList<>();

		campoValor = new JTextField("");
		campoValor.setEditable(false);
		campoValor.setHorizontalAlignment(SwingConstants.CENTER);
		campoValor.setPreferredSize(new Dimension(85, 35));
		campoValor.setFocusable(false);
		campoQuantidade = new JTextField("1");
		campoQuantidade.setHorizontalAlignment(SwingConstants.CENTER);
		campoQuantidade.setPreferredSize(new Dimension(40, 35));
		addProduto = new ProdutosComboBox(todosProdutos.getCategorias(), 1);
		addProduto.setPreferredSize(new Dimension(350, 110));
		addProduto.addActionListener(this);

		flecha1 = new WebButton("");
		flecha1.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/arrow1.png")));
		flecha1.setToolTipText("Move todos para mesa.");
		flecha1.setPreferredSize(new Dimension(50, 50));
		flecha1.setUndecorated(true);
		flecha1.addActionListener(this);

		flecha2 = new WebButton("");
		flecha2.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/arrow2.png")));
		flecha2.setToolTipText("Move todos para pagando.");
		flecha2.setPreferredSize(new Dimension(50, 50));
		flecha2.setUndecorated(true);
		flecha2.addActionListener(this);		

		campoComentario = new WebTextField();
		campoComentario.setMargin(5, 5, 5, 5);
		campoComentario.setInputPrompt("Comentário p/ Cozinha");
		campoComentario.setPreferredSize(new Dimension(290, 35));

		adicionarProduto = new WebButton("Adicionar Produto");
		adicionarProduto.setFont(new Font("Helvetica", Font.BOLD, 14));
		adicionarProduto.setRolloverShine(true);
		adicionarProduto.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/produtos_add.png")));
		adicionarProduto.setHorizontalTextPosition(AbstractButton.CENTER);
		adicionarProduto.setVerticalTextPosition(AbstractButton.BOTTOM);
		adicionarProduto.setPreferredSize(new Dimension(150, 90));
		adicionarProduto.addActionListener(this);

		adicionaisPainel1 = new WebPanel();
		adicionaisPainel1.setLayout(new MigLayout());
		adicionaisPainel = new WebPanel();
		adicionaisPainel.setLayout(new MigLayout());
		
		DashedBorderPainter<JComponent> bp4 = new DashedBorderPainter<>(new float[]{ 6f, 10f });
		bp4.setRound(2);
		bp4.setWidth(4);
		bp4.setColor(new Color( 205, 205, 205 ));
		
		painelProdutos.add(addProduto, "cell 1 0, span 4");
		painelProdutos.add(labelQuantidade, "cell 1 1, gapleft 15px");
		painelProdutos.add(campoQuantidade, "cell 2 1");		
		painelProdutos.add(labelValor, "cell 3 1, gapleft 20px");		
		painelProdutos.add(campoValor, "cell 4 1");
		painelProdutos.add(campoComentario, "cell 1 2, gapleft 10px, gaptop 10px, span 4");

		WebScrollPane scroll = new WebScrollPane(adicionaisPainel, false);
		scroll.setMinimumSize(new Dimension(300, 180));
		scroll.setMaximumSize(new Dimension(300, 180));
		scroll.setPreferredSize(new Dimension(300, 180));
		adicionaisPainel1.add(scroll);
		adicionaisPainel1.setPainter(bp4);
		
		final PainelVendaMesa referencia = this;
		TooltipManager.addTooltip(adicionaisPainel, "Adicionais", TooltipWay.up, 500);
		adicionaisPainel.addMouseListener(new MouseListener(){
			@Override
			public void mouseClicked(MouseEvent e) {}

			@Override
			public void mousePressed(MouseEvent e) {
				if(todosProdutos.getCategorias().get(0).getProdutos().size() == 0)
				{
					JOptionPane.showMessageDialog(null, "Nenhum produto adicional cadastrado!");
				}
				else if(addAdicional.size() >= 6)
				{
					JOptionPane.showMessageDialog(null, "Máximo de 6 adicionais por produto!");
				}
				else
				{
					JButton botao = new JButton();
					botao.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/remove.png")));
					botao.setBorder(BorderFactory.createEmptyBorder());
					botao.setContentAreaFilled(false);
					botao.addActionListener(referencia);

					ProdutosComboBox adcCombo = new ProdutosComboBox(todosProdutos.getCategorias(), 0);
					adcCombo.setMinimumSize(new Dimension(255, 50));
					adcCombo.setMaximumSize(new Dimension(255, 50));
					adcCombo.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							updateCampo();
						}
					});
					addAdicional.add(adcCombo);
					addRemover.add(botao);

					adicionaisPainel.add(addAdicional.get(addAdicional.size()-1));
					adicionaisPainel.add(addRemover.get(addAdicional.size()-1), "wrap");

					adicionaisPainel.revalidate();
					adicionaisPainel.repaint();
					addAdicional.get(addAdicional.size()-1).requestFocus();
					updateCampo();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {}

			@Override
			public void mouseEntered(MouseEvent e) {
				SwingUtilities.invokeLater(new Runnable() {  
					public void run() {
						DashedBorderPainter<JComponent> bp4 = new DashedBorderPainter<>(new float[]{ 6f, 10f });
						bp4.setRound(2);
						bp4.setWidth(4);
						bp4.setColor(new Color(140, 140, 140));
						adicionaisPainel1.setPainter(bp4);
						adicionaisPainel1.revalidate();
						adicionaisPainel1.repaint();
					}
				});
			}

			@Override
			public void mouseExited(MouseEvent e) {
				SwingUtilities.invokeLater(new Runnable() {  
					public void run() {
						DashedBorderPainter<JComponent> bp4 = new DashedBorderPainter<>(new float[]{ 6f, 10f });
						bp4.setRound(2);
						bp4.setWidth(4);
						bp4.setColor(new Color(205, 205, 205));
						adicionaisPainel1.setPainter(bp4);
						adicionaisPainel1.revalidate();
						adicionaisPainel1.repaint();
					}
				});
			}
		});
		
		painelProdutos.add(adicionaisPainel1, "cell 5 0, gaptop 20px, span 1 5");
		painelProdutos.add(adicionarProduto, "cell 6 0, aligny center, gapleft 40px, span 1 5");

		pedidoPainel = new JPanel(new BorderLayout());
		pedidoPainel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Pedido"));
		
		tabelaModel = new VendasTableModel();

		tabelaPedido = new JTable() {
			private static final long serialVersionUID = 1L;
			Color alternate = new Color(206, 220, 249);

			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
				Component stamp = super.prepareRenderer(renderer, row, column);
				if (row % 2 == 0)
					stamp.setBackground(alternate);
				else
					stamp.setBackground(this.getBackground());
				return stamp;
			}				
		};
		
		tabelaPedido.setFocusable(false);
		tabelaPedido.setModel(tabelaModel);
		tabelaPedido.getColumnModel().getColumn(0).setMinWidth(70);
		tabelaPedido.getColumnModel().getColumn(0).setMaxWidth(70);
		tabelaPedido.getColumnModel().getColumn(1).setPreferredWidth(180);
		tabelaPedido.getColumnModel().getColumn(2).setPreferredWidth(45);
		tabelaPedido.getColumnModel().getColumn(3).setPreferredWidth(80);			
		tabelaPedido.getColumnModel().getColumn(4).setPreferredWidth(80);
		tabelaPedido.getColumnModel().getColumn(5).setPreferredWidth(200);
		tabelaPedido.getColumnModel().getColumn(6).setPreferredWidth(200);
		tabelaPedido.getColumnModel().getColumn(7).setMinWidth(60);
		tabelaPedido.getColumnModel().getColumn(7).setMaxWidth(60);
		tabelaPedido.setRowHeight(30);
		tabelaPedido.getTableHeader().setReorderingAllowed(false);

		tabelaPedido.getColumn("+/-").setCellRenderer(new OpcoesCell());
		tabelaPedido.getColumn("+/-").setCellEditor(new OpcoesCell());		
		tabelaPedido.getColumn("Preço").setCellRenderer(new CustomRenderer());
		tabelaPedido.getColumn("Qntd").setCellRenderer(new CustomRenderer());
		tabelaPedido.getColumn("Pago").setCellRenderer(new CustomRenderer());
		tabelaPedido.getColumn("Nome").setCellRenderer(new CustomRenderer());
		tabelaPedido.getColumn("Adicionais").setCellRenderer(new CustomRenderer());
		tabelaPedido.getColumn("Comentário").setCellRenderer(new CustomRenderer());
		tabelaPedido.getColumn("Deletar").setCellRenderer(new ButtonRenderer());
		tabelaPedido.getColumn("Deletar").setCellEditor(new ButtonEditor(new JCheckBox()));	
		tabelaPedido.setPreferredScrollableViewportSize(new Dimension(800, 200));
		WebScrollPane scrolltabela = new WebScrollPane(tabelaPedido, true);
		scrolltabela.setFocusable(false);
		scrolltabela.getViewport().setBackground(new Color(237, 237, 237));
		pedidoPainel.add(scrolltabela, BorderLayout.CENTER);

		painelProdutos1 = new JPanel();
		painelProdutos1.setLayout(new BoxLayout(painelProdutos1, BoxLayout.Y_AXIS));	

		painelProdutos1.add(painelProdutos);
		painelProdutos1.add(pedidoPainel);

		painelPagamento = new JPanel();
		painelPagamento.setLayout(new MigLayout("aligny center, alignx center", "[][]40[]40[][]", "[][]10[]10[]10[]10[]10[]10[]"));

		adicionarDezPorcento = new JCheckBox("<html>+ 10% Opcional: <br>(R$0,00)</html>");
		adicionarDezPorcento.setPreferredSize(new Dimension(150, 35));
		adicionarDezPorcento.setMaximumSize(new Dimension(150, 35));
		adicionarDezPorcento.setFont(new Font("Helvetica", Font.BOLD, 14));
		adicionarDezPorcento.addItemListener(this);
		adicionarDezPorcento.setSelected(false);

		if(!config.getDezPorcento())
			adicionarDezPorcento.setEnabled(false);

		labelCliente = new JLabel("Cliente:");
		labelCliente.setFont(new Font("Helvetica", Font.BOLD, 16));

		escolherCliente = new WebButton("Escolher");
		escolherCliente.setRolloverShine(true);
		escolherCliente.setPreferredSize(new Dimension(170, 40));
		escolherCliente.setHorizontalTextPosition(AbstractButton.LEFT);
		escolherCliente.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/report_user_mini.png")));	
		escolherCliente.addActionListener(this);

		deletarCliente = new WebButton("");
		deletarCliente.setUndecorated(true);
		deletarCliente.setPreferredSize(new Dimension(20, 20));
		deletarCliente.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/delete.png")));	
		deletarCliente.addActionListener(this);				

		labelTotal = new JLabel("Total:");
		labelTotal.setFont(new Font("Helvetica", Font.BOLD, 16));
		campoTotal = new JTextField("0,00");
		campoTotal.setHorizontalAlignment(SwingConstants.CENTER);
		campoTotal.setPreferredSize(new Dimension(110, 35));
		campoTotal.setEnabled(false);

		labelRecebido = new JLabel("Recebido:");
		labelRecebido.setFont(new Font("Helvetica", Font.BOLD, 16));
		campoRecebido = new JTextField("");
		campoRecebido.setHorizontalAlignment(SwingConstants.CENTER);
		campoRecebido.setPreferredSize(new Dimension(110, 35));
		campoRecebido.setEditable(true);
		campoRecebido.addFocusListener(this);

		ImageIcon iconeCalcular = new ImageIcon(getClass().getClassLoader().getResource("imgs/calcular.png"));
		calcular = new JButton(iconeCalcular);
		calcular.addActionListener(this);
		calcular.setBorder(BorderFactory.createEmptyBorder());
		calcular.setContentAreaFilled(false);

		labelTroco = new JLabel("Troco:");
		labelTroco.setFont(new Font("Helvetica", Font.BOLD, 16));
		campoTroco = new JTextField("0,00");
		campoTroco.setHorizontalAlignment(SwingConstants.CENTER);
		campoTroco.setPreferredSize(new Dimension(110, 35));
		campoTroco.setEnabled(false);

		labelForma = new JLabel("Pagamento:");
		labelForma.setFont(new Font("Helvetica", Font.BOLD, 16));

		String[] tiposPagamento = {"Dinheiro", "Ticket Refeição", "Cartão de Crédito", 
				"Cartão de Débito", "Cheque", "Fiado" };
		campoForma = new WebComboBox(tiposPagamento);
		campoForma.setSelectedIndex(0);
		campoForma.setPreferredSize(new Dimension(140, 40));
		
		campoFuncionario = new JComboBox<Object>();
		funcionarioModel = new FuncionariosComboModel(funs);
		campoFuncionario.setModel(funcionarioModel);
		campoFuncionario.setPreferredSize(new Dimension(140, 40));

		finalizarVenda = new WebButton("Concluir Venda");
		finalizarVenda.setRolloverShine(true);
		finalizarVenda.setFont(new Font("Helvetica", Font.BOLD, 16));
		finalizarVenda.setPreferredSize(new Dimension(270, 50));
		finalizarVenda.setIcon(iconeFinalizar);	
		finalizarVenda.addActionListener(this);

		painelDropIn = new DragPanel();
		painelDropIn.tipo = 1;
		painelDropIn.setLayout(new BoxLayout(painelDropIn, BoxLayout.Y_AXIS));
		painelDropIn.setMinimumSize(new Dimension(305, 120));
		painelDropIn.setMaximumSize(new Dimension(305, 120));	

		WebScrollPane scrollDropIn = new WebScrollPane(painelDropIn, false);
		scrollDropIn.setMinimumSize(new Dimension(305,120));
		scrollDropIn.setMaximumSize(new Dimension(305,120));
		scrollDropIn.setPreferredSize(new Dimension(305,120));

		WebPanel painelDropIn1 = new WebPanel();
		painelDropIn1.setPainter(new TitledBorderPainter(config.getTipoNome()));
		painelDropIn1.add(scrollDropIn);

		MouseListener handler = new Handler();
		painelDropIn.addMouseListener(handler);
		LabelTransferHandler th = new LabelTransferHandler();
		painelDropIn.setTransferHandler(th); 

		painelPagamento.add(painelDropIn1, "cell 3 0, span 2 2");

		painelDropOut = new DragPanel();
		painelDropOut.tipo = 2;
		painelDropOut.setLayout(new BoxLayout(painelDropOut, BoxLayout.Y_AXIS));
		painelDropOut.setMinimumSize(new Dimension(305, 120));
		painelDropOut.setMaximumSize(new Dimension(305, 120));
		WebScrollPane scrollDropOut = new WebScrollPane(painelDropOut, false);
		scrollDropOut.setMinimumSize(new Dimension(305,120));
		scrollDropOut.setMaximumSize(new Dimension(305,120));
		scrollDropOut.setPreferredSize(new Dimension(305,120));		
		WebPanel painelDropOut1 = new WebPanel();
		painelDropOut1.setPainter ( new TitledBorderPainter ( "Pagando" ) );
		painelDropOut1.add(scrollDropOut);		

		painelDropOut.addMouseListener(handler);
		painelDropOut.setTransferHandler(th);		
			
		painelPagamento.add(painelDropOut1, "cell 0 0, span 2 2");
		painelPagamento.add(flecha2, "cell 2 0, gaptop 15px");
		painelPagamento.add(flecha1, "cell 2 1");
		painelPagamento.add(labelForma, "cell 0 2");
		painelPagamento.add(campoForma, "cell 1 2, align right");	
		painelPagamento.add(adicionarDezPorcento, "cell 0 3, span 2");
		painelPagamento.add(campoFuncionario, "cell 0 3, gapleft 20px");	
		painelPagamento.add(labelTotal, "cell 0 4");
		painelPagamento.add(campoTotal, "cell 1 4, align right");
		painelPagamento.add(labelRecebido, "cell 0 5");
		painelPagamento.add(campoRecebido, "cell 1 5, align right");		
		painelPagamento.add(labelTroco, "cell 0 6");
		painelPagamento.add(campoTroco, "cell 1 6, align right");		
		painelPagamento.add(finalizarVenda, "cell 0 7, span 2, align center");

		WebPanel reciboPainel = new WebPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
		reciboPainel.setPreferredSize(new Dimension(240, 140));

		DashedBorderPainter bp5 = new DashedBorderPainter ( new float[]{ 3f, 3f } );
		bp5.setWidth(2);
		bp5.setColor(new Color( 205, 205, 205 ));
		reciboPainel.setPainter(bp5);			

		campoRecibo = new JEditorPane();
		campoRecibo.setFont(new Font("Verdana", Font.PLAIN, 8));
		campoRecibo.setEditable(false);
		campoRecibo.setText("### Nenhum produto marcado ###");

		JScrollPane scrollrecibo = new JScrollPane(campoRecibo);
		scrollrecibo.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollrecibo.setPreferredSize(new Dimension(220, 120));
		reciboPainel.add(scrollrecibo);

		painelPagamento.add(labelCliente, "cell 3 2, gapleft 15px");	
		painelPagamento.add(escolherCliente, "cell 4 2, gapleft 15px, split 2");
		painelPagamento.add(deletarCliente, "cell 4 2, gapleft 5px");		
		painelPagamento.add(reciboPainel, "cell 3 3, span 2 4, align center");

		imprimir = new WebButton("Imprimir");
		imprimir.setPreferredSize(new Dimension(270, 50));
		imprimir.setRolloverShine(true);
		imprimir.setFont(new Font("Helvetica", Font.BOLD, 16));
		imprimir.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/imprimir.png")));
		imprimir.addActionListener(this);

		painelPagamento.add(imprimir, "cell 3 7, span 2, align center");		

		if(config.getTipoPrograma() == UtilCoffe.TIPO_MESA)
		{
			divisaoPainel.addTab(config.getTipoNome(), 
					new ImageIcon(getClass().getClassLoader().getResource("imgs/mesa_mini.png")), painelProdutos1, "Gerenciar o pedido da mesa (ALT + W)");	
		}
		else
		{
			divisaoPainel.addTab(config.getTipoNome(), 
					new ImageIcon(getClass().getClassLoader().getResource("imgs/comanda_24.png")), painelProdutos1, "Gerenciar o pedido da comanda (ALT + W)");		
		}
				
		divisaoPainel.addTab("Pagamento", new ImageIcon(getClass().getClassLoader().getResource("imgs/recibo_mini.png")), painelPagamento, "Pagamento do Pedido (ALT + E)");		
		add(divisaoPainel);

		clienteVenda = null;		
		taxaOpcional = 0.0;
		
		setFocusCycleRoot(true);
		ArrayList<Component> ordem = new ArrayList<Component>();
		ordem.add(addProduto.getEditorTextField());
		ordem.add(campoQuantidade);
		ordem.add(campoComentario);
		ordem.add(adicionarProduto);
		FocusTraversal ordemFocus = new FocusTraversal(ordem);
		setFocusTraversalPolicy(ordemFocus);
		
		divisaoPainel.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if(divisaoPainel.getSelectedIndex() == 0)
				{
					ArrayList<Component> ordem = new ArrayList<Component>();
					ordem.add(addProduto.getEditorTextField());
					ordem.add(campoQuantidade);
					ordem.add(campoComentario);
					ordem.add(adicionarProduto);
					FocusTraversal ordemFocus = new FocusTraversal(ordem);
					setFocusTraversalPolicy(ordemFocus);
					
					addProduto.requestFocus();
				}
				else
				{
					ArrayList<Component> ordem = new ArrayList<Component>();
					ordem.add(campoForma);
					
					if(config.getDezPorcento())
						ordem.add(adicionarDezPorcento);
					
					ordem.add(escolherCliente);
					ordem.add(campoFuncionario);
					ordem.add(campoRecebido);
					ordem.add(finalizarVenda);
					ordem.add(imprimir);
					FocusTraversal ordemFocus = new FocusTraversal(ordem);
					setFocusTraversalPolicy(ordemFocus);
					
					campoForma.requestFocus();
				}
			}
		});
		
		ActionMap actionMap = getActionMap();
		actionMap.put("botao1", new AtalhoAction(0));
		actionMap.put("botao2", new AtalhoAction(1));
		actionMap.put("botao3", new AtalhoAction(2));
		actionMap.put("botao4", new AtalhoAction(3));
		setActionMap(actionMap);
		
		InputMap imap = getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
		imap.put(KeyStroke.getKeyStroke("control Q"), "botao1");
		imap.put(KeyStroke.getKeyStroke("alt Q"), "botao1");
		imap.put(KeyStroke.getKeyStroke("alt ENTER"), "botao2");
		imap.put(KeyStroke.getKeyStroke("alt A"), "botao2");
		imap.put(KeyStroke.getKeyStroke("control A"), "botao2");
		imap.put(KeyStroke.getKeyStroke("alt W"), "botao3");
		imap.put(KeyStroke.getKeyStroke("alt E"), "botao4");
		
		TooltipManager.addTooltip(adicionarProduto, "ALT + A", TooltipWay.up, 1000);
		TooltipManager.addTooltip(campoQuantidade, "ALT + Q", TooltipWay.up, 1000);
		
		adicionarDezPorcento.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("ENTER"), "enter");
		adicionarDezPorcento.getActionMap().put("enter", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			@Override
			public void actionPerformed(ActionEvent e) {
				adicionarDezPorcento.doClick();
			}
		});
	}
	
	private class AtalhoAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		private int tipo = 0;
		
		public AtalhoAction(int tipo) {
	        this.tipo = tipo;
	    }		
		
        @Override
        public void actionPerformed(ActionEvent e)
        {
        	switch(this.tipo)
        	{
        		case 0:
        		{
        			campoQuantidade.requestFocus();
        			break;
        		}
        		case 1:
        		{
        			adicionarProduto.doClick();
        			break;
        		}
        		case 2:
        		{
        			divisaoPainel.setSelectedIndex(0);
        			break;
        		}
        		case 3:
        		{
        			divisaoPainel.setSelectedIndex(1);
        			break;
        		}
        	}
        }
	}

	class DragPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		public DragPanel() {
			super();
		}
		public DragLabel draggingLabel;
		public int tipo;
	}

	class Handler extends MouseAdapter {
		@Override public void mousePressed(MouseEvent e) {
			DragPanel p = (DragPanel)e.getSource();
			Component c = SwingUtilities.getDeepestComponentAt(p, e.getX(), e.getY());
			if(c!=null && c instanceof DragLabel) {
				p.draggingLabel = (DragLabel)c;
				p.getTransferHandler().exportAsDrag(p, e, TransferHandler.MOVE);
			}
		}
	}
	class LabelTransferHandler extends TransferHandler {
		private static final long serialVersionUID = 1L;
		private final DataFlavor localObjectFlavor;
		private final DragLabel label = new DragLabel() {
			private static final long serialVersionUID = 1L;

			@Override public boolean contains(int x, int y) {
				return false;
			}
		};
		private final JWindow window = new JWindow();
		public LabelTransferHandler() {
			localObjectFlavor = new ActivationDataFlavor(DragPanel.class, DataFlavor.javaJVMLocalObjectMimeType, "DragLabel");
			window.add(label);
			window.setAlwaysOnTop(true);
			window.setBackground(new Color(0,true));
			DragSource.getDefaultDragSource().addDragSourceMotionListener(
					new DragSourceMotionListener() {
						@Override public void dragMouseMoved(DragSourceDragEvent dsde) {
							Point pt = dsde.getLocation();
							pt.translate(5, 5); // offset
							window.setLocation(pt);
						}
					});
		}
		@Override protected Transferable createTransferable(JComponent c) {
			DragPanel p = (DragPanel)c;
			DragLabel l = p.draggingLabel;
			String text = l.getText();
			final DataHandler dh = new DataHandler(c, localObjectFlavor.getMimeType());
			if(text==null) return dh;
			final StringSelection ss = new StringSelection(text+"\n");
			return new Transferable() {
				@Override public DataFlavor[] getTransferDataFlavors() {
					ArrayList<DataFlavor> list = new ArrayList<>();
					for(DataFlavor f:ss.getTransferDataFlavors()) {
						list.add(f);
					}
					for(DataFlavor f:dh.getTransferDataFlavors()) {
						list.add(f);
					}
					return list.toArray(dh.getTransferDataFlavors());
				}
				public boolean isDataFlavorSupported(DataFlavor flavor) {
					for (DataFlavor f: getTransferDataFlavors()) {
						if (flavor.equals(f)) {
							return true;
						}
					}
					return false;
				}
				public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
					if(flavor.equals(localObjectFlavor)) {
						return dh.getTransferData(flavor);
					} else {
						return ss.getTransferData(flavor);
					}
				}
			};
		}
		@Override public boolean canImport(TransferSupport support) {
			if(!support.isDrop()) {
				return false;
			}
			return true;
		}
		@Override public int getSourceActions(JComponent c) {
			DragPanel p = (DragPanel)c;
			label.setIcon(p.draggingLabel.getIcon());
			label.setText(p.draggingLabel.getText());
			label.setFont(p.draggingLabel.getFont());    
			window.pack();
			Point pt = p.draggingLabel.getLocation();
			SwingUtilities.convertPointToScreen(pt, p);
			window.setLocation(pt);
			window.setVisible(true);
			return MOVE;
		}
		@Override public boolean importData(TransferSupport support) {
			if(!canImport(support)) return false;
			DragPanel target = (DragPanel)support.getComponent();
			try {
				DragPanel src = (DragPanel)support.getTransferable().getTransferData(localObjectFlavor);
				DragLabel l = new DragLabel();
				l.setProduto(UtilCoffe.cloneProdutoVenda(src.draggingLabel.getProduto()));
				
				l.setIcon(src.draggingLabel.getIcon());
				l.setText(src.draggingLabel.getText());
				l.setFont(src.draggingLabel.getFont());

				target.add(l);
				target.revalidate();		      
				return true;
			} catch(UnsupportedFlavorException ufe) {
				ufe.printStackTrace();
				new PainelErro(ufe);
			} catch(java.io.IOException ioe) {
				ioe.printStackTrace();
				new PainelErro(ioe);
			}
			return false;
		}
		@Override protected void exportDone(JComponent c, Transferable data, int action) {
			DragPanel src = (DragPanel)c;
			if(action == TransferHandler.MOVE) 
			{				
				src.remove(src.draggingLabel);
				src.revalidate();
				src.repaint();

				double total = 0.0;
				for(int i = 0; i < painelDropOut.getComponentCount(); i++)
				{
					DragLabel dragL = (DragLabel)painelDropOut.getComponent(i);
					total += dragL.getProduto().getTotalProduto();
				}

				if(config.getDezPorcento())
				{
					taxaOpcional = total * 0.10;
					adicionarDezPorcento.setText("<html>+ 10% Opcional: <br>(R$" + UtilCoffe.doubleToPreco(taxaOpcional) + ")</html>");
				}

				if(adicionarDezPorcento.isSelected())
					campoTotal.setText(UtilCoffe.doubleToPreco((total + (total * 0.10))));
				else
					campoTotal.setText(UtilCoffe.doubleToPreco(total));

				atualizarCampoRecibo();
			}
			src.draggingLabel = null;
			window.setVisible(false);
		}
	}

	class CustomRenderer extends DefaultTableCellRenderer 
	{
		private static final long serialVersionUID = 1L;

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if(isSelected)
			{
				setHorizontalAlignment( JLabel.CENTER );
				c.setForeground(new Color(72, 61, 139));
				return c;
			}
			else
			{
				setHorizontalAlignment( JLabel.CENTER );
				c.setForeground(Color.BLACK);
				return c;
			}
		}
	}

	public void updateCampo()
	{
		if(addProduto.getProdutoSelecionado() != null)
		{
			double aDouble = addProduto.getProdutoSelecionado().getPreco();

			for(int i = 0; i < addAdicional.size() ; i++)
			{
				if(addAdicional.get(i).getProdutoSelecionado() != null)
				{
					aDouble += addAdicional.get(i).getProdutoSelecionado().getPreco();	
				}
			}

			campoValor.setText(UtilCoffe.doubleToPreco(aDouble));
		}
	}
	
	public void refreshModel() 
	{
		addProduto.refreshModel();
		for(int i = 0; i < addAdicional.size(); i++)
			addAdicional.get(i).refreshModel();
	}

	private void atualizarCampoRecibo()
	{
		String formataRecibo = "";

		formataRecibo += ("===========================\n");
		formataRecibo += config.getMensagemSuperior() + "\n";
		formataRecibo += ("===========================\n");
		formataRecibo += ("********* NAO TEM VALOR FISCAL ********\n");
		formataRecibo += ("===========================\n");		                	
		formataRecibo += ("PRODUTO              QTDE  VALOR UN.  VALOR\n");

		Venda vendaAgora = new Venda();

		for(int i = 0; i < painelDropOut.getComponentCount(); i++)
		{
			DragLabel dragL = (DragLabel)painelDropOut.getComponent(i);
			vendaAgora.adicionarProduto(UtilCoffe.cloneProdutoVenda(dragL.getProduto()));
		}

		vendaAgora.calculaTotal();

		for(int i = 0; i < vendaAgora.getQuantidadeProdutos(); i++)
		{
			formataRecibo += (String.format("%-20.20s", vendaAgora.getProduto(i).getReferencia()));
			formataRecibo += (String.format("%4s     ", vendaAgora.getProduto(i).getQuantidade()));

			double totalsub = 0.0;

			for(int j = 0; j < vendaAgora.getProduto(i).getTotalAdicionais(); j++)
			{
				totalsub += vendaAgora.getProduto(i).getAdicional(j).getPreco();
			}

			formataRecibo += (String.format("%7s       ", UtilCoffe.doubleToPreco((vendaAgora.getProduto(i).getPreco() - totalsub))));
			formataRecibo += (String.format("%6s     \n", UtilCoffe.doubleToPreco((vendaAgora.getProduto(i).getPreco() - totalsub) * vendaAgora.getProduto(i).getQuantidade())));

			for(int j = 0; j < vendaAgora.getProduto(i).getTotalAdicionais(); j++)
			{
				formataRecibo += (String.format("%-20.20s", "+" + vendaAgora.getProduto(i).getAdicional(j).getReferencia()));
				formataRecibo += (String.format("%3s     ", vendaAgora.getProduto(i).getQuantidade()));
				formataRecibo += (String.format("%5s    ", UtilCoffe.doubleToPreco(vendaAgora.getProduto(i).getAdicional(j).getPreco())));
				formataRecibo += (String.format("%6s    \n", UtilCoffe.doubleToPreco((vendaAgora.getProduto(i).getAdicional(j).getPreco()*vendaAgora.getProduto(i).getQuantidade()))));
			}
		}            

		formataRecibo += ("===========================\n");
		formataRecibo += ("INFORMACOES PARA FECHAMENTO DE CONTA    \n");
		formataRecibo += ("===========================\n");
		
		formataRecibo += (String.format("%-18.18s", "Local: "));
		formataRecibo += (config.getTipoNome() + " " + (mesaID+1) + "\n");
		
		formataRecibo += (String.format("%-18.18s", "Permanência: "));
		
		long minutes = 0;
		if(vendaMesa != null)
		{
			if(vendaMesa.getData() != null)
			{
				long duration = System.currentTimeMillis() - vendaMesa.getData().getTime();
				minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
			}
		}
		
		String formatap = "";
		
		if((minutes/60) > 0)
		{
			formatap += (minutes/60) + " hora";
			
			if((minutes/60) > 1) {
				formatap += "s";
			}
			
			if((minutes % 60) > 1) {
				formatap += " e " + (minutes % 60) + " minutos";
			}
		}
		else
		{
			if((minutes % 60) > 1)
				formatap += (minutes % 60) + " minutos";
			else
				formatap += (minutes % 60) + " minuto";
		}
		
		formataRecibo += (formatap) + "\n";
		
		formataRecibo += (String.format("%-18.18s", "Atendido por: "));
		formataRecibo += (campoFuncionario.getSelectedItem().toString() + "\n");

		Locale locale = new Locale("pt","BR"); 
		GregorianCalendar calendar = new GregorianCalendar(); 
		SimpleDateFormat formatador = new SimpleDateFormat("EEE, dd'/'MM'/'yyyy' - 'HH':'mm", locale);		                

		formataRecibo += (String.format("%-18.18s", "Data: "));
		formataRecibo += (formatador.format(calendar.getTime()) + "\n");

		formataRecibo += ("===========================\n");
		formataRecibo += ("                     -------------------\n");
		formataRecibo += ("Total                            R$" + UtilCoffe.doubleToPreco(vendaAgora.getTotal()) + "\n");

		if(config.getDezPorcento())
		{
			formataRecibo += ("                     ----------------------\n");
			formataRecibo += ("10% Opcional                     R$" + UtilCoffe.doubleToPreco(vendaAgora.getTotal() + taxaOpcional) + "\n");            	  
		}		

		formataRecibo += ("===========================\n");
		formataRecibo += config.getMensagemInferior() + "\n";
		formataRecibo += ("       Sistema CodeCoffe " + UtilCoffe.VERSAO + "\n");

		campoRecibo.setText(formataRecibo);		
	}	

	private void criarRecibo()
	{
		Venda vendaAgora = new Venda();

		for(int i = 0; i < painelDropOut.getComponentCount(); i++)
		{
			DragLabel dragL = (DragLabel)painelDropOut.getComponent(i);
			vendaAgora.adicionarProduto(UtilCoffe.cloneProdutoVenda(dragL.getProduto()));
		}

		vendaAgora.calculaTotal();

		CacheImpressao criaImpressao = new CacheImpressao(vendaAgora);
		criaImpressao.getVendaFeita().setData(vendaMesa.getData());
		criaImpressao.setTotal(UtilCoffe.doubleToPreco(vendaAgora.getTotal()));
		criaImpressao.setAtendente(campoFuncionario.getSelectedItem().toString());
		criaImpressao.setFiado_id(clienteVenda == null ? 0 : clienteVenda.getIdUnico());
		criaImpressao.setCaixa((mesaID+1));
		criaImpressao.setDelivery("0,00");
		criaImpressao.setDezporcento(UtilCoffe.doubleToPreco(vendaAgora.getTotal() + taxaOpcional));
		criaImpressao.setClasse(UtilCoffe.CLASSE_VENDA_MESA);
		
		if(config.getModo() == UtilCoffe.SERVER)
			Recibo.gerarNotaVenda(config, criaImpressao);
		else {
			((Client) modoPrograma).enviarObjeto(criaImpressao);
			
			JOptionPane.showMessageDialog(null, "Pedido de impressão enviado ao computador principal!", 
					"Impressão enviada!", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private class DragLabel extends JLabel 
	{
		private static final long serialVersionUID = 1L;
		private ProdutoVenda produto;

		public DragLabel() {
			super();
		}

		public ProdutoVenda getProduto() {
			return produto;
		}

		public void setProduto(ProdutoVenda produto) {
			this.produto = produto;
		}
	}

	private void dragAdicionaProduto(ProdutoVenda p)
	{
		int cacheQuantidade = (p.getQuantidade() - p.getPagos());

		for(int i = 0; i < cacheQuantidade; i++)
		{
			DragLabel dragP = new DragLabel();
			dragP.setProduto(UtilCoffe.cloneProdutoVenda(p));
			dragP.getProduto().setQuantidade(1, 0);
			dragP.getProduto().calcularPreco();

			dragP.setFont(new Font("Verdana", Font.PLAIN, 10));
			dragP.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/icon_food.png")));

			if(dragP.getProduto().getAdicionaisList().size() > 0)
				dragP.setText(dragP.getProduto().getNome() + " com " + dragP.getProduto().getAllAdicionais() 
						+ " - " + UtilCoffe.doubleToPreco(dragP.getProduto().getTotalProduto()));
			else
				dragP.setText(dragP.getProduto().getNome() + " - " + UtilCoffe.doubleToPreco(dragP.getProduto().getPreco()));

			painelDropIn.add(dragP);
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				painelDropIn.revalidate();
				painelDropIn.repaint();
			}
		});
	}

	public void termina(boolean delete)
	{
		CacheMesaHeader mh = new CacheMesaHeader(mesaID , vendaMesa, UtilCoffe.MESA_LIMPAR);
		if(config.getModo() == UtilCoffe.SERVER) {
			atualizaMesa(mh, null, (short)0);
		}
		else {
			((Client) modoPrograma).enviarObjeto(mh);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == imprimir)
		{
			criarRecibo();
		}
		else if(e.getSource() == addProduto)
		{
			updateCampo();
		}
		else if(e.getSource() == deletarCliente)
		{
			escolherCliente.setText("Escolher");
			clienteVenda = null;
			atualizarCampoRecibo();
		}			
		else if(e.getSource() == escolherCliente)
		{
			painelListener.atualizarPainel(new Header(UtilCoffe.UPDATE_CALLBACK, this));
			painelListener.atualizarPainel(new Header(UtilCoffe.ABRIR_CLIENTES));
		}		
		else if(e.getSource() == flecha1)
		{
			if(painelDropOut.getComponentCount() > 0)
			{
				while(painelDropOut.getComponentCount() > 0)
				{
					DragLabel dragL = (DragLabel) painelDropOut.getComponent(0);
					painelDropOut.remove(0);
					painelDropIn.add(dragL);
				}

				double total = 0.0;
				for(int i = 0; i < painelDropOut.getComponentCount(); i++)
				{
					DragLabel dragL = (DragLabel)painelDropOut.getComponent(i);
					total += dragL.getProduto().getTotalProduto();
				}

				if(config.getDezPorcento())
				{
					taxaOpcional = total * 0.10;
					adicionarDezPorcento.setText("<html>+ 10% Opcional: <br>(R$" + UtilCoffe.doubleToPreco(taxaOpcional) + ")</html>");
				}				

				if(adicionarDezPorcento.isSelected())
					campoTotal.setText(UtilCoffe.doubleToPreco((total + (total * 0.10))));
				else
					campoTotal.setText(UtilCoffe.doubleToPreco(total));

				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						atualizarCampoRecibo();
						painelDropOut.revalidate();
						painelDropOut.repaint();
						painelDropIn.revalidate();
						painelDropIn.repaint();
					}
				});
			}
		}
		else if(e.getSource() == flecha2)
		{
			if(painelDropIn.getComponentCount() > 0)
			{
				while(painelDropIn.getComponentCount() > 0)
				{
					DragLabel dragL = (DragLabel) painelDropIn.getComponent(0);
					painelDropIn.remove(0);
					painelDropOut.add(dragL);
				}

				double total = 0.0;
				for(int i = 0; i < painelDropOut.getComponentCount(); i++)
				{
					DragLabel dragL = (DragLabel)painelDropOut.getComponent(i);
					total += dragL.getProduto().getTotalProduto();
				}

				if(config.getDezPorcento())
				{
					taxaOpcional = total * 0.10;
					adicionarDezPorcento.setText("<html>+ 10% Opcional: <br>(R$" + UtilCoffe.doubleToPreco(taxaOpcional) + ")</html>");
				}

				if(adicionarDezPorcento.isSelected())
					campoTotal.setText(UtilCoffe.doubleToPreco((total + (total * 0.10))));
				else
					campoTotal.setText(UtilCoffe.doubleToPreco(total));

				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						atualizarCampoRecibo();
						painelDropOut.revalidate();
						painelDropOut.repaint();
						painelDropIn.revalidate();
						painelDropIn.repaint();
					}
				});
			}
		}
		else if(e.getSource() == finalizarVenda)
		{	
			if(painelDropOut.getComponentCount() > 0)
			{
				if("".equals(campoRecebido.getText().trim()))
					campoRecebido.setText("0,00");					

				String limpeza1 = campoRecebido.getText().replaceAll("[^0-9.,]+","");
				limpeza1 = limpeza1.replaceAll(",", ".");
				String limpeza2 = campoTotal.getText().replaceAll("[^0-9.,]+","");
				limpeza2 = limpeza2.replaceAll(",", ".");

				if(campoForma.getSelectedItem() == "Fiado" && (Double.parseDouble(limpeza1) >= Double.parseDouble(limpeza2)))
				{
					JOptionPane.showMessageDialog(null, "A quantia recebida não pode ser maior ou igual o total para fiado!");
				}
				else if("".equals(campoRecebido.getText().trim()) && campoForma.getSelectedItem() != "Fiado")
				{
					JOptionPane.showMessageDialog(null, "É necessário preencher o campo recebido caso a venda não seja fiada!", "Erro", JOptionPane.INFORMATION_MESSAGE);
				}				
				else
				{
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							
							if(campoForma.getSelectedItem() != "Fiado" || (campoForma.getSelectedItem() == "Fiado" && !escolherCliente.getText().equals("Escolher") && clienteVenda != null))
							{						
								if(campoForma.getSelectedItem() != "Fiado" && Double.parseDouble(campoRecebido.getText().replaceAll(",",".")) < Double.parseDouble(campoTotal.getText().replaceAll(",",".")))
									campoRecebido.setText(campoTotal.getText());

								String confirmacao = "";

								if(campoForma.getSelectedItem() == "Fiado")
								{				
									String divida = UtilCoffe.doubleToPreco((UtilCoffe.precoToDouble(campoTotal.getText()) - UtilCoffe.precoToDouble(campoRecebido.getText())));						
									confirmacao = "Valor Total: " + campoTotal.getText() + "\n" + 
											"Valor Pago: " + campoRecebido.getText() + 
											"\n\nForma de Pagamento: " + campoForma.getSelectedItem() + "\n\n" +
											"Será adicionado a dívida de R$" + divida + " na conta de " + escolherCliente.getText() + ".\n" + "\nConfirmar ?";							
								}
								else
								{
									confirmacao = "Valor Total: " + campoTotal.getText() + "\n" + 
											"Valor Pago: " + campoRecebido.getText() + "\n(Troco: " + campoTroco.getText() + ")\n\n" + 
											"Forma de Pagamento: " + campoForma.getSelectedItem() + "\n\n" +
											"Confirmar ?";							
								}
								int opcao = JOptionPane.showConfirmDialog(null, confirmacao, "Confirmar Venda", JOptionPane.YES_NO_OPTION);			
								if(opcao == JOptionPane.YES_OPTION)
								{
									Venda vendaAgora = new Venda();

									for(int i = 0; i < painelDropOut.getComponentCount(); i++)
									{
										DragLabel dragL = (DragLabel)painelDropOut.getComponent(i);
										vendaAgora.adicionarProduto(UtilCoffe.cloneProdutoVenda(dragL.getProduto()));
									}

									Calendar c = Calendar.getInstance();
									Locale locale = new Locale("pt","BR"); 
									GregorianCalendar calendar = new GregorianCalendar(); 
									SimpleDateFormat formatador = new SimpleDateFormat("dd'/'MM'/'yyyy' - 'HH':'mm", locale);

									for(int i = 0; i < vendaAgora.getQuantidadeProdutos(); i++)
									{
										for(int x = 0; x < vendaMesa.getQuantidadeProdutos(); x++)
										{
											if(vendaAgora.getProduto(i).compareTo(vendaMesa.getProduto(x)))
											{
												vendaMesa.getProduto(x).setPagos(vendaAgora.getProduto(i).getQuantidade());
												break;
											}
										}
									}
									
									tabelaModel.refreshTable();

									CacheMesaHeader mesaAgora			= new CacheMesaHeader(mesaID, vendaMesa, UtilCoffe.MESA_ATUALIZAR2);
									CacheVendaFeita vendaMesaFeita		= new CacheVendaFeita(vendaAgora, vendaMesa, mesaAgora);
									
									vendaMesaFeita.setTotal(campoTotal.getText());
									vendaMesaFeita.setAtendente(campoFuncionario.getSelectedItem().toString());
									vendaMesaFeita.setAno(c.get(Calendar.YEAR));
									vendaMesaFeita.setMes(c.get(Calendar.MONTH));
									vendaMesaFeita.setDia_mes(c.get(Calendar.DAY_OF_MONTH));
									vendaMesaFeita.setDia_semana(c.get(Calendar.DAY_OF_WEEK));
									vendaMesaFeita.setHorario(formatador.format(calendar.getTime()));
									vendaMesaFeita.setForma_pagamento(campoForma.getSelectedItem().toString());
									vendaMesaFeita.setValor_pago(campoRecebido.getText());
									vendaMesaFeita.setTroco(campoTroco.getText());
									vendaMesaFeita.setFiado_id(clienteVenda == null ? 0 : clienteVenda.getIdUnico());
									vendaMesaFeita.setCaixa((mesaID+1));
									vendaMesaFeita.setDelivery("0,00");
									vendaMesaFeita.setClasse(UtilCoffe.CLASSE_VENDA_MESA);
									vendaMesaFeita.getVendaFeita().setData(vendaMesa.getData());
									
									if(adicionarDezPorcento.isSelected())
										vendaMesaFeita.setDezporcento(UtilCoffe.doubleToPreco(taxaOpcional));
									else
										vendaMesaFeita.setDezporcento(UtilCoffe.doubleToPreco(0.0));

									if(config.getModo() == UtilCoffe.SERVER) {
										enviarVenda(vendaMesaFeita, null);
									}
									else {
										((Client) modoPrograma).enviarObjeto(vendaMesaFeita);
									}
								}
							}
							else
							{
								JOptionPane.showMessageDialog(null, "Escolha um cliente antes!");
							}
						}
					});					
				}					
			}
		}
		else if(e.getSource() == calcular)
		{
			String limpeza = campoRecebido.getText().replaceAll("[^0-9.,]+","");

			if(!"".equals(limpeza.trim()))
			{
				double pegaTotal = Double.parseDouble(campoTotal.getText().replaceAll(",", "."));
				double pegaRecebido = Double.parseDouble(limpeza.replaceAll(",", "."));

				if(((pegaTotal - pegaRecebido)*-1) <= 0)
				{
					campoTroco.setText("0,00");
				}
				else
				{
					String resultado = String.format("%.2f", (pegaTotal - pegaRecebido)*-1);
					resultado.replaceAll(",", ".");
					campoTroco.setText(resultado);					
				}
			}

			finalizarVenda.requestFocus();
		}
		else if(e.getSource() == adicionarProduto)
		{
			campoComentario.setText(campoComentario.getText().replaceAll("'", ""));
			
			if(addProduto.getProdutoSelecionado() == null) {
				JOptionPane.showMessageDialog(null, "Você precisa selecionar um produto antes!");
			}
			else if(campoComentario.getText().length() > 100) {
				JOptionPane.showMessageDialog(null, "Campo comentário pode ter no máximo 100 caracteres!");
			}
			else {
				atualizaMesa(null, null, UtilCoffe.INTERFACE_MESA_ADD);
			}
		}

		if(addRemover.size() > 0)
		{
			for(int i = 0; i < addRemover.size(); i++)
			{
				if(e.getSource() == addRemover.get(i))
				{
					adicionaisPainel.remove(addAdicional.get(i));
					adicionaisPainel.remove(addRemover.get(i));
					addAdicional.remove(i);
					addRemover.remove(i);
					double novoValor = 0.0;

					if(addProduto != null)
					{
						if(addProduto.getProdutoSelecionado() != null)
						{
							novoValor += addProduto.getProdutoSelecionado().getPreco();
							
							for(int x = 0; x < addAdicional.size() ; x++)
							{
								if(addAdicional.get(x).getProdutoSelecionado() != null)
								{
									novoValor += addAdicional.get(x).getProdutoSelecionado().getPreco();
								}
							}

							campoValor.setText(UtilCoffe.doubleToPreco(novoValor));
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									adicionaisPainel.revalidate();
									adicionaisPainel.repaint();	
								}
							});	
						}
					}

					break;
				}
			}
		}
	}

	class ButtonRenderer extends JButton implements TableCellRenderer 
	{
		private static final long serialVersionUID = 1L;

		public ButtonRenderer() {
			setOpaque(true);
		}

		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {

			setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/delete.png")));

			if (isSelected) {
				setForeground(table.getSelectionForeground());
				setBackground(table.getSelectionBackground());
			} else {
				setForeground(table.getSelectionForeground());
				setBackground(table.getSelectionBackground());
			}
			return this;
		}
	}

	class ButtonEditor extends DefaultCellEditor {
		private static final long serialVersionUID = 1L;
		protected JButton button;
		private String label;
		private boolean isPushed;

		public ButtonEditor(JCheckBox checkBox) {
			super(checkBox);
			button = new JButton();
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
			label = (value == null) ? "" : value.toString();
			button.setIcon(new ImageIcon(getClass().getClassLoader().getResource("imgs/delete.png")));
			isPushed = true;
			return button;
		}

		public Object getCellEditorValue() 
		{
			if (isPushed) {
				if(vendaMesa.getQuantidadeProdutos() <= 0)
				{
					tabelaModel.refreshTable();
					isPushed = false;
					return new String(label);		    		
				}
				if(tabelaPedido.getSelectedRowCount() == 1)
				{
					boolean continua = true;
					int fazendo = verificaStatusPedido((mesaID+1), tabelaModel.getProdutoVenda(tabelaPedido.getSelectedRow()));

					if(fazendo > 0)
					{
						int opcao = JOptionPane.showConfirmDialog(null, "Esses produtos já estão marcados como Fazendo na cozinha."
								+ "\n\nVocê tem certeza que quer deletar?\n\n", "Deletar Produto", JOptionPane.YES_NO_OPTION);
						if(opcao != JOptionPane.YES_OPTION)
							continua = false;
					}

					if(continua) {
						atualizaMesa(null, null, UtilCoffe.INTERFACE_MESA_REMOVE_TABELA2);
					}
				}
			}
			isPushed = false;
			return new String(label);
		}

		public boolean stopCellEditing() {
			isPushed = false;
			return super.stopCellEditing();
		}

		protected void fireEditingStopped() {
			super.fireEditingStopped();
		}
	}

	public synchronized void setMesa(int mesa, Venda v)
	{
		mesaID = mesa;
		vendaMesa = v;
		tabelaModel.setProdutoVenda(vendaMesa.getProdutos());

		SwingUtilities.invokeLater(new Runnable() {  
			public void run() {
				tabelaModel.refreshTable();
				adicionarDezPorcento.setSelected(false);
				
				if(addProduto.getProdutoSelecionado() != null)
					campoValor.setText(UtilCoffe.doubleToPreco(addProduto.getProdutoSelecionado().getPreco()));
				else
					campoValor.setText("");
					
				campoQuantidade.setText("1");
				campoTotal.setText("0,00");
				campoRecebido.setText("");
				campoTroco.setText("0,00");
				campoForma.setSelectedIndex(0);
				campoComentario.setText("");
				addAdicional.clear();
				addRemover.clear();			
				adicionaisPainel.removeAll();
				adicionaisPainel.revalidate();
				adicionaisPainel.repaint();
				clienteVenda = null;
				escolherCliente.setText("Escolher");
				painelDropOut.removeAll();
				painelDropOut.revalidate();
				painelDropOut.repaint();
				painelDropIn.removeAll();
				painelDropIn.revalidate();
				painelDropIn.repaint();    		
				campoRecibo.setText("### Nenhum produto marcado ###");
				addProduto.requestFocus();
				taxaOpcional = 0.0;
				adicionarDezPorcento.setText("<html>+ 10% Opcional: <br>(R$0,00)</html>");
				divisaoPainel.setTitleAt(0, config.getTipoNome() + " " + (mesaID+1));
				for(int i = 0; i < vendaMesa.getQuantidadeProdutos(); i++) {
					dragAdicionaProduto(vendaMesa.getProduto(i));						
				}		    		  
			}  
		});
	}

	@Override
	public void focusGained(FocusEvent e) {
		if(e.getSource() == campoRecebido) {
			campoRecebido.setText("");
		}
	}

	@Override
	public void focusLost(FocusEvent e) {
		if(e.getSource() == campoRecebido)
		{
			String limpeza = campoRecebido.getText().replaceAll("[^0-9.,]+","");

			if(!"".equals(limpeza.trim()))
			{
				double pegaTotal = Double.parseDouble(campoTotal.getText().replaceAll(",", "."));
				double pegaRecebido = Double.parseDouble(limpeza.replaceAll(",", "."));

				if(((pegaTotal - pegaRecebido)*-1) <= 0) {
					campoTroco.setText("0,00");
				}
				else {
					campoTroco.setText(UtilCoffe.doubleToPreco((pegaTotal - pegaRecebido)*-1));					
				}
			}		
		}
	}

	public void setFiado(Clientes cliente)
	{
		if(cliente.getIdUnico() > 0)
		{
			clienteVenda = cliente;
			escolherCliente.setText(clienteVenda.getNome());
			atualizarCampoRecibo();
		}
	}
	
	public void setFuncionarioSelected(String nome)
	{
		for(int i = 0; i < campoFuncionario.getItemCount(); i++)
		{
			if(campoFuncionario.getItemAt(i).toString().equals(nome)) {
				campoFuncionario.setSelectedIndex(i);
				break;
			}
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if(e.getItemSelectable() == adicionarDezPorcento)
		{
			if(adicionarDezPorcento.isSelected())
			{
				taxaOpcional = (UtilCoffe.precoToDouble(campoTotal.getText()) * 0.10);
				campoTotal.setText(UtilCoffe.doubleToPreco((UtilCoffe.precoToDouble(campoTotal.getText()) + taxaOpcional)));
				adicionarDezPorcento.setText("<html>+ 10% Opcional: <br>(R$" + UtilCoffe.doubleToPreco(taxaOpcional) + ")</html>");
			}
			else
			{
				campoTotal.setText(UtilCoffe.doubleToPreco((UtilCoffe.precoToDouble(campoTotal.getText()) - taxaOpcional)));
			}
		}
	}

	class OpcoesCellComponent extends JPanel
	{
		private static final long serialVersionUID = 1L;
		private WebButton maisProduto, menosProduto;
		private int linha;

		public OpcoesCellComponent()
		{
			setLayout(new FlowLayout(FlowLayout.CENTER, 5, 2));
			ActionListener al = new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e) {
					if(e.getSource() == maisProduto)
					{
						atualizaMesa(new CacheMesaHeader(linha, linha, linha), null, UtilCoffe.INTERFACE_MESA_ADD_TABELA);
					}
					else
					{
						if(linha < tabelaModel.getRowCount() && linha >= 0)
						{
							boolean continua = true;
							int fazendo = verificaStatusPedido((mesaID+1), tabelaModel.getProdutoVenda(linha));

							if(fazendo >= tabelaModel.getProdutoVenda(linha).getQuantidade())
							{
								int opcao = JOptionPane.showConfirmDialog(null, "Esse produto já está marcado como Fazendo na cozinha."
										+ "\n\nVocê tem certeza que quer deletar?\n\n", "Deletar Produto", JOptionPane.YES_NO_OPTION);
								if(opcao != JOptionPane.YES_OPTION)
									continua = false;
							}

							if(continua)
							{
								atualizaMesa(new CacheMesaHeader(linha, linha, linha), null, UtilCoffe.INTERFACE_MESA_REMOVE_TABELA);
							}
						}
					}
				}
			};

			maisProduto = new WebButton(new ImageIcon(getClass().getClassLoader().getResource("imgs/plus2.png")));
			maisProduto.setUndecorated(true);
			maisProduto.setPreferredSize(new Dimension(28, 24));
			maisProduto.addActionListener(al);

			menosProduto = new WebButton(new ImageIcon(getClass().getClassLoader().getResource("imgs/remove.png")));
			menosProduto.setUndecorated(true);
			menosProduto.setPreferredSize(new Dimension(28, 24));
			menosProduto.addActionListener(al);

			add(maisProduto);
			add(menosProduto);
		}

		public void setLinha(int li) {
			linha = li;
		}
	}

	class OpcoesCell extends AbstractCellEditor implements TableCellEditor, TableCellRenderer
	{
		private static final long serialVersionUID = 1L;
		OpcoesCellComponent cellOpcoes;
		Color alternate = new Color(206, 220, 249);

		public OpcoesCell() {
			cellOpcoes = new OpcoesCellComponent();
		}	

		@Override
		public Object getCellEditorValue() {
			return null;
		}

		@Override
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			atualizaPainel(row);
			return cellOpcoes;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			atualizaPainel(row);
			return cellOpcoes;
		}

		public void atualizaPainel(int row)
		{
			if(row % 2 == 0)
				cellOpcoes.setBackground(alternate);
			else
				cellOpcoes.setBackground(Color.WHITE);

			cellOpcoes.setLinha(row);
		}
	}
	
	private class FuncionariosComboModel extends AbstractListModel<Object> implements ComboBoxModel<Object> 
	{
		private static final long serialVersionUID = 1L;
		private List<Funcionario> listFuncionarios;
	    private Funcionario selectedFuncionario;
	    private final static int FIRSTINDEX = 0;
	     
	    public FuncionariosComboModel(List<Funcionario> listFun) {
	    	this.listFuncionarios = listFun;
	        if (getSize() > 0) {
	            setSelectedItem(this.listFuncionarios.get(FIRSTINDEX));
	        }
	    }
	    
		public void refreshModelFuncionarios() {
			fireContentsChanged(this, 0, 0);
		}
	     
	    @Override
	    public Object getElementAt(int index) {
	        return listFuncionarios.get(index);
	    }
	 
	    @Override
	    public int getSize() {
	        return listFuncionarios.size();
	    }
	 
	    @Override
	    public Object getSelectedItem() {
	        return selectedFuncionario;
	    }
	 
	    @Override
	    public void setSelectedItem(Object anItem) {
	    	selectedFuncionario = (Funcionario) anItem;
	    }
	}
	
	public int verificaStatusPedido(int local, ProdutoVenda produto)
	{
		int produtos_fazendo = 0;
		for(int i = 0; i < todosPedidos.size(); i++)
		{
			if(todosPedidos.get(i).getLocal() == local)
			{
				if(todosPedidos.get(i).getProduto().compareTo(produto))
				{	
					if(todosPedidos.get(i).getStatus() == UtilCoffe.PEDIDO_FAZENDO)
						produtos_fazendo += todosPedidos.get(i).getProduto().getQuantidade();
				}
			}
		}
		return produtos_fazendo;
	}
	
	public int enviarVenda(CacheVendaFeita v, ObjectOutputStream socket)
	{
		int venda_id = 0;
		
		try {
    		long duration = System.currentTimeMillis() - v.getVendaFeita().getData().getTime();
    		long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);  
			
			String formatacao;
			Query envia = new Query();
			formatacao = "INSERT INTO vendas(total, atendente, ano, mes, dia_mes, dia_semana, horario, forma_pagamento, valor_pago, "
					+ "troco, fiado_id, caixa, delivery, dezporcento, permanencia, data, hora) VALUES('"
			+ v.getTotal() +
			"', '" + v.getAtendente() +
			"', " + v.getAno() + ", "
			+ v.getMes() + ", "
			+ v.getDia_mes() + ", "
			+ v.getDia_semana() +
			", '" + v.getHorario() + "', '" + v.getForma_pagamento() + "', '" + v.getValor_pago() + "', '" + v.getTroco()
			+ "', " + v.getFiado_id() + ", " + (v.getVendaMesa().getMesaId()+1) + ", '0,00', '" + v.getDezporcento() 
			+ "', '" + minutes + "', CURDATE(), CURTIME());";
			envia.executaUpdate(formatacao);				
			
			Query pega = new Query();
			pega.executaQuery("SELECT vendas_id FROM vendas ORDER BY vendas_id DESC limit 0, 1");
			
			if(pega.next())
			{
				venda_id = pega.getInt("vendas_id");
				String pegaPreco = "";
				
				for(int i = 0; i < v.getVendaFeita().getQuantidadeProdutos(); i++)
				{
					formatacao = "UPDATE mesas SET `pago` = (`pago` + " + v.getVendaFeita().getProduto(i).getQuantidade() + ") WHERE `mesas_id` = " + v.getVendaMesa().getMesaId()
					+ " AND `produto` = " + v.getVendaFeita().getProduto(i).getIdUnico() + " AND `adicionais` = '" + v.getVendaFeita().getProduto(i).getAllAdicionaisId() + "';";
					envia.executaUpdate(formatacao);
					
					pegaPreco = String.format("%.2f", (v.getVendaFeita().getProduto(i).getPreco() * v.getVendaFeita().getProduto(i).getQuantidade()));
					pegaPreco.replaceAll(",", ".");						
					
					formatacao = "INSERT INTO vendas_produtos(id_link, nome_produto, adicionais_produto, preco_produto, quantidade_produto, dia, mes, ano, data) VALUES('"
							+ venda_id +
							"', '" + v.getVendaFeita().getProduto(i).getNome() +
							"', '" + v.getVendaFeita().getProduto(i).getAllAdicionais() + "', '" + pegaPreco + "', '" + v.getVendaFeita().getProduto(i).getQuantidade() + 
							"', " + v.getDia_mes() + ", " + v.getMes() + ", " + v.getAno() + ", CURDATE());";
							envia.executaUpdate(formatacao);						
				}
			}
			
			envia.fechaConexao();
			pega.fechaConexao();
			
			if(venda_id > 0)
			{
				if(socket == null)
				{
					CacheMesaHeader mh = new CacheMesaHeader(v.getVendaMesa().getMesaId(), v.getVendaFeita(), UtilCoffe.MESA_ATUALIZAR2);
					((Servidor) modoPrograma).enviaTodos(mh);
					
					CacheAviso aviso = new CacheAviso(1, v.getClasse(), "A venda foi concluída com sucesso!", "Venda #" + venda_id);
					avisoRecebido(aviso);
				}
				else
				{
					CacheMesaHeader mh = new CacheMesaHeader(v.getVendaMesa().getMesaId(), v.getVendaFeita(), UtilCoffe.MESA_ATUALIZAR2);
					((Servidor) modoPrograma).enviaTodos(mh);
					
					CacheAviso aviso = new CacheAviso(1, v.getClasse(), "A venda foi concluída com sucesso!", "Venda #" + venda_id);
					((Servidor) modoPrograma).enviaObjeto(aviso, socket);
				}
				
				DiarioLog.add(v.getAtendente(), "Adicionou a Venda #" + venda_id + " de R$" + v.getTotal() + ".", 1);
				painelListener.atualizarPainel(new Header(UtilCoffe.UPDATE_VENDAS));
				if(v.getFiado_id() > 0)
					painelListener.atualizarPainel(new Header(UtilCoffe.UPDATE_FIADOS));
				
				termina(false);
				return venda_id;
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			new PainelErro(e);
			return 0;
		}				
		
		return 0;
	}
	
	public void refreshModelFuncionarios() {
		funcionarioModel.refreshModelFuncionarios();
	}
	
	public void refreshConfig()
	{
		SwingUtilities.invokeLater(new Runnable() {  
			public void run() {
				if(config.getDezPorcento())
				{
					adicionarDezPorcento.setEnabled(true);
					adicionarDezPorcento.setSelected(false);
					adicionarDezPorcento.revalidate();
					adicionarDezPorcento.repaint();
					atualizarCampoRecibo();
				}
				else
				{
					adicionarDezPorcento.setEnabled(false);
					adicionarDezPorcento.setSelected(false);
					adicionarDezPorcento.revalidate();
					adicionarDezPorcento.repaint();
					atualizarCampoRecibo();
				}
			}
		});
	}
	
	public void avisoRecebido(CacheAviso objeto)
	{
		aviso = objeto;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if(aviso != null)
				{
					if(aviso.getTipo() == 1)
					{
						if(!config.getReciboFim())
							JOptionPane.showMessageDialog(null, aviso.getMensagem(), aviso.getTitulo(), JOptionPane.INFORMATION_MESSAGE);
						else
						{
							int opcao = JOptionPane.showConfirmDialog(null, aviso.getMensagem() 
									+ "\n\nDeseja imprimir o recibo?", "Venda #" + aviso.getTitulo(), JOptionPane.YES_NO_OPTION);
							if(opcao == JOptionPane.YES_OPTION)
							{
								criarRecibo();
							}			
						}		

						setMesa(mesaID, vendaMesa);
						mesaListener.atualizarMesa(mesaID);
						termina(false);					
					}
					else
					{
						JOptionPane.showMessageDialog(null, aviso.getMensagem(), aviso.getTitulo(), JOptionPane.ERROR_MESSAGE);
					}				
				}
			}
		});
	}
	
	public synchronized void atualizaMesa(CacheMesaHeader m, ObjectOutputStream socket, short operacao) 
	{
		if(operacao == UtilCoffe.INTERFACE_SETAR_MESA)
		{
			mesaListener.getVendaMesas().set(m.getMesaId(), m.getMesaVenda());
		}
		else if(operacao > 0)
		{
			if(operacao == UtilCoffe.INTERFACE_MESA_ADD)
			{
				ProdutoVenda produto = new ProdutoVenda(addProduto.getProdutoSelecionado().getNome(), 
						addProduto.getProdutoSelecionado().getReferencia(), 
						addProduto.getProdutoSelecionado().getPreco(), 
						addProduto.getProdutoSelecionado().getIdUnico(), 
						addProduto.getProdutoSelecionado().getCodigo());

				if(!UtilCoffe.vaziu(campoComentario.getText()))
					produto.setComentario(campoComentario.getText());
				
				if(addAdicional.size() > 0)
				{
					for(int x = 0 ; x < addAdicional.size() ; x++)
					{
						produto.adicionrAdc(UtilCoffe.cloneProduto(addAdicional.get(x).getProdutoSelecionado()));
					}
				}

				String limpeza = UtilCoffe.limpaNumero(campoQuantidade.getText());
				if(!UtilCoffe.vaziu(limpeza) && limpeza.length() < 6)
				{
					int sizeAntes = vendaMesa.getQuantidadeProdutos();

					if(Integer.parseInt(limpeza) > 0)
					{
						if(vendaMesa.getQuantidadeProdutos() <= 0)
							vendaMesa.setData(new Date());
						
						produto.setQuantidade(Integer.parseInt(limpeza), 0);
						vendaMesa.adicionarProduto(produto);
						tabelaModel.refreshTable();
						dragAdicionaProduto(produto);

						if(sizeAntes == vendaMesa.getQuantidadeProdutos())
						{
							mesaListener.atualizarMesa(mesaID);
							CacheMesaHeader mh = new CacheMesaHeader(mesaID, produto, vendaMesa, UtilCoffe.MESA_ATUALIZAR, 
													Integer.parseInt(limpeza), campoFuncionario.getSelectedItem().toString());
							if(config.getModo() == UtilCoffe.SERVER) {
								atualizaMesa(mh, null, (short)0);
							}
							else {
								((Client) modoPrograma).enviarObjeto(mh);
							}
						}
						else
						{
							mesaListener.atualizarMesa(mesaID);
							CacheMesaHeader mh = new CacheMesaHeader(mesaID, produto, vendaMesa, UtilCoffe.MESA_ADICIONAR, 
									Integer.parseInt(limpeza), campoFuncionario.getSelectedItem().toString());
							
							if(config.getModo() == UtilCoffe.SERVER) {
								atualizaMesa(mh, null, (short)0);
							}
							else {
								((Client) modoPrograma).enviarObjeto(mh);
							}
						}
					}
				}
				
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if(addProduto.getProdutoSelecionado() != null)
							campoValor.setText(UtilCoffe.doubleToPreco(addProduto.getProdutoSelecionado().getPreco()));
						else
							campoValor.setText("");
						campoQuantidade.setText("1");
						campoComentario.setText("");
						addAdicional.clear();
						addRemover.clear();
						adicionaisPainel.removeAll();
						adicionaisPainel.revalidate();
						adicionaisPainel.repaint();
						addProduto.requestFocus();
					}
				});
			}
			else if(operacao == UtilCoffe.INTERFACE_MESA_ADD_TABELA)
			{
				final CacheMesaHeader x = m;
				
				SwingUtilities.invokeLater(new Runnable() {  
					public void run() {  
						if(x.getHeader() < tabelaModel.getRowCount() && x.getHeader() >= 0)
						{
							tabelaModel.getProdutoVenda(x.getHeader()).setQuantidade(1, 1);
							tabelaModel.getProdutoVenda(x.getHeader()).calcularPreco();
							tabelaModel.refreshTable();
							vendaMesa.calculaTotal();
							
							ProdutoVenda maisUm = UtilCoffe.cloneProdutoVenda(tabelaModel.getProdutoVenda(x.getHeader()));
							maisUm.setQuantidade(1, 0);
							dragAdicionaProduto(maisUm);
							
							mesaListener.atualizarMesa(mesaID);

							CacheMesaHeader mh = new CacheMesaHeader(mesaID, maisUm, vendaMesa, UtilCoffe.MESA_ATUALIZAR, 1, campoFuncionario.getSelectedItem().toString());
							if(config.getModo() == UtilCoffe.SERVER) {
								atualizaMesa(mh, null, (short)0);
							}
							else {
								((Client) modoPrograma).enviarObjeto(mh);
							}
						}
					}
				});
			}
			else if(operacao == UtilCoffe.INTERFACE_MESA_REMOVE_TABELA)
			{
				final CacheMesaHeader x = m;
				
				SwingUtilities.invokeLater(new Runnable() {  
					public void run() {
						
						if(x.getHeader() >= 0 && x.getHeader() < tabelaModel.getRowCount())
						{
							boolean deletar_all = true;
							if(tabelaModel.getProdutoVenda(x.getHeader()).getQuantidade() > 1) {
								deletar_all = false;
							}

							int quantidadeDeletar = 1;
							for(int i = 0; i < painelDropOut.getComponentCount(); i++)
							{	  
								DragLabel dragL = (DragLabel)painelDropOut.getComponent(i);
								if(dragL.getProduto().compareTo(tabelaModel.getProdutoVenda(x.getHeader())))
								{
									painelDropOut.remove(i);
									quantidadeDeletar = 0;
									break;
								}
							}

							if(quantidadeDeletar > 0 && painelDropOut.getComponentCount() > 0)
							{
								DragLabel dragL = (DragLabel)painelDropOut.getComponent(0);
								if(dragL.getProduto().compareTo(tabelaModel.getProdutoVenda(x.getHeader())))
								{
									painelDropOut.remove(0);
									quantidadeDeletar = 0;
								}	    		  
							}

							if(quantidadeDeletar > 0)
							{
								for(int i = 0; i < painelDropIn.getComponentCount(); i++)
								{ 
									DragLabel dragL = (DragLabel)painelDropIn.getComponent(i);
									if(dragL.getProduto().compareTo(tabelaModel.getProdutoVenda(x.getHeader())))
									{
										painelDropIn.remove(i);
										quantidadeDeletar = 0;
										break;
									}
								}		    		  
							}

							if(quantidadeDeletar > 0 && painelDropIn.getComponentCount() > 0)
							{
								DragLabel dragL = (DragLabel)painelDropIn.getComponent(0);
								if(dragL.getProduto().compareTo(tabelaModel.getProdutoVenda(x.getHeader())))
								{
									painelDropIn.remove(0);
									quantidadeDeletar = 0;
								}	    		  
							}

							painelDropOut.revalidate();
							painelDropOut.repaint();
							painelDropIn.revalidate();
							painelDropIn.repaint();

							if(deletar_all)
							{
								ProdutoVenda pv = UtilCoffe.cloneProdutoVenda(tabelaModel.getProdutoVenda(x.getHeader()));
								tabelaModel.removeRow(x.getHeader());
								vendaMesa.calculaTotal();
								mesaListener.atualizarMesa(mesaID);

								CacheMesaHeader mh = new CacheMesaHeader(mesaID, pv, vendaMesa, UtilCoffe.MESA_DELETAR, 1);
								if(config.getModo() == UtilCoffe.SERVER) {
									atualizaMesa(mh, null, (short)0);
								}
								else {
									((Client) modoPrograma).enviarObjeto(mh);
								}
							}
							else
							{
								ProdutoVenda prod = UtilCoffe.cloneProdutoVenda(tabelaModel.getProdutoVenda(x.getHeader()));
								prod.setQuantidade(1, 0);

								tabelaModel.getProdutoVenda(x.getHeader()).setQuantidade(1, 2);
								tabelaModel.getProdutoVenda(x.getHeader()).calcularPreco();
								tabelaModel.refreshTable();
								vendaMesa.calculaTotal();
								mesaListener.atualizarMesa(mesaID);

								CacheMesaHeader mh = new CacheMesaHeader(mesaID, prod, vendaMesa, UtilCoffe.MESA_ATUALIZAR, -1);
								if(config.getModo() == UtilCoffe.SERVER) {
									atualizaMesa(mh, null, (short)0);
								}
								else {
									((Client) modoPrograma).enviarObjeto(mh);
								}
							}

							double total = 0.0;
							for(int i = 0; i < painelDropOut.getComponentCount(); i++)
							{
								DragLabel dragL = (DragLabel)painelDropOut.getComponent(i);
								total += dragL.getProduto().getTotalProduto();
							}

							if(config.getDezPorcento())
							{
								taxaOpcional = total * 0.10;
								adicionarDezPorcento.setText("<html>+ 10% Opcional: <br>(R$" + UtilCoffe.doubleToPreco(taxaOpcional) + ")</html>");
							}

							if(adicionarDezPorcento.isSelected())
								campoTotal.setText(UtilCoffe.doubleToPreco((total + (total * 0.10))));
							else
								campoTotal.setText(UtilCoffe.doubleToPreco(total));

							atualizarCampoRecibo();	
						}
					}
				});						
			}
			else if(operacao == UtilCoffe.INTERFACE_MESA_REMOVE_TABELA2)
			{
				int quantidadeDeletar = tabelaModel.getProdutoVenda(tabelaPedido.getSelectedRow()).getQuantidade();
				
				for(int i = 0; i < painelDropOut.getComponentCount(); i++)
				{	  
					DragLabel dragL = (DragLabel)painelDropOut.getComponent(i);
					if(dragL.getProduto().compareTo(tabelaModel.getProdutoVenda(tabelaPedido.getSelectedRow())))
					{
						painelDropOut.remove(i);
						quantidadeDeletar--;
						i = 0;

						if(quantidadeDeletar <= 0)
							break;		
					}
				}

				if(quantidadeDeletar > 0 && painelDropOut.getComponentCount() > 0)
				{
					DragLabel dragL = (DragLabel)painelDropOut.getComponent(0);
					if(dragL.getProduto().compareTo(tabelaModel.getProdutoVenda(tabelaPedido.getSelectedRow())))
					{
						painelDropOut.remove(0);
						quantidadeDeletar--;
					}    		  
				}

				if(quantidadeDeletar > 0)
				{
					for(int i = 0; i < painelDropIn.getComponentCount(); i++)
					{ 
						DragLabel dragL = (DragLabel)painelDropIn.getComponent(i);
						if(dragL.getProduto().compareTo(tabelaModel.getProdutoVenda(tabelaPedido.getSelectedRow())))
						{
							painelDropIn.remove(i);
							quantidadeDeletar--;
							i = 0;

							if(quantidadeDeletar <= 0)
								break;		
						}
					}		    		  
				}

				if(quantidadeDeletar > 0 && painelDropIn.getComponentCount() > 0)
				{
					DragLabel dragL = (DragLabel)painelDropIn.getComponent(0);
					if(dragL.getProduto().compareTo(tabelaModel.getProdutoVenda(tabelaPedido.getSelectedRow())))
					{
						painelDropIn.remove(0);
						quantidadeDeletar--;
					} 	    		  
				}
				
				if(tabelaPedido.getSelectedRow() >= 0 && tabelaPedido.getSelectedRowCount() == 1) 
				{
					ProdutoVenda prod = UtilCoffe.cloneProdutoVenda(tabelaModel.getProdutoVenda(tabelaPedido.getSelectedRow()));
					tabelaModel.removeRow(tabelaPedido.getSelectedRow());
					vendaMesa.calculaTotal();
					mesaListener.atualizarMesa(mesaID);
					
					CacheMesaHeader mh = new CacheMesaHeader(mesaID, prod, vendaMesa, 
							UtilCoffe.MESA_DELETAR, prod.getQuantidade(), campoFuncionario.getSelectedItem().toString());
					
					if(config.getModo() == UtilCoffe.SERVER) {
						atualizaMesa(mh, null, (short)0);
					}
					else {
						((Client) modoPrograma).enviarObjeto(mh);
					}
				}

				double total = 0.0;
				for(int i = 0; i < painelDropOut.getComponentCount(); i++)
				{
					DragLabel dragL = (DragLabel)painelDropOut.getComponent(i);
					total += dragL.getProduto().getTotalProduto();
				}

				if(config.getDezPorcento())
				{
					taxaOpcional = total * 0.10;
					adicionarDezPorcento.setText("<html>+ 10% Opcional: <br>(R$" + UtilCoffe.doubleToPreco(taxaOpcional) + ")</html>");
				}

				if(adicionarDezPorcento.isSelected())
					campoTotal.setText(UtilCoffe.doubleToPreco((total + (total * 0.10))));
				else
					campoTotal.setText(UtilCoffe.doubleToPreco(total));

				atualizarCampoRecibo();
				SwingUtilities.invokeLater(new Runnable() {  
					public void run() {  
						painelDropOut.revalidate();
						painelDropOut.repaint();
						painelDropIn.revalidate();
						painelDropIn.repaint();
					}  
				});
			}
		}
		else
		{
			switch(m.getHeader())
			{
				case UtilCoffe.MESA_ADICIONAR:
				{
					try {
						String formatacao;
						Query envia = new Query();
						formatacao = "INSERT INTO mesas(mesas_id, produto, quantidade, pago, adicionais, comentario, data) VALUES("
						+ m.getMesaId() + ", " + m.getProdutoMesa().getIdUnico() + ", " + m.getHeaderExtra() + ", 0, '" 
						+ m.getProdutoMesa().getAllAdicionaisId() + "', '" + m.getProdutoMesa().getComentario() + "', '"
						+ m.getMesaVenda().getDataString() + "');";
						envia.executaUpdate(formatacao);
						envia.fechaConexao();
						
						if(socket != null) {
							mesaListener.getVendaMesas().set(m.getMesaId(), m.getMesaVenda());
							mesaListener.atualizarMesa(m.getMesaId());
						}
						
						((Servidor) modoPrograma).enviaTodos(m, socket);
						Pedido ped = new Pedido(m.getProdutoMesa(), m.getAtendente(), (m.getMesaId()+1));
						ped.getProduto().setQuantidade(m.getHeaderExtra(), 0);
						painelListener.atualizarPainel(ped);
						
						if(Usuario.INSTANCE.getOlhando() == m.getMesaId() && socket != null) {
							painelListener.atualizarPainel(new Header(UtilCoffe.UPDATE_VENDA_MESA, new Integer(m.getMesaId()), 
									mesaListener.getVendaMesas().get(m.getMesaId())));
						}
					} catch (ClassNotFoundException | SQLException e) {
						e.printStackTrace();
						((Servidor) modoPrograma).enviaObjeto(new CacheMesaHeader(m.getMesaId(), 
								mesaListener.getVendaMesas().get(m.getMesaId()), UtilCoffe.MESA_ERROR), socket);
						new PainelErro(e);
					}
					
					break;
				}
				case UtilCoffe.MESA_ATUALIZAR:
				{
					try {
						String formatacao;
						Query envia = new Query();
						formatacao = "UPDATE mesas SET `quantidade` = (`quantidade` + " + m.getHeaderExtra() + ") WHERE `mesas_id` = " + m.getMesaId()
						+ " AND `produto` = " + m.getProdutoMesa().getIdUnico() 
						+ " AND `adicionais` = '" + m.getProdutoMesa().getAllAdicionaisId() 
						+ "' AND `comentario` = '" + m.getProdutoMesa().getComentario() + "';";						
						envia.executaUpdate(formatacao);
						envia.fechaConexao();
						
						if(socket != null) {
							mesaListener.getVendaMesas().set(m.getMesaId(), m.getMesaVenda());
							mesaListener.atualizarMesa(m.getMesaId());
						}
						
						((Servidor) modoPrograma).enviaTodos(m, socket);
						ProdutoVenda pNovo = UtilCoffe.cloneProdutoVenda(m.getProdutoMesa());
						
						/* adicionar pedido */
						if(m.getHeaderExtra() > 0) // se for menor que zero ele ta deletando um pedido..
						{
							Pedido ped = new Pedido(pNovo, m.getAtendente(), (m.getMesaId()+1));
							painelListener.atualizarPainel(ped);
						}
						else
						{
							Pedido ped = new Pedido(pNovo, m.getAtendente(), (m.getMesaId()+1));
							ped.setHeader(UtilCoffe.PEDIDO_STATUS);
							painelListener.atualizarPainel(ped);						
						}
						
						if(Usuario.INSTANCE.getOlhando() == m.getMesaId() && socket != null) {
							painelListener.atualizarPainel(new Header(UtilCoffe.UPDATE_VENDA_MESA, new Integer(m.getMesaId()), 
									mesaListener.getVendaMesas().get(m.getMesaId())));
						}
					} catch (ClassNotFoundException | SQLException e) {
						e.printStackTrace();
						((Servidor) modoPrograma).enviaObjeto(new CacheMesaHeader(m.getMesaId(), 
								mesaListener.getVendaMesas().get(m.getMesaId()), UtilCoffe.MESA_ERROR), socket);
						new PainelErro(e);
					}
					
					break;
				}
				case UtilCoffe.MESA_ATUALIZAR2:
				{
					if(socket != null) {
						mesaListener.getVendaMesas().set(m.getMesaId(), m.getMesaVenda());
						mesaListener.atualizarMesa(m.getMesaId());
					}
					
					if(Usuario.INSTANCE.getOlhando() == m.getMesaId() && socket != null) {
						painelListener.atualizarPainel(new Header(UtilCoffe.UPDATE_VENDA_MESA, new Integer(m.getMesaId()), 
								mesaListener.getVendaMesas().get(m.getMesaId())));
					}
					
					((Servidor) modoPrograma).enviaTodos(m, socket);
					break;
				}
				case UtilCoffe.MESA_TRANSFERIR:
				{
					try {
						Query transfere = new Query();
						transfere.executaUpdate("UPDATE mesas SET mesas_id = " + m.getHeaderExtra() + " WHERE mesas_id = " + m.getMesaId());
						transfere.fechaConexao();
						
						for(int i = 0; i < mesaListener.getVendaMesas().get(m.getMesaId()).getQuantidadeProdutos(); i++) {
							mesaListener.getVendaMesas().get(m.getHeaderExtra()).getProdutos().add(UtilCoffe.cloneProdutoVenda(mesaListener.getVendaMesas().get(m.getMesaId()).getProduto(i)));
						}
						
						mesaListener.getVendaMesas().get(m.getMesaId()).clear();
						mesaListener.getVendaMesas().get(m.getMesaId()).calculaTotal();
						mesaListener.getVendaMesas().get(m.getHeaderExtra()).calculaTotal();
						
						mesaListener.atualizarMesa(m.getMesaId());
						mesaListener.atualizarMesa(m.getHeaderExtra());
						
						if(Usuario.INSTANCE.getOlhando() == m.getMesaId() && socket != null) {
							painelListener.atualizarPainel(new Header(UtilCoffe.UPDATE_VENDA_MESA, new Integer(m.getMesaId()), 
									mesaListener.getVendaMesas().get(m.getMesaId())));
						}
						else if(Usuario.INSTANCE.getOlhando() == m.getHeaderExtra() && socket != null) {
							painelListener.atualizarPainel(new Header(UtilCoffe.UPDATE_VENDA_MESA, new Integer(m.getHeaderExtra()), 
									mesaListener.getVendaMesas().get(m.getHeaderExtra())));
						}
						
						Pedido ped = new Pedido(new ProdutoVenda(), m.getAtendente(), (m.getMesaId()+1));
						ped.setStatus((m.getHeaderExtra()+1));
						ped.setHeader(UtilCoffe.PEDIDO_TRANSFERE);
						painelListener.atualizarPainel(ped);
							
						NotificationManager.setLocation(2);
						NotificationManager.showNotification(this, config.getTipoNome() + " " + (m.getMesaId()+1) + " transferida para " + (m.getHeaderExtra()+1), 
								new ImageIcon(getClass().getClassLoader().getResource("imgs/notifications_ok.png"))).setDisplayTime(2000);
						
						CacheMesaHeader newCache = new CacheMesaHeader(m.getMesaId(), m.getHeaderExtra(), UtilCoffe.MESA_TRANSFERIR, mesaListener.getVendaMesas().get(m.getHeaderExtra()));
						((Servidor) modoPrograma).enviaTodos(newCache);
					} catch (ClassNotFoundException | SQLException e) {
						e.printStackTrace();
						new PainelErro(e);
					}
					break;
				}
				default:	// delete ou limpar
				{
					boolean termina = false;
					try {
						Query pega = new Query();
						if(m.getHeader() == UtilCoffe.MESA_DELETAR)
						      pega.executaUpdate("DELETE FROM mesas WHERE `produto` = " + m.getProdutoMesa().getIdUnico() 
						    		  + " AND `adicionais` = '" + m.getProdutoMesa().getAllAdicionaisId()
						    		  + "' AND `comentario` = '" + m.getProdutoMesa().getComentario()
						    		  + "' AND `mesas_id` = " + m.getMesaId() + ";");
						
						pega.executaQuery("SELECT * FROM mesas WHERE `quantidade` != `pago` AND `mesas_id` = "+ m.getMesaId() +";");
						if(!pega.next())
						{
							pega.executaUpdate("DELETE FROM mesas WHERE `mesas_id` = "+ m.getMesaId() +";");
							m.getMesaVenda().clear();
							termina = true;
						}
						
						pega.fechaConexao();
						
						if(m.getHeader() == UtilCoffe.MESA_DELETAR)
						{
							m.getProdutoMesa().setQuantidade(m.getHeaderExtra(), 0);
							Pedido ped = new Pedido(m.getProdutoMesa(), m.getAtendente(), (m.getMesaId()+1));
							ped.setHeader(UtilCoffe.PEDIDO_STATUS);
							painelListener.atualizarPainel(ped);
						}
						
					} catch (ClassNotFoundException | SQLException e) {
						e.printStackTrace();
						((Servidor) modoPrograma).enviaObjeto(new CacheMesaHeader(m.getMesaId(), 
								mesaListener.getVendaMesas().get(m.getMesaId()), UtilCoffe.MESA_ERROR), socket);
						new PainelErro(e);
					} finally {
						if(m.getHeader() == UtilCoffe.MESA_LIMPAR)
						{
							if(termina)
							{
								mesaListener.getVendaMesas().set(m.getMesaId(), m.getMesaVenda());
								mesaListener.atualizarMesa(m.getMesaId());
								((Servidor) modoPrograma).enviaTodos(m);
								
								if(Usuario.INSTANCE.getOlhando() == m.getMesaId() && socket != null) {
									painelListener.atualizarPainel(new Header(UtilCoffe.UPDATE_VENDA_MESA, new Integer(m.getMesaId()), 
											mesaListener.getVendaMesas().get(m.getMesaId())));
								}
							}					
						}
						else if(m.getHeader() == UtilCoffe.MESA_DELETAR)
						{
							if(!termina)
							{
								if(socket != null) {
									mesaListener.getVendaMesas().set(m.getMesaId(), m.getMesaVenda());
									mesaListener.atualizarMesa(m.getMesaId());
								}
								
								if(Usuario.INSTANCE.getOlhando() == m.getMesaId() && socket != null) {
									painelListener.atualizarPainel(new Header(UtilCoffe.UPDATE_VENDA_MESA, new Integer(m.getMesaId()), 
											mesaListener.getVendaMesas().get(m.getMesaId())));
								}
								
								((Servidor) modoPrograma).enviaTodos(m, socket);
							}
							else
							{
								mesaListener.getVendaMesas().set(m.getMesaId(), m.getMesaVenda());
								mesaListener.atualizarMesa(m.getMesaId());
								((Servidor) modoPrograma).enviaTodos(m);
								
								if(Usuario.INSTANCE.getOlhando() == m.getMesaId() && socket != null) {
									painelListener.atualizarPainel(new Header(UtilCoffe.UPDATE_VENDA_MESA, new Integer(m.getMesaId()), 
											mesaListener.getVendaMesas().get(m.getMesaId())));
								}
							}	
						}
					}
				}
			}
		}
	}
}
package view;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import controller.MainController;

public class MainView extends JFrame implements ActionListener{
	
	private MainController controller;
	private JTextField path;
	private JButton find;
	private JButton calculate;
	private JButton clean;
	private JTextArea evaluation;
	
	public MainView(MainController controller) {
		super("BPMN Evaluator");
		
		this.controller = controller;
		
		setLayout(new FlowLayout());
		
		setGUI();
		
		setSize(600, 800);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	private void setGUI() {
		path = new JTextField(40);
		find = new JButton("Select Diagram");
		calculate = new JButton("Calculate");
		clean = new JButton("Clean");
		evaluation = new JTextArea(40, 50);
		
		find.addActionListener(this);
		calculate.addActionListener(this);
		clean.addActionListener(this);
		
		evaluation.setLineWrap(true);
		evaluation.setWrapStyleWord(true);
		JScrollPane textArea = new JScrollPane(evaluation);
		
		add(path);
		add(find);
		add(calculate);
		add(clean);
		add(textArea);
		
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == find) {
			JFileChooser fileChooser = new JFileChooser();
			int returnValue = fileChooser.showOpenDialog(getParent());
			if(returnValue == JFileChooser.APPROVE_OPTION) {
				File diagram = fileChooser.getSelectedFile();
				path.setText(diagram.getPath());
			}
		} else if(e.getSource() == calculate) {
			evaluation.setText("loading...");
			evaluation.setText(controller.calculate(path.getText()));
		} else if(e.getSource() == clean) {
			path.setText("");
			evaluation.setText("");
		} 
	}
}

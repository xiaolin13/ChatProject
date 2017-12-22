package lin.server.view;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import lin.dao.Save;
import lin.server.ClientThread;
import lin.server.ServerThread;
import lin.utils.SendUtils;

public class ServerGUI{

	//用来保存当前在线用户的信息
	
	public static int count = 0;
	private JFrame frame;
	private JPanel panel; 
	
	private JLabel portLabel;
	private JTextField portText;
	
	//记录当前连接人数
	private JLabel maxNum;
	public static JTextField maxText;
	
	//
	public static JTextArea showMsg;
	private JTextField txtMsg;
	
	
	private JButton btnSend;
	private JButton btnRecord;
	private JButton bthOther;
	private JButton btnDel;
	private JButton btnFile;
	
	private JButton btnStart;
	private JButton btnStop;
	
	private JScrollPane scroll;
	
	public static JList<String> list;
	public static DefaultListModel<String> model;
	
	//操作主要集中在这个界面
	private ServerThread serverThread;
	public static ArrayList<ClientThread> clients;
	private int port;
	private ServerSocket serverSocket;
	
	private boolean serverRunning = false;
	
	private void init() {
		Font font = new Font("宋体",Font.PLAIN,25);
		showMsg.setEditable(false);
		showMsg.setFont(font);
		
		txtMsg.setFocusable(true);
		txtMsg.setFont(font);
		
		portText.setText("30000");
		maxText.setEditable(false);
		
		btnStop.setEnabled(false);
		btnSend.setEnabled(false);
		btnRecord.setEnabled(false);
		btnDel.setEnabled(false);
		btnFile.setEnabled(false);
		bthOther.setEnabled(false);
		
		list.setFont(new Font("宋体",Font.PLAIN,20));
		String[] str = {"默认群发"};
		list.setListData(str);
		list.setSelectedIndex(0);
	}
	
	private ServerGUI() {
		frame = new JFrame("lin聊天室");
		frame.setSize(900, 635);
		frame.setLocation(520,  230);
		frame.setResizable(false);
		
		panel = new JPanel();
		panel.setLayout(null);
		
		//设置统一的字体大小
		
		
		showMsg = new JTextArea();
		//showMsg.setBounds(10,10, 600, 505);
		//panel.add(showMsg);	
		scroll = new JScrollPane(showMsg);
		scroll.setBounds(10,10, 600, 505);
		panel.add(scroll);
		
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		txtMsg = new JTextField();
		txtMsg.setBounds(10, 530, 700, 50);
		panel.add(txtMsg);
		
		portLabel = new JLabel("端口:");
		portLabel.setBounds(618, 8, 35, 30);
		panel.add(portLabel);
		
		portText = new JTextField();
		portText.setBounds(660, 8, 70, 30);
		panel.add(portText);
		
		btnStart = new JButton("开启");
		btnStart.setBounds(737, 8, 68, 28);
		panel.add(btnStart);
		
		btnStop = new JButton("关闭");
		btnStop.setBounds(810, 8, 68, 28);
		panel.add(btnStop);
		
		maxNum = new JLabel("当前在线人数: ");
		maxNum.setBounds(660, 42, 100, 30);
		panel.add(maxNum);
		
		maxText = new JTextField();
		maxText.setBounds(760, 42, 100, 30);
		panel.add(maxText);
		
		list = new JList<>();
		list.setBounds(620, 80, 245, 300);
		model = new DefaultListModel<>();
		list.setModel(model);
		panel.add(list);
		
		btnSend = new JButton("发送");
		btnSend.setBounds(720, 530, 150, 46);
		panel.add(btnSend);
		
		btnRecord = new JButton("聊天记录");
		btnRecord.setBounds(675,390,180,35);
		btnRecord.setEnabled(false);
		panel.add(btnRecord);
		
		bthOther = new JButton("删除记录");
		bthOther.setBounds(675,435,180,35);
		panel.add(bthOther);
		
		btnDel = new JButton("删除该用户");
		btnDel.setBounds(675,482,180, 35);
		panel.add(btnDel);
		
		btnFile = new JButton("传文件");
		/*btnFile.setBounds(675,482,180, 35);
		panel.add(btnFile);*/
		
		frame.add(panel);
		frame.setVisible(true);
		
		//关闭窗口事件
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if(serverRunning) {
					//关闭服务器函数
					//...
					serverStop();
					JOptionPane.showMessageDialog(frame, "服务器成功关闭!");
				}
				System.exit(0);
			}
		});
		
		btnStart.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if(portText.getText().trim().length() == 0) {
						JOptionPane.showMessageDialog(frame, "端口号不能为空", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					//开启服务器方法
					serverStart();
					if(!serverRunning) //不成功则回退
						return;
					//界面变化事件
					stateChange(true);
				} catch(Exception ev) {
					JOptionPane.showMessageDialog(frame, "服务器启动异常" + ev.getMessage(), "error", JOptionPane.ERROR_MESSAGE);
				}
			}
			
		});
		
		btnStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//关闭服务器方法
				serverStop();
				//界面变化事件
				if(!serverRunning)
					JOptionPane.showMessageDialog(frame, "服务器关闭成功","Success",JOptionPane.INFORMATION_MESSAGE);
				stateChange(false);
			}
		});
		
		btnSend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SendMsg();
			}
			
		});
		
		//删除用户
		btnDel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(list.isSelectionEmpty() || list.getSelectedIndex() == 0) {
					JOptionPane.showMessageDialog(frame, "请选中一个用户", "Error", JOptionPane.ERROR_MESSAGE);
				} else {
					String selectedName = list.getSelectedValue().toString();
					for(int i = clients.size() - 1; i >= 0; i--) {
						if(selectedName.equals(clients.get(i).getUser().getName())) {
							SendUtils.sendData(clients.get(i).getDos(), "DELETE@" + selectedName +"@DELETE");
							break;
						}
					}
					JOptionPane.showMessageDialog(frame, "删除"+selectedName+"成功", "Success",JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		
		btnRecord.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				showMsg.setText(null);
				Save.initDB();
				showMsg.setText(Save.insertRecord());
			}
		});
		
		bthOther.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Save.initDB();
				Save.deleteSystemMsg();
				showMsg.setText(null);
			}
		});
	}
	
	//启动 和 停止后 的界面是不同的
	private void stateChange(boolean state) {
		btnStart.setEnabled(!state);
		btnStop.setEnabled(state);
		btnFile.setEnabled(state);
		btnSend.setEnabled(state);
		btnDel.setEnabled(state);
		bthOther.setEnabled(state);
		btnRecord.setEnabled(state);
		if(state) 
			maxText.setText("" + count);
		else 
			maxText.setText("");
		portText.setEnabled(!state);
	}
	
	//开启服务器
	private void serverStart() {
		port = Integer.parseInt(portText.getText());
		try {
			clients = new ArrayList<ClientThread>();
			serverSocket = new ServerSocket(port);
			serverThread = new ServerThread(serverSocket);
			serverThread.start();
			serverRunning = true;
		} catch(BindException e) {
			serverRunning = false;
			JOptionPane.showMessageDialog(frame, "端口号已被占用， 请更换端口号","Error", JOptionPane.ERROR_MESSAGE);
		} catch(Exception e) {
			JOptionPane.showMessageDialog(frame, "内部发生异常"+e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
			serverRunning = false;
		}
	} 
	
	//关闭服务器
	private void serverStop() {
		if(serverThread != null) {
			//先暂时用着  但该方法已经被淘汰
			for(int i = clients.size() - 1; i >= 0; i--) {
					SendUtils.sendData(clients.get(i).getDos(), "SERVERCLOSE@SERVERCLOSE@SERVERCLOSE");
					clients.get(i).setStop(true);
					clients.get(i).interrupt();
				
			}
			serverThread.setStatus(true);
			serverThread.interrupt();
			try {
				serverThread.getServerSocket().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		serverRunning = false;
	}
	
	//发送消息
	private void SendMsg() {
		if(clients.size() == 0) {
			JOptionPane.showMessageDialog(frame, "没有用户在线！发送失败","Error",JOptionPane.ERROR_MESSAGE);
			txtMsg.setText(null);
			return;
		}
		String message = txtMsg.getText().trim();
		//禁止发空消息
		if(message == null || message.equals("")) {
			JOptionPane.showMessageDialog(frame, "消息不能为空 !","Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		String msg = "";
		
		if(list.isSelectionEmpty() || list.getSelectedIndex() == 0) {
			for(int i = clients.size() - 1; i >= 0; i--) {
				SendUtils.sendData(clients.get(i).getDos(), "SYSTEM@" + message);
			}
		} else {
			String selectedName = list.getSelectedValue().toString();
			for(int i = clients.size() - 1; i >= 0; i--) {
				if(clients.get(i).getUser().getName().equals(selectedName)) {
					SendUtils.sendData(clients.get(i).getDos(), "SYSTEM@" + message);
				}
			}
			msg += "系统" + "私发给" + selectedName + ": ";
		}
		showMsg.append("[系统消息]" + msg + message + "\r\n");
		Save.initDB();
		Save.saveChatMsg("[系统消息]" + msg + message + "\r\n");
		txtMsg.setText(null);
	}

	public static void main(String[] args) {
		new ServerGUI().init();
	}
}

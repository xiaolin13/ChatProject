package lin.client.view;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import lin.client.ClientThread;
import lin.utils.SendUtils;

public class ClientGUI {
	
	public static File file;
	public static File fileSave;
	public static boolean isConnected = false;
	public static List<String> friendList;
	public static JFrame frame;
	public static JTextArea showMsg;  //显示消息
	
	public static String clientName;
	public static DefaultListModel<String> model = null;
	public static List<String> listFriend = new ArrayList<String>();
	public static JList<String> list;  // 显示好友列表
	
	private String name;	
    FriendList friendFrame;
	
	
	private ClientThread clientThread;
	private Socket socket;
	private JPanel panel;
	private DataOutputStream dos;
	
	private JTextField txtMsg;  //写消息
	
	private JButton btnSend;
	private JButton btnFile;
	private JButton btnRecord;
	private JButton btnClearRecord;
	private JButton btnAdd;
	private JButton btnDel;
	
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private JFileChooser fileChooser;
	private JScrollPane scroll;
	
	public List<String> getListFriend() {
		return listFriend;
	}

	public void setListFriend(List<String> listFriend1) {
		listFriend = listFriend1;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
		ClientGUI.clientName = name;
	}

	public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket socket) {
		this.socket = socket;
		//顺带设置 socket 的输入输出端
		try {
			this.dos = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public JFrame getFrame() {
		return frame;
	}

	public void setFrame(JFrame frame) {
		ClientGUI.frame = frame;
	}

	public ClientThread getClientThread() {
		return clientThread;
	}

	public void setClientThread(ClientThread clientThread) {
		this.clientThread = clientThread;
	}

	public ClientGUI() {
		//friendFrame = new FriendList();
		//主界面设计
		friendFrame = null;
		frame = new JFrame("lin聊天室");
		frame.setSize(900, 635);
		frame.setResizable(false);
		frame.setLocation(520, 230);
		
		panel = new JPanel();
		panel.setLayout(null);
		
		//字体大小  作为聊天记录的大小
		Font font = new Font("宋体",Font.PLAIN,25);
		
		showMsg = new JTextArea();
		//showMsg.setText("wojfjek");
		//showMsg.setBackground(Color.white);
		//设置不可改动
		showMsg.setEditable(false);
		//设置字体大小
		showMsg.setFont(font);
		//showMsg.setBounds(10, 10, 600, 500);
		//panel.add(showMsg);
		
		scroll = new JScrollPane(showMsg);
		scroll.setBounds(10,10, 600, 505);
		panel.add(scroll);
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		txtMsg = new JTextField();
		txtMsg.setFont(font);
		txtMsg.setBounds(10, 530, 700, 50);
		txtMsg.setFocusable(true);
		panel.add(txtMsg);
		
		btnSend = new JButton("发送");
		btnSend.setBounds(720, 532, 150, 46);
		panel.add(btnSend);
		
		//list = new JList();
		list = new JList<>();
		list.setBounds(620, 10 , 265, 400);
		model = new DefaultListModel<>();
		list.setModel(model);
		list.setFont(font);
		//list.setBackground(Color.white);
		panel.add(list);
		
		//待解决
		btnRecord = new JButton("聊天记录");
		btnRecord.setBounds(620, 460, 90, 30);
		panel.add(btnRecord);
		
		//待解决
		btnClearRecord = new JButton("删除记录");
		btnClearRecord.setBounds(620,495,90,30);
		panel.add(btnClearRecord);
		
		btnAdd = new JButton("添加好友");
		btnAdd.setBounds(620,420,130,35);
		panel.add(btnAdd);
		
		btnDel = new JButton("删除好友");
		btnDel.setBounds(760,420,130,35);
		panel.add(btnDel);
		
		btnFile = new JButton("传文件");
		btnFile.setBounds(730,475,130,45);
		panel.add(btnFile);
		
		btnRecord.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SendUtils.sendData(dos, "FINDMSG@" + name);
			}
		});
		
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				//重置状态
				//发送断开请求
				if(!socket.isClosed()) {
					SendUtils.sendData(dos, "CLOSE@" + clientThread.getUsername() + "@CLOSE");
					try {
						socket.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				//先这样做，等会再弄
				clientThread.stop = true;
				clientThread.interrupt();
				System.exit(0);
			}
		});
		
		btnSend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String str = null;
				if(list.getSelectedIndex() == 0 || list.isSelectionEmpty()) {
					str = "ALL@" + getName() + "@" + txtMsg.getText();
				} else {
					String source = list.getSelectedValue().split("@")[0].trim();
					str = source + "@"+ getName() + "@" + txtMsg.getText();
					String time = formatter.format(new Date());
					showMsg.append(time + "\r\n" + "你说：" + txtMsg.getText() + "\r\n");
				}
				SendUtils.sendData(dos, str);
				txtMsg.setText(null);
			}
		});
		
		btnAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				friendFrame = new FriendList();
			}
		});
		
		btnDel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String selectedName = list.getSelectedValue();
				SendUtils.sendData(dos, "DELFRIEND@" + name + "@" + list.getSelectedValue().split("@")[0].trim());
				model.removeElement(selectedName);
			}
		});
		
		btnFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				fileChooser = new JFileChooser("F:/temp");
				fileChooser.showOpenDialog(frame);
				file = fileChooser.getSelectedFile();
				if(file == null)
					return;
				SendUtils.sendData(dos, "FILE@" + name + "@" + list.getSelectedValue().split("@")[0].trim() + "@" + file.getName());
			}
		});
		
		btnClearRecord.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SendUtils.sendData(dos, "deleteUserMsg@" + name + "@deleteUserMsg");
				showMsg.setText(null); 
			}
			
		});
		frame.add(panel);
		//frame.pack();
		frame.setVisible(false);
	}

	//初始化好友列表
	public void initFriend(String name) {
		SendUtils.sendData(dos, "INITFRIEND@" + name + "@INITFRIEND");
	}

	public void setFrameName(String str) {
		frame.setName(str);
	}
}

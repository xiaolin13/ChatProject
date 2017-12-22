package lin.client.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import lin.client.ClientThread;
import lin.utils.SendUtils;

public class Login{

	public static boolean check = false;
	public static JFrame frame;
	private ClientGUI clientGUI;
	private JPanel panel;
	
	private JLabel userLabel;
	private JTextField userText;
	
	private JLabel passwordLabel;
	private JPasswordField passwordText;
	
	private JLabel ipLabel;
	private JTextField ipText;
	
	private JLabel portLabel;
	private JTextField portText;
	
	private JButton loginButton;
	
	boolean loginStatus;
	
	public Login() {
		//初始化 用户界面
		clientGUI = new ClientGUI();
		
		loginStatus = false;
		frame = new JFrame("login");
		//设置登陆界面大小
		frame.setSize(400, 230);
		//不可设置大小
		frame.setResizable(false);
		//初试时在window窗体下的坐标
		frame.setLocation(720,420);
		//窗口关闭事件
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//new 一个panel 出来 并添加到 frame 里去
		panel = new JPanel();
		frame.add(panel);
		
		placeComponents(panel);
		
		frame.setVisible(true);
	}

	
	//放置控件
	private void placeComponents(JPanel panel2) {
		panel.setLayout(null);
		
		//直接以坐标设置位置
		userLabel = new JLabel("User: ");
		userLabel.setBounds(50,20,80,25);
		panel.add(userLabel);
		
		//文本输入框
		userText = new JTextField(20);
	    userText.setBounds(140,20,165,25);
	    panel.add(userText);
	    
	    //密码标签
		passwordLabel = new JLabel("Password:");
	    passwordLabel.setBounds(50,50,80,25);
	    panel.add(passwordLabel);
	 
	    //这个类似用于输入的文本域
	    // 但是输入的信息会以点号代替，用于包含密码的安全性
	    passwordText = new JPasswordField(20);
	    passwordText.setBounds(140,50,165,25);
	    panel.add(passwordText);
	    
	    ipLabel = new JLabel("ip地址: ");
	    ipLabel.setBounds(50,80,80,25);
	    panel.add(ipLabel);
	    
	    ipText = new JTextField(15);
	    ipText.setBounds(140,80,165,25);
	    ipText.setText("127.0.0.1");
	    panel.add(ipText);
	    
	    portLabel = new JLabel("端口号: ");
	    portLabel.setBounds(50, 110, 80, 25);
	    panel.add(portLabel);
	    
	    portText = new JTextField(8);
	    portText.setText("30000");
	    portText.setBounds(140,110,165,25);
	    panel.add(portText);
	 
	    // 创建登录按钮
	    loginButton = new JButton("login");
	    loginButton.setBounds(150, 150, 80, 25);
	    panel.add(loginButton);
	    
	    //上面都是经过简单位置调整来完成的
	    
	    //鼠标点击事件
	    loginButton.addActionListener(new ActionListener() {
	    	@Override
			public void actionPerformed(ActionEvent e) {
				check();
			}
	    });
	   
	    //键盘回车事件
	    KeyListener key_Listener = new KeyListener()
		{
			public void keyTyped(KeyEvent e) {}
			public void keyReleased(KeyEvent e){}
			public void keyPressed(KeyEvent e){
				if(e.getKeyChar() == KeyEvent.VK_ENTER )
				{
					check();
				}
			}
		};
		
		userText.addKeyListener(key_Listener);
		passwordText.addKeyListener(key_Listener);
		ipText.addKeyListener(key_Listener);
		portText.addKeyListener(key_Listener);
	}
	
	//检查ip和端口
	public void checkIpAndPort() {
		//clientGUI.setSocket(null);
		try {
			clientGUI.setSocket(new Socket(ipText.getText(), Integer.parseInt(portText.getText())));
			ClientGUI.isConnected = true;
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	//检查输入是否有错
	public void check() {
		if(userText.getText().isEmpty() || new String(passwordText.getPassword()).isEmpty() || ipText.getText().trim().length() == 0 || portText.getText().trim().length() == 0) {
			JOptionPane.showMessageDialog(null, "信息不能为空", "Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		checkIpAndPort();
		if(!ClientGUI.isConnected) {
			JOptionPane.showMessageDialog(null, "连接服务器失败,请保证服务器打开", "Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
		clientGUI.setClientThread(new ClientThread(clientGUI.getSocket(),userText.getText(),new String(passwordText.getPassword()) ,ipText.getText()));
		if(check == false) {
			JOptionPane.showMessageDialog(null, "用户名或密码错误或用户已经在线", "Error",JOptionPane.ERROR_MESSAGE);
			try {
				SendUtils.sendData(new DataOutputStream(clientGUI.getSocket().getOutputStream()), "STOP");
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		frame.setVisible(false);
		clientGUI.getFrame().setTitle(userText.getText().toString());
		clientGUI.setName(userText.getText().toString());
		clientGUI.getClientThread().start();
		clientGUI.initFriend(userText.getText().toString());
		clientGUI.getFrame().setVisible(true);
		//把当前信息发过去  在服务器端验证
		//clientGUI.setClientThread(new ClientThread(clientGUI.getSocket(),userText.getText(),ipText.getText()));
		
	}
	
	public static void main(String[] args) {
		new Login();
	}

}

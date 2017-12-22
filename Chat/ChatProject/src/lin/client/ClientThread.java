package lin.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import lin.client.view.ClientGUI;
import lin.client.view.FriendList;
import lin.client.view.Login;
import lin.utils.SendUtils;

public class ClientThread extends Thread{

	private int count;
	private RandomAccessFile myFile;
	private Socket socket;
	private DataInputStream dis;
	public static DataOutputStream dos;
	private String username;
	private JFileChooser fileChooser;
	//private FileOutputStream fose = null;
	private String password;
	static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	//初始化五个String 类型的值， 供下面使用， 就不用一直new 新对象出来
	private StringTokenizer st;
	private String time;
	private String message;
	private String command;
	private String firstParams;
	private String secondParams;
	private String thirdParams;
	private String fouthParams;
	
	public boolean stop = false;
	
	public String getPassword() {
		return password;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public DataOutputStream getDataOutputStream() {
		return dos;
	}

	public DataInputStream getDateInputStream() {
		return dis;
	}
	
	public ClientThread(Socket socket, String username, String password, String ipAddr) {
		this.username = username;
		this.password = password;
		this.socket = socket;
		try {
			this.dis = new DataInputStream(socket.getInputStream());
			ClientThread.dos = new DataOutputStream(socket.getOutputStream());
			SendUtils.sendData(dos, "LOGIN@" + username + "@" + password);
			message = dis.readUTF();
			if(!message.equals("TRUE")) {
				//登陆失败
				Login.check = false;
				return;
			} 
			Login.check = true;
			SendUtils.sendData(dos, username + "@" + ipAddr);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		message = "";
		while(!stop) {
			time = formatter.format(new Date());
			try{
				message = dis.readUTF();
				st = new StringTokenizer(message, "@");
				command = st.nextToken();
				if(command.equals("ADMIN")) {
					//初始化操作  获得在线人员列表 然后匹配更新好友列表 
					firstParams = st.nextToken(); //这个绝对不会空，所以不用进行非空判断
					String[] friends = firstParams.split(",");
					boolean stopStatus = false;
					for(int i = 0; i < friends.length; i++) {
						ClientGUI.listFriend.add(friends[i]);
						for(int j = 0; j < ClientGUI.model.size() && !stopStatus; j++) {
							if(compareString(friends[i],ClientGUI.model.get(j))) {
								ClientGUI.model.remove(j);
								ClientGUI.model.addElement(friends[i] + "@[在线]");
								stopStatus = true;
								ClientGUI.list.setModel(ClientGUI.model); //不可以到这一步
							}
						}
					}
				} else if(command.equals("FINDMSG")) {
					firstParams = st.nextToken();
					ClientGUI.showMsg.setText(firstParams);
				} else if(command.equals("SERVERCLOSE")){
					//服务器关闭
					stop = true;
					closeCon();
					ClientGUI.frame.setVisible(false);
					JOptionPane.showInputDialog("服务器出错");
					Login.frame.setVisible(true);
				} else if(command.equals("USERCLOSE")){
					firstParams = st.nextToken();
					ClientGUI.showMsg.append(time + "\r\n" + firstParams + "下线了" + "\r\n");
					SendUtils.sendData(dos, "SAVEUSERMSG@" + username + "@" + time + "\r\n" + firstParams + "下线了" + "\r\n");
					changeFriendList(firstParams, false);
				} else if(command.equals("REJECT")) {
					firstParams = st.nextToken();
					ClientGUI.showMsg.append(firstParams + "\r\n");
					SendUtils.sendData(dos, "SAVEUSERMSG@" + username + "@" + firstParams);
				} else if(command.equals("FILE")) {
					//接收文件
					firstParams = st.nextToken();
					secondParams = st.nextToken();
					ClientGUI.showMsg.append(firstParams + "发文件给你：" + secondParams +"\r\n");
					SendUtils.sendData(dos, "SAVEUSERMSG@" + username + "@" + time + "\r\n" + firstParams + "发文件给你：" + secondParams + "\r\n");
					//新增两行代码
					fileChooser = new JFileChooser("F:/temp1");
					fileChooser.setSelectedFile(new File(secondParams));
					int option = fileChooser.showSaveDialog(ClientGUI.frame);
					ClientGUI.fileSave = fileChooser.getSelectedFile();
					if(option == JFileChooser.APPROVE_OPTION) {
						//保存文件，开始传输
						//fose = new FileOutputStream(ClientGUI.fileSave);
						myFile = new RandomAccessFile(ClientGUI.fileSave, "rw");
						count = 0;
						SendUtils.sendData(dos, "RECEIVE@" + firstParams + "@" + username); 
					} else {
						ClientGUI.showMsg.append("你拒绝了" + firstParams + "发给你的文件：" + secondParams +"\r\n");
						SendUtils.sendData(dos, "SAVEUSERMSG@" + username + "@" + "你拒绝了" + firstParams + "发给你的文件：" + secondParams);
						SendUtils.sendData(dos, "REJECT@" + firstParams + "@" + username);
					}
				} else if(command.equals("SEND")) {
					//开始发送文件
					sendFile(st.nextToken());
					System.out.println(message);
				} else if(command.equals("ADD")) {
					//上线更新信号  //更新一个函数
					firstParams = st.nextToken();
					ClientGUI.showMsg.append(time + "\r\n" + firstParams + "上线啦" + "\r\n");
					changeFriendList(firstParams, true);
				} else if(command.equals("DELETE")) {
					//被服务器强制下线信号
					SendUtils.sendData(dos, "CLOSE@" + username + "@CLOSE");
					closeCon();
					ClientGUI.frame.setVisible(false);
					JOptionPane.showInputDialog("服务器出错");
					System.exit(0);//直接关闭
				} else if(command.equals("SYSTEM")) {
					firstParams = st.nextToken();
					ClientGUI.showMsg.append(time + "\r\n" + "[系统消息]" + firstParams + "\r\n");
					SendUtils.sendData(dos, "SAVEUSERMSG@" + username + "@" + "你拒绝了" +time + "\r\n" + "[系统消息]" + firstParams + "\r\n");
				} else if(command.equals("FILEDATE")) {
					//byte[] by = st.nextToken().getBytes("ISO-8859-1");
					byte[] by = st.nextToken().getBytes();
					//fose.write(by, 0, by.length);
					//fose.flush();
					myFile.seek(count * 1024);;
					myFile.write(by);
					count++;
				} else if(command.equals("INITFRIEND")) {
					firstParams = st.nextToken().trim();
					String[] temp2 = firstParams.split(" ");
					ClientGUI.model.clear();
					ClientGUI.model.addElement("默认群发");
					for(int i = 0; i < temp2.length; i++)
						ClientGUI.model.addElement(temp2[i]);
				} else if(command.equals("FINDUSER")) {
					firstParams = st.nextToken();
					FriendList.list.setListData(firstParams.trim().split(" "));
				} else if(command.equals("NOTREC")) {
					firstParams = st.nextToken();
					secondParams = st.nextToken();
					thirdParams = st.nextToken();
					fouthParams = st.nextToken();
					ClientGUI.showMsg.append(thirdParams + "\r\n" + firstParams + "说: " + fouthParams + "\r\n");
					SendUtils.sendData(dos, "SAVEUSERMSG@" + username + "@" + thirdParams + "\r\n" + firstParams + "说: " + fouthParams + "\r\n");
				} else if(command.equals("FINISH")) {
					count = 0;
				} else {
					//暂时没想到其它  先放着
					//普通消息 打印出来
					String str = null;
					firstParams = st.nextToken();
					secondParams = st.nextToken();
					if(command.equals("ALL")) {
						//普通展示
						str ="[" + time + "]" + "\r\n" + firstParams + "说：" + secondParams + "\r\n";
					} else {
						str ="[" + time + "]" + "\r\n" + firstParams + "说：" + secondParams + "[私聊信息]" + "\r\n";
					}
					//然后就可以添加到UI界面上
					//现在先发到控制台上
					ClientGUI.showMsg.append(str);
					SendUtils.sendData(dos, "SAVEUSERMSG@" + username + "@" + str);
				} 
			} catch(IOException ex) {
				ex.printStackTrace();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void closeCon() throws IOException {
		//将好友 全部点暗
		SendUtils.sendData(dos, "CHANGEUSERSTATE@" + getUsername() + "@CHANGEUSERSTATE");
		if(dis != null) {
			dis.close();
		}
		
		if(dos != null) {
			dos.close();
		}
		
		if(socket != null) {
			socket.close();
		}
		
		ClientGUI.isConnected = false;
	}
	
	//被这里坑了
	private boolean compareString(String s1, String s2) {
		return s1.trim().equals(s2.trim());
	}
	
	private void changeFriendList(String s1, boolean state) {
		for(int j = 0; j < ClientGUI.model.size(); j++) {
			if(compareString(s1,ClientGUI.model.get(j).split("@")[0])) {
				ClientGUI.model.remove(j);
				if(state)
					ClientGUI.model.addElement(s1 + "@[在线]");
				else 
					ClientGUI.model.addElement(s1);
				ClientGUI.list.setModel(ClientGUI.model); //可以到这一步
				break;
			}
		}
		if(state) {
			ClientGUI.listFriend.add(s1.trim());
		} else {
			for(int i = 0 ; i < ClientGUI.listFriend.size(); i++) {
				if(ClientGUI.listFriend.get(i).trim().equals(s1.trim())) {
					ClientGUI.listFriend.remove(i);
					break;
				}
			}
		}
	}
	
	private void sendFile(String name) {
		try {
			FileInputStream fis = new FileInputStream(ClientGUI.file);
			byte[] bytes = new byte[1024];
			int length = 0;
			while((length = fis.read(bytes)) > 0) {
				//SendUtils.sendData(dos, "FILEDATE@" + name + "@" + new String(bytes, 0, length, "ISO-8859-1"));
				SendUtils.sendData(dos, "FILEDATE@" + name + "@" + bytes.toString());
			}
			fis.close();
			SendUtils.sendData(dos, "FINISH@" + name + "传输文件成功");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch(IOException ex) {
				ex.printStackTrace();
		}
	}	
}

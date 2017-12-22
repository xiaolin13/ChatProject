package lin.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import lin.dao.Save;
import lin.domain.NoRecMsg;
import lin.domain.User;
import lin.server.view.ServerGUI;
import lin.utils.SendUtils;

public class ClientThread extends Thread{

	private int i = 0;
	private User user;
	private Socket socket;
	private DataInputStream dis;
	private DataOutputStream dos;
	private boolean stop;
	private DataOutputStream tempDos = null;
	
	private StringTokenizer st;
	private String message;
	private String command;
	private String firstParams;
	private String secondParams;
	private String thirdParams;
	
	public void setUser(User user) {
		this.user = user;
	}

	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	//只需要使用getter 方法，  无需setter方法
	public User getUser() {
		return user;
	}

	public DataInputStream getDis() {
		return dis;
	}

	public DataOutputStream getDos() {
		return dos;
	}

	public ClientThread(Socket socket) {
		try {
			this.socket = socket;
			//记录下该线程的输入、输出流   方便以后调用
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			//卡在这里
			message = dis.readUTF();
			StringTokenizer st = new StringTokenizer(message,"@");
			command = st.nextToken();
			if(command.equals("LOGIN"));
			firstParams = st.nextToken();  //这里代表的账号密码
			secondParams = st.nextToken();
			
			Save.initDB();
			if(Save.checkUser(firstParams, secondParams)) {
				SendUtils.sendData(dos, "TRUE");
				String temp = dis.readUTF();
				notice(temp);
				Save.initDB();
				this.start();
			} else {
				SendUtils.sendData(dos, "FALSE");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run() {
		message = null;
		stop = false;
		//不断接收消息      写信息在ServerGUI那边
		while(!stop) {
			try {
				message = dis.readUTF();
				st = new StringTokenizer(message,"@");
				command = st.nextToken();
				if(command.equals("CLOSE")) {    //客户端发来下线消息
					firstParams = st.nextToken();
					Save.changeUserStatus(firstParams);
					//马上弹出该用户已下线 然后禁止向该用户发送消息
					dis.close();
					dos.close();
					socket.close();
					//在列表项挂上该用户已离线
					//向所有用户发送该用户的下线消息  然后将该用户的在线状态改为离线
					for(int i = ServerGUI.clients.size() - 1; i >= 0; i--) {
						if(!ServerGUI.clients.get(i).getUser().getName().equals(user.getName())){
							SendUtils.sendData(ServerGUI.clients.get(i).getDos(), "USERCLOSE@" + user.getName() + "@USERCLOSEs");
						}
					}
					
					//删除在clients上的该客户端服务线程 
					for(int i = ServerGUI.clients.size() - 1; i >= 0; i--) {
						if(ServerGUI.clients.get(i).getUser().getName().equals(user.getName())) {
							ServerGUI.clients.remove(i);
							break;//将stop 置为 true 然后 一个continue 可以直接退出循环，即退出该线程
						}
					}
					stop = true;
					ServerGUI.count--;  //人数减一
					ServerGUI.maxText.setText("" + ServerGUI.count);
					ServerGUI.showMsg.append("[系统消息]" + user.getName() + "下线了" + "\r\n");
					Save.saveChatMsg("[系统消息]" + user.getName() + "下线了" + "\r\n");
					changeList();
				} else if(command.equals("deleteUserMsg")) {
					firstParams = st.nextToken();
					Save.deleteUserMSg(firstParams);
				} else if(command.equals("DELFRIEND")) {
					firstParams = st.nextToken();
					secondParams = st.nextToken();
					Save.delFriend(firstParams, secondParams);
				} else if(command.equals("INITFRIEND")) {
					firstParams = st.nextToken();
					secondParams = " ";
					String[] friendList = Save.initFriend(firstParams);
					if(friendList != null) {
						for(int i = 0; i < friendList.length; i++) {
							secondParams += friendList[i] + " ";
						}
					}
					SendUtils.sendData(findUser(firstParams), "INITFRIEND@" + secondParams + "@INITFRIEND");
					sendNotRec(user.getName());
					noticeFriend();
				} else if(command.equals("STOP")) {
					dis.close();
					dos.close();
					socket.close();
					stop = true;
				} else if(command.equals("FILE")) {
					//传输文件 以后再写
					firstParams = st.nextToken();
					secondParams = st.nextToken();
					thirdParams = st.nextToken();
					fileQuerySend(firstParams, secondParams, thirdParams);
				} else if(command.equals("REJECT")) {
					//对方拒收文件
					firstParams = st.nextToken();
					secondParams = st.nextToken();
					SendUtils.sendData(findUser(firstParams), "REJECT@" + secondParams + "拒接了" + "你的文件");
					ServerGUI.showMsg.append(secondParams + "拒接了" + firstParams + "的文件\r\n");
					Save.saveChatMsg(secondParams + "拒接了" + firstParams + "的文件");
				} else if(command.equals("RECEIVE")){
					//对方准备接收文件  这边开始发文件
					System.out.println(message);
					firstParams = st.nextToken();
					secondParams = st.nextToken();
					SendUtils.sendData(findUser(firstParams), "SEND@" + secondParams);
				} else if(command.equals("FINDMSG")){
					firstParams = st.nextToken();
					secondParams = Save.insertUserMsg(firstParams);
					SendUtils.sendData(dos, "FINDMSG@" + secondParams);
				} else if(command.equals("FILEDATE")) {
					firstParams = st.nextToken();
					secondParams = st.nextToken();
					SendUtils.sendData(findUser(firstParams), "FILEDATE@" + secondParams);
				} else if(command.equals("SAVEUSERMSG")) {
					firstParams = st.nextToken();
					secondParams = st.nextToken();
					Save.saveUserMsg(firstParams, secondParams);
				} else if(command.equals("FINISH")) {
					System.out.println(message);
					SendUtils.sendData(dos, message);
				} else if(command.equals("CHANGEUSERSTATE")) {
					firstParams = st.nextToken();
					Save.changeUserStatus(firstParams);
				} else if(command.equals("INSERTFRIENDDB")) {
					firstParams = st.nextToken();
					secondParams = st.nextToken();
					Save.insertFriendDB(firstParams, secondParams);
				} else if(command.equals("FINDUSER")) {
					firstParams = st.nextToken();
					secondParams = st.nextToken();
					thirdParams = "";
					String[] findUser = Save.findUser(secondParams, secondParams);
					for(int i = 0; i < findUser.length; i++) {
						thirdParams += findUser[i] + " ";
					}
					SendUtils.sendData(findUser(firstParams), "FINDUSER@" + thirdParams + "@FINDUSER");
				} else {
					//传输普通的聊天内容
					transitMsg(message);
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			if(stop == true)
				break;
		}
	}
	
	private void transitMsg(String message) {
		String[] s = message.split("@");  //可以用来区分
		//文本内容分为source@owner@content三部分
		String source = s[0];   //发给谁
		String owner = s[1];    //自己
		String content = s[2];  //内容
		message = source + "@" + owner + "@" + content;
		//在自己的文本框上再做处理
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String time = format.format(new Date());
		ServerGUI.showMsg.append(time + "\r\n" + owner + "对" + (source.equals("ALL")?"所有人":source) + "说:" + content + "\r\n");
		Save.initDB();
		Save.saveChatMsg(time + "\r\n" + owner + "对" + (source.equals("ALL")?"所有人":source) + "说:" + content + "\r\n");
		if(source.equals("ALL")) {         //群聊
			//每一个用户都会收到该信息
			for(int i = ServerGUI.clients.size() - 1; i >= 0; i--) {
				SendUtils.sendData(ServerGUI.clients.get(i).getDos(), message);
			}
		} else {   //私聊给对方source
			tempDos = findUser(source);
			if(tempDos != null) {
				SendUtils.sendData(tempDos, message);
			} else {
				//保存到数据库  以后再用
				Save.saveNoRecMsg(source, owner, content);
			}
		}
	}
	
	//更新好友操作
	private void changeList() {
		ServerGUI.model.removeAllElements();
		ServerGUI.model.addElement("默认群发");
		for(int i = 0; i < ServerGUI.clients.size(); i++) {
			ServerGUI.model.addElement(ServerGUI.clients.get(i).getUser().getName());
		}
		ServerGUI.list.setModel(ServerGUI.model);
	}
	
	//发送文件  让接收的人接收请求  //但现在群发 只能一个人接收
	private void fileQuerySend(String owner, String source, String filename) {
		if(source.equals("默认群发")) {
			for(int i = ServerGUI.clients.size() - 1; i >= 0; i--) {
				String userName = ServerGUI.clients.get(i).getUser().getName();
				if(!userName.equals(source)) {
					SendUtils.sendData(ServerGUI.clients.get(i).getDos(), "FILE@" + owner + "@" + filename);
				}	
			}
		} else {
			tempDos = findUser(source);
			if(tempDos != null) {
				SendUtils.sendData(tempDos, "FILE@" + owner + "@" + filename);
			} else {
				SendUtils.sendData(dos, "REJECT@" + source +"不在线，不能接收您的文件\r\n");
			}
		}
	}
	
	private DataOutputStream findUser(String userName) {
		for(int i = ServerGUI.clients.size() - 1; i >= 0; i--) {
			String name = ServerGUI.clients.get(i).getUser().getName();
			if(name.equals(userName)) {
				return ServerGUI.clients.get(i).getDos();
			}
		}
		
		return null;
	}
	
	private void notice(String ifo) {
		StringTokenizer st = new StringTokenizer(ifo,"@");
		user = new User(st.nextToken(), st.nextToken());
		
		//向用户展示现在在线人员的信息
		ServerGUI.clients.add(this);
		if(ServerGUI.clients.size() > 0) {
			//设置一个临时变量来记录用户的信息
			String tempStr = "SYSTEM@通知：\r\n";
			String tempInit = "ADMIN@";
			for(int i = ServerGUI.clients.size() - 1; i >= 0; i--) 
			{
				tempStr += ServerGUI.clients.get(i).getUser().getName() + " ";
				tempInit += ServerGUI.clients.get(i).getUser().getName() + " ";
				if(i != 0) {
					tempStr += ",";
					tempInit += ",";
				}
			}
				
			tempStr+="在线";
			SendUtils.sendData(dos, tempStr);
			SendUtils.sendData(dos, tempInit);
		}
		
		for(int i = ServerGUI.clients.size() - 1; i >= 0; i--) {
			if(!ServerGUI.clients.get(i).getUser().getName().equals(user.getName())) {
				SendUtils.sendData(ServerGUI.clients.get(i).getDos(), "ADD@" + getUser().getName() + "@ADD");
			}
		}
		//没考虑到并发问题
		ServerGUI.maxText.setText((ServerGUI.count + 1) + "");
		ServerGUI.count++;
		ServerGUI.showMsg.append("[系统消息]" + getUser().getName() + "上线了" + "\r\n");
		Save.initDB();
		Save.saveChatMsg("[系统消息]" + user.getName() + "上线了" + "\r\n");
		changeList();
	}
	
	private void noticeFriend() {
		String tempInit = "ADMIN@";
		for(int i = ServerGUI.clients.size() - 1; i >= 0; i--) 
		{
			tempInit += ServerGUI.clients.get(i).getUser().getName() + " ";
			if(i != 0) {
				tempInit += ",";
			}
		}
		SendUtils.sendData(dos, tempInit);
	}
	
	private void sendNotRec(String name) {
		String temp = "";
		NoRecMsg[] queryNoRecMsg = Save.queryNoRecMsg(name);
		if(queryNoRecMsg != null) {
			for(int i = 0; i < queryNoRecMsg.length; i++) {
				temp = "NOTREC@" + queryNoRecMsg[i].getSendName() + "@" + queryNoRecMsg[i].getRecName() + "@" + queryNoRecMsg[i].getMsg();
				SendUtils.sendData(dos, temp);
				temp = "";
			}
		}
		Save.delNoRecMsg(name);
	}
}

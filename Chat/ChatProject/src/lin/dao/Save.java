package lin.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import lin.domain.NoRecMsg;

public class Save {

	static Connection conn;
	static PreparedStatement pstmt;
	static String sql;
	
	//初始化数据库操作
	public static void initDB() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		String url = "jdbc:mysql://127.0.0.1:3306/chatproject";
		try {
			conn = DriverManager.getConnection(url, "root", "123456");
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//保存服务器端聊天记录
	public static void saveChatMsg(String message) {
		sql = "insert into message value(null,?)";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, message);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//从数据库取出 聊天记录（服务器端）
	public static String insertRecord() {
		String temp = "";
		sql = "select msg from message";
		try {
			pstmt = conn.prepareStatement(sql);
			ResultSet executeQuery = pstmt.executeQuery();
			while(executeQuery.next()) {
				temp += executeQuery.getString(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return temp;
	}
	
	//保存客户端 聊天记录
	public static void saveUserMsg(String name, String msg) {
		sql = "insert into usermessage value(null,?,?)";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, name);
			pstmt.setString(2, msg);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//提取聊天记录
	public static String insertUserMsg(String name) {
		String temp = "";
		sql = "select msg from usermessage where username=?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, name);
			ResultSet executeQuery = pstmt.executeQuery();
			while(executeQuery.next()) {
				temp += executeQuery.getString(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return temp;
	}
	
	//删除服务器端聊天记录
	public static void deleteSystemMsg() {
		sql = "delete from message";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//删除客户端聊天记录
	public static void deleteUserMSg(String name) {
		sql = "delete from usermessage where username=?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, name);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//将好友信息插入到数据库中
	public static void insertFriendDB(String firstParams, String secondParams) {
		try {
			pstmt = conn.prepareStatement("insert into friend value(null,?,?)");
			pstmt.setString(1,firstParams);
			pstmt.setString(2, secondParams);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//上线找好友
	public static String[] findUser(String firstParam, String secondParam) throws SQLException {
		pstmt = conn.prepareStatement("select username from user where username like ?");
		pstmt.setString(1,firstParam);
		ResultSet query = pstmt.executeQuery();
		pstmt = conn.prepareStatement("select count(*) from user where username like ?");
		pstmt.setString(1, "%"+secondParam+"%");
		ResultSet query1 = pstmt.executeQuery();
		int num = 0;
		if(query1.next()) {
			num = query1.getInt(1);
		}
		String[] strFriend = new String[num];
		int i = 0;
		while(query.next()) {
			strFriend[i] = query.getString(1);
			i++;
		}
		return strFriend;
	}
	
	// 插入未发送成功的聊天记录
	public static void saveNoRecMsg(String send, String receive, String msg) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = formatter.format(new Date());
		sql = "insert into notsend value(null, ?, ?, ?)";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, receive);
			pstmt.setString(2, send);
			pstmt.setString(3, time + "@" + msg);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//删除未接收的聊天记录
	public static void delNoRecMsg(String receive) {
		sql = "delete from notsend where receive=?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, receive);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//获取未接收的聊天记录
	public static NoRecMsg[] queryNoRecMsg(String receive) {
		int n = 0;
		int i = 0;
		sql = "select count(*) from notsend where receive=?";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, receive);
			ResultSet resultSet = pstmt.executeQuery();
			if(resultSet.next()) {
				n = resultSet.getInt(1);
			}
			if(n != 0) {
				NoRecMsg[] temp = new NoRecMsg[n];
				sql = "select * from notsend where receive=?";
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, receive);
				resultSet = pstmt.executeQuery();
				while(resultSet.next()) {
					temp[i++] = new NoRecMsg(resultSet.getString(2), resultSet.getString(3),resultSet.getString(4));
				}
				
				return temp;
			} 
		} catch(SQLException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	//退出时改变登陆状态
	public static void changeUserStatus(String name) {
		try {
			String url = "jdbc:mysql://127.0.0.1:3306/chatproject";
			conn = DriverManager.getConnection(url, "root", "123456");
			pstmt = conn.prepareStatement("update user set status=? where username=?");
			pstmt.setInt(1, 0);
			pstmt.setString(2, name);
			pstmt.execute();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//客户端删除某位好友
	public static void delFriend(String firstParams, String secondParams) {
		try {
			pstmt = conn.prepareStatement("delete from friend where username=? and friendname=?");
			pstmt.setString(1, firstParams);
			pstmt.setString(2, secondParams);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	//初始化好友列表
	public static String[] initFriend(String name) {
		ResultSet resultset;
		int n = 0;
		int i = 0;
		try {
			pstmt = conn.prepareStatement("select count(*) from friend where username=?");
			pstmt.setString(1, name);
			resultset = pstmt.executeQuery();
			if(resultset.next()) {
				n = resultset.getInt(1);
			}
			pstmt = conn.prepareStatement("select friendname from friend where username=?");
			pstmt.setString(1, name);
			resultset = pstmt.executeQuery();
			if(n != 0) {
				String[] temp = new String[n];
				while(resultset.next()) {
					temp[i++] = resultset.getString(1); 
				}
				return temp;
			}
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	//检查用户是否存在
	public static boolean checkUser(String name, String password) {
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement("select * from user where username=? and password=? and status=0");
			pstmt.setString(1, name);
			pstmt.setString(2, password);
			resultSet = pstmt.executeQuery();
			if(resultSet.next()) {
	            pstmt = conn.prepareStatement("update user set status=? where username=?");//修改用户状态
	            pstmt.setInt(1, 1);
	            pstmt.setString(2, name);
	            pstmt.execute();
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally{
			try {
				pstmt.close();
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
}

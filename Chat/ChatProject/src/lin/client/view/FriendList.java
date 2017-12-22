package lin.client.view;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import lin.client.ClientThread;
import lin.utils.SendUtils;

public class FriendList {

	private JFrame frame;
	private JPanel panel;
	public static JList<String> list;
	private JButton btnAdd;
	private JButton btnCancel;
	private JTextField textFind;
	private JButton btnFind;
	
	public JPanel getPanel() {
		return panel;
	}

	public void setPanel(JPanel panel) {
		this.panel = panel;
	}

	public static JList<String> getList() {
		return list;
	}

	public static void setList(JList<String> list) {
		FriendList.list = list;
	}

	public JButton getBtnAdd() {
		return btnAdd;
	}

	public void setBtnAdd(JButton btnAdd) {
		this.btnAdd = btnAdd;
	}

	public JButton getBtnCancel() {
		return btnCancel;
	}

	public void setBtnCancel(JButton btnCancel) {
		this.btnCancel = btnCancel;
	}

	public JTextField getTextFind() {
		return textFind;
	}

	public void setTextFind(JTextField textFind) {
		this.textFind = textFind;
	}

	public JButton getBtnFind() {
		return btnFind;
	}

	public void setBtnFind(JButton btnFind) {
		this.btnFind = btnFind;
	}

	public JFrame getFrame() {
		return frame;
	}

	public void setFrame(JFrame frame) {
		this.frame = frame;
	}

	public FriendList() {
		Font font = new Font("宋体",Font.PLAIN,25);
		frame = new JFrame("添加好友");
		frame.setSize(400, 500);
		frame.setLocation(720, 210);
		frame.setResizable(false);
		
		panel = new JPanel();
		panel.setLayout(null);
		
		//textFind = new JText
		textFind = new JTextField();
		textFind.setBounds(10, 10, 250, 35);
		textFind.setFont(font);
		panel.add(textFind);
		
		btnFind = new JButton("搜索");
		btnFind.setBounds(270,10,100,35);
		panel.add(btnFind);
		
		list = new JList<>();
		list.setBounds(10, 50, 362, 340);
		panel.add(list);
		list.setFont(font);
		
		btnAdd = new JButton("添加");
		btnAdd.setBounds(80, 400, 100, 45);
		panel.add(btnAdd);
		
		btnCancel = new JButton("取消");
		btnCancel.setBounds(220, 400, 100, 45);
		panel.add(btnCancel);
		
		frame.add(panel);
		frame.setVisible(true);
		
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				hide();
			}
		});
		
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				hide();
			}
			
		});
		
		btnFind.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				SendUtils.sendData(ClientThread.dos, "FINDUSER@" + ClientGUI.clientName + "@" + textFind.getText().toString());
			}
		});
		
		btnAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(list.isSelectionEmpty())
				{
					JOptionPane.showMessageDialog(frame, "请先选中一个用户", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
					
				String selected = list.getSelectedValue();
				if(ClientGUI.model.contains(selected)) {
					JOptionPane.showMessageDialog(frame, "该好友已经存在","Error",JOptionPane.ERROR_MESSAGE);
					hide();
					return;
				}
				for(int i = 0; i < ClientGUI.listFriend.size(); i++) 
					if(ClientGUI.listFriend.get(i).trim().equals(selected)) {
						selected += "@[在线]";
						break;
					}
				ClientGUI.model.addElement(selected);
				SendUtils.sendData(ClientThread.dos, "INSERTFRIENDDB@" + ClientGUI.clientName + "@" + list.getSelectedValue().trim());
				hide();
			}
		});
	}
	
	public void hide() {
		frame.setVisible(false);
		textFind.setText("");
		list.removeAll();
	}
}

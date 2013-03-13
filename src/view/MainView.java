package view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import run.Controller;
import data.Channel;
import data.ChannelGroup;
import data.ChannelTree;

public class MainView extends JFrame{
	private static final long serialVersionUID = 8610292631217381000L;
	private JFrame frame;
	private JPanel topPanel,  messagePanel, toolPanel, leftPanel, midPanel, connectionPanel, serverNamePanel, loginPanel, rightPanel;
	private JTabbedPane chatPanel;
	private JScrollPane channelsPanel, channelsUserPanel, usersListPanel;
	
	private JTextField txtServerIp, txtServerPort, txtNickname, txtMessage;
	private JLabel lbServerName;
	
	private HashMap<Long,JTextPane> channelViews;
	
	private JMenuBar menuBar;
	private JMenu mnRemoveChannel;
	
	private JButton btnConnect,btnLogin,btnSend;
	
	private JTree channels, userChannels;
	private JList<String> usersList;
	private DefaultListModel<String> usersListModel;
	
	private Controller ctrl;
	
	private ActionListener connect, disconnect, login, logout, send;
	
	private class CustomIconRenderer extends DefaultTreeCellRenderer {
	    ImageIcon chanIcon;
	    ImageIcon groupIcon;

	    public CustomIconRenderer() {
	        chanIcon = new ImageIcon(CustomIconRenderer.class.getResource("/images/defaultIcon.gif"));
	        groupIcon = new ImageIcon(CustomIconRenderer.class.getResource("/images/specialIcon.gif"));
	    }

	    public Component getTreeCellRendererComponent(JTree tree,
	      Object value,boolean sel,boolean expanded,boolean leaf,
	      int row,boolean hasFocus) {

	        super.getTreeCellRendererComponent(tree, value, sel, 
	          expanded, leaf, row, hasFocus);

	        if (((DefaultMutableTreeNode)value).getChildCount() != 0) {
	            setIcon(groupIcon);
	        } else {
	            setIcon(chanIcon);
	        } 
	        return this;
	    }
	}
	
	public MainView(Controller newCtrl) {
		super();
		ctrl = newCtrl;
		frame = this;
		channelViews = new HashMap<Long,JTextPane>();

		initFrame();
		initActionListener();
		initState();
		frame.setMinimumSize(new Dimension(620,600));
	}
	
	public void closeAllChannelViews(){
		for(long id : channelViews.keySet())
			chatPanel.remove(channelViews.get(id));
	}
	
	public void closeChannelView(long channelId){
		if(channelViews.containsKey(channelId)){
			chatPanel.remove(channelViews.get(channelId));
			channelViews.remove(channelId);	
		}
	}
	
	public void connectedState(){
		btnConnect.setText("Disconnect");
		btnConnect.removeActionListener(connect);
		btnConnect.removeActionListener(disconnect);
		btnConnect.addActionListener(disconnect);
		txtServerIp.setEnabled(false);
		txtServerPort.setEnabled(false);
		btnConnect.setEnabled(true);
		
		btnLogin.setText("Login");
		txtNickname.setEnabled(true);
		btnLogin.setEnabled(true);
		btnLogin.removeActionListener(login);
		btnLogin.removeActionListener(logout);
		btnLogin.addActionListener(login);
	}
	
	public void connectingState(){
		btnConnect.setText("Connecting...");
		btnConnect.setEnabled(false);
		btnConnect.removeActionListener(disconnect);
		btnConnect.removeActionListener(connect);
		
		btnLogin.setText("Login");
		
		txtServerIp.setEnabled(false);
		txtServerPort.setEnabled(false);
	}
	
	public long getClickedChannelId(MouseEvent event){
		long channelId;
		
		TreePath path = channels.getPathForLocation((int)event.getPoint().getX(), (int)event.getPoint().getY());
		if ( path != null){
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
			Object object = node.getUserObject();
			channelId = ((ChannelTree)object).getId();
		}
		else
			channelId = 0;
		
		return channelId;
	}
	
	public long getSelectedChannelId() {
		long channelId;
		
		TreePath path = channels.getSelectionPath();
		if ( path != null){
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
			Object object = node.getUserObject();
			channelId = ((ChannelTree)object).getId();
		}
		else
			channelId = 0;
		
		return channelId;
	}
	
	public void initActionListener(){
		connect = new ActionListener(){		@Override
			public void actionPerformed(ActionEvent arg0) {
				if(txtServerPort.getText().matches("^\\d{1,5}$")){
					int port = Integer.parseInt(txtServerPort.getText());
					if(txtServerIp.getText().matches("^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$"))
						ctrl.connect(txtServerIp.getText(), port);
					else
						JOptionPane.showMessageDialog(frame,
							    "The address is incorrect.",
							    "ServerIP Error",
							    JOptionPane.ERROR_MESSAGE);
				}
				else{
					JOptionPane.showMessageDialog(frame,
						    "NaN. The port given isn't a number.",
						    "Port Error",
						    JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		disconnect = new ActionListener(){	@Override
					public void actionPerformed(ActionEvent arg0) {
						ctrl.disconnect();
					}};
					
		login = new ActionListener(){	@Override
				public void actionPerformed(ActionEvent arg0) {
					ctrl.login(txtNickname.getText());
				}};
		logout = new ActionListener(){	@Override
					public void actionPerformed(ActionEvent arg0) {
					ctrl.logout();
				}};
				
		send = new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0){
				// TODO Recup de l'id du chan actif ( Onglet selectionné ).
				long chanId = 1;
				ctrl.sendMessage(txtMessage.getText(),chanId);
				txtMessage.setText("");
			}
			
		};
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				ctrl.exit();
	        }
	      });
	}
	
	public void initChannelsPanel(){
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Root Node");
		DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);

		channels = new JTree(treeModel);
		channels.setRootVisible(false);
		channels.setBorder(UIManager.getBorder("ComboBox.border"));
		//channels.setCellRenderer(new CustomIconRenderer());
		channelsPanel = new JScrollPane(channels);
		channelsPanel.setMaximumSize(new Dimension(150,1000));
		
		channels.addMouseListener(new MouseAdapter(){
			@Override
			public void mousePressed(MouseEvent event){
				super.mousePressed(event);
				if(SwingUtilities.isLeftMouseButton(event) && event.getClickCount()==1){

					long channelId = getClickedChannelId(event);
					updateUsers(ctrl.listUsernames(channelId));
					if(channelId == 0)
						channels.setSelectionPath(null);
				}
			}
			public void mouseReleased(MouseEvent event){
				super.mouseReleased(event);
				if(SwingUtilities.isLeftMouseButton(event) && event.getClickCount()==2){

					long channelId = getClickedChannelId(event);
					if(!ctrl.isLinkedToChannel(channelId))
						ctrl.sendJoinned(channelId);
				}
				else if(SwingUtilities.isRightMouseButton(event)){

					final long channelId = getClickedChannelId(event);
					if(ctrl.getChannel(channelId) instanceof Channel){
						JPopupMenu channelPopup = new JPopupMenu();
						if(!ctrl.isLinkedToChannel(channelId)){
							JMenuItem item = new JMenuItem("Rejoindre");
							item.addActionListener(new ActionListener(){
								@Override
								public void actionPerformed(ActionEvent arg0) {
									ctrl.sendJoinned(channelId);
								}});
							channelPopup.add(item);
						}
						else{
							JMenuItem item = new JMenuItem("Quitter");
							item.addActionListener(new ActionListener(){
								@Override
								public void actionPerformed(ActionEvent arg0) {
									ctrl.sendLeft(channelId);
								}});
							channelPopup.add(item);
						}
						// TODO Gestion de la suppression, du don et l'affichage de ses infos.
						channelPopup.show(channels,event.getX(), event.getY());
					}
				}
			}
		});
	}
	
	public void initChannelsUserPanel(){
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Root Node");
		DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
		
		userChannels = new JTree(treeModel);
		userChannels.setRootVisible(false);
		userChannels.setBorder(UIManager.getBorder("ComboBox.border"));
		channelsUserPanel = new JScrollPane(userChannels);
		channelsUserPanel.setMaximumSize(new Dimension(150,1000));
		userChannels.addMouseListener(new MouseAdapter(){
			@Override
			public void mousePressed(MouseEvent event){
				super.mousePressed(event);
				if(SwingUtilities.isLeftMouseButton(event) && event.getClickCount()==1){
					// TODO recupération des events des channels
					// Afficher les usersList de ce chan ou de ce changroup.
				}
			}
			public void mouseReleased(MouseEvent event){
				super.mouseReleased(event);
				if(SwingUtilities.isLeftMouseButton(event) && event.getClickCount()==2){
					// TODO
					// Ouvrir la fenetre du chan
				}
				else if(SwingUtilities.isRightMouseButton(event)){

					// TODO
					// Ouvrir un menu pour quitter le chan, afficher ses infos, le supprimer, le donner etc...
				}
			}
		});
	}
	
	public void initChatPanel(){
		chatPanel = new JTabbedPane();
	}
	
	public void initConnectionPanel(){
		txtServerIp = new JTextField();
		txtServerIp.setHorizontalAlignment(SwingConstants.LEFT);
		txtServerIp.setToolTipText("The IPv4 of the server you want to join.");
		txtServerIp.setText("192.168.0.3");
		txtServerIp.setMinimumSize(new Dimension(100,20));
		txtServerIp.setPreferredSize(new Dimension(100,20));
		txtServerIp.setMaximumSize(new Dimension(100,20));
		txtServerIp.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent arg0) {
				if(txtServerIp.getText().equals("Server IP..."))
					txtServerIp.setText("");
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				if(txtServerIp.getText().isEmpty())
					txtServerIp.setText("Server IP...");
			}
			
		});
		
		txtServerPort = new JTextField();
		txtServerPort.setHorizontalAlignment(SwingConstants.LEFT);
		txtServerPort.setToolTipText("The port of the server you want to join.");
		txtServerPort.setText("34253");
		txtServerPort.setMinimumSize(new Dimension(60,20));
		txtServerPort.setPreferredSize(new Dimension(60,20));
		txtServerPort.setMaximumSize(new Dimension(60,20));
		txtServerPort.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent arg0) {
				if(txtServerPort.getText().equals("Port..."))
					txtServerPort.setText("");
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				if(txtServerPort.getText().isEmpty())
					txtServerPort.setText("Port...");
			}
		});
		
		btnConnect = new JButton("Connect");
		
		connectionPanel = new JPanel();
		FlowLayout layout = new FlowLayout();
		layout.setAlignment(FlowLayout.LEFT);
		connectionPanel.setLayout(layout);
		connectionPanel.add(txtServerIp);
		connectionPanel.add(txtServerPort);
		connectionPanel.add(btnConnect);

	}
	
	public void initFrame(){
		initMenu();
		initTopPanel();
		initLeftPanel();
		initMidPanel();
		initRightPanel();
		
		
		frame.setJMenuBar(menuBar);
		frame.getContentPane().add(topPanel, BorderLayout.NORTH);
		frame.getContentPane().add(leftPanel,BorderLayout.WEST);
		frame.getContentPane().add(midPanel,BorderLayout.CENTER);
		frame.getContentPane().add(rightPanel,BorderLayout.EAST);
	}
	
	public void initLeftPanel(){
		initToolPanel();
		initChannelsPanel();
		initChannelsUserPanel();
		
		leftPanel = new JPanel();
		GroupLayout layout = new GroupLayout(leftPanel);
		/*layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);*/
		
		layout.setHorizontalGroup(layout.createSequentialGroup().addGap(5)
																.addGroup(layout.createParallelGroup().addComponent(toolPanel)
																										.addComponent(channelsPanel)
																										.addComponent(channelsUserPanel))
																.addGap(5));
		layout.setVerticalGroup(layout.createSequentialGroup().addGap(5)
																.addComponent(toolPanel)
																.addComponent(channelsPanel)
																.addComponent(channelsUserPanel)
																.addGap(5));
		leftPanel.setLayout(layout);
		leftPanel.setMaximumSize(new Dimension(50,0));
	}
	
	public void initLoginPanel(){
		txtNickname = new JTextField();
		txtNickname.setToolTipText("The nickname you wanna use on the server.");
		txtNickname.setText("Nickname...");
		txtNickname.setMinimumSize(new Dimension(100,20));
		txtNickname.setPreferredSize(new Dimension(100,20));
		txtNickname.setMaximumSize(new Dimension(100,20));
		txtNickname.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent arg0) {
				if(txtNickname.getText().equals("Nickname..."))
					txtNickname.setText("");
			}

			@Override
			public void focusLost(FocusEvent arg0) {
				if(txtNickname.getText().isEmpty())
					txtNickname.setText("Nickname...");
			}
		});
		
		btnLogin = new JButton("Login");
		
		loginPanel = new JPanel();
		FlowLayout layout = new FlowLayout();
		layout.setAlignment(FlowLayout.RIGHT);
		loginPanel.setLayout(layout);
		loginPanel.add(txtNickname);
		loginPanel.add(btnLogin);

	}
	
	public void initMenu(){
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnHome = new JMenu("Home");
		menuBar.add(mnHome);
		
		JMenuItem mntmProfile = new JMenuItem("Profile");
		mnHome.add(mntmProfile);
		
		JMenuItem mntmSettings = new JMenuItem("Settings");
		mnHome.add(mntmSettings);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
		mnHome.add(mntmExit);
		
		JMenu mnLayout = new JMenu("Layout");
		menuBar.add(mnLayout);
		
		JCheckBoxMenuItem mntmShowUsers = new JCheckBoxMenuItem("Show Users");
		mntmShowUsers.setSelected(true);
		mnLayout.add(mntmShowUsers);
		
		JCheckBoxMenuItem mntmShowChannels = new JCheckBoxMenuItem("Show Channels");
		mntmShowChannels.setSelected(true);
		mnLayout.add(mntmShowChannels);
		
		JCheckBoxMenuItem mntmShowMyChannels = new JCheckBoxMenuItem("Show My Channels");
		mntmShowMyChannels.setSelected(true);
		mnLayout.add(mntmShowMyChannels);
		
		JMenu mnChannel = new JMenu("Channel");
		menuBar.add(mnChannel);
		
		JMenuItem mntmNewChannel = new JMenuItem("New Channel");
		mnChannel.add(mntmNewChannel);
		
		mnRemoveChannel = new JMenu("Remove Channel");
		mnChannel.add(mnRemoveChannel);
		
		JMenu mnShare = new JMenu("Share");
		menuBar.add(mnShare);
		
		JMenuItem mntmUploadFile = new JMenuItem("Upload File");
		mnShare.add(mntmUploadFile);
		
		JMenuItem mntmSharePicture = new JMenuItem("Share Picture");
		mnShare.add(mntmSharePicture);
	}
	
	public void initMessagePanel(){
		btnSend = new JButton("Send");
		txtMessage = new JTextField("");
		BorderLayout layout = new BorderLayout(5, 5);
		
		messagePanel = new JPanel();
		messagePanel.setLayout(layout);
		messagePanel.add(txtMessage, BorderLayout.CENTER);
		messagePanel.add(btnSend, BorderLayout.EAST);
		messagePanel.setPreferredSize(new Dimension(0,20));
	}
	
	public void initMidPanel(){
		initChatPanel();
		initMessagePanel();
		midPanel = new JPanel();
		GroupLayout layout = new GroupLayout(midPanel);
		
		layout.setHorizontalGroup(layout.createParallelGroup().addComponent(chatPanel)
																.addComponent(messagePanel));
		layout.setVerticalGroup(layout.createSequentialGroup().addGap(5)
																.addComponent(chatPanel)
																.addGap(5)
																.addComponent(messagePanel,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE)
																.addGap(5));

		midPanel.setLayout(layout);
	}
	public void initRightPanel(){
		initUsersPanel();
		
		rightPanel = new JPanel();
		GroupLayout layout = new GroupLayout(rightPanel);
		layout.setHorizontalGroup(layout.createSequentialGroup().addGap(5)
																.addComponent(usersListPanel)
																.addGap(5));
		layout.setVerticalGroup(layout.createSequentialGroup().addGap(5)
																.addComponent(usersListPanel)
																.addGap(5));
		rightPanel.setLayout(layout);
		rightPanel.setPreferredSize(new Dimension(150,-1));
		rightPanel.setMaximumSize(new Dimension(150,-1));
	}
	
	public void initServerNamePanel(){
		lbServerName = new JLabel("");
		lbServerName.setFont(new Font("Tahoma", Font.BOLD, 15));
		lbServerName.setHorizontalAlignment(SwingConstants.CENTER);
		lbServerName.setMaximumSize(new Dimension(2000,20));
		
		serverNamePanel = new JPanel();
		FlowLayout layout = new FlowLayout();
		layout.setAlignment(FlowLayout.CENTER);
		serverNamePanel.setLayout(layout);
		serverNamePanel.add(lbServerName);
	}
	
	public void initState(){
		btnConnect.setText("Connect");
		btnConnect.removeActionListener(connect);
		btnConnect.removeActionListener(disconnect);
		btnConnect.addActionListener(connect);
		btnConnect.setEnabled(true);
		
		btnSend.removeActionListener(send);
		btnSend.addActionListener(send);
		
		btnLogin.setText("Login");
		txtServerIp.setEnabled(true);
		txtServerPort.setEnabled(true);
		
		txtNickname.setEnabled(false);
		btnLogin.setEnabled(false);
		lbServerName.setText("");
	}
	
	public void initToolPanel(){

		JToolBar toolbar = new JToolBar("Text options", JToolBar.HORIZONTAL);
		
		JButton colorButton = new JButton(new ImageIcon("res/color.png"));
		toolbar.add(colorButton);
		JButton boldButton = new JButton(new ImageIcon("res/bold.png"));
		toolbar.add(boldButton);
		JButton italicButton = new JButton(new ImageIcon("res/italic.png"));
		toolbar.add(italicButton);
		toolbar.setFloatable(false);
		
		toolPanel = new JPanel();
		FlowLayout layout = new FlowLayout();
		layout.setAlignment(FlowLayout.LEFT);
		toolPanel.setLayout(layout);
		toolPanel.add(toolbar);
		toolPanel.setMaximumSize(new Dimension(150,55));
	}
	
	public void initTopPanel(){
		topPanel = new JPanel();
		initConnectionPanel();
		initServerNamePanel();
		initLoginPanel();
		
		GroupLayout layout = new GroupLayout(topPanel);

		layout.setHorizontalGroup(layout.createSequentialGroup().addComponent(connectionPanel)
																.addComponent(serverNamePanel)
																.addComponent(loginPanel));
		layout.setVerticalGroup(layout.createParallelGroup().addComponent(connectionPanel)
															.addComponent(serverNamePanel)
															.addComponent(loginPanel));
	
		topPanel.setLayout(layout);
	}

	public void initUsersPanel(){
		usersList = new JList<String>();
		usersListModel = new DefaultListModel<String>();
		usersList.setModel(usersListModel);
		
		usersList.setBorder(UIManager.getBorder("ComboBox.border"));
		
		usersListPanel = new JScrollPane(usersList);
	}
	
	public void loggedState(){
		btnConnect.removeActionListener(disconnect);
		btnConnect.removeActionListener(connect);
		btnConnect.addActionListener(disconnect);
		
		btnLogin.setText("Logout");
		btnLogin.removeActionListener(login);
		btnLogin.removeActionListener(logout);
		btnLogin.addActionListener(logout);
		btnLogin.setEnabled(true);
		
		txtNickname.setEnabled(false);
	}
	
	public void loggingState(){
		btnConnect.removeActionListener(disconnect);
		btnConnect.removeActionListener(connect);
		
		btnLogin.setText("Logging...");
		btnLogin.removeActionListener(login);
		btnLogin.removeActionListener(logout);
		btnLogin.setEnabled(false);
		
		txtNickname.setEnabled(false);
	}
	
	private void newNode(DefaultMutableTreeNode parent, ChannelTree chTree){
		if(chTree instanceof ChannelGroup){
			ChannelGroup group = (ChannelGroup)chTree;
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(group);
			parent.add(node);
			for(long id : group.getChildren()){
				newNode(node, ctrl.getChannel(id));
			}
		}
		else if(chTree instanceof Channel){
			Channel ch = (Channel)chTree;
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(ch);
			node.setAllowsChildren(false);
			parent.add(node);
		}
	}
	
	private void newNode(DefaultMutableTreeNode parent, ChannelTree chTree, Vector<Long> userChannels, Vector<Long> parentIds){
		if(chTree instanceof ChannelGroup){
			ChannelGroup group = (ChannelGroup)chTree;
			if(parentIds.contains(group.getId())){
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(group);
				parent.add(node);
				for(long id : group.getChildren()){
					newNode(node, ctrl.getChannel(id), userChannels, parentIds);
				}
			}
		}
		else if(chTree instanceof Channel){
			Channel ch = (Channel)chTree;
			if(userChannels.contains(ch.getId())){
				DefaultMutableTreeNode node = new DefaultMutableTreeNode(ch);
				node.setAllowsChildren(false);
				parent.add(node);
			}
		}
	}
	
	public void openChannelView(long channelId){
		if(!channelViews.containsKey(channelId)){
			// TODO
			// Réglage de la fenetre en fonction des infos dont on dispose.
			
			//Channel chan = (Channel)ctrl.getChannel(channelId); // A décommenter pour enlever les warnings.
			channelViews.put(channelId,new JTextPane());
			chatPanel.add(channelViews.get(channelId));
		}
	}
	
	public void resetJTrees(){
		((DefaultTreeModel)userChannels.getModel()).setRoot(new DefaultMutableTreeNode("root"));
		((DefaultTreeModel)channels.getModel()).setRoot(new DefaultMutableTreeNode("root"));
	}
	
	public void setServerName(String name){
		lbServerName.setText(name);
	}
	
	public void updateChannels() {
		DefaultMutableTreeNode root = null;
		root = new DefaultMutableTreeNode("root");
		for(long id : ((ChannelGroup)ctrl.getChannel(0)).getChildren()){
			newNode(root, ctrl.getChannel(id));
		}
		((DefaultTreeModel)channels.getModel()).setRoot(root);
	}
	
	public void updateUserChannels(Vector<Long> channels){
		Vector<Long> parentIds = new Vector<Long>();
		for(long id : channels){
			while(id != 0){
				id = ctrl.getChannel(id).getParent().getId();
				if(!parentIds.contains(id))
					parentIds.add(id);
			}
		}
		DefaultMutableTreeNode root = null;
		root = new DefaultMutableTreeNode("root");
		for(long id : ((ChannelGroup)ctrl.getChannel(0)).getChildren()){
			newNode(root, ctrl.getChannel(id), channels, parentIds);
		}
		((DefaultTreeModel)userChannels.getModel()).setRoot(root);
	}

	public void updateUsers(Vector<String> usernames) {
		this.usersListModel.clear();
		for(String username : usernames){
			usersListModel.addElement(username);
		}
		usersList.setModel(usersListModel);
	}
}

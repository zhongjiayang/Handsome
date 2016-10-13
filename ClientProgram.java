package Service;     //与服务端包名相同

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.SystemTray;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.TrayIcon;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author wealthypanda
 */

//接收图像并绘制的线程
class ReceiveImageThread implements Runnable {
    
    private String ipNumber;                     //用于存储输入的Ip地址
    private int intPortNumber;                   //用于存储输入的端口号
    private int intSleepTime=100;
    private String connectNumber;                //用于存储输入的连接密码
    private JFrame frame;                        //接收图像的主窗口
    private JPanel panelImage=new JPanel();      //绘制图像的组件
    private JPanel panelButton;                  //存放按钮的组件
    private JButton controlB;                    //控制按钮
    private JSlider setSleepTime;
    private JLabel tipsT;                        //接收图片组件的提示信息
    private BufferedImage image;                 //用于存储接收的图片
    private InputStream is2;                     //Socket输入流（接收图片）
    private Socket socket;                       //Socket
    private BufferedInputStream bis;             //用于输出密码
    private JPEGImageDecoder decoder;            //
    private ObjectOutputStream oos;
    private OutputStream os;
    private int hp;          //存储组件的高
    private int hi;          //存储图像的高
    private int wp;          //存储组件的宽
    private int wi;          //存储图像的宽    主要用于比例缩放
    private ControlInterface control;       //用于存储远程对象的存根对象（RMI相关）
    
    private MouseAndKeyOfButtonChange mkbc0 = new MouseAndKeyOfButtonChange(0);         //开启控制按钮
    private MouseAndKeyOfButtonChange mkbc1 = new MouseAndKeyOfButtonChange(1);         //开启监视按钮
    private MouseAndKeyOfButtonChange mkbc2 = new MouseAndKeyOfButtonChange(2);         //远程控制
        
    public void run() {

        ipNumber=MainFrameThread.ipNumberT.getText();                           //获得Ip地址
        try{
            intPortNumber=new Integer(MainFrameThread.portNumberT.getText());   //端口号
            if(intPortNumber>65535){
                MainFrameThread.information.append("端口号大于65535\n");
            }
        }catch(Exception e){
             MainFrameThread.information.append("IP地址与端口号非法\n");
        }
        try{
            intSleepTime=Math.abs(new Integer(MainFrameThread.sleepTimeT.getText()));
            if(intSleepTime>1000){
                MainFrameThread.information.append("毫秒数超出设置范围，使用默认值100\n");
                intSleepTime=100;
            }
        }catch(Exception e){
            MainFrameThread.information.append("毫秒数设置为空或存在非法字符\n");
            MainFrameThread.information.append("使用默认毫秒数100\n");
            intSleepTime=100;
        }
        connectNumber=MainFrameThread.connectNumberT.getText();                 //连接密码
        try {      
            socket = new Socket();                                              //创建socket，核验密码
            SocketAddress sAdd=new InetSocketAddress(ipNumber,intPortNumber);
            socket.connect(sAdd, 60000);
            os=socket.getOutputStream();
            oos=new ObjectOutputStream(os);
            oos.writeObject(connectNumber);

            is2 = socket.getInputStream();	                                // 获取网络输入流
            bis = new BufferedInputStream(is2);
            decoder = JPEGCodec.createJPEGDecoder(bis);
            socket.setTcpNoDelay(true);
            socket.setReceiveBufferSize(1024*1024);

            frame=new JFrame();                                                 //用于显示远程桌面的组件
            frame.setBounds(0, 0, 860, 574);
            frame.setIconImage(MainFrameThread.icon.getImage());
            frame.setTitle(ipNumber);
            frame.setFocusable(true);
            frame.setLayout(null);
            frame.addWindowListener(new MainFrameWinLis());
                                    
            controlB=new JButton("控  制");                                     //控制开关按钮
            controlB.setBounds(30, 5, 70, 27);
            controlB.setFocusable(false);

            setSleepTime=new JSlider(0, 1000, intSleepTime);
            setSleepTime.setBounds(150, 10, 200, 20);
            setSleepTime.addChangeListener(new SleepTimeChange());
            
            tipsT=new JLabel(String.valueOf(intSleepTime)+" ms");                                        //信息提示
            tipsT.setBounds(355, 5, 50, 27);
            tipsT.setFocusable(false);
            tipsT.setForeground(Color.red);

            tipsT.setFont(tipsT.getFont().deriveFont(20));
            tipsT.setVisible(true);

            panelImage=new JPanel();                                            //显示图像的panel
            panelImage.setFocusable(false);
            controlB.addMouseListener(mkbc0);
            
            panelButton=new JPanel();                                           //操作按钮的panel
            panelButton.setLayout(null);
            panelButton.add(controlB);
            panelButton.add(tipsT);
            panelButton.add(setSleepTime);
            panelButton.setFocusable(false);
            
            frame.add(panelImage);
            frame.add( panelButton);
            frame.setVisible(true);

            control= (ControlInterface)Naming.lookup("//"+ipNumber+":1099/Control");   //获得远程对象的存根对象（RMI相关）
            control.sleepTime(intSleepTime);                                           //调用远程方法，设置休眠时间
            MainFrameThread.information.append(MainFrameThread.getTime());
            MainFrameThread.information.append("连接成功，开始接收数据\n");

            while (!socket.isClosed()) {
                Thread.sleep(100);
                socket.sendUrgentData(0xff);  //目的在于判断远程连接是否关闭
                panelImage.setBounds(0, 0, frame.getWidth()-5, frame.getHeight()-70);
                panelButton.setBounds(0, frame.getHeight()-70, frame.getWidth(),70);
	        // 创建JPEG解码器
                image = decoder.decodeAsBufferedImage();	// 从输入流解码JPEG图片
                wi=image.getWidth();         //获得图像的大小，用于计算比例，精确控制鼠标位置
                hi=image.getHeight();
                Graphics g=panelImage.getGraphics();
                wp=panelImage.getWidth();   //获得组件的大小，用于计算比例，精确控制鼠标的位置
                hp=panelImage.getHeight();
                g.drawImage(image, 0, 0,wp,hp, panelImage);  //在组件上绘制图像
            }
        } catch (UnknownHostException ex) {
            MainFrameThread.information.append("无法连接主机\n");
        } catch (Exception ex) {
            MainFrameThread.information.append("连接关闭\n");
            frame.dispose();
        }

    }
    class MainFrameWinLis implements WindowListener{

        public void windowOpened(WindowEvent e) {
            frame.getFocusListeners();//frame获得焦点，用于监听键盘事件
        }

        public void windowClosing(WindowEvent e) {
            try {
                MainFrameThread.information.append(MainFrameThread.getTime());
                MainFrameThread.information.append("关闭与"+ipNumber+"的连接"+"\n");
                socket.close();    //SocketException 如何处理？？
            } catch (IOException ex) {
                MainFrameThread.information.append("socket无法关闭\n");
            }
        }

        public void windowClosed(WindowEvent e) {
            
        }

        public void windowIconified(WindowEvent e) {

        }

        public void windowDeiconified(WindowEvent e) {

        }

        public void windowActivated(WindowEvent e) {

            frame.getFocusListeners();
        }

        public void windowDeactivated(WindowEvent e) {

        }

    }

    class SleepTimeChange implements ChangeListener{

        public void stateChanged(ChangeEvent e) {
            try {
                control.sleepTime(setSleepTime.getValue());
                tipsT.setText(String.valueOf(setSleepTime.getValue())+" ms");
            } catch (RemoteException ex) {
                MainFrameThread.information.append("远程方法调用异常原因在于ST\n");
            }
        }

    }

    class MouseAndKeyOfButtonChange implements MouseListener, MouseMotionListener,KeyListener,MouseWheelListener{

        int whereB;
        Integer intScanP;
        String copyNetIp;
        String copyStartIp;
        String copyEndIp;
        Socket scanSocket;

        MouseAndKeyOfButtonChange(int wherebcopy) {
            whereB = wherebcopy;  //0代表开启控制；1代表开启监视；2代表控制
        }
       
        public void mouseClicked(MouseEvent e1) {
            switch(whereB){
                case 0 : {
                    
                }
                break;
                case 1 : {
                    
                }
                break;
                case 2 : {
                    
                }
                break;
            }
        }

        public void mousePressed(MouseEvent e2) {
            
                switch (whereB){
                    case 0 : {
                        if (e2.getButton() == MouseEvent.BUTTON1){
                            panelImage.addMouseMotionListener(mkbc2);         //开启对远程桌面的控制
                            panelImage.addMouseListener(mkbc2);
                            panelImage.addMouseWheelListener(mkbc2);
                            frame.addKeyListener(mkbc2);
                            controlB.setText("监  视");
                            controlB.removeMouseListener(mkbc0);
                            controlB.addMouseListener(mkbc1);
                        }                        
                    }
                    break;
                    case 1 : {
                        if(e2.getButton()==MouseEvent.BUTTON1){                  
                            panelImage.removeMouseListener(mkbc2);
                            panelImage.removeMouseMotionListener(mkbc2);
                            panelImage.removeMouseWheelListener(mkbc2);
                            frame.removeKeyListener(mkbc2);
                            controlB.setText("控  制");
                            controlB.removeMouseListener(mkbc1);
                            controlB.addMouseListener(mkbc0);
                        }
                    }
                    break;
                    case 2 : {
                        int buttons=e2.getButton();
                        switch (buttons){
                            case MouseEvent.BUTTON1 : buttons=InputEvent.BUTTON1_MASK;
                            break;
                            case MouseEvent.BUTTON2 : buttons=InputEvent.BUTTON2_MASK;
                            break;
                            case MouseEvent.BUTTON3 : buttons=InputEvent.BUTTON3_MASK;
                            break;
                        }
                        try{
                            control.controlMousePress(buttons);
                        }catch (Exception e){
                            MainFrameThread.information.append("远程控制错误在MP");
                        }
                    }
                }  
        }

        public void mouseReleased(MouseEvent e3) {
            switch(whereB){
                case 0 : {
                    
                }
                break;
                case 1 : {
                    
                }
                break;
                case 2 : {
                    int buttons=e3.getButton();
                        switch (buttons){
                            case MouseEvent.BUTTON1 : buttons=InputEvent.BUTTON1_MASK;
                            break;
                            case MouseEvent.BUTTON2 : buttons=InputEvent.BUTTON2_MASK;
                            break;
                            case MouseEvent.BUTTON3 : buttons=InputEvent.BUTTON3_MASK;
                            break;
                        }
                        try{
                            control.controlMouseRelease(buttons);
                        }catch (Exception e){
                            MainFrameThread.information.append("远程控制错误在MR");
                        }
                }
                break;
            }
        }

        public void mouseEntered(MouseEvent e4) {
            switch(whereB){
                case 0 : {
                }
                break;
                case 1 : {
                }
                break;
                case 2 : {
                }
                break;
            }
        }

        public void mouseExited(MouseEvent e5) {
            switch(whereB){
                case 0 : {
                    
                }
                break;
                case 1 : {
                    
                }
                break;
                case 2 : {
                    
                }
                break;
            }
        }

        public void keyTyped(KeyEvent e6) {
            switch(whereB){
                case 0 : {
                    
                }
                break;
                case 1 : {
                    
                }
                break;
                case 2 : {
                    
                }
                break;
            }
        }

        public void keyPressed(KeyEvent e7) {
            switch(whereB){
                case 0 : {
                    
                }
                break;
                case 1 : {
                    
                }
                break;
                case 2 : {
                    try {
                        
                        control.controlKeyPress(e7.getKeyCode());
                    } catch (Exception ex) {
                        MainFrameThread.information.append("远程控制错误在KP");
                    }
                }
                break;
            }
        }

        public void keyReleased(KeyEvent e8) {
            switch(whereB){
                case 0 : {
                    
                }
                break;
                case 1 : {
                   
                }
                break;
                case 2 : {
                     try {
                         control.controlKeyRelease(e8.getKeyCode());
                    } catch (Exception ex) {
                        MainFrameThread.information.append("远程控制错误在KR");
                    }
                }
                break;
            }
        }

        public void mouseDragged(MouseEvent e9) {
            switch(whereB){
                case 0 : {
                    
                }
                break;
                case 1 : {
                    
                }
                break;
                case 2 : {
                    int x=e9.getX();       //获得鼠标在组件中的位置
                    int y=e9.getY();
                    x=(x*(1024*10000/wp))/10000;        //计算远程鼠标移动的位置，乘以10000用于减小误差，除以10000用以确定位置
                    y=(y*(768*10000/hp))/10000;
                    try {
                        control.controlMouseMove(x, y);
                    } catch (Exception ex) {
                        MainFrameThread.information.append("远程控制错误在MD");
                    }
                }
                break;
            }
        }

        public void mouseMoved(MouseEvent e10) {
            switch(whereB){
                case 0 : {
                    
                }
                break;
                case 1 : {
                    
                }
                break;
                case 2 : {
                    int x=e10.getX();       //获得鼠标在组件中的位置
                    int y=e10.getY();
                    x=(x*(1024*10000/wp))/10000;        //计算远程鼠标移动的位置，乘以10000用于减小误差，除以10000用以确定位置
                    y=(y*(768*10000/hp))/10000;
                    try {
                        control.controlMouseMove(x, y);
                    } catch (Exception ex) {
                        MainFrameThread.information.append("远程控制错误在MM");
                    }
                }
                break;
            }          
        }

        public void mouseWheelMoved(MouseWheelEvent e11) {
            switch(whereB){
                case 0 : {

                }
                break;
                case 1 : {

                }
                break;
                case 2 : {
                     try {
                        control.controlMouseWheel(e11.getWheelRotation());
                    } catch (Exception ex) {
                        MainFrameThread.information.append("远程控制错误在MWM");
                    }
                }
                break;
            }
           
        }

    }
}

//*******************************************************************************************************************
//主界面线程
//*******************************************************************************************************************
class MainFrameThread implements Runnable{

    private JFrame mainFrame;
    static TextArea information;
    private JLabel ipNumberL;
    private JLabel portNumberL;
    private JLabel connectNumberL;
    
    static TextField ipNumberT;
    static TextField portNumberT;
    static TextField connectNumberT;
    static TextField sleepTimeT;

    private JButton moreB;
    private JButton connectB;
    
    private JLabel scanPortNL;
    private JLabel netIpNL;
    private JLabel startIpNL;
    private JLabel endIpNL;
    private JLabel sleepTimeLS;
    private JLabel sleepTimeL;
    
    private TextField scanPortNT;
    private TextField netIpNT;
    private TextField startIpNT;
    private TextField endIpNT;
    
    private JButton startB;

    private SystemTray systemTray;
    static ImageIcon icon= new ImageIcon("trayIconC.png");
    private TrayIcon trayIcon;

    private MouseAndKeyOfButtonChange mkbc0 = new MouseAndKeyOfButtonChange(0);        //用于设置拉开按钮的监听
    private MouseAndKeyOfButtonChange mkbc1 = new MouseAndKeyOfButtonChange(1);        //用于设置收起按钮的监听
    private MouseAndKeyOfButtonChange mkbc2 = new MouseAndKeyOfButtonChange(2);        //用于设置连接按钮的监听
    private MouseAndKeyOfButtonChange mkbc3 = new MouseAndKeyOfButtonChange(3);        //用于设定开始按钮的监听
    private MouseAndKeyOfButtonChange mkbc4 = new MouseAndKeyOfButtonChange(4);        //用于设定输入连接密码文本框的监听
    private MouseAndKeyOfButtonChange mkbc5 = new MouseAndKeyOfButtonChange(5);

    public void run() { 

        mainFrame=new JFrame("远程桌面系统客户端");
        mainFrame.setBounds(345, 228, 333, 253);
        mainFrame.setIconImage(icon.getImage());
        mainFrame.setResizable(false);
        mainFrame.addWindowListener(new MainFrameWinLis());

        mainFrame.setBackground(Color.lightGray);
        mainFrame.setLayout(null);

        information=new TextArea();
        information.setBounds(0, 170, 333, 73);
        information.setEditable(true);
        information.setFocusable(false);
        try {
            InetAddress ia = InetAddress.getLocalHost();
            String hostName=ia.getHostName();
            String hostIp=ia.getHostAddress();
            information.append(getTime());
            information.append("客户端程序启动\n");
            information.append("本地主机名:"+hostName+"\n");
            information.append("本地地址:"+hostIp+"\n");
        } catch (UnknownHostException ex) {
            information.append("网络异常或主机不存在\n");
            System.exit(1);
        }
        
        ipNumberL=new JLabel("请输入IP地址：");
        ipNumberL.setBounds(20, 20, 100, 20);
        ipNumberT=new TextField(15);
        ipNumberT.setBounds(120, 20, 165, 20);

        portNumberL=new JLabel("请输入端口号：");
        portNumberL.setBounds(20, 50, 100, 20);
        portNumberT=new TextField(5);
        portNumberT.setBounds(120, 50, 165, 20);

        connectNumberL=new JLabel("连接密码：");
        connectNumberL.setBounds(20, 80, 70, 20);
        connectNumberT=new TextField(6);
        connectNumberT.setBounds(90, 80, 60, 20);
        connectNumberT.setEchoChar('*');
        connectNumberT.addKeyListener(mkbc4);

        sleepTimeLS=new JLabel("设置");
        sleepTimeLS.setBounds(170, 80, 30, 20);

        sleepTimeT=new TextField();
        sleepTimeT.setBounds(200, 80, 40, 20);
        sleepTimeT.addKeyListener(mkbc5);

        sleepTimeL=new JLabel("毫秒/帧");
        sleepTimeL.setBounds(245, 80, 50, 20);
        

        moreB=new JButton("拉  开");
        moreB.setBounds(50, 120, 70, 30);
        moreB.addMouseListener(mkbc0);

        connectB=new JButton("连  接");
        connectB.setBounds(205, 120, 70, 30);
        connectB.addMouseListener(mkbc2);
        connectB.addKeyListener(mkbc2);
        
        mainFrame.add(information);
        mainFrame.add(ipNumberL);
        mainFrame.add(ipNumberT);
        mainFrame.add(portNumberL);
        mainFrame.add(portNumberT);
        mainFrame.add(connectNumberL);
        mainFrame.add(connectNumberT);
        mainFrame.add(sleepTimeLS);
        mainFrame.add(sleepTimeT);
        mainFrame.add(sleepTimeL);
        mainFrame.add(moreB);
        mainFrame.add(connectB);

        mainFrame.setVisible(true);
        
    }

    static String getTime(){
        GregorianCalendar gc=new GregorianCalendar();
        String now=gc.get(Calendar.HOUR_OF_DAY)+"时"+gc.get(Calendar.MINUTE)+"分"+gc.get(Calendar.SECOND)+"秒";
        return now;
    }

    class MouseAndKeyOfButtonChange implements MouseListener, KeyListener{

        int whereB;
        Integer intScanP;
        String copyNetIp;
        String copyStartIp;
        String copyEndIp;
        Socket scanSocket;

        MouseAndKeyOfButtonChange(int wherebcopy) {
            whereB = wherebcopy;
        }

        public void mouseClicked(MouseEvent e1) {

        }

        public void mousePressed(MouseEvent e2) {
            if (e2.getButton() == MouseEvent.BUTTON1){
                switch(whereB){
                    case 0 : {
                        moreB.removeMouseListener(mkbc0);
                        moreB.addMouseListener(mkbc1);
                        mainFrame.setVisible(false);
                        moreB.setText("收  起");
                        mainFrame.setBounds(345, 128,333, 423);

                        scanPortNL=new JLabel("要扫描端口号：");
                        scanPortNL.setBounds(20, 260, 100, 20);
                        scanPortNT=new TextField("8000");
                        scanPortNT.setBounds(120, 260, 150, 20);

                        netIpNL=new JLabel("输入网络地址：");
                        netIpNL.setBounds(20, 290, 100, 20);
                        netIpNT=new TextField("192.168.1.");
                        netIpNT.setBounds(120, 290, 150, 20);

                        startIpNL=new JLabel("输入主机地址：  从");
                        startIpNL.setBounds(20, 320, 115, 20);
                        startIpNT=new TextField("1");
                        startIpNT.setBounds(135, 320, 30, 20);

                        endIpNL=new JLabel("到");
                        endIpNL.setBounds(170, 320, 15, 20);
                        endIpNT=new TextField("254");
                        endIpNT.setBounds(190, 320, 30, 20);

                        startB=new JButton("开  始");
                        startB.setBounds(190, 350, 70, 30);
                        startB.addMouseListener(mkbc3);
                                              
                        mainFrame.add(scanPortNL);
                        mainFrame.add(scanPortNT);
                        mainFrame.add(netIpNL);
                        mainFrame.add(netIpNT);
                        mainFrame.add(startIpNL);
                        mainFrame.add(startIpNT);
                        mainFrame.add(endIpNL);
                        mainFrame.add(endIpNT);
                        mainFrame.add(startB);
                        mainFrame.setVisible(true);                       
                    }
                    break;
                    case 1 : {
                        moreB.setText("拉  开");
                        moreB.removeMouseListener(mkbc1);
                        moreB.addMouseListener(mkbc0);
                        mainFrame.setBounds(345, 228, 333, 253);
                    }
                    break;
                    case 2 : {
                        information.append(getTime());
                        information.append("正在与"+ipNumberT.getText()+"请求连接……\n");
                        ReceiveImageThread rit=new ReceiveImageThread();
                        Thread t2=new Thread(rit);
                        t2.start();
                    }
                    break;
                    case 3 : {
                        information.append(getTime());
                        information.append("开始扫描\n");
                        intScanP=new Integer(scanPortNT.getText());
                        copyNetIp=netIpNT.getText();
                        int intStartIp=new Integer(startIpNT.getText());
                        int intEndIp=new Integer(endIpNT.getText());                       
                        for(;intStartIp<=intEndIp||intStartIp==254;intStartIp++){                         
                            try {
                                scanSocket = new Socket();
                                scanSocket.setSoLinger(true, 0);
                                SocketAddress sa=new InetSocketAddress(copyNetIp + String.valueOf(intStartIp),intScanP);
                                scanSocket.connect(sa, 500);
                                OutputStream os=scanSocket.getOutputStream();
                                ObjectOutputStream oos=new ObjectOutputStream(os);
                                oos.writeObject("测试连接");
                                information.append(copyNetIp + String.valueOf(intStartIp)+"可连接\n");
                            }catch (IOException ex) {                               
                                information.append(copyNetIp + String.valueOf(intStartIp)+"未连接\n");
                            }
                        }
                    }
                    information.append(getTime());
                    information.append("扫描结束\n");
                    break;
                    case 5 : {
                        mainFrame.setVisible(true);
                        systemTray.remove(trayIcon);
                    }
                    break;
                }
            }
        }

        public void mouseReleased(MouseEvent e3) {

        }

        public void mouseEntered(MouseEvent e4) {

        }

        public void mouseExited(MouseEvent e5) {

        }

        public void keyTyped(KeyEvent e6) {

        }

        public void keyPressed(KeyEvent e7) {
            switch(whereB){
                case 2 : {
                   switch(e7.getKeyCode()){
                        case 10 : {
                            information.append(getTime());
                            information.append("与"+ipNumberT.getText()+"建立连接\n");
                            ReceiveImageThread rit=new ReceiveImageThread();
                            Thread t2=new Thread(rit);
                            t2.start();
                        }
                        break;
                    }
                }
                break;
                case 4 : {
                    switch(e7.getKeyCode()){
                        case 10 : {
                            information.append(getTime());
                            information.append("与"+ipNumberT.getText()+"建立连接\n");
                            ReceiveImageThread rit=new ReceiveImageThread();
                            Thread t2=new Thread(rit);
                            t2.start();
                        }
                        break;
                    }
                }
                break;
                case 5 : {
                    switch(e7.getKeyCode()){
                        case 10 : {
                            information.append(getTime());
                            information.append("与"+ipNumberT.getText()+"建立连接\n");
                            ReceiveImageThread rit=new ReceiveImageThread();
                            Thread t2=new Thread(rit);
                            t2.start();
                        }
                        break;
                    }
                }
            }         
        }

        public void keyReleased(KeyEvent e8) {

        }

    }
    class MainFrameWinLis implements WindowListener{

        public void windowOpened(WindowEvent e9) {

        }

        public void windowClosing(WindowEvent e10) {
            System.exit(0);
        }

        public void windowClosed(WindowEvent e11) {
            System.exit(0);
        }

        public void windowIconified(WindowEvent e12) {
            initSystemTray();
            mainFrame.setVisible(false);
        }

        public void windowDeiconified(WindowEvent e13) {

        }

        public void windowActivated(WindowEvent e14) {

        }

        public void windowDeactivated(WindowEvent e15) {

        }

    }
    //最小化系统托盘的方法
    private void initSystemTray() {
        if (SystemTray.isSupported()) {
            systemTray = SystemTray.getSystemTray();
        }
        //icon = new ImageIcon("trayIconC.png");
        trayIcon = new TrayIcon(icon.getImage());
        trayIcon.setImageAutoSize(true);
        trayIcon.addMouseListener(new MouseAndKeyOfButtonChange(5));
        try {
            systemTray.add(trayIcon);
        } catch (AWTException e) {
            MainFrameThread.information.append("找不到文件:trayIcon.png");
        }
    }
}
public class ClientProgram{
    public static void main(String args[]){
        MainFrameThread aaa=new MainFrameThread();
        Thread t1=new Thread(aaa);
        t1.start();
    }
}
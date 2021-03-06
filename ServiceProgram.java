package Service;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import java.awt.AWTException;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.SystemTray;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.KeyListener;
import java.io.*;
import java.awt.event.MouseListener;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;


//主界面线程
/**
 * 
 * 
 */
public class ServiceProgram {                                  //*************主类，启动线程

    public static void main(String args[]) {
        
        MainFrameThread MFT = new MainFrameThread();         //创建主界面对象
        Thread t1 = new Thread(MFT);
        t1.setPriority(1);
        t1.start();                                     //启动主界面线程
    }
}
class MainFrameThread implements Runnable {               //*****************主界面类，用于基本设置

    private RandomAccessFile RandomNDF;          //随机读写文件
    private int readPoint[] = new int[]{6, 14};     //文件指针定位
    static String portN;                 //端口号
    static String connectN;              //连接密码
    static String manageN;               //管理密码
    static Thread t2;                    //监听线程
    private JFrame mainFrame;               //主界面
    private TextField openmanageT;          //用于输入打开管理密码的文本框
    static TextArea information;    //多行文本框，用于显示信息
    private TextField portNumberT;          //端口文本框
    private TextField connectNumberT;       //连接密码文本框
    private TextField reInputCNT;           //再次输入连接密码文本框
    private TextField manageNumberT;        //管理密码文本框
    private TextField reInputMNT;           //再次输入管理密码文本框
    private JButton defaultB;               //默认按钮
    private JButton settedB;                //设定按钮
    private JButton exitB;                  //退出按钮
    private JButton openmanageB;            //设置按钮
    private JDialog exitBD;                 //退出对话框
    private SystemTray systemTray;         //可获取系统托盘区，也可判断系统是否支持系统托盘
    private ImageIcon icon;                //可创建系统托盘图像
    private TrayIcon trayIcon;             //可添加系统托盘
    private MouseAndKeyOfButtonChange mkbc0 = new MouseAndKeyOfButtonChange(0);        //用于退出按钮的监听与撤销
    private MouseAndKeyOfButtonChange mkbc1 = new MouseAndKeyOfButtonChange(1);        //用于设置按钮的监听与撤销
    private MouseAndKeyOfButtonChange mkbc2 = new MouseAndKeyOfButtonChange(2);        //用于默认按钮的监听与撤销
    private MouseAndKeyOfButtonChange mkbc3 = new MouseAndKeyOfButtonChange(3);        //用于设定按钮的监听与撤销

    /**
     * run方法
     * 初始化界面
     * 创建密码数据文件
     * 从密码数据文件中读取密码
     * 监听端口
     * 等待连接
     * 设定仅可创建一个连接*
     */
    public void run() {

        mainFrame = new JFrame("远程桌面监控系统");             //远程桌面系统主界面
        mainFrame.setLocation(700, 200);                     //远程桌面窗口启动时在显示屏的位置
        mainFrame.setSize(303, 530);                         //远程桌面窗口的大小
        icon = new ImageIcon("trayIconS.png");
        mainFrame.setIconImage(icon.getImage());             //改变frame左上角图标

        mainFrame.setResizable(false);                       //不可以改变大小
        mainFrame.addWindowListener(new MainFrameWinLis());  //主窗口的窗口事件监听

        mainFrame.setBackground(Color.lightGray);         //背景色为浅灰色
        mainFrame.setLayout(null);                        //不使用布局管理器

        information = new TextArea();                       //设定信息显示区域的位置与大小
        information.setBounds(0, 350, 300, 150);          //设置大小及位置
        information.setEditable(true);                   //可编辑状态
        information.setFocusable(false);                 //不可得到焦点   也就不可编辑，目的在于Tab键循环
        mainFrame.add(information);                       //在mainFrame中添加多行文本框，首先添加是为了接收信息。
        information.append(getTime());                   //显示系统启动时间
        information.append("服务端启动\n");

        //判断文件是否存在，不存在则创建，并写入初始数据；
        File NumberDataFile = new File("NumberDataFile.txt");               //创建文件对象
        if (!NumberDataFile.exists()) {                                 //测试文件是否存在
            try {
                NumberDataFile.createNewFile();                       //如果不存在，则创建文件
                RandomNDF = new RandomAccessFile(NumberDataFile, "rw"); //以读写方式创建随即读写文件
                information.append("未发现数据文件，重新初始化∶）\n");
                RandomNDF.seek(0);                                    //指针定位到文件头
                RandomNDF.writeUTF("8000");
                information.append("端口号初始化成功∶）\n");
                RandomNDF.seek(readPoint[0]);
                RandomNDF.writeUTF("123456");
                information.append("连接密码初始化成功∶）\n");
                RandomNDF.seek(readPoint[1]);
                RandomNDF.writeUTF("888888");
                information.append("管理密码初始化成功∶）\n");
                RandomNDF.seek(0);
            } catch (IOException ex) {
                information.append("文件创建失败∶（\n");
            }
        }
        try {
            RandomNDF = new RandomAccessFile(NumberDataFile, "rw");   //如果文件存在，创建随即读写文件
            try {
                RandomNDF.seek(0);                                    //定位到文件头
                portN = RandomNDF.readUTF();                            //读取数据
                information.append("端口号读取成功∶）\n");
                RandomNDF.seek(readPoint[0]);
                connectN = RandomNDF.readUTF();
                information.append("连接密码读取成功∶）\n");
                RandomNDF.seek(readPoint[1]);
                manageN = RandomNDF.readUTF();
                information.append("管理密码读取成功∶）\n");
                RandomNDF.seek(0);
            } catch (IOException ex) {
                information.append("文件读取失败∶（\n");
            }
        } catch (FileNotFoundException ex) {
            information.append("文件未找到∶（\n");
        }

        exitB = new JButton("退  出");                    //创建设定按钮
        exitB.setBounds(50, 70, 70, 30);                  //设定按钮大小与位置
        exitB.addMouseListener(mkbc0);                    //鼠标监听

        openmanageB = new JButton("设  置");              //创建退出按钮
        openmanageB.setBounds(170, 70, 70, 30);            //设定按钮大小与位置
        openmanageB.addMouseListener(mkbc1);               //鼠标监听

        JLabel openmanage = new JLabel("请输入密码:");      //密码输入框，需正确输入密码，才可操作其他
        openmanage.setBounds(20, 20, 120, 20);
        openmanageT = new TextField(6);
        openmanageT.setBounds(145, 20, 120, 20);
        openmanageT.setEchoChar('*');

        JLabel portNumber = new JLabel("设置监听端口:");     //重新设定监听端口的标签与单行文本框
        portNumber.setBounds(20, 125, 120, 20);
        portNumberT = new TextField(portN, 5);
        portNumberT.setBounds(145, 125, 120, 20);
        portNumberT.setEnabled(false);

        JLabel connectNumber = new JLabel("重设连接密码:");   //重新设定连接密码的标签与文本框
        connectNumber.setBounds(20, 165, 120, 20);
        connectNumberT = new TextField(connectN, 6);
        connectNumberT.setBounds(145, 165, 120, 20);
        connectNumberT.setEchoChar('*');
        connectNumberT.setEnabled(false);

        JLabel reInputCN = new JLabel("再次输入密码:");      //再次输入连接密码
        reInputCN.setBounds(20, 190, 120, 20);
        reInputCNT = new TextField(connectN, 6);
        reInputCNT.setBounds(145, 190, 120, 20);
        reInputCNT.setEchoChar('*');
        reInputCNT.setEnabled(false);

        JLabel manageNumber = new JLabel("重设管理密码：");  //重新设定管理密码的标签与文本框
        manageNumber.setBounds(20, 220, 120, 20);
        manageNumberT = new TextField(manageN, 6);
        manageNumberT.setBounds(145, 220, 120, 20);
        manageNumberT.setEchoChar('*');
        manageNumberT.setEnabled(false);

        JLabel reInputMN = new JLabel("再次输入密码:");     //再次输入管理密码
        reInputMN.setBounds(20, 245, 120, 20);
        reInputMNT = new TextField(manageN, 6);
        reInputMNT.setBounds(145, 245, 120, 20);
        reInputMNT.setEchoChar('*');
        reInputMNT.setEnabled(false);

        defaultB = new JButton("默  认");                  //创建退出按钮
        defaultB.setBounds(50, 300, 70, 30);                //设定按钮大小与位置
        defaultB.setEnabled(false);

        settedB = new JButton("设  定");                   //创建设定按钮
        settedB.setBounds(170, 300, 70, 30);             //设定按钮大小与位置
        settedB.setEnabled(false);

        mainFrame.add(exitB);                           //添加各种组件
        mainFrame.add(openmanageB);
        mainFrame.add(portNumber);
        mainFrame.add(openmanage);
        mainFrame.add(openmanageT);
        mainFrame.add(connectNumber);
        mainFrame.add(reInputCN);
        mainFrame.add(manageNumber);
        mainFrame.add(reInputMN);
        mainFrame.add(portNumberT);
        mainFrame.add(connectNumberT);
        mainFrame.add(reInputCNT);
        mainFrame.add(manageNumberT);
        mainFrame.add(reInputMNT);
        mainFrame.add(settedB);
        mainFrame.add(defaultB);

        //mainFrame.setVisible(true);   //(保留的目的是，防止不支持系统托盘） //显示主窗口，代码放在这里的目的是保证组件显示全面
        initSystemTray();    //启动时，最小化到系统托盘
        startsit();          //启动监听线程的方法

        

    }

    //创建启动监听线程的方法
    static void startsit() {
        startSendImageThread startSIT = new startSendImageThread();  //创建监听线程对象
        t2 = new Thread(startSIT);
        t2.start();                                                 //启动监听线程
        MainFrameThread.information.append(getTime());
    }

    static String getTime(){                                  //获得系统时间方法
        GregorianCalendar gc=new GregorianCalendar();
        String now=gc.get(Calendar.HOUR_OF_DAY)+"时"+gc.get(Calendar.MINUTE)+"分"+gc.get(Calendar.SECOND)+"秒";
        return now;
    }

    //创建窗口监听类
    class MainFrameWinLis implements WindowListener {

        public void windowOpened(WindowEvent e) {
        }

        public void windowClosing(WindowEvent e) {      //点击关闭按钮时，最小化的系统托盘
            initSystemTray();
        }

        public void windowClosed(WindowEvent e) {
        }

        public void windowIconified(WindowEvent e) {
        }

        public void windowDeiconified(WindowEvent e) {
        }

        public void windowActivated(WindowEvent e) {
        }

        public void windowDeactivated(WindowEvent e) {
        }
    }

    //实现按钮事件监听类
    class MouseAndKeyOfButtonChange implements MouseListener, KeyListener {

        int whereB;

        MouseAndKeyOfButtonChange(int wherebcopy) {
            whereB = wherebcopy;
        }

        public void mouseClicked(MouseEvent e1) {
        }

        public void mousePressed(MouseEvent e2) {
            if (e2.getButton() == MouseEvent.BUTTON1) {         //只设置左键单击
                switch (whereB) {
                    case 0: {
                        if (manageN.equals(openmanageT.getText())) {                           //如果密码输入正确，弹出对话框，确认。
                            openmanageT.setText(null);
                            exitBD = new JDialog(mainFrame, "退出确认", true);
                            exitBD.setBounds(400, 300, 400, 200);
                            exitBD.setLayout(null);
                            exitBD.setResizable(false);
                            JButton yesB = new JButton("是");
                            yesB.setBounds(70, 100, 70, 30);
                            yesB.addMouseListener(new MouseAndKeyOfButtonChange(4));
                            JButton noB = new JButton("否");
                            noB.setBounds(240, 100, 70, 30);
                            noB.addMouseListener(new MouseAndKeyOfButtonChange(5));
                            JLabel sureL = new JLabel("确定要退出吗？");
                            sureL.setBounds(150, 30, 120, 20);
                            exitBD.add(sureL);
                            exitBD.add(yesB);
                            exitBD.add(noB);
                            exitBD.setVisible(true);//此代码一定放在最后，因为如果先执行此代码，且模式为真，则等待对话，线程停止，无法执行下面的代码。
                        } else {
                            JDialog exiterrorN = new JDialog(mainFrame, "error", true);         //密码输入不正确，提示重新输入
                            openmanageT.setText(null);
                            exiterrorN.setBounds(400, 300, 300, 100);
                            exiterrorN.setResizable(false);
                            exiterrorN.setLayout(null);
                            JLabel errorNumber = new JLabel("密码错误，请重新输入！∶（");
                            errorNumber.setBounds(50, 20, 170, 20);
                            exiterrorN.add(errorNumber);
                            exiterrorN.setVisible(true);
                        }
                    }//case 0   退出按钮
                    break;
                    case 1: {
                        if (manageN.equals(openmanageT.getText())) {              //密码输入正确，则激活下面文本框与按钮，令密码输入框不可用
                            MainFrameThread.information.append(MainFrameThread.getTime());
                            information.append("更改系统设置∶）\n");
                            openmanageB.setEnabled(false);
                            openmanageB.removeMouseListener(this);
                            openmanageT.setEnabled(false);
                            portNumberT.setEnabled(true);
                            connectNumberT.setEnabled(true);
                            reInputCNT.setEnabled(true);
                            manageNumberT.setEnabled(true);
                            reInputMNT.setEnabled(true);
                            defaultB.setEnabled(true);
                            defaultB.addMouseListener(mkbc2);
                            settedB.setEnabled(true);
                            settedB.addMouseListener(mkbc3);
                            openmanageT.setText(null);
                        } else {
                            openmanageT.setText(null);
                            JDialog secreterror = new JDialog(mainFrame, "error", true);  //密码输入错误，提示重新输入
                            secreterror.setBounds(400, 300, 300, 100);
                            secreterror.setLayout(null);
                            secreterror.setResizable(false);
                            JLabel errorNumber = new JLabel("密码错误，请重新输入！∶（");
                            errorNumber.setBounds(50, 20, 170, 20);
                            secreterror.add(errorNumber);
                            secreterror.setVisible(true);
                        }
                    }//case 1   打开管理按钮
                    break;
                    case 2: {
                        try {
                            information.append("正在恢复默认值……∶）\n");
                            RandomNDF.seek(0);
                            RandomNDF.writeUTF("8000");
                            portNumberT.setText("8000");
                            portN="8000";
                            information.append("端口号恢复成功∶）\n");
                            RandomNDF.seek(readPoint[0]);
                            RandomNDF.writeUTF("123456");
                            connectNumberT.setText("123456");
                            reInputCNT.setText("123456");
                            connectN="123456";
                            information.append("连接密码恢复成功∶）\n");
                            RandomNDF.seek(readPoint[1]);
                            RandomNDF.writeUTF("888888");
                            manageNumberT.setText("888888");
                            reInputMNT.setText("888888");
                            manageN="888888";
                            information.append("管理密码恢复成功∶）\n");
                            RandomNDF.seek(0);
                            openmanageB.setEnabled(true);
                            openmanageB.addMouseListener(mkbc1);
                            openmanageT.setEnabled(true);
                            portNumberT.setEnabled(false);
                            connectNumberT.setEnabled(false);
                            reInputCNT.setEnabled(false);
                            manageNumberT.setEnabled(false);
                            reInputMNT.setEnabled(false);
                            defaultB.setEnabled(false);
                            defaultB.removeMouseListener(mkbc2);
                            settedB.setEnabled(false);
                            settedB.removeMouseListener(mkbc3);
                        } catch (IOException ex) {
                            information.append("恢复失败是因为文件写入失败∶）\n");
                        }
                    }//case 2   默认
                    break;
                    case 3: {
                        try {
                            String pnt1 = portNumberT.getText();
                            String pnt2 = connectNumberT.getText();
                            String pnt3 = reInputCNT.getText();
                            String pnt4 = manageNumberT.getText();
                            String pnt5 = reInputMNT.getText();
                            try {
                                Integer intNumber = new Integer(pnt1);
                                if (intNumber < 1024 || intNumber > 65535) {
                                    information.append("端口号必须大于1024小于65535∶(\n");
                                    information.append("数据输入错误，未能改变系统设置∶(\n");
                                    portNumberT.setText(portN);
                                    connectNumberT.setText(connectN);
                                    reInputCNT.setText(connectN);
                                    manageNumberT.setText(manageN);
                                    reInputMNT.setText(manageN);
                                    RandomNDF.seek(0);
                                    openmanageB.setEnabled(true);
                                    openmanageB.addMouseListener(mkbc1);
                                    openmanageT.setEnabled(true);
                                    portNumberT.setEnabled(false);
                                    connectNumberT.setEnabled(false);
                                    reInputCNT.setEnabled(false);
                                    manageNumberT.setEnabled(false);
                                    reInputMNT.setEnabled(false);
                                    defaultB.setEnabled(false);
                                    defaultB.removeMouseListener(mkbc2);
                                    settedB.setEnabled(false);
                                    settedB.removeMouseListener(mkbc3);
                                } else {
                                    switch (textFiledNumberCompare(pnt2, pnt3)) {
                                        case 1: {
                                            information.append("“连接密码”数据输入错误，未能改变系统设置∶(\n");
                                            portNumberT.setText(portN);
                                            connectNumberT.setText(connectN);
                                            reInputCNT.setText(connectN);
                                            manageNumberT.setText(manageN);
                                            reInputMNT.setText(manageN);
                                            RandomNDF.seek(0);
                                            openmanageB.setEnabled(true);
                                            openmanageB.addMouseListener(mkbc1);
                                            openmanageT.setEnabled(true);
                                            portNumberT.setEnabled(false);
                                            connectNumberT.setEnabled(false);
                                            reInputCNT.setEnabled(false);
                                            manageNumberT.setEnabled(false);
                                            reInputMNT.setEnabled(false);
                                            defaultB.setEnabled(false);
                                            defaultB.removeMouseListener(mkbc2);
                                            settedB.setEnabled(false);
                                            settedB.removeMouseListener(mkbc3);
                                        }
                                        break;
                                        case 2: {
                                            information.append("“连接密码”数据输入错误，未能改变系统设置∶(\n");
                                            portNumberT.setText(portN);
                                            connectNumberT.setText(connectN);
                                            reInputCNT.setText(connectN);
                                            manageNumberT.setText(manageN);
                                            reInputMNT.setText(manageN);
                                            RandomNDF.seek(0);
                                            openmanageB.setEnabled(true);
                                            openmanageB.addMouseListener(mkbc1);
                                            openmanageT.setEnabled(true);
                                            portNumberT.setEnabled(false);
                                            connectNumberT.setEnabled(false);
                                            reInputCNT.setEnabled(false);
                                            manageNumberT.setEnabled(false);
                                            reInputMNT.setEnabled(false);
                                            defaultB.setEnabled(false);
                                            defaultB.removeMouseListener(mkbc2);
                                            settedB.setEnabled(false);
                                            settedB.removeMouseListener(mkbc3);
                                        }
                                        break;
                                        case 0: {                                        
                                            switch (textFiledNumberCompare(pnt4, pnt5)) {
                                                case 1: {
                                                    information.append("“管理密码”数据输入错误，未能改变系统设置∶(\n");
                                                    portNumberT.setText(portN);
                                                    connectNumberT.setText(connectN);
                                                    reInputCNT.setText(connectN);
                                                    manageNumberT.setText(manageN);
                                                    reInputMNT.setText(manageN);
                                                    RandomNDF.seek(0);
                                                    openmanageB.setEnabled(true);
                                                    openmanageB.addMouseListener(mkbc1);
                                                    openmanageT.setEnabled(true);
                                                    portNumberT.setEnabled(false);
                                                    connectNumberT.setEnabled(false);
                                                    reInputCNT.setEnabled(false);
                                                    manageNumberT.setEnabled(false);
                                                    reInputMNT.setEnabled(false);
                                                    defaultB.setEnabled(false);
                                                    defaultB.removeMouseListener(mkbc2);
                                                    settedB.setEnabled(false);
                                                    settedB.removeMouseListener(mkbc3);
                                                }
                                                break;
                                                case 2: {
                                                    information.append("“管理密码”数据输入错误，未能改变系统设置∶(\n");
                                                    portNumberT.setText(portN);
                                                    connectNumberT.setText(connectN);
                                                    reInputCNT.setText(connectN);
                                                    manageNumberT.setText(manageN);
                                                    reInputMNT.setText(manageN);
                                                    RandomNDF.seek(0);
                                                    openmanageB.setEnabled(true);
                                                    openmanageB.addMouseListener(mkbc1);
                                                    openmanageT.setEnabled(true);
                                                    portNumberT.setEnabled(false);
                                                    connectNumberT.setEnabled(false);
                                                    reInputCNT.setEnabled(false);
                                                    manageNumberT.setEnabled(false);
                                                    reInputMNT.setEnabled(false);
                                                    defaultB.setEnabled(false);
                                                    defaultB.removeMouseListener(mkbc2);
                                                    settedB.setEnabled(false);
                                                    settedB.removeMouseListener(mkbc3);
                                                }
                                                break;
                                                case 0: {
                                                    RandomNDF.seek(0);
                                                    RandomNDF.writeUTF(pnt1);
                                                    portN=String.valueOf(pnt1);
                                                    RandomNDF.seek(readPoint[0]);
                                                    RandomNDF.writeUTF(connectNumberT.getText());
                                                    connectN=String.valueOf(pnt2);
                                                    RandomNDF.seek(readPoint[1]);
                                                    RandomNDF.writeUTF(manageNumberT.getText());
                                                    manageN=String.valueOf(pnt4);
                                                    MainFrameThread.information.append(MainFrameThread.getTime());
                                                    information.append("系统设置成功\n");
                                                    openmanageB.setEnabled(true);
                                                    openmanageB.addMouseListener(mkbc1);
                                                    openmanageT.setEnabled(true);
                                                    portNumberT.setEnabled(false);
                                                    connectNumberT.setEnabled(false);
                                                    reInputCNT.setEnabled(false);
                                                    manageNumberT.setEnabled(false);
                                                    reInputMNT.setEnabled(false);
                                                    defaultB.setEnabled(false);
                                                    defaultB.removeMouseListener(mkbc2);
                                                    settedB.setEnabled(false);
                                                    settedB.removeMouseListener(mkbc3);
                                                }
                                                break;
                                            }
                                        }
                                        break;
                                    }
                                }
                            } catch (RuntimeException i) {
                                information.append("端口号必须为数字，不能有其它字符∶(\n");
                                portNumberT.setText(portN);
                            }
                        } catch (IOException ex) {
                            information.append("输入输出错误，强行退出∶(\n");
                            System.exit(1);
                        }
                        break;
                    }//  设定
                    case 4: {
                        System.exit(0);
                    }//case 4   是
                    break;
                    case 5: {
                        exitBD.dispose();
                    }//case 5   否
                    break;
                    default:
                        break;
                    case 6: {
                        mainFrame.setVisible(true);
                        systemTray.remove(trayIcon);
                    }
                    break;
                }
            }
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void keyTyped(KeyEvent e) {
        }

        public void keyPressed(KeyEvent e) {
        }

        public void keyReleased(KeyEvent e) {
        }

        int textFiledNumberCompare(String str1, String str2) {      //更改系统设置时，密码校验方法

            int n;
            if (str1.length() != 6) {
                information.append("密码必须为6位\n");    //密码6位；
                n = 1;
                return n;
            }
            if (!str1.equals(str2)) {
                information.append("密码两次输入不匹配\n");    //两次输入不匹配；
                n = 2;
                return n;
            }
            n = 0;
            return n;

        }
    }

//初始化系统托盘的方法
    private void initSystemTray() {
        if (SystemTray.isSupported()) {
            systemTray = SystemTray.getSystemTray();
        }
        trayIcon = new TrayIcon(icon.getImage());
        trayIcon.setImageAutoSize(true);
        trayIcon.addMouseListener(new MouseAndKeyOfButtonChange(6));
        try {
            systemTray.add(trayIcon);
        } catch (AWTException e) {
            MainFrameThread.information.append("找不到文件:trayIcon.png");
        }
    }
}

//启动监听线程的类
class startSendImageThread implements Runnable {

    static ServerSocket serverSocket;
    static Socket socket = null;
    static Thread t3;                 //图片发送线程
    private String readedConnectN = "$$$$$$";   //设置这个符号的目的是防止和connectN取得同样的初始化值；
    private Integer intNumber = new Integer(MainFrameThread.portN);     //将定义的String对象转换为Integer对象
    static ObjectInputStream ois;
    static InputStream os;

    public void run() {
        
        try {
            serverSocket = new ServerSocket(intNumber.intValue(), 1);              //监听端口
            MainFrameThread.information.append("监听线程启动∶）\n端口号：" + intNumber.intValue() + "   监听中……∶）\n");  //在信息区域中，显示状态
            socket = serverSocket.accept();               //等待连接
            } catch (Exception e) {
            try {
                startSendImageThread.socket.close();
                startSendImageThread.socket = null;
                startSendImageThread.serverSocket.close();
                MainFrameThread.information.append("监听端口失败或等待连接失败\n");
                MainFrameThread.information.append("重启监听线程\n");
                MainFrameThread.startsit();
            } catch (IOException ex) {
                MainFrameThread.information.append("套接字关闭失败，强行退出\n");
                System.exit(1);
            }
        }

        try {                                              //判断密码是否正确
            socket.setOOBInline(false);
            os = socket.getInputStream();
            ois =new ObjectInputStream(os);
            readedConnectN = (String) ois.readObject();  
        } catch (Exception ex) {
            try {
                startSendImageThread.socket.close();
                startSendImageThread.socket = null;
                startSendImageThread.serverSocket.close();
                startSendImageThread.ois.close();
                startSendImageThread.os.close();
                MainFrameThread.information.append("客户端连接密码读取失败\n");
                MainFrameThread.information.append("关闭与其建立的连接，重启监听线程\n");
                MainFrameThread.startsit();
            } catch (IOException et) {
                MainFrameThread.information.append("套接字关闭失败，强行退出\n");
                System.exit(1);
            }
        }
        if (MainFrameThread.connectN.equals(readedConnectN)) {
            SendImageThread SIT = new SendImageThread(socket);         //创建图片发送线程对象
            t3 = new Thread(SIT);
            t3.setPriority(5);
            t3.start();                                             //启动图片发送线程
            MainFrameThread.information.append(MainFrameThread.getTime());
            MainFrameThread.information.append("密码验证成功\n");
            try {
                serverSocket.close();                                       //关闭监听（用于设置只能被一台主机监控）
                MainFrameThread.information.append("已启动桌面信息发送线程\n");
                MainFrameThread.information.append("监听线程关闭\n");
                MainFrameThread.information.append(MainFrameThread.getTime());
                MainFrameThread.information.append("正在发送桌面信息……\n");
            } catch (IOException ex) {
                MainFrameThread.information.append("serverSocket异常，强行退出\n");
                System.exit(1);
            }
        } else {
            try {
                startSendImageThread.socket.close();
                startSendImageThread.socket = null;
                startSendImageThread.serverSocket.close();
                startSendImageThread.ois.close();
                startSendImageThread.os.close();
                MainFrameThread.information.append(MainFrameThread.getTime());
                MainFrameThread.information.append("客户端连接密码错误\n");
                MainFrameThread.information.append("关闭与其建立的连接，重启监听线程\n");
                MainFrameThread.startsit();
            } catch (IOException ex) {
                MainFrameThread.information.append("套接字或输出流关闭失败，试强行终止\n");
                System.exit(1);
            }
        }

    }
}

//启动抓图线程
/**
 * 实现抓图
 * 实现图片发送线程
 * 根据当前鼠标位置绘制图形用以标记位置鼠标在客户端可见
 */
class SendImageThread implements Runnable {

    static int sleepTime=0;
    private Dimension screenSize;                          //用于存储屏幕尺寸
    private Rectangle rectangle;                           //用于定义区域
    static Robot robot;                                   //用于调用抓图方法
    private BufferedImage image;                           //用于存储比较图像
    private BufferedImage screenImage;                     //用于存储，绘制像

    SendImageThread(Socket socket) {              //构造函数
        try {
            socket.setSendBufferSize(1024 * 1024);   //设置缓冲区大小
            socket.setTcpNoDelay(true);            //确保数据及时发送
            robot = new Robot();                     //创建Robot对象，用于抓图
            } catch (Exception e) {
            MainFrameThread.information.append("无法对socket进行设置\n");
        }
    }

    public void run() {

        InetAddress ipAddI=startSendImageThread.socket.getLocalAddress();  //创建InetAddress对象
        String ipAddS=ipAddI.getHostAddress();                            //获得本地Ip地址
        ControlService cs=new ControlService();                         //创建服务对象
        cs.runns(robot, ipAddS);                                      //启动RMI服务器程序

        Toolkit toolkit = Toolkit.getDefaultToolkit();     //静态方法getDefaultToolkit（）用于获取默认工具包， 返回Toolkit对象，该对象可以使用Toolkit类中定义方法，本程序实现抓图。
        screenSize = toolkit.getScreenSize();              //获取屏幕大小，换回Dimension对象
        rectangle = new Rectangle(screenSize);             //定义一个和屏幕尺寸大小相同的区域
        OutputStream outputStream = null;                  //输出流对象
        BufferedOutputStream bout = null;                  //缓冲输出流对象
        try {
            outputStream = startSendImageThread.socket.getOutputStream();        //获得socket输出流
            bout = new BufferedOutputStream(outputStream);  //缓冲输出流
            } catch (Exception e1) {
            MainFrameThread.information.append("获取输出流失败，强行终止\n");
            System.exit(1);
        }
        JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(bout);    //JPEG编码输出流对象
        Image imageMouse = toolkit.getImage("mouseimage.png");
        
        while (startSendImageThread.socket != null) {                                                  //不断输出图像
            try {
                Thread.sleep(sleepTime);
                int x = (int) MouseInfo.getPointerInfo().getLocation().getX();    //获得鼠标当前位置
                int y = (int) MouseInfo.getPointerInfo().getLocation().getY();
                screenImage = robot.createScreenCapture(rectangle);              //抓取图像
                Graphics g = screenImage.getGraphics();                          //定义图像对象
                g.drawImage(imageMouse, x, y, null);                            //根据鼠标位置画图用于显示鼠标
                if (image == null) {
                    image = screenImage;                                         //比较图片，不同则发送
                    } else {
                    image = screenImage;
                }
                encoder.encode(image);             //编码发送
            } catch (Exception e) {
                try {
                    if (startSendImageThread.socket != null && !startSendImageThread.socket.isClosed()) {
                        startSendImageThread.socket.close();
                    }
                    startSendImageThread.socket = null;
                    bout.close();
                    outputStream.close();
                    startSendImageThread.serverSocket.close();
                    startSendImageThread.ois.close();
                    startSendImageThread.os.close();
                    MainFrameThread.information.append(MainFrameThread.getTime());
                    MainFrameThread.information.append("客户端已关闭连接\n");
                    MainFrameThread.information.append("桌面信息发送线程已关闭，重启监听线程\n");
                    MainFrameThread.startsit();
                } catch (IOException ex) {
                    MainFrameThread.information.append("套接字或输出流关闭失败，强行终止\n");
                    System.exit(1);
                }
            }
        }
    }

    private boolean ImageEquals(BufferedImage image1, BufferedImage image2) {  //图片比较方法
        int w1 = image1.getWidth();
        int h1 = image1.getHeight();
        int w2 = image2.getWidth();
        int h2 = image2.getHeight();
        if (w1 != w2 || h1 != h2) {
            return false;
        }
        for (int i = 0; i < w1; i += 4) {
            for (int j = 0; j < h1; j += 4) {
                if (image1.getRGB(i, j) != image2.getRGB(i, j)) {
                    return false;
                }
            }
        }
        return true;
    }
}

package Service;


import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ControlInterface extends Remote {

    public void controlKeyPress(int keycode) throws RemoteException;    // 执行远程主机键盘指定按键的按下动作
	
    public void controlKeyRelease(int keycode) throws RemoteException;   // 执行远程主机键盘指定按键的抬起动作
	
    public void controlMouseMove(int x, int y) throws RemoteException;    // 执行远程主机的鼠标移动方法
	
    public void controlMousePress(int buttons) throws RemoteException;    // 执行远程主机的鼠标指定按键的按下动作
	
    public void controlMouseRelease(int buttons) throws RemoteException;  // 执行远程主机的鼠标指定按键的释放动作
	
    public void controlMouseWheel(int wheelAmt) throws RemoteException;   // 执行远程主机的鼠标滚轮动作

    public void sleepTime(int t) throws RemoteException;                   // 设定休眠时间，以毫秒为单位
}

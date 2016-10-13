package Service;

import java.awt.Robot;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
/**
 *
 * @author wealthypanda
 */
public class Control extends UnicastRemoteObject implements ControlInterface{

    private Robot robot;                                   //用于调用控制方法

    public Control(Robot rb) throws RemoteException{  //构造函数，传递Robot，用于控制鼠标键盘
        this.robot=rb;
    }
    
    public void controlKeyPress(int keycode) throws RemoteException {
        robot.keyPress(keycode);
    }

    public void controlKeyRelease(int keycode) throws RemoteException {
        robot.keyRelease(keycode);
    }

    public void controlMouseMove(int x, int y) throws RemoteException {
        robot.mouseMove(x, y);
    }

    public void controlMousePress(int buttons) throws RemoteException {
        robot.mousePress(buttons);
    }

    public void controlMouseRelease(int buttons) throws RemoteException {
        robot.mouseRelease(buttons);
    }

    public void controlMouseWheel(int wheelAmt) throws RemoteException {
        robot.mouseWheel(wheelAmt);
    }

    public void sleepTime(int t) throws RemoteException {
        SendImageThread.sleepTime = t;
    }

}

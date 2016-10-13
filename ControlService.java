package Service;

import java.awt.Robot;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

/**
 *
 * @author wealthypanda
 */
public class ControlService {

    private Robot robot=null;
    private String ipAddS;
    void runns(Robot rb,String ipAddS){
        this.robot=rb;
        this.ipAddS=ipAddS;
        try{
            LocateRegistry.createRegistry(1099);
            ControlInterface control=new Control(robot);
            Naming.rebind("//"+ipAddS+":1099/Control",control);
        }catch(Exception e){
            MainFrameThread.information.append("Ô¶³Ì¶ÔÏó×¢²áÊ§°Ü");
        }
    }
}

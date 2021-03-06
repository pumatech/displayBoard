package Jinput;
import java.util.ArrayList;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;

public class JinputTestRunner {

	public static void main(String[] args) {
		Controller[] ca = ControllerEnvironment.getDefaultEnvironment().getControllers();
		ArrayList<Controller> gamePads = new ArrayList<Controller>();
		Controller input = null;

		for (int i = 0; i < ca.length; i++) {
			if (ca[i].getType() == Controller.Type.GAMEPAD) {
				input = ca[i];
			}

		}
		System.out.println(input);
		Component[] components = input.getComponents();
		for(Component c: components) {
			System.out.println(c);
		}
		 while(true) {
	         Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
	         if(controllers.length==0) {
	            System.out.println("Found no controllers.");
	            System.exit(0);
	         }
	         
	         for(int i=0;i<controllers.length;i++) {
	            controllers[i].poll();
	            EventQueue queue = controllers[i].getEventQueue();
	            Event event = new Event();
	            while(queue.getNextEvent(event)) {
	                StringBuffer buffer = new StringBuffer(controllers[i].getName());
	                buffer.append(" at ");
	                buffer.append(event.getNanos()).append(", ");
	                Component comp = event.getComponent();
	                
	                
	                
	                if(comp.getName().equals("Button 0")) {
	                	System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
	                }
	                buffer.append(comp.getName()).append(" changed to ");
	                float value = event.getValue(); 
	                if(comp.isAnalog()) {
	                	System.out.println("X: " + comp);
	                   buffer.append(value);
	                   
	                } else {
	                   if(value==1.0f) {
	                      buffer.append("On");
	                   } else {
	                      buffer.append("Off");
	                   }
	                }
	                System.out.println(buffer.toString());
	             }
	          }
	          
	          try {
	             Thread.sleep(20);
	          } catch (InterruptedException e) {
	             // TODO Auto-generated catch block
	             e.printStackTrace();
	          }
	       }
	}

}

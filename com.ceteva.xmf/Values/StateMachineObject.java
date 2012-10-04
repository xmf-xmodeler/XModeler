package Values;

public class StateMachineObject extends Value {
	
	public void receiveMessage(Str message,Value args,StateMachineObject source) {
		send(message.toString(),0);
	}
	
	public void sendMessage(Str message,Value args,StateMachineObject target) {
		target.receiveMessage(message,args,this);
	}
	

}
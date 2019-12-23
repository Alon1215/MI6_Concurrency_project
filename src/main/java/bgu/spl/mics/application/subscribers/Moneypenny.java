package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.Future;
import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.passiveObjects.Squad;

import java.util.HashMap;
import java.util.Map;

/**
 * Only this type of Subscriber can access the squad.
 * Three are several Moneypenny-instances - each of them holds a unique serial number that will later be printed on the report.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Moneypenny extends Subscriber {
	private int serial;
	private Squad squad ;

	public Moneypenny(int serial) {
		super("Moneypenny");
		this.serial=serial;
		this.squad=Squad.getInstance();
	}

	@Override
	protected void initialize() {
		System.out.println("Moneypenny " + serial + " initialized"); //TODO: delete before submission
		subscribeOriginal();
	}

	/**
	 * subscribing all events & broadcasts
	 */
	private void subscribeOriginal() {
		subscribeTickBroadcast();
		subscribeAgentAvailableEvent();
		//subscribeSendAgentsEvent();
		//subscribeReleaseAgentsEvent();
	}



	/**
	 * next methods are private methods
	 * to make initialize() more readable
	 */

	private void subscribeTickBroadcast() {
		subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) -> {
			if (tick.isFinalTick()) {
				System.out.println("terminate Moneypenny " + serial + " executed" ); //TODO: delete before submission
				terminate();
			}
		});
	}


	private void subscribeAgentAvailableEvent() {
		subscribeEvent(AgentsAvailableEvent.class, (AgentsAvailableEvent e) -> {
			System.out.println("	Moneypenny " + serial + ": AgentAvailableEvent -> " + e.getAgents().toString() + ""); //TODO: delete before submission

			Map<String, Object> map = new HashMap<>();
			map.put("serial", serial);
			map.put("acquired", squad.getAgents(e.getAgents()) ? 1 : 0);
			map.put("names", squad.getAgentsNames(e.getAgents())); //return a list of names (of agents)

			Future<Boolean> isSend = new Future<>(); //TODO ALON: 23.12 NEW IMPLEMENTATION
			map.put("future", isSend);

			System.out.println("	Moneypenny " + serial + ": AgentAvailableEvent -> " + e.getAgents().toString() + " result: " + map.get("acquired")); //TODO: delete before submission
			complete(e, map); //finished checking AvailableAgentsEvent, continue to process whether send/ release:


			synchronized (isSend){
				while (!isSend.isDone()){
					try {
						isSend.wait();
					} catch (InterruptedException ex) {}
				}
			}
			boolean result =  isSend.get();

			if (result){
				System.out.println("	Moneypenny " + serial + ": SendAgents "+ e.getAgents().toString()  +" (and released automatically)"); //TODO: delete before submission
				squad.sendAgents(e.getAgents(),e.getDuration());
			} else {
				System.out.println("	Moneypenny " + serial + ": ReleaseAgents " + e.getAgents().toString()  +" (without sending to mission)"); //TODO: delete before submission
				squad.releaseAgents(e.getAgents());
			}


		});
	}

	// TODO: check if necessary (not suppose to, because we changed impl) :
//
//	private void subscribeAlternative() {
//		if (serial % 2 == 0) { //TODO: ALON 23.12 impl
//			subscribeAgentAvailableEvent();
//		} else {
//			subscribeSendAgentsEvent();
//
//			subscribeReleaseAgentsEvent();
//		}
//	}

	/**
	 * getter
	 * @return serial number
	 */

	public int getSerial() {
		return serial;
	}

	// TODO: check if necessary (not suppose to, because we changed impl) :
//
//	/**
//	 * next 2 methods are private methods
//	 * to make initialize() more readable
//	 */
//	private void subscribeReleaseAgentsEvent() {
//		subscribeEvent(ReleaseAgentsEvent.class, (ReleaseAgentsEvent e) -> {
//			System.out.println("Moneypenny " + serial + ": ReleaseAgentsEvent (without sending to mission)"); //TODO: delete before submission
//			squad.releaseAgents(e.getAgents());
//			complete(e, "ReleaseAgents -> executed");//TODO: ALON: 22.12 11:00
//		});
//	}
//
//	private void subscribeSendAgentsEvent() {
//		subscribeEvent(SendAgentsEvent.class, (SendAgentsEvent e) -> {
//			System.out.println("Moneypenny " + serial + ": SendAgentsEvent (and released automatically)"); //TODO: delete before submission
//			squad.sendAgents(e.getAgents(), e.getDuration());
//			complete(e, "SendAgents -> executed");//TODO: ALON: 22.12 11:00
//
//		});

}

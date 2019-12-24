package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.MessageBroker;
import bgu.spl.mics.MessageBrokerImpl;
import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.messages.GadgetAvailableEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.Inventory;

import java.util.HashMap;
import java.util.Map;

/**
 * Q is the only Subscriber\Publisher that has access to the {@link bgu.spl.mics.application.passiveObjects.Inventory}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Q extends Subscriber {
	int currentTick;
	Inventory inv;

	private static class QHolder {
		private static Q instance = new Q();
	}
	public static Q getInstance() {
		return Q.QHolder.instance;
	}


	public Q() {
		super("Q");
		 currentTick=0;
		 inv= Inventory.getInstance();
	}

	@Override
	protected void initialize() {
		System.out.println("Q initialized"); //TODO: delete before submission

		subscribeBroadcast(TickBroadcast.class, (TickBroadcast tick) -> {
			if (tick.isFinalTick()) {
				System.out.println("terminate Q executed"); //TODO: delete before submission
				System.out.println("Q's Current Tick: "+ tick.getTickNumber()); //TODO: delete before submission
				terminate();
				inv.printToFile("inventoryOutputFile.json");
			} else {
				currentTick = tick.getTickNumber();
				System.out.println("Q's Current Tick: "+ tick.getTickNumber()); //TODO: delete before submission
			}
		});
		subscribeEvent(GadgetAvailableEvent.class,(GadgetAvailableEvent e)->{
			System.out.println("Q received GadgetAvailableEvent, [ "+ e.getGadgetname() + "] , Current Tick: "+ currentTick); //TODO: delete before submission
			Map<String,Integer> map=new HashMap<>();
			map.put("timeTick", currentTick);
			map.put("acquired", inv.getItem(e.getGadgetname()) ? 1 : 0);
			complete(e,map);
			}
		);
	}
}

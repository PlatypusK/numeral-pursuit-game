package com.games.numeral.pursuit.messages;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.games.common.gsonmessages.ParentChatMessage;
import com.games.common.gsonmessages.ParentGameInviteMessage;
import com.games.common.gsonmessages.ParentGameUpdateMessage;
import com.games.common.gsonmessages.ParentNewGameMessage;


/**Contains a map of the conversions from serverclass to local class.
 * 
 * @author Ketil
 *
 */
public abstract class GsonToLocalConverter{
	private static final Map<String, String> CONVERSION_MAP;
	static{
		HashMap<String, String> aMap=new HashMap<String, String>();
		aMap.put(ParentNewGameMessage.class.getName(), ChildNewGameMessage.class.getName());
		aMap.put(ParentGameUpdateMessage.class.getName(), ChildGameUpdateMessage.class.getName());
		aMap.put(ParentChatMessage.class.getName(), ChildChatMessage.class.getName());
		aMap.put(ParentGameInviteMessage.class.getName(), ChildGameInviteMessage.class.getName());
		aMap.put(GameUpdateWrapper.class.getName(), GameUpdateWrapper.class.getName());
		CONVERSION_MAP=Collections.unmodifiableMap(aMap);
	}
	/**
	 * 
	 * @return A list of the subclasses of GsonMessage that GsonMesAdapter is allowed to instantiate on the android device
	 */
	public static Map<String, String> getConversionMap(){
		return CONVERSION_MAP;
	}

}

package com.elikill58.negativity.universal;

import com.elikill58.negativity.universal.adapter.Adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemUseBypass {

	public static final HashMap<Object, ItemUseBypass> ITEM_BYPASS = new HashMap<>();

	private Object item;
	private List<AbstractCheat> cheats = new ArrayList<>();
	private WhenBypass when;

	public ItemUseBypass(String itemName, String cheats, String when) {
		this.item = Adapter.getAdapter().getItem(itemName);
		this.when = WhenBypass.getWhenBypass(when);
		this.cheats = updateCheats(cheats);
		if(this.item == null)
			Adapter.getAdapter().error("[Config - Error] Item bypass System - Unknow item : " + itemName);
		else if(this.when == WhenBypass.UNKNOW)
			Adapter.getAdapter().error("[Config - Error] Item bypass System - Unknow when : " + when);
		else if(this.cheats.size() == 0)
			Adapter.getAdapter().error("[Config - Error] Item bypass System - Unknow cheats : " + cheats);
		else
			ITEM_BYPASS.put(item, this);
	}

	private List<AbstractCheat> updateCheats(String cheats){
		List<AbstractCheat> list = new ArrayList<>();
		for(AbstractCheat ac : Adapter.getAdapter().getAbstractCheats())
			for(String s : cheats.split(","))
				if(ac.name().equalsIgnoreCase(s))
					list.add(ac);
		return list;
	}

	public List<AbstractCheat> getCheats(){
		return cheats;
	}

	public boolean isForThisCheat(AbstractCheat c) {
		return cheats.contains(c);
	}

	public Object getItem() {
		return item;
	}

	public WhenBypass getWhen() {
		return when;
	}

	public enum WhenBypass {
		ALWAYS, RIGHT_CLICK(true), LEFT_CLICK(true), UNKNOW;

		private boolean isClick = false;

		WhenBypass() {
		}

		WhenBypass(boolean isClick) {
			this.isClick = isClick;
		}

		public static WhenBypass getWhenBypass(String when) {
			for(WhenBypass wb : WhenBypass.values())
				if(wb.name().equalsIgnoreCase(when))
					return wb;
			return UNKNOW;
		}

		public boolean isClick() {
			return isClick;
		}
	}
}

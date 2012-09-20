package dxdy.XPPromote;

import org.bukkit.entity.Player;

public class ExpRefreshTask implements Runnable{
	public ExpRefreshTask(Player p) {
		mP = p;
	}
	
	@Override
	public void run() {
		float ur = mP.getExp();
		mP.setExp(30);
		mP.setExp(ur);
		int xx = mP.getLevel();
		mP.setLevel(30);
		mP.setLevel(xx);
	}
	
	Player mP;

}

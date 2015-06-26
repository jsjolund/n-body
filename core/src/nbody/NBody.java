package nbody;

import com.badlogic.gdx.Game;

public class NBody extends Game {

	@Override
	public void create() {
		this.setScreen(new SimScreen(this));
	}
	
}

package nbody;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class Body {

	public Vector3 pos = new Vector3();
	public Vector3 vel = new Vector3();
	public float mass = 0;

	public float scl = 1;
	private Model model;
	private ModelInstance instance;

	public BoundingBox bb = new BoundingBox();
	public Vector3 center = new Vector3();
	public Vector3 dimensions = new Vector3();

	private Vector3 acc = new Vector3();
	private Vector3 tmp = new Vector3();

	public Vector3 forceFrom(Body other) {
		float G = 6.674E-11f; // N*m^2/kg^2
		float m1 = this.mass;
		float m2 = other.mass;
		Vector3 dr = tmp.set(other.pos).sub(this.pos);
		float l = dr.len();
		return dr.scl(G * m1 * m2 / (l * l * l));
	}

	public void applyForce(Vector3 f, float dt) {
		acc.set(f).scl(1 / mass);
		vel.add(tmp.set(acc).scl(dt));
		pos.add(tmp.set(vel).scl(dt));

		instance.transform.setTranslation(pos);
		instance.calculateTransforms();
	}

	public ModelInstance getModelInstance() {
		return instance;
	}

	public void setModel(Model model) {
		this.model = model;
		instance = new ModelInstance(model, pos);
		instance.transform.scale(scl, scl, scl);
		instance.calculateBoundingBox(bb);
		bb.getCenter(center);
		bb.getDimensions(dimensions);
	}

	public Model getModel() {
		return model;
	}

}

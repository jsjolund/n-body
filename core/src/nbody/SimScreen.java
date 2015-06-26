package nbody;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;

public class SimScreen implements Screen {

	private class MyInputProcessor extends CameraInputController
			implements
				InputProcessor {

		Camera cam;

		public MyInputProcessor(Camera camera) {
			super(camera);
			this.cam = camera;
			autoUpdate = false;
		}

		@Override
		public boolean touchDown(float x, float y, int pointer, int button) {
			cam.update();
			if (button != Input.Buttons.LEFT) {
				return super.touchDown(x, y, pointer, button);
			}

			Ray ray = cam.getPickRay(x, y);
			BoundingBox box = new BoundingBox();
			for (Body body : bodies) {
				ModelInstance instance = body.getModelInstance();
				instance.calculateBoundingBox(box).mul(instance.transform);
				if (Intersector.intersectRayBoundsFast(ray, box)) {
					camFocus = body.getPos();
				}
			}
			target.set(camFocus);
			return true;

		}

	}

	private Environment environment;

	private PerspectiveCamera camera;
	private MyInputProcessor camController;

	private ModelBatch modelBatch;
	private AssetManager assets;

	private ShapeRenderer shapeRenderer;

	private SpriteBatch batch = new SpriteBatch();
	private Texture bkgTex;
	private ShaderProgram shaderSun;
	
	private Body sun;
	private ModelInstance skyboxInstance;

	private int maxNumBodies = 10;

	private Array<ModelInstance> instancesEnv = new Array<ModelInstance>(maxNumBodies);
	private Array<ModelInstance> instances = new Array<ModelInstance>(maxNumBodies);

	private Array<Body> bodies = new Array<Body>(maxNumBodies);
	private Array<Array<Vector3>> bodiesPosHist = new Array<Array<Vector3>>(
			maxNumBodies);

	private Vector3 camFocus;

	public SimScreen(Game game) {
		environment = new Environment();

		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.3f,
				0.3f, 0.3f, 1f));
		environment.add(new PointLight().set(Color.WHITE, 0, 0, 0, 1000f));

		modelBatch = new ModelBatch();

		camera = new PerspectiveCamera(30, Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());
		float camd = 20f;
		camera.position.set(camd, camd, camd);
		camera.lookAt(0, 0, 0);
		camera.near = 1E-2f;
		camera.far = 1.5E3f;
		camera.update();

		shapeRenderer = new ShapeRenderer();

		assets = new AssetManager();
		assets.load("./skybox.g3db", Model.class);
		assets.load("./planet_sun.g3db", Model.class);
		assets.load("./planet_ako.g3db", Model.class);
		assets.load("./planet_dante.g3db", Model.class);
		assets.load("./planet_down.g3db", Model.class);
		assets.load("./planet_dust.g3db", Model.class);
		assets.load("./planet_reststop.g3db", Model.class);
		assets.finishLoading();

		skyboxInstance = new ModelInstance(assets.get("./skybox.g3db",
				Model.class), new Vector3(0, 0, 0));
		skyboxInstance.transform.scale(500, 500, 500);

		Body body;

		// //////////////////////////////////////////////////////////////////

		body = new Body();
		body.pos = new Vector3(0, 0, 0);
		body.vel = new Vector3(0, 0, 0);
		body.mass = 2E11f;
		body.scl = 1;
		body.setModel(assets.get("./planet_sun.g3db", Model.class));

		sun = body;
		bodies.add(body);
		instances.add(body.getModelInstance());

		// //////////////////////////////////////////////////////////////////

		body = new Body();
		body.pos = new Vector3(-10, 10, 4);
		body.vel = new Vector3(-0.3f, 0, -0.2f);
		body.mass = 5E8f;
		body.scl = 0.2f;
		body.setModel(assets.get("./planet_ako.g3db", Model.class));

		bodies.add(body);
		instancesEnv.add(body.getModelInstance());

		// //////////////////////////////////////////////////////////////////

		body = new Body();
		body.pos = new Vector3(10, 7, 1);
		body.vel = new Vector3(-0.3f, 0, -0.5f);
		body.mass = 1E9f;
		body.scl = 0.2f;
		body.setModel(assets.get("./planet_dante.g3db", Model.class));

		bodies.add(body);
		instancesEnv.add(body.getModelInstance());

		// //////////////////////////////////////////////////////////////////

		body = new Body();
		body.pos = new Vector3(10.4f, 7, 1);
		body.vel = new Vector3(-0.3f, 0, -0.7f);
		body.mass = 1E7f;
		body.scl = 0.04f;
		body.setModel(assets.get("./planet_down.g3db", Model.class));

		bodies.add(body);
		instancesEnv.add(body.getModelInstance());

		// //////////////////////////////////////////////////////////////////

		body = new Body();
		body.pos = new Vector3(20, -7, 0.5f);
		body.vel = new Vector3(-0.1f, 0, -0.4f);
		body.mass = 1E8f;
		body.scl = 0.5f;
		body.setModel(assets.get("./planet_dust.g3db", Model.class));

		bodies.add(body);
		instancesEnv.add(body.getModelInstance());

		// //////////////////////////////////////////////////////////////////

		body = new Body();
		body.pos = new Vector3(4, 1, 1);
		body.vel = new Vector3(-0.1f, 0, -0.7f);
		body.mass = 1E7f;
		body.scl = 0.1f;
		body.setModel(assets.get("./planet_reststop.g3db", Model.class));

		bodies.add(body);
		instancesEnv.add(body.getModelInstance());

		// //////////////////////////////////////////////////////////////////

		for (Body b : bodies) {
			// Body position history
			Array<Vector3> bodyPosHist = new Array<Vector3>();
			bodyPosHist.add(new Vector3(b.getPos()));
			bodiesPosHist.add(bodyPosHist);
		}

		camController = new MyInputProcessor(camera);
		Gdx.input.setInputProcessor(camController);
		camFocus = bodies.get(0).getPos();

		String vert = Gdx.files.local("shaders/sun.vert").readString();
		String frag = Gdx.files.local("shaders/sun.frag").readString();
		shaderSun = new ShaderProgram(vert, frag);
		ShaderProgram.pedantic = false;
		if (!shaderSun.isCompiled()) {
			Gdx.app.debug("Shader", shaderSun.getLog());
			Gdx.app.exit();
		}
		if (shaderSun.getLog().length() != 0) {
			Gdx.app.debug("Shader", shaderSun.getLog());
		}
		bkgTex = new Texture(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),
				Format.RGBA8888);
		batch = new SpriteBatch();

	}

	@Override
	public void dispose() {
		modelBatch.dispose();
		for (Body body : bodies) {
			body.getModel().dispose();
		}
		shapeRenderer.dispose();
		shaderSun.dispose();
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
	}

	private void updateBodies(float dt) {
		for (int i = 1; i < bodies.size; i++) {
			Body a = bodies.get(i);
			for (int j = 0; j < bodies.size; j++) {
				if (i == j) {
					continue;
				}
				Body b = bodies.get(j);
				a.applyForce(a.forceFrom(b), dt);
			}
			Array<Vector3> aHist = bodiesPosHist.get(i);
			aHist.add(new Vector3(a.getPos()));

			if (aHist.size > 2000) {
				aHist.removeIndex(0);
			}
		}
	}

	@Override
	public void render(float dt) {
		camController.update();

		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		renderSkybox();
		renderSun();
		renderTrail();
		renderBodies();
		renderBodiesEnv();

		Vector3 oldFocus = camFocus.cpy();
		updateBodies(dt);
		Vector3 newFocus = camFocus.cpy();
		camera.position.add(newFocus.sub(oldFocus));
		camera.lookAt(camFocus);
		camera.up.set(Vector3.Y);
		camera.update();

	}

	private void renderBodies() {
		modelBatch.begin(camera);
		modelBatch.render(instances);
		modelBatch.end();
	}

	private void renderBodiesEnv() {
		modelBatch.begin(camera);
		modelBatch.render(instancesEnv, environment);
		modelBatch.end();
	}

	private void renderSkybox() {
		modelBatch.begin(camera);
		modelBatch.render(skyboxInstance);
		modelBatch.end();
	}

	private void renderSun() {
		batch.begin();
		shaderSun.begin();

		Vector3 w_pos_sun = sun.getPos();
		Vector3 s_pos_sun = camera.project(w_pos_sun.cpy());
		s_pos_sun.y = Gdx.graphics.getHeight() - s_pos_sun.y;
		shaderSun.setUniformf("pos_sun", s_pos_sun);

		BoundingBox sun_bb = new BoundingBox();
		sun.getModelInstance().model.calculateBoundingBox(sun_bb);
		float w_radius_sun = sun_bb.getHeight() / 2;

		Vector3 w_pos_sun_ort_bound = Vector3.Y.cpy().crs(camera.position)
				.nor().scl(w_radius_sun);
		Vector3 s_pos_sun_ort_bound = camera.project(w_pos_sun_ort_bound.cpy());
		s_pos_sun_ort_bound.y = Gdx.graphics.getHeight()
				- s_pos_sun_ort_bound.y;

		shaderSun.setUniformf("radius_sun", s_pos_sun_ort_bound.dst(s_pos_sun));
		shaderSun.setUniformf("time", Gdx.graphics.getDeltaTime());
		shaderSun.setUniformf("resolution", Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());

		shaderSun.end();

		batch.setShader(shaderSun);
		batch.draw(bkgTex, 0, 0);
		batch.end();

		batch.begin();
		batch.setShader(null);
		batch.end();
	}

	private void renderTrail() {
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.begin(ShapeType.Line);

		for (int i = 0; i < bodies.size; i++) {

			Color c = Color.GRAY;

			Array<Vector3> hist = bodiesPosHist.get(i);
			Vector3 a = hist.get(0);

			for (int j = 1; j < hist.size; j++) {

				c.a = 0.7f * j / hist.size;
				shapeRenderer.setColor(c);

				Vector3 b = hist.get(j);

				shapeRenderer.line(a, b);
				a = b;
			}
		}
		shapeRenderer.end();
		Gdx.gl.glDisable(GL20.GL_BLEND);
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
	}

}

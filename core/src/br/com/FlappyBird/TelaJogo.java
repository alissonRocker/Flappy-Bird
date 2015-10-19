package br.com.FlappyBird;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class TelaJogo extends TelaBase {

    private OrthographicCamera camera; // camera do jogo.
    private World mundo; // representa o mundo do Box2D.
    private Body chao; // corpo do chão.
    private Passaro passaro; // corpo do passaro;

    private Box2DDebugRenderer debug; // desenha o mundo na tela para ajudar no desenvolvimento.

    public TelaJogo(MainGame game) { super(game); }

    @Override
    public void show() {
        camera = new OrthographicCamera(Gdx.graphics.getWidth() / Util.ESCALA, Gdx.graphics.getHeight() / Util.ESCALA);
        debug = new Box2DDebugRenderer();
        mundo = new World(new Vector2(0, -9.8f), false);

        initChao();
        initPassaro();
    }

    private void initChao() { chao = Util.criarCorpo(mundo, BodyDef.BodyType.StaticBody, 0, 0); }

    private void initPassaro() { passaro = new Passaro(mundo, camera, null); }

    /*
    BodyDef def = new BodyDef(); // Objeto de definição do corpo.
        def.type = BodyDef.BodyType.DynamicBody;
        float y = (Gdx.graphics.getHeight() / ESCALA / 2) / PIXEL_METRO + 10;
        float x = (Gdx.graphics.getWidth() / ESCALA / 2) / PIXEL_METRO + 2;
        def.position.set(x ,y);
        def.fixedRotation = true;

        Body corpo = mundo.createBody(def);  // criação do corpo.
        CircleShape shape = new CircleShape(); // forma do corpo.
        shape.setRadius(20 / PIXEL_METRO); // raio de 20 metros dividido pelo pixel_metro.

        Fixture fixacao = corpo.createFixture(shape, 1); // objeto de apresentação do corpo.

        shape.dispose();
     */

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(.25f, .25f, .25f, 1); // limpa a tela e pinta a cor de fundo.
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // mantem o buffer de cores.

        capturaTeclas();
        atualizar(delta);
        renderizar(delta);

        debug.render(mundo, camera.combined.cpy().scl(Util.PIXEL_METRO));
    }

    private boolean pulando = false;

    private void capturaTeclas() {
        pulando = false;
        if(Gdx.input.justTouched()) {
            pulando = true;
        }
    }

    /**
     * Arualizar/calculo dos corpos.
     *
     * @param delta
     */
    private void atualizar(float delta) {
        passaro.atualizar(delta);
        mundo.step(1f / 60f, 6, 2); // Um passo dentro do mundo.
        atualizarCamera();
        atualizarChao();
        if(pulando) {
            passaro.pular();
        }
    }

    private void atualizarCamera() {
        camera.position.x = (passaro.getCorpo().getPosition().x + 34 / Util.PIXEL_METRO) * Util.PIXEL_METRO;
        camera.update();
    }

    /**
     * Atualiza a posição do chao para acompanhar o pássaro.
     */
    private void atualizarChao() {
        float largura = camera.viewportWidth / Util.PIXEL_METRO;
        Vector2 posicao = chao.getPosition();
        posicao.x = largura / 2;
        chao.setTransform(posicao, 0);
    }

    /**
     * Renderizar/desenhar as imagens.
     *
     * @param delta
     */
    private void renderizar(float delta) {

    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width / Util.ESCALA, height / Util.ESCALA);
        camera.update();

        redimensionaChao();
    }

    /**
     * Configura o tamanho do chão de acordo com a tela.
     */
    private void redimensionaChao() {
        chao.getFixtureList().clear(); // limpa todas as formas antigas.
        float largura = camera.viewportWidth  / Util.PIXEL_METRO;
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(largura / 2, Util.ALTURA_CHAO / 2);

        Fixture forma = Util.criarForma(chao, shape, "CHAO");

        shape.dispose();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void dispose() {
        debug.dispose();
        mundo.dispose();
    }

}

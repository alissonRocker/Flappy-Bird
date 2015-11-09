package br.com.FlappyBird;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FillViewport;

public class TelaJogo extends TelaBase {

    private OrthographicCamera camera; // camera do jogo.
    private World mundo; // representa o mundo do Box2D.
    private Body chao; // corpo do chão.
    private Passaro passaro; // corpo do passaro;
    private Array<Obstaculo> obstaculos = new Array<Obstaculo>();

    private int pontuacao = 0;
    private BitmapFont fontePontuacao;
    private Stage palcoInformacoes;
    private OrthographicCamera cameraInfo;
    private Label lbPontuação;
    private ImageButton btnPlay;
    private ImageButton btnGameOver;

    private boolean gameOver = false;

    private Box2DDebugRenderer debug; // desenha o mundo na tela para ajudar no desenvolvimento.

    public TelaJogo(MainGame game) {
        super(game);
    }

    @Override
    public void show() {
        camera = new OrthographicCamera(Gdx.graphics.getWidth() / Util.ESCALA, Gdx.graphics.getHeight() / Util.ESCALA);
        cameraInfo = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        debug = new Box2DDebugRenderer();
        mundo = new World(new Vector2(0, -9.8f), false);
        mundo.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) { // inicio
                detectarColicao(contact.getFixtureA(), contact.getFixtureB());
            }

            @Override
            public void endContact(Contact contact) { // apos

            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {

            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {

            }
        });

        initChao();
        initPassaro();
        initFontes();
        initInformacoes();
    }

    /**
     * Verifica se o passaro está envolvido na colisão.
     * @param fixtureA
     * @param fixtureB
     */
    private void detectarColicao(Fixture fixtureA, Fixture fixtureB) {
        if("PASSARO".equals(fixtureA.getUserData()) || "PASSARO".equals(fixtureB.getUserData())) {
            // game over.
            gameOver = true;
        }
    }

    private void initFontes() {
        FreeTypeFontGenerator.FreeTypeFontParameter fonteParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
        fonteParam.size = 24;
        fonteParam.color = Color.WHITE;
        fonteParam.shadowColor = Color.BLACK;
        fonteParam.shadowOffsetX = 4;
        fonteParam.shadowOffsetY = 4;

        FreeTypeFontGenerator gerador = new FreeTypeFontGenerator(Gdx.files.internal("fonts/roboto.ttf"));

        fontePontuacao = gerador.generateFont(fonteParam);

        gerador.dispose();
    }

    private void initInformacoes() {
        palcoInformacoes = new Stage(new FillViewport(cameraInfo.viewportHeight, cameraInfo.viewportHeight, cameraInfo));
        Gdx.input.setInputProcessor(palcoInformacoes); // Palco vira processador de entradas. Click,  Toque ...

        Label.LabelStyle estilo = new Label.LabelStyle();
        estilo.font = fontePontuacao;

        lbPontuação = new Label("0", estilo);
        palcoInformacoes.addActor(lbPontuação);
    }

    private void initChao() {
        chao = Util.criarCorpo(mundo, BodyDef.BodyType.StaticBody, 0, 0);
    }

    private void initPassaro() {
        passaro = new Passaro(mundo, camera, null);
    }

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
        palcoInformacoes.act(delta);
        passaro.atualizar(delta);
        mundo.step(1f / 60f, 6, 2); // Um passo dentro do mundo.

        atulizarInformacoes();
        atualizarObstaculos();
        atualizarCamera();
        atualizarChao();
        if(pulando) {
            passaro.pular();
        }
    }

    private void atulizarInformacoes() {
        lbPontuação.setText(pontuacao + "");
        lbPontuação.setPosition(cameraInfo.viewportWidth / 2 - lbPontuação.getPrefWidth() / 2,
                                cameraInfo.viewportHeight - lbPontuação.getPrefHeight());
    }

    private void atualizarObstaculos() {
        // Enquanto a lista tiver menos de 4 elementos... cria obstaculos.
        while(obstaculos.size < 4) {
            Obstaculo ultimo = null;
            if(obstaculos.size > 0) {
                ultimo = obstaculos.peek(); // retorna o ultimo elemento.
            }
            Obstaculo o = new Obstaculo(mundo, camera, ultimo);
            obstaculos.add(o);
        }

        // Verifica se os obstaculos sairam da tela para removê-los.
        for(Obstaculo o : obstaculos) {
            float inicioCamera = passaro.getCorpo().getPosition().x - (camera.viewportWidth / 2 / Util.PIXEL_METRO) - o.getLargura();
            // verifica se o obstaculo saiu da tela.
            if(inicioCamera > o.getPosX()) {
                o.remover();
                obstaculos.removeValue(o, true);
            } else if(!o.isPassou() && o.getPosX() < passaro.getCorpo().getPosition().x) { // Verifica senao marcou como passou e ultrapassou o passaro.
                o.setPassou(true);
                // Calcular pontuação.
                pontuacao++;
                // Reproduzir o som.
            }

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
        Vector2 posicao = passaro.getCorpo().getPosition(); // Pega posição do passaro.
        chao.setTransform(posicao.x, 0, 0); // Faz o chão acompanhar o passaro.
    }

    /**
     * Renderizar/desenhar as imagens.
     *
     * @param delta
     */
    private void renderizar(float delta) {
        palcoInformacoes.draw();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width / Util.ESCALA, height / Util.ESCALA);
        camera.update();

        redimensionaChao();
        cameraInfo.setToOrtho(false, width, height);
        cameraInfo.update();
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
        palcoInformacoes.dispose();
        fontePontuacao.dispose();
    }

}

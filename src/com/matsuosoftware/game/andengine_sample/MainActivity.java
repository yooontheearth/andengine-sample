package com.matsuosoftware.game.andengine_sample;

import android.graphics.Typeface;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.*;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.TextUtils;
import org.andengine.util.color.Color;
import org.andengine.util.modifier.IModifier;
import org.andengine.util.modifier.ease.EaseExponentialOut;
import org.andengine.util.modifier.ease.EaseQuadIn;

public class MainActivity extends SimpleBaseGameActivity {
    public static final int CAMERA_WIDTH = 480;
    public static final int CAMERA_HEIGHT = 320;
    public static final int CENTERX = CAMERA_WIDTH / 2;
    public static final int CENTERY = CAMERA_HEIGHT / 2;
    private Camera _camera;
    private Scene _scene;
    private BitmapTextureAtlas _backgroundAtlas;
    private BitmapTextureAtlas _charactersAtlas;
    private TextureRegion _backgroundRegion;
    private TiledTextureRegion _shipRegion;
    private TiledTextureRegion _squareRegion;
    private Font _font;

    @Override
    public EngineOptions onCreateEngineOptions() {
        _camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
        return new EngineOptions(true, ScreenOrientation.LANDSCAPE_SENSOR, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), _camera);
    }

    @Override
    protected void onCreateResources() {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

        // 画像などのResourceを読み込む
        _backgroundAtlas = new BitmapTextureAtlas(this.getTextureManager(), 512, 512);
        _backgroundRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(_backgroundAtlas, this, "Background02.png", 0, 0);
        _backgroundAtlas.load();

        _charactersAtlas = new BitmapTextureAtlas(this.getTextureManager(), 1024, 256);
        _shipRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(_charactersAtlas, this, "BlueShip.png", 0, 0, 6, 1);
        _squareRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(_charactersAtlas, this, "WalkingSquare.png", 0, 100, 5, 1);
        _charactersAtlas.load();

        // Fontの最適値は不明
        _font = FontFactory.create(this.getFontManager(), this.getTextureManager(), 256, 256, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 24, Color.WHITE_ARGB_PACKED_INT);
        _font.load();
    }

    @Override
    protected Scene onCreateScene() {
        _scene = new Scene();
        _scene.setTouchAreaBindingOnActionDownEnabled(true);

        // _sceneに画像をぺたぺた貼り付けていく
        Sprite bg = new Sprite(0, 0, _backgroundRegion, this.getVertexBufferObjectManager());
        bg.setScaleCenter(0, 0);
        bg.setScale(CAMERA_WIDTH/400f, CAMERA_HEIGHT/300f);
        _scene.attachChild(bg);

        Text text = new Text(100, 10, _font, "Click on the characters!", this.getVertexBufferObjectManager());
        text.setColor(new Color(0, 0, 0));  // 色ごとにFontを用意するのは難儀なので、WhiteでFontを読み込み、後から色を設定する
        _scene.attachChild(text);

        final ButtonSprite ship = new ButtonSprite(50, 50, _shipRegion.getTextureRegion(0), this.getVertexBufferObjectManager());
        final AnimatedSprite shipAnimation = new AnimatedSprite(10, 10, _shipRegion, this.getVertexBufferObjectManager());
        shipAnimation.animate(50);
        shipAnimation.setPosition(50, 50);  // コンストラクタで指定しているけれど、このコードがないとRotationの挙動がおかしい
        ship.setOnClickListener( new ButtonSprite.OnClickListener(){
            @Override
            public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                _scene.detachChild(pButtonSprite);
                shipAnimation.setRotation(0);
                SequenceEntityModifier shipModifier =
                    new SequenceEntityModifier(
                        new IEntityModifier.IEntityModifierListener() {
                            @Override
                            public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {
                            }
                            @Override
                            public void onModifierFinished(final IModifier<IEntity> pEntityModifier, final IEntity pEntity) {
                                MainActivity.this.mEngine.runOnUpdateThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        _scene.attachChild(ship);
                                        _scene.detachChild(shipAnimation);
                                        pEntity.unregisterEntityModifier((IEntityModifier)pEntityModifier);
                                    }
                                });
                            }
                        },
                        new RotationByModifier(0.5f, 90)
                        ,new MoveModifier(1.0f, 50, CAMERA_WIDTH+50, 50, 50, EaseExponentialOut.getInstance())
                        ,new RotationByModifier(0.1f, 180)
                        ,new MoveModifier(1.0f, CAMERA_WIDTH+50, 50, 50, 50, EaseExponentialOut.getInstance())
                        ,new RotationByModifier(0.5f, 90)
                    );
                shipAnimation.registerEntityModifier(shipModifier);
                _scene.attachChild(shipAnimation);
            }
        });
        _scene.registerTouchArea(ship);
        _scene.attachChild(ship);

        final ButtonSprite sq = new ButtonSprite(50, 180, _squareRegion.getTextureRegion(0), this.getVertexBufferObjectManager());
        final AnimatedSprite sqAnimation = new AnimatedSprite(50, 180, _squareRegion, this.getVertexBufferObjectManager());
        sqAnimation.animate(50);
        sqAnimation.setPosition(50, 180);
        sq.setOnClickListener(new ButtonSprite.OnClickListener(){
            @Override
            public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                _scene.detachChild(pButtonSprite);
                sqAnimation.setFlippedHorizontal(false);    // 元にもどす
                final SequenceEntityModifier sqSecondModifier =
                    new SequenceEntityModifier(
                        new IEntityModifier.IEntityModifierListener() {
                            @Override
                            public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {
                                sqAnimation.setFlippedHorizontal(true); // ひっくり返す
                            }
                            @Override
                            public void onModifierFinished(final IModifier<IEntity> pEntityModifier, final IEntity pEntity) {
                                MainActivity.this.mEngine.runOnUpdateThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        _scene.attachChild(sq);
                                        _scene.detachChild(sqAnimation);
                                        pEntity.unregisterEntityModifier((IEntityModifier)pEntityModifier);
                                    }
                                });
                            }
                        },
                        new MoveModifier(2.5f, CAMERA_WIDTH+50, 50, 180, 180, EaseQuadIn.getInstance())
                    );
                SequenceEntityModifier sqFirstModifier =
                    new SequenceEntityModifier(
                        new IEntityModifier.IEntityModifierListener() {
                            @Override
                            public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {
                            }
                            @Override
                            public void onModifierFinished(final IModifier<IEntity> pEntityModifier, final IEntity pEntity) {
                                MainActivity.this.mEngine.runOnUpdateThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        sqAnimation.registerEntityModifier(sqSecondModifier);
                                        pEntity.unregisterEntityModifier((IEntityModifier)pEntityModifier);
                                    }
                                });
                            }
                        },
                        new MoveModifier(2.5f, 50, CAMERA_WIDTH+50, 180, 180, EaseQuadIn.getInstance())
                    );
                sqAnimation.registerEntityModifier(sqFirstModifier);
                _scene.attachChild(sqAnimation);
            }
        });
        _scene.registerTouchArea(sq);
        _scene.attachChild(sq);

        //
        // this.mEngine.registerUpdateHandler(IUpdateHandler)に登録するオブジェクトのonUpdateがいわゆるゲームのメインループになる
        //

        return _scene;
    }
}

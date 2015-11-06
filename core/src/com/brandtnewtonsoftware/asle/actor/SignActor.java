package com.brandtnewtonsoftware.asle.actor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;

import com.brandtnewtonsoftware.asle.ASLEGame;
import com.brandtnewtonsoftware.asle.knn.LiteHand;
import com.brandtnewtonsoftware.asle.leap.PrimaryHandListener;
import com.brandtnewtonsoftware.asle.sign.Sign;
import com.brandtnewtonsoftware.asle.util.Attempt;
import com.brandtnewtonsoftware.asle.util.Database;
import com.leapmotion.leap.Hand;

import java.sql.SQLException;
import java.util.Random;

/**
 * Created by Brandt on 11/1/2015.
 */
public class SignActor extends Actor implements PrimaryHandListener {

    private Texture texture;
    private Sign sign;
    private SignRegisteredListener listener;
    private BitmapFont font;
    private Attempt attempt;

    public SignActor() {
        changeSign();
    }

    public void changeSign() {
        this.changeSign(new Random().nextInt(10));
    }
    public void changeSign(int signValue) {
        try {
            sign = (Sign) Class.forName("com.brandtnewtonsoftware.asle.sign.Sign" + signValue).getConstructor().newInstance();
        } catch (Exception e) {
            sign = null;
        }
        font = new BitmapFont();
        font.setColor(Color.BLACK);
        texture = new Texture(Gdx.files.internal(sign.getImageFileName()));
        setPosition(Gdx.graphics.getWidth() - texture.getWidth() - 15, Gdx.graphics.getHeight() - texture.getHeight() - 15);
        setBounds(getX(), getY(), texture.getWidth(), texture.getHeight());

        Database database = new Database();
        try {
            attempt = database.getAttempt(ASLEGame.getUser(), sign);

            if (attempt == null)
                attempt = new Attempt(sign, 0, 0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void draw(Batch batch, float alpha){
        if (!isVisible())
            return;

        Color c = this.getColor();
        batch.setColor(c);
        // always make sure to only multiply by the parent alpha
        batch.getColor().a *= alpha;
        batch.draw(texture, getX(), getY(), getOriginX(), getOriginY(), getWidth(),
                getHeight(), getScaleX(), getScaleY(), getRotation(), 0, 0,
                texture.getWidth(), texture.getHeight(), false, false);
        batch.setColor(Color.BLACK);

        font.draw(batch, "Attempt Count: " + attempt.getAttemptCount(), getX(), getY() - (texture.getHeight() + 20));
        font.draw(batch, "Success Count: " + attempt.getSuccessCount(), getX(), getY() - (texture.getHeight() + 40));
    }

    public void setListener(SignRegisteredListener listener) {
        this.listener = listener;
    }

    @Override
    public void onPrimaryHandUpdated(Hand hand) {
        LiteHand liteHand = ASLEGame.getHandCalibrator().evaluateHand(hand);
        listener.onSignRegistered(sign, sign.equals(liteHand));
    }
}

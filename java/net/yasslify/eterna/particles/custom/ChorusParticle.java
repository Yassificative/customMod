package net.yasslify.eterna.particles.custom;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ChorusParticle extends PortalParticle {
    protected ChorusParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
        this.quadSize *= 1.5F;
        this.lifetime = (int) (Math.random() * 20.0D) + 100;
        this.rCol = 1;
        this.gCol = 0.9f;
        this.bCol = 1;
    }

    public float getQuadSize(float pScaleFactor) {
        float f = 1.0F - ((float)this.age + pScaleFactor) / ((float)this.lifetime * 1.5F);
        return this.quadSize * f;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            float f = (float)this.age / (float)this.lifetime;
            this.x += this.xd * (double)f;
            this.y += this.yd * (double)f;
            this.z += this.zd * (double)f;
            this.setPos(this.x, this.y, this.z); // FORGE: update the particle's bounding box
            this.alpha = 1 - f;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet pSprites) {
            this.sprite = pSprites;
        }

        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            ChorusParticle chorusParticle = new ChorusParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
            chorusParticle.pickSprite(this.sprite);
            return chorusParticle;
        }
    }
}

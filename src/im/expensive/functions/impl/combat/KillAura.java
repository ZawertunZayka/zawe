package im.expensive.functions.impl.combat;

import com.google.common.eventbus.Subscribe;
import im.expensive.Expensive;
import im.expensive.command.friends.FriendStorage;
import im.expensive.events.EventInput;
import im.expensive.events.EventMotion;
import im.expensive.events.EventUpdate;
import im.expensive.functions.api.Category;
import im.expensive.functions.api.Function;
import im.expensive.functions.api.FunctionRegister;
import im.expensive.functions.settings.impl.BooleanSetting;
import im.expensive.functions.settings.impl.ModeListSetting;
import im.expensive.functions.settings.impl.ModeSetting;
import im.expensive.functions.settings.impl.SliderSetting;
import im.expensive.utils.math.SensUtils;
import im.expensive.utils.math.StopWatch;
import im.expensive.utils.player.InventoryUtil;
import im.expensive.utils.player.MouseUtil;
import im.expensive.utils.player.MoveUtils;
import lombok.Getter;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;  // Добавлен импорт Random

import static java.lang.Math.hypot;
import static net.minecraft.util.math.MathHelper.clamp;
import static net.minecraft.util.math.MathHelper.wrapDegrees;

@FunctionRegister(name = "KillAura", type = Category.Combat)
public class KillAura extends Function {
    @Getter
    private final ModeSetting type = new ModeSetting("Тип", "Плавная", "Плавная", "Резкая");
    private final SliderSetting attackRange = new SliderSetting("Дистанция аттаки", 3f, 3f, 6f, 0.1f);

    final ModeListSetting targets = new ModeListSetting("Таргеты",
            new BooleanSetting("Игроки", true),
            new BooleanSetting("Голые", true),
            new BooleanSetting("Мобы", false),
            new BooleanSetting("Животные", false),
            new BooleanSetting("Друзья", false),
            new BooleanSetting("Голые невидимки", true),
            new BooleanSetting("Невидимки", true));

    @Getter
    final ModeListSetting options = new ModeListSetting("Опции",
            new BooleanSetting("Только криты", true),
            new BooleanSetting("Ломать щит", true),
            new BooleanSetting("Отжимать щит", true),
            new BooleanSetting("Ускорять ротацию при атаке", false),
            new BooleanSetting("Синхронизировать атаку с ТПС", false),
            new BooleanSetting("Фокусировать одну цель", true),
            new BooleanSetting("Коррекция движения", true));

    // Новые настройки для "Бить через стены" и "Wall Bypass"
    final ModeListSetting moreOptions = new ModeListSetting("Триггеры",
            new BooleanSetting("Бить через стены", true),
            new BooleanSetting("Wall Bypass", false));

    final ModeSetting correctionType = new ModeSetting("Тип коррекции", "Незаметный", "Незаметный", "Сфокусированный");

    @Getter
    private final StopWatch stopWatch = new StopWatch();
    private Vector2f rotateVector = new Vector2f(0, 0);
    @Getter
    private LivingEntity target;
    private Entity selected;

    int ticks = 0;
    boolean isRotated;

    final AutoPotion autoPotion;

    public KillAura(AutoPotion autoPotion) {
        this.autoPotion = autoPotion;
        addSettings(type, attackRange, targets, options, correctionType, moreOptions);  // Добавляем moreOptions
    }

    @Subscribe
    public void onUpdate(EventUpdate e) {
        if (options.getValueByName("Фокусировать одну цель").get() && (target == null || !isValid(target)) || !options.getValueByName("Фокусировать одну цель").get()) {
            updateTarget();
        }

        if (target != null && !(autoPotion.isState() && autoPotion.isActive())) {
            isRotated = false;
            if (shouldPlayerFalling() && (stopWatch.hasTimeElapsed())) {
                updateAttack();
                ticks = 2;
            }
            if (type.is("Резкая")) {
                if (ticks > 0) {
                    updateRotation(true, 180, 90);
                    ticks--;
                } else {
                    reset();
                }
            } else {
                if (!isRotated) {
                    updateRotation(false, 80, 35);
                }
            }

        } else {
            stopWatch.setLastMS(0);
            reset();
        }
    }

    private void updateRotation(boolean attack, float rotationYawSpeed, float rotationPitchSpeed) {
        Vector3d vec = target.getPositionVec().add(0, clamp(mc.player.getPosYEye() - target.getPosY(),
                        0, target.getHeight() * (mc.player.getDistanceEyePos(target) / attackRange.get())), 0)
                .subtract(mc.player.getEyePosition(1.0F));

        isRotated = true;

        float yawToTarget = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90);
        float pitchToTarget = (float) (-Math.toDegrees(Math.atan2(vec.y, hypot(vec.x, vec.z))));

        float yawDelta = (wrapDegrees(yawToTarget - rotateVector.x));
        float pitchDelta = (wrapDegrees(pitchToTarget - rotateVector.y));

        float clampedYaw = Math.min(Math.max(Math.abs(yawDelta), 1.0f), rotationYawSpeed);
        float clampedPitch = Math.min(Math.max(Math.abs(pitchDelta), 1.0f), rotationPitchSpeed);

        if (attack && selected != target && options.getValueByName("Ускорять ротацию при атаке").get()) {
            clampedPitch = Math.max(Math.abs(pitchDelta), 1.0f);
        } else {
            clampedPitch /= 3f;
        }

        float yaw = rotateVector.x + (yawDelta > 0 ? clampedYaw : -clampedYaw);
        float pitch = clamp(rotateVector.y + (pitchDelta > 0 ? clampedPitch : -clampedPitch), -89.0F, 89.0F);

        float gcd = SensUtils.getGCDValue();
        yaw -= (yaw - rotateVector.x) % gcd;
        pitch -= (pitch - rotateVector.y) % gcd;

        rotateVector = new Vector2f(yaw, pitch);
    }

    // Реализация логики для "Бить через стены" и "Wall Bypass"
    private void updateAttack() {
        selected = MouseUtil.getMouseOver(target, rotateVector.x, rotateVector.y, attackRange.get());

        if ((mc.player.getDistanceEyePos(target)) > attackRange.get()) {
            return;
        }

        if (!moreOptions.getValueByName("Бить через стены").get()) {
            if (!mc.player.canEntityBeSeen(target)) {
                return;
            }
        } else {
            if (moreOptions.getValueByName("Wall Bypass").get() && !mc.player.canEntityBeSeen(target)) {
                Random random = new Random();
                float randomX = -0.15F + (0.15F - (-0.15F)) * random.nextFloat();
                float randomZ = -0.15F + (0.15F - (-0.15F)) * random.nextFloat();
                target.getPosition().add(randomX, target.getBoundingBox().getYSize(), randomZ);  // Используем Random вместо MathUtil
            }
        }

        stopWatch.setLastMS(500);
        mc.playerController.attackEntity(mc.player, target);
        mc.player.swingArm(Hand.MAIN_HAND);
    }

    @Subscribe
    private void onWalking(EventMotion e) {
        if (target == null || autoPotion.isState() && autoPotion.isActive()) return;

        float yaw = rotateVector.x;
        float pitch = rotateVector.y;

        e.setYaw(yaw);
        e.setPitch(pitch);
        mc.player.rotationYawHead = yaw;
        mc.player.renderYawOffset = yaw;
        mc.player.rotationPitchHead = pitch;
    }

    private void updateTarget() {
        List<LivingEntity> targets = new ArrayList<>();

        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof LivingEntity living && isValid(living)) {
                targets.add(living);
            }
        }

        if (targets.isEmpty()) {
            target = null;
            return;
        }

        if (targets.size() == 1) {
            target = targets.get(0);
            return;
        }

        targets.sort(Comparator.comparingDouble(object -> mc.player.getDistance((LivingEntity) object)));

        target = targets.get(0);
    }

    // Используем стандартные проверки на жидкости
    private boolean shouldPlayerFalling() {
        boolean cancelReason = mc.player.isInWater() || mc.player.isInLava() || mc.player.isOnLadder() || mc.player.isPassenger() || mc.player.abilities.isFlying;

        float attackStrength = mc.player.getCooledAttackStrength(options.getValueByName("Синхронизировать атаку с ТПС").get()
                ? Expensive.getInstance().getTpsCalc().getAdjustTicks() : 1.5f);

        if (attackStrength < 0.92f) {
            return false;
        }

        if (!cancelReason && options.getValueByName("Только криты").get()) {
            return !mc.player.isOnGround() && mc.player.fallDistance > 0;
        }

        return true;
    }

    private boolean isValid(LivingEntity entity) {
        if (entity instanceof ClientPlayerEntity) return false;

        if (entity.ticksExisted < 3) return false;
        if (mc.player.getDistanceEyePos(entity) > attackRange.get()) return false;

        if (entity instanceof PlayerEntity p) {
            if (AntiBot.isBot(entity)) {
                return false;
            }
            if (!targets.getValueByName("Друзья").get() && FriendStorage.isFriend(p.getName().getString())) {
                return false;
            }
            if (p.getName().getString().equalsIgnoreCase(mc.player.getName().getString())) return false;
        }

        if (entity instanceof PlayerEntity && !targets.getValueByName("Игроки").get()) {
            return false;
        }
        if (entity instanceof PlayerEntity && entity.getTotalArmorValue() == 0 && !targets.getValueByName("Голые").get()) {
            return false;
        }
        if (entity instanceof PlayerEntity && entity.isInvisible() && entity.getTotalArmorValue() == 0 && !targets.getValueByName("Голые невидимки").get()) {
            return false;
        }
        if (entity instanceof PlayerEntity && entity.isInvisible() && !targets.getValueByName("Невидимки").get()) {
            return false;
        }

        if (entity instanceof MonsterEntity && !targets.getValueByName("Мобы").get()) {
            return false;
        }
        if (entity instanceof AnimalEntity && !targets.getValueByName("Животные").get()) {
            return false;
        }

        return !entity.isInvulnerable() && entity.isAlive() && !(entity instanceof ArmorStandEntity);
    }

    private void reset() {
        if (options.getValueByName("Коррекция движения").get()) {
            mc.player.rotationYawOffset = Integer.MIN_VALUE;
        }
        rotateVector = new Vector2f(mc.player.rotationYaw, mc.player.rotationPitch);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        reset();
        target = null;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        reset();
        stopWatch.setLastMS(0);
        target = null;
    }
}

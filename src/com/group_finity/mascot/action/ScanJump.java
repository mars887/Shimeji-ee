package com.group_finity.mascot.action;

import com.group_finity.mascot.Main;
import com.group_finity.mascot.Mascot;

import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.group_finity.mascot.animation.Animation;
import com.group_finity.mascot.exception.BehaviorInstantiationException;
import com.group_finity.mascot.exception.CantBeAliveException;
import com.group_finity.mascot.exception.LostGroundException;
import com.group_finity.mascot.exception.VariableException;
import com.group_finity.mascot.script.VariableMap;

/**
 * Original Author: Yuki Yamada of Group Finity (http://www.group-finity.com/Shimeji/)
 * Currently developed by Shimeji-ee Group.
 */
public class ScanJump extends ActionBase {
    private static final Logger log = Logger.getLogger(ScanJump.class.getName());

    private static final String PARAMETER_AFFORDANCE = "Affordance";

    private static final String DEFAULT_AFFORDANCE = "";

    public static final String PARAMETER_BEHAVIOUR = "Behaviour";

    private static final String DEFAULT_BEHAVIOUR = "";

    public static final String PARAMETER_TARGETBEHAVIOUR = "TargetBehaviour";

    private static final String DEFAULT_TARGETBEHAVIOUR = "";

    //An Action Attribute is already named Velocity
    public static final String PARAMETER_VELOCITY = "VelocityParam";

    private static final double DEFAULT_VELOCITY = 20.0;

    private WeakReference<Mascot> target;

    public ScanJump(java.util.ResourceBundle schema, final List<Animation> animations, final VariableMap params) {
        super(schema, animations, params);
    }

    @Override
    public boolean hasNext() throws VariableException {
        if (getMascot().getManager() == null)
            return super.hasNext();

        if (target == null) {
            target = getMascot().getManager().getMascotWithAffordance(getAffordance());
        }

        return super.hasNext() && target != null && target.get() != null && target.get().getAffordances().contains(getAffordance());
    }

    @Override
    protected void tick() throws LostGroundException, VariableException {
        int targetX = target.get().getAnchor().x;
        int targetY = target.get().getAnchor().y;

        if (getMascot().getAnchor().x != targetX) {
            getMascot().setLookRight(getMascot().getAnchor().x < targetX);
        }

        double distanceX = targetX - getMascot().getAnchor().x;
        double distanceY = targetY - getMascot().getAnchor().y - Math.abs(distanceX) / 2;

        double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);

        double velocity = getVelocity();

        if (distance != 0) {
            int velocityX = (int) (velocity * distanceX / distance);
            int velocityY = (int) (velocity * distanceY / distance);

            getMascot().setAnchor(new Point(getMascot().getAnchor().x + velocityX,
                    getMascot().getAnchor().y + velocityY));
            getAnimation().next(getMascot(), getTime());
        }

        if (distance <= velocity) {
            getMascot().setAnchor(new Point(targetX, targetY));

            try {
                getMascot().setBehavior(Main.getInstance().getConfiguration(getMascot().getImageSet()).buildBehavior(getBehavior()));
                target.get().setBehavior(Main.getInstance().getConfiguration(target.get().getImageSet()).buildBehavior(getTargetBehavior()));
            } catch (final NullPointerException | BehaviorInstantiationException | CantBeAliveException e) {
                log.log(Level.SEVERE, "Fatal Exception", e);
                Main.showError(Main.getInstance().getLanguageBundle().getString("FailedSetBehaviourErrorMessage") + "\n" + e.getMessage() + "\n" + Main.getInstance().getLanguageBundle().getString("SeeLogForDetails"));
            }
        }
    }

    private String getAffordance() throws VariableException {
        return eval(getSchema().getString(PARAMETER_AFFORDANCE), String.class, DEFAULT_AFFORDANCE);
    }

    private String getBehavior() throws VariableException {
        return eval(getSchema().getString(PARAMETER_BEHAVIOUR), String.class, DEFAULT_BEHAVIOUR);
    }

    private String getTargetBehavior() throws VariableException {
        return eval(getSchema().getString(PARAMETER_TARGETBEHAVIOUR), String.class, DEFAULT_TARGETBEHAVIOUR);
    }

    private double getVelocity() throws VariableException {
        return eval(getSchema().getString(PARAMETER_VELOCITY), Number.class, DEFAULT_VELOCITY).doubleValue();
    }
}

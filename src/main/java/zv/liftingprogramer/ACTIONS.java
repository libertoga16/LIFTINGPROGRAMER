package zv.liftingprogramer;

import zv.liftingprogramer.GameGUI;

public interface ACTIONS {
    void performAttack(MONSTER monster, GameGUI gui);
    void performDodge(GameGUI gui);
    void performDefend(GameGUI gui);
    void performSpecialAction(MONSTER monster, GameGUI gui);
}
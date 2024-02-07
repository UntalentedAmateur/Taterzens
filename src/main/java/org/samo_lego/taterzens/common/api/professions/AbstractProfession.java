package org.samo_lego.taterzens.common.api.professions;

import org.samo_lego.taterzens.common.npc.TaterzenNPC;

public abstract class AbstractProfession implements TaterzenProfession {

    protected final TaterzenNPC npc;

    public AbstractProfession(TaterzenNPC npc) {
        this.npc = npc;
    }
}

package org.samo_lego.taterzens.common.interfaces;

import org.jetbrains.annotations.Nullable;
import org.samo_lego.taterzens.common.npc.NPCData;
import org.samo_lego.taterzens.common.npc.TaterzenNPC;

import java.util.Optional;

/**
 * Interface for players who edit TaterzenNPCs.
 */
public interface ITaterzenEditor {
    /**
     * Gets the selected Taterzen of player.
     *
     * @return selected Taterzen
     * @deprecated use {@link #getSelectedNpc()} instead
     */
    @Deprecated
    @Nullable
    default TaterzenNPC getNpc() {
        return this.getSelectedNpc().orElse(null);
    }


    /**
     * Gets the selected Taterzen of player.
     *
     * @return selected Taterzen
     */
    Optional<TaterzenNPC> getSelectedNpc();

    /**
     * Selects {@link TaterzenNPC} to be editoed.
     *
     * @param npc Taterzen to select, can be null to deselect.
     * @return true if successful, false if taterzen is locked and cannot be selected by this player.
     */
    boolean selectNpc(@Nullable TaterzenNPC npc);

    /**
     * Sets the index of message that's
     * being edited by the player.
     * Range: 0 - (size of {@link NPCData#messages} array - 1)
     * @param selected selected message in the messages array
     */
    void setEditingMessageIndex(int selected);

    /**
     * Gets the index of the message
     * player is editing for selected
     * {@link TaterzenNPC}.
     * @return index of message being edited in {@link NPCData#messages}
     */
    int getEditingMessageIndex();

    /**
     * Sets the active editor mode for the selected Taterzen.
     * @param mode editor mode type
     */
    void setEditorMode(EditorMode mode);

    /**
     * Gets current edit mode type.
     * @return current editor mode; NONE is default
     */
    EditorMode getEditorMode();

    /**
     * Available editor modes.
     */
    enum EditorMode {
        NONE,
        MESSAGES,
        PATH,
        EQUIPMENT
    }
}

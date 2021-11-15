package su.nightexpress.ama.editor;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorInputHandler;
import su.nightexpress.ama.AMA;

public abstract class ArenaInputHandler<T> implements EditorInputHandler<ArenaEditorType, T> {

    protected AMA plugin;

	public ArenaInputHandler(@NotNull AMA plugin) {
	    this.plugin = plugin;
    }
}

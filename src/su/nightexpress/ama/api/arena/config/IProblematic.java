package su.nightexpress.ama.api.arena.config;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nexmedia.engine.utils.StringUT;

import java.util.List;
import java.util.function.UnaryOperator;

public interface IProblematic extends IPlaceholder {

	String PLACEHOLDER_PROBLEMS = "%problems%";

	String PROBLEM_PREFIX = StringUT.color("&c⚠ &e");
	String PROBLEM_REGION_CUBOID_INVALID = "Invalid Region Cuboid!";
	String PROBLEM_REGION_SPAWN_LOCATION = "Invalid Spawn Location!";
	String PROBLEM_REGION_SPAWNERS_EMPTY = "No Mob Spawners Defined!";

	default boolean hasProblems() {
		return !this.getProblems().isEmpty();
	}
	
	@NotNull List<String> getProblems();

	@Override
	@NotNull
	default UnaryOperator<String> replacePlaceholders() {
		List<String> problems = this.getProblems().stream().map(str -> PROBLEM_PREFIX + str).toList();

		return str -> str.replace(PLACEHOLDER_PROBLEMS, String.join("\n", problems));
	}
}

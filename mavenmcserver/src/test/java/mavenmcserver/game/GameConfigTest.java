package mavenmcserver.game;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.joml.Vector3i;
import org.junit.jupiter.api.Test;

public class GameConfigTest {

	@Test
	public void testValidateTest() {
		GameConfig config = new GameConfig(null, null, new Vector3i(0, 0, 0), 2);
		List<String> errors = config.validate();
		assertTrue(errors.contains("Couldn't add you to the game. Please retry!"));
		assertTrue(errors.contains("Couldn't add the opponent player to the game."));
		assertTrue(errors.contains("No dimension of the game can be smaller than 1. The smallest possible game is (" + GameConfig.minFlatSize + ", " + GameConfig.minHeight + ", " + GameConfig.minFlatSize + ")."));
		assertTrue(errors.contains("The X and Z size of the game must not be smaller than " + GameConfig.minFlatSize));
		assertTrue(errors.contains("The required win amount must not be larger than the size's largest dimension"));
	}
	
}

package mavenmcserver.game;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.joml.Vector3i;
import org.junit.jupiter.api.Test;

public class GameConfigTest {

	// This test is a little useless...
	@Test
	public void testValidate() {
		GameConfig config = new GameConfig(null, null, new Vector3i(2, 1, 2), 2);
		List<String> errors = config.validateReturningErrors();
		assertTrue(errors.contains(GameConfig.ERROR_MAIN_PLAYER_NULL));
	}
	
	
	@Test
	public void testValidateNumbers() {
		GameConfig config = new GameConfig(null, null, new Vector3i(0, 0, 0), 2);
		List<String> errors = config.validateNumbersReturningErrors();
		assertTrue(errors.contains("No dimension of the game can be smaller than 1. The smallest possible game is (" + GameConfig.MIN_X_Z_SIZE + ", " + GameConfig.MIN_HEIGHT + ", " + GameConfig.MIN_X_Z_SIZE + ")."));
		assertTrue(errors.contains("The X and Z size of the game must not be smaller than " + GameConfig.MIN_X_Z_SIZE + "."));
		assertTrue(errors.contains("The required win amount must not be larger than the size's largest dimension."));
		
		config = new GameConfig(null, null, new Vector3i(0, 1, 2), 2);
		errors = config.validateNumbersReturningErrors();
		assertTrue(errors.contains("No dimension of the game can be smaller than 1. The smallest possible game is (" + GameConfig.MIN_X_Z_SIZE + ", " + GameConfig.MIN_HEIGHT + ", " + GameConfig.MIN_X_Z_SIZE + ")."));

		config = new GameConfig(null, null, new Vector3i(3, 1, 2), 4);
		errors = config.validateNumbersReturningErrors();
		assertTrue(errors.contains(GameConfig.ERROR_WIN_REQUIRED_AMOUNT_TOO_LARGE));
		
		config = new GameConfig(null, null, new Vector3i(1, 1, 2), 3);
		errors = config.validateNumbersReturningErrors();
		assertTrue(errors.contains("The X and Z size of the game must not be smaller than " + GameConfig.MIN_X_Z_SIZE + "."));
		
	}
	
}

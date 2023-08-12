package command;

import java.util.Deque;
import java.util.Iterator;

import enumerate.Action;
import enumerate.State;
import fighting.Character;
import input.KeyData;
import struct.Key;

/**
 * Class that performs the process of converting key input data into corresponding actions.
 */
public class CommandTable {

	/**
     * Class constructor.
     */
	public CommandTable() {

	}

	/**
     * Processes P1's or P2's key input data and returns the corresponding action.<br>
     * The distinction between P1 and P2 is based on the player number in the character data.
     *
     * @param character
     *            Character data
     * @param input
     *            Queue containing key inputs for both P1 and P2
     *
     * @return Action corresponding to the key input data
     *
     * @see KeyData
     */
	public Action interpretationCommandFromKeyData(Character character, Deque<KeyData> input) {
		Key nowKeyData;
		boolean pushA = false;
		boolean pushB = false;
		boolean pushC = false;
		int charIndex = character.isPlayerNumber() ? 0 : 1;

		KeyData temp;

		// get current key state
		temp = input.removeLast();
		nowKeyData = temp.getKeys()[charIndex];

		// Determine input only at the moment a button is pressed. Holding a button is treated as flicking.
		if (!input.isEmpty()) {
			pushA = nowKeyData.A && !input.getLast().getKeys()[charIndex].A;
			pushB = nowKeyData.B && !input.getLast().getKeys()[charIndex].B;
			pushC = nowKeyData.C && !input.getLast().getKeys()[charIndex].C;
		} else {
			pushA = nowKeyData.A;
			pushB = nowKeyData.B;
			pushC = nowKeyData.C;
		}

		input.addLast(temp);

		int lever;
		int[] commandList = { 5, 5, 5, 5 };
		int commandLength = 0;
		for (Iterator<KeyData> i = input.descendingIterator(); i.hasNext() && commandLength < 3;) {

			lever = i.next().getKeys()[charIndex].getLever(character.isFront());

			if (lever != commandList[commandLength]) {
				if (commandList[commandLength] != 5)
					commandLength++;
				commandList[commandLength] = lever;
			}
		}

		return convertKeyToAction(pushA, pushB, pushC, nowKeyData, commandList, character.getState(),
				character.isFront());
	}

	/**
     * Processes P1's or P2's key input data and returns the corresponding action.<br>
     * This method is only called within the simulator.
     *
     * @param character
     *            Character data
     * @param input
     *            Queue containing key inputs for either P1 or P2
     *
     * @return Action corresponding to the key input data
     *
     * @see Key
     */
	public Action interpretationCommandFromKey(Character character, Deque<Key> input) {
		boolean pushA = false;
		boolean pushB = false;
		boolean pushC = false;

		// get current key state
		Key nowKey = new Key(input.removeLast());

		// The decision as input only at the moment you press the button. Press
		// keeps flick.
		if (!input.isEmpty()) {
			pushA = nowKey.A && !input.getLast().A;
			pushB = nowKey.B && !input.getLast().B;
			pushC = nowKey.C && !input.getLast().C;
		} else {
			pushA = nowKey.A;
			pushB = nowKey.B;
			pushC = nowKey.C;
		}

		input.addLast(nowKey);

		int lever;
		int[] commandList = { 5, 5, 5, 5 };
		int commandLength = 0;
		for (Iterator<Key> i = input.descendingIterator(); i.hasNext() && commandLength < 3;) {
			lever = i.next().getLever(character.isFront());

			if (lever != commandList[commandLength]) {
				if (commandList[commandLength] != 5)
					commandLength++;
				commandList[commandLength] = lever;
			}
		}

		return convertKeyToAction(pushA, pushB, pushC, nowKey, commandList, character.getState(), character.isFront());
	}

	/**
     * Returns the action corresponding to the provided key input data and character information.
     *
     * @param pushA
     *            Whether the A key (P1: Z, P2: T) is being pushed in the latest key input
     * @param pushB
     *            Whether the B key (P1: X, P2: Y) is being pushed in the latest key input
     * @param pushC
     *            Whether the C key (P1: C, P2: U) is being pushed in the latest key input
     * @param nowKeyData
     *            The latest key input
     * @param commandList
     *            Array containing the most recent 4 directional key inputs (newer inputs have smaller indices)
     * @param state
     *            Current state of the character
     * @param isFront
     *            Direction the character is facing (true for right, false for left)
     *
     * @return Action corresponding to the provided key input data and character information
     *
     * @see Key
     * @see State
     * @see Action
     */
	private Action convertKeyToAction(boolean pushA, boolean pushB, boolean pushC, Key nowKeyData, int[] commandList,
			State state, boolean isFront) {
		// 789
		// 456
		// 123

		// AIR Action
		if (state == State.AIR) {
			if (pushB) {
				// special move
				if ((commandList[0] == 6 && commandList[1] == 3 && commandList[2] == 2)) {
					return Action.AIR_D_DF_FB;// AIR236B

				} else if ((commandList[0] == 3 && commandList[1] == 2 && commandList[2] == 6)
						|| (commandList[0] == 3 && commandList[1] == 2 && commandList[2] == 3 && commandList[3] == 6)) {
					return Action.AIR_F_D_DFB;// AIR623B

				} else if (commandList[0] == 4 && commandList[1] == 1 && commandList[2] == 2) {
					return Action.AIR_D_DB_BB;// AIR214B

				} else if (nowKeyData.getLever(isFront) == 2) {
					return Action.AIR_DB;// AIR2B

				} else if (nowKeyData.getLever(isFront) == 8) {
					return Action.AIR_UB;// AIR8B

				} else if (nowKeyData.getLever(isFront) == 6) {
					return Action.AIR_FB;// AIR6B

				} else {
					return Action.AIR_B;// AIR5B
				}

			} else if (pushA) {
				// special move
				if ((commandList[0] == 6 && commandList[1] == 3 && commandList[2] == 2)) {
					return Action.AIR_D_DF_FA;// AIR236A

				} else if ((commandList[0] == 3 && commandList[1] == 2 && commandList[2] == 6)
						|| (commandList[0] == 3 && commandList[1] == 2 && commandList[2] == 3 && commandList[3] == 6)) {
					return Action.AIR_F_D_DFA;// AIR623A

				} else if (commandList[0] == 4 && commandList[1] == 1 && commandList[2] == 2) {
					return Action.AIR_D_DB_BA;// AIR214A

				} else if (nowKeyData.getLever(isFront) == 2) {
					return Action.AIR_DA;// AIR2A

				} else if (nowKeyData.getLever(isFront) == 8) {
					return Action.AIR_UA;// AIR8A

				} else if (nowKeyData.getLever(isFront) == 6) {
					return Action.AIR_FA;// AIR6A

				} else {
					return Action.AIR_A;// AIR5A
				}

			} else if (nowKeyData.getLever(isFront) == 4) {
				return Action.AIR_GUARD;// AIR4

			} else {
				return Action.AIR;// AIR5
			}

			// Ground Action
		} else {
			// Super special move
			if (pushC) {
				if ((commandList[0] == 6 && commandList[1] == 3 && commandList[2] == 2)) {
					return Action.STAND_D_DF_FC;// STAND236A
				}

			} else if (pushB) {
				// special move
				if ((commandList[0] == 6 && commandList[1] == 3 && commandList[2] == 2)) {
					return Action.STAND_D_DF_FB;// STAND236B

				} else if ((commandList[0] == 3 && commandList[1] == 2 && commandList[2] == 6)
						|| (commandList[0] == 3 && commandList[1] == 2 && commandList[2] == 3 && commandList[3] == 6)) {
					return Action.STAND_F_D_DFB;// STAND623B

				} else if (commandList[0] == 4 && commandList[1] == 1 && commandList[2] == 2) {
					return Action.STAND_D_DB_BB;// STAND214B

					// normal move
				} else if (nowKeyData.getLever(isFront) == 3) {
					return Action.CROUCH_FB;// STAND3B

				} else if (nowKeyData.getLever(isFront) == 2) {
					return Action.CROUCH_B;// STAND2B

				} else if (nowKeyData.getLever(isFront) == 4) {
					return Action.THROW_B;// STAND4B

				} else if (nowKeyData.getLever(isFront) == 6) {
					return Action.STAND_FB;// STAND6B

				} else {
					return Action.STAND_B;// STAND5B
				}

			} else if (pushA) {
				// special move
				if ((commandList[0] == 6 && commandList[1] == 3 && commandList[2] == 2)) {
					return Action.STAND_D_DF_FA;// STAND236A

				} else if ((commandList[0] == 3 && commandList[1] == 2 && commandList[2] == 6)
						|| (commandList[0] == 3 && commandList[1] == 2 && commandList[2] == 3 && commandList[3] == 6)) {
					return Action.STAND_F_D_DFA;// STAND623A

				} else if (commandList[0] == 4 && commandList[1] == 1 && commandList[2] == 2) {
					return Action.STAND_D_DB_BA;// STAND214A

					// normal move
				} else if (nowKeyData.getLever(isFront) == 3) {
					return Action.CROUCH_FA;// CROUCH3A

				} else if (nowKeyData.getLever(isFront) == 2) {
					return Action.CROUCH_A;// CROUCH2A

				} else if (nowKeyData.getLever(isFront) == 4) {
					return Action.THROW_A;// THROW4A

				} else if (nowKeyData.getLever(isFront) == 6) {
					return Action.STAND_FA;// STAND6A

				} else {
					return Action.STAND_A;// STAND5A
				}

			} else if (nowKeyData.getLever(isFront) == 6) {
				if (commandList[1] == 6) {
					return Action.DASH;// STAND66

				} else {
					return Action.FORWARD_WALK;// STAND6
				}

			} else if (nowKeyData.getLever(isFront) == 4) {
				if (commandList[1] == 4) {
					return Action.BACK_STEP;// STAND44

				} else {
					return Action.STAND_GUARD;// STAND4
				}

			} else {
				if (nowKeyData.getLever(isFront) == 1) {
					return Action.CROUCH_GUARD;// CROUCH1

				} else if (nowKeyData.getLever(isFront) == 2) {
					return Action.CROUCH;// CROUCH2

				} else if (nowKeyData.getLever(isFront) == 7) {
					return Action.BACK_JUMP;// STAND7

				} else if (nowKeyData.getLever(isFront) == 9) {
					return Action.FOR_JUMP;// STAND9
				}

				else if (nowKeyData.getLever(isFront) == 8) {
					return Action.JUMP;// STAND8

				} else {
					return Action.STAND;// STAND
				}

			}
		}
		return Action.STAND;
	}
}

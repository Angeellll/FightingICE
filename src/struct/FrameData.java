package struct;

import java.util.Deque;
import java.util.LinkedList;

import input.KeyData;
import manager.SoundManager;
import setting.FlagSetting;
import setting.GameSetting;

/**
 * The class dealing with the information in the game such as the current frame
 * number, number of rounds and character information.
 */
public class FrameData {

    /**
     * The character's data of both characters<br>
     * Index 0 is P1, index 1 is P2.
     */
    private CharacterData[] characterData;

    /**
     * The current frame of the round.
     */
    private int currentFrameNumber;

    /**
     * The current round number.
     */
    private int currentRound;

    /**
     * The projectile data of both characters.
     */
    private Deque<AttackData> projectileData;

    /**
     * If this value is true, no data are available or they are dummy data.
     */
    private boolean emptyFlag;


    /**
     * Audio's data of both players <br>
     * Index 0 is P1, index 1 is P2
     */
    private AudioData audioData;

    /**
     * imported from @{@link CharacterData 's hp}
     */
    private int[] hp;

    /**
     * imported from {@link CharacterData's front}
     */
    private boolean[] front;

    /**
     * The class constructor.
     */
    public FrameData() {
        this.characterData = new CharacterData[]{null, null};
        this.currentFrameNumber = -1;
        this.currentRound = -1;
        this.projectileData = new LinkedList<AttackData>();
        this.emptyFlag = true;
        this.audioData = new AudioData();
    }

    /**
     * The class constructor that creates a new instance of the FrameData class
     * by copying the data passed as the arguments.
     *
     * @param characterData  an instance of the CharacterData class
     * @param currentFrame   the frame number of the current frame
     * @param currentRound   the round number of the current round
     * @param projectileData the queue that stores information on projectiles of P1 and P2
     * @see CharacterData
     * @see KeyData
     */
    public FrameData(CharacterData[] characterData, int currentFrame, int currentRound,
                     Deque<AttackData> projectileData, boolean renderAudio) {
        this.characterData = new CharacterData[]{characterData[0], characterData[1]};
        this.currentFrameNumber = currentFrame;
        this.currentRound = currentRound;

        // make deep copy of the attacks list
        this.projectileData = new LinkedList<AttackData>();
        for (AttackData attack : projectileData) {
            this.projectileData.add(new AttackData(attack));
        }
        // sample raw audio data
        if (renderAudio && (FlagSetting.soundPlay || FlagSetting.soundTrain))
            this.audioData = new AudioData(SoundManager.getInstance().getSoundRenderer().sampleAudio());
        else
            this.audioData = new AudioData();
        this.emptyFlag = false;
        this.hp = new int[2];
        this.hp[0] = characterData[0].getHp();
        this.hp[1] = characterData[1].getHp();
        this.front = new boolean[2];
        this.front[0] = characterData[0].isFront();
        this.front[1] = characterData[1].isFront();
    }

    /**
     * A copy constructor that creates a copy of an instance of the FrameData
     * class by copying the values of the variables from an instance of the
     * FrameData class passed as the argument.
     *
     * @param frameData an instance of the FrameData class
     */
    public FrameData(FrameData frameData) {
        this.characterData = new CharacterData[2];
        this.characterData[0] = frameData.getCharacter(true);
        this.characterData[1] = frameData.getCharacter(false);
        this.currentFrameNumber = frameData.getFramesNumber();
        this.currentRound = frameData.getRound();

        // make deep copy of the attacks list
        this.projectileData = new LinkedList<AttackData>();
        Deque<AttackData> temp = frameData.getProjectiles();
        for (AttackData attack : temp) {
            this.projectileData.add(new AttackData(attack));
        }

        this.emptyFlag = frameData.getEmptyFlag();
        // copy audio data
        this.audioData = new AudioData(frameData.getAudioData());
        try {
            this.hp = new int[2];
            this.hp[0] = this.characterData[0].getHp();
            this.hp[1] = this.characterData[1].getHp();
            this.front = new boolean[2];
            this.front[0] = this.characterData[0].isFront();
            this.front[1] = this.characterData[1].isFront();
        } catch (NullPointerException ex) {
            // there is no character data
        }
    }

    /**
     * Create FrameData for AI
     * it removes visual-data in sound mode, otherwise does nothing
     */

    public void removeVisualData() {
        if (!FlagSetting.soundTrain && !FlagSetting.soundPlay) {
            return;
        } else {
            this.characterData = new CharacterData[2];
            this.currentFrameNumber = -1;
            this.currentRound = -1;
            this.projectileData = new LinkedList<AttackData>();
            if (FlagSetting.soundPlay) {
                this.hp[0] = 0;
                this.hp[1] = 0;
            }
        }
    }

    /**
     * Returns an instance of the CharacterData class of the player specified by
     * an argument.
     *
     * @param playerNumber the number of the player. {@code true} if the player is P1, or
     *                     {@code false} if P2.
     * @return an instance of the CharacterData class of the player
     */
    public CharacterData getCharacter(boolean playerNumber) {
        CharacterData temp = this.characterData[playerNumber ? 0 : 1];

        return temp == null ? null : new CharacterData(temp);
    }

    public int getHp(boolean playerNumber) {
        return playerNumber ? this.hp[0] : this.hp[1];
    }

    /**
     * Returns the expected remaining time in milliseconds of the current round.
     * <br>
     * When FightingICE was launched with the training mode, this method returns
     * the max value of integer.
     *
     * @return the expected remaining time in milliseconds of the current round
     */
    public int getRemainingTimeMilliseconds() {
        if (FlagSetting.trainingModeFlag) {
            return Integer.MAX_VALUE;
        } else {
            return GameSetting.ROUND_TIME - (int) (((float) this.currentFrameNumber / GameSetting.FPS) * 1000);
        }
    }

    /**
     * Returns the expected remaining time in seconds of the current round.<br>
     * When FightingICE was launched with the training mode, this method returns
     * the max value of integer.
     *
     * @return the expected remaining time in seconds of the current round
     * @deprecated Use {@link #getRemainingTimeMilliseconds()} instead. This
     * method has been renamed to more clearly reflect its purpose.
     */
    public int getRemainingTime() {
        if (FlagSetting.trainingModeFlag) {
            return Integer.MAX_VALUE;
        } else {
            return (int) Math.ceil((float) getRemainingTimeMilliseconds() / 1000);
        }
    }

    /**
     * Returns the number of remaining frames of the round. <br>
     * When FightingICE was launched with the training mode, this method returns
     * the max value of integer.
     *
     * @return the number of remaining frames of the round
     */
    public int getRemainingFramesNumber() {
        if (FlagSetting.trainingModeFlag) {
            return Integer.MAX_VALUE;
        } else {
            return (GameSetting.ROUND_FRAME_NUMBER - currentFrameNumber);
        }
    }

    /**
     * Returns the number of frames since the beginning of the round.
     *
     * @return the number of frames since the beginning of the round
     */
    public int getFramesNumber() {
        return this.currentFrameNumber;
    }

    /**
     * Returns the current round number.
     *
     * @return the current round number
     */
    public int getRound() {
        return this.currentRound;
    }

    /**
     * Returns the projectile data of both characters.
     *
     * @return the projectile data of both characters
     */
    public Deque<AttackData> getProjectiles() {
        // create a deep copy of the attacks list
        if (FlagSetting.soundPlay || FlagSetting.soundTrain)
            return new LinkedList<>();
        LinkedList<AttackData> attackList = new LinkedList<AttackData>();
        for (AttackData attack : this.projectileData) {
            attackList.add(new AttackData(attack));
        }
        return attackList;
    }

    /**
     * Returns the projectile data of player 1.
     *
     * @return the projectile data of player 1
     */
    public Deque<AttackData> getProjectilesByP1() {
        if (FlagSetting.soundPlay || FlagSetting.soundTrain)
            return new LinkedList<>();
        LinkedList<AttackData> attackList = new LinkedList<AttackData>();
        for (AttackData attack : this.projectileData) {
            if (attack.isPlayerNumber()) {
                attackList.add(new AttackData(attack));
            }
        }
        return attackList;
    }

    /**
     * Returns the projectile data of player 2.
     *
     * @return the projectile data of player 2
     */
    public Deque<AttackData> getProjectilesByP2() {
        if (FlagSetting.soundPlay || FlagSetting.soundTrain)
            return new LinkedList<>();
        LinkedList<AttackData> attackList = new LinkedList<AttackData>();
        for (AttackData attack : this.projectileData) {
            if (!attack.isPlayerNumber()) {
                attackList.add(new AttackData(attack));
            }
        }
        return attackList;
    }

    /**
     * Returns true if this instance is empty, false if it contains meaningful
     * data.
     *
     * @return {@code true} if this instance is empty, or {@code false} if it
     * contains meaningful data
     */
    public boolean getEmptyFlag() {
        return this.emptyFlag;
    }

    /**
     * Returns the horizontal distance between P1 and P2.
     *
     * @return the horizontal distance between P1 and P2
     */
    public int getDistanceX() {
        if (FlagSetting.soundPlay || FlagSetting.soundTrain)
            return 0;
        return Math.abs((this.characterData[0].getCenterX() - this.characterData[1].getCenterX()));
    }

    /**
     * Returns the vertical distance between P1 and P2.
     *
     * @return the vertical distance between P1 and P2
     */
    public int getDistanceY() {
        if (FlagSetting.soundPlay || FlagSetting.soundTrain)
            return 0;
        return Math.abs((this.characterData[0].getCenterY() - this.characterData[1].getCenterY()));
    }

    public AudioData getAudioData() {
        return audioData;
    }

    public boolean isFront(boolean player) {
        return this.front[player ? 0 : 1];
    }
}

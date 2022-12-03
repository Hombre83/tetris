package tetris;

import java.util.Random;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Tetris extends Application implements EventHandler<KeyEvent>{
    public static final int FIELDS_HOR = 10;
    public static final int FIELDS_VER = 20;
    private static BorderPane root;
    private static volatile Node [][]patternA = new Node[FIELDS_HOR][FIELDS_VER];
    private static Tetrimino currTetriminoA, nextTetriminoA;
    public static int gameSpeedA = 1000;
    
    private static int linesCountA = 0;
    private static Text txtCurrentScoreA = new Text("00000000");
    private static Text txtCurrentLevelA = new Text("1");
    private static Rectangle[][] blockPreviewA = new Rectangle[12][12];
    private static int highScoreA = 0;
    private static boolean goA = false;
    private static int choiceA = 0;
    
    @Override
    public void start(Stage stage) {
        root = new BorderPane();
        Rectangle2D r = Screen.getPrimary().getBounds();
        root.setMinSize(r.getWidth(), r.getHeight());
        root.setPrefSize(r.getWidth(), r.getHeight());
        root.setMaxSize(r.getWidth(), r.getHeight());
        root.setLayoutX(0.0);
        root.setLayoutY(0.0);

        Scene scene = new Scene(root, r.getWidth(), r.getHeight());
        scene.setOnKeyPressed(this);
        scene.setOnKeyReleased(this);
        
        double playfieldHeight = r.getHeight() * 0.85;
        double cubeSize = playfieldHeight / FIELDS_VER;
        double playfieldX, playfieldY;
        
        //  create background
        Rectangle rect = new Rectangle(r.getWidth(), r.getHeight());
        rect.setFill(Paint.valueOf("rgb(25, 0, 200)"));
        root.getChildren().add(rect);
        
        //  create hud
        Rectangle hud = new Rectangle(playfieldHeight * 1.05 / FIELDS_VER * (FIELDS_HOR + 3), playfieldHeight * 1.05);
        hud.setX((r.getWidth() - hud.getWidth()) / 2.0);
        hud.setY((r.getHeight() - hud.getHeight()) / 2.0);
        hud.setFill(Paint.valueOf("rgb(50, 25, 255)"));
        root.getChildren().add(hud);
        
        //  create playfield background
        Rectangle playfieldA = new Rectangle(playfieldHeight / FIELDS_VER * FIELDS_HOR, playfieldHeight);
        playfieldA.setX(playfieldX = Math.round((hud.getHeight() - playfieldA.getHeight()) / 2.0 + hud.getX()));
        playfieldA.setY(playfieldY = Math.round(r.getHeight() - playfieldHeight) / 2.0);
        playfieldA.setFill(Paint.valueOf("white"));
        root.getChildren().add(playfieldA);
        
        //  create block preview display
        double blockPrevSize = Math.round(hud.getWidth() - playfieldA.getWidth() - (hud.getHeight() - playfieldA.getHeight()) / 2.0 * 3.0);        
        Rectangle blockPrevA = new Rectangle(blockPrevSize, blockPrevSize);
        blockPrevA.setX(Math.round(playfieldA.getX() + playfieldA.getWidth() + (hud.getHeight() - playfieldA.getHeight()) / 2.0));
        blockPrevA.setY(Math.round(hud.getY() + hud.getHeight() / 2.0 - blockPrevSize / 2.0));
        blockPrevA.setFill(Paint.valueOf("white"));
        root.getChildren().add(blockPrevA);
        
        Text txtBlockPrevA = new Text("NEXT");
        txtBlockPrevA.setFont(new Font("Verdana", blockPrevSize / 5.0));
        txtBlockPrevA.setX(blockPrevA.getX() + (blockPrevSize - txtBlockPrevA.getBoundsInLocal().getWidth()) / 2.0);
        txtBlockPrevA.setY(Math.round(blockPrevA.getY() - txtBlockPrevA.getBoundsInLocal().getHeight() / 2.5));
        txtBlockPrevA.setFill(Paint.valueOf("white"));
        root.getChildren().add(txtBlockPrevA);
        
        double bpPatternSize = Math.round(blockPrevSize / (double) blockPreviewA.length);
        
        for (int i = 0; i < blockPreviewA.length; i++) {
            for (int j = 0; j < blockPreviewA[0].length; j++) {
                blockPreviewA[i][j] = new Rectangle(bpPatternSize, bpPatternSize);
                blockPreviewA[i][j].setX(blockPrevA.getX() + bpPatternSize * i);
                blockPreviewA[i][j].setY(blockPrevA.getY() + bpPatternSize * j);
                blockPreviewA[i][j].setOpacity(0.0);
                root.getChildren().add(blockPreviewA[i][j]);
            }
        }
        
        //  create highscore display
        Text txtHiScoreA = new Text("HI-SCORE");
        txtHiScoreA.setFont(txtBlockPrevA.getFont());
        txtHiScoreA.setX(blockPrevA.getX() + (blockPrevSize - txtHiScoreA.getBoundsInLocal().getWidth()) / 2.0);
        txtHiScoreA.setY(Math.round(hud.getY() + (hud.getHeight() - playfieldA.getHeight()) / 4.0 * 3.5));
        txtHiScoreA.setFill(Paint.valueOf("white"));
        root.getChildren().add(txtHiScoreA);
                        
        Text txtCurrentHiScoreA = new Text("00000000");
        txtCurrentHiScoreA.setFont(new Font("Verdana Bold", blockPrevSize / 5.0));
        txtCurrentHiScoreA.setX(blockPrevA.getX() + (blockPrevSize - txtCurrentHiScoreA.getBoundsInLocal().getWidth()) / 2.0);
        txtCurrentHiScoreA.setY(Math.round(txtHiScoreA.getY() + txtHiScoreA.getBoundsInLocal().getHeight()));
        txtCurrentHiScoreA.setFill(Paint.valueOf("white"));
        root.getChildren().add(txtCurrentHiScoreA);
                        
        //  create score display
        Text txtScoreA = new Text("SCORE");
        txtScoreA.setFont(txtBlockPrevA.getFont());
        txtScoreA.setX(blockPrevA.getX() + (blockPrevSize - txtScoreA.getBoundsInLocal().getWidth()) / 2.0);
        txtScoreA.setY(Math.round(txtCurrentHiScoreA.getY() + (hud.getHeight() - playfieldA.getHeight()) / 4.0 * 3.0));
        txtScoreA.setFill(Paint.valueOf("white"));
        root.getChildren().add(txtScoreA);
                        
        txtCurrentScoreA.setFont(new Font("Verdana Bold", blockPrevSize / 5.0));
        txtCurrentScoreA.setX(blockPrevA.getX() + (blockPrevSize - txtCurrentScoreA.getBoundsInLocal().getWidth()) / 2.0);
        txtCurrentScoreA.setY(Math.round(txtScoreA.getY() + txtScoreA.getBoundsInLocal().getHeight()));
        txtCurrentScoreA.setFill(Paint.valueOf("white"));
        root.getChildren().add(txtCurrentScoreA);
        
        //  create level display
        txtCurrentLevelA.setFont(new Font("Verdana Bold", blockPrevSize / 2.5));
        txtCurrentLevelA.setX(blockPrevA.getX() + (blockPrevSize - txtCurrentLevelA.getBoundsInLocal().getWidth()) / 2.0);
        txtCurrentLevelA.setY(Math.round(hud.getY() + hud.getHeight() / 3.25));
        txtCurrentLevelA.setFill(Paint.valueOf("white"));
        root.getChildren().add(txtCurrentLevelA);
                        
        Text txtLvlA = new Text("LEVEL");
        txtLvlA.setFont(txtBlockPrevA.getFont());
        txtLvlA.setX(blockPrevA.getX() + (blockPrevSize - txtLvlA.getBoundsInLocal().getWidth()) / 2.0);
        txtLvlA.setY(Math.round(txtCurrentLevelA.getY() - txtLvlA.getBoundsInLocal().getHeight() * 1.75));
        txtLvlA.setFill(Paint.valueOf("white"));
        root.getChildren().add(txtLvlA);
        
        //  create patternA
        for (int i = 0; i < FIELDS_VER; i++) {
            for (int j = 0; j < FIELDS_HOR; j++) {
                Rectangle cube = new Rectangle(cubeSize, cubeSize);
                cube.setX(playfieldX + cube.getWidth() * j);
                cube.setY(playfieldY + cube.getWidth() * i);
                cube.setFill(Paint.valueOf("white"));
                cube.setOpacity(0.0);
                root.getChildren().add(cube);
                patternA[j][i] = root.getChildren().get(root.getChildren().size() - 1);
            }
        }
        
        //  create "game over" message
        Rectangle gameOverA = new Rectangle(playfieldA.getWidth() / 3.0 * 2.0, playfieldA.getWidth() / 6.0 * 2.0);
        gameOverA.setX(playfieldA.getX() + playfieldA.getWidth() / 6.0);
        gameOverA.setY(playfieldA.getY() + playfieldA.getHeight() / 3.0);
        gameOverA.setFill(Paint.valueOf("rgb(150, 0, 0)"));
        gameOverA.setVisible(false);
        root.getChildren().add(gameOverA);
        
        Text txtTitleGameOverA = new Text();
        txtTitleGameOverA.setText("GAME OVER");
        txtTitleGameOverA.setFont(new Font("Verdana Bold", gameOverA.getHeight() / 6.0));
        txtTitleGameOverA.setFill(Paint.valueOf("white"));
        txtTitleGameOverA.setX(gameOverA.getX() + (gameOverA.getWidth() - txtTitleGameOverA.getBoundsInLocal().getWidth()) / 2.0);
        txtTitleGameOverA.setY(gameOverA.getY() + txtTitleGameOverA.getBoundsInLocal().getHeight());
        txtTitleGameOverA.setVisible(false);
        root.getChildren().add(txtTitleGameOverA);
        
        Line lineGameOverA = new Line();
        lineGameOverA.setStroke(Paint.valueOf("white"));
        lineGameOverA.setStrokeWidth(2.0);
        lineGameOverA.setStartX(gameOverA.getX() + gameOverA.getWidth() / 10.0);
        lineGameOverA.setEndX(gameOverA.getX() + gameOverA.getWidth() - gameOverA.getWidth() / 10.0);
        lineGameOverA.setStartY(txtTitleGameOverA.getY() + txtTitleGameOverA.getBoundsInLocal().getHeight() / 3.0);
        lineGameOverA.setEndY(txtTitleGameOverA.getY() + txtTitleGameOverA.getBoundsInLocal().getHeight() / 3.0);
        lineGameOverA.setVisible(false);
        root.getChildren().add(lineGameOverA);
        
        Text txtPlayAgainGameOverA = new Text();
        txtPlayAgainGameOverA.setText("Play again?");
        txtPlayAgainGameOverA.setFont(new Font("Verdana", gameOverA.getHeight() / 8.0));
        txtPlayAgainGameOverA.setFill(Paint.valueOf("white"));
        txtPlayAgainGameOverA.setX(gameOverA.getX() + (gameOverA.getWidth() - txtPlayAgainGameOverA.getBoundsInLocal().getWidth()) / 2.0);
        txtPlayAgainGameOverA.setY(lineGameOverA.getStartY() + txtTitleGameOverA.getBoundsInLocal().getHeight() / 2.0 + txtTitleGameOverA.getBoundsInLocal().getHeight() / 2.5);
        txtPlayAgainGameOverA.setVisible(false);
        root.getChildren().add(txtPlayAgainGameOverA);
        
        Group yesA = new Group();
        Rectangle yesRectGameOverA = new Rectangle(gameOverA.getWidth() / 3.0, gameOverA.getWidth() / 8.0);
        yesRectGameOverA.setX(gameOverA.getX() + gameOverA.getWidth() / 9.0);
        yesRectGameOverA.setY(txtPlayAgainGameOverA.getY() + txtPlayAgainGameOverA.getBoundsInLocal().getHeight() / 1.25);
        yesRectGameOverA.setFill(Paint.valueOf("white"));
        yesA.getChildren().add(yesRectGameOverA);
        
        Text txtYesGameOverA = new Text();
        txtYesGameOverA.setText("YES");
        txtYesGameOverA.setFont(new Font("Verdana", gameOverA.getHeight() / 12.0));
        txtYesGameOverA.setFill(Paint.valueOf("rgb(150, 0, 0)"));
        txtYesGameOverA.setX(yesRectGameOverA.getX() + (yesRectGameOverA.getWidth() - txtYesGameOverA.getBoundsInLocal().getWidth()) / 2.0);
        txtYesGameOverA.setY(yesRectGameOverA.getY() + (yesRectGameOverA.getHeight() - txtYesGameOverA.getBoundsInLocal().getHeight() / 2.0) / 1.25);
        yesA.getChildren().add(txtYesGameOverA);
        
        yesA.setOnMouseEntered((event) -> {
            scene.setCursor(Cursor.HAND);
            yesRectGameOverA.setFill(Paint.valueOf("rgb(255, 200, 200)"));
        });
        yesA.setOnMouseExited((event) -> {
            scene.setCursor(Cursor.DEFAULT);
            yesRectGameOverA.setFill(Paint.valueOf("white"));
        });
        yesA.setOnMouseClicked((event) -> {
            choiceA = 1;
        });
        yesA.setVisible(false);
        root.getChildren().add(yesA);

        Group noA = new Group();
        Rectangle noRectGameOverA = new Rectangle(gameOverA.getWidth() / 3.0, gameOverA.getWidth() / 8.0);
        noRectGameOverA.setX(gameOverA.getX() + gameOverA.getWidth() - gameOverA.getWidth() / 9.0 - noRectGameOverA.getWidth());
        noRectGameOverA.setY(txtPlayAgainGameOverA.getY() + txtPlayAgainGameOverA.getBoundsInLocal().getHeight() / 1.25);
        noRectGameOverA.setFill(Paint.valueOf("white"));
        noA.getChildren().add(noRectGameOverA);
        
        Text txtNoGameOverA = new Text();
        txtNoGameOverA.setText("NO");
        txtNoGameOverA.setFont(new Font("Verdana", gameOverA.getHeight() / 12.0));
        txtNoGameOverA.setFill(Paint.valueOf("rgb(150, 0, 0)"));
        txtNoGameOverA.setX(noRectGameOverA.getX() + (noRectGameOverA.getWidth() - txtNoGameOverA.getBoundsInLocal().getWidth()) / 2.0);
        txtNoGameOverA.setY(noRectGameOverA.getY() + (noRectGameOverA.getHeight() - txtNoGameOverA.getBoundsInLocal().getHeight() / 2.0) / 1.25);
        noA.getChildren().add(txtNoGameOverA);
        noA.setOnMouseEntered((event) -> {
            scene.setCursor(Cursor.HAND);
            noRectGameOverA.setFill(Paint.valueOf("rgb(255, 200, 200)"));
        });
        noA.setOnMouseExited((event) -> {
            scene.setCursor(Cursor.DEFAULT);
            noRectGameOverA.setFill(Paint.valueOf("white"));
        });
        noA.setOnMouseClicked((event) -> {
            choiceA = -1;
        });
        noA.setVisible(false);
        root.getChildren().add(noA);
                
        //  set stage
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.setFullScreen(true);
        stage.show();
        
        //  player A's game
        Thread gameA = new Thread() {
            @Override
            public void run() {
                Random rnd = new Random();
                nextTetriminoA = new Tetrimino(rnd.nextInt(7), patternA);
                while (true) {
                    currTetriminoA = new Tetrimino(nextTetriminoA.getIndex(), patternA);
                    nextTetriminoA = new Tetrimino(rnd.nextInt(7), patternA);
                    setBlockPreview('a');
                    currTetriminoA.drop();
                    
                    for (int i = 0; i < 4; i++) {
                        //  check pattern of player A for game over
                        if (patternA[i + FIELDS_HOR / 2 - 2][0].getOpacity() > 1.0) {
                            clearBlockPreview('a');
                            goA = true;
                            
                            if (highScoreA < getScore('a'))
                                highScoreA = getScore('a');
                            
                            //  show player A's game over message
                            gameOverA.setVisible(true);
                            txtTitleGameOverA.setVisible(true);
                            lineGameOverA.setVisible(true);
                            txtPlayAgainGameOverA.setVisible(true);
                            yesA.setVisible(true);
                            noA.setVisible(true);
                            
                            while (choiceA == 0) {
                                try {
                                    sleep(100);
                                } catch(InterruptedException ex) {
                                }
                            }
                            
                            if (choiceA > 0) {
                                goA = false;
                                linesCountA = 0;
                                
                                if (getScore('a') > getScore(txtCurrentHiScoreA.getText()))
                                txtCurrentHiScoreA.setText(txtCurrentScoreA.getText());
                                txtCurrentScoreA.setText("00000000");
                                setLevel('a');
                                setBlockPreview('a');
                                
                                clearPattern('a');
                                
                                gameOverA.setVisible(false);
                                txtTitleGameOverA.setVisible(false);
                                lineGameOverA.setVisible(false);
                                txtPlayAgainGameOverA.setVisible(false);
                                yesA.setVisible(false);
                                noA.setVisible(false);
                                
                                choiceA = 0;
                            } else {
                                System.exit(0);
                            }
                            
                            
                            break;
                        }
                    }
                }
            }
        };

        gameA.setName("gameA");
        gameA.setDaemon(true);
        gameA.start();
        
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void handle(KeyEvent event) {
        //  ROTATE: 58, DOWN: 54, LEFT: 36, RIGHT: 39
        //  y: 60, n: 49
        if (event.getEventType().equals(KeyEvent.KEY_PRESSED)) {
            if (event.getCode().ordinal() == 58)
                currTetriminoA.rotate(true);
            else if (event.getCode().ordinal() == 54)
                currTetriminoA.moveDown(true);
            else if (event.getCode().ordinal() == 36)
                currTetriminoA.moveLeft(true);
            else if (event.getCode().ordinal() == 39)
                currTetriminoA.moveRight(true);
            else if (event.getCode().ordinal() == 60) {
                if (goA)
                    choiceA = 1;
            } else if (event.getCode().ordinal() == 49) {
                if (goA)
                    choiceA = -1;
            }
                
        } else if (event.getEventType().equals(KeyEvent.KEY_RELEASED)) {
            if (event.getCode().ordinal() == 58)
                currTetriminoA.rotate(false);
            else if (event.getCode().ordinal() == 54)
                currTetriminoA.moveDown(false);
            else if (event.getCode().ordinal() == 36)
                currTetriminoA.moveLeft(false);
            else if (event.getCode().ordinal() == 39)
                currTetriminoA.moveRight(false);
                
        }
    }
    
    public static void setScore(char player, int lines) {
        StringBuilder scoreString = new StringBuilder();
        Text scoreDisplay = null;
        Text levelDisplay = null;
        
        if ((player == 'a') || (player == 'A')) {
            scoreDisplay = txtCurrentScoreA;
            levelDisplay = txtCurrentLevelA;
            linesCountA += lines;
        }
        
        int score = Integer.valueOf(scoreDisplay.getText());
        
        switch (lines) {
            case 1:
                score += Integer.valueOf(levelDisplay.getText()) * 100;
                break;
            case 2:
                score += Integer.valueOf(levelDisplay.getText()) * 300;
                break;
            case 3:
                score += Integer.valueOf(levelDisplay.getText()) * 500;
                break;
            case 4:
                score += Integer.valueOf(levelDisplay.getText()) * 800;
                break;
        }

        scoreString.append(score);
        System.out.println("score len: " + scoreString.length());

        int len = 8 - scoreString.length();
        for (int i = 0; i < len; i++)
            scoreString.insert(0, "0");
        System.out.println("score: " + scoreString.toString());
        
        scoreDisplay.setText(scoreString.toString());
        
        //blinkText(scoreDisplay, "white", "red", 10, 100);
        
        setLevel(player);
        
    }
    
    private static void setLevel(char player) {
        Text levelDisplay = null;
        int level, linesCount;
        linesCount = 0;
        
        if ((player == 'a') || (player == 'A')) {
            levelDisplay = txtCurrentLevelA;
            linesCount = linesCountA;
        }
        
        System.out.println("lines count: " + linesCount);
        level = linesCountA / 12 + 1;
        
        if (level == 1) {
            gameSpeedA = 1000;
        } else if (level > Integer.valueOf(levelDisplay.getText())) {
            if (gameSpeedA > 25)
                gameSpeedA -= 50;
        }
        
        double oldWidth = levelDisplay.getBoundsInLocal().getWidth();

        levelDisplay.setText(String.valueOf(level));
        levelDisplay.setX(levelDisplay.getX() + (oldWidth - levelDisplay.getBoundsInLocal().getWidth()) / 2.0);
        //blinkText(levelDisplay, "white", "red", 10, 100);
    }
    
    private static void blinkText(Text text, String startColor, String blinkColor, int times, long speedInMillis) {
        final boolean opacity = blinkColor == null;
        final Paint start = Paint.valueOf(startColor);
        final Paint blink = Paint.valueOf(blinkColor);
        
        Platform.runLater(new Thread( () -> {
            for (int i = 0; i < times * 2; i++) {
                if (opacity && (text.getOpacity() > 1.0))
                    text.setOpacity(0.0);
                else if (opacity && (text.getOpacity() < 99.0))
                    text.setOpacity(100.0);
                else if (text.getFill().equals(start))
                    text.setFill(blink);
                else if (text.getFill().equals(blink))
                    text.setFill(start);
                else
                    text.setFill(start);

                try {
                    Thread.sleep(speedInMillis);
                } catch (InterruptedException ex) {
                }
            }
        }));
    }
    
    private static void setBlockPreview(char player) {
        Rectangle[][] blockPreview = null;
        Tetrimino nextTetrimino = null;
        
        if ((player == 'a') || (player == 'A')) {
            blockPreview = blockPreviewA;
            nextTetrimino = nextTetriminoA;
        }
        
        int offsetX = (blockPreview.length - Tetriminos.getWidth(nextTetrimino.getIndex(), "origin") * 2) / 2;
        int offsetY = (blockPreview.length - Tetriminos.getHeight(nextTetrimino.getIndex(), "origin") * 2) / 2;
        System.out.println("ID: " + nextTetrimino.getId() + " / OFFSET Y: " + offsetY + " / HEIGHT: " + Tetriminos.getHeight(nextTetrimino.getIndex()));
        
        int[] posX = new int[16];
        int[] posY = new int[16];
        
        for (int i = 3, j = 0; i < posX.length; i += 4, j++) {
            posX[i - 3] = offsetX + Tetriminos.getRotationX(nextTetrimino.getIndex(), "origin")[j] * 2;
            posY[i - 3] = offsetY + Tetriminos.getRotationY(nextTetrimino.getIndex(), "origin")[j] * 2;
            posX[i - 2] = posX[i - 3] + 1;
            posY[i - 2] = posY[i - 3];
            posX[i - 1] = posX[i - 3];
            posY[i - 1] = posY[i - 3] + 1;
            posX[i] = posX[i - 3] + 1;
            posY[i] = posY[i - 3] + 1;
        }
        
        clearBlockPreview(player);
        
        for (int i = 0; i < posX.length; i++) {
            for (int j = 0; j < posY.length; j++) {
                blockPreview[posX[i]][posY[i]].setFill(Tetriminos.getColor(nextTetrimino.getIndex()));
                blockPreview[posX[i]][posY[i]].setOpacity(100.0);
            }
        }
    }
    
    private static void clearBlockPreview(char player) {
        Rectangle[][] blockPreview = null;
        
        if ((player == 'a') || (player == 'A'))
            blockPreview = blockPreviewA;
        
        for (int i = 0; i < blockPreview.length; i++)
            for (int j = 0; j < blockPreview[0].length; j++)
                blockPreview[i][j].setOpacity(0.0);
    }
    
    private static int getScore(String scoreStr) {
        StringBuilder score = new StringBuilder(scoreStr);
        
        while (score.charAt(0) == '0' && score.length() > 1)
            score.deleteCharAt(0);
        
        return Integer.valueOf(score.toString());
    }
    
    private static int getScore(char player) {
        StringBuilder score = new StringBuilder();
        
        if ((player == 'a') || (player == 'A'))
            score.append(txtCurrentScoreA.getText());
        
        while (score.charAt(0) == '0' && score.length() > 1)
            score.deleteCharAt(0);
        
        return Integer.valueOf(score.toString());
    }
    
    private static void clearPattern(char player) {
        Node[][] pattern = null;
        
        if ((player == 'a') || (player == 'A')) {
            pattern = patternA;
        }
        
        for (int i = 0; i < FIELDS_HOR; i++)
            for (int j = 0; j < FIELDS_VER; j++)
                pattern[i][j].setOpacity(0.0);
    }
    
}

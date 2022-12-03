package tetris;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

public class Tetrimino {
    private final String id;
    private int index;
    private volatile int[] xPos, yPos;
    private Paint color;
    public volatile boolean rotate, moveLeft, moveRight, moveDown, inputAllowed, changeAllowed;

    private volatile int blockSpeed;
    private volatile Node[][] pattern;
    
    public Tetrimino(String id, Node [][]pattern) {
        this.id = id.toUpperCase();
        this.index = Tetriminos.indexes.get(id);
        this.pattern = pattern;

        initializeTetrimino();
    }
    
    public Tetrimino(int index, Node [][]pattern) {
        this.id = Tetriminos.ids.get(index);
        this.index = index;
        this.pattern = pattern;
        
        initializeTetrimino();
    }
    
    private void initializeTetrimino() {
        this.xPos = new int[4];
        this.yPos = new int[4];
        this.rotate = this.moveLeft = this.moveRight = this.moveDown = false;
        this.blockSpeed = Tetris.gameSpeedA;
        this.inputAllowed = false;
        this.changeAllowed = true;

        for (int i = 0; i < xPos.length; i++) {
            this.xPos[i] = Tetriminos.getRotationX(this.index, "origin")[i] + 
                    ((Tetris.FIELDS_HOR - Tetriminos.getWidth(this.index)) / 2);
            this.yPos[i] = Tetriminos.getRotationY(this.index, "origin")[i] - 
                    Tetriminos.getHeight(this.index, "origin");
        }
        
        Tetriminos.setRotation(this.index, "origin");
                
        if (Tetriminos.getWidth(this.index) % 2 == 0)
            for (int i = 0; i < this.xPos.length; i++)
                this.xPos[i]--;
        
        this.color = Tetriminos.getColor(this.index);
        
    }

    public String getId() {
        return this.id;
    }
    
    public int getIndex() {
        return this.index;
    }
    
    public void rotate(Boolean requested) {
        if (!inputAllowed)
            return;
        
        while (!changeAllowed)
            try {
                Thread.sleep(10);
            } catch(InterruptedException ex) {
            }
        
        if (requested) {
            changeAllowed = false;
            
            int originX = xPos[0];
            int originY = yPos[0];
            for (int i = 1; i < xPos.length; i++) {
                if (xPos[i] < originX)
                    originX = xPos[i];

                if (yPos[i] < originY)
                    originY = yPos[i];
            }
            
            originX -= (Tetriminos.getWidth(this.index) - Tetriminos.getWidth(this.index, "left")) / 2;
            originY -= (Tetriminos.getHeight(this.index) - Tetriminos.getHeight(this.index, "left")) / 2;

            //  avoid rotation into the right or left border (set offset in that case)
            int offset = 0;
            for (int i = 0; i < xPos.length; i++) {
                if ((originX + Tetriminos.getRotationX(this.index, "left")[i] + offset) >= Tetris.FIELDS_HOR) {
                    offset--;
                    i--;
                } else if ((originX + Tetriminos.getRotationX(this.index, "left")[i] + offset) < 0) {
                    offset++;
                    i--;
                }
            }
            
            //  write new coordinates (incl. offset) into new variable
            int[] newX = new int[4];
            int[] newY = new int[4];
            for (int i = 0; i < xPos.length; i++) {
                newX[i] = originX + Tetriminos.getRotationX(this.index, "left")[i] + offset;
                newY[i] = originY + Tetriminos.getRotationY(this.index, "left")[i];
            }
            
            //  avoid rotation into the ground
            for (int i = 0; i < yPos.length; i++) {
                if (newY[i] >= Tetris.FIELDS_VER) {
                    for (int j = 0; j < yPos.length; j++)
                        newY[j]--;
                    
                    i = 0;
                }
            }
            
            //  avoid rotation into another block
            boolean isBlocked = false;
            offset = 0;
            for (int i = 0; i < xPos.length; i++) {
                if(newY[i] < 0)
                    continue;

                isBlocked = false;
                if ((newX[i] + offset >= 0) && (newX[i] + offset < Tetris.FIELDS_HOR)) {
                    if (this.pattern[newX[i] + offset][newY[i]].getOpacity() > 1.0) {
                        for (int j = 0; j < xPos.length; j++) {
                            if (yPos[j] < 0)
                                continue;

                            if (this.pattern[newX[i] + offset][newY[i]] != this.pattern[xPos[j]][yPos[j]]) {
                                isBlocked = true;                        
                            } else {
                                isBlocked = false;
                                break;
                            }
                        }
                    }
                } else {
                    isBlocked = true;
                }

                if (isBlocked && offset < 0) {
                    changeAllowed = true;
                    return;
                } else if (isBlocked && offset == 0) {
                    System.out.println("offset to 1");
                    offset = 1;
                    i = 0;
                } else if (isBlocked && offset > 0) {
                    System.out.println("offset to -1");
                    offset = -1;
                    i = 0;
                }                    
            }
            
            System.out.println("OFFSET: " + offset + " / IS BLOCKED: " + isBlocked);
            
            if (offset != 0)
                for (int i = 0; i < newX.length; i++)
                    newX[i] += offset;
            
            //  hide old cubes
            for (int i = 0; i < xPos.length; i++) {
                if (yPos[i] < 0)
                    continue;
                
                final Rectangle old = (Rectangle) this.pattern[xPos[i]][yPos[i]];
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        old.setOpacity(0.0);
                    }
                });
            }
            
            //  write new coordinates to xPos and yPos
            for (int i = 0; i < xPos.length; i++) {
                xPos[i] = newX[i];
                yPos[i] = newY[i];
            }
            
            Tetriminos.setRotation(this.index, "left");

            //  show new cubes
            for (int i = 0; i < xPos.length; i++) {
                if (yPos[i] < 0)
                    continue;
                
                final Rectangle change = (Rectangle) this.pattern[xPos[i]][yPos[i]];
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        change.setFill(color);
                        change.setOpacity(100.0);
                    }
                });
            }
            
            changeAllowed = true;
        }
    }
    
    public void moveLeft(Boolean requested) {
        if (this.moveRight)
            this.moveRight = false;
        
        if (requested && !this.moveLeft && inputAllowed) {
            this.moveLeft = true;
            move("left");
        } else
            this.moveLeft = false;
    }
    
    public void moveRight(Boolean requested) {
        if (this.moveLeft)
            this.moveLeft = false;
        
        if (requested && !this.moveRight && inputAllowed) {
            this.moveRight = true;
            move("right");
        } else
            this.moveRight = false;
    }
    
    public void moveDown(Boolean requested) {
        if (requested && inputAllowed) {
            if (!this.moveDown) {
                this.moveDown = true;
                this.blockSpeed = Tetris.gameSpeedA;

                if (this.blockSpeed > 25)
                    this.blockSpeed = 100;
            }
        } else {
            this.moveDown = false;
            this.blockSpeed = Tetris.gameSpeedA;
        }
    }
    
    public void move(String direction) {
        Thread move = new Thread() {
            @Override
            public void run() {
                while (moveLeft || moveRight) {
                    while (!changeAllowed)
                        try {
                            sleep(10);
                        } catch(InterruptedException ex) {
                        }
                    
                    changeAllowed = false;

                    int step = 0;
                    boolean obstacle = true;

                    if (direction.equals("left")) {
                        //  check if Tetrimino hits the left border or another Tetrimino
                        for (int i = 0; i < xPos.length; i++) {
                            obstacle = true;

                            if (xPos[i] - 1 < 0) {
                                break;
                            } else if (yPos[i] < 0) {
                                obstacle = false;
                            } else if (pattern[xPos[i] - 1][yPos[i]].getOpacity() > 1.0) {
                                for (int j = 0; j < xPos.length; j++) {
                                    if (yPos[j] < 0) {
                                        obstacle = false;
                                    } else if (pattern[xPos[j]][yPos[j]] == pattern[xPos[i] - 1][yPos[i]]) {
                                        obstacle = false;
                                    }
                                }
                                
                                if (obstacle)
                                    break;
                            } else {
                                obstacle = false;
                            }
                        }
                        
                        if (obstacle) {
                            changeAllowed = true;
                            
                            try {
                                sleep(10);
                            } catch (InterruptedException ex) {
                            }
                            
                            moveLeft = false;
                            return;
                        }

                        step = -1;
                    } else if (direction.equals("right")) {
                        //  check if Tetrimino hits the right border or another Tetrimino
                        for (int i = 0; i < xPos.length; i++) {
                            obstacle = true;
                        
                            if (xPos[i] + 1 >= Tetris.FIELDS_HOR) {
                                break;
                            } else if (yPos[i] < 0) {
                                obstacle = false;
                            } else if (pattern[xPos[i] + 1][yPos[i]].getOpacity() > 1.0) {
                                for (int j = 0; j < xPos.length; j++) {
                                    if (yPos[j] < 0) {
                                        obstacle = false;
                                    } else if (pattern[xPos[j]][yPos[j]] == pattern[xPos[i] + 1][yPos[i]]) {
                                        obstacle = false;
                                    }
                                }
                                
                                if (obstacle)
                                    break;
                            } else {
                                obstacle = false;
                            }
                        }
                        
                        if (obstacle) {
                            changeAllowed = true;
                            
                            try {
                                sleep(10);
                            } catch (InterruptedException ex) {
                            }
                            
                            moveRight = false;
                            return;
                        }
                        
                        step = 1;
                    }

                    //  hide old cubes
                    for (int i = 0; i < xPos.length; i++) {
                        if (yPos[i] < 0)
                            continue;
                        
                        final Rectangle old = (Rectangle) pattern[xPos[i]][yPos[i]];

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                old.setOpacity(0.0);
                            }
                        });
                    }

                    //  show new cubes
                    for (int i = 0; i < xPos.length; i++) {
                        xPos[i] += step;
                        
                        if (yPos[i] < 0)
                            continue;
                        
                        final Rectangle change = (Rectangle) pattern[xPos[i]][yPos[i]];

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                change.setFill(color);
                                change.setOpacity(100.0);
                            }
                        });
                    }
                    
                    changeAllowed = true;
                    
                    try {
                        sleep(100);
                    } catch (InterruptedException ex) {
                    }
                }
            }
        };
        
        move.setDaemon(true);
        move.start();
    }
    
    public synchronized void drop() {
        while (true) {
            try {
                while (!changeAllowed)
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException ex) {
                    }
                
                changeAllowed = false;
                for (int i = 0; i < xPos.length; i++)
                    System.out.println("\"" + this.id + "\" 1. POSITIONS: xPos: " + xPos[i] + " / yPos: " + yPos[i]);
                
                //  hide old cubes
                if (!isBlocked()) {
                    for (int i = 0; i < yPos.length; i++) {
                        if (yPos[i] >= 0) {
                            final Node old = pattern[xPos[i]][yPos[i]];
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    old.setOpacity(0.0);
                                }
                            });
                        }

                        yPos[i]++;
                    }

                    boolean rowVisible = false;
                    //  show new cubes
                    for (int i = 0; i < yPos.length; i++) {
                        if (yPos[i] >= 0) {
                            rowVisible = true;
                            
                            final Rectangle change = (Rectangle) pattern[xPos[i]][yPos[i]];
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    change.setFill(color);
                                    change.setOpacity(100.0);
                                }
                            });
                        }
                        System.out.println("\"" + this.id + "\" 2. POSITIONS: xPos: " + xPos[i] + " / yPos: " + yPos[i]);
                    }
                    
                    if (rowVisible)
                        inputAllowed = true;
                    
                    changeAllowed = true;

                    for (int i = 0; i < 10; i++)
                        Thread.sleep(this.blockSpeed / 10);
                } else {
                    boolean isBlocked = false;
                    
                    for (int i = 0; i < yPos.length; i++) {
                        if (yPos[i] + 1 < 0) {
                            continue;
                        } else if (yPos[i] + 1 >= Tetris.FIELDS_VER) {
                            isBlocked = true;
                            break;
                        } else if (pattern[xPos[i]][yPos[i] + 1].getOpacity() > 1.0) {
                            isBlocked = true;
                            
                            for (int j = 0; j < yPos.length; j++) {
                                if (yPos[j] < 0) {
                                    continue;
                                } else if (pattern[xPos[i]][yPos[i] + 1] == pattern[xPos[j]][yPos[j]]) {
                                    isBlocked = false;
                                    break;
                                }
                            }
                            
                            if (isBlocked)
                                break;
                        }
                            
                    }
                    
                    if (!isBlocked)
                        continue;
                        
                    inputAllowed = false;
                    clearLines();
                    return;
                }
            } catch (InterruptedException ex) {
            }
        }
    }
    
    private boolean isBlocked() {
        for (int i = 0; i < yPos.length; i++) {
            if (yPos[i] + 1 >= Tetris.FIELDS_VER)
                return true;
            
            if ((xPos[i] < 0) || (yPos[i] + 1 < 0))
                continue;
            System.out.println("xPos: " + xPos[i] + " / yPos: " + yPos[i]);
            if (pattern[xPos[i]][yPos[i] + 1].getOpacity() > 1.0) {
                boolean obstacle = true;
                
                for (int j = 0; j < yPos.length; j++) {
                    if (yPos[j] < 0)
                        continue;
                    
                    if (pattern[xPos[j]][yPos[j]] == pattern[xPos[i]][yPos[i] + 1])
                        obstacle = false;
                }
                
                if (obstacle) {
                    lockUserInput();
                    return true;
                }
            }
        }
        
        return false;
    }
    private int clearLines() {
        boolean clear;
        ArrayList<Integer> clearedLines = new ArrayList();
        
        for (int i = 0; i < Tetris.FIELDS_VER; i++) {
            clear = true;
            
            for (int j = 0; j < Tetris.FIELDS_HOR; j++) {
                if (this.pattern[j][i].getOpacity() < 99.0) {
                    clear = false;
                    break;
                }
            }
            
            if (clear)
                clearedLines.add(i);
        }
        
        if (clearedLines.isEmpty())
            return 0;
        
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Tetris.setScore('a', clearedLines.size());
            }
        });
        
        linesBlink(clearedLines);
        
        int lineOffset;
        boolean clearFirst = false;
        for (int i = 0; i < clearedLines.size(); i++) {                         //  cleared lines
            for (int j = clearedLines.get(i); j > 0; j--) {                     //  lines
                for (int k = 0; k < Tetris.FIELDS_HOR; k++) {                   //  columns
                    ((Rectangle) this.pattern[k][j]).setFill(((Rectangle) this.pattern[k][j - 1]).getFill());
                    this.pattern[k][j].setOpacity(this.pattern[k][j - 1].getOpacity());
                }
            }
        }
        
        for (int i = 0; i < Tetris.FIELDS_HOR; i++)                             //  if first line gets cleared
            this.pattern[i][0].setOpacity(0.0);
        
        return clearedLines.size();
    }
    
    private void linesBlink(ArrayList<Integer> lines) {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < lines.size(); j++)
                for (int k = 0; k < Tetris.FIELDS_HOR; k++)
                    if (this.pattern[k][lines.get(j)].getOpacity() > 1.0)
                        this.pattern[k][lines.get(j)].setOpacity(0.0);
                    else
                        this.pattern[k][lines.get(j)].setOpacity(100.0);
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Logger.getLogger(Tetrimino.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void lockUserInput() {
        this.inputAllowed = false;
        this.moveLeft = false;
        this.moveRight = false;
        this.moveDown = false;
    }
        
}

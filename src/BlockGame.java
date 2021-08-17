import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class BlockGame {
    static class MyFrame extends JFrame {

        //constant 상수
        static int BALL_WIDTH = 20;
        static int BALL_HEIGHT = 20;
        static int BLOCK_ROWS = 5;
        static int BLOCK_COLUMNS = 10;
        static int BLOCK_WIDTH = 40;
        static int BLOCK_HEIGHT = 20;
        static int BLOCK_GAP = 3; //블록 간의 간격
        static int BAR_WIDTH = 80;
        static int BAR_HEIGHT = 20;
        static int CANVAS_WIDTH = 400 + (BLOCK_GAP * BLOCK_COLUMNS) - BLOCK_GAP;
        static int CANVAS_HEIGHT = 600;

        //variable 변수
        static MyPanel myPanel = null;
        static int score = 0;
        static Timer timer = null;
        static Block[][] blocks = new Block[BLOCK_ROWS][BLOCK_COLUMNS]; //공간 생성 //밑의 for문 돌면서 채워줌.
        static Bar bar = new Bar();
        static Ball ball = new Ball();
        static int barXTarget = bar.x; //Target Value - interpolation
        static int dir = 0; //움직이는 방향 // 0:Up-Right 1:Down-Right 2:Up-Left 4:Down-Left
        static int ballSpeed = 5;
        static boolean isGameFinish = false;


        static class Ball {
            int x = (CANVAS_WIDTH - BALL_WIDTH) / 2;
            int y = (CANVAS_HEIGHT - BALL_HEIGHT) / 2;
            ;
            int width = BALL_WIDTH;
            int height = BALL_HEIGHT;

            Point getCenter() {
                return new Point(x + (BALL_WIDTH / 2), y + (BALL_HEIGHT) / 2);
            }

            Point getBottomCenter() {
                return new Point(x + (BALL_WIDTH / 2), y + (BALL_HEIGHT));
            }

            Point getTopCenter() {
                return new Point(x + (BALL_WIDTH) / 2, y);
            }

            Point getLeftCenter() {
                return new Point(x, y + (BALL_HEIGHT) / 2);
            }

            Point getRightCenter() {
                return new Point(x + (BALL_WIDTH) / 2, y + (BALL_HEIGHT) / 2);
            }
        }

        static class Bar {
            int x = (CANVAS_WIDTH - BAR_WIDTH) / 2;
            int y = CANVAS_HEIGHT - 100;
            int width = BAR_WIDTH;
            int height = BAR_HEIGHT;
        }

        static class Block {
            int x = 0;
            int y = 0;
            int width = BLOCK_WIDTH;
            int height = BLOCK_HEIGHT;
            int color = 0; //0:white, 1:yellow, 2:blue, 3:mazanta, 4:red
            boolean isHidden = false; //after collision, blck will be hidden. 충돌 후에 블록 사라짐.
        }

        static class MyPanel extends JPanel { //canvas for Draw!
            public MyPanel() {
                this.setSize(CANVAS_WIDTH, CANVAS_HEIGHT);
                this.setBackground(Color.BLACK);
            }

            @Override //jpanel에서 가져오는 오버라이드 메서드 //이미 지정된거 가져와서 씀.
            public void paint(Graphics g) {
                super.paint(g);
                Graphics g2d = (Graphics2D) g;

                drawUI(g2d);
            }

            private void drawUI(Graphics g2d) {
                //draw Blocks
                for (int i = 0; i < BLOCK_ROWS; i++) {
                    for (int j = 0; j < BLOCK_COLUMNS; j++) {
                        if (blocks[i][j].isHidden) {
                            continue;
                        }
                        if (blocks[i][j].color == 0) {
                            g2d.setColor(Color.WHITE);
                        } else if ((blocks[i][j].color == 1)) {
                            g2d.setColor(Color.YELLOW);
                        } else if ((blocks[i][j].color == 2)) {
                            g2d.setColor(Color.BLUE);
                        } else if ((blocks[i][j].color == 3)) {
                            g2d.setColor(Color.MAGENTA);
                        } else if ((blocks[i][j].color == 4)) {
                            g2d.setColor(Color.RED);
                        }
                        g2d.fillRect(blocks[i][j].x, blocks[i][j].y, blocks[i][j].width, blocks[i][j].height);
                    }

                    //draw score
                    g2d.setColor(Color.WHITE);
                    g2d.setFont(new Font("TimesRoman", Font.BOLD, 20));
                    g2d.drawString("score : " + score, CANVAS_WIDTH / 2 - 30, 30);

                    if(isGameFinish) {
                        g2d.setColor(Color.RED);
                        g2d.drawString("score : " + score + " Game Finished!", CANVAS_WIDTH / 2 - 55, 50);
                    }
                    //draw Ball
                    g2d.setColor(Color.WHITE);
                    g2d.fillOval(ball.x, ball.y, BALL_WIDTH, BLOCK_HEIGHT);

                    //draw Bar
                    g2d.setColor(Color.WHITE);
                    g2d.fillRect(bar.x, bar.y, bar.width, bar.height);
                }

            }
        }


        public MyFrame(String title) {
            super(title);
            this.setVisible(true);
            this.setSize(CANVAS_WIDTH, CANVAS_HEIGHT);
            this.setLocation(400, 300); //창이 젤 왼쪽 젤 위에 뜨지 않고 간격 띄워서 창 표시
            this.setLayout(new BorderLayout());
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            initData(); //변수들을 초기화함.

            myPanel = new MyPanel();
            this.add("Center", myPanel);

            setKeyListener();
            startTimer();

        }

        public void initData() {
            for (int i = 0; i < BLOCK_ROWS; i++) {
                for (int j = 0; j < BLOCK_COLUMNS; j++) {
                    blocks[i][j] = new Block();
                    blocks[i][j].x = (BLOCK_WIDTH + BLOCK_GAP) * j;
                    blocks[i][j].y = 100 + (BLOCK_HEIGHT + BLOCK_GAP) * i; //왜냐? i가 세로니까
                    blocks[i][j].width = BLOCK_WIDTH;
                    blocks[i][j].height = BLOCK_HEIGHT;
                    blocks[i][j].color = 4 - i; //0:white, 1:yellow, 2:blue, 3:mazanta, 4:red
                    blocks[i][j].isHidden = false;


                }
            }
        }

        public void setKeyListener() {
            this.addKeyListener(new KeyAdapter() { //JFrame에서 갖고있는 함수
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                        System.out.println("Pressed Left Key");
                        barXTarget -= 20;
                        if (bar.x < barXTarget) { //repeat key pressed... 계속 누르는 경우
                            barXTarget = bar.x; //현재 값으로 초기화
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                        System.out.println("Pressed Right Key");
                        barXTarget += 20;
                        if (bar.x > barXTarget) { //repeat key pressed... 계속 누르는 경우
                            barXTarget = bar.x; //현재 값으로 초기화
                        }
                    }
                }
            });
        }

        public void startTimer() {
            timer = new Timer(20, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) { //Timer Event
                    movement();
                    checkCollision(); //충돌처리 //Wall,Bar
                    checkCollisionBlock(); //Blocks 50개의 블럭
                    myPanel.repaint(); //바뀌고 UI 다시 칠해줌. //Redraw!

                    isGameFinish();
                }
            }); //java 기본 타이머 x / swing에서 제공해줌.
            timer.start(); //Start Timer!
        }
        public void isGameFinish() {
            //Game Success!
            int count = 0;
            for(int i=0; i<BLOCK_ROWS; i++) {
                for(int j=0; j<BLOCK_COLUMNS; j++) {
                    Block block = blocks[i][j];
                    if(block.isHidden)
                        count++;
                }
            }
            if(count == BLOCK_ROWS * BLOCK_COLUMNS) {
                //Game Finished!
                //timer.stop(); //타이머 스탑하면 게임 더 이상 돌지 않음.
                isGameFinish = true;
            }
        }
        public void movement() {
            if (bar.x < barXTarget) {
                bar.x += 5;
            } else if (bar.x > barXTarget) {
                bar.x -= 5;
            }

            if (dir == 0) { //0 : Up-Right
                ball.x += ballSpeed;
                ball.y -= ballSpeed;
            } else if (dir == 1) { //1 : Down-Right
                ball.x += ballSpeed;
                ball.y += ballSpeed;
            } else if (dir == 2) { //2 : Up-Left
                ball.x -= ballSpeed;
                ball.y -= ballSpeed;
            } else if (dir == 3) { //3 : Down-Left
                ball.x -= ballSpeed;
                ball.y += ballSpeed;
            }
        }

        public boolean duplRect(Rectangle rect1, Rectangle rect2) {
            return rect1.intersects(rect2); //충돌여부 체크해주는 함수 //check two Rect is Duplicated!
        }

        public void checkCollision() { //벽에 부딪혔을때
            if (dir == 0) { //0 : Up-Right
                //Wall
                if (ball.y < 0) { //wall upper
                    dir = 1;
                }
                if (ball.x > CANVAS_WIDTH - BALL_WIDTH) { //wall right
                    dir = 2;
                }
                //Bar-none
            } else if (dir == 1) { //1 : Down-Right
                //Wall
                if (ball.y > CANVAS_HEIGHT - BALL_HEIGHT - BALL_HEIGHT) { //wall bottom
                    dir = 0;

                    //game reset
                    dir = 0;
                    ball.x = (CANVAS_WIDTH - BALL_WIDTH)/2;
                    ball.y = (CANVAS_HEIGHT - BALL_HEIGHT)/2;
                    score = 0;
                }
                if (ball.x > CANVAS_WIDTH - BALL_WIDTH) { //wall right
                    dir = 3;
                }
                //Bar
                if (ball.getBottomCenter().y >= bar.y) {
                    if (duplRect(new Rectangle(ball.x, ball.y, ball.width, ball.height),
                            new Rectangle(bar.x, bar.y, bar.width, bar.height))) { //충돌

                        dir = 0;
                    }
                }
            } else if (dir == 2) { //2 : Up-Left
                //Wall
                if (ball.y < 0) { //wall upper
                    dir = 3;
                }
                if (ball.x < 0) { //wall left
                    dir = 0;
                }
                //Bar-non
            } else if (dir == 3) { //3 : Down-Left
                //Wall
                if (ball.y > CANVAS_HEIGHT - BALL_HEIGHT - BALL_HEIGHT) { //wall bottom
                    dir = 2;

                    //game reset
                    dir = 0;
                    ball.x = (CANVAS_WIDTH - BALL_WIDTH)/2;
                    ball.y = (CANVAS_HEIGHT - BALL_HEIGHT)/2;
                    score = 0;

                }
                if (ball.x < 0) { //wall left
                    dir = 1;
                }
                //Bar
                if (ball.getBottomCenter().y >= bar.y) {
                    if (duplRect(new Rectangle(ball.x, ball.y, ball.width, ball.height),
                            new Rectangle(bar.x, bar.y, bar.width, bar.height))) { //충돌

                        dir = 2;
                    }
                }
            }
        }

        public void checkCollisionBlock() {
            // 0:Up-Right 1:Down-Right 2:Up-Left 3:Down-Left
            for (int i = 0; i < BLOCK_ROWS; i++) {
                for (int j = 0; j < BLOCK_COLUMNS; j++) {
                    Block block = blocks[i][j];
                    if (block.isHidden == false) {
                        if (dir == 0) { // 0:Up-Right
                            if (duplRect(new Rectangle(ball.x, ball.y, ball.width, ball.height),
                                    new Rectangle(block.x, block.y, block.width, block.height))) { //충돌
                                if (ball.x > block.x + 2 &&
                                        ball.getRightCenter().x <= block.x + block.width - 2) { //좀 안쪽으로 부딪혀도 되도록 +2 해줌. 왜냐? 원형이니까!
                                    //block bottom collision
                                    dir = 1;
                                } else {
                                    //block left collision
                                    dir = 2;
                                }
                                block.isHidden = true;
                                if(block.color==0) {
                                    score += 10;
                                }else if(block.color==1) {
                                    score += 20;
                                }else if(block.color==2) {
                                    score += 30;
                                }else if(block.color==3) {
                                    score += 40;
                                }else if(block.color==4) {
                                    score += 50;
                                }
                            }
                        } else if (dir == 1) { // 1:Down-Right
                            if (duplRect(new Rectangle(ball.x, ball.y, ball.width, ball.height),
                                    new Rectangle(block.x, block.y, block.width, block.height))) { //충돌
                                if (ball.x > block.x + 2 &&
                                        ball.getRightCenter().x <= block.x + block.width - 2) { //좀 안쪽으로 부딪혀도 되도록 +2 해줌. 왜냐? 원형이니까!
                                    //block top collision
                                    dir = 0;
                                } else {
                                    //block left collision
                                    dir = 3;
                                }
                                block.isHidden = true;
                            }
                        } else if (dir == 2) { // 2:Up-Left
                            if (duplRect(new Rectangle(ball.x, ball.y, ball.width, ball.height),
                                    new Rectangle(block.x, block.y, block.width, block.height))) { //충돌
                                if (ball.x > block.x + 2 &&
                                        ball.getRightCenter().x <= block.x + block.width - 2) { //좀 안쪽으로 부딪혀도 되도록 +2 해줌. 왜냐? 원형이니까!
                                    //block bottom collision
                                    dir = 3;
                                } else {
                                    //block right collision
                                    dir = 0;
                                }
                                block.isHidden = true;
                            }
                        } else if (dir == 3) { // 3:Down-Left
                            if (duplRect(new Rectangle(ball.x, ball.y, ball.width, ball.height),
                                    new Rectangle(block.x, block.y, block.width, block.height))) { //충돌
                                if (ball.x > block.x + 2 &&
                                        ball.getRightCenter().x <= block.x + block.width - 2) { //좀 안쪽으로 부딪혀도 되도록 +2 해줌. 왜냐? 원형이니까!
                                    //block top collision
                                    dir = 2;
                                } else {
                                    //block right collision
                                    dir = 1;
                                }
                                block.isHidden = true;
                            }
                        }
                    }
                }
            }
        }
    }
    public static void main(String[] args) {
        new MyFrame("Block Game");
    }
}

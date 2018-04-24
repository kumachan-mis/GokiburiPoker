import processing.core.PApplet;
import processing.core.PImage;

class ButtonTemplate{
    private int WIDTH;
    private int HEIGHT;
    private int INSECTNUM;

    private int numOfButton = 0;
    private int buttonY = 0;
    private int interval = 0;
    private int buttonWidth = 0;
    private int buttonHeight = 0;
    private GUIClient pa;

    public ButtonTemplate(GUIClient pa, int INSECTNUM){
        this.pa = pa;
        WIDTH = pa.WIDTH;
        HEIGHT = pa.HEIGHT;
        this.INSECTNUM = INSECTNUM;
    }

    public void setButtonData(int numOfButton, int buttonY, int interval, int buttonWidth, int buttonHeight){
        this.numOfButton = numOfButton;
        this.buttonY = buttonY;
        this.interval = interval;
        this.buttonWidth = buttonWidth;
        this.buttonHeight = buttonHeight;
    }

    public void resetButtondata(){
        numOfButton = 0;
        buttonY = 0;
        interval = 0;
        buttonWidth = 0;
        buttonHeight = 0;
    }

    public void createButtonRect(){
        int buttonX;
        pa.strokeWeight(1);
        pa.fill(255);
        pa.rectMode(pa.CENTER);
        int mid = numOfButton / 2;

        for(int i = 0; i < numOfButton; ++i){
            if(numOfButton % 2 == 0){
                buttonX =
                (buttonWidth + interval) * (i - numOfButton / 2)
                + buttonWidth / 2 - interval / 2 + WIDTH / 2;
            }else{
                buttonX = (buttonWidth + interval) * (i - mid) + WIDTH / 2;
            }

            pa.rect(buttonX, buttonY, buttonWidth, buttonHeight);
        }
    }

    public void createButtonImage(){
        int buttonX;
        pa.strokeWeight(1);
        pa.imageMode(pa.CENTER);
        int mid = INSECTNUM / 2;

        for(int i = 0; i < INSECTNUM; ++i){
            PImage img = pa.loadImage("Images/" + GameShow.insectsImage[i]);

            if(INSECTNUM % 2 == 0){
                buttonX =
                (buttonWidth + interval) * (i - INSECTNUM / 2)
                + buttonWidth / 2 - interval / 2 + WIDTH / 2;

            }else{
                buttonX = (buttonWidth + interval) * (i - mid) + WIDTH / 2;
            }
            
            pa.image(img, buttonX, buttonY, buttonWidth, buttonHeight);
        }
    }

    public void createButtonText(String[] texts){
        int buttonX;

        pa.strokeWeight(1);
		pa.fill(0);
        pa.textAlign(pa.CENTER, pa.CENTER);
        pa.textSize(30);
        
        int mid = numOfButton / 2;

        for(int i = 0; i < numOfButton; ++i){
            if(numOfButton % 2 == 0){
                buttonX =
                (buttonWidth + interval) * (i - numOfButton / 2)
                + buttonWidth / 2 - interval / 2 + WIDTH / 2;
            }else{
                buttonX = (buttonWidth + interval) * (i - mid) + WIDTH / 2;
            }

            pa.text(texts[i], buttonX, buttonY, buttonWidth, buttonHeight);
        }
    }
    
    public void createCross(boolean[] choosables){  // Kimura: 選択できないカードに×を表示
        int buttonX;
        pa.strokeWeight(3);
        pa.fill(0);
        int mid = numOfButton / 2;

        for(int i = 0; i < numOfButton; ++i){
            if(numOfButton % 2 == 0){
                buttonX =
                (buttonWidth + interval) * (i - numOfButton / 2)
                + buttonWidth / 2 - interval / 2 + WIDTH / 2;
            }else{
                buttonX = (buttonWidth + interval) * (i - mid) + WIDTH / 2;
            }

            if(choosables[i] == false){
                pa.line(buttonX - buttonWidth / 2, buttonY - buttonHeight / 2,
                buttonX + buttonWidth / 2, buttonY + buttonHeight / 2);
                pa.line(buttonX + buttonWidth / 2, buttonY - buttonHeight / 2,
                buttonX - buttonWidth / 2, buttonY + buttonHeight / 2);
            }
        }
    }

    public int releasedAction(Action action, int phase, boolean backActive){
        int ret = phase;
        int mid = numOfButton / 2;
        int mouseX = pa.mouseX;
        int mouseY = pa.mouseY;

        if(backActive &&
        mouseX >= WIDTH * 15 / 18 && mouseX <= WIDTH * 17 / 18 &&
        mouseY >= HEIGHT / 60 && mouseY <= HEIGHT / 20){
            ret--;
            
         }else if(numOfButton % 2 == 0){
            for(int i = 0; i < numOfButton; ++i){
                int buttonX =
                (buttonWidth + interval) * (i - numOfButton / 2)
                + buttonWidth / 2 - interval / 2 + WIDTH / 2;

                if(mouseX >= buttonX - buttonWidth / 2 && mouseX <= buttonX + buttonWidth / 2 &&
                mouseY >= buttonY - buttonHeight / 2 && mouseY <= buttonY + buttonHeight / 2){
                    ret = action.action(i);
                    break;
                }
            }
        }else{
            for(int i = 0; i < numOfButton; ++i){
                int buttonX = (buttonWidth + interval) * (i - mid) + WIDTH / 2;

                if(mouseX >= buttonX - buttonWidth / 2 && mouseX <= buttonX + buttonWidth / 2 &&
                mouseY >= buttonY - buttonHeight / 2 && mouseY <= buttonY + buttonHeight / 2){
                    ret = action.action(i);
                    break;
                }
            }
        }
        return ret;
    }
    
}
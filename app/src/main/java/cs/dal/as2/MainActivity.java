package cs.dal.as2;

/*
CSCi 5708 Mobile Computing
Assignment 2 minesweeper
Fangye Tang
B00612172
 */

import android.content.Context;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private GridView gv;
    private Integer[] items=new Integer[81];//used to store the status for each cell
    private Boolean[] visible=new Boolean[81];//used to store this cell is visible or not
    private Boolean[] flag=new Boolean[81];//used to store this cell is flaged or not

    private int num_mines,level=8;//num_mines used to store how many remaining mines and showed in left-top,
                                 //level used to store how many mines in this level: beginner has 8 mines, intermediate has 25 mines, advanced has 40
    private Boolean is_died=false;//used to check player is died or not
    private Button check;//a button that when player click, it will check win or lose
    private ImageView face,setting;//setting is a imageview button, let player can choose level, face is just a imageview
    private TextView beginner,intermediate,advanced;//three textview to store best time for three levels

    private Boolean start=false;//record game is start or not

    private Chronometer timer;//a Chronometer class to record time
    private int min,sec;//to stroe minutes and seconds from above timer. change to int

    private String best_b="99:99",best_i="99:99",best_a="99.99";//store the best score for three level

    private CustomGridAdapter gridAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //give value to required variables
        beginner=(TextView)this.findViewById(R.id.beginner);
        intermediate=(TextView)this.findViewById(R.id.intermediate);
        advanced=(TextView)this.findViewById(R.id.advanced);
        timer=(Chronometer)this.findViewById(R.id.timer);
        face=(ImageView)this.findViewById(R.id.face);
        setting=(ImageView)this.findViewById(R.id.setting);
        check=(Button)this.findViewById(R.id.check);
        gv= (GridView) this.findViewById(R.id.mygrid);
        gridAdapter=new CustomGridAdapter(MainActivity.this,items,visible,flag);
        gv.setAdapter(gridAdapter);

        //first initialize before start game
        for(int i=0;i<=80;i++){
            items[i]=0;
            visible[i]=false;
            flag[i]=false;
        }
        timer.setBase(SystemClock.elapsedRealtime());
        beginner.setText("Beginner:"+best_b);
        intermediate.setText("Intermediate:"+best_i);
        advanced.setText("Advanced:"+best_b);

        //some buttons click listener
        //click the cell, it might be a mine or number
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (!is_died) {//only works when not died
                    //first click will never be a mine, grid will initialize after click
                    if (!start) {
                        init_mine(i);//initialize game based on level
                        draw_mines_count();//shows number of mines
                        timer.setBase(SystemClock.elapsedRealtime());
                        timer.start();//start timer
                        gridAdapter.notifyDataSetChanged();
                    }
                    //if this click hit a mine
                    if (items[i] == 20 && !visible[i] && !flag[i]) {//make sure this cell is not visible and not be flaged
                        visible[i] = true;
                        items[i] = 30;//set this cell as hit mines
                        is_died = true;
                        game_over();//call game over function
                    } else if (!flag[i]) {// the cell is flaged cannot be click
                        open(i);//open this cell by call open() function

                    }
                    draw_mines_count();//every hit may change num of mines, we need to change mines UI
                    gridAdapter.notifyDataSetChanged();
                }
            }
        });

        //long click to set flag on current cell
        gv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(start&&!is_died) {//long click only worked when game start
                    //player can only set a flag when this cell is not flaged and not visible
                    if (!flag[i] && !visible[i]) {
                        flag[i] = true;
                        num_mines--;//every flag will cause remaining number of mines decrease
                    } else if (flag[i]) {//cancel flag by reLongClick this cell
                        flag[i] = false;
                        visible[i] = false;
                        num_mines++;//add back remaining number of mines
                    }
                    draw_mines_count();//remaing number of mines changed
                    gridAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });


        //check button listener
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //make sure player can check only when num of mines is nonnegative
                if(num_mines<0){
                    Context context = getApplicationContext();
                    CharSequence text = "Please check your num of mines!";
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }
                //if not died check player win or lose
                if(!is_died&&start) {
                    check();//function to check play win or lose
                    start=false;
                    timer.stop();//stop timer
                    check.setText("Restart");
                    gridAdapter.notifyDataSetChanged();
                }
                else{//means player is died, check button will be a restart button
                    is_died=false;
                    check.setText("Check");
                    start=false;
                    //reinitialize game
                    for(int i=0;i<=80;i++){
                        items[i]=0;
                        visible[i]=false;
                        flag[i]=false;
                    }
                    face.setImageLevel(0);
                    timer.setBase(SystemClock.elapsedRealtime());//set timer to 0
                    gridAdapter.notifyDataSetChanged();
                }
            }
        });

        //setting listener, will give a popupmenu to choose level
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(setting);
            }
        });

        //timer tick listener, every seconds will change timer UI
        timer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                getTimer();//get minutes and seconds from timer
                draw_timer_count();//set timer UI
                gridAdapter.notifyDataSetChanged();
            }
        });

    }

    //initialize game
    private void init_mine(int p){
        //first initialize gridview, because gridview might have items before player win or lose
        for(int i=0;i<=80;i++){
            items[i]=0;
            visible[i]=false;
            flag[i]=false;
        }
        start=true;
        face.setImageLevel(0);

        //random choose several mines and set on grid
        Random r=new Random();
        num_mines=1;
        while(num_mines<=level){//initialize mines' position
            int position=r.nextInt(80);
            if(items[position]!=20&&position!=p){//make sure your first step is not a mine
                items[position]=20;
                num_mines++;
            }
        }
        num_mines--;

        //count how many mines around this cell
        for(int i=0;i<=80;i++){
            if(items[i]!=20){//if this cell is not a mine
                int count=0;
                if(i%9!=0&&(i+1)%9!=0) {//if cell is not at left/right column
                    if(i-10>=0)
                        if (items[i - 10] == 20)//check if its top-left is mine or not
                            count++;
                    if(i-9>=0)
                        if (items[i - 9] == 20)//check if its top is mine or not
                            count++;
                    if(i-8>=0)
                        if (items[i - 8] == 20)//check if its top-right is mine or not
                            count++;
                    if(i-1>=0)
                        if (items[i - 1] == 20)//check if its left is mine or not
                            count++;
                    if(i+1<=80)
                        if (items[i + 1] == 20)//check if its right is mine or not
                            count++;
                    if(i+8<=80)
                        if (items[i + 8] == 20)//check if its bottom-left is mine or not
                            count++;
                    if(i+9<=80)
                        if (items[i + 9] == 20)//check if its bottom is mine or not
                            count++;
                    if(i+10<=80)
                        if (items[i + 10] == 20)//check if its bottom-right is mine or not
                            count++;
                }
                else if((i)%9==0){//the cell is at left column
                    if(i-9>=0)
                        if (items[i - 9] == 20)//check if its top is mine or not
                            count++;
                    if(i-8>=0)
                        if (items[i - 8] == 20)//check if its top-right is mine or not
                            count++;
                    if(i+1<=80)
                        if (items[i + 1] == 20)//check if its right is mine or not
                            count++;
                    if(i+9<=80)
                        if (items[i + 9] == 20)//check if its bottom is mine or not
                            count++;
                    if(i+10<=80)
                        if (items[i + 10] == 20)//check if its bottom-right is mine or not
                            count++;
                }
                else{//the cell is at right column
                    if(i-10>=0)
                        if (items[i - 10] == 20)//check if its top-left is mine or not
                            count++;
                    if(i-9>=0)
                        if (items[i - 9] == 20)//check if its top is mine or not
                            count++;
                    if(i-1>=0)
                        if (items[i - 1] == 20)//check if its left is mine or not
                            count++;
                    if(i+8<=80)
                        if (items[i + 8] == 20)//check if its bottom-left is mine or not
                            count++;
                    if(i+9<=80)
                        if (items[i + 9] == 20)//check if its bottom is mine or not
                            count++;
                }
                items[i]=count;
            }
        }
    }

    //draw remaining number of mines UI
    private void draw_mines_count(){
        //three imageview to shows hundreds, tens and digits for remaining number of mines
        ImageView mines_hundred=(ImageView) this.findViewById(R.id.mines_hundred);
        ImageView mines_ten=(ImageView) this.findViewById(R.id.mines_ten);
        ImageView mines_bit=(ImageView) this.findViewById(R.id.mines_bit);

        int ten=0,bit=0;

        if(num_mines>=0) {//if remaining number of mines is greater than 0
            int hundred = num_mines / 100;
            ten = (num_mines - hundred * 100) / 10;
            bit = (num_mines - hundred * 100 - ten * 10);

            switch (hundred) {//draw first bit
                case 0:mines_hundred.setImageLevel(0);break;
                case 1:mines_hundred.setImageLevel(1);break;
                case 2:mines_hundred.setImageLevel(2);break;
                case 3:mines_hundred.setImageLevel(3);break;
                case 4:mines_hundred.setImageLevel(4);break;
                case 5:mines_hundred.setImageLevel(5);break;
                case 6:mines_hundred.setImageLevel(6);break;
                case 7:mines_hundred.setImageLevel(7);break;
                case 8:mines_hundred.setImageLevel(8);break;
                case 9:mines_hundred.setImageLevel(9);break;
            }
        }
        else{//if remaining number of mines is negative
            mines_hundred.setImageLevel(10);//hundreds will be a minus sign
            if(num_mines>=-9)
                bit=-num_mines;
            else{
                ten=-num_mines/10;
                bit=(-num_mines-ten*10);
            }
        }

        switch(ten){//draw second bit
            case 0:mines_ten.setImageLevel(0);break;
            case 1:mines_ten.setImageLevel(1);break;
            case 2:mines_ten.setImageLevel(2);break;
            case 3:mines_ten.setImageLevel(3);break;
            case 4:mines_ten.setImageLevel(4);break;
            case 5:mines_ten.setImageLevel(5);break;
            case 6:mines_ten.setImageLevel(6);break;
            case 7:mines_ten.setImageLevel(7);break;
            case 8:mines_ten.setImageLevel(8);break;
            case 9:mines_ten.setImageLevel(9);break;
        }

        switch(bit){//draw last bit
            case 0:mines_bit.setImageLevel(0);break;
            case 1:mines_bit.setImageLevel(1);break;
            case 2:mines_bit.setImageLevel(2);break;
            case 3:mines_bit.setImageLevel(3);break;
            case 4:mines_bit.setImageLevel(4);break;
            case 5:mines_bit.setImageLevel(5);break;
            case 6:mines_bit.setImageLevel(6);break;
            case 7:mines_bit.setImageLevel(7);break;
            case 8:mines_bit.setImageLevel(8);break;
            case 9:mines_bit.setImageLevel(9);break;
        }
    }

    //set timer UI
    private void draw_timer_count(){
        //four imageview to shows minutes and seconds
        ImageView time_kilobit=(ImageView) this.findViewById(R.id.time_kilobit);
        ImageView time_hundred=(ImageView) this.findViewById(R.id.time_hundred);
        ImageView time_ten=(ImageView) this.findViewById(R.id.time_ten);
        ImageView time_bit=(ImageView) this.findViewById(R.id.time_bit);

        //get every bits
        int kilobit=min/10;
        int hundred=min-10*kilobit;
        int ten=sec/10;
        int bit=sec-ten*10;

        switch(kilobit){//draw first bit
            case 0:time_kilobit.setImageLevel(0);break;
            case 1:time_kilobit.setImageLevel(1);break;
            case 2:time_kilobit.setImageLevel(2);break;
            case 3:time_kilobit.setImageLevel(3);break;
            case 4:time_kilobit.setImageLevel(4);break;
            case 5:time_kilobit.setImageLevel(5);break;
            case 6:time_kilobit.setImageLevel(6);break;
            case 7:time_kilobit.setImageLevel(7);break;
            case 8:time_kilobit.setImageLevel(8);break;
            case 9:time_kilobit.setImageLevel(9);break;
        }

        switch(hundred){//draw second bit
            case 0:time_hundred.setImageLevel(0);break;
            case 1:time_hundred.setImageLevel(1);break;
            case 2:time_hundred.setImageLevel(2);break;
            case 3:time_hundred.setImageLevel(3);break;
            case 4:time_hundred.setImageLevel(4);break;
            case 5:time_hundred.setImageLevel(5);break;
            case 6:time_hundred.setImageLevel(6);break;
            case 7:time_hundred.setImageLevel(7);break;
            case 8:time_hundred.setImageLevel(8);break;
            case 9:time_hundred.setImageLevel(9);break;
        }

        switch(ten){//draw thrid bit
            case 0:time_ten.setImageLevel(0);break;
            case 1:time_ten.setImageLevel(1);break;
            case 2:time_ten.setImageLevel(2);break;
            case 3:time_ten.setImageLevel(3);break;
            case 4:time_ten.setImageLevel(4);break;
            case 5:time_ten.setImageLevel(5);break;
            case 6:time_ten.setImageLevel(6);break;
            case 7:time_ten.setImageLevel(7);break;
            case 8:time_ten.setImageLevel(8);break;
            case 9:time_ten.setImageLevel(9);break;
        }

        switch(bit){//draw last bit
            case 0:time_bit.setImageLevel(0);break;
            case 1:time_bit.setImageLevel(1);break;
            case 2:time_bit.setImageLevel(2);break;
            case 3:time_bit.setImageLevel(3);break;
            case 4:time_bit.setImageLevel(4);break;
            case 5:time_bit.setImageLevel(5);break;
            case 6:time_bit.setImageLevel(6);break;
            case 7:time_bit.setImageLevel(7);break;
            case 8:time_bit.setImageLevel(8);break;
            case 9:time_bit.setImageLevel(9);break;
        }
    }

    //if open a cell is empty, it will auto open nears empty cell until hit a number
    private void open(int i){
        if(!visible[i]) {//only valid when this cell is not visible
            if (items[i] == 0) {//if this cell is empty, shows this cell and call recursive function nears this empty cell
                visible[i] = true;
                if(i%9!=0&&(i+1)%9!=0) {//if cell is not at left/right column
                    if(i-10>=0)
                        open(i-10);
                    if(i-9>=0)
                        open(i-9);
                    if(i-8>=0)
                        open(i-8);
                    if(i-1>=0)
                        open(i-1);
                    if(i+1<=80)
                        open(i+1);
                    if(i+8<=80)
                        open(i+8);
                    if(i+9<=80)
                        open(i+9);
                    if(i+10<=80)
                        open(i+10);
                }
                else if(i%9==0){//the cell is at left column
                    if(i-9>=0)
                        open(i-9);
                    if(i-8>=0)
                        open(i-8);
                    if(i+1<=80)
                        open(i+1);
                    if(i+9<=80)
                        open(i+9);
                    if(i+10<=80)
                        open(i+10);
                }
                else{
                    if(i-10>=0)
                        open(i-10);
                    if(i-9>=0)
                        open(i-9);
                    if(i-1>=0)
                        open(i-1);
                    if(i+8<=80)
                        open(i+8);
                    if(i+9<=80)
                        open(i+9);
                }
            }
            //if this cell is a mines, just return and do not shows this cell
            if (items[i] == 20) {
                return;
            }
            //if this cell is a number, shows this cell and do not recurse
            if (items[i] != 0) {
                visible[i] = true;
                return;
            }
        }
        return;
    }

    //check player is win or lose
    private void check(){
        //check every cells
        for(int i=0;i<=80;i++){
            if(!flag[i]&&items[i]==20){//this cell is a mine but no flag
                is_died=true;
                game_over();
                return;
            }
            if(flag[i]&&items[i]!=20){//this cell has a flag but it isn't a mine
                is_died=true;
                game_over();
                return;
            }
        }
        //give a toast let player know
        Context context = getApplicationContext();
        CharSequence text = "You Win!";
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);
        toast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
        toast.show();

        //renew best score
        for(int i=0;i<=80;i++)//shows all cells
            visible[i]=true;
        //renew best record if possible
        if(level==8&&best_b.compareTo(timer.getText().toString())>0)
            best_b=timer.getText().toString();
        if(level==25&&best_i.compareTo(timer.getText().toString())>0)
            best_i=timer.getText().toString();
        if(level==40&&best_a.compareTo(timer.getText().toString())>0)
            best_a=timer.getText().toString();

        beginner.setText("Beginner:"+best_b);
        intermediate.setText("Intermediate:"+best_i);
        advanced.setText("Advanced:"+best_a);
    }

    //popup menu for setting click
    private void showPopupMenu(View view){
        PopupMenu popupMenu=new PopupMenu(this,view);
        popupMenu.getMenuInflater().inflate(R.menu.menu,popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //once set a new level, automatically restart game and initialize everything
                if(item.getTitle().equals("Beginner"))
                    level=8;
                if(item.getTitle().equals("Intermediate"))
                    level=25;
                if(item.getTitle().equals("Advanced"))
                    level=40;
                is_died=false;
                check.setText("Check");
                start=false;
                timer.stop();
                timer.setBase(SystemClock.elapsedRealtime());
                draw_mines_count();
                for(int i=0;i<=80;i++){
                    items[i]=0;
                    visible[i]=false;
                    flag[i]=false;
                }
                gridAdapter.notifyDataSetChanged();
                return false;
            }
        });
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {

            }
        });
        popupMenu.show();
    }

    //if player is died
    private void game_over(){
        for(int i=0;i<=80;i++){//for every cell
            //if cell is flaged, but it's not a mine, shows a wrong flag sign. This is one of reason causes player died
            if(flag[i]&&items[i]!=20){
                items[i]=31;//wrong flag sign
                flag[i]=false;
                visible[i]=true;
            }
            else{
                visible[i]=true;
            }
        }
        //give a toast let player know
        Context context = getApplicationContext();
        CharSequence text = "You Died!";
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);
        toast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
        ViewGroup group=(ViewGroup) toast.getView();
        toast.show();
        check.setText("Restart");//change check button to restart button
        face.setImageLevel(1);
        start=false;
        timer.stop();
    }

    //get timer data and set to integer, used in set timer UI function
    private void getTimer(){
        String str=timer.getText().toString();
        String[] split=str.split(":");
        min=Integer.parseInt(split[0]);
        sec=Integer.parseInt(split[1]);
    }
}

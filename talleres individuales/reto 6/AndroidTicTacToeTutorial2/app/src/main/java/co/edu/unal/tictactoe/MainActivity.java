package co.edu.unal.tictactoe;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    // Buttons making up the board
    private Button boardButtons[];
    private Toast toast;
    private int selected;
    private boolean endgame=false;
    // Various text displayed
    private TextView mInfoTextView,pcScore,human,empate;
    private TicTacToeGame mGame;
    private boolean GameOver,humanStarts;
    private Integer humanSc=0,androidScore=0,empateScore=0;
    static final int DIALOG_DIFFICULTY_ID = 0;
    static final int DIALOG_QUIT_ID = 1;
    private BoardView mBoardView;
    MediaPlayer mHumanMediaPlayer;
    MediaPlayer mComputerMediaPlayer;
    private SharedPreferences mPrefs;

    @Override
    protected void onResume() {
        super.onResume();
        mHumanMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.human);
        mComputerMediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.robot);
    }
    @Override
    protected void onPause() {
        super.onPause();
        mHumanMediaPlayer.release();
        mComputerMediaPlayer.release();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_game:
                startNewGame();
                endgame=false;
                return true;
            case R.id.ai_difficulty:
                showDialog(DIALOG_DIFFICULTY_ID);
                return true;
            case R.id.quit:
                showDialog(DIALOG_QUIT_ID);
                return true;
        }
        return false;
    }



    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch (id) {
            case DIALOG_DIFFICULTY_ID:
                builder.setTitle(R.string.difficulty_choose);
                final CharSequence[] levels = {
                        getResources().getString(R.string.difficulty_easy),
                        getResources().getString(R.string.difficulty_harder),
                        getResources().getString(R.string.difficulty_expert)};
// TODO: Set selected, an integer (0 to n-1), for the Difficulty dialog.
// selected is the radio button that should be selected.
                builder.setSingleChoiceItems(levels, selected,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                dialog.dismiss(); // Close dialog
// TODO: Set the diff level of mGame based on which item was selected.
// Display the selected difficulty level
                                switch (item){
                                    case 0:

                                        mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Easy);
                                        break;

                                    case 1:
                                        mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Harder);
                                        break;

                                    case 2:
                                        mGame.setDifficultyLevel(TicTacToeGame.DifficultyLevel.Expert);
                                        break;
                                }

                                Toast.makeText(getApplicationContext(), levels[item], Toast.LENGTH_SHORT).show();

                            }
                        });
                dialog = builder.create();
                break;

            case DIALOG_QUIT_ID:
// Create the quit confirmation dialog
                builder.setMessage(R.string.quit_question)
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                MainActivity.this.resetScore();
                            }
                        })
                        .setNegativeButton(R.string.no, null);
                dialog = builder.create();
                break;
        }
        return dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGame= new TicTacToeGame();
        setContentView(R.layout.activity_main);
        boardButtons = new Button[TicTacToeGame.BOARD_SIZE];
        mBoardView = (BoardView) findViewById(R.id.board);
        mBoardView.setGame(mGame);
        // Listen for touches on the board
        mBoardView.setOnTouchListener(mTouchListener);
        mInfoTextView = (TextView) findViewById(R.id.information);
        human = (TextView) findViewById(R.id.humanScore);
        pcScore = (TextView) findViewById(R.id.pcScore);
        empate = (TextView) findViewById(R.id.empate);
        human.setText(humanSc.toString());
        pcScore.setText(androidScore.toString());
        empate.setText(empateScore.toString());
        humanStarts=true;
        toast = Toast.makeText(this,"",Toast.LENGTH_SHORT);
        selected = 0;
        mPrefs = getSharedPreferences("ttt_prefs", MODE_PRIVATE);
        humanSc = mPrefs.getInt("mHumanWins", 0);
        androidScore = mPrefs.getInt("mComputerWins", 0);
        empateScore = mPrefs.getInt("mTies", 0);
        if (savedInstanceState == null) {
            startNewGame();
        }
        else {
// Restore the game's state
            mGame.setmBoard(savedInstanceState.getCharArray("board"));
            GameOver = savedInstanceState.getBoolean("mGameOver");
            mInfoTextView.setText(savedInstanceState.getCharSequence("info"));
            humanSc = savedInstanceState.getInt("mHumanWins");
            androidScore = savedInstanceState.getInt("mComputerWins");
            empateScore = savedInstanceState.getInt("mTies");
            humanStarts = savedInstanceState.getChar("mGoFirst")=='h'?true:false;

        }
        displayScores();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharArray("board", mGame.getmBoard());
        outState.putBoolean("mGameOver", GameOver);
        outState.putInt("mHumanWins", Integer.valueOf(humanSc));
        outState.putInt("mComputerWins", Integer.valueOf(androidScore));
        outState.putInt("mTies", Integer.valueOf(empateScore));
        outState.putCharSequence("info", mInfoTextView.getText());
        outState.putChar("mGoFirst", humanStarts?'h':'a');
    }

    private void displayScores() {
        human.setText(Integer.toString(humanSc));
        pcScore.setText(Integer.toString(androidScore));
        empate.setText(Integer.toString(empateScore));
    }
    private void resetScore(){
        this.humanSc=0;
        this.androidScore=0;
        this.empateScore=0;
        empate.setText(empateScore.toString());
        human.setText(humanSc.toString());
        pcScore.setText(androidScore.toString());
    }

    @Override
    protected void onStop() {
        super.onStop();
// Save the current scores
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putInt("mHumanWins", humanSc);
        ed.putInt("mComputerWins", androidScore);
        ed.putInt("mTies", empateScore);
        ed.commit();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mGame.setmBoard(savedInstanceState.getCharArray("board"));
        GameOver = savedInstanceState.getBoolean("mGameOver");
        mInfoTextView.setText(savedInstanceState.getCharSequence("info"));
        humanSc = savedInstanceState.getInt("mHumanWins");
        androidScore = savedInstanceState.getInt("mComputerWins");
        empateScore = savedInstanceState.getInt("mTies");
        humanStarts = savedInstanceState.getChar("mGoFirst")=='h'?true:false;
    }


    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
// Determine which cell was touched
            int col = (int) event.getX() / mBoardView.getBoardCellWidth();
            int row = (int) event.getY() / mBoardView.getBoardCellHeight();
            int pos = row * 3 + col;
            if (!endgame && setMove(TicTacToeGame.HUMAN_PLAYER, pos)){
// If no winner yet, let the computer make a move
                final int[] winner = {mGame.checkForWinner()};
                if (winner[0] == 0 ) {
                    final Handler handler = new Handler();
                    mBoardView.setEnabled(false);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Do something after 5s = 5000ms
                            int move = mGame.getComputerMove();
                            setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                            mComputerMediaPlayer.start();
                            winner[0] = mGame.checkForWinner();
                            mBoardView.invalidate();
                            mBoardView.setEnabled(true);
                            if (winner[0] == 1)
                            {
                                mInfoTextView.setText("It's a tie!");
                                GameOver=true;
                                empateScore++;
                                empate.setText(empateScore.toString());
                                endgame=true;
                            }

                        }
                    }, 2000);
                    mInfoTextView.setText("It's Android's turn.");





                }
                if (winner[0] == 0) {
                    mInfoTextView.setText("It's your turn.");
                    mHumanMediaPlayer.start();
                }
                else if (winner[0] == 1)
                {
                    mInfoTextView.setText("It's a tie!");
                    GameOver=true;
                    empateScore++;
                    empate.setText(empateScore.toString());
                    endgame=true;
                }

                else if (winner[0] == 2) {
                    mInfoTextView.setText("You won!");
                    GameOver = true;
                    humanSc++;
                    human.setText(humanSc.toString());
                    endgame=true;
                }
                else {
                    mInfoTextView.setText("Android won!");
                    GameOver = true;
                    androidScore++;
                    pcScore.setText(androidScore.toString());
                    endgame=true;
                }
            }
// So we aren't notified of continued events when finger is moved
            return false;
        }
    };

    private void startNewGame() {
        mGame.clearBoard();
        mBoardView.invalidate();
        this.GameOver = false;
        if(humanStarts) {
            mInfoTextView.setText("you go first.");
            humanStarts=!humanStarts;
        }
        else {
            mInfoTextView.setText("android go first.");
            int move = mGame.getComputerMove();
            setMove(TicTacToeGame.COMPUTER_PLAYER, move);
            humanStarts=!humanStarts;
        }
    }
    private class ButtonClickListener implements View.OnClickListener {
        int location;
        public ButtonClickListener(int location) {
            this.location = location;
        }
        public void onClick(View view) {
            if (boardButtons[location].isEnabled()&& !endgame) {
                setMove(TicTacToeGame.HUMAN_PLAYER, location);
                // If no winner yet, let the computer make a move
                int winner = mGame.checkForWinner();

                if (winner == 0 ) {

                    mInfoTextView.setText("It's Android's turn.");
                    int move = mGame.getComputerMove();
                    setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                    winner = mGame.checkForWinner();

                }
                if (winner == 0) {
                    mInfoTextView.setText("It's your turn.");
                }
                else if (winner == 1)
                {
                    mInfoTextView.setText("It's a tie!");
                    GameOver=true;
                    empateScore++;
                    empate.setText(empateScore.toString());
                    endgame=true;
                }

                else if (winner == 2) {
                    mInfoTextView.setText("You won!");
                    GameOver = true;
                    humanSc++;
                    human.setText(humanSc.toString());
                    endgame=true;
                }
                else {
                    mInfoTextView.setText("Android won!");
                    GameOver = true;
                    androidScore++;
                    pcScore.setText(androidScore.toString());
                    endgame=true;
                }
            }
        }
    }

    private boolean setMove(char player, int location) {
        if (mGame.setMove(player, location)) {
            mBoardView.invalidate(); // Redraw the board
            return true;
        }
        return false;
    }
}











package group.cs242.beatblocks;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class GameActivity extends AppCompatActivity {

    private static final String PREF_FILE_NAME = "PreferencesFile";

    // The view to be drawn to the screen
    private BeatBlockBoardView beatBlockBoardView;

    private TextView highScore;
    private ImageButton imgButton;

    //requirements for the whole BeatMap
    private BeatMapView beatMapView; //Work on trimming down possibly
    private Song song;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Get the device dimensions.
        Point p = new Point();
        getWindowManager().getDefaultDisplay().getSize(p);

        song = new Song(getApplicationContext(), 90);


        // Inflate the layout.
        LayoutInflater inflater = LayoutInflater.from(this);
        View mainLayout = inflater.inflate(R.layout.game_activity, null);

        SharedPreferences preferences = getSharedPreferences(PREF_FILE_NAME, 0);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        if (!preferences.contains("highScore")) {
            preferencesEditor.putString("highScore", "High Score: 0");
            preferencesEditor.commit();
        }
        highScore = (TextView) mainLayout.findViewById(R.id.high_score);
        highScore.setText(preferences.getString("highScore", "High Score: 0"));

        // Retrieve and setup the beatBlockBoardView.
        beatBlockBoardView = (BeatBlockBoardView)mainLayout.findViewById(R.id.beat_block_board_view);
        beatBlockBoardView.setOnTouchListener(new MoveGestureListener(this));
        beatBlockBoardView.setDimensions(p.x, p.y);
        beatBlockBoardView.setScore((TextView) mainLayout.findViewById(R.id.score));
        beatBlockBoardView.setHighScore(highScore);

        beatMapView = (BeatMapView)mainLayout.findViewById(R.id.beat_map_view);
        beatMapView.setUp(song);

        // Set the content as the layout.
        setContentView(mainLayout);
        //Set the title
        TextView title = (TextView) mainLayout.findViewById(R.id.title);
        title.setText("Beat Blocks");
        // Retrieve, scale, and set the on click function of the image button.
        imgButton = (ImageButton) mainLayout.findViewById(R.id.pauseButton);
        imgButton.setScaleX(2);
        imgButton.setScaleY(2);
        imgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beatBlockBoardView.togglePause();
                beatMapView.togglePause();
                if (beatBlockBoardView.isPaused()) {
                    imgButton.setImageResource(R.mipmap.play);
                } else {
                    imgButton.setImageResource(R.mipmap.pause);
                }
            }
        });

        // Retrieve and scale the image.
        ImageView img = (ImageView) mainLayout.findViewById(R.id.gameIcon);
        img.setScaleX(2);
        img.setScaleY(2);

        beatMapView.run();
    }

    /**
     * When leaving the application pause the game.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (!beatBlockBoardView.isPaused()) {
            beatBlockBoardView.togglePause();
            beatMapView.togglePause();
            imgButton.setImageResource(R.mipmap.play);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences preferences = getSharedPreferences(PREF_FILE_NAME, 0);
        SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putString("highScore", highScore.getText().toString());
        preferencesEditor.commit();
    }

    /**
     * Handles touch events for the game view.
     */
    class MoveGestureListener extends GestureDetector.SimpleOnGestureListener implements View.OnTouchListener {

        /* Data members */

        private GestureDetector gd;
        Context context;

        /* Constructors */

        public MoveGestureListener() {
            super();
        }

        public MoveGestureListener(Context context) {
            this(context, null);
        }

        public MoveGestureListener(Context context, GestureDetector gd) {
            if (gd == null) {
                this.gd = new GestureDetector(context, this);
            }
            this.context = context;
        }

        /* public methods */

        /**
         *  Moves blocks according to the direction and magnitude of the fling event.
         *
         * @param e1 -  The initial event.
         * @param e2 - The ending event.
         * @param velX - x velocity
         * @param velY - y velocity
         * @return true.
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velX, float velY) {
            if (beatBlockBoardView.isPaused()) {
                return false;
            }
            int blockSize = beatBlockBoardView.getBlockDimensions();
            // The index to move is the initial events x/y divided by the dimensions of the square.
            Index moveIndex = new Index((int)e1.getX()/blockSize, (int)e1.getY()/blockSize);
            // Move in the appropriate direction if the user swiped a distance of 100 px.
            //if(beatMapView.isGoodMove()) {
                if (Math.abs(e1.getX() - e2.getX()) >= Math.abs(e1.getY() - e2.getY())) {
                    if (e1.getX() - e2.getX() <= -blockSize / 2) {
                        beatBlockBoardView.moveBlockRight(moveIndex);
                    } else if (e1.getX() - e2.getX() >= blockSize / 2) {
                        beatBlockBoardView.moveBlockLeft(moveIndex);
                    }
                } else {
                    if (e1.getY() - e2.getY() >= blockSize / 2) {
                        beatBlockBoardView.moveBlockUp(moveIndex);
                    } else if (e1.getY() - e2.getY() <= -blockSize / 2) {
                        beatBlockBoardView.moveBlockDown(moveIndex);
                    }
                }
                return true;
            //} else {
               // Log.i("Move", "Not good");
           // }
            //return false;
        }



        /**
         * Needs to return true because a fling is dependant on this event.
         *
         * @param e - An event.
         * @return true.
         */
        @Override
        public boolean onDown(MotionEvent e) {

            return true;
        }

        /**
         *
         * @param v - the attached view.
         * @param e - An event.
         * @return a boolean.
         */
        @Override
        public boolean onTouch(View v, MotionEvent e) {

            return gd.onTouchEvent(e);
        }
    }
}

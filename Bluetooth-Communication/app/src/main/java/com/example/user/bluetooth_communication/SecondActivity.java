package com.example.user.bluetooth_communication;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by marrj on 03.04.2018.
 */

public class SecondActivity extends AppCompatActivity {

    private static int whichImage;
    private static int correct;
    private static List<Integer> allImages;
    private static boolean enemyFinished;
    private static boolean youFinished;
    private static boolean enemySurrended;
    static {
        whichImage = 1;
        correct = 0;
        allImages = new ArrayList<>();
        allImages.add(R.drawable.hangman0);
        allImages.add(R.drawable.hangman00);
        allImages.add(R.drawable.hangman1);
        allImages.add(R.drawable.hangman2);
        allImages.add(R.drawable.hangman3);
        allImages.add(R.drawable.hangman4);
        allImages.add(R.drawable.hangman5);
        allImages.add(R.drawable.hangman6);
        allImages.add(R.drawable.hangman7);
    }

    private final String TAG = "Rozpoczęto grę";
    EditText etSend;
    BluetoothDevice mBluetoothDevice;
    BluetoothConnectionService mBluetoothConnection;

    TextView incomingMessages;
    StringBuilder messages;
    ImageView image;
    ImageView imageEnemy;

    TextView passwordView;
    private Button a,aa,b,c,cc,d,e,ee,f,g,h,i,j,k,l,ll,m,n,nn,o,oo,p,q,r,s,ss,t,u,v,w,x,y,z,zz,zzz,again;
    public String password;
    private String passwordShow;
    char[] charArray;

    // lista przycisków, żeby szybciej nimi zarządzać
    Button[] alphabet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        Intent intent = getIntent();
        mBluetoothDevice = intent.getParcelableExtra("mBluetoothDevice");
        mBluetoothConnection = MainActivity.getmBluetoothConnection();
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("incomingMessage"));



        Log.d(TAG, "onCreate: started.");
        passwordView = (TextView) findViewById(R.id.hasloView);
        //setPassword("default");
        //passwordView.setText(passwordShow);
        image = (ImageView) findViewById(R.id.imageView);
        imageEnemy = (ImageView) findViewById(R.id.imageView2);

        // ustawianie literek
        alphabet = new Button[35];
        setButtons();

        etSend = (EditText) findViewById(R.id.editText);
        incomingMessages = (TextView) findViewById(R.id.textView);
        messages = new StringBuilder();

        enemyFinished = false;
        youFinished = false;
        whichImage = 1;
        correct = 0;
        enemySurrended = false;

        again = (Button) findViewById(R.id.again);
        again.setVisibility(View.GONE);
        again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recreate();
                etSend.setText("");
                // TODO: Zablokować możliwość wysyłania hasła, póki drugi użytkownik się nie podłączy
            }
        });

        final Button btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    byte[] bytes = etSend.getText().toString().getBytes(Charset.defaultCharset());
                    mBluetoothConnection.write(bytes);
                    EditText editText = (EditText) findViewById(R.id.editText);
                    editText.setVisibility(View.GONE);
                    btnSend.setVisibility(View.GONE);
                    imageEnemy.setVisibility(View.VISIBLE);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    //if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    //}
                    btnSend.setOnClickListener(null);
                } catch (NullPointerException e) {
                    Toast.makeText(getBaseContext(), R.string.again, Toast.LENGTH_LONG);
                }
            }
        });
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("theMessage");
            try {
                String [] values = text.split("\\D+");
                int correctEnemy = Integer.parseInt(values[0]);
                int whichImage = Integer.parseInt(values[1]);
                imageEnemy.setImageResource(allImages.get(whichImage));

                if (correctEnemy==2000) {
                    incomingMessages.setText("Twój przeciwnik właśnie został powieszony!");
                    enemyFinished = true;
                }
                else if (correctEnemy==1996) {
                    incomingMessages.setText("Twój przeciwnik właśnie odgadł hasło!");
                    enemyFinished = true;
                }
                else {
                    incomingMessages.setText("Ilość liter odgadniętych przez przeciwnika: "+correctEnemy);
                }
                if (enemyFinished && youFinished) {
                    again.setVisibility(View.VISIBLE);
                }

            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                Log.d(TAG, "This time it's not an integer");
                setPassword(text);
                enableButtons();
            }
        }
    };

    private void setPassword(String pass) {
        password = pass;
        passwordShow = password.toUpperCase().replaceAll("[A-Ż]","_ ");
        passwordView.setText(passwordShow);
        charArray = password.toUpperCase().toCharArray();
    }

    private char[] checkButton(Button button) {
        String buttonString = button.getText().toString();
        char[] charShow = passwordShow.toCharArray();
        char buttonChar= buttonString.charAt(0);
        String message = "";
        boolean contains = false;
        for (int i = 0; i < charArray.length; i++) {
            if(charArray[i]==buttonChar){
                charShow[i*2] = buttonChar;
                correct++;
                contains = true;
                if (correct==password.length()) {
                    message = "1996"+";"+String.valueOf(whichImage);
                    disableButtons();
                    image.setImageResource(R.drawable.winnerpl);
                    youFinished = true;
                    if (enemyFinished) again.setVisibility(View.VISIBLE);
                }
                else message = String.valueOf(correct)+";"+String.valueOf(whichImage);
            }
        }
        if (!contains) {
            whichImage++;
            image.setImageResource(allImages.get(whichImage));
            if (whichImage==allImages.size()-1) {
                message = "2000"+";"+String.valueOf(whichImage);
                disableButtons();
                youFinished = true;
                if (enemyFinished) again.setVisibility(View.VISIBLE);
            }
            else message = String.valueOf(correct)+";"+String.valueOf(whichImage);
        }

        byte[] bytes = message.getBytes(Charset.defaultCharset());
        mBluetoothConnection.write(bytes);
        return charShow;
    }

    //metoda do sprawdzania przycisków i czyszczenia ich.
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void checkButtonAndSend(View view){
        Button button = (Button) view;
        char[] charShow = checkButton(button);
        button.setOnClickListener(null);
        String previousPassword = passwordShow;
        passwordShow = new String(charShow);
        passwordView.setText(passwordShow);

        if(!previousPassword.equals(passwordShow)){
            button.setBackgroundTintList(ColorStateList.valueOf(0xFF4CAF50));
        }else {
            button.setBackgroundTintList(ColorStateList.valueOf(0xfff44336));
        }

        if (whichImage==allImages.size()-1) {
            char[] tmp = password.toUpperCase().toCharArray();
            StringBuilder correctAnswer = new StringBuilder("");
            for (char c: tmp) {
                correctAnswer.append(c + " ");
            }
            passwordView.setText(correctAnswer.toString());
        }
    }

    private void setButtons(){
        a=(Button) findViewById(R.id.a);
        aa=(Button) findViewById(R.id.aa);
        b=(Button) findViewById(R.id.b);
        c=(Button) findViewById(R.id.c);
        cc=(Button) findViewById(R.id.cc);
        d=(Button) findViewById(R.id.d);
        e=(Button) findViewById(R.id.e);
        ee=(Button) findViewById(R.id.ee);
        f=(Button) findViewById(R.id.f);
        g=(Button) findViewById(R.id.g);
        h=(Button) findViewById(R.id.h);
        i=(Button) findViewById(R.id.i);
        j=(Button) findViewById(R.id.j);
        k=(Button) findViewById(R.id.k);
        l=(Button) findViewById(R.id.l);
        ll=(Button) findViewById(R.id.ll);
        m=(Button) findViewById(R.id.m);
        n=(Button) findViewById(R.id.n);
        nn=(Button) findViewById(R.id.nn);
        o=(Button) findViewById(R.id.o);
        oo=(Button) findViewById(R.id.oo);
        p=(Button) findViewById(R.id.p);
        q=(Button) findViewById(R.id.q);
        r=(Button) findViewById(R.id.r);
        s=(Button) findViewById(R.id.s);
        ss=(Button) findViewById(R.id.ss);
        t=(Button) findViewById(R.id.t);
        u=(Button) findViewById(R.id.u);
        v=(Button) findViewById(R.id.v);
        w=(Button) findViewById(R.id.w);
        x=(Button) findViewById(R.id.x);
        y=(Button) findViewById(R.id.y);
        z=(Button) findViewById(R.id.z);
        zz=(Button) findViewById(R.id.zz);
        zzz=(Button) findViewById(R.id.zzz);

        alphabet = new Button[] {a,aa,b,c,cc,d,e,ee,f,g,h,i,j,k,l,ll,m,n,nn,o,oo,p,q,r,s,ss,t,u,v,w,x,y,z,zz,zzz};

    }

    private void enableButtons(){
        for (Button button : alphabet) {
            button.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onClick(View view) {
                    checkButtonAndSend(view);
                }
            });
        }
    }

    private void disableButtons() {
        for (Button button : alphabet) {
            button.setOnClickListener(null);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // prześlij przez BT info że się poddał i w Broadcast Receiver zmień enemySurrended na true
    }
}

// TODO: Podkreślić wybrany element ListView albo jakoś inaczej zaznaczyć wybrany telefon
// TODO: Sprawdzić, czy połączenie jest stabilne ZANIM zmieni się aktywność
// TODO: Konsekwentne czcionki w polach, jakaś estetyczna kosmetyka
// TODO: Obsługa błędów, np. zerwane połączenie, bo użytkownik wyłączył aplikację, wysłanie pustego hasła
// TODO: Obsługa haseł ze spacjami
